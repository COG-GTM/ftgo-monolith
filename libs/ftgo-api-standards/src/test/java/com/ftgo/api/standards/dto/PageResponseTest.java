package com.ftgo.api.standards.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResponseTest {

    @Test
    void createPageResponse() {
        List<String> items = List.of("item1", "item2", "item3");
        PageResponse<String> response = PageResponse.of(items, 0, 10, 25);

        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getPage().getNumber()).isZero();
        assertThat(response.getPage().getSize()).isEqualTo(10);
        assertThat(response.getPage().getTotalElements()).isEqualTo(25);
        assertThat(response.getPage().getTotalPages()).isEqualTo(3);
    }

    @Test
    void totalPagesCalculation() {
        PageResponse<String> response = PageResponse.of(List.of("a"), 0, 10, 10);
        assertThat(response.getPage().getTotalPages()).isEqualTo(1);
    }

    @Test
    void totalPagesWithPartialLastPage() {
        PageResponse<String> response = PageResponse.of(List.of("a"), 0, 10, 11);
        assertThat(response.getPage().getTotalPages()).isEqualTo(2);
    }

    @Test
    void emptyPage() {
        PageResponse<String> response = PageResponse.of(List.of(), 0, 10, 0);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPage().getTotalElements()).isZero();
        assertThat(response.getPage().getTotalPages()).isZero();
    }

    @Test
    void zeroSizeHandling() {
        PageResponse<String> response = PageResponse.of(List.of(), 0, 0, 0);
        assertThat(response.getPage().getTotalPages()).isZero();
    }
}
