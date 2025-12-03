# Quick Start Guide

## 🚀 Local Development Setup (5 Minutes)

### 1. Prerequisites Check
```bash
java -version  # Should be 17+
mvn -version   # Should be 3.6+
psql --version # Should be 14+
docker --version
```

### 2. Setup Database
```bash
# Create database
psql -U postgres
CREATE DATABASE mail_server;
CREATE USER mail_server WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE mail_server TO mail_server;
\q
```

### 3. Configure Environment
```bash
cd mail-server-backend

# Run setup script
./setup.sh

# Or manually copy .env.example to .env and edit
cp .env.example .env
nano .env
```

### 4. Start Docker-Mailserver
```bash
cd docker-mailserver
mkdir -p data config state
docker-compose up -d
```

### 5. Run Application
```bash
cd mail-server-backend
mvn spring-boot:run
```

### 6. Test
```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## ☁️ EC2 Production Setup (30 Minutes)

### 1. Launch EC2 Instance
- **AMI**: Ubuntu 22.04 LTS
- **Instance**: t3.medium (2 vCPU, 4GB RAM)
- **Storage**: 20GB
- **Security Group**: Open ports 22, 25, 80, 443, 587, 993, 8080

### 2. Connect to EC2
```bash
ssh -i your-key.pem ubuntu@your-ec2-ip
```

### 3. Run Setup Script
```bash
# Upload ec2-setup.sh to EC2
scp -i your-key.pem ec2-setup.sh ubuntu@your-ec2-ip:~/

# SSH and run
ssh -i your-key.pem ubuntu@your-ec2-ip
chmod +x ec2-setup.sh
./ec2-setup.sh
```

### 4. Configure DNS
Add these DNS records at your domain registrar:

```
A Record:        @ → your_ec2_ip
A Record:        mail → your_ec2_ip
MX Record:       @ → mail.lssgoo.com (priority 10)
TXT Record:      @ → v=spf1 mx a ip4:your_ec2_ip ~all
TXT Record:      default._domainkey → (from DKIM key)
TXT Record:      _dmarc → v=DMARC1; p=quarantine; rua=mailto:dmarc@lssgoo.com;
```

### 5. Start Services
```bash
sudo systemctl start mail-server
sudo systemctl status mail-server
```

---

## 📝 Environment Variables Quick Reference

### Required Variables
```env
DB_HOST=localhost
DB_NAME=mail_server
DB_USERNAME=mail_server
DB_PASSWORD=your_password
JWT_SECRET=generate-with-openssl-rand-base64-32
MAIL_SERVER_DOMAIN=lssgoo.com
MAIL_SERVER_IP=your_server_ip
```

### Generate JWT Secret
```bash
openssl rand -base64 32
```

---

## 🔍 Verification Commands

### Check Application
```bash
# Status
sudo systemctl status mail-server

# Logs
sudo journalctl -u mail-server -f

# Health
curl http://localhost:8080/actuator/health
```

### Check Mail Server
```bash
# Docker status
docker ps | grep mailserver

# Mail server logs
docker logs mailserver

# Test ports
telnet localhost 587
telnet localhost 993
```

### Check Database
```bash
psql -h localhost -U mail_server -d mail_server
\dt  # List tables
\q
```

### Check DNS
```bash
dig lssgoo.com MX
dig mail.lssgoo.com A
dig lssgoo.com TXT | grep spf
```

---

## 🆘 Common Issues

### Port Already in Use
```bash
sudo lsof -i :8080
sudo kill -9 <PID>
```

### Database Connection Failed
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Check connection
psql -h localhost -U mail_server -d mail_server
```

### Mail Server Not Starting
```bash
# Check Docker
docker ps -a
docker logs mailserver

# Restart
cd docker-mailserver
docker-compose restart
```

---

## 📚 Full Documentation

- **DEPLOYMENT_GUIDE.md** - Complete deployment guide
- **APPLICATION_FLOW.md** - Application architecture
- **INTERNAL_MAIL_FEATURES.md** - API documentation

---

**Need Help?** Check the logs and verify all services are running!

