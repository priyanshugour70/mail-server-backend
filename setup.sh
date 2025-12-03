#!/bin/bash

# Mail Server Backend - Setup Script
# This script helps you configure the mail server for local development or production

set -e

echo "=========================================="
echo "Mail Server Backend - Setup Script"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to prompt for input
prompt_input() {
    local prompt_text=$1
    local default_value=$2
    local var_name=$3
    
    if [ -z "$default_value" ]; then
        read -p "$prompt_text: " input
    else
        read -p "$prompt_text [$default_value]: " input
        input=${input:-$default_value}
    fi
    
    eval "$var_name='$input'"
}

# Function to generate random secret
generate_secret() {
    openssl rand -base64 32 2>/dev/null || echo "fallback-secret-$(date +%s)-$(shuf -i 1000-9999 -n 1)"
}

echo "This script will help you configure your mail server."
echo ""

# Check if .env already exists
if [ -f ".env" ]; then
    echo -e "${YELLOW}Warning: .env file already exists${NC}"
    read -p "Do you want to overwrite it? (y/N): " overwrite
    if [ "$overwrite" != "y" ] && [ "$overwrite" != "Y" ]; then
        echo "Setup cancelled."
        exit 0
    fi
fi

echo ""
echo "=== Database Configuration ==="
prompt_input "Database Host" "localhost" DB_HOST
prompt_input "Database Port" "5432" DB_PORT
prompt_input "Database Name" "mail_server" DB_NAME
prompt_input "Database Username" "mail_server" DB_USERNAME
prompt_input "Database Password" "" DB_PASSWORD

echo ""
echo "=== JWT Configuration ==="
echo "Generating JWT secret..."
JWT_SECRET=$(generate_secret)
echo -e "${GREEN}Generated JWT secret${NC}"
prompt_input "JWT Access Token Expiration (ms)" "3600000" JWT_ACCESS_TOKEN_EXPIRATION
prompt_input "JWT Refresh Token Expiration (ms)" "604800000" JWT_REFRESH_TOKEN_EXPIRATION

echo ""
echo "=== Mail Server Configuration ==="
prompt_input "Mail Server Domain" "lssgoo.com" MAIL_SERVER_DOMAIN
prompt_input "SMTP Host" "localhost" MAIL_SERVER_HOST
prompt_input "SMTP Port" "587" MAIL_SERVER_PORT
prompt_input "IMAP Host" "localhost" MAIL_SERVER_IMAP_HOST
prompt_input "IMAP Port" "993" MAIL_SERVER_IMAP_PORT
prompt_input "IMAP SSL (true/false)" "true" MAIL_SERVER_IMAP_SSL

# Get server IP
echo ""
read -p "Enter your server public IP (for DNS records): " MAIL_SERVER_IP
if [ -z "$MAIL_SERVER_IP" ]; then
    MAIL_SERVER_IP="your_server_ip_here"
fi

echo ""
echo "=== Application Configuration ==="
prompt_input "Server Port" "8080" SERVER_PORT
prompt_input "Spring Profile (dev/prod)" "dev" SPRING_PROFILES_ACTIVE

if [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then
    SWAGGER_ENABLED="false"
else
    SWAGGER_ENABLED="true"
fi

# Create .env file
cat > .env << EOF
# ============================================
# Mail Server Backend - Environment Variables
# Generated on $(date)
# ============================================

# Database Configuration
DB_HOST=$DB_HOST
DB_PORT=$DB_PORT
DB_NAME=$DB_NAME
DB_USERNAME=$DB_USERNAME
DB_PASSWORD=$DB_PASSWORD

# JWT Configuration
JWT_SECRET=$JWT_SECRET
JWT_ACCESS_TOKEN_EXPIRATION=$JWT_ACCESS_TOKEN_EXPIRATION
JWT_REFRESH_TOKEN_EXPIRATION=$JWT_REFRESH_TOKEN_EXPIRATION

# Mail Server Configuration
MAIL_SERVER_DOMAIN=$MAIL_SERVER_DOMAIN
MAIL_SERVER_HOST=$MAIL_SERVER_HOST
MAIL_SERVER_PORT=$MAIL_SERVER_PORT
MAIL_SERVER_USERNAME=
MAIL_SERVER_PASSWORD=
MAIL_SERVER_FROM=noreply@$MAIL_SERVER_DOMAIN
MAIL_SERVER_IP=$MAIL_SERVER_IP
MAIL_SERVER_IMAP_HOST=$MAIL_SERVER_IMAP_HOST
MAIL_SERVER_IMAP_PORT=$MAIL_SERVER_IMAP_PORT
MAIL_SERVER_IMAP_SSL=$MAIL_SERVER_IMAP_SSL
MAIL_SCRIPTS_PATH=./scripts

# Application Configuration
SERVER_PORT=$SERVER_PORT
SPRING_APPLICATION_NAME=mail-server-backend
SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE
SWAGGER_ENABLED=$SWAGGER_ENABLED
EOF

echo ""
echo -e "${GREEN}✓ .env file created successfully!${NC}"
echo ""
echo "=== Next Steps ==="
echo "1. Review the .env file: cat .env"
echo "2. Setup PostgreSQL database"
echo "3. Configure DNS records (see DEPLOYMENT_GUIDE.md)"
echo "4. Start docker-mailserver"
echo "5. Build and run the application: mvn spring-boot:run"
echo ""
echo -e "${GREEN}Setup complete!${NC}"

