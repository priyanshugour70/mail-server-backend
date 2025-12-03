#!/bin/bash

# Add user to docker-mailserver
# Usage: ./add-user.sh <email> <password>

set -e

EMAIL="${1}"
PASSWORD="${2}"

if [ -z "$EMAIL" ] || [ -z "$PASSWORD" ]; then
    echo "Usage: $0 <email> <password>"
    exit 1
fi

# Extract domain and username
USERNAME=$(echo "$EMAIL" | cut -d'@' -f1)
DOMAIN=$(echo "$EMAIL" | cut -d'@' -f2)

# Hash password using doveadm
HASHED_PASSWORD=$(docker exec mailserver doveadm pw -s SHA512-CRYPT -p "$PASSWORD" | tr -d '\n')

# Add to postfix-accounts.cf
echo "$USERNAME|$DOMAIN:$HASHED_PASSWORD" | docker exec -i mailserver sh -c 'cat >> /tmp/docker-mailserver/postfix-accounts.cf'

# Reload postfix
docker exec mailserver postfix reload

echo "User $EMAIL added successfully"

