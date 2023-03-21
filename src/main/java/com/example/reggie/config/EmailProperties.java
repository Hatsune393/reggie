package com.example.reggie.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("spring.mail")
@Component
@Data
public class EmailProperties {
    private String host;
    private String username;
    private String password;
}
