package org.jujubeframework.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class CalcsTest {

    @Test
    public void equ() {
        Assertions.assertThat(Calcs.equ(0.0, 0)).isTrue();
        Assertions.assertThat(Calcs.equ(0.000001, 0)).isFalse();
        Assertions.assertThat(Calcs.equ(0.0f, 0)).isTrue();
    }

    @Test
    public void add() {
        Assertions.assertThat(Calcs.add(0.0, 0, 2)).isEqualTo(0.0);
        Assertions.assertThat(Calcs.add(0.01, 0, 2)).isEqualTo(0.01);
        Assertions.assertThat(Calcs.add(0.005, 0, 2)).isEqualTo(0.01);
        Assertions.assertThat(Calcs.add(0.004, 0, 2)).isEqualTo(0.0);
    }

}