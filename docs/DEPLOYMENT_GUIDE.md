# Mail Server - Complete Deployment & Configuration Guide

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Domain Configuration](#domain-configuration)
4. [EC2 Deployment](#ec2-deployment)
5. [DNS Configuration](#dns-configuration)
6. [Docker-Mailserver Setup](#docker-mailserver-setup)
7. [Environment Variables](#environment-variables)
8. [Verification & Testing](#verification--testing)

---

## 🔧 Prerequisites

### Required Software
- **Java 17+** (JDK)
- **Maven 3.6+**
- **PostgreSQL 14+**
- **Docker** and **Docker Compose**
- **Git**

### Required Accounts/Services
- Domain name (e.g., lssgoo.com)
- AWS EC2 instance (for production)
- DNS management access (for domain)

---

## 💻 Local Development Setup

### Step 1: Clone and Setup Project
```bash
cd /path/to/your/projects
git clone <your-repo-url> mail-server
cd mail-server/mail-server-backend
```

### Step 2: Setup PostgreSQL Database
```bash
# Install PostgreSQL (if not installed)
# macOS: brew install postgresql
# Ubuntu: sudo apt-get install postgresql

# Start PostgreSQL
# macOS: brew services start postgresql
# Ubuntu: sudo systemctl start postgresql

# Create database and user
psql -U postgres
```

```sql
CREATE DATABASE mail_server;
CREATE USER mail_server WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE mail_server TO mail_server;
\q
```

### Step 3: Configure Environment Variables
```bash
# Copy example env file
cp .env.example .env

# Edit .env file with your local settings
nano .env  # or use your preferred editor
```

**Local .env Configuration:**
```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mail_server
DB_USERNAME=mail_server
DB_PASSWORD=your_local_password

# JWT
JWT_SECRET=local-dev-secret-key-change-in-production-min-256-bits
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Mail Server (Local)
MAIL_SERVER_DOMAIN=lssgoo.com
MAIL_SERVER_HOST=localhost
MAIL_SERVER_PORT=587
MAIL_SERVER_IMAP_HOST=localhost
MAIL_SERVER_IMAP_PORT=993
MAIL_SERVER_IMAP_SSL=true
MAIL_SERVER_IP=127.0.0.1

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

### Step 4: Update application.yaml
Update `src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:mail_server}
    username: ${DB_USERNAME:mail_server}
    password: ${DB_PASSWORD:password}
    
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  access-token-expiration: ${JWT_ACCESS_TOKEN_EXPIRATION:3600000}
  refresh-token-expiration: ${JWT_REFRESH_TOKEN_EXPIRATION:604800000}

mail:
  server:
    host: ${MAIL_SERVER_HOST:localhost}
    port: ${MAIL_SERVER_PORT:587}
    domain: ${MAIL_SERVER_DOMAIN:lssgoo.com}
    ip: ${MAIL_SERVER_IP:}
    imap:
      host: ${MAIL_SERVER_IMAP_HOST:localhost}
      port: ${MAIL_SERVER_IMAP_PORT:993}
      ssl: ${MAIL_SERVER_IMAP_SSL:true}
```

### Step 5: Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR
java -jar target/mail-server-backend-1.0.0.jar
```

---

## 🌐 Domain Configuration

### Step 1: Choose Your Domain
- Example: `lssgoo.com`
- Ensure you have DNS management access

### Step 2: Get Your Server IP
```bash
# On your server, get public IP
curl ifconfig.me
# Or
curl ipinfo.io/ip
```

### Step 3: Update Environment Variables
```env
MAIL_SERVER_DOMAIN=lssgoo.com
MAIL_SERVER_IP=your_public_ip_here
```

---

## ☁️ EC2 Deployment

### Step 1: Launch EC2 Instance

1. **Go to AWS Console → EC2 → Launch Instance**
2. **Choose Instance Type:**
   - Minimum: `t2.medium` (2 vCPU, 4GB RAM)
   - Recommended: `t3.medium` or `t3.large`
3. **Configure Instance:**
   - **AMI**: Ubuntu 22.04 LTS or Amazon Linux 2023
   - **Instance Type**: t3.medium
   - **Key Pair**: Create or select existing
   - **Network Settings**: 
     - Allow HTTP (80), HTTPS (443)
     - Allow Custom TCP (25, 587, 993, 143, 465)
     - Allow Custom TCP (8080) for API
4. **Storage**: 20GB minimum
5. **Launch Instance**

### Step 2: Configure Security Groups

**Inbound Rules:**
```
Type            Protocol    Port Range    Source
HTTP            TCP         80            0.0.0.0/0
HTTPS           TCP         443           0.0.0.0/0
SMTP            TCP         25            0.0.0.0/0
SMTP Submission TCP         587           0.0.0.0/0
IMAP            TCP         143           0.0.0.0/0
IMAPS           TCP         993           0.0.0.0/0
SMTPS           TCP         465           0.0.0.0/0
Custom TCP      TCP         8080          Your IP/32 (or 0.0.0.0/0 for testing)
SSH             TCP         22            Your IP/32
```

### Step 3: Connect to EC2 Instance

```bash
# SSH into your instance
ssh -i your-key.pem ubuntu@your-ec2-ip

# Or for Amazon Linux
ssh -i your-key.pem ec2-user@your-ec2-ip
```

### Step 4: Install Required Software

**For Ubuntu:**
```bash
# Update system
sudo apt-get update && sudo apt-get upgrade -y

# Install Java 17
sudo apt-get install openjdk-17-jdk -y

# Install Maven
sudo apt-get install maven -y

# Install PostgreSQL
sudo apt-get install postgresql postgresql-contrib -y

# Install Docker
sudo apt-get install docker.io docker-compose -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Install Git
sudo apt-get install git -y

# Install Nginx (for reverse proxy, optional)
sudo apt-get install nginx -y
```

**For Amazon Linux:**
```bash
# Update system
sudo yum update -y

# Install Java 17
sudo amazon-linux-extras install java-openjdk17 -y

# Install Maven
sudo yum install maven -y

# Install PostgreSQL
sudo yum install postgresql15-server postgresql15 -y
sudo /usr/pgsql-15/bin/postgresql-15-setup initdb
sudo systemctl enable postgresql-15
sudo systemctl start postgresql-15

# Install Docker
sudo yum install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Install Git
sudo yum install git -y
```

### Step 5: Setup PostgreSQL on EC2

```bash
# Switch to postgres user
sudo -u postgres psql

# Create database and user
CREATE DATABASE mail_server;
CREATE USER mail_server WITH PASSWORD 'your_secure_production_password';
GRANT ALL PRIVILEGES ON DATABASE mail_server TO mail_server;
ALTER USER mail_server CREATEDB;
\q

# Configure PostgreSQL to accept connections
sudo nano /etc/postgresql/15/main/postgresql.conf
# Find and set:
# listen_addresses = '*'

sudo nano /etc/postgresql/15/main/pg_hba.conf
# Add line:
# host    all             all             0.0.0.0/0               md5

# Restart PostgreSQL
sudo systemctl restart postgresql
```

### Step 6: Deploy Application

```bash
# Clone your repository
cd /opt
sudo git clone <your-repo-url> mail-server
cd mail-server/mail-server-backend

# Create .env file
sudo nano .env
```

**Production .env Configuration:**
```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mail_server
DB_USERNAME=mail_server
DB_PASSWORD=your_secure_production_password

# JWT (Generate a strong secret!)
JWT_SECRET=generate-strong-random-256-bit-secret-here-use-openssl-rand-base64-32
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Mail Server
MAIL_SERVER_DOMAIN=lssgoo.com
MAIL_SERVER_HOST=localhost
MAIL_SERVER_PORT=587
MAIL_SERVER_IMAP_HOST=localhost
MAIL_SERVER_IMAP_PORT=993
MAIL_SERVER_IMAP_SSL=true
MAIL_SERVER_IP=your_ec2_public_ip
MAIL_SCRIPTS_PATH=./scripts

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
SWAGGER_ENABLED=false
```

**Generate Strong JWT Secret:**
```bash
openssl rand -base64 32
```

### Step 7: Build and Run Application

```bash
# Build the application
mvn clean package -DskipTests

# Create systemd service
sudo nano /etc/systemd/system/mail-server.service
```

**Service File Content:**
```ini
[Unit]
Description=Mail Server Backend Application
After=network.target postgresql.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/mail-server/mail-server-backend
EnvironmentFile=/opt/mail-server/mail-server-backend/.env
ExecStart=/usr/bin/java -jar /opt/mail-server/mail-server-backend/target/mail-server-backend-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable mail-server
sudo systemctl start mail-server

# Check status
sudo systemctl status mail-server

# View logs
sudo journalctl -u mail-server -f
```

---

## 🌍 DNS Configuration

### Step 1: Get Your EC2 Public IP
```bash
# On EC2 instance
curl ifconfig.me
```

### Step 2: Configure DNS Records

Go to your domain registrar (GoDaddy, Namecheap, Route53, etc.) and add these DNS records:

#### A Record (Main Domain)
```
Type: A
Name: @
Value: your_ec2_public_ip
TTL: 3600
```

#### A Record (Mail Subdomain)
```
Type: A
Name: mail
Value: your_ec2_public_ip
TTL: 3600
```

#### MX Record
```
Type: MX
Name: @
Priority: 10
Value: mail.lssgoo.com
TTL: 3600
```

#### SPF Record
```
Type: TXT
Name: @
Value: v=spf1 mx a ip4:your_ec2_public_ip ~all
TTL: 3600
```

#### DKIM Record
After setting up docker-mailserver, get DKIM key:
```bash
cd /opt/mail-server/mail-server-backend
./scripts/get-dkim.sh
```

Then add:
```
Type: TXT
Name: default._domainkey
Value: v=DKIM1; k=rsa; p=YOUR_DKIM_PUBLIC_KEY_HERE
TTL: 3600
```

#### DMARC Record
```
Type: TXT
Name: _dmarc
Value: v=DMARC1; p=quarantine; rua=mailto:dmarc@lssgoo.com; ruf=mailto:dmarc@lssgoo.com; sp=quarantine; aspf=r;
TTL: 3600
```

### Step 3: Verify DNS Records

```bash
# Check A record
dig lssgoo.com A
dig mail.lssgoo.com A

# Check MX record
dig lssgoo.com MX

# Check SPF
dig lssgoo.com TXT | grep spf

# Check DKIM
dig default._domainkey.lssgoo.com TXT

# Check DMARC
dig _dmarc.lssgoo.com TXT
```

---

## 🐳 Docker-Mailserver Setup

### Step 1: Setup Docker-Mailserver Directory

```bash
cd /opt/mail-server/mail-server-backend/docker-mailserver

# Create required directories
mkdir -p data config state

# Set permissions
sudo chown -R 1000:1000 data config state
```

### Step 2: Generate DKIM Keys

```bash
# Run docker-mailserver to generate DKIM
docker run --rm \
  -v "$(pwd)/config:/tmp/docker-mailserver" \
  -ti docker.io/mailserver/docker-mailserver:latest \
  generate-dkim-config lssgoo.com

# Copy the generated key
cat config/opendkim/keys/lssgoo.com/mail.txt
```

### Step 3: Update docker-compose.yml

```yaml
version: '3.8'

services:
  mailserver:
    image: docker.io/mailserver/docker-mailserver:latest
    container_name: mailserver
    hostname: mail
    domainname: lssgoo.com
    ports:
      - "25:25"
      - "143:143"
      - "465:465"
      - "587:587"
      - "993:993"
    volumes:
      - ./data:/var/mail
      - ./config:/tmp/docker-mailserver
      - ./state:/var/mail-state
    environment:
      - ENABLE_SPAMASSASSIN=1
      - ENABLE_CLAMAV=1
      - ENABLE_FAIL2BAN=1
      - ENABLE_POSTGREY=1
      - ONE_DIR=1
      - DMS_DEBUG=0
      - PERMIT_DOCKER=host
      - POSTMASTER_ADDRESS=postmaster@lssgoo.com
    cap_add:
      - NET_ADMIN
      - SYS_PTRACE
    restart: always
    networks:
      - mail-network

networks:
  mail-network:
    driver: bridge
```

### Step 4: Start Docker-Mailserver

```bash
cd /opt/mail-server/mail-server-backend/docker-mailserver
docker-compose up -d

# Check logs
docker-compose logs -f mailserver

# Check status
docker ps | grep mailserver
```

### Step 5: Create First Mailbox User

```bash
cd /opt/mail-server/mail-server-backend

# Make scripts executable
chmod +x scripts/*.sh

# Create a mailbox user
./scripts/add-user.sh admin@lssgoo.com SecurePassword123!
```

---

## 🔐 Environment Variables Reference

### Complete .env Template

```env
# ============================================
# DATABASE
# ============================================
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mail_server
DB_USERNAME=mail_server
DB_PASSWORD=your_secure_password

# ============================================
# JWT
# ============================================
JWT_SECRET=generate-with-openssl-rand-base64-32
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# ============================================
# MAIL SERVER
# ============================================
MAIL_SERVER_DOMAIN=lssgoo.com
MAIL_SERVER_HOST=localhost
MAIL_SERVER_PORT=587
MAIL_SERVER_USERNAME=
MAIL_SERVER_PASSWORD=
MAIL_SERVER_FROM=noreply@lssgoo.com
MAIL_SERVER_IP=your_ec2_public_ip
MAIL_SERVER_IMAP_HOST=localhost
MAIL_SERVER_IMAP_PORT=993
MAIL_SERVER_IMAP_SSL=true
MAIL_SCRIPTS_PATH=./scripts

# ============================================
# APPLICATION
# ============================================
SERVER_PORT=8080
SPRING_APPLICATION_NAME=mail-server-backend
SPRING_PROFILES_ACTIVE=prod
SWAGGER_ENABLED=false

# ============================================
# DOCKER
# ============================================
DOCKER_MAILSERVER_CONTAINER=mailserver
DOCKER_MAILSERVER_NETWORK=mail-network
```

---

## ✅ Verification & Testing

### Step 1: Test Database Connection
```bash
psql -h localhost -U mail_server -d mail_server
# Enter password when prompted
\q
```

### Step 2: Test Application
```bash
# Check if application is running
curl http://localhost:8080/actuator/health

# Test API (after creating user)
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@lssgoo.com",
    "password": "Test@123",
    "firstName": "Test",
    "lastName": "User",
    "phone": "1234567890",
    "organisationId": 1
  }'
```

### Step 3: Test Mail Server
```bash
# Check docker-mailserver logs
docker logs mailserver

# Test SMTP connection
telnet localhost 587

# Test IMAP connection
telnet localhost 993
```

### Step 4: Test Email Sending
```bash
# After authentication, test sending email
curl -X POST "http://localhost:8080/api/v1/mail/server/send?email=admin@lssgoo.com&password=SecurePassword123!" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "test@example.com",
    "subject": "Test Email",
    "body": "This is a test email",
    "isHtml": false
  }'
```

### Step 5: Test Email Receiving
```bash
# Test inbox retrieval
curl -X GET "http://localhost:8080/api/v1/mail/server/inbox?email=admin@lssgoo.com&password=SecurePassword123!&limit=10&offset=0" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🔒 Security Checklist

- [ ] Changed default JWT secret to strong random value
- [ ] Changed database password
- [ ] Configured firewall (only necessary ports open)
- [ ] Set up SSL/TLS certificates (for HTTPS)
- [ ] Disabled Swagger in production
- [ ] Set up log rotation
- [ ] Configured backup strategy
- [ ] Set up monitoring and alerts
- [ ] Restricted SSH access (key-based only)
- [ ] Updated all default passwords

---

## 📝 Quick Start Commands

### Local Development
```bash
# Start PostgreSQL
sudo systemctl start postgresql

# Start Docker-Mailserver
cd docker-mailserver && docker-compose up -d

# Run application
mvn spring-boot:run
```

### Production (EC2)
```bash
# Start services
sudo systemctl start postgresql
sudo systemctl start mail-server
cd docker-mailserver && docker-compose up -d

# Check status
sudo systemctl status mail-server
docker ps | grep mailserver

# View logs
sudo journalctl -u mail-server -f
docker logs -f mailserver
```

---

## 🆘 Troubleshooting

### Application won't start
```bash
# Check logs
sudo journalctl -u mail-server -n 100

# Check database connection
psql -h localhost -U mail_server -d mail_server

# Check port availability
sudo netstat -tulpn | grep 8080
```

### Mail server issues
```bash
# Check docker-mailserver logs
docker logs mailserver

# Restart mail server
cd docker-mailserver && docker-compose restart

# Check ports
sudo netstat -tulpn | grep -E '25|587|993'
```

### DNS not working
```bash
# Test DNS propagation
dig lssgoo.com MX
dig mail.lssgoo.com A

# Wait for DNS propagation (can take up to 48 hours)
```

---

This guide provides complete setup instructions from local development to production EC2 deployment! 🚀

