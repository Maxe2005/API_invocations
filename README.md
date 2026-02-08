

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

## Swager

```html
http://localhost:8085/swagger-ui/index.html
```

## Visualiser la BDD posgreSQL avec pgAdmin

1. Ouvre ton navigateur sur http://localhost:5050
2. Connecte-toi avec :
    - Email : admin@admin.com
    - Password : admin
3. Une fois connecté, ajoute un serveur :
    - Clic droit "Servers" → "Register" → "Server"
    - Onglet "General" → Name : api_invocations
    - Onglet "Connection" :
        - Host : postgres
        - Port : 5432
        - Database : api_invocationsdb
        - Username : api_invocations
        - Password : api_invocations
