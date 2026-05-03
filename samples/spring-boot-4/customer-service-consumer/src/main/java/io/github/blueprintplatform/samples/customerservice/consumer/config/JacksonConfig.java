package io.github.blueprintplatform.samples.customerservice.consumer.config;

import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule sortDirectionModule() {
        SimpleModule module = new SimpleModule();

        module.addSerializer(
                SortDirection.class,
                new ValueSerializer<>() {
                    @Override
                    public void serialize(SortDirection value, JsonGenerator gen, SerializationContext ctxt)
                            throws JacksonException {
                        gen.writeString(value.value());
                    }
                });

        module.addDeserializer(
                SortDirection.class,
                new ValueDeserializer<>() {
                    @Override
                    public SortDirection deserialize(JsonParser parser, DeserializationContext ctxt)
                            throws JacksonException {
                        return SortDirection.from(parser.getValueAsString());
                    }
                });

        return module;
    }
}