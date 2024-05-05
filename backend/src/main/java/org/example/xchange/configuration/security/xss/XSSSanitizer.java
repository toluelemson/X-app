package org.example.xchange.configuration.security.xss;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.Objects;

import static org.jsoup.parser.Parser.unescapeEntities;

@JsonComponent
public class XSSSanitizer extends JsonDeserializer<String> implements ContextualDeserializer {

    public static final PolicyFactory POLICY_FACTORY = new HtmlPolicyBuilder()
            .allowCommonInlineFormattingElements()
            .toFactory();

    @Override
    public String deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String value = parser.getValueAsString();
        if (Objects.isNull(value) || value.isEmpty()) return value;
        return unescapeEntities(POLICY_FACTORY.sanitize(value), true);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty property) {
        return this;
    }


}
