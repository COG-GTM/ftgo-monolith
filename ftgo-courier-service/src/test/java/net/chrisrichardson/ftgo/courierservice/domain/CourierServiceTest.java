package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CourierServiceTest {

  @Mock
  private CourierRepository courierRepository;
  @Mock
  private CourierCredentialsRepository credentialsRepository;

  private PasswordEncoder passwordEncoder;
  private CourierService courierService;

  @Before
  public void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    courierService = new CourierService(courierRepository, credentialsRepository, passwordEncoder, "operator");
    when(courierRepository.findById(1L))
            .thenReturn(Optional.of(new Courier(new PersonName("Carol", "Courier"), new Address())));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectShortPassword() {
    courierService.setCredentials(1L, "carol", "too-short");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectOperatorUsername() {
    courierService.setCredentials(1L, "Operator", "long-enough-password");
  }

  @Test(expected = CourierNotFoundException.class)
  public void shouldRejectUnknownCourier() {
    when(courierRepository.findById(99L)).thenReturn(Optional.empty());
    courierService.setCredentials(99L, "carol", "long-enough-password");
  }

  @Test(expected = CourierUsernameTakenException.class)
  public void shouldRejectUsernameOwnedByAnotherCourier() {
    when(credentialsRepository.findByUsername("dave"))
            .thenReturn(Optional.of(new CourierCredentials(2L)));

    courierService.setCredentials(1L, "dave", "long-enough-password");
  }

  @Test
  public void shouldStoreBcryptEncodedPassword() {
    when(credentialsRepository.findByUsername("carol")).thenReturn(Optional.empty());
    when(credentialsRepository.findById(1L)).thenReturn(Optional.empty());
    when(credentialsRepository.saveAndFlush(any(CourierCredentials.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    courierService.setCredentials(1L, "carol", "a-long-password");

    ArgumentCaptor<CourierCredentials> captor = ArgumentCaptor.forClass(CourierCredentials.class);
    verify(credentialsRepository).saveAndFlush(captor.capture());
    assertTrue(passwordEncoder.matches("a-long-password", captor.getValue().getPasswordHash()));
    assertFalse("a-long-password".equals(captor.getValue().getPasswordHash()));
  }

  @Test(expected = CourierUsernameTakenException.class)
  public void shouldTranslateDataIntegrityViolationWhenSavingCredentials() {
    when(credentialsRepository.findByUsername("carol")).thenReturn(Optional.empty());
    when(credentialsRepository.findById(1L)).thenReturn(Optional.empty());
    when(credentialsRepository.saveAndFlush(any(CourierCredentials.class)))
            .thenThrow(new DataIntegrityViolationException("username collision"));

    courierService.setCredentials(1L, "carol", "a-long-password");
  }

  @Test
  public void shouldPropagateDatabaseResourceFailureWhenSavingCredentials() {
    when(credentialsRepository.findByUsername("carol")).thenReturn(Optional.empty());
    when(credentialsRepository.findById(1L)).thenReturn(Optional.empty());
    DataAccessResourceFailureException databaseFailure = new DataAccessResourceFailureException("db down");
    when(credentialsRepository.saveAndFlush(any(CourierCredentials.class))).thenThrow(databaseFailure);

    try {
      courierService.setCredentials(1L, "carol", "a-long-password");
      fail("Expected database resource failure");
    } catch (DataAccessResourceFailureException e) {
      assertSame(databaseFailure, e);
    }
  }
}
