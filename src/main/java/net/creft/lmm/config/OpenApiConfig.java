package net.creft.lmm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI libreMediaManagerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Libre Media Manager API")
                        .version("v1")
                        .description("REST API for creating, listing, updating, and deleting media records."));
    }
}
