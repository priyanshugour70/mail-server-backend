#!/bin/bash

# Delete user from docker-mailserver
# Usage: ./delete-user.sh <email>

set -e

EMAIL="${1}"

if [ -z "$EMAIL" ]; then
    echo "Usage: $0 <email>"
    exit 1
fi

# Extract domain and username
USERNAME=$(echo "$EMAIL" | cut -d'@' -f1)
DOMAIN=$(echo "$EMAIL" | cut -d'@' -f2)

# Remove from postfix-accounts.cf
docker exec mailserver sed -i "/^${USERNAME}|${DOMAIN}:/d" /tmp/docker-mailserver/postfix-accounts.cf

# Reload postfix
docker exec mailserver postfix reload

echo "User $EMAIL deleted successfully"

