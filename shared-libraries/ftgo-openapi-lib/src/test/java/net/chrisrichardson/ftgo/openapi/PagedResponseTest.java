package net.chrisrichardson.ftgo.openapi;

import net.chrisrichardson.ftgo.openapi.pagination.PagedResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PagedResponseTest {

    @Test
    void shouldCreatePagedResponse() {
        List<String> content = List.of("item1", "item2", "item3");
        PagedResponse<String> response = new PagedResponse<>(content, 0, 20, 100, 5);

        assertEquals(content, response.getContent());
        assertEquals(0, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(100, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
    }

    @Test
    void shouldDetectLastPage() {
        List<String> content = List.of("item1");
        PagedResponse<String> response = new PagedResponse<>(content, 4, 20, 100, 5);

        assertFalse(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    void shouldHandleSinglePage() {
        List<String> content = List.of("item1");
        PagedResponse<String> response = new PagedResponse<>(content, 0, 20, 1, 1);

        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }
}
