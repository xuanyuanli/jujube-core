package org.jujubeframework.constant;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProfilesTest {

    @Test
    public void setSpringProfileAsSystemProperty() {
        Profiles.setSpringProfileToSystemProperty(Profiles.PRODUCTION);
        Assertions.assertThat(Profiles.getSpringProfileFromSystemProperty()).isEqualTo(Profiles.PRODUCTION);
    }

    @Test
    public void getSpringProfileAsSystemProperty() {
        Profiles.setSpringProfileToSystemProperty(Profiles.PRODUCTION);
        Assertions.assertThat(Profiles.getSpringProfileFromSystemProperty()).isEqualTo(Profiles.PRODUCTION);
    }

}