# URL Shortener

A robust and scalable URL shortener service built with Spring Boot. This application allows users to shorten long URLs, track analytics (clicks, dates), and manage their links securely. It features a complete authentication system using JWT.

## 🚀 Features

- **User Authentication**: Secure Login and Registration functionality using JWT (JSON Web Tokens).
- **URL Shortening**: Instantly convert long URLs into short, easy-to-share links.
- **Redirection**: Fast and reliable redirection from short URLs to original URLs.
- **Analytics Dashboard**: 
    - Track total clicks per URL.
    - View click history over specific date ranges.
    - Detailed analytics for individual URLs.
- **My URLs**: Users can view and manage their list of shortened URLs.
- **Multi-Database Support**: Configured to work with both MySQL and PostgreSQL.

## 🛠️ Tech Stack

- **Core**: Java 21, Spring Boot 3.5.8
- **Security**: Spring Security, JWT (jjwt 0.13.0)
- **Database**: Spring Data JPA (Hibernate), MySQL / PostgreSQL Drivers
- **Build Tool**: Maven
- **Containerization**: Docker


## 🔌 API Endpoints

### Authentication
- `POST /api/auth/public/register` - Register a new user.
  - Body: `{ "username": "...", "email": "...", "password": "..." }`
- `POST /api/auth/public/login` - Login and receive JWT.
  - Body: `{ "username": "...", "password": "..." }`

### URL Management (Requires Bearer Token)
- `POST /api/urls/shorten` - Shorten a URL.
  - Body: `{ "originalUrl": "https://..." }`
- `GET /api/urls/myurls` - Get all URLs created by the current user.
- `GET /api/urls/analytics/{shortUrl}?startDate=...&endDate=...` - Get specific URL analytics.
- `GET /api/urls/totalClicks?startDate=...&endDate=...` - Get total clicks for the user's URLs.

### Public
- `GET /{shortUrl}` - Redirect to the original URL.

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a pull request for any enhancements or bug fixes.
