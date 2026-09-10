#!/bin/bash
set -euo pipefail
export DEBIAN_FRONTEND=noninteractive

DATA_DISK_DEV="/dev/disk/by-id/google-polymorphia-data-disk"
DATA_MOUNT="/mnt/data"
POSTGRES_DATA="${DATA_MOUNT}/postgres"
APP_CONFIG="${DATA_MOUNT}/app-config-production"
STATIC_DATA="${DATA_MOUNT}/static"
EXECUTOR_DIR="${DATA_MOUNT}/executor"

# SWAP 3 GB
if [ ! -f /swapfile ]; then
  fallocate -l 3G /swapfile || dd if=/dev/zero of=/swapfile bs=1M count=3072
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo "/swapfile none swap sw 0 0" >> /etc/fstab
fi
sysctl -w vm.swappiness=10

# Oczekiwanie na dysk trwaly ze stanu MIG
while [ ! -e "${DATA_DISK_DEV}" ]; do
  sleep 2
done

mkdir -p "${DATA_MOUNT}"
if ! mountpoint -q "${DATA_MOUNT}"; then
  # Bezpieczne sprawdzenie systemu plikow przed formatowaniem (ochrona istniejacych danych)
  FSTYPE=$(lsblk -no FSTYPE "${DATA_DISK_DEV}" 2>/dev/null || true)
  if [ -z "${FSTYPE}" ]; then
    if ! blkid "${DATA_DISK_DEV}" >/dev/null 2>&1; then
      echo "Inicjalizacja nowego dysku danych..."
      mkfs.ext4 -m 0 -F -E lazy_itable_init=0,lazy_journal_init=0,discard "${DATA_DISK_DEV}"
    fi
  fi
  mount -o discard,defaults "${DATA_DISK_DEV}" "${DATA_MOUNT}"
  if ! grep -q "${DATA_DISK_DEV}" /etc/fstab; then
    echo "${DATA_DISK_DEV} ${DATA_MOUNT} ext4 discard,defaults,nofail,_netdev 0 2" >> /etc/fstab
  fi
fi

# Twarde zaleznosci montowania dysku dla bazy i Dockera w systemd
mkdir -p /etc/systemd/system/postgresql.service.d /etc/systemd/system/docker.service.d
cat > /etc/systemd/system/postgresql.service.d/override.conf <<'EOF'
[Unit]
RequiresMountsFor=/mnt/data
EOF
cat > /etc/systemd/system/docker.service.d/override.conf <<'EOF'
[Unit]
RequiresMountsFor=/mnt/data
EOF
systemctl daemon-reload

mkdir -p "${POSTGRES_DATA}" "${APP_CONFIG}" "${STATIC_DATA}" "${EXECUTOR_DIR}"

# Czekanie na zwolnienie blokad pakietow
while fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1; do sleep 3; done

# Kopia i przywracanie kluczy hosta SSH (ochrona przed ostrzezeniem o zmianie klucza hosta)
SSH_KEYS_BACKUP="/mnt/data/ssh-host-keys"
mkdir -p "${SSH_KEYS_BACKUP}"
if [ -f "${SSH_KEYS_BACKUP}/ssh_host_ed25519_key" ]; then
  cp ${SSH_KEYS_BACKUP}/ssh_host_* /etc/ssh/
  chmod 600 /etc/ssh/ssh_host_*_key
  chmod 644 /etc/ssh/ssh_host_*_key.pub
  systemctl restart sshd
else
  cp /etc/ssh/ssh_host_* "${SSH_KEYS_BACKUP}/"
fi

apt-get update
apt-get install -y \
  curl ca-certificates gnupg lsb-release jq \
  debian-keyring debian-archive-keyring apt-transport-https \
  git build-essential libprotobuf-dev libnl-route-3-dev protobuf-compiler \
  libseccomp-dev flex bison pkg-config

# Instalacja Docker (przed dodaniem uzytkownika do grupy docker)
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
    "max-size": "50m",
    "max-file": "3"
  }
}
EOF
systemctl restart docker

# Konfiguracja limitu dziennika systemd dla executora i uslug systemowych
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

# Uzytkownik SSH, uprawnienia do Dockera i sudo
if ! id -u k_rudny1 >/dev/null 2>&1; then
  useradd -m -s /bin/bash k_rudny1
fi
usermod -aG docker k_rudny1
echo "k_rudny1 ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/90-k_rudny1
chmod 0440 /etc/sudoers.d/90-k_rudny1

# Instalacja PostgreSQL 17
if ! command -v psql &>/dev/null; then
  install -d /usr/share/postgresql-common/pgdg
  curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc --fail https://www.postgresql.org/media/keys/ACCC4CF8.asc
  echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list
  apt-get update
  apt-get install -y postgresql-17
fi

# Inicjalizacja i weryfikacja wlasnosci katalogu bazy na dysku trwalym
systemctl stop postgresql || true
chown -R postgres:postgres "${POSTGRES_DATA}"
chmod 0700 "${POSTGRES_DATA}"
if [ ! -f "${POSTGRES_DATA}/PG_VERSION" ]; then
  sudo -u postgres /usr/lib/postgresql/17/bin/initdb -D "${POSTGRES_DATA}"
fi

sed -i "s|^data_directory.*|data_directory = '${POSTGRES_DATA}'|" /etc/postgresql/17/main/postgresql.conf
sed -i "s|^#listen_addresses.*|listen_addresses = '*'|" /etc/postgresql/17/main/postgresql.conf
sed -i "s|^listen_addresses.*|listen_addresses = '*'|" /etc/postgresql/17/main/postgresql.conf
if ! grep -q "172.17.0.0/16" /etc/postgresql/17/main/pg_hba.conf; then
  echo "host all all 172.17.0.0/16 md5" >> /etc/postgresql/17/main/pg_hba.conf
fi

systemctl enable postgresql
systemctl start postgresql

# Baza danych produkcyjna i uprawnienia (konto bez uprawnien SUPERUSER dla bezpieczenstwa)
sudo -u postgres psql <<'EOF'
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'admin') THEN
    CREATE USER admin WITH PASSWORD 'admin-polymorphia';
  END IF;
END $$;
SELECT 'CREATE DATABASE polymorphia OWNER admin' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'polymorphia')\gexec
GRANT ALL PRIVILEGES ON DATABASE polymorphia TO admin;
\c polymorphia
GRANT ALL ON SCHEMA public TO admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO admin;
EOF

# Instalacja Java 25 Adoptium dla lokalnego executora
if ! command -v java &>/dev/null; then
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
  echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" > /etc/apt/sources.list.d/adoptium.list
  apt-get update
  apt-get install -y temurin-25-jdk
fi

# Kompilacja nsjail
if ! command -v nsjail &>/dev/null; then
  git clone https://github.com/google/nsjail.git /tmp/nsjail
  cd /tmp/nsjail
  make -j"$(nproc)"
  cp nsjail /usr/local/bin/
  cd /
  rm -rf /tmp/nsjail
fi

# Konfiguracja serwisu polymorphia-code-executor na produkcji (konfiguracja na trwalym dysku)
mkdir -p "${EXECUTOR_DIR}/config"
if gcloud secrets versions access latest --secret=executor-properties-production > /tmp/exec.props.tmp && [ -s /tmp/exec.props.tmp ]; then
  mv /tmp/exec.props.tmp "${EXECUTOR_DIR}/config/application.properties"
  chmod 640 "${EXECUTOR_DIR}/config/application.properties"
fi

cat > /etc/systemd/system/polymorphia-code-executor.service <<'EOF'
[Unit]
Description=Polymorphia Code Executor (Production)
After=network.target docker.service
RequiresMountsFor=/mnt/data

[Service]
Type=simple
User=root
WorkingDirectory=/mnt/data/executor
ExecStart=/usr/bin/java -Xms128m -Xmx256m -jar /mnt/data/executor/executor.jar --spring.config.location=file:/mnt/data/executor/config/application.properties
Restart=always
RestartSec=5
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable polymorphia-code-executor
if [ -f /mnt/data/executor/executor.jar ]; then
  systemctl restart polymorphia-code-executor
fi

# Instalacja Caddy
if ! command -v caddy &>/dev/null; then
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | tee /etc/apt/sources.list.d/caddy-stable.list
  apt-get update
  apt-get install -y caddy
fi

# Trwaly magazyn certyfikatow Caddy (ochrona przed limitami Let's Encrypt)
CADDY_DATA="/mnt/data/caddy"
mkdir -p "${CADDY_DATA}/data" "${CADDY_DATA}/config"
chown -R caddy:caddy "${CADDY_DATA}"
mkdir -p /etc/systemd/system/caddy.service.d
cat > /etc/systemd/system/caddy.service.d/override.conf <<EOF
[Service]
Environment="XDG_DATA_HOME=${CADDY_DATA}/data"
Environment="XDG_CONFIG_HOME=${CADDY_DATA}/config"
EOF
systemctl daemon-reload

# Pobranie pliku konfiguracyjnego backendu z Secret Manager (atomowy zapis)
if gcloud secrets versions access latest --secret=backend-properties-production > /tmp/prod.props.tmp && [ -s /tmp/prod.props.tmp ]; then
  mv /tmp/prod.props.tmp "${APP_CONFIG}/application.properties"
  chmod 644 "${APP_CONFIG}/application.properties"
fi

# Konfiguracja Caddy
cat > /etc/caddy/Caddyfile <<'EOF'
production.polymorphia.pl {
  reverse_proxy localhost:8080
}
EOF
systemctl enable caddy
systemctl restart caddy

# Uruchomienie kontenera produkcyjnego backendu po starcie/resecie maszyny (jesli obraz istnieje)
IMAGE="europe-west1-docker.pkg.dev/polymorphia-b52b06/polymorphia/polymorphia-backend:latest-production"
gcloud auth configure-docker europe-west1-docker.pkg.dev --quiet || true

if docker image inspect "${IMAGE}" >/dev/null 2>&1 || docker pull "${IMAGE}" 2>/dev/null; then
  # Jesli kontener juz istnieje, usuwamy go aby zawsze wystartowal z najnowszego pobranego obrazu
  if docker ps -aq --filter "name=^polymorphia-backend-production$" | grep -q .; then
    docker stop polymorphia-backend-production 2>/dev/null || true
    docker rm polymorphia-backend-production 2>/dev/null || true
  fi

  docker run -d \
    --name "polymorphia-backend-production" \
    --restart unless-stopped \
    --log-opt max-size=50m \
    --log-opt max-file=3 \
    -p 8080:8080 \
    -v "${APP_CONFIG}":/app/config:ro \
    -v "${STATIC_DATA}":/app/static \
    "${IMAGE}" \
    --spring.config.location=file:/app/config/application.properties
else
  echo "Obraz ${IMAGE} nie jest jeszcze dostepny w Artifact Registry. Kontener zostanie uruchomiony podczas pierwszego wdrozenia CI/CD."
fi
