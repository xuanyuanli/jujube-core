package org.jujubeframework.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

public class ResourcesTest {

    @Test
    public void getClassPathAllResources() {
    }

    @Test
    public void getClassPathResources() {
        Resource classPathResources = Resources.getClassPathResources("material/testRealCount.xlsx");
        Assertions.assertThat(classPathResources.getFilename()).isEqualTo("testRealCount.xlsx");
    }
}