package io.github.blueprintplatform.openapi.generics.server.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Tag("unit")
@DisplayName("Unit Test: MvcResponseTypeDiscoveryStrategy")
class MvcResponseTypeDiscoveryStrategyTest {

  @Test
  @DisplayName("discover -> should return empty when no handler mappings exist")
  void discover_shouldReturnEmpty_whenNoMappings() {
    ListableBeanFactory beanFactory = mock(ListableBeanFactory.class);
    when(beanFactory.getBeansOfType(RequestMappingHandlerMapping.class)).thenReturn(Map.of());

    MvcResponseTypeDiscoveryStrategy strategy = new MvcResponseTypeDiscoveryStrategy(beanFactory);

    Set<ResolvableType> result = strategy.discover();

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("discover -> should extract return types from handler methods")
  void discover_shouldExtractReturnTypes() throws Exception {
    SampleController controller = new SampleController();
    Method method = SampleController.class.getMethod("getString");

    HandlerMethod handlerMethod = new HandlerMethod(controller, method);

    RequestMappingHandlerMapping mapping = mock(RequestMappingHandlerMapping.class);
    Map<RequestMappingInfo, HandlerMethod> handlerMethods = new HashMap<>();
    handlerMethods.put(RequestMappingInfo.paths("/test").build(), handlerMethod);
    when(mapping.getHandlerMethods()).thenReturn(handlerMethods);

    ListableBeanFactory beanFactory = mock(ListableBeanFactory.class);
    when(beanFactory.getBeansOfType(RequestMappingHandlerMapping.class))
        .thenReturn(Map.of("handlerMapping", mapping));

    MvcResponseTypeDiscoveryStrategy strategy = new MvcResponseTypeDiscoveryStrategy(beanFactory);

    Set<ResolvableType> result = strategy.discover();

    assertThat(result).hasSize(1);
    assertThat(result.iterator().next().resolve()).isEqualTo(String.class);
  }

  @Test
  @DisplayName("discover -> should merge types from multiple mappings")
  void discover_shouldMergeMultipleMappings() throws Exception {
    SampleController controller = new SampleController();

    Method stringMethod = SampleController.class.getMethod("getString");
    Method intMethod = SampleController.class.getMethod("getInteger");

    RequestMappingHandlerMapping mapping1 = mock(RequestMappingHandlerMapping.class);
    Map<RequestMappingInfo, HandlerMethod> handlers1 = new HashMap<>();
    handlers1.put(
        RequestMappingInfo.paths("/a").build(), new HandlerMethod(controller, stringMethod));
    when(mapping1.getHandlerMethods()).thenReturn(handlers1);

    RequestMappingHandlerMapping mapping2 = mock(RequestMappingHandlerMapping.class);
    Map<RequestMappingInfo, HandlerMethod> handlers2 = new HashMap<>();
    handlers2.put(RequestMappingInfo.paths("/b").build(), new HandlerMethod(controller, intMethod));
    when(mapping2.getHandlerMethods()).thenReturn(handlers2);

    ListableBeanFactory beanFactory = mock(ListableBeanFactory.class);
    when(beanFactory.getBeansOfType(RequestMappingHandlerMapping.class))
        .thenReturn(Map.of("m1", mapping1, "m2", mapping2));

    MvcResponseTypeDiscoveryStrategy strategy = new MvcResponseTypeDiscoveryStrategy(beanFactory);

    Set<ResolvableType> result = strategy.discover();

    assertThat(result).hasSize(2);
  }

  // --- Test fixture ---

  static class SampleController {
    @RequestMapping("/string")
    public String getString() {
      return "test";
    }

    @RequestMapping("/int")
    public Integer getInteger() {
      return 42;
    }
  }
}
