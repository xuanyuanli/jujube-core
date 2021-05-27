package org.jujubeframework.util.support.freemarker;

import freemarker.cache.URLTemplateLoader;
import org.springframework.util.ClassUtils;

import java.net.URL;
import java.util.Objects;

/**
 * 为解决不能读取jar中目录的问题，拓展Freemarker的TemplateLoader
 *
 * @author John Li
 */
public class ClassloaderTemplateLoader extends URLTemplateLoader {
    private final String path;

    public ClassloaderTemplateLoader(String path) {
        super();
        this.path = canonicalizePrefix(path);
    }

    @Override
    protected URL getURL(String name) {
        name = path + name;
        return Objects.requireNonNull(ClassUtils.getDefaultClassLoader()).getResource(name);
    }

}
