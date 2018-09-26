package org.jujubeframework.util;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Sets;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class DynamicsTest {

    @Test
    public void testOfElse() {
        String t = null;
        String other = "default";
        assertThat(Dynamics.orElse(t, other)).isEqualTo(other);
    }

    @Test
    public void testBool() {
        assertThat(Dynamics.bool(null)).isFalse();
        assertThat(Dynamics.bool(0)).isFalse();
        assertThat(Dynamics.bool(0.0)).isFalse();
        assertThat(Dynamics.bool("")).isFalse();
        assertThat(Dynamics.bool(Lists.newArrayList())).isFalse();
        assertThat(Dynamics.bool(Sets.newHashSet())).isFalse();
        assertThat(Dynamics.bool(Maps.newHashMap())).isFalse();
        assertThat(Dynamics.bool(false)).isFalse();
        assertThat(Dynamics.bool(new Boolean(false))).isFalse();

        assertThat(Dynamics.bool(-1)).isTrue();
        assertThat(Dynamics.bool(1)).isTrue();
        assertThat(Dynamics.bool("0")).isTrue();
        assertThat(Dynamics.bool(Lists.newArrayList("1"))).isTrue();
        assertThat(Dynamics.bool(Sets.newHashSet("1"))).isTrue();
        assertThat(Dynamics.bool(Maps.newHashMap("1", "2"))).isTrue();
        assertThat(Dynamics.bool(true)).isTrue();
        assertThat(Dynamics.bool(new Boolean(true))).isTrue();
    }

}
