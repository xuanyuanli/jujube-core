package org.jujubeframework.constant;

import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.spring.ApplicationContextHolder;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.Envs;
import org.springframework.core.env.Environment;

/**
 * Spring Profiles
 *
 * @author John Li
 */
public class Profiles {

    /**
     * H2的驱动
     */
    public static final String H2_DRIVER_CLASS_NAME = "org.h2.Driver";
    /**
     * Junit的Test类
     */
    private static final String JUNIT_TEST_CLASS_NAME = "org.junit.jupiter.api.Test";

    public static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";

    private static boolean IS_TEST = false;

    static {
        try {
            Beans.forName(JUNIT_TEST_CLASS_NAME);
            IS_TEST = true;
        } catch (Throwable ignored) {
        }
    }

    private Profiles() {
    }

    /** 设置Spring Profile到系统变量 */
    public static void setSpringProfileToSystemProperty(String profile) {
        System.setProperty(SPRING_PROFILES_ACTIVE, profile);
    }

    /** 从系统变量中获取Spring Profile */
    public static String getSpringProfileFromSystemProperty() {
        String profile = "";
        try {
            Environment environment = ApplicationContextHolder.getEnvironment();
            if (environment != null) {
                profile = environment.getActiveProfiles()[0];
            }
        } catch (Throwable ignored) {
        }
        if (StringUtils.isBlank(profile)) {
            profile = Envs.getEnv(SPRING_PROFILES_ACTIVE);
        }
        return profile;
    }

    /**
     * 是否是测试环境
     */
    public static boolean isTestProfile() {
        return IS_TEST;
    }

    /**
     * 是否是正式环境
     */
    public static boolean isProdProfile() {
        return PRODUCTION.equalsIgnoreCase(getSpringProfileFromSystemProperty());
    }

    public static final String ACTIVE_PROFILE = SPRING_PROFILES_ACTIVE;
    public static final String DEFAULT_PROFILE = "spring.profiles.default";
    public static final String PRODUCTION = "prod";
    public static final String DEVELOPMENT = "dev";
    public static final String UNIT_TEST = "test";
    public static final String FUNCTIONAL_TEST = "func";
}
