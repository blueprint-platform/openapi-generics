package io.github.blueprintplatform.openapi.generics.server.mvc;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Spring MVC implementation of {@link ResponseTypeDiscoveryStrategy}.
 *
 * <p>Scans Spring MVC handler mappings, discovers controller handler methods, and extracts their
 * declared return types as {@link ResolvableType} instances.
 *
 * <p>This strategy intentionally uses Spring's {@code HandlerMethod} return type abstraction rather
 * than reading the raw Java {@code Method} directly. This keeps discovery aligned with how Spring
 * MVC represents handler metadata internally.
 *
 * <p>This class is intentionally limited to discovery only. It does not interpret generic
 * structures, apply contract rules, or perform OpenAPI schema logic.
 */
public class MvcResponseTypeDiscoveryStrategy implements ResponseTypeDiscoveryStrategy {

  private final ListableBeanFactory beanFactory;

  public MvcResponseTypeDiscoveryStrategy(ListableBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
  public Set<ResolvableType> discover() {
    Set<ResolvableType> result = new LinkedHashSet<>();

    Map<String, RequestMappingHandlerMapping> mappings =
        beanFactory.getBeansOfType(RequestMappingHandlerMapping.class);

    if (mappings.isEmpty()) {
      return result;
    }

    mappings
        .values()
        .forEach(
            mapping ->
                mapping
                    .getHandlerMethods()
                    .values()
                    .forEach(
                        handlerMethod -> {
                          MethodParameter returnType = handlerMethod.getReturnType();
                          ResolvableType type = ResolvableType.forMethodParameter(returnType);

                          if (type.resolve() != null) {
                            result.add(type);
                          }
                        }));

    return result;
  }
}
