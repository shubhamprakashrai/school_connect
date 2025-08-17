# School Management System - Multi-Tenant Backend

## 🎓 Overview

A comprehensive multi-tenant school management system built with Spring Boot, following Clean Architecture principles. This system provides complete backend APIs for managing schools, students, teachers, parents, attendance, examinations, and more.

## 🚀 Features

### Core Features
- **Multi-Tenancy**: Complete data isolation between schools
- **Authentication & Authorization**: JWT-based security with role-based access control
- **User Management**: Support for multiple user roles (Admin, Teacher, Student, Parent)
- **Student Management**: Complete student lifecycle management
- **Teacher Management**: Teacher profiles, assignments, and schedules
- **Attendance System**: Daily attendance tracking and reporting
- **Examination & Grading**: Exam management and grade calculation
- **Parent Portal**: APIs for parent access to student information
- **File Management**: Document upload and management
- **Bulk Operations**: Import/export via CSV and Excel

### Technical Features
- Clean Architecture design
- RESTful API design
- Swagger/OpenAPI documentation
- Docker containerization
- Database migrations with Flyway
- Redis caching
- Comprehensive security
- Multi-factor authentication (MFA) support

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2+
- **Language**: Java 21
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Security**: Spring Security + JWT
- **API Documentation**: OpenAPI 3.0 (Swagger)
- **Database Migration**: Flyway
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15 (if running without Docker)
- Redis 7 (if running without Docker)

## 🔧 Installation & Setup

### 1. Clone the Repository
```bash
cd C:\Users\HimanshuSharma\Downloads\mcpLearning
cd school-management-system
```

### 2. Configure Environment
Update `src/main/resources/application.properties` with your configuration:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/school_mgmt_db
spring.datasource.username=school_admin
spring.datasource.password=school_password

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT Secret (Change this in production!)
jwt.secret=your-secret-key-here
```

### 3. Using Docker Compose (Recommended)
 -
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### 4. Manual Setup (Without Docker)

```bash
# Install dependencies
mvn clean install

# Run database migrations
mvn flyway:migrate

# Run the application
mvn spring-boot:run
```

## 🏃‍♂️ Running the Application

### Development Mode
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Production Mode
```bash
java -jar target/school-management-system-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### Using Docker
```bash
docker-compose up
```

## 📚 API Documentation

### Swagger UI
Once the application is running, access the Swagger UI at:
```
http://localhost:8080/api/swagger-ui.html
```

### API Documentation
```
http://localhost:8080/api/v3/api-docs
```

## 🔐 Authentication

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "tenantId": "public"
  }'
```

### Using JWT Token
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "X-Tenant-ID: public"
```

## 🏗️ Project Structure

```
school-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/schoolmgmt/
│   │   │   ├── domain/              # Domain entities and repositories
│   │   │   │   ├── common/          # Base entities and interfaces
│   │   │   │   ├── tenant/          # Tenant management
│   │   │   │   ├── user/            # User management
│   │   │   │   └── ...
│   │   │   ├── application/         # Business logic and services
│   │   │   │   └── service/
│   │   │   ├── infrastructure/      # Technical implementations
│   │   │   │   ├── multitenancy/    # Multi-tenancy configuration
│   │   │   │   ├── security/        # Security configuration
│   │   │   │   └── ...
│   │   │   └── presentation/        # REST controllers and DTOs
│   │   │       ├── controller/
│   │   │       └── dto/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/        # Flyway migrations
│   └── test/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## 🗄️ Database Schema

The system uses a shared database with tenant isolation through a `tenant_id` column in all tables.

### Key Tables
- `tenants` - School organizations
- `users` - System users
- `students` - Student information
- `teachers` - Teacher information
- `parents` - Parent/Guardian information
- `attendance` - Attendance records
- `exams` - Examination details
- `marks` - Student marks/grades

## 🔒 Security

### Features
- JWT-based authentication
- Role-based authorization (RBAC)
- Password encryption (BCrypt)
- Account lockout after failed attempts
- Email verification
- Password reset functionality
- MFA support (TOTP)
- API rate limiting
- CORS configuration

### User Roles
- `SUPER_ADMIN` - System administrator (cross-tenant)
- `ADMIN` - School administrator
- `TEACHER` - Teaching staff
- `STUDENT` - Students
- `PARENT` - Parents/Guardians
- `STAFF` - Other staff members

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
```

## 📊 Monitoring

### Health Check
```
http://localhost:8080/api/actuator/health
```

### Metrics
```
http://localhost:8080/api/actuator/metrics
```

## 🚢 Deployment

### Building for Production
```bash
# Build JAR
mvn clean package -Pprod

# Build Docker Image
docker build -t school-management-system:latest .

# Push to Registry
docker tag school-management-system:latest your-registry/school-management-system:latest
docker push your-registry/school-management-system:latest
```

### Environment Variables
```bash
SPRING_PROFILES_ACTIVE=prod
DB_HOST=your-db-host
DB_PORT=5432
DB_NAME=school_mgmt_db
DB_USER=school_admin
DB_PASSWORD=secure-password
REDIS_HOST=your-redis-host
REDIS_PORT=6379
JWT_SECRET=your-secure-jwt-secret
```

## 📱 Mobile App Integration

This backend is designed to support mobile applications with:
- RESTful API design
- JWT authentication
- Consistent error handling
- Pagination support
- File upload capabilities
- Real-time updates (WebSocket - planned)

### Mobile SDK Usage Example
```java
// Initialize API client
SchoolApiClient client = new SchoolApiClient("https://api.schoolmgmt.com");

// Login
AuthResponse auth = client.login("username", "password", "tenantId");
client.setAuthToken(auth.getAccessToken());

// Get students
List<Student> students = client.getStudents(classId, sectionId);
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## 👥 Team

- Backend Development Team
- Mobile Development Team (Integration pending)

## 📞 Support

For support, email admin@schoolmgmt.com or raise an issue in the repository.

## 🎯 Roadmap

### Phase 1 ✅
- [x] Multi-tenancy implementation
- [x] Authentication & Authorization
- [x] User Management
- [x] Basic project structure

### Phase 2 (In Progress)
- [ ] Student Management Module
- [ ] Teacher Management Module
- [ ] Parent Management Module

### Phase 3 (Planned)
- [ ] Attendance System
- [ ] Examination & Grading
- [ ] Report Generation

### Phase 4 (Planned)
- [ ] Parent Portal
- [ ] File Management
- [ ] Bulk Import/Export

### Phase 5 (Future)
- [ ] Real-time notifications
- [ ] WebSocket support
- [ ] Analytics dashboard
- [ ] Mobile push notifications

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- PostgreSQL for the robust database
- Docker for containerization
- All contributors and testers

---

**Note**: This is a backend-only implementation. Frontend and mobile applications need to be developed separately to consume these APIs.
