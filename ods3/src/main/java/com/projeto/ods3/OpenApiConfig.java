package com.projeto.ods3;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Documentação OpenAPI — Swagger UI em {@code /swagger-ui.html}.
 *
 * <p>Todo endpoint exige o esquema {@value #BEARER_AUTH}, exceto os marcados com
 * {@code @SecurityRequirements} vazio (cadastro e login). Para testar pela UI: cadastrar,
 * fazer login, copiar o token e colá-lo em "Authorize".
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Plataforma de Compartilhamento de Treinos — ODS 3",
                version = "1.2",
                description = "Cadastro de treinos, comentários e avaliação de comentários com like/dislike. "
                        + "Treinos são endereçados pelo nome e comentários pelo número dentro do treino."),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
@SecurityScheme(name = OpenApiConfig.BEARER_AUTH, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
}
