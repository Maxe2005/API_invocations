package com.imt.api_invocations.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("API Invocations - Gatch Game")
                        .description("API de gestion des invocations de monstres et de leurs compétences pour le jeu Gatcha. " +
                                "Cette API permet de créer, consulter, modifier et supprimer des monstres et leurs compétences, " +
                                "ainsi que d'effectuer des invocations aléatoires basées sur des taux de drop configurables.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Équipe IMT Gatch")
                                .url("https://github.com/Maxe2005/GatchaApi")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8085")
                                .description("Serveur de développement local"),
                        new Server()
                                .url("http://localhost:8085")
                                .description("Serveur Docker local")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT pour l'authentification. " +
                                                "Format: Bearer <token>")));
    }
}
