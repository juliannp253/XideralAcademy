package com.logistic.fast_track.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Logística Fast Track",
                version = "1.0",
                description = "API REST para la gestión de envíos aéreos y terrestres.",
                contact = @Contact(
                        name = "Julian Padron / Xideral",
                        email = "julian.padronnunez03@gmail.com"
                )
        )
)
public class SwaggerConfig {
}
