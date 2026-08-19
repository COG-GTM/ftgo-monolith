package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsumerAuthenticatorTest {

  private ConsumerRepository consumerRepository;
  private ConsumerAuthenticator consumerAuthenticator;

  @Before
  public void setUp() {
    consumerRepository = mock(ConsumerRepository.class);
    consumerAuthenticator = new ConsumerAuthenticator();
    ReflectionTestUtils.setField(consumerAuthenticator, "consumerRepository", consumerRepository);
  }

  @Test(expected = ConsumerAuthenticationException.class)
  public void shouldRejectRequestWithoutAuthorizationHeader() {
    consumerAuthenticator.authenticate(new MockHttpServletRequest());
  }

  @Test(expected = ConsumerAuthenticationException.class)
  public void shouldRejectRequestWithBlankApiKey() {
    consumerAuthenticator.authenticate(requestWithApiKey(" "));
  }

  @Test(expected = ConsumerAuthenticationException.class)
  public void shouldRejectUnknownApiKey() {
    when(consumerRepository.findByApiKeyHash(ConsumerApiKeys.hash("nope"))).thenReturn(Optional.empty());

    consumerAuthenticator.authenticate(requestWithApiKey("nope"));
  }

  @Test
  public void shouldResolveTheConsumerOwningTheApiKey() {
    String apiKey = ConsumerApiKeys.generateApiKey();
    Consumer consumer = new Consumer(new PersonName("John", "Doe"), ConsumerApiKeys.hash(apiKey));
    when(consumerRepository.findByApiKeyHash(ConsumerApiKeys.hash(apiKey))).thenReturn(Optional.of(consumer));

    assertSame(consumer, consumerAuthenticator.authenticate(requestWithApiKey(apiKey)));
  }

  @Test
  public void shouldHashApiKeysToASha256HexDigest() {
    assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", ConsumerApiKeys.hash("hello"));
  }

  private MockHttpServletRequest requestWithApiKey(String apiKey) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + apiKey);
    return request;
  }
}
