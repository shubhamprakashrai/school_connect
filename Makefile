
RENDER_DEPLOY_HOOK ?= $(shell cat .render-hook 2>/dev/null)
BRANCH ?= development

dockerUp:
	docker-compose up -d

runTunnel:
	cloudflared tunnel --url http://localhost:8080

runSpring:
	mvn spring-boot:run

runAll: dockerUp runTunnel

# --- Deployment ---

# Build Docker image locally to test before deploying
docker-build:
	docker build -t school-connect .

# Run the Docker image locally
docker-run:
	docker run --rm -p 8080:8080 --env-file .env school-connect

# Deploy to Render (triggers deploy via hook URL)
# First time: run `make set-hook URL=<your-render-deploy-hook-url>`
deploy:
	@if [ -z "$(RENDER_DEPLOY_HOOK)" ]; then \
		echo "No deploy hook set. Run: make set-hook URL=<your-render-deploy-hook-url>"; \
		exit 1; \
	fi
	git push origin $(BRANCH)
	curl -s $(RENDER_DEPLOY_HOOK) | head -1
	@echo "\nDeploy triggered. Check Render dashboard for status."

# Deploy via git push only (if Render auto-deploy is on)
deploy-push:
	git push origin $(BRANCH)
	@echo "Pushed to $(BRANCH). Render auto-deploy will pick it up."

# Save your Render deploy hook URL locally
set-hook:
	@echo "$(URL)" > .render-hook
	@echo "Deploy hook saved to .render-hook"

# Build jar locally (for testing)
build:
	mvn clean package -DskipTests

clean:
	mvn clean
