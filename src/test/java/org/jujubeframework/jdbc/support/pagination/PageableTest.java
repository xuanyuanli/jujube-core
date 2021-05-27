package org.jujubeframework.jdbc.support.pagination;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class PageableTest {

    @Test
    public void hasPreviousPage() {
        Pageable pageable = new Pageable(1, 10);
        pageable.setTotalElements(100);
        Assertions.assertThat(pageable.hasPreviousPage()).isFalse();

        pageable.setIndex(2);
        Assertions.assertThat(pageable.hasPreviousPage()).isTrue();
    }

    @Test
    public void isFirstPage() {
        Pageable pageable = new Pageable(1, 10);
        Assertions.assertThat(pageable.isFirstPage()).isTrue();

        pageable.setIndex(2);
        Assertions.assertThat(pageable.isFirstPage()).isFalse();
    }

    @Test
    public void hasNextPage() {
        Pageable pageable = new Pageable(1, 10);
        pageable.setTotalElements(100);
        Assertions.assertThat(pageable.hasNextPage()).isTrue();

        pageable.setIndex(10);
        Assertions.assertThat(pageable.hasNextPage()).isFalse();
    }

    @Test
    public void isLastPage() {
        Pageable pageable = new Pageable(1, 10);
        pageable.setTotalElements(100);
        Assertions.assertThat(pageable.isLastPage()).isFalse();

        pageable.setIndex(10);
        Assertions.assertThat(pageable.isLastPage()).isTrue();
    }

    @Test
    public void getStart(){
        Pageable pageable = new Pageable(1, 10);
        Assertions.assertThat(pageable.getStart()).isEqualTo(0);

        pageable.setIndex(2);
        Assertions.assertThat(pageable.getStart()).isEqualTo(10);

        pageable.setIndex(20);
        Assertions.assertThat(pageable.getStart()).isEqualTo(190);
    }

    @Test
    public void getTotalPages(){
        Pageable pageable = new Pageable(1, 10);
        pageable.setTotalElements(20);
        Assertions.assertThat(pageable.getTotalPages()).isEqualTo(2);

        pageable.setTotalElements(201);
        Assertions.assertThat(pageable.getTotalPages()).isEqualTo(21);
    }
}