


dockerUp:
	docker-compose up -d

runTunnel:
	cloudflared tunnel --url http://localhost:8080

runSpring:
	mvn spring-boot:run

runAll: dockerUp runTunnel
