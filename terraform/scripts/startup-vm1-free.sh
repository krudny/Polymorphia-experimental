#!/bin/bash
set -euo pipefail
export DEBIAN_FRONTEND=noninteractive

# Konfiguracja partycji SWAP 3 GB dla ochrony 1 GB RAM
SWAPFILE="/swapfile"
if [ ! -f "${SWAPFILE}" ]; then
  fallocate -l 3G "${SWAPFILE}" || dd if=/dev/zero of="${SWAPFILE}" bs=1M count=3072
  chmod 600 "${SWAPFILE}"
  mkswap "${SWAPFILE}"
  swapon "${SWAPFILE}"
  echo "${SWAPFILE} none swap sw 0 0" >> /etc/fstab
fi
sysctl -w vm.swappiness=10

# Czekanie na zwolnienie blokad pakietów
while fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1; do sleep 3; done

# Pakiety podstawowe
apt-get update
apt-get install -y curl ca-certificates gnupg lsb-release jq debian-keyring debian-archive-keyring apt-transport-https

# Instalacja PostgreSQL 17
if ! command -v psql &>/dev/null; then
  install -d /usr/share/postgresql-common/pgdg
  curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc --fail https://www.postgresql.org/media/keys/ACCC4CF8.asc
  echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list
  apt-get update
  apt-get install -y postgresql-17
fi

# Instalacja Docker
if ! command -v docker &>/dev/null; then
  curl -fsSL https://get.docker.com -o get-docker.sh
  sh get-docker.sh
  rm get-docker.sh
fi

# Konfiguracja limitow logow Dockera (ochrona przed zapelnieniem dysku)
mkdir -p /etc/docker
cat > /etc/docker/daemon.json <<'EOF'
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "30m",
    "max-file": "3"
  }
}
EOF
systemctl restart docker

# Konfiguracja limitu dziennika systemd (ochrona przed zapelnieniem partycji glownej)
mkdir -p /etc/systemd/journald.conf.d
cat > /etc/systemd/journald.conf.d/size-limit.conf <<'EOF'
[Journal]
SystemMaxUse=200M
SystemMaxFileSize=50M
MaxRetentionSec=7day
EOF
systemctl restart systemd-journald

# Okresowe czyszczenie nieuzywanych zasobow Dockera
cat > /etc/cron.daily/docker-prune <<'EOF'
#!/bin/bash
docker system prune -af --filter "until=168h" --volumes=false >/dev/null 2>&1 || true
EOF
chmod +x /etc/cron.daily/docker-prune

# Konfiguracja uprawnien uzytkownika SSH do Dockera i sudo
if ! id -u k_rudny1 >/dev/null 2>&1; then
  useradd -m -s /bin/bash k_rudny1
fi
usermod -aG docker k_rudny1
echo "k_rudny1 ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/90-k_rudny1
chmod 0440 /etc/sudoers.d/90-k_rudny1

# Instalacja Caddy
if ! command -v caddy &>/dev/null; then
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | tee /etc/apt/sources.list.d/caddy-stable.list
  apt-get update
  apt-get install -y caddy
fi

# Konfiguracja sieciowa PostgreSQL dla polaczen z kontenerow (172.17.0.1)
sed -i "s|^#listen_addresses.*|listen_addresses = '*'|" /etc/postgresql/17/main/postgresql.conf
sed -i "s|^listen_addresses.*|listen_addresses = '*'|" /etc/postgresql/17/main/postgresql.conf
if ! grep -q "172.17.0.0/16" /etc/postgresql/17/main/pg_hba.conf; then
  echo "host all all 172.17.0.0/16 md5" >> /etc/postgresql/17/main/pg_hba.conf
fi
systemctl restart postgresql

# Utworzenie baz danych develop i staging oraz nadanie uprawnien do schematu public
sudo -u postgres psql <<'EOF'
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'polymorphia_user') THEN
    CREATE USER polymorphia_user WITH PASSWORD 'polymorphia_pass_dev';
  END IF;
END $$;
SELECT 'CREATE DATABASE polymorphia_dev OWNER polymorphia_user' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'polymorphia_dev')\gexec
SELECT 'CREATE DATABASE polymorphia_staging OWNER polymorphia_user' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'polymorphia_staging')\gexec
GRANT ALL PRIVILEGES ON DATABASE polymorphia_dev TO polymorphia_user;
GRANT ALL PRIVILEGES ON DATABASE polymorphia_staging TO polymorphia_user;
\c polymorphia_dev
GRANT ALL ON SCHEMA public TO polymorphia_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO polymorphia_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO polymorphia_user;
\c polymorphia_staging
GRANT ALL ON SCHEMA public TO polymorphia_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO polymorphia_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO polymorphia_user;
EOF

# Utworzenie katalogow konfiguracji
mkdir -p /mnt/data/app-config-develop /mnt/data/app-config-staging /mnt/data/static

# Pobranie konfiguracji z Google Cloud Secret Manager (zabezpieczenie przed wyczyszczeniem pliku w razie bledu)
if gcloud secrets versions access latest --secret=backend-properties-develop > /tmp/develop.props.tmp && [ -s /tmp/develop.props.tmp ]; then
  mv /tmp/develop.props.tmp /mnt/data/app-config-develop/application.properties
  chmod 644 /mnt/data/app-config-develop/application.properties
fi

if gcloud secrets versions access latest --secret=backend-properties-staging > /tmp/staging.props.tmp && [ -s /tmp/staging.props.tmp ]; then
  mv /tmp/staging.props.tmp /mnt/data/app-config-staging/application.properties
  chmod 644 /mnt/data/app-config-staging/application.properties
fi

# Konfiguracja Caddy
cat > /etc/caddy/Caddyfile <<'EOF'
develop.polymorphia.pl {
  reverse_proxy localhost:8082
}

staging.polymorphia.pl {
  reverse_proxy localhost:8081
}
EOF

systemctl enable caddy
systemctl restart caddy
