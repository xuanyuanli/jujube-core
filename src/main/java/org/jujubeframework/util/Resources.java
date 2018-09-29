package org.jujubeframework.util;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Resources {
    private static Logger logger = LoggerFactory.getLogger(Resources.class);

    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    /**
     * 获得包下的所有class
     */
    public static List<Class<?>> getPackageClasses(String packageName) {
        List<Class<?>> list = Lists.newArrayList();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(packageName) + "/" + DEFAULT_RESOURCE_PATTERN;
        try {
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    String filePath = URLDecoder.decode(resource.getURL().getFile(), "UTF-8");
                    String className = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length() - 6);
                    if (!className.contains("$")) {
                        try {
                            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(getRealPackageName(filePath, packageName) + "." + className);
                            list.add(clazz);
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("resourcePatternResolver.getResources", e);
        }
        return list;
    }

    private static String getRealPackageName(String filePath, String packageName) {
        String pName = packageName.replace(".", "/");
        String tPath = filePath.substring(filePath.indexOf(pName));
        pName = tPath.substring(0, tPath.lastIndexOf("/"));
        return pName.replace("/", ".");
    }

    /**
     * 获取所有classpath下对应名称的Properties文件属性
     *
     * @param fileName 相对于classpath的文件位置
     */
    public static Properties getProperties(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName不能为空");
        }

        Properties properties = new Properties();
        try {
            Resource[] resources = getClassPathAllResources(fileName);
            for (int i = resources.length - 1; i >= 0; i--) {
                Resource resource = resources[i];
                properties.load(resource.getInputStream());
                logger.debug("load properties:{}", resource.getURL());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    /**
     * 获取当前classpath下对应名称的Properties文件属性
     *
     * @param fileName 相对于classpath的文件位置
     */
    public static Properties getCurrentClasspathProperties(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName不能为空");
        }

        Properties properties = new Properties();
        Resource resource = getClassPathResources(fileName);
        try (InputStream inputStream = resource.getInputStream()) {
            properties.load(inputStream);
            logger.debug("load properties:{}", resource.getURL());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    /**
     * 获得classpath*下的指定资源
     */
    public static Resource[] getClassPathAllResources(String resourceName) {
        Resource[] resources = null;
        try {
            resources = resourcePatternResolver.getResources("classpath*:" + resourceName);
        } catch (IOException e) {
        }
        return resources;
    }

    /**
     * 获得classpath下的指定资源
     */
    public static Resource getClassPathResources(String resourceName) {
        Resource[] resources = null;
        try {
            resources = resourcePatternResolver.getResources("classpath:" + resourceName);
        } catch (IOException e) {
        }
        return resources != null && resources.length > 0 ? resources[0] : null;
    }

    /**
     * 获得当前的classpath目录
     */
    public static File getCurrentClasspath() {
        Resource resource = resourcePatternResolver.getResource("classpath:./");
        try {
            return resource.getFile();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 获得当前项目路径（只限于Eclipse或IDEA中管用）
     */
    public static String getProjectPath() {
        File dir = getCurrentClasspath();
        if (dir != null) {
            // 找到target目录
            while (true) {
                if ("target".equals(dir.getName())) {
                    break;
                } else {
                    dir = dir.getParentFile();
                }
            }
            String projectDir = dir.getParentFile().getAbsolutePath();
            return projectDir;
        }
        return null;
    }

    /**
     * 获得某个class所在的jar所在的目录（例如打包A项目为jar，获取的就是A.jar所在的目录）
     */
    public static String getJarHome(Class<?> cl) {
        String path = cl.getProtectionDomain().getCodeSource().getLocation().getFile();
        File jarFile = new File(path);
        return jarFile.getParentFile().getAbsolutePath();
    }

    /**
     * 文件是否来自于Jar中
     */
    public static boolean isJarFile(URL url) {
        return "jar".equals(url.getProtocol());
    }

    /**
     * 此Class是否从jar中启动
     */
    public static boolean isJarStartByClass(Class<?> cl) {
        return "jar".equals(cl.getResource(cl.getSimpleName() + ".class").getProtocol());
    }
}
