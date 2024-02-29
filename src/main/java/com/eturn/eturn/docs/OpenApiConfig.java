package com.eturn.eturn.docs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Eturn application",
                description = "Creating turns for the students", version = "1.5.0",
                contact = @Contact(
                        name = "Eteam production",
                        email = "aavasilev@stud.etu.ru",
                        url = "http://eturn.ru/contacts"
                )
        )
)
public class OpenApiConfig {
}
