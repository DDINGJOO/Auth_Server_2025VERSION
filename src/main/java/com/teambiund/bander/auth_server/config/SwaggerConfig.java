package com.teambiund.bander.auth_server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:9010").description("로컬 개발 서버 (Nginx Load Balancer)"),
                        new Server().url("http://localhost:8080").description("로컬 개발 서버 (Auth Server #1)")
                ))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요 (Bearer 접두사 제외)")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }

    private Info apiInfo() {
    return new Info()
        .title("Bander Auth Server API")
        .description(
            "사용자 인증 및 회원 관리를 담당하는 Auth Server의 REST API 문서입니다.\n\n"
                + "## 주요 기능\n"
                + "- 이메일/소셜 로그인 기반 회원가입 및 로그인\n"
                + "- JWT Access/Refresh 토큰 발급 및 갱신\n"
                + "- 사용자 정보 관리 (상태, 정지, 탈퇴 등)\n"
                + "- 동의서 관리\n"
                + "- 이메일/비밀번호 변경\n\n"
                + "## 아키텍처\n"
                + "- 3-Tier 아키텍처: Nginx Load Balancer → 3개의 Auth Server 인스턴스\n"
                + "- 기술 스택: Spring Boot 3.5.5, Java 21, MariaDB, Redis, Kafka\n\n"
                + "## 보안\n"
                + "- JWT 기반 인증\n"
                + "- 비밀번호 BCrypt 암호화\n"
                + "- 이메일 AES-256 암호화\n"
                + "- Bean Validation 입력값 검증")
        .version("0.0.3_proto")
        .contact(new Contact().name("Bander Development Team").email("ddingsha9@teambind.co,kr"))
        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT"));
    }
}
