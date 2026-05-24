# TicketFlow Ticket Management System - API Documentation (Postman)

## 📋 Quick Setup Requirements

Before testing the API, execute these SQL commands to create base data:

```sql
-- Create Roles (Required)
INSERT INTO role (name, description) VALUES 
('ADMIN', 'Administrator with full access'),
('STAFF', 'Support staff who can work on tickets'),
('USER', 'Regular user who can submit tickets');

-- Create Departments (Required)
INSERT INTO department (name, description) VALUES 
('Support', 'Technical support department'),
('Sales', 'Sales department'),
('HR', 'Human resources department'),
('IT', 'Internal IT department');

-- Create Ticket Categories (Required for ticket creation)
INSERT INTO ticket_category (name, description) VALUES 
('Technical', 'Technical issues and bug reports'),
('Feature Request', 'New feature requests'),
('Account', 'Account management issues'),
('Billing', 'Billing related issues');
```

**That's it!** No other setup needed. Ready to test.

---

## Base URL
```
http://localhost:8080
```

---

## Authentication Flow

1. **POST** `/auth/register` - Create account
2. **POST** `/auth/sign` - Get JWT token
3. Use token in `Authorization: Bearer <TOKEN>` header for all protected endpoints

---

## Endpoints

### 1. AUTH - Register User
**POST** `/auth/register`

**Request Body:**
```json
{
  "username": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "phoneNumber": "+254712345678",
  "status": "ACTIVE",
  "role": {
    "id": 1
  },
  "department": {
    "id": 1
  }
}
```

**Response:** User object with ID

---

### 2. AUTH - Sign In
**POST** `/auth/sign`

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePassword123!"
}
```

**Response:** JWT Token (string) - Copy this for all subsequent requests

---

### 3. USERS - Get All Users
**GET** `/users`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Response:** Array of all users

---

### 4. USERS - Get User by ID
**GET** `/users/{id}`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Path Variable:** `id` = 1 (example)

**Response:** Single user object

---

### 5. TICKETS - Create Ticket
**POST** `/tickets/create`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Database connection issue",
  "description": "Unable to connect to MySQL database after server restart",
  "ticketType": "BUG",
  "priority": "HIGH",
  "status": "OPEN",
  "category": {
    "id": 1
  },
  "department": {
    "id": 1
  },
  "createdBy": {
    "id": 1
  },
  "assignedTo": {
    "id": 1
  }
}
```

**Response:** Ticket object with ID and timestamps

---

### 6. TICKETS - Get Ticket by ID
**GET** `/tickets/{id}`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Path Variable:** `id` = 1

**Response:** Single ticket object with all details

---

### 7. TICKETS - Get User's Tickets
**GET** `/tickets/user/{userId}`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Path Variable:** `userId` = 1

**Response:** Array of tickets created by user

---

### 8. TICKETS - Get Staff Assigned Tickets
**GET** `/tickets/staff/{staffId}`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Path Variable:** `staffId` = 1

**Response:** Array of tickets assigned to staff

---

### 9. ASSETS - Register Asset
**POST** `/assets/register`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Dell Laptop XPS 13",
  "description": "High-performance laptop for development",
  "serialNumber": "DL-XPS-2026-001",
  "status": "ACTIVE",
  "procurementDate": "2026-01-15",
  "warrantyExpiryDate": "2028-01-15",
  "location": "Office Building A, Floor 2",
  "department": {
    "id": 1
  },
  "assignedTo": {
    "id": 1
  }
}
```

**Response:** Asset object with ID

---

### 10. ASSETS - Get Department Assets
**GET** `/assets/department/{departmentId}`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Path Variable:** `departmentId` = 1

**Response:** Array of assets in department

---

### 11. COMMENTS - Add Comment
**POST** `/comments/add`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "comment": "I've started investigating this issue. Need to check database logs.",
  "ticket": {
    "id": 1
  },
  "author": {
    "id": 1
  }
}
```

**Response:** Comment object with ID and timestamp

---

### 12. COMMENTS - Get Ticket Comments
**GET** `/comments/ticket/{ticketId}`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Path Variable:** `ticketId` = 1

**Response:** Array of comments on ticket

---

### 13. RATINGS - Rate Ticket
**POST** `/ratings/rate`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "rating": 5,
  "feedbackComment": "Excellent support! Issue resolved quickly.",
  "ticket": {
    "id": 1
  },
  "staff": {
    "id": 1
  }
}
```

**Response:** Rating object with ID

---

### 14. RATINGS - Get Staff Average Rating
**GET** `/ratings/staff/{staffId}`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Path Variable:** `staffId` = 1

**Response:** Double (e.g., 4.5)

---

### 15. NOTIFICATIONS - Get User Notifications
**GET** `/notifications/user/{userId}`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Path Variable:** `userId` = 1

**Response:** Array of unread notifications

---

### 16. NOTIFICATIONS - Mark as Read
**PUT** `/notifications/read/{id}`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Path Variable:** `id` = 1

**Response:** No content (200 OK)

---

### 17. KNOWLEDGE BASE - Create Article
**POST** `/knowledge/create`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "How to Reset Database Connection",
  "content": "Step 1: Log into the server\nStep 2: Run the reset command\nStep 3: Restart the service",
  "status": "PENDING",
  "createdBy": {
    "id": 1
  }
}
```

**Response:** Article object with ID

---

### 18. KNOWLEDGE BASE - Get Approved Articles
**GET** `/knowledge/approved`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Response:** Array of approved articles

---

### 19. KNOWLEDGE BASE - Get Pending Articles
**GET** `/knowledge/pending`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Response:** Array of pending articles

---

### 20. DASHBOARD - Admin Statistics
**GET** `/dashboard/admin`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Response:** 
```json
{
  "totalTickets": 45,
  "openTickets": 12,
  "resolvedTickets": 30,
  "slaBreaches": 3,
  "totalUsers": 25
}
```

---

### 21. ADMIN - Get All Users
**GET** `/admin/users`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Response:** Array of all users (admin endpoint)

---

### 22. ADMIN - Get All Tickets
**GET** `/admin/tickets`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Response:** Array of all tickets (admin endpoint)

---

### 23. AUDIT LOG - Get All Logs
**GET** `/audit`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Response:** Array of all audit logs

---

### 24. AUDIT LOG - Get User Logs
**GET** `/audit/user/{userId}`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Path Variable:** `userId` = 1

**Response:** Array of actions performed by user

---

## Postman Collection Import

Save this as `ticketflow-api.json` and import into Postman:

```json
{
  "info": {
    "name": "TicketFlow Ticket Management API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "AUTH",
      "item": [
        {
          "name": "Register User",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {"mode": "raw", "raw": "{\"username\":\"john_doe\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@example.com\",\"password\":\"SecurePassword123!\",\"phoneNumber\":\"+254712345678\",\"status\":\"ACTIVE\",\"role\":{\"id\":1},\"department\":{\"id\":1}}"},
            "url": {"raw": "http://localhost:8080/auth/register", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["auth", "register"]}
          }
        },
        {
          "name": "Sign In",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {"mode": "raw", "raw": "{\"email\":\"john@example.com\",\"password\":\"SecurePassword123!\"}"},
            "url": {"raw": "http://localhost:8080/auth/sign", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["auth", "sign"]}
          }
        }
      ]
    },
    {
      "name": "USERS",
      "item": [
        {
          "name": "Get All Users",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/users", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["users"]}
          }
        },
        {
          "name": "Get User by ID",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/users/1", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["users", "1"]}
          }
        }
      ]
    },
    {
      "name": "TICKETS",
      "item": [
        {
          "name": "Create Ticket",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}, {"key": "Authorization", "value": "Bearer {{token}}"}],
            "body": {"mode": "raw", "raw": "{\"title\":\"Database connection issue\",\"description\":\"Unable to connect to MySQL\",\"ticketType\":\"BUG\",\"priority\":\"HIGH\",\"status\":\"OPEN\",\"category\":{\"id\":1},\"department\":{\"id\":1},\"createdBy\":{\"id\":1},\"assignedTo\":{\"id\":1}}"},
            "url": {"raw": "http://localhost:8080/tickets/create", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["tickets", "create"]}
          }
        },
        {
          "name": "Get Ticket",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/tickets/1", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["tickets", "1"]}
          }
        },
        {
          "name": "Get User Tickets",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/tickets/user/1", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["tickets", "user", "1"]}
          }
        },
        {
          "name": "Get Staff Tickets",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/tickets/staff/1", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["tickets", "staff", "1"]}
          }
        }
      ]
    },
    {
      "name": "ASSETS",
      "item": [
        {
          "name": "Register Asset",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}, {"key": "Authorization", "value": "Bearer {{token}}"}],
            "body": {"mode": "raw", "raw": "{\"name\":\"Dell Laptop\",\"serialNumber\":\"DL-001\",\"status\":\"ACTIVE\",\"department\":{\"id\":1},\"assignedTo\":{\"id\":1}}"},
            "url": {"raw": "http://localhost:8080/assets/register", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["assets", "register"]}
          }
        },
        {
          "name": "Get Department Assets",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/assets/department/1", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["assets", "department", "1"]}
          }
        }
      ]
    },
    {
      "name": "COMMENTS",
      "item": [
        {
          "name": "Add Comment",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}, {"key": "Authorization", "value": "Bearer {{token}}"}],
            "body": {"mode": "raw", "raw": "{\"comment\":\"Investigating the issue\",\"ticket\":{\"id\":1},\"author\":{\"id\":1}}"},
            "url": {"raw": "http://localhost:8080/comments/add", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["comments", "add"]}
          }
        },
        {
          "name": "Get Ticket Comments",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/comments/ticket/1", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["comments", "ticket", "1"]}
          }
        }
      ]
    },
    {
      "name": "RATINGS",
      "item": [
        {
          "name": "Rate Ticket",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}, {"key": "Authorization", "value": "Bearer {{token}}"}],
            "body": {"mode": "raw", "raw": "{\"rating\":5,\"feedbackComment\":\"Excellent support!\",\"ticket\":{\"id\":1},\"staff\":{\"id\":1}}"},
            "url": {"raw": "http://localhost:8080/ratings/rate", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["ratings", "rate"]}
          }
        },
        {
          "name": "Get Staff Average Rating",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/ratings/staff/1", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["ratings", "staff", "1"]}
          }
        }
      ]
    },
    {
      "name": "NOTIFICATIONS",
      "item": [
        {
          "name": "Get User Notifications",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/notifications/user/1", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["notifications", "user", "1"]}
          }
        },
        {
          "name": "Mark as Read",
          "request": {
            "method": "PUT",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/notifications/read/1", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["notifications", "read", "1"]}
          }
        }
      ]
    },
    {
      "name": "KNOWLEDGE BASE",
      "item": [
        {
          "name": "Create Article",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}, {"key": "Authorization", "value": "Bearer {{token}}"}],
            "body": {"mode": "raw", "raw": "{\"title\":\"How to Reset DB\",\"content\":\"Step 1...\",\"status\":\"PENDING\",\"createdBy\":{\"id\":1}}"},
            "url": {"raw": "http://localhost:8080/knowledge/create", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["knowledge", "create"]}
          }
        },
        {
          "name": "Get Approved Articles",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/knowledge/approved", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["knowledge", "approved"]}
          }
        },
        {
          "name": "Get Pending Articles",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/knowledge/pending", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["knowledge", "pending"]}
          }
        }
      ]
    },
    {
      "name": "DASHBOARD",
      "item": [
        {
          "name": "Admin Statistics",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/dashboard/admin", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["dashboard", "admin"]}
          }
        }
      ]
    },
    {
      "name": "ADMIN",
      "item": [
        {
          "name": "Get All Users",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/admin/users", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["admin", "users"]}
          }
        },
        {
          "name": "Get All Tickets",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/admin/tickets", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["admin", "tickets"]}
          }
        }
      ]
    },
    {
      "name": "AUDIT LOG",
      "item": [
        {
          "name": "Get All Logs",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/audit", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["audit"]}
          }
        },
        {
          "name": "Get User Logs",
          "request": {
            "method": "GET",
            "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
            "url": {"raw": "http://localhost:8080/audit/user/1", "protocol": "http", "host": ["localhost"], "port": "8080", "path": ["audit", "user", "1"]}
          }
        }
      ]
    }
  ]
}
```

---

## Postman Setup Instructions

1. **Create Collection Variable:**
   - In Postman, go to collection settings
   - Add variable: `token` = (leave empty initially)

2. **Get Token:**
   - Call `/auth/sign` endpoint
   - Copy the response (JWT token)
   - Paste into `token` variable

3. **Use in Requests:**
   - All requests use `{{token}}` automatically
   - Update IDs (user, ticket, etc.) as needed

---

## Response Error Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Success |
| 201 | Created - Resource created |
| 400 | Bad Request - Invalid data |
| 401 | Unauthorized - Invalid token/credentials |
| 404 | Not Found - Resource doesn't exist |
| 500 | Server Error |

---

## Testing Workflow

1. **Execute SQL setup** (roles, departments, categories only)
2. **POST** `/auth/register` - Create your account
3. **POST** `/auth/sign` - Get JWT token
4. **Use token** in Authorization header for all other requests
5. **Update IDs** in requests based on created entities

---

**API Version:** 1.0  
**Last Updated:** March 12, 2026  
**Status:** Production Ready
