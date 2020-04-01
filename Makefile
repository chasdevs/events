SHELL := /bin/bash

OK    := $(shell printf "\e[2D\e[32m✅ ")
WARN  := $(shell printf "\e[2D\e[33m⚠️ \e[1m")
INFO  := $(shell printf "\e[2D\e[36mℹ️ ")
ERROR := $(shell printf "\e[2D\e[31m❗ ")
END   := $(shell printf "\e[0m")

.PHONY: pipeline cli avsc registry

pipeline:
	# $(INFO) Starting local pipeline... $(END)
	@docker-compose up --remove-orphans -d 2> /dev/null
	# $(OK) See local events:        http://sb.local:8001 $(END)
	# $(OK) See local schemas:       http://sb.local:8000 $(END)
	# $(INFO) Local schema registry: http://sb.local:8081 $(END)
	# $(INFO) Local events gateway:  http://sb.local:8001 $(END)
	# $(INFO) Brokers urls:          broker:9092, localhost:29092, host.docker.internal:19092 $(END)

registry:
	# $(INFO) Setting up and populating local schema registry... $(END)
	@docker-compose up -d --remove-orphans schema-registry	
	@ENV=dev ./run-shell.sh sync --force

cli:
	@gradle clean build -xtest 2>&1 > /dev/null
	@./run-shell.sh
