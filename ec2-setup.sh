#!/bin/bash

# EC2 Setup Script for Mail Server
# Run this script on your EC2 instance to setup everything

set -e

echo "=========================================="
echo "EC2 Mail Server - Complete Setup Script"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check if running as root
if [ "$EUID" -eq 0 ]; then 
    echo -e "${RED}Please do not run as root. Use a regular user with sudo privileges.${NC}"
    exit 1
fi

echo "This script will install and configure:"
echo "- Java 17"
echo "- Maven"
echo "- PostgreSQL"
echo "- Docker & Docker Compose"
echo "- Application dependencies"
echo ""

read -p "Continue? (y/N): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    exit 0
fi

# Detect OS
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
else
    echo -e "${RED}Cannot detect OS${NC}"
    exit 1
fi

echo ""
echo "Detected OS: $OS"
echo ""

# Update system
echo "=== Updating system ==="
if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    sudo apt-get update
    sudo apt-get upgrade -y
elif [ "$OS" = "amzn" ] || [ "$OS" = "rhel" ] || [ "$OS" = "centos" ]; then
    sudo yum update -y
fi

# Install Java 17
echo ""
echo "=== Installing Java 17 ==="
if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    sudo apt-get install -y openjdk-17-jdk
elif [ "$OS" = "amzn" ]; then
    sudo amazon-linux-extras install -y java-openjdk17
fi

java -version

# Install Maven
echo ""
echo "=== Installing Maven ==="
if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    sudo apt-get install -y maven
elif [ "$OS" = "amzn" ]; then
    sudo yum install -y maven
fi

mvn -version

# Install PostgreSQL
echo ""
echo "=== Installing PostgreSQL ==="
if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    sudo apt-get install -y postgresql postgresql-contrib
    sudo systemctl start postgresql
    sudo systemctl enable postgresql
elif [ "$OS" = "amzn" ]; then
    sudo yum install -y postgresql15-server postgresql15
    sudo /usr/pgsql-15/bin/postgresql-15-setup initdb
    sudo systemctl enable postgresql-15
    sudo systemctl start postgresql-15
fi

# Setup PostgreSQL database
echo ""
echo "=== Setting up PostgreSQL database ==="
read -p "Enter database password: " DB_PASSWORD

if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    sudo -u postgres psql << EOF
CREATE DATABASE mail_server;
CREATE USER mail_server WITH PASSWORD '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE mail_server TO mail_server;
ALTER USER mail_server CREATEDB;
\q
EOF
elif [ "$OS" = "amzn" ]; then
    sudo -u postgres psql << EOF
CREATE DATABASE mail_server;
CREATE USER mail_server WITH PASSWORD '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE mail_server TO mail_server;
ALTER USER mail_server CREATEDB;
\q
EOF
fi

# Install Docker
echo ""
echo "=== Installing Docker ==="
if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    sudo apt-get install -y docker.io docker-compose
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker $USER
elif [ "$OS" = "amzn" ]; then
    sudo yum install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker $USER
    # Install docker-compose
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

docker --version
docker-compose --version

# Install Git
echo ""
echo "=== Installing Git ==="
if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    sudo apt-get install -y git
elif [ "$OS" = "amzn" ]; then
    sudo yum install -y git
fi

# Get server IP
SERVER_IP=$(curl -s ifconfig.me || curl -s ipinfo.io/ip)
echo ""
echo -e "${GREEN}Detected server IP: $SERVER_IP${NC}"

# Clone or setup application
echo ""
echo "=== Application Setup ==="
read -p "Enter application directory path [/opt/mail-server]: " APP_DIR
APP_DIR=${APP_DIR:-/opt/mail-server}

if [ ! -d "$APP_DIR" ]; then
    read -p "Clone from Git repository? (y/N): " clone_repo
    if [ "$clone_repo" = "y" ] || [ "$clone_repo" = "Y" ]; then
        read -p "Enter Git repository URL: " GIT_REPO
        sudo mkdir -p $(dirname $APP_DIR)
        sudo git clone $GIT_REPO $APP_DIR
        sudo chown -R $USER:$USER $APP_DIR
    else
        echo "Please copy your application files to $APP_DIR"
        read -p "Press Enter when files are copied..."
    fi
fi

cd $APP_DIR/mail-server-backend

# Run setup script
if [ -f "setup.sh" ]; then
    chmod +x setup.sh
    echo ""
    echo "Running application setup script..."
    ./setup.sh
else
    echo ""
    echo "Creating .env file manually..."
    read -p "Enter mail server domain: " MAIL_DOMAIN
    MAIL_DOMAIN=${MAIL_DOMAIN:-lssgoo.com}
    
    JWT_SECRET=$(openssl rand -base64 32)
    
    cat > .env << EOF
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mail_server
DB_USERNAME=mail_server
DB_PASSWORD=$DB_PASSWORD
JWT_SECRET=$JWT_SECRET
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000
MAIL_SERVER_DOMAIN=$MAIL_DOMAIN
MAIL_SERVER_HOST=localhost
MAIL_SERVER_PORT=587
MAIL_SERVER_IMAP_HOST=localhost
MAIL_SERVER_IMAP_PORT=993
MAIL_SERVER_IMAP_SSL=true
MAIL_SERVER_IP=$SERVER_IP
MAIL_SCRIPTS_PATH=./scripts
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
SWAGGER_ENABLED=false
EOF
fi

# Setup docker-mailserver
echo ""
echo "=== Setting up Docker-Mailserver ==="
cd docker-mailserver
mkdir -p data config state
sudo chown -R 1000:1000 data config state

# Generate DKIM
echo "Generating DKIM keys..."
docker run --rm \
  -v "$(pwd)/config:/tmp/docker-mailserver" \
  -ti docker.io/mailserver/docker-mailserver:latest \
  generate-dkim-config $MAIL_DOMAIN

echo ""
echo -e "${GREEN}DKIM key generated. Add this to your DNS:${NC}"
cat config/opendkim/keys/$MAIL_DOMAIN/mail.txt || echo "DKIM file not found"

# Start docker-mailserver
echo ""
read -p "Start docker-mailserver now? (y/N): " start_mail
if [ "$start_mail" = "y" ] || [ "$start_mail" = "Y" ]; then
    docker-compose up -d
    echo "Waiting for mailserver to start..."
    sleep 10
    docker ps | grep mailserver
fi

# Build application
echo ""
echo "=== Building Application ==="
cd $APP_DIR/mail-server-backend
mvn clean package -DskipTests

# Create systemd service
echo ""
echo "=== Creating Systemd Service ==="
sudo tee /etc/systemd/system/mail-server.service > /dev/null << EOF
[Unit]
Description=Mail Server Backend Application
After=network.target postgresql.service

[Service]
Type=simple
User=$USER
WorkingDirectory=$APP_DIR/mail-server-backend
EnvironmentFile=$APP_DIR/mail-server-backend/.env
ExecStart=/usr/bin/java -jar $APP_DIR/mail-server-backend/target/mail-server-backend-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable mail-server

echo ""
echo -e "${GREEN}=== Setup Complete! ===${NC}"
echo ""
echo "Next steps:"
echo "1. Review DNS configuration in DEPLOYMENT_GUIDE.md"
echo "2. Add DNS records to your domain"
echo "3. Start the application: sudo systemctl start mail-server"
echo "4. Check status: sudo systemctl status mail-server"
echo "5. View logs: sudo journalctl -u mail-server -f"
echo ""
echo "Important:"
echo "- JWT Secret: Check .env file"
echo "- Database Password: $DB_PASSWORD"
echo "- Server IP: $SERVER_IP"
echo "- Domain: $MAIL_DOMAIN"

