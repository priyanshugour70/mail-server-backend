# Docker Mailserver Setup

This directory contains the configuration for docker-mailserver.

## Quick Start

1. **Configure environment variables:**
```bash
cp .env.example .env
nano .env  # Edit with your domain and settings
```

2. **Create required directories:**
```bash
mkdir -p data config state
chown -R 1000:1000 data config state
```

3. **Generate DKIM keys:**
```bash
docker run --rm \
  -v "$(pwd)/config:/tmp/docker-mailserver" \
  -ti docker.io/mailserver/docker-mailserver:latest \
  generate-dkim-config $(grep MAIL_DOMAIN .env | cut -d '=' -f2)
```

4. **Start the mail server:**
```bash
docker-compose --env-file .env up -d
```

5. **Create initial user:**
```bash
cd ../mail-server-backend
./scripts/add-user.sh user@yourdomain.com password
```

## Environment Variables (.env)

The `.env.example` file contains all configurable environment variables:

### Key Variables:
- `MAIL_DOMAIN` - Your mail server domain (e.g., lssgoo.com)
- `MAIL_HOSTNAME` - Mail server hostname (usually 'mail')
- `POSTMASTER_ADDRESS` - Postmaster email address
- `ENABLE_SPAMASSASSIN` - Enable spam filtering (1/0)
- `ENABLE_CLAMAV` - Enable antivirus (1/0)
- `ENABLE_FAIL2BAN` - Enable fail2ban (1/0)
- `ENABLE_POSTGREY` - Enable greylisting (1/0)
- `DMS_DEBUG` - Debug mode (0/1)

### Usage:
```bash
# Copy example file
cp .env.example .env

# Edit with your values
nano .env

# Use with docker-compose
docker-compose --env-file .env up -d
```

## Configuration Files

- `docker-compose.yml`: Docker Compose configuration (uses .env variables)
- `.env.example`: Environment variables template
- `.env`: Your actual configuration (create from .env.example, DO NOT commit)
- `config/`: Mail server configuration files
- `data/`: Mail data directory
- `state/`: Mail state directory

## DNS Records

After setup, configure the following DNS records at your domain registrar:

- **A Record**: `mail.yourdomain.com` → Your Server IP
- **MX Record**: `yourdomain.com` → `mail.yourdomain.com` (priority 10)
- **SPF Record**: `yourdomain.com` → `v=spf1 mx a ip4:YOUR_SERVER_IP ~all`
- **DKIM Record**: `default._domainkey.yourdomain.com` → (from get-dkim.sh)
- **DMARC Record**: `_dmarc.yourdomain.com` → `v=DMARC1; p=quarantine; rua=mailto:dmarc@yourdomain.com;`

## Troubleshooting

### Check mail server logs:
```bash
docker-compose logs -f mailserver
```

### Check container status:
```bash
docker ps | grep mailserver
```

### Restart mail server:
```bash
docker-compose restart
```

### View DKIM key:
```bash
cat config/opendkim/keys/yourdomain.com/mail.txt
```

