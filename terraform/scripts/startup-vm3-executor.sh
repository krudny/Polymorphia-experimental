#!/bin/bash
set -euo pipefail
export DEBIAN_FRONTEND=noninteractive

# SWAP 2 GB na boot dysku dla ochrony 1 GB RAM (e2-micro)
if [ ! -f /swapfile ]; then
  fallocate -l 2G /swapfile || dd if=/dev/zero of=/swapfile bs=1M count=2048
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo "/swapfile none swap sw 0 0" >> /etc/fstab
fi
sysctl -w vm.swappiness=10

# Czekanie na zwolnienie blokad pakietow
while fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1; do sleep 3; done

# Instalacja pakietow podstawowych
apt-get update
apt-get install -y curl ca-certificates gnupg lsb-release jq debian-keyring debian-archive-keyring apt-transport-https

# Instalacja Java 25 (Adoptium Temurin)
if ! command -v java &>/dev/null; then
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
  echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" > /etc/apt/sources.list.d/adoptium.list
  apt-get update
  apt-get install -y temurin-25-jdk
fi

# Instalacja Docker
if ! command -v docker &>/dev/null; then
  curl -fsSL https://get.docker.com -o get-docker.sh
  sh get-docker.sh
  rm get-docker.sh
fi

# Konfiguracja limitow logow Dockera
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

# Konfiguracja limitu dziennika systemd dla executora (ochrona boot dysku)
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

# Instalacja Cloudflare Tunnel (cloudflared)
if ! command -v cloudflared &>/dev/null; then
  curl -L --output cloudflared.deb https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb
  dpkg -i cloudflared.deb
  rm cloudflared.deb
fi

# Pobranie tokenu tunelu z metadanych instancji i uruchomienie serwisu
TUNNEL_TOKEN=$(curl -s "http://metadata.google.internal/computeMetadata/v1/instance/attributes/cloudflare-tunnel-token" -H "Metadata-Flavor: Google")
if [ -n "${TUNNEL_TOKEN}" ]; then
  if [ ! -f /etc/systemd/system/cloudflared.service ]; then
    cloudflared service install "${TUNNEL_TOKEN}"
  fi
  systemctl enable cloudflared
  systemctl restart cloudflared
fi

# Przygotowanie katalogu aplikacji executora
mkdir -p /mnt/data/executor/config
touch /mnt/data/executor/config/application.properties
mkdir -p /opt
ln -sfn /mnt/data/executor /opt/polymorphia-executor
chown -R root:root /mnt/data/executor

# Pobranie konfiguracji z Secret Managera (atomowy zapis)
if gcloud secrets versions access latest --secret=executor-properties-dev > /tmp/exec_dev.props.tmp && [ -s /tmp/exec_dev.props.tmp ]; then
  mv /tmp/exec_dev.props.tmp /mnt/data/executor/config/application.properties
  chmod 640 /mnt/data/executor/config/application.properties
fi

# Konfiguracja serwisu systemd dla executora (z limitem pamieci dla 1 GB RAM)
cat > /etc/systemd/system/polymorphia-code-executor.service <<'EOF'
[Unit]
Description=Polymorphia Code Executor
After=network.target docker.service

[Service]
Type=simple
User=root
WorkingDirectory=/mnt/data/executor
ExecStart=/usr/bin/java -Xms128m -Xmx256m -jar /mnt/data/executor/executor.jar --spring.config.location=optional:file:/mnt/data/executor/config/application.properties
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
