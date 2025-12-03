#!/bin/bash

# List all users in docker-mailserver
# Usage: ./list-users.sh

set -e

# Read postfix-accounts.cf and extract usernames
docker exec mailserver cat /tmp/docker-mailserver/postfix-accounts.cf | grep -v '^#' | grep -v '^$' | cut -d'|' -f1 | cut -d':' -f1 | while read line; do
    if [ ! -z "$line" ]; then
        echo "$line"
    fi
done

