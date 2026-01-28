.PHONY: build run stop clean logs shell-backend shell-postgres shell-keycloak db-reset

# Build the Docker image
build:
	@echo "Building Docker images..."
	docker-compose build --no-cache

# Start all services
run:
	@echo "Starting SIMs application..."
	docker-compose up -d

# Stop all services
stop:
	@echo "Stopping SIMs application..."
	docker-compose down

# Stop and remove all containers, volumes, and networks
clean:
	@echo "Cleaning up..."
	docker-compose down -v --remove-orphans
	docker system prune -f

# View logs
logs:
	@echo "Showing logs..."
	docker-compose logs -f

# View backend logs
logs-backend:
	docker-compose logs -f backend

# View database logs
logs-postgres:
	docker-compose logs -f postgres

# View Keycloak logs
logs-keycloak:
	docker-compose logs -f keycloak

# Open shell in backend container
shell-backend:
	docker-compose exec backend sh

# Open shell in PostgreSQL container
shell-postgres:
	docker-compose exec postgres psql -U syte_user -d syte_db

# Open shell in Keycloak container
shell-keycloak:
	docker-compose exec keycloak /bin/bash

# Reset database (WARNING: Deletes all data!)
db-reset:
	@echo "Resetting database..."
	docker-compose down -v
	docker volume rm -f sims-backend_postgres_data
	@echo "Database reset complete. Run 'make run' to start fresh."

# Check service status
status:
	@echo "Service Status:"
	docker-compose ps

# Health check
health:
	@echo "Checking service health..."
	@echo "Backend:"
	@curl -f http://localhost:8080/api/actuator/health || echo "Backend is not healthy"
	@echo ""
	@echo "PostgreSQL:"
	@docker-compose exec postgres pg_isready -U syte_user -d syte_db || echo "PostgreSQL is not ready"
	@echo ""
	@echo "Keycloak:"
	@curl -f http://localhost:8081/health/ready || echo "Keycloak is not ready"

# Build and run
all: build run
	@echo "Build and start complete!"
	@echo "Frontend: http://localhost:3000"
	@echo "Backend API: http://localhost:8080/api"
	@echo "Swagger UI: http://localhost:8080/api/swagger-ui.html"
	@echo "Keycloak Admin: http://localhost:8081 (admin/admin123)"

# Help
help:
	@echo "Available commands:"
	@echo "  make build        - Build Docker images"
	@echo "  make run          - Start all services"
	@echo "  make stop         - Stop all services"
	@echo "  make clean        - Stop and remove everything"
	@echo "  make logs         - View all logs"
	@echo "  make logs-backend - View backend logs"
	@echo "  make status       - Check service status"
	@echo "  make health       - Check service health"
	@echo "  make all          - Build and start everything"
	@echo "  make help         - Show this help"