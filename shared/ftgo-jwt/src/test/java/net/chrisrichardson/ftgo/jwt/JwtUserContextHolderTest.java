package net.chrisrichardson.ftgo.jwt;

import net.chrisrichardson.ftgo.jwt.model.FtgoUserContext;
import net.chrisrichardson.ftgo.jwt.util.JwtUserContextHolder;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link JwtUserContextHolder}.
 *
 * <p>Tests verify thread-local user context management including
 * setting, getting, clearing, and authentication checks.</p>
 */
public class JwtUserContextHolderTest {

    @After
    public void tearDown() {
        JwtUserContextHolder.clear();
    }

    @Test
    public void shouldReturnEmptyWhenNoUserContextSet() {
        Optional<FtgoUserContext> user = JwtUserContextHolder.getCurrentUser();
        assertFalse(user.isPresent());
    }

    @Test
    public void shouldReturnUserContextWhenSet() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("testuser")
                .roles(Arrays.asList("USER"))
                .build();

        JwtUserContextHolder.setCurrentUser(context);

        Optional<FtgoUserContext> retrieved = JwtUserContextHolder.getCurrentUser();
        assertTrue(retrieved.isPresent());
        assertEquals("user-1", retrieved.get().getUserId());
        assertEquals("testuser", retrieved.get().getUsername());
    }

    @Test
    public void shouldReturnUserIdWhenSet() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-123")
                .username("testuser")
                .build();

        JwtUserContextHolder.setCurrentUser(context);

        Optional<String> userId = JwtUserContextHolder.getCurrentUserId();
        assertTrue(userId.isPresent());
        assertEquals("user-123", userId.get());
    }

    @Test
    public void shouldReturnUsernameWhenSet() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .build();

        JwtUserContextHolder.setCurrentUser(context);

        Optional<String> username = JwtUserContextHolder.getCurrentUsername();
        assertTrue(username.isPresent());
        assertEquals("john.doe", username.get());
    }

    @Test
    public void shouldReportAuthenticatedWhenContextSet() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("testuser")
                .build();

        JwtUserContextHolder.setCurrentUser(context);
        assertTrue(JwtUserContextHolder.isAuthenticated());
    }

    @Test
    public void shouldReportNotAuthenticatedWhenContextNotSet() {
        assertFalse(JwtUserContextHolder.isAuthenticated());
    }

    @Test
    public void shouldClearUserContext() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("testuser")
                .build();

        JwtUserContextHolder.setCurrentUser(context);
        assertTrue(JwtUserContextHolder.isAuthenticated());

        JwtUserContextHolder.clear();
        assertFalse(JwtUserContextHolder.isAuthenticated());
        assertFalse(JwtUserContextHolder.getCurrentUser().isPresent());
    }

    @Test
    public void shouldRequireCurrentUserWhenSet() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("testuser")
                .build();

        JwtUserContextHolder.setCurrentUser(context);

        FtgoUserContext required = JwtUserContextHolder.requireCurrentUser();
        assertNotNull(required);
        assertEquals("user-1", required.getUserId());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenRequiringCurrentUserButNoneSet() {
        JwtUserContextHolder.requireCurrentUser();
    }

    @Test
    public void shouldReturnEmptyUserIdWhenNotSet() {
        Optional<String> userId = JwtUserContextHolder.getCurrentUserId();
        assertFalse(userId.isPresent());
    }

    @Test
    public void shouldReturnEmptyUsernameWhenNotSet() {
        Optional<String> username = JwtUserContextHolder.getCurrentUsername();
        assertFalse(username.isPresent());
    }
}
