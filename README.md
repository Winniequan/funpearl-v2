# Funpearl API

A secure Spring Boot REST API with JWT authentication, email verification, and role-based access control.

---

## Table of Contents

1. [Features](#features)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [Prerequisites](#prerequisites)
5. [Installation](#installation)
6. [Configuration](#configuration)
7. [Running the Application](#running-the-application)
8. [API Endpoints](#api-endpoints)
9. [Authentication Flow](#authentication-flow)
10. [Security Features](#security-features)
11. [Database Schema](#database-schema)

---

## Features

- **User Authentication**: Signup, login, logout with JWT tokens
- **Email Verification**: Required before login
- **Refresh Tokens**: Secure token refresh mechanism
- **Password Security**: Strong password validation (uppercase, lowercase, digit, special char)
- **Role-Based Access Control**: USER and ADMIN roles
- **Profile Management**: Update profile, change password, delete account
- **Admin Panel**: User management, role assignment

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17+ | Programming Language |
| Spring Boot | 3.5.x | Framework |
| Spring Security | 6.x | Authentication & Authorization |
| Spring Data JPA | 3.x | Database ORM |
| PostgreSQL | 14+ | Database |
| JWT (jjwt) | 0.12.x | Token Generation |
| Lombok | Latest | Boilerplate Reduction |
| Maven | 3.9+ | Build Tool |

---

## Project Structure

```
funpearl/
├── src/main/java/com/funpearl/funpearl/
│   ├── FunpearlApplication.java          # Main application entry
│   ├── auth/                              # Authentication module
│   │   ├── controller/AuthController.java
│   │   ├── service/AuthService.java
│   │   ├── service/EmailVerificationService.java
│   │   ├── service/RefreshTokenService.java
│   │   ├── dto/                           # Data Transfer Objects
│   │   │   ├── SignupRequest.java
│   │   │   ├── SignupResponse.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── AuthResponse.java
│   │   │   ├── RefreshTokenRequest.java
│   │   │   └── TokenRefreshResponse.java
│   │   ├── entity/
│   │   │   ├── RefreshToken.java
│   │   │   └── EmailVerificationToken.java
│   │   └── repository/
│   │       ├── RefreshTokenRepository.java
│   │       └── EmailVerificationTokenRepository.java
│   ├── user/                              # User module
│   │   ├── controller/UserController.java
│   │   ├── service/UserService.java
│   │   ├── entity/User.java
│   │   ├── entity/Role.java
│   │   ├── repository/UserRepository.java
│   │   └── dto/
│   │       ├── UpdateProfileRequest.java
│   │       └── ChangePasswordRequest.java
│   ├── admin/                             # Admin module
│   │   ├── controller/AdminController.java
│   │   └── dto/
│   │       ├── UserResponse.java
│   │       └── AssignRoleRequest.java
│   ├── security/                          # Security configuration
│   │   ├── config/SecurityConfig.java
│   │   ├── jwt/JwtService.java
│   │   ├── jwt/JwtAuthenticationFilter.java
│   │   ├── CustomUserDetails.java
│   │   └── CustomUserDetailsService.java
│   ├── common/                            # Shared components
│   │   └── validation/
│   │       ├── StrongPassword.java
│   │       └── StrongPasswordValidator.java
│   └── exception/                         # Exception handling
│       ├── GlobalExceptionHandler.java
│       ├── BadRequestException.java
│       ├── UnauthorizedException.java
│       ├── ResourceNotFoundException.java
│       └── ErrorResponse.java
├── src/main/resources/
│   └── application.properties             # Application configuration
├── .env.example                           # Environment variables template
├── .gitignore
├── pom.xml                                # Maven dependencies
└── README.md
```

---

## Prerequisites

- **Java 17** or higher
- **PostgreSQL 14** or higher
- **Maven 3.9** or higher

---

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/Winniequan/funpearl-v2.git
cd funpearl
```

### 2. Create PostgreSQL database

```bash
createdb funpearl
```

### 3. Set environment variables

Copy the example file and fill in your values:

```bash
cp .env.example .env
```

Edit `.env` with your settings:

```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/funpearl
DB_USERNAME=postgres
DB_PASSWORD=your_password_here

# JWT
JWT_SECRET=your_jwt_secret_key_minimum_32_characters
```

---

## Configuration

### application.properties

```properties
# Server
server.port=8080

# Database
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/funpearl}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
app.jwt.secret=${JWT_SECRET:}
app.jwt.expiration=86400000              # 24 hours
app.jwt.refresh-expiration=604800000     # 7 days

# Email Verification
app.email.verification-expiration=86400000  # 24 hours
```

---

## Running the Application

### Option 1: Using Maven with environment variables

```bash
DB_PASSWORD=yourpassword JWT_SECRET=yourSecretKey123456789012345678 ./mvnw spring-boot:run
```

### Option 2: Export variables first

```bash
export DB_PASSWORD=yourpassword
export JWT_SECRET=yourSecretKey123456789012345678
./mvnw spring-boot:run
```

### Option 3: Using IDE

Set environment variables in your IDE's run configuration.

The application will start at: `http://localhost:8080`

---

## API Endpoints

### Authentication (`/api/auth`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/signup` | Public | Register new user |
| POST | `/login` | Public | Login and get tokens |
| POST | `/refresh` | Public | Refresh access token |
| POST | `/logout` | Required | Logout (invalidate refresh token) |
| GET | `/verify-email?token=xxx` | Public | Verify email address |
| POST | `/resend-verification` | Required | Resend verification email |

### User (`/api/users`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/me` | Required | Get current user profile |
| PUT | `/me` | Required | Update profile |
| PUT | `/me/password` | Required | Change password |
| DELETE | `/me` | Required | Delete account |

### Admin (`/api/admin`) - Requires ADMIN role

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/users` | ADMIN | List all users |
| GET | `/users/paged` | ADMIN | List users (paginated) |
| GET | `/users/{id}` | ADMIN | Get user by ID |
| POST | `/users/{id}/roles` | ADMIN | Assign role to user |
| DELETE | `/users/{id}/roles` | ADMIN | Remove role from user |
| DELETE | `/users/{id}` | ADMIN | Delete user |

---

## Authentication Flow

### 1. Signup

```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "message": "Registration successful. Please check your email to verify your account.",
  "email": "john@example.com"
}
```

### 2. Verify Email

```http
GET /api/auth/verify-email?token=<verification_token>
```

### 3. Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com"
}
```

### 4. Access Protected Resources

```http
GET /api/users/me
Authorization: Bearer <access_token>
```

### 5. Refresh Token

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

## Security Features

### Password Requirements

- Minimum 8 characters
- At least 1 uppercase letter (A-Z)
- At least 1 lowercase letter (a-z)
- At least 1 digit (0-9)
- At least 1 special character (!@#$%^&*()_+-=[]{}|;:,.<>?)

### Email Verification

- Required before login
- Token expires after 24 hours
- Can request new verification token

### Account Security

- Account can be enabled/disabled by admin
- Email re-verification required when changing email
- Password hashed with BCrypt (strength 10)

### JWT Security

- Access token expires in 24 hours
- Refresh token expires in 7 days
- Tokens are stateless (no server-side session)

---

## Database Schema

### Users Table

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| username | VARCHAR(255) | Unique username |
| email | VARCHAR(255) | Unique email |
| password | VARCHAR(255) | BCrypt hashed |
| enabled | BOOLEAN | Account status |
| email_verified | BOOLEAN | Email verification status |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

### User Roles Table

| Column | Type | Description |
|--------|------|-------------|
| user_id | BIGINT | Foreign key to users |
| role | VARCHAR(255) | ROLE_USER or ROLE_ADMIN |

### Refresh Tokens Table

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| token | VARCHAR(255) | UUID token |
| user_id | BIGINT | Foreign key to users |
| expiry_date | TIMESTAMP | Expiration timestamp |

### Email Verification Tokens Table

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| token | VARCHAR(255) | UUID token |
| user_id | BIGINT | Foreign key to users |
| expiry_date | TIMESTAMP | Expiration timestamp |

---

## Error Responses

All errors follow this format:

```json
{
  "status": 400,
  "message": "Error description",
  "timestamp": "2024-01-01T12:00:00"
}
```

### Common Error Codes

| Code | Description |
|------|-------------|
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (invalid/expired token) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found (resource doesn't exist) |
| 500 | Internal Server Error |

---

## License

This project is proprietary.

---

## Author

Winnie Quan

---

*Last updated: April 2026*
