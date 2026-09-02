package net.chrisrichardson.ftgo.common.security;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authorizes privileged API calls using per-role shared secrets presented as
 * {@code Authorization: Bearer <key>}. A role whose key is not configured can never
 * authenticate, so the endpoints it protects fail closed.
 */
public class RoleApiKeyAuthorizer {

  static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final Map<ApiRole, byte[]> keysByRole;

  public RoleApiKeyAuthorizer(Map<ApiRole, String> keysByRole) {
    Map<ApiRole, byte[]> keys = new EnumMap<>(ApiRole.class);
    keysByRole.forEach((role, key) -> {
      if (key != null && !key.trim().isEmpty()) {
        byte[] keyBytes = key.trim().getBytes(StandardCharsets.UTF_8);
        keys.forEach((otherRole, otherKey) -> {
          if (MessageDigest.isEqual(otherKey, keyBytes)) {
            throw new IllegalStateException("API key for role " + role + " must differ from the key for role " + otherRole);
          }
        });
        keys.put(role, keyBytes);
      }
    });
    this.keysByRole = Collections.unmodifiableMap(keys);
  }

  public ApiRole requireRole(HttpServletRequest request, ApiRole... allowedRoles) {
    ApiRole role = authenticate(request);
    if (!Arrays.asList(allowedRoles).contains(role)) {
      throw new ApiAccessDeniedException("Role " + role + " is not permitted to perform this action");
    }
    return role;
  }

  public ApiRole authenticate(HttpServletRequest request) {
    String presented = bearerToken(request)
            .orElseThrow(() -> new ApiAuthenticationException("An API key is required"));
    byte[] presentedBytes = presented.getBytes(StandardCharsets.UTF_8);
    for (Map.Entry<ApiRole, byte[]> entry : keysByRole.entrySet()) {
      if (MessageDigest.isEqual(entry.getValue(), presentedBytes)) {
        return entry.getKey();
      }
    }
    throw new ApiAuthenticationException("Invalid API key");
  }

  private Optional<String> bearerToken(HttpServletRequest request) {
    String header = request.getHeader(AUTHORIZATION_HEADER);
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      return Optional.empty();
    }
    String token = header.substring(BEARER_PREFIX.length()).trim();
    return token.isEmpty() ? Optional.empty() : Optional.of(token);
  }
}
