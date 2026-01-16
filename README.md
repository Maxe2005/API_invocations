

## Démarer

### Le docker

```bach
docker compose up
```

### L'app en local

```bach
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

## Vider et relancer BDD par défaut

```bach
docker compose down -v
docker compose up -d --build
```
