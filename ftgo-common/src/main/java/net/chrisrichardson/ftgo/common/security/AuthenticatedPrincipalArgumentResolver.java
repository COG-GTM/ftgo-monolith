package net.chrisrichardson.ftgo.common.security;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AuthenticatedPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

  private final AuthTokenService authTokenService;

  public AuthenticatedPrincipalArgumentResolver(AuthTokenService authTokenService) {
    this.authTokenService = authTokenService;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(AuthenticatedPrincipal.class);
  }

  @Override
  public Object resolveArgument(MethodParameter parameter,
                                ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest,
                                WebDataBinderFactory binderFactory) {
    String authorization = webRequest.getHeader("Authorization");
    if (authorization == null || authorization.length() < 7 ||
            !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
      throw new UnauthenticatedException("A valid Bearer token is required");
    }
    return authTokenService.verify(authorization.substring(7))
            .orElseThrow(() -> new UnauthenticatedException("A valid Bearer token is required"));
  }
}
