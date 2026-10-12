package com.sonddd.paywallet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình tài liệu OpenAPI/Swagger, thêm nút "Authorize" cho JWT Bearer
 * để có thể gọi các API cần đăng nhập ngay trên Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI payWalletOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PayWallet API")
                        .version("1.0.0")
                        .description("Hệ thống Ví điện tử & Thanh toán (mô phỏng) - Demo Backend Java/Spring")
                        .contact(new Contact().name("sonddd").email("sonddd17@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .name(SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
