# Internal Mail Server - Complete Feature Set

## ✅ Removed External Email Account Features
All external email account functionality has been removed:
- ❌ UserEmailAccount entity
- ❌ EmailAccountService
- ❌ SmtpValidator / ImapValidator
- ❌ UnifiedMailService
- ❌ EmailAccountController
- ❌ EncryptionUtil (no longer needed)

## 🎯 Internal Mail Server Features

### 1. **Send Email**
**Endpoint:** `POST /api/v1/mail/server/send`

**Request:**
```json
{
  "to": "recipient@example.com",
  "cc": ["cc1@example.com"],
  "bcc": ["bcc1@example.com"],
  "subject": "Hello",
  "body": "Email body",
  "isHtml": false
}
```

**Query Parameters:**
- `email`: Your mailbox email (e.g., user@lssgoo.com)
- `password`: Your mailbox password

**Features:**
- ✅ Send to single or multiple recipients
- ✅ CC and BCC support
- ✅ HTML email support
- ✅ Plain text email support

---

### 2. **Receive Email (Get Inbox)**
**Endpoint:** `GET /api/v1/mail/server/inbox`

**Query Parameters:**
- `email`: Your mailbox email
- `password`: Your mailbox password
- `limit`: Number of messages to retrieve (default: 50)
- `offset`: Offset for pagination (default: 0)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "messageId": 1,
      "messageUid": "1",
      "from": "sender@example.com",
      "to": ["you@lssgoo.com"],
      "cc": [],
      "bcc": [],
      "subject": "Test Email",
      "body": "Email content",
      "isHtml": false,
      "sentDate": "2025-12-04T10:00:00Z",
      "receivedDate": "2025-12-04T10:01:00Z",
      "isRead": false,
      "hasAttachments": false,
      "attachments": [],
      "folder": "INBOX"
    }
  ]
}
```

**Features:**
- ✅ List all inbox messages
- ✅ Pagination support (limit/offset)
- ✅ Shows read/unread status
- ✅ Shows attachment information
- ✅ Returns latest messages first

---

### 3. **Get Single Email Message**
**Endpoint:** `GET /api/v1/mail/server/message/{messageId}`

**Query Parameters:**
- `email`: Your mailbox email
- `password`: Your mailbox password
- `folder`: Folder name (default: "INBOX")

**Response:**
```json
{
  "success": true,
  "data": {
    "messageId": 1,
    "from": "sender@example.com",
    "subject": "Test Email",
    "body": "Full email content",
    "attachments": [
      {
        "filename": "document.pdf",
        "contentType": "application/pdf",
        "size": 1024
      }
    ]
  }
}
```

**Features:**
- ✅ Get full message details
- ✅ Extract attachments information
- ✅ Support for HTML and plain text
- ✅ Works with any folder (INBOX, SENT, etc.)

---

### 4. **Mark Message as Read**
**Endpoint:** `POST /api/v1/mail/server/message/{messageId}/read`

**Query Parameters:**
- `email`: Your mailbox email
- `password`: Your mailbox password
- `folder`: Folder name (default: "INBOX")

**Features:**
- ✅ Marks message as read
- ✅ Updates IMAP flags
- ✅ Works with any folder

---

### 5. **Delete Email Message**
**Endpoint:** `DELETE /api/v1/mail/server/message/{messageId}`

**Query Parameters:**
- `email`: Your mailbox email
- `password`: Your mailbox password
- `folder`: Folder name (default: "INBOX")

**Features:**
- ✅ Deletes message from folder
- ✅ Sets DELETED flag
- ✅ Works with any folder

---

### 6. **Reply to Email**
**Endpoint:** `POST /api/v1/mail/server/reply`

**Request:**
```json
{
  "messageId": 1,
  "body": "This is my reply",
  "isHtml": false,
  "replyAll": false
}
```

**Query Parameters:**
- `email`: Your mailbox email
- `password`: Your mailbox password

**Features:**
- ✅ Reply to original sender
- ✅ Reply to all (includes CC recipients)
- ✅ Includes original message in reply
- ✅ Preserves original subject with "Re:" prefix
- ✅ Supports HTML and plain text replies
- ✅ Automatically formats reply with original message

---

### 7. **Create Mailbox User**
**Endpoint:** `POST /api/v1/mail/server/users/create`

**Query Parameters:**
- `email`: Email address (e.g., user@lssgoo.com)
- `password`: Mailbox password

**Features:**
- ✅ Creates new mailbox in docker-mailserver
- ✅ Hashes password using doveadm
- ✅ Adds to postfix-accounts.cf
- ✅ Reloads postfix automatically

---

### 8. **Get DNS Records**
**Endpoint:** `GET /api/v1/mail/server/dns`

**Response:**
```json
{
  "success": true,
  "data": {
    "SPF": "v=spf1 mx a ip4:YOUR_SERVER_IP ~all",
    "SPF_Type": "TXT",
    "SPF_Host": "@",
    "DKIM": "v=DKIM1; k=rsa; p=YOUR_DKIM_PUBLIC_KEY",
    "DKIM_Type": "TXT",
    "DKIM_Host": "default._domainkey",
    "DMARC": "v=DMARC1; p=quarantine; rua=mailto:dmarc@lssgoo.com;",
    "DMARC_Type": "TXT",
    "DMARC_Host": "_dmarc",
    "MX": "10 mail.lssgoo.com",
    "MX_Type": "MX",
    "MX_Host": "@"
  }
}
```

**Features:**
- ✅ SPF record generation
- ✅ DKIM key retrieval
- ✅ DMARC record generation
- ✅ MX record information

---

### 9. **Mail Server Health Check**
**Endpoint:** `GET /api/v1/mail/server/health`

**Features:**
- ✅ Checks mail server status
- ✅ Returns DNS configuration
- ✅ Returns server health information

---

### 10. **Restart Mail Server**
**Endpoint:** `POST /api/v1/mail/server/restart`

**Features:**
- ✅ Restarts docker-mailserver container
- ✅ Applies configuration changes

---

## 🔧 Configuration

### application.yaml
```yaml
mail:
  server:
    host: localhost
    port: 587
    domain: lssgoo.com
    imap:
      host: localhost
      port: 993
      ssl: true
    scripts:
      path: ./scripts
```

### Environment Variables
- `MAIL_SERVER_HOST`: SMTP host (default: localhost)
- `MAIL_SERVER_PORT`: SMTP port (default: 587)
- `MAIL_SERVER_IMAP_HOST`: IMAP host (default: localhost)
- `MAIL_SERVER_IMAP_PORT`: IMAP port (default: 993)
- `MAIL_SERVER_IMAP_SSL`: Use SSL for IMAP (default: true)
- `MAIL_SERVER_DOMAIN`: Mail domain (default: lssgoo.com)

---

## 📋 Complete API Endpoints

### Mail Operations
1. `POST /api/v1/mail/server/send` - Send email
2. `GET /api/v1/mail/server/inbox` - Get inbox messages
3. `GET /api/v1/mail/server/message/{id}` - Get single message
4. `POST /api/v1/mail/server/message/{id}/read` - Mark as read
5. `DELETE /api/v1/mail/server/message/{id}` - Delete message
6. `POST /api/v1/mail/server/reply` - Reply to email

### Server Management
7. `POST /api/v1/mail/server/users/create` - Create mailbox
8. `GET /api/v1/mail/server/dns` - Get DNS records
9. `GET /api/v1/mail/server/health` - Health check
10. `POST /api/v1/mail/server/restart` - Restart server

### DNS (via MailDNSController)
11. `GET /api/v1/mail/dns/server` - Get DNS server records
12. `GET /api/v1/mail/dns/status` - Get DNS status

---

## 🔒 Security

- ✅ All endpoints require JWT authentication
- ✅ User must provide their own mailbox credentials
- ✅ No password storage in database
- ✅ Secure IMAP/SMTP connections (TLS/SSL)

---

## 🚀 Usage Examples

### Send Email
```bash
curl -X POST "http://localhost:8080/api/v1/mail/server/send?email=user@lssgoo.com&password=mypass" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "recipient@example.com",
    "subject": "Hello",
    "body": "This is a test email"
  }'
```

### Get Inbox
```bash
curl -X GET "http://localhost:8080/api/v1/mail/server/inbox?email=user@lssgoo.com&password=mypass&limit=10&offset=0" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Reply to Email
```bash
curl -X POST "http://localhost:8080/api/v1/mail/server/reply?email=user@lssgoo.com&password=mypass" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": 1,
    "body": "This is my reply",
    "replyAll": false
  }'
```

---

## ✅ All Features Working 100%

- ✅ Send email (with CC, BCC, HTML support)
- ✅ Receive email (inbox listing)
- ✅ Read email (get full message)
- ✅ Reply to email (with original message)
- ✅ Mark as read
- ✅ Delete email
- ✅ Create mailbox users
- ✅ DNS management
- ✅ Health monitoring
- ✅ Server restart

All features are fully implemented and ready to use! 🎉

