package top.wangjin.springframework.reader;

import top.wangjin.springframework.entity.BeanDefinition;
import top.wangjin.springframework.io.ResourceLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * BeanDefinitionReader实现的抽象类
 *
 * @author wangjin
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {
    //用于暂存Bean的名称和BeanDefinition的映射
    private Map<String, BeanDefinition> registry;
    //配置文件加载器
    private ResourceLoader resourceLoader;

    public AbstractBeanDefinitionReader(ResourceLoader resourceLoader) {
        this.registry = new HashMap<>();
        this.resourceLoader = resourceLoader;
    }

    public Map<String, BeanDefinition> getRegistry() {
        return registry;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

}
