# Mail Server Application - Flow & Architecture Documentation

## 📋 Table of Contents
1. [Overview](#overview)
2. [Authentication Flow](#authentication-flow)
3. [Internal Mail Server Flow](#internal-mail-server-flow)
4. [System Architecture](#system-architecture)
5. [Database Schema](#database-schema)
6. [API Request Flow](#api-request-flow)
7. [Security Flow](#security-flow)
8. [Session Management Flow](#session-management-flow)

---

## 🎯 Overview

This application is an **internal mail server system** built on docker-mailserver for the lssgoo.com domain.

### Key Components:
- **Spring Boot Backend** with JWT authentication
- **PostgreSQL Database** for user and session storage
- **Docker Mailserver** for complete email handling (SMTP/IMAP)
- **JavaMail API** for email operations
- **IMAP/SMTP Integration** for receiving and sending emails

### Features:
- ✅ User authentication and session management
- ✅ Send emails via internal SMTP
- ✅ Receive emails via IMAP
- ✅ Reply to emails
- ✅ Mark emails as read/unread
- ✅ Delete emails
- ✅ Mailbox user management
- ✅ DNS record management (SPF, DKIM, DMARC)
- ✅ Health monitoring

---

## 🔐 Authentication Flow

### 1. User Registration
```
Client → POST /api/v1/auth/register
  ↓
AuthController.register()
  ↓
AuthServiceImpl.register()
  ├─→ Validates username/email uniqueness
  ├─→ Encrypts password (BCrypt)
  ├─→ Creates User entity
  ├─→ Creates Session entity
  ├─→ Records SESSION_CREATED activity
  ├─→ Generates JWT tokens (access + refresh)
  ├─→ Creates AuditLog entry
  └─→ Returns AuthResponse with tokens
```

### 2. User Login
```
Client → POST /api/v1/auth/login
  ↓
AuthController.login()
  ↓
AuthServiceImpl.login()
  ├─→ Authenticates via Spring Security
  ├─→ Creates new Session
  ├─→ Records LOGIN activity in SessionActivity
  ├─→ Updates session.lastActivityAt
  ├─→ Generates JWT tokens
  ├─→ Updates User with sessionId and tokens
  └─→ Returns AuthResponse
```

### 3. Token Refresh
```
Client → POST /api/v1/auth/refresh
  ↓
AuthController.refreshToken()
  ↓
AuthServiceImpl.refreshToken()
  ├─→ Validates refresh token
  ├─→ Verifies session is active
  ├─→ Records TOKEN_REFRESHED activity
  ├─→ Updates session.lastActivityAt
  ├─→ Updates session.statusCheckedAt
  ├─→ Increments session.refreshCount
  ├─→ Generates new tokens
  └─→ Returns TokenResponse
```

### 4. Request Authentication
```
Every Request → JwtAuthenticationFilter
  ├─→ Extracts Bearer token from Authorization header
  ├─→ Validates token via JwtTokenProvider
  ├─→ Extracts userId and sessionId
  ├─→ Loads UserDetails from CustomUserDetailsService
  ├─→ Sets SecurityContext with authentication
  └─→ Request proceeds to controller
```

---

## 📧 Internal Mail Server Flow

### 1. Mailbox User Creation
```
Client → POST /api/v1/mail/server/users/create?email=user@lssgoo.com&password=xxx
  ↓
MailServerController.createUser()
  ↓
MailServerUserService.createMailbox()
  ├─→ Executes scripts/add-user.sh
  │   ├─→ Hashes password using doveadm
  │   ├─→ Adds to postfix-accounts.cf
  │   └─→ Reloads postfix
  └─→ Returns success
```

### 2. Sending Email via Internal Server
```
Client → POST /api/v1/mail/server/send
  Query Params: email=user@lssgoo.com&password=xxx
  Body: { to, cc, bcc, subject, body, isHtml }
  ↓
MailServerController.sendEmail()
  ↓
MailServerSendService.sendEmail()
  ├─→ Creates JavaMailSender with user's credentials
  ├─→ Sets from: user@lssgoo.com (user's email)
  ├─→ Sets to, cc, bcc recipients
  ├─→ Sets subject and body (HTML or plain text)
  ├─→ Sends via SMTP to docker-mailserver
  └─→ Returns success
```

### 3. Receiving Email (Get Inbox)
```
Client → GET /api/v1/mail/server/inbox?email=user@lssgoo.com&password=xxx&limit=50&offset=0
  ↓
MailServerController.getInbox()
  ↓
MailReceiveService.getInboxMessages()
  ├─→ Connects to IMAP server (docker-mailserver)
  ├─→ Opens INBOX folder
  ├─→ Retrieves messages (with pagination)
  ├─→ Converts messages to EmailMessageResponse
  │   ├─→ Extracts from, to, cc, bcc
  │   ├─→ Extracts subject, body (HTML/plain text)
  │   ├─→ Extracts attachments information
  │   ├─→ Gets sent/received dates
  │   └─→ Checks read/unread status
  ├─→ Returns list of messages (latest first)
  └─→ Closes IMAP connection
```

### 4. Get Single Email Message
```
Client → GET /api/v1/mail/server/message/{messageId}?email=user@lssgoo.com&password=xxx&folder=INBOX
  ↓
MailServerController.getMessage()
  ↓
MailReceiveService.getMessage()
  ├─→ Connects to IMAP server
  ├─→ Opens specified folder (default: INBOX)
  ├─→ Retrieves message by ID
  ├─→ Converts to EmailMessageResponse
  │   ├─→ Full message content
  │   ├─→ All headers
  │   ├─→ Attachments details
  │   └─→ Read status
  └─→ Returns message details
```

### 5. Mark Message as Read
```
Client → POST /api/v1/mail/server/message/{messageId}/read?email=user@lssgoo.com&password=xxx
  ↓
MailServerController.markAsRead()
  ↓
MailReceiveService.markAsRead()
  ├─→ Connects to IMAP server
  ├─→ Opens folder (READ_WRITE mode)
  ├─→ Gets message by ID
  ├─→ Sets SEEN flag
  ├─→ Saves changes
  └─→ Returns success
```

### 6. Delete Email Message
```
Client → DELETE /api/v1/mail/server/message/{messageId}?email=user@lssgoo.com&password=xxx
  ↓
MailServerController.deleteMessage()
  ↓
MailReceiveService.deleteMessage()
  ├─→ Connects to IMAP server
  ├─→ Opens folder (READ_WRITE mode)
  ├─→ Gets message by ID
  ├─→ Sets DELETED flag
  ├─→ Saves changes
  └─→ Returns success
```

### 7. Reply to Email
```
Client → POST /api/v1/mail/server/reply?email=user@lssgoo.com&password=xxx
  Body: { messageId, body, isHtml, replyAll }
  ↓
MailServerController.replyToEmail()
  ↓
MailReplyService.replyToMessage()
  ├─→ Gets original message via MailReceiveService
  ├─→ Extracts original sender, subject, body
  ├─→ Creates reply message
  │   ├─→ Sets to: original sender (or all if replyAll=true)
  │   ├─→ Sets subject: "Re: " + original subject
  │   ├─→ Sets from: user@lssgoo.com
  │   └─→ Builds reply body with original message
  ├─→ Sends via MailServerSendService
  └─→ Returns success
```

### 8. DNS Records Retrieval
```
Client → GET /api/v1/mail/server/dns
  ↓
MailServerController.getDns()
  ↓
MailDnsService.getDnsRecords()
  ├─→ Generates SPF record
  ├─→ Gets DKIM key (via scripts/get-dkim.sh)
  ├─→ Generates DMARC record
  ├─→ Generates MX record
  └─→ Returns all DNS records
```

### 9. Mail Server Health Check
```
Client → GET /api/v1/mail/server/health
  ↓
MailServerController.getHealth()
  ↓
MailDnsService.getDnsStatus()
  ├─→ Gets DNS configuration
  ├─→ Checks server status
  └─→ Returns health information
```

### 10. Restart Mail Server
```
Client → POST /api/v1/mail/server/restart
  ↓
MailServerController.restartMailServer()
  ↓
MailServerUserService.restartMailServer()
  ├─→ Executes scripts/restart-mail.sh
  ├─→ Restarts docker-mailserver container
  └─→ Returns success
```

---

## 🏗️ System Architecture

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT (Frontend)                      │
│                    React/Vue/Angular/Postman                  │
└───────────────────────────┬──────────────────────────────────┘
                             │ HTTPS/REST API
                             │ JWT Bearer Token
                             ▼
┌─────────────────────────────────────────────────────────────┐
│              SPRING BOOT APPLICATION LAYER                   │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Controllers (REST Endpoints)             │  │
│  │  AuthController │ MailServerController │ Session... │  │
│  └──────────────────┬───────────────────────────────────┘  │
│                     │                                       │
│  ┌──────────────────▼───────────────────────────────────┐  │
│  │              Services (Business Logic)                  │  │
│  │  AuthService │ MailServerSendService │ MailReceive... │  │
│  │  MailReplyService │ MailServerUserService │ MailDns...│  │
│  └──────────────────┬───────────────────────────────────┘  │
│                     │                                       │
│  ┌──────────────────▼───────────────────────────────────┐  │
│  │         Repositories (Data Access Layer)              │  │
│  │  UserRepository │ SessionRepository │ AuditLog...   │  │
│  └──────────────────┬───────────────────────────────────┘  │
└──────────────────────┼───────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  PostgreSQL  │ │ Docker-Mail   │ │   Scripts    │
│  Database    │ │ Server        │ │   (Bash)     │
│              │ │ (SMTP/IMAP)   │ │              │
└──────────────┘ └──────────────┘ └──────────────┘
```

### Request Flow Through Layers
```
HTTP Request
    ↓
JwtAuthenticationFilter (Security)
    ↓
Controller (REST API)
    ↓
Service (Business Logic)
    ↓
Repository (Data Access) / IMAP/SMTP Connection
    ↓
Database / Docker-Mailserver
```

---

## 💾 Database Schema

### Core Tables
```
users
├─ id (PK)
├─ username, email, password (BCrypt encrypted)
├─ firstName, lastName, phone
├─ currentSessionId, accessToken, refreshToken
├─ organisationId (FK)
├─ isActive, isEmailVerified
└─ timestamps (createdAt, updatedAt)

sessions
├─ id (PK)
├─ userId (FK)
├─ sessionToken, ipAddress, userAgent
├─ deviceInfo, browserInfo, location
├─ loginAt, logoutAt, lastActivityAt
├─ statusCheckedAt, refreshCount
├─ expiresAt, isActive, logoutReason
└─ timestamps

session_activities
├─ id (PK)
├─ sessionId (FK)
├─ activityType (LOGIN, REFRESH, STATUS_CHECK, LOGOUT, etc.)
├─ activityDetails, timestamp
└─ timestamps

organisations
├─ id (PK)
├─ name, domain, description
├─ isActive
└─ timestamps

audit_logs
├─ id (PK)
├─ userId (FK), sessionId (FK)
├─ action, entityType, entityId
├─ description, ipAddress, userAgent
└─ timestamps
```

---

## 🔄 API Request Flow Example

### Example 1: Sending Email via Internal Server

```
1. CLIENT REQUEST
   POST /api/v1/mail/server/send?email=user@lssgoo.com&password=mypass
   Headers: Authorization: Bearer <jwt_token>
   Body: {
     "to": "recipient@example.com",
     "cc": ["cc@example.com"],
     "bcc": ["bcc@example.com"],
     "subject": "Hello",
     "body": "Test email",
     "isHtml": false
   }

2. SECURITY FILTER
   JwtAuthenticationFilter.doFilterInternal()
   ├─ Extracts token from header
   ├─ Validates token
   ├─ Loads user details
   └─ Sets SecurityContext

3. CONTROLLER
   MailServerController.sendEmail()
   ├─ Validates request body
   ├─ Extracts email/password from query params
   └─ Calls MailServerSendService

4. SERVICE LAYER
   MailServerSendService.sendEmail()
   ├─ Creates JavaMailSender with user credentials
   ├─ Configures SMTP properties (host, port, TLS)
   ├─ Creates MimeMessage
   ├─ Sets from, to, cc, bcc, subject, body
   └─ Sends via Transport.send()

5. SMTP SERVER
   Docker-Mailserver SMTP
   ├─ Authenticates user credentials
   ├─ Accepts email
   └─ Delivers to recipient

6. RESPONSE
   Returns 200 OK with success message
```

### Example 2: Receiving Email (Get Inbox)

```
1. CLIENT REQUEST
   GET /api/v1/mail/server/inbox?email=user@lssgoo.com&password=mypass&limit=50&offset=0
   Headers: Authorization: Bearer <jwt_token>

2. SECURITY FILTER
   JwtAuthenticationFilter validates token

3. CONTROLLER
   MailServerController.getInbox()
   ├─ Extracts email/password from query params
   ├─ Extracts limit and offset
   └─ Calls MailReceiveService

4. SERVICE LAYER
   MailReceiveService.getInboxMessages()
   ├─ Creates IMAP session
   ├─ Connects to IMAP server (docker-mailserver)
   ├─ Opens INBOX folder (READ_ONLY)
   ├─ Calculates message range (pagination)
   ├─ Retrieves messages
   ├─ Converts each message to EmailMessageResponse
   │   ├─ Extracts headers (from, to, subject)
   │   ├─ Extracts body (HTML or plain text)
   │   ├─ Extracts attachments
   │   └─ Checks read status
   └─ Returns list of messages

5. RESPONSE
   Returns 200 OK with list of EmailMessageResponse
```

### Example 3: Replying to Email

```
1. CLIENT REQUEST
   POST /api/v1/mail/server/reply?email=user@lssgoo.com&password=mypass
   Headers: Authorization: Bearer <jwt_token>
   Body: {
     "messageId": 5,
     "body": "This is my reply",
     "isHtml": false,
     "replyAll": false
   }

2. SECURITY FILTER
   JwtAuthenticationFilter validates token

3. CONTROLLER
   MailServerController.replyToEmail()
   ├─ Validates request body
   └─ Calls MailReplyService

4. SERVICE LAYER
   MailReplyService.replyToMessage()
   ├─ Gets original message via MailReceiveService
   │   └─ Connects to IMAP, retrieves message by ID
   ├─ Extracts original sender, subject, body
   ├─ Creates reply MimeMessage
   │   ├─ Sets to: original sender (or all if replyAll)
   │   ├─ Sets subject: "Re: " + original subject
   │   ├─ Sets from: user@lssgoo.com
   │   └─ Builds reply body with original message quoted
   └─ Sends via MailServerSendService

5. SMTP SERVER
   Docker-Mailserver SMTP
   ├─ Authenticates and sends reply
   └─ Delivers to recipient

6. RESPONSE
   Returns 200 OK with success message
```

---

## 🔒 Security Flow

### Password Encryption Flow (User Passwords)
```
User Input: "myPassword123"
    ↓
RegisterRequest.password
    ↓
AuthServiceImpl.register()
    ↓
BCryptPasswordEncoder.encode()
    ├─ Uses BCrypt hashing algorithm
    ├─ One-way encryption (cannot be decrypted)
    └─ Returns hashed password
    ↓
Stored in Database: "$2a$10$hashed..." (BCrypt hash)
    ↓
During Login:
    ↓
BCryptPasswordEncoder.matches()
    └─ Compares input with stored hash
```

### JWT Token Flow
```
Login/Register
    ↓
JwtTokenProvider.generateAccessToken()
    ├─ Claims: userId, username, sessionId, type="access"
    ├─ Expires: 1 hour
    └─ Returns JWT string
    ↓
Stored in User.accessToken
    ↓
Client stores in localStorage/cookie
    ↓
Every Request:
    ├─ Client sends: Authorization: Bearer <token>
    ├─ JwtAuthenticationFilter validates
    └─ Extracts userId for authorization
```

### Mailbox Credentials
```
User provides email/password in query parameters
    ↓
Used directly for IMAP/SMTP authentication
    ↓
NOT stored in database
    ↓
Passed securely via HTTPS
    ↓
Used only for current request
```

---

## 📊 Session Management Flow

### Session Lifecycle
```
1. LOGIN
   ├─ Creates Session entity
   ├─ Records SESSION_CREATED activity
   ├─ Sets loginAt, isActive=true
   ├─ Updates lastActivityAt
   └─ Links to User.currentSessionId

2. TOKEN REFRESH
   ├─ Updates session.lastActivityAt
   ├─ Updates session.statusCheckedAt
   ├─ Increments session.refreshCount
   └─ Records TOKEN_REFRESHED activity

3. STATUS CHECK
   ├─ GET /api/v1/sessions/current
   ├─ Updates session.statusCheckedAt
   └─ Records STATUS_CHECK activity

4. ACTIVITY CHECK
   ├─ GET /api/v1/auth/me
   ├─ Updates session.lastActivityAt
   └─ Records ACTIVITY_CHECK activity

5. LOGOUT
   ├─ Sets session.isActive=false
   ├─ Sets session.logoutAt
   ├─ Records LOGOUT activity
   └─ Clears User.currentSessionId
```

---

## 🎯 Key Design Patterns

### 1. **Service Layer Pattern**
- Controllers delegate to Services
- Services contain business logic
- Repositories handle data access

### 2. **Repository Pattern**
- Abstraction over database access
- HQL queries for complex operations
- Spring Data JPA for CRUD

### 3. **Dependency Injection**
- Spring manages all dependencies
- @Autowired for loose coupling
- Easy testing and maintenance

### 4. **Session Management Pattern**
- Tracks all user activities
- Updates timestamps on interactions
- Records activities for audit trail

---

## 🚀 Startup Flow

```
1. Application Starts
   MailServerBackendApplication.main()
    ↓
2. Spring Boot Initialization
   ├─ Loads application.yaml
   ├─ Configures DataSource (PostgreSQL)
   ├─ Configures JPA/Hibernate
   ├─ Configures Spring Security
   └─ Scans for @Component, @Service, @Controller
    ↓
3. Database Connection
   ├─ Connects to PostgreSQL
   ├─ Runs Hibernate DDL (update mode)
   └─ Creates/updates tables
    ↓
4. Security Configuration
   SecurityConfig.filterChain()
   ├─ Configures JWT filter
   ├─ Sets public endpoints (/api/v1/auth/**)
   ├─ Sets mail server endpoints (authenticated)
   └─ Configures CORS
    ↓
5. Swagger Configuration
   SwaggerConfig.customOpenAPI()
   └─ Sets up API documentation
    ↓
6. Logging Configuration
   logback-spring.xml
   ├─ Creates date-based log folders
   ├─ Configures multiple log files
   └─ Sets log levels
    ↓
7. Application Ready
   └─ Listening on port 8080
```

---

## 📝 Summary

### **Authentication & Authorization**
- JWT-based stateless authentication
- Session tracking with activities
- User-scoped data access
- Token refresh mechanism

### **Internal Mail Server**
- Docker-mailserver integration
- Script-based user management
- Full SMTP/IMAP support
- Send, receive, reply functionality
- Email management (read, delete, mark as read)

### **Email Operations**
- **Send**: Full SMTP support with CC, BCC, HTML
- **Receive**: IMAP inbox listing with pagination
- **Read**: Get full message with attachments
- **Reply**: Reply to sender or reply-all with original message
- **Manage**: Mark as read, delete messages

### **Security Features**
- Password encryption (BCrypt for user passwords)
- JWT token validation
- Session management with activity tracking
- Audit logging
- Secure IMAP/SMTP connections (TLS/SSL)

### **Monitoring & Management**
- DNS record management (SPF, DKIM, DMARC)
- Health check endpoints
- Mail server restart capability
- Comprehensive logging

---

## 🔄 Complete API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Refresh tokens
- `POST /api/v1/auth/logout` - User logout
- `GET /api/v1/auth/me` - Get current user
- `POST /api/v1/auth/change-password` - Change password

### Mail Operations
- `POST /api/v1/mail/server/send` - Send email
- `GET /api/v1/mail/server/inbox` - Get inbox messages
- `GET /api/v1/mail/server/message/{id}` - Get single message
- `POST /api/v1/mail/server/message/{id}/read` - Mark as read
- `DELETE /api/v1/mail/server/message/{id}` - Delete message
- `POST /api/v1/mail/server/reply` - Reply to email

### Server Management
- `POST /api/v1/mail/server/users/create` - Create mailbox
- `GET /api/v1/mail/server/dns` - Get DNS records
- `GET /api/v1/mail/server/health` - Health check
- `POST /api/v1/mail/server/restart` - Restart server

### Session Management
- `GET /api/v1/sessions/current` - Get current session
- `GET /api/v1/sessions/{id}` - Get session by ID
- `GET /api/v1/sessions` - Get all user sessions
- `GET /api/v1/sessions/active` - Get active sessions
- `POST /api/v1/sessions/status` - Update session status

### DNS
- `GET /api/v1/mail/dns/server` - Get DNS server records
- `GET /api/v1/mail/dns/status` - Get DNS status

---

This architecture provides a **complete, secure, and scalable** internal mail server solution with full email functionality (send, receive, reply) while maintaining clean separation of concerns and comprehensive logging.
