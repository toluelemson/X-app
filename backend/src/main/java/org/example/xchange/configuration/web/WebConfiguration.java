package org.example.xchange.configuration.web;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.HttpMessageDecoder;
import org.springframework.http.codec.HttpMessageEncoder;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@EnableWebFlux
@Configuration
public class WebConfiguration implements WebFluxConfigurer {
    private final HttpMessageEncoder<Object> jsonEncoder;
    private final HttpMessageDecoder<Object> jsonDecoder;

    public WebConfiguration(@Qualifier("jsonEncoder") HttpMessageEncoder<Object> jsonEncoder,
                            @Qualifier("jsonDecoder") HttpMessageDecoder<Object> jsonDecoder) {
        this.jsonEncoder = jsonEncoder;
        this.jsonDecoder = jsonDecoder;
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        CodecConfigurer.CustomCodecs customCodecs = configurer.customCodecs();
        customCodecs.registerWithDefaultConfig(jsonEncoder);
        customCodecs.registerWithDefaultConfig(jsonDecoder);
    }

    @Override
    public void configurePathMatching(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/v1", HandlerTypePredicate.forAnnotation(RestController.class));
    }
}