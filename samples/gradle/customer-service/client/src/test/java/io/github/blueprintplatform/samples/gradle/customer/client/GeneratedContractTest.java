package io.github.blueprintplatform.samples.gradle.customer.client;

import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.gradle.customer.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;
import io.github.blueprintplatform.samples.gradle.customer.generated.dto.ServiceResponseCustomerDto;
import io.github.blueprintplatform.samples.gradle.customer.generated.dto.ServiceResponsePageCustomerDto;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GeneratedContractTest {
  @Test
  void clientAdapterExposesSharedContractsWithoutGeneratedTypes() throws Exception {
    assertEquals(
        ServiceResponse.class.getName() + "<" + CustomerDto.class.getName() + ">",
        CustomerClientAdapter.class
            .getMethod("getCustomer", long.class)
            .getGenericReturnType()
            .getTypeName());
    assertEquals(
        ServiceResponse.class.getName()
            + "<"
            + Page.class.getName()
            + "<"
            + CustomerDto.class.getName()
            + ">>",
        CustomerClientAdapter.class.getMethod("getCustomers").getGenericReturnType().getTypeName());
  }

  @Test
  void generatedBindingsReuseContractsAndAreNormalized() throws Exception {
    var dto =
        Path.of(System.getProperty("sample.generatedJava"))
            .resolve("io/github/blueprintplatform/samples/gradle/customer/generated/dto");
    assertEquals(ServiceResponse.class, ServiceResponseCustomerDto.class.getSuperclass());
    assertEquals(ServiceResponse.class, ServiceResponsePageCustomerDto.class.getSuperclass());
    for (var name :
        new String[] {"CustomerDto", "ServiceResponse", "PageCustomerDto", "Meta", "Sort"}) {
      assertFalse(Files.exists(dto.resolve(name + ".java")), "Duplicate shared contract: " + name);
    }
    for (var name : new String[] {"ServiceResponseCustomerDto", "ServiceResponsePageCustomerDto"}) {
      var source = Files.readString(dto.resolve(name + ".java"));
      assertFalse(source.contains("import java.util.Objects;"), source);
      assertFalse(source.contains("import java.util.Arrays;"), source);
      assertTrue(
          source.contains(
              "import io.github.blueprintplatform.samples.gradle.customer.contract.CustomerDto;"),
          source);
      assertTrue(source.contains(name + " extends ServiceResponse<"), source);
      assertTrue(source.endsWith(" {}\n"), source);
    }
  }
}
