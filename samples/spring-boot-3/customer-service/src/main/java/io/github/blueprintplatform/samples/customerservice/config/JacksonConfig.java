package io.github.blueprintplatform.samples.customerservice.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  @Bean
  public SimpleModule sortDirectionModule() {
    SimpleModule module = new SimpleModule();

    module.addSerializer(
        SortDirection.class,
        new JsonSerializer<>() {
          @Override
          public void serialize(
              SortDirection value, JsonGenerator gen, SerializerProvider serializers)
              throws IOException {
            gen.writeString(value.value());
          }
        });

    module.addDeserializer(
        SortDirection.class,
        new JsonDeserializer<>() {
          @Override
          public SortDirection deserialize(JsonParser parser, DeserializationContext context)
              throws IOException {
            return SortDirection.from(parser.getValueAsString());
          }
        });

    return module;
  }
}