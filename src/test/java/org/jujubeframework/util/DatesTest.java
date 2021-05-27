package org.jujubeframework.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class DatesTest {

    @Test
    public void isSameDay() {
        long t1 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 10, 45, 8));
        long t2 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 18, 45, 8));
        Assertions.assertThat(Dates.isSameDay(t1, t2)).isTrue();

        t1 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 2, 10, 45, 8));
        t2 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 18, 45, 8));
        Assertions.assertThat(Dates.isSameDay(t1, t2)).isFalse();

        t1 = Dates.getEpochSecond(LocalDateTime.of(2021, 2, 1, 10, 45, 8));
        t2 = Dates.getEpochSecond(LocalDateTime.of(2021, 1, 1, 18, 45, 8));
        Assertions.assertThat(Dates.isSameDay(t1, t2)).isFalse();
    }

}
