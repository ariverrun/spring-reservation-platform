ENV_FILE := $(if $(wildcard .env.local),.env.local,.env)

build:
	docker compose --env-file $(ENV_FILE) build

up:
	docker compose --env-file $(ENV_FILE) up -d

down:
	docker compose --env-file $(ENV_FILE) down

logs-reservation:
	docker compose --env-file $(ENV_FILE) logs -f reservation-app

logs-auth:
	docker compose --env-file $(ENV_FILE) logs -f auth-app
