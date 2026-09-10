#!/bin/bash
set -x

CONTAINER="polymorphia-backend-production"

# Bezpieczne zatrzymanie serwisu executora (zwalnia otwarte pliki na /mnt/data/executor)
systemctl stop polymorphia-code-executor 2>/dev/null || true

# Bezpieczne zatrzymanie kontenera bez niszczenia flagi unless-stopped
docker stop --time 10 "${CONTAINER}" 2>/dev/null || true

# Zatrzymanie serwera Caddy
systemctl stop caddy 2>/dev/null || true

# Czyste zamkniecie PostgreSQL przez systemd (wykonuje checkpoint i zwalnia zasoby)
systemctl stop postgresql 2>/dev/null || true

sync
umount /mnt/data 2>/dev/null || true
