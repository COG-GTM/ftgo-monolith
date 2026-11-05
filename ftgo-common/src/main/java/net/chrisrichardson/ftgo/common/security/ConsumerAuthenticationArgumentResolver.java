package net.chrisrichardson.ftgo.common.security;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public class ConsumerAuthenticationArgumentResolver implements HandlerMethodArgumentResolver {

  private final ConsumerTokenService consumerTokenService;

  public ConsumerAuthenticationArgumentResolver(ConsumerTokenService consumerTokenService) {
    this.consumerTokenService = consumerTokenService;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType() == AuthenticatedConsumer.class;
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    String authorization = webRequest.getHeader("Authorization");
    if (authorization != null) {
      String[] parts = authorization.trim().split("\\s+");
      if (parts.length == 2 && "Bearer".equalsIgnoreCase(parts[0])) {
        Optional<AuthenticatedConsumer> consumer = consumerTokenService.verify(parts[1]);
        if (consumer.isPresent()) {
          return consumer.get();
        }
      }
    }

    HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
    if (response != null) {
      response.setHeader("WWW-Authenticate", "Bearer");
    }
    throw new AuthenticationRequiredException();
  }
}
