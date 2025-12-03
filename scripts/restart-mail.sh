#!/bin/bash

# Restart docker-mailserver
# Usage: ./restart-mail.sh

set -e

echo "Restarting docker-mailserver..."

docker restart mailserver

echo "Mail server restarted successfully"

