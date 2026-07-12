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

logs-gateway:
	docker compose --env-file $(ENV_FILE) logs -f gateway-app

gen-ssh-keys:
	@mkdir -p ssh-keys
	@openssl genrsa -out ssh-keys/private.pem 2048
	@openssl rsa -in ssh-keys/private.pem -pubout -out ssh-keys/public.pem
	@chmod 644 ssh-keys/private.pem
	@chmod 644 ssh-keys/public.pem

stop-auth:
	docker compose --env-file $(ENV_FILE) stop auth-app

start-auth:
	docker compose --env-file $(ENV_FILE) start auth-app

stop-reservation:
	docker compose --env-file $(ENV_FILE) stop reservation-app

start-reservation:
	docker compose --env-file $(ENV_FILE) start reservation-app