# Setup Checklist - Mail Server Configuration

## ✅ Pre-Deployment Checklist

### 1. Environment Setup
- [ ] Java 17+ installed
- [ ] Maven 3.6+ installed
- [ ] PostgreSQL 14+ installed
- [ ] Docker & Docker Compose installed
- [ ] Git installed

### 2. Database Configuration
- [ ] PostgreSQL running
- [ ] Database `mail_server` created
- [ ] User `mail_server` created
- [ ] Password set and secure
- [ ] Permissions granted

### 3. Environment Variables (.env)
- [ ] `.env` file created from `.env.example`
- [ ] `DB_HOST` set
- [ ] `DB_NAME` set to `mail_server`
- [ ] `DB_USERNAME` set to `mail_server`
- [ ] `DB_PASSWORD` set (strong password)
- [ ] `JWT_SECRET` generated (openssl rand -base64 32)
- [ ] `MAIL_SERVER_DOMAIN` set (e.g., lssgoo.com)
- [ ] `MAIL_SERVER_IP` set (your server public IP)
- [ ] `SPRING_PROFILES_ACTIVE` set (dev/prod)

### 4. Domain Configuration
- [ ] Domain purchased/owned
- [ ] DNS management access
- [ ] Server public IP obtained

### 5. DNS Records (At Domain Registrar)
- [ ] A Record: `@` → Your Server IP
- [ ] A Record: `mail` → Your Server IP
- [ ] MX Record: `@` → `mail.yourdomain.com` (priority 10)
- [ ] SPF Record: `@` → `v=spf1 mx a ip4:YOUR_IP ~all`
- [ ] DKIM Record: `default._domainkey` → (from get-dkim.sh)
- [ ] DMARC Record: `_dmarc` → `v=DMARC1; p=quarantine; rua=mailto:dmarc@yourdomain.com;`

### 6. Docker-Mailserver Setup
- [ ] Docker running
- [ ] `docker-mailserver` directory created
- [ ] `data`, `config`, `state` directories created
- [ ] Permissions set (chown 1000:1000)
- [ ] DKIM keys generated
- [ ] `docker-compose.yml` configured
- [ ] Mail server container running

### 7. Application Configuration
- [ ] `application.yaml` updated (uses .env variables)
- [ ] Application builds successfully (`mvn clean package`)
- [ ] Application starts without errors
- [ ] Health endpoint responds (`/actuator/health`)

### 8. EC2 Configuration (Production)
- [ ] EC2 instance launched
- [ ] Security group configured (ports 22, 25, 80, 443, 587, 993, 8080)
- [ ] Key pair created/downloaded
- [ ] SSH access working
- [ ] All software installed (Java, Maven, PostgreSQL, Docker)
- [ ] Systemd service created
- [ ] Application runs as service
- [ ] Auto-start on boot enabled

### 9. Security
- [ ] All default passwords changed
- [ ] Strong JWT secret generated
- [ ] `.env` file permissions set (chmod 600)
- [ ] Firewall configured
- [ ] Swagger disabled in production
- [ ] SSL/TLS certificates configured (optional but recommended)

### 10. Testing
- [ ] Database connection works
- [ ] Application health check passes
- [ ] User registration works
- [ ] User login works
- [ ] Mailbox creation works
- [ ] Email sending works
- [ ] Email receiving works
- [ ] Email reply works
- [ ] DNS records verified (dig commands)

---

## 🚀 Quick Setup Commands

### Generate JWT Secret
```bash
openssl rand -base64 32
```

### Get Server IP
```bash
curl ifconfig.me
```

### Test Database
```bash
psql -h localhost -U mail_server -d mail_server
```

### Test Application
```bash
curl http://localhost:8080/actuator/health
```

### Test Mail Server
```bash
docker ps | grep mailserver
docker logs mailserver
```

### Verify DNS
```bash
dig yourdomain.com MX
dig mail.yourdomain.com A
dig yourdomain.com TXT | grep spf
```

---

## 📋 Configuration Files Summary

| File | Purpose | Location |
|------|---------|----------|
| `.env` | Environment variables | `mail-server-backend/.env` |
| `.env.example` | Template | `mail-server-backend/.env.example` |
| `application.yaml` | Spring Boot config | `mail-server-backend/src/main/resources/` |
| `docker-compose.yml` | Docker-mailserver | `docker-mailserver/` |
| `setup.sh` | Local setup script | `mail-server-backend/` |
| `ec2-setup.sh` | EC2 setup script | `mail-server-backend/` |

---

## 🔍 Verification Steps

### 1. Database
```bash
psql -h localhost -U mail_server -d mail_server -c "\dt"
```

### 2. Application
```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

### 3. Mail Server
```bash
docker ps | grep mailserver
# Should show running container
```

### 4. DNS
```bash
dig yourdomain.com MX
# Should show MX record pointing to mail.yourdomain.com
```

### 5. API
```bash
# Test registration
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@yourdomain.com","password":"Test@123",...}'
```

---

## 📝 Configuration Values Reference

### Local Development
```env
DB_HOST=localhost
MAIL_SERVER_HOST=localhost
MAIL_SERVER_IMAP_HOST=localhost
SPRING_PROFILES_ACTIVE=dev
SWAGGER_ENABLED=true
```

### Production (EC2)
```env
DB_HOST=localhost
MAIL_SERVER_HOST=localhost
MAIL_SERVER_IMAP_HOST=localhost
MAIL_SERVER_IP=your_ec2_public_ip
SPRING_PROFILES_ACTIVE=prod
SWAGGER_ENABLED=false
```

---

## 🎯 Domain Configuration Example

**Domain:** `lssgoo.com`  
**Server IP:** `54.123.45.67`

### .env Configuration
```env
MAIL_SERVER_DOMAIN=lssgoo.com
MAIL_SERVER_IP=54.123.45.67
```

### DNS Records
```
A:     lssgoo.com → 54.123.45.67
A:     mail.lssgoo.com → 54.123.45.67
MX:    lssgoo.com → mail.lssgoo.com (10)
SPF:   lssgoo.com → v=spf1 mx a ip4:54.123.45.67 ~all
DKIM:  default._domainkey.lssgoo.com → (from get-dkim.sh)
DMARC: _dmarc.lssgoo.com → v=DMARC1; p=quarantine; rua=mailto:dmarc@lssgoo.com;
```

---

## ✅ Post-Deployment Verification

- [ ] Application accessible at `http://your-server-ip:8080`
- [ ] Swagger UI accessible (if enabled)
- [ ] Health endpoint responds
- [ ] User can register
- [ ] User can login
- [ ] JWT tokens work
- [ ] Mailbox can be created
- [ ] Email can be sent
- [ ] Email can be received
- [ ] Email can be replied to
- [ ] DNS records propagated (check with dig)

---

**Follow this checklist step by step for a successful deployment!** ✅

