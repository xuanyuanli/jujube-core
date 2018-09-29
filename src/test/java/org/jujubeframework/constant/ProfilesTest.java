package org.jujubeframework.constant;

import org.assertj.core.api.Assertions;
import static org.junit.Assert.*;
import org.junit.Test;

public class ProfilesTest {

    @Test
    public void setSpringProfileAsSystemProperty() {
        Profiles.setSpringProfileAsSystemProperty(Profiles.PRODUCTION);
        Assertions.assertThat(Profiles.getSpringProfileAsSystemProperty()).isEqualTo(Profiles.PRODUCTION);
    }

    @Test
    public void getSpringProfileAsSystemProperty() {
        Profiles.setSpringProfileAsSystemProperty(Profiles.PRODUCTION);
        Assertions.assertThat(Profiles.getSpringProfileAsSystemProperty()).isEqualTo(Profiles.PRODUCTION);
    }

}