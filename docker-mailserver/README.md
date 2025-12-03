# Docker Mailserver Setup

This directory contains the configuration for docker-mailserver.

## Setup Instructions

1. Create the required directories:
```bash
mkdir -p data config state
```

2. Generate DKIM keys:
```bash
docker run --rm \
  -v "$(pwd)/config:/tmp/docker-mailserver" \
  -ti docker.io/mailserver/docker-mailserver:latest \
  generate-dkim-config lssgoo.com
```

3. Start the mail server:
```bash
docker-compose up -d
```

4. Create initial user:
```bash
./scripts/add-user.sh user@lssgoo.com password
```

## Configuration Files

- `docker-compose.yml`: Docker Compose configuration
- `config/`: Mail server configuration files
- `data/`: Mail data directory
- `state/`: Mail state directory

## DNS Records

After setup, configure the following DNS records:

- **MX Record**: `mail.lssgoo.com` (priority 10)
- **SPF Record**: `v=spf1 mx a ip4:YOUR_SERVER_IP ~all`
- **DKIM Record**: Get from `./scripts/get-dkim.sh`
- **DMARC Record**: `v=DMARC1; p=quarantine; rua=mailto:dmarc@lssgoo.com;`

