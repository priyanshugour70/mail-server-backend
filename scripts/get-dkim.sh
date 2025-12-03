#!/bin/bash

# Get DKIM public key from docker-mailserver
# Usage: ./get-dkim.sh

set -e

# Get DKIM public key
DKIM_KEY=$(docker exec mailserver cat /tmp/docker-mailserver/opendkim/keys/lssgoo.com/mail.txt 2>/dev/null || echo "")

if [ -z "$DKIM_KEY" ]; then
    echo "DKIM key not found. Please ensure DKIM is configured in docker-mailserver."
    exit 1
fi

echo "$DKIM_KEY"

