# Spring Boot Security: Registration & Login Flow

This document details the end-to-end execution flow for User Registration and Authentication (Login) within the `url-shortener` application.

---

## 1. User Registration Flow

This process handles creating a new user account, encrypting their credentials, and storing them in the database.

### Step 1: Client Request
The client (Postman, Frontend, etc.) sends a **POST** request to the public registration endpoint.

*   **URL:** `http://localhost:8090/api/auth/public/register`
*   **Payload (JSON):**
    ```json
    {
      "username": "user1",
      "email": "user1@example.com",
      "password": "password123"
    }
    ```

### Step 2: Controller Layer (`AuthController.java`)
The request enters the `registerUser` method.
1.  **Mapping:** Spring routes the request to `@PostMapping("/public/register")`.
2.  **Deserialization:** The incoming JSON is converted into a Java `RegisterRequest` DTO (Data Transfer Object).
3.  **Entity Creation:** A new `User` entity is instantiated and populated with data from the DTO. The default role `ROLE_USER` is assigned here.
4.  **Delegation:** The controller calls `userService.registerUser(user)`.

### Step 3: Service Layer (`UserService.java`)
The business logic handles the security aspects before saving.
1.  **Password Encoding:** The plain-text password (`password123`) is encrypted using `BCryptPasswordEncoder`. This ensures raw passwords are never stored.
    ```java
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    ```
2.  **Persistence:** The service calls `userRepository.save(user)`.

### Step 4: Data Layer (`UserRepository`) & Database
1.  The `JpaRepository` generates the SQL `INSERT` statement.
2.  The user row is created in the MySQL database table.

### Step 5: Response
The API returns a `200 OK` status with the message: `"user registered successfully"`.

---

## 2. User Login Flow

This process authenticates existing users and issues a JSON Web Token (JWT) for stateless session management.

### Step 1: Client Request
The client sends a **POST** request with credentials.

*   **URL:** `http://localhost:8090/api/auth/public/login`
*   **Payload (JSON):**
    ```json
    {
      "username": "user1",
      "password": "password123"
    }
    ```

### Step 2: Controller Layer (`AuthController.java`)
The request enters the `loginUser` method.
1.  **Deserialization:** JSON is converted to a `LoginRequest` DTO.
2.  **Delegation:** The controller calls `userService.authenticateUser(loginRequest)`.

### Step 3: Authentication Manager
This is the core of Spring Security. The `UserService` initiates authentication:
```java
authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(...))
```

**Internal Execution:**
1.  **Load User:** The `AuthenticationManager` calls `UserDetailsServiceImpl.loadUserByUsername("user1")`.
2.  **Fetch from DB:** `UserDetailsServiceImpl` queries the repository. If found, it returns a `UserDetailsImpl` object (the "Passport").
3.  **Verify Password:** The `DaoAuthenticationProvider` compares the **raw password** from the request against the **hashed password** in the database (wrapped in `UserDetailsImpl`).
    *   *If match:* Success.
    *   *If mismatch:* Throws `BadCredentialsException`.

### Step 4: Security Context & Token Generation
Once authentication is verified:
1.  **Update Context:** The valid `Authentication` object is stored in the `SecurityContextHolder`. This marks the request as "Logged In" internally.
2.  **Generate JWT:** The service calls `jwtUtils.generateToken(userDetails)`.
    *   The token is signed with the `HS384` (HMAC) algorithm using the secret key from `application.properties`.
    *   It embeds the **username** (subject), **roles**, **issued time**, and **expiration time**.

### Step 5: Response
The API returns the JWT to the client wrapped in a `JwtAuthenticationResponse`.

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ1c2VyMSIsInJvbGVzIjoiUk9MRV9VU0VSIi..."
}
```

---

## Summary Diagram

### Registration
`Client` -> `AuthController` (Create User) -> `UserService` (Encrypt Password) -> `UserRepository` -> `Database`

### Login
`Client` -> `AuthController` -> `UserService` -> **`AuthenticationManager`** -> `UserDetailsServiceImpl` (DB Fetch) -> **`Password Verification`** -> `JwtUtils` (Create Token) -> `Response`
