# School Management System

A comprehensive multi-tenant School Management System built with Spring Boot.

## Environment Variables

The application supports environment variables for configuration, particularly for database credentials. This is useful for:
- Docker deployments
- Production environments
- Security (avoiding hardcoded credentials)

### Database Configuration

The following environment variables can be set to override default database settings:

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://host.docker.internal:5432/school_mgmt_db` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `school_admin` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `school_password` |

### Redis Configuration

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `SPRING_DATA_REDIS_HOST` | Redis host | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Redis port | `6379` |

### Spring Profile

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |

## Local Development

### Using Environment Variables

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` with your actual database credentials:
   ```bash
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/school_mgmt_db
   SPRING_DATASOURCE_USERNAME=your_username
   SPRING_DATASOURCE_PASSWORD=your_password
   ```

3. Load the environment variables before running the application:
   ```bash
   export $(cat .env | xargs)
   mvn spring-boot:run
   ```

   Or use a tool like `direnv` or `dotenv` to automatically load environment variables.

### Without Environment Variables

If you don't set environment variables, the application will use the default values from `application.properties`.

## Docker Deployment

When using Docker Compose, the environment variables are automatically set in `docker-compose.yml`:

```bash
docker-compose up
```

The docker-compose configuration sets:
- `SPRING_DATASOURCE_URL` to connect to the PostgreSQL container
- `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` from the PostgreSQL container
- `SPRING_DATA_REDIS_HOST` to connect to the Redis container
- `SPRING_PROFILES_ACTIVE` to `docker`

## Security Notes

- **Never commit `.env` files** to version control (already in `.gitignore`)
- Use strong passwords in production
- Consider using secrets management tools (e.g., AWS Secrets Manager, HashiCorp Vault) for production deployments
- Rotate credentials regularly

## Building

```bash
mvn clean package
```

## Running Tests

```bash
mvn test
```
