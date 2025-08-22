package com.devmode.shop.global.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "naver.datalab")
public class DataLabApiProperties {

    private String clientId;
    private String clientSecret;
    private String apiUrl;
    private Integer maxDailyCalls;
    private Integer warningThreshold;
    private Integer cacheTtl;
}
