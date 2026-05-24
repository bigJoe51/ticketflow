# TicketFlow Ticket Management System

Master Documentation Source

This file is a reusable source for writing the system proposal, SRS, SDD, PRS, and user manual for the TicketFlow Ticket Management System. It is based on the current implementation in the codebase as of May 3, 2026, and was created as a new file so no existing documentation was overwritten.

## 1. System Overview

TicketFlow Ticket Management System is a web-based support and service management platform for logging, assigning, tracking, resolving, and reviewing user issues. It supports three main actors: administrators, support staff, and clients/end users. The system centralizes support operations through role-based dashboards, secure authentication, ticket workflows, notifications, knowledge sharing, audit logging, SLA monitoring, escalation handling, and service feedback.

Core goals:
- Replace informal issue reporting with a structured workflow
- Improve ticket visibility and accountability
- Support timely issue resolution through SLA and escalation monitoring
- Provide a reusable knowledge base for common issues
- Enable management oversight through dashboards, reports, ratings, and audit logs

## 2. Problem Statement

Organizations that handle support requests manually through calls, chats, or email often face lost requests, unclear ownership, poor tracking, delayed resolution, low accountability, and weak reporting. TicketFlow solves this by providing a centralized platform where issues are recorded, assigned, updated, escalated, reviewed, and archived in a traceable way.

## 3. Vision, Objectives, and Benefits

Vision:
To provide a secure, easy-to-use, and transparent support platform that improves service coordination between users, support staff, and administrators.

General objective:
To design and implement a web-based ticket management system for structured support service delivery.

Specific objectives:
- Provide secure sign-in and role-based access
- Enable simple ticket submission and tracking
- Support staff assignment and ticket updates
- Monitor SLA and escalation rules
- Manage departments, assets, and user records
- Build a knowledge base of reusable support content
- Capture ratings and maintain audit trails

Expected benefits:
- Faster response and resolution
- Better communication and visibility
- Improved user satisfaction
- More accountable support operations
- Better reporting for managers

## 4. Stakeholders

Primary stakeholders:
- Clients / end users: submit and track issues
- Support staff: work on assigned tickets and contribute knowledge
- Administrators: manage users, configuration, compliance, and reporting

Secondary stakeholders:
- Organization management
- System administrators
- Developers and maintainers

## 5. Scope

In scope:
- User registration and login
- Google OAuth sign-in
- Forgot password and password reset by email
- Role-based dashboards
- Ticket creation, assignment, update, tracking, and resolution
- Ticket comments and attachments
- Notifications
- Departments and ticket categories
- SLA and escalation configuration
- Knowledge base submission and review
- Ratings and staff feedback
- Audit logging
- Asset registration and lookup
- Profile update and avatar upload

Out of scope:
- Payment processing
- Multi-tenant support
- Native mobile app
- AI ticket classification
- External messaging integrations beyond current email support

## 6. User Roles

Administrator:
- Manages users, roles, statuses, departments, assets, SLA rules, escalation rules, knowledge review, audit logs, ratings, and global ticket oversight

Support Staff:
- Manages assigned tickets, updates status, communicates with clients, views performance, and submits knowledge articles

Client / End User:
- Creates tickets, views own tickets, uploads attachments, comments, reads knowledge articles, rates resolved tickets, and updates profile

Role note:
The security model recognizes `ADMIN`, `STAFF`, `USER`, and `CLIENT`. Client-facing features are available to `USER` and `CLIENT`. Google sign-in users are currently auto-assigned to `CLIENT`.

## 7. Functional Modules

1. Authentication and Access Control
- Email/password login
- Google OAuth login
- JWT authentication
- Cookie-backed token access
- Forgot-password and reset-password flow
- Role-based endpoint protection

2. User Management
- Register user
- View users
- View user by ID
- Update profile
- Upload avatar
- Update role
- Update status

3. Ticket Management
- Create ticket
- View ticket by ID
- View tickets by creator
- View tickets by assigned staff
- Update status and priority
- Assign and unassign tickets
- Track resolution and escalation timestamps

4. Ticket Communication
- Add comments
- View comments by ticket

5. Attachment Management
- Upload attachments to tickets
- Retrieve attachments by ticket
- Download stored files

6. Dashboards and Reporting
- Admin dashboard statistics
- Client dashboard
- Staff dashboard
- Section-based dashboard navigation

7. Knowledge Base
- Submit article
- View approved articles
- View pending articles
- Review article
- Delete article

8. Notifications
- Generate event notifications
- View notifications by user
- Mark notifications as read

9. Ratings and Feedback
- Rate resolved tickets
- View average staff rating
- View detailed feedback records
- View all ratings for admin review

10. Department Management
- View departments
- Create department
- Update department
- Delete department

11. Ticket Categories
- View categories used during ticket creation

12. Asset Management
- Register asset
- View assets by department
- Delete asset

13. SLA and Escalation
- Configure SLA rules
- Configure escalation rules
- Auto-calculate breach and escalation state
- Notify affected stakeholders

14. Audit Logging
- Record key actions such as login, create, update, and escalation
- View audit logs
- View audit logs by user
- Clear logs

## 8. Business Processes

User registration and login:
1. User signs up or chooses Google sign-in
2. System authenticates user
3. System generates JWT
4. User is routed to the dashboard for their role

Forgot password:
1. User enters email
2. System creates token and expiry
3. Reset link is emailed
4. User resets password through the link

Client ticket submission:
1. Client opens Submit Ticket
2. Client fills title, description, priority, category, and department
3. System creates the ticket
4. Attachments are uploaded
5. User is redirected to My Tickets

Ticket processing:
1. Admin or staff views tickets
2. Ticket is assigned
3. Staff updates status and comments
4. System monitors SLA and escalation
5. Ticket is resolved and later rated

Knowledge workflow:
1. Staff submits article
2. Admin reviews article
3. Approved content becomes available to users

## 9. Functional Requirements

- The system shall allow users to register and authenticate securely
- The system shall support Google OAuth sign-in
- The system shall support password reset via email
- The system shall allow clients to create tickets
- The system shall store ticket title, description, priority, category, department, creator, assignee, and timestamps
- The system shall allow authorized users to update ticket status and priority
- The system shall allow administrators to assign tickets
- The system shall allow users to add comments and attachments to tickets
- The system shall notify relevant users about important ticket events
- The system shall provide role-specific dashboards
- The system shall allow staff to submit knowledge articles
- The system shall allow administrators to review knowledge articles
- The system shall allow clients to rate resolved tickets
- The system shall record audit logs for important actions
- The system shall support SLA breach detection and escalation

## 10. Non-Functional Requirements

Usability:
- The system should be understandable to non-technical users
- Ticket creation should use simple language and minimal mandatory fields

Security:
- Passwords shall be hashed with BCrypt
- Protected endpoints shall require authentication
- Access shall be role-based
- JWT tokens shall be validated
- Password reset tokens shall expire

Performance:
- Common operations should complete within reasonable web response times
- Notification refresh should not significantly degrade performance

Reliability:
- Business records shall persist in the database
- Errors should be handled consistently

Maintainability:
- The application should maintain separation between controller, service, repository, mapper, DTO, and model layers

Portability:
- The system should run as a Spring Boot web application in standard Java environments

## 11. Technology Stack

- Java 21
- Spring Boot 3.5.5
- Spring Web
- Spring Data JPA
- Spring Security
- Thymeleaf
- MySQL
- H2 for runtime/testing support
- JWT via `io.jsonwebtoken`
- Spring OAuth2 Client
- Spring Mail
- Lombok
- Maven

## 12. System Architecture

Architecture style:
Layered monolithic web application

Layers:
- Presentation layer: Thymeleaf templates, HTML, CSS, JavaScript
- Controller layer: REST and view controllers
- Service layer: business logic
- Repository layer: database access through Spring Data JPA
- Database layer: MySQL relational storage

High-level design flow:
Browser -> Thymeleaf/JS UI -> Spring Controllers -> Services -> Repositories -> Database

Security flow:
Login/OAuth -> JWT generation -> token stored in cookie/header -> request filter validates token -> role-based access granted

## 13. Core Entities

Main entities in the current implementation:
- User
- Role
- Department
- Ticket
- TicketCategory
- TicketComment
- Attachment
- TicketRating
- Notification
- KnowledgeArticle
- AuditLog
- Asset
- SLA
- EscalationRule

Important entity notes:
- `User` stores profile, role, department, and password reset data
- `Ticket` stores issue details, status, priority, creator, assignee, and escalation timestamps
- `Notification` stores message and read state per user
- `KnowledgeArticle` supports staff contribution and admin review
- `AuditLog` supports traceability
- `SLA` and `EscalationRule` support service monitoring

## 14. Security Design

Implemented security features:
- Role-based security configuration in Spring Security
- JWT validation by request filter
- Support for Authorization header and auth cookie
- OAuth2 login using Google
- Password reset token creation and expiry validation
- Access restrictions for admin, staff, client, and public endpoints

Examples of protected access:
- `/admin/**` for administrator functions
- `/staff/**` for staff/admin operations
- `/client/**` for client-facing operations
- `/tickets/**` for authenticated users

## 15. API Summary

Authentication:
- `POST /auth/register`
- `POST /auth/sign`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`

Users:
- `GET /users`
- `GET /users/{id}`
- `PUT /users/{id}/profile`
- `POST /users/{id}/avatar`
- `GET /users/avatar/{fileName}`

Admin:
- `GET /admin/users`
- `POST /admin/users`
- `PUT /admin/users/{id}/status`
- `PUT /admin/users/{id}/role`
- `GET /admin/tickets`
- `GET /admin/config/sla`
- `PUT /admin/config/sla`
- `GET /admin/config/escalation`
- `PUT /admin/config/escalation`

Tickets:
- `POST /tickets/create`
- `GET /tickets/{id}`
- `GET /tickets/user/{userId}`
- `GET /tickets/staff/{staffId}`
- `PUT /tickets/{id}`

Comments:
- `POST /comments/add`
- `GET /comments/ticket/{ticketId}`

Attachments:
- `POST /attachments/ticket/{ticketId}`
- `POST /attachments/ticket/{ticketId}/self`
- `GET /attachments/ticket/{ticketId}`
- `GET /attachments/file/{fileName}`

Notifications:
- `GET /notifications/user/{userId}`
- `PUT /notifications/read/{id}`

Knowledge base:
- `POST /knowledge/create`
- `GET /knowledge/approved`
- `GET /knowledge/pending`
- `PUT /knowledge/{id}/review`
- `DELETE /knowledge/{id}`

Ratings:
- `POST /ratings/rate`
- `GET /ratings/staff/{staffId}`
- `GET /ratings/staff/{staffId}/details`
- `GET /ratings/all`

Departments:
- `GET /departments`
- `POST /departments`
- `PUT /departments/{departmentId}`
- `DELETE /departments/{departmentId}`

Ticket categories:
- `GET /ticket-categories`

Assets:
- `POST /assets/register`
- `GET /assets/department/{departmentId}`
- `DELETE /assets/{assetId}`

Audit logs:
- `GET /audit`
- `GET /audit/user/{userId}`
- `DELETE /audit/clear`

## 16. User Interface Summary

Public pages:
- Landing page
- Login page
- Sign-up page
- Forgot password page
- Reset password page

Dashboard pages:
- Admin dashboard
- Staff dashboard
- Client dashboard

Dashboard behavior:
- Role-based routing
- Section-based navigation
- Notification access
- Profile update
- Centered popup alerts for user feedback
- Back button that returns to previous section history

Client dashboard sections:
- Overview
- Submit Ticket
- My Tickets
- Communication
- Feedback
- Knowledge Base
- Profile

Staff dashboard sections:
- Overview
- Assigned ticket workspace
- Communication
- Knowledge Base
- Performance
- Profile

Admin dashboard sections:
- Overview
- User Management
- Department Management
- Asset Management
- SLA and Escalation
- Reports and Analytics
- Knowledge Base
- Audit Logs
- Profile

## 17. Suggested Proposal Content

Use these themes in the system proposal:
- Background: organizations need structured support handling
- Justification: better tracking, accountability, and service quality
- Objectives: secure, role-based, centralized support platform
- Benefits: faster resolution, better reporting, improved user satisfaction
- Deliverables: web app, database, dashboards, security, reporting modules

## 18. Suggested SRS Content

Include:
- Introduction and scope
- Product perspective
- User classes
- Functional requirements by module
- Non-functional requirements
- External interface requirements
- Security requirements
- Data requirements
- Constraints and assumptions

## 19. Suggested SDD Content

Include:
- Design goals
- Layered architecture
- Module decomposition
- Controller/service/repository structure
- Entity and DTO design
- Security design
- UI design
- Data flow and request flow

## 20. Suggested PRS Content

Include:
- Product objective
- Target users
- Feature list
- User problems solved
- Value proposition
- Success metrics such as response time, resolution time, satisfaction, and dashboard visibility

## 21. User Manual Source

How to sign up:
1. Open Sign Up
2. Fill personal details
3. Submit form
4. Sign in

How to sign in:
1. Open Login
2. Enter email and password or use Google sign-in
3. System opens your dashboard

How to reset password:
1. Click Forgot Password
2. Enter your email
3. Open the emailed reset link
4. Enter new password

How to create a ticket:
1. Sign in as client
2. Open Submit Ticket
3. Enter title and description
4. Choose priority, category, and department
5. Add attachments if needed
6. Submit
7. System opens My Tickets

How to track a ticket:
1. Open My Tickets
2. Filter by category or status
3. Open details or comments

How staff handle tickets:
1. Open assigned tickets
2. Review status
3. Update progress
4. Add comments
5. Resolve or escalate as needed

How admin manages the system:
1. Open admin dashboard
2. Manage users, departments, assets, and configs
3. Review knowledge articles
4. Monitor audit logs and performance

## 22. Assumptions, Constraints, and Risks

Assumptions:
- Database is available and initialized
- SMTP is configured for password reset email
- Google OAuth is configured when needed
- Roles, departments, and categories exist

Constraints:
- Monolithic architecture
- Spring Boot and MySQL dependency
- Browser-based interaction

Risks:
- Missing configuration can break auth/reset features
- Poor role setup can affect authorization
- Missing master data can block ticket creation or user flows

## 23. Testing Recommendations

Test areas:
- Registration and login
- Google OAuth
- Forgot/reset password
- Role-based access
- Ticket creation and update
- Comments and attachments
- Notifications
- Knowledge article review
- Ratings and feedback
- SLA and escalation logic
- Audit logging

Recommended test types:
- Unit testing
- Integration testing
- UI workflow testing
- Security testing
- User acceptance testing

## 24. Future Enhancements

- Email notifications for more ticket events
- SMS or messaging integration
- Advanced reports and exports
- Multi-tenant support
- Mobile app
- Deeper search and analytics
- AI-assisted categorization or summarization

## 25. Reusable Project Description

Short version:
TicketFlow Ticket Management System is a secure web-based support platform that helps organizations log, assign, track, resolve, escalate, and review service requests through role-based dashboards for administrators, staff, and clients.

Long version:
TicketFlow Ticket Management System is a full-stack ticketing and support operations application built with Spring Boot, Thymeleaf, JavaScript, and MySQL. It centralizes support workflows by enabling users to submit tickets, staff to process assigned issues, and administrators to monitor and configure service operations. The system includes secure authentication with JWT and Google OAuth, password reset by email, notifications, attachments, comments, knowledge base workflows, asset management, SLA monitoring, escalation handling, feedback collection, and audit logging.

## 26. Suggested Reuse Map

Proposal:
- Sections 1 to 5, 17, 25

SRS:
- Sections 5 to 10, 15

SDD:
- Sections 11 to 16

PRS:
- Sections 3, 7, 19, 25

User Manual:
- Sections 16 and 21
