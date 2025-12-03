# Configuration Summary - Mail Server

## 📁 Files Created

### 1. Environment Configuration
- **`.env.example`** - Template with all environment variables
- **`.env`** - Your actual configuration (create from .env.example, DO NOT commit)

### 2. Setup Scripts
- **`setup.sh`** - Interactive setup script for local/production
- **`ec2-setup.sh`** - Automated EC2 deployment script

### 3. Documentation
- **`DEPLOYMENT_GUIDE.md`** - Complete deployment guide
- **`QUICK_START.md`** - Quick reference guide
- **`CONFIGURATION_SUMMARY.md`** - This file

---

## 🔧 Configuration Process

### Step 1: Choose Your Domain
Example: `lssgoo.com`

### Step 2: Get Your Server IP
```bash
# On your server
curl ifconfig.me
```

### Step 3: Create .env File
```bash
cd mail-server-backend
cp .env.example .env
nano .env  # Edit with your values
```

### Step 4: Update Key Variables
```env
# Your domain
MAIL_SERVER_DOMAIN=lssgoo.com

# Your server IP (for DNS)
MAIL_SERVER_IP=your_ec2_public_ip

# Database password
DB_PASSWORD=your_secure_password

# JWT secret (generate with: openssl rand -base64 32)
JWT_SECRET=your_generated_secret
```

### Step 5: Configure DNS Records
At your domain registrar, add:

| Type | Name | Value | Priority |
|------|------|-------|----------|
| A | @ | your_ec2_ip | - |
| A | mail | your_ec2_ip | - |
| MX | @ | mail.lssgoo.com | 10 |
| TXT | @ | v=spf1 mx a ip4:your_ec2_ip ~all | - |
| TXT | default._domainkey | (from DKIM) | - |
| TXT | _dmarc | v=DMARC1; p=quarantine; rua=mailto:dmarc@lssgoo.com; | - |

---

## 🌐 Domain Configuration Example

### For Domain: `lssgoo.com`

**1. Update .env:**
```env
MAIL_SERVER_DOMAIN=lssgoo.com
MAIL_SERVER_IP=54.123.45.67  # Your EC2 IP
```

**2. Update application.yaml:**
Already configured to use environment variables!

**3. DNS Records:**
```
A Record:        lssgoo.com → 54.123.45.67
A Record:        mail.lssgoo.com → 54.123.45.67
MX Record:       lssgoo.com → mail.lssgoo.com (10)
SPF Record:      lssgoo.com → v=spf1 mx a ip4:54.123.45.67 ~all
DKIM Record:     default._domainkey.lssgoo.com → (from get-dkim.sh)
DMARC Record:    _dmarc.lssgoo.com → v=DMARC1; p=quarantine; rua=mailto:dmarc@lssgoo.com;
```

---

## ☁️ EC2 Configuration

### Security Group Rules
```
Inbound Rules:
- SSH (22) - Your IP only
- HTTP (80) - 0.0.0.0/0
- HTTPS (443) - 0.0.0.0/0
- SMTP (25) - 0.0.0.0/0
- SMTP Submission (587) - 0.0.0.0/0
- IMAP (143) - 0.0.0.0/0
- IMAPS (993) - 0.0.0.0/0
- SMTPS (465) - 0.0.0.0/0
- API (8080) - Your IP or 0.0.0.0/0
```

### EC2 Instance Requirements
- **Minimum**: t2.medium (2 vCPU, 4GB RAM)
- **Recommended**: t3.medium or t3.large
- **Storage**: 20GB minimum
- **OS**: Ubuntu 22.04 LTS or Amazon Linux 2023

### Quick EC2 Setup
```bash
# 1. Connect to EC2
ssh -i key.pem ubuntu@your-ec2-ip

# 2. Run automated setup
./ec2-setup.sh

# 3. Configure DNS (at domain registrar)

# 4. Start services
sudo systemctl start mail-server
```

---

## 📋 Environment Variables Checklist

### Required for Local Development
- [ ] `DB_HOST` - Database host
- [ ] `DB_NAME` - Database name
- [ ] `DB_USERNAME` - Database user
- [ ] `DB_PASSWORD` - Database password
- [ ] `JWT_SECRET` - JWT secret key
- [ ] `MAIL_SERVER_DOMAIN` - Your domain

### Required for Production
- [ ] All local development variables
- [ ] `MAIL_SERVER_IP` - Your server public IP
- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `SWAGGER_ENABLED=false`

### Optional
- [ ] `SERVER_PORT` - Application port (default: 8080)
- [ ] `MAIL_SERVER_HOST` - SMTP host (default: localhost)
- [ ] `MAIL_SERVER_PORT` - SMTP port (default: 587)

---

## 🔐 Security Best Practices

### 1. Generate Strong Secrets
```bash
# JWT Secret
openssl rand -base64 32

# Database Password
openssl rand -base64 16
```

### 2. File Permissions
```bash
# .env file should be readable only by owner
chmod 600 .env

# Scripts should be executable
chmod +x setup.sh ec2-setup.sh scripts/*.sh
```

### 3. Production Checklist
- [ ] Changed all default passwords
- [ ] Generated strong JWT secret
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Disabled Swagger (`SWAGGER_ENABLED=false`)
- [ ] Configured firewall
- [ ] Set up SSL/TLS certificates
- [ ] Configured log rotation
- [ ] Set up backups

---

## 🚀 Quick Commands

### Generate JWT Secret
```bash
openssl rand -base64 32
```

### Get Server IP
```bash
curl ifconfig.me
# Or
curl ipinfo.io/ip
```

### Test Database Connection
```bash
psql -h localhost -U mail_server -d mail_server
```

### Test Mail Server
```bash
# Check docker-mailserver
docker ps | grep mailserver
docker logs mailserver

# Test SMTP
telnet localhost 587

# Test IMAP
telnet localhost 993
```

### Check Application
```bash
# Status
sudo systemctl status mail-server

# Logs
sudo journalctl -u mail-server -f

# Health
curl http://localhost:8080/actuator/health
```

---

## 📝 Configuration Files Location

```
mail-server/
├── mail-server-backend/
│   ├── .env.example          # Template
│   ├── .env                  # Your config (create from .env.example)
│   ├── setup.sh              # Local setup script
│   ├── ec2-setup.sh          # EC2 setup script
│   ├── src/main/resources/
│   │   └── application.yaml  # Spring Boot config (uses .env)
│   └── scripts/              # Mail server scripts
├── docker-mailserver/
│   ├── docker-compose.yml     # Docker config
│   ├── data/                  # Mail data
│   ├── config/                # Mail server config
│   └── state/                 # Mail state
├── DEPLOYMENT_GUIDE.md        # Full guide
├── QUICK_START.md             # Quick reference
└── CONFIGURATION_SUMMARY.md   # This file
```

---

## 🎯 Configuration Flow

```
1. Choose Domain
   ↓
2. Get Server IP
   ↓
3. Create .env from .env.example
   ↓
4. Update .env with your values
   ↓
5. Configure DNS records
   ↓
6. Setup PostgreSQL
   ↓
7. Setup Docker-Mailserver
   ↓
8. Build & Run Application
   ↓
9. Test & Verify
```

---

## 📞 Support

For issues:
1. Check logs: `sudo journalctl -u mail-server -f`
2. Check mail server: `docker logs mailserver`
3. Verify DNS: `dig your-domain.com MX`
4. Review DEPLOYMENT_GUIDE.md for detailed steps

---

**All configuration files are ready! Follow the steps above to get started.** 🚀

