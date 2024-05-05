package org.example.xchange.configuration;

import io.netty.handler.logging.LogLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;
import reactor.netty.http.HttpProtocol;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfiguration {
    private final XchangeProperty xchangeProperty;

    public WebClientConfiguration(XchangeProperty xchangeProperty) {
        this.xchangeProperty = xchangeProperty;
    }

    @Bean(name = "XchangeWebClient")
    public WebClient webClient() {
        HttpClient httpClient = getHttpClient();
        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder()
                .clientConnector(connector)
                .build();
    }

    private HttpClient getHttpClient() {
        int connectTimeout = xchangeProperty.getConnectTimeoutMs() == 0 ? 5_000 : xchangeProperty.getConnectTimeoutMs();
        long readTimeout = xchangeProperty.getReadTimeoutMs() == 0 ? 30_000 : xchangeProperty.getReadTimeoutMs();
        long writeTimeout = xchangeProperty.getWriteTimeoutMs() == 0 ? 30_000 : xchangeProperty.getWriteTimeoutMs();
        LogLevel logLevel = xchangeProperty.getLogLevel() == null ? LogLevel.INFO : xchangeProperty.getLogLevel();

        ConnectionProvider connectionProvider = getConnectionProvider();
        LoopResources loopResources = LoopResources.create("event-loops");

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .keepAlive(true)
                .baseUrl(xchangeProperty.getLithuaniaBaseUrl())
                .secure()
                .protocol(HttpProtocol.H2)
                .compress(true)
                .doOnConnected(conn ->
                        conn.addHandlerFirst(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS)))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .runOn(loopResources)
                .wiretap("reactor.netty.http.client.HttpClient", logLevel, AdvancedByteBufFormat.TEXTUAL, StandardCharsets.UTF_8)
                .followRedirect(true)
                .responseTimeout(Duration.ofMillis(readTimeout));

        httpClient.warmup().block();
        return httpClient;
    }

    private ConnectionProvider getConnectionProvider() {
        return ConnectionProvider.builder("custom")
                .maxConnections(100)
                .pendingAcquireMaxCount(-1)
                .maxIdleTime(Duration.ofSeconds(60))
                .maxLifeTime(Duration.ofMinutes(30))
                .build();
    }

    private LoopResources loopResources() {
        return LoopResources.create("netty-loop", 4, true);
    }
}
