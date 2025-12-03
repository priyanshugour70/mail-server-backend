# Mail Server - Complete Setup & Configuration

## 📚 Documentation Index

- **[QUICK_START.md](mail-server-backend/QUICK_START.md)** - Get started in 5 minutes
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - Complete deployment guide
- **[CONFIGURATION_SUMMARY.md](CONFIGURATION_SUMMARY.md)** - Configuration reference
- **[APPLICATION_FLOW.md](APPLICATION_FLOW.md)** - Application architecture

---

## 🚀 Quick Setup

### Local Development
```bash
# 1. Setup environment
cd mail-server-backend
./setup.sh

# 2. Start docker-mailserver
cd ../docker-mailserver
docker-compose up -d

# 3. Run application
cd ../mail-server-backend
mvn spring-boot:run
```

### EC2 Production
```bash
# 1. Connect to EC2
ssh -i key.pem ubuntu@your-ec2-ip

# 2. Run automated setup
./ec2-setup.sh

# 3. Configure DNS (see DEPLOYMENT_GUIDE.md)

# 4. Start services
sudo systemctl start mail-server
```

---

## ⚙️ Configuration Files

### .env File
Create from `.env.example`:
```bash
cp .env.example .env
nano .env
```

**Key Variables:**
```env
MAIL_SERVER_DOMAIN=lssgoo.com
MAIL_SERVER_IP=your_ec2_ip
DB_PASSWORD=your_secure_password
JWT_SECRET=generate-with-openssl-rand-base64-32
```

### application.yaml
Already configured to use environment variables from `.env`!

---

## 🌐 Domain Configuration

### DNS Records Required
1. **A Record**: `@` → Your EC2 IP
2. **A Record**: `mail` → Your EC2 IP
3. **MX Record**: `@` → `mail.lssgoo.com` (priority 10)
4. **SPF Record**: `@` → `v=spf1 mx a ip4:YOUR_IP ~all`
5. **DKIM Record**: `default._domainkey` → (from get-dkim.sh)
6. **DMARC Record**: `_dmarc` → `v=DMARC1; p=quarantine; rua=mailto:dmarc@lssgoo.com;`

See **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** for detailed DNS setup.

---

## 📦 Project Structure

```
mail-server/
├── mail-server-backend/          # Spring Boot Application
│   ├── src/
│   │   └── main/
│   │       ├── java/             # Java source code
│   │       └── resources/
│   │           └── application.yaml
│   ├── scripts/                  # Mail server scripts
│   ├── .env.example              # Environment template
│   ├── setup.sh                  # Local setup script
│   └── ec2-setup.sh              # EC2 setup script
├── docker-mailserver/             # Docker-mailserver config
│   ├── docker-compose.yml
│   ├── data/                     # Mail data
│   ├── config/                   # Mail server config
│   └── state/                    # Mail state
└── Documentation/
    ├── DEPLOYMENT_GUIDE.md
    ├── QUICK_START.md
    └── CONFIGURATION_SUMMARY.md
```

---

## 🔑 Environment Variables

### Generate Secrets
```bash
# JWT Secret
openssl rand -base64 32

# Database Password
openssl rand -base64 16
```

### Required Variables
- `MAIL_SERVER_DOMAIN` - Your domain (e.g., lssgoo.com)
- `MAIL_SERVER_IP` - Your server public IP
- `DB_PASSWORD` - PostgreSQL password
- `JWT_SECRET` - JWT signing secret

See **[.env.example](mail-server-backend/.env.example)** for complete list.

---

## 🐳 Docker-Mailserver

### Setup
```bash
cd docker-mailserver
mkdir -p data config state
docker-compose up -d
```

### Generate DKIM
```bash
docker run --rm \
  -v "$(pwd)/config:/tmp/docker-mailserver" \
  -ti docker.io/mailserver/docker-mailserver:latest \
  generate-dkim-config lssgoo.com
```

### Create Mailbox
```bash
cd ../mail-server-backend
./scripts/add-user.sh user@lssgoo.com password
```

---

## 📡 API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register user
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/refresh` - Refresh token

### Mail Operations
- `POST /api/v1/mail/server/send` - Send email
- `GET /api/v1/mail/server/inbox` - Get inbox
- `GET /api/v1/mail/server/message/{id}` - Get message
- `POST /api/v1/mail/server/reply` - Reply to email
- `POST /api/v1/mail/server/message/{id}/read` - Mark as read
- `DELETE /api/v1/mail/server/message/{id}` - Delete message

### Server Management
- `POST /api/v1/mail/server/users/create` - Create mailbox
- `GET /api/v1/mail/server/dns` - Get DNS records
- `GET /api/v1/mail/server/health` - Health check

**Full API Documentation**: http://localhost:8080/swagger-ui.html

---

## 🔒 Security

- ✅ JWT authentication
- ✅ BCrypt password hashing
- ✅ Session management
- ✅ Audit logging
- ✅ HTTPS ready (configure SSL certificates)

---

## 📝 Next Steps

1. **Read [QUICK_START.md](mail-server-backend/QUICK_START.md)** for quick setup
2. **Follow [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** for production deployment
3. **Check [CONFIGURATION_SUMMARY.md](CONFIGURATION_SUMMARY.md)** for configuration reference
4. **Review [APPLICATION_FLOW.md](APPLICATION_FLOW.md)** for architecture understanding

---

## 🆘 Troubleshooting

### Application won't start
```bash
sudo journalctl -u mail-server -n 100
```

### Mail server issues
```bash
docker logs mailserver
docker-compose restart
```

### DNS not working
```bash
dig lssgoo.com MX
# Wait up to 48 hours for DNS propagation
```

---

**Ready to deploy? Start with [QUICK_START.md](mail-server-backend/QUICK_START.md)!** 🚀

