package io.github.blueprintplatform.openapi.generics.server.core.schema.base;

import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.PropertyNames;
import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.SchemaNames;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BaseSchemaRegistrarTest {

  private final BaseSchemaRegistrar registrar = new BaseSchemaRegistrar();

  @Test
  @DisplayName("register -> should create all base schemas")
  void shouldRegisterAllBaseSchemas() {
    OpenAPI openApi = new OpenAPI();

    registrar.register(openApi);

    Map<String, Schema> schemas = openApi.getComponents().getSchemas();

    assertNotNull(schemas);
    assertTrue(schemas.containsKey(SchemaNames.SORT));
    assertTrue(schemas.containsKey(SchemaNames.META));
    assertTrue(schemas.containsKey(SchemaNames.SERVICE_RESPONSE));
    assertTrue(schemas.containsKey(SchemaNames.SERVICE_RESPONSE_VOID));
  }

  @Test
  @DisplayName("register -> should be idempotent (no overwrite)")
  void shouldBeIdempotent() {
    OpenAPI openApi = new OpenAPI();

    registrar.register(openApi);
    Map<String, Schema> first = openApi.getComponents().getSchemas();

    registrar.register(openApi);
    Map<String, Schema> second = openApi.getComponents().getSchemas();

    assertSame(first.get(SchemaNames.SORT), second.get(SchemaNames.SORT));
    assertSame(first.get(SchemaNames.META), second.get(SchemaNames.META));
    assertSame(first.get(SchemaNames.SERVICE_RESPONSE), second.get(SchemaNames.SERVICE_RESPONSE));
    assertSame(
        first.get(SchemaNames.SERVICE_RESPONSE_VOID),
        second.get(SchemaNames.SERVICE_RESPONSE_VOID));
  }

  @Test
  @DisplayName("ServiceResponse -> should contain data and meta")
  void shouldCreateServiceResponseStructure() {
    OpenAPI openApi = new OpenAPI();

    registrar.register(openApi);

    Schema<?> schema = openApi.getComponents().getSchemas().get(SchemaNames.SERVICE_RESPONSE);

    assertNotNull(schema.getProperties());
    assertTrue(schema.getProperties().containsKey(PropertyNames.DATA));
    assertTrue(schema.getProperties().containsKey(PropertyNames.META));

    assertNotNull(schema.getRequired());
    assertTrue(schema.getRequired().contains(PropertyNames.META));
  }

  @Test
  @DisplayName("Meta -> should contain serverTime and sort")
  void shouldCreateMetaStructure() {
    OpenAPI openApi = new OpenAPI();

    registrar.register(openApi);

    Schema<?> meta = openApi.getComponents().getSchemas().get(SchemaNames.META);

    assertNotNull(meta.getProperties());
    assertTrue(meta.getProperties().containsKey("serverTime"));
    assertTrue(meta.getProperties().containsKey("sort"));
  }

  @Test
  @DisplayName("Sort -> should contain field and direction enum")
  void shouldCreateSortStructure() {
    OpenAPI openApi = new OpenAPI();

    registrar.register(openApi);

    Schema<?> sort = openApi.getComponents().getSchemas().get(SchemaNames.SORT);

    assertNotNull(sort.getProperties());
    assertTrue(sort.getProperties().containsKey("field"));
    assertTrue(sort.getProperties().containsKey("direction"));

    Schema<?> direction = (Schema<?>) sort.getProperties().get("direction");

    assertNotNull(direction.getEnum());
    assertTrue(direction.getEnum().contains("asc"));
    assertTrue(direction.getEnum().contains("desc"));
  }
}
