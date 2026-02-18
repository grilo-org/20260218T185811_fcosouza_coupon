package com.example.couponapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração global da documentação OpenAPI 3 / Swagger UI.
 *
 * Swagger UI disponível em: http://localhost:8080/swagger-ui.html
 * JSON spec disponível em:  http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI couponApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Coupon API")
                        .description(
                                "API REST para gerenciamento de **cupons de desconto**.\n\n" +
                                "### Regras de negócio\n" +
                                "- O campo `code` deve resultar em **exatamente 6 caracteres alfanuméricos** " +
                                "após remoção automática de caracteres especiais.\n" +
                                "- `discountValue` mínimo: **0.5**.\n" +
                                "- `expirationDate` não pode estar no passado.\n" +
                                "- Deleção é **soft delete** (dado preservado no banco).\n" +
                                "- Não é possível deletar um cupom já removido."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Coupon API Team")
                                .email("dev@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor local de desenvolvimento")
                ));
    }
}
