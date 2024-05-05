package org.example.xchange.configuration;


import io.netty.handler.logging.LogLevel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("xchange.integration")
@Configuration
@Data
public class XchangeProperty {
    private String lithuaniaBaseUrl;
    private String lithuaniaBasePath;
    private int connectTimeoutMs;
    private int readTimeoutMs;
    private LogLevel logLevel;
    private int writeTimeoutMs;
}
