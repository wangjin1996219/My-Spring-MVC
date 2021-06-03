package top.wangjin.springframework.context;

import top.wangjin.springframework.entity.BeanDefinition;
import top.wangjin.springframework.factory.AbstractBeanFactory;
import top.wangjin.springframework.factory.AutowiredCapableBeanFactory;
import top.wangjin.springframework.io.ResourceLoader;
import top.wangjin.springframework.reader.XmlBeanDefinitionReader;

import java.util.Map;

public class ClassPathXmlApplicationContext extends AbstractApplicationContext {

    private final Object startupShutdownMonitor = new Object();
    private String location;

    /**
     * 传入一个路径，根据路径的配置文件确定是XML还是注解方式
     * @param location
     * @throws Exception
     */
    public ClassPathXmlApplicationContext(String location) throws Exception {
        super();
        this.location = location;
        refresh();
    }

    /**
     * 先通过obtainBeanFactory获取初步注册beanDefinition（Bean属性中的基本类型已经实例化，引用类型没有实例化）的Bean工厂
     * 再通过prepareBeanFactory，将所有注册的Bean实例化（Bean引用属性实例化）
     * @throws Exception
     */
    public void refresh() throws Exception {
        synchronized (startupShutdownMonitor) {
            AbstractBeanFactory beanFactory = obtainBeanFactory();
            prepareBeanFactory(beanFactory);
            this.beanFactory = beanFactory;
        }
    }

    /**
     * 实例化所有Bean
     * @param beanFactory
     * @throws Exception
     */
    private void prepareBeanFactory(AbstractBeanFactory beanFactory) throws Exception {
        beanFactory.populateBeans();
    }

    /**
     * 获取Bean工厂，并向工厂中初步注册beanDefinition（Bean属性中的引用类型没有实例化）
     * @return
     * @throws Exception
     */
    private AbstractBeanFactory obtainBeanFactory() throws Exception {
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(new ResourceLoader());
        beanDefinitionReader.loadBeanDefinitions(location);
        AbstractBeanFactory beanFactory = new AutowiredCapableBeanFactory();
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionReader.getRegistry().entrySet()) {
            beanFactory.registerBeanDefinition(beanDefinitionEntry.getKey(), beanDefinitionEntry.getValue());
        }
        return beanFactory;
    }

    public void addNewBeanDefinition(String name, BeanDefinition beanDefinition) throws Exception {
        XmlBeanDefinitionReader.processAnnotationProperty(beanDefinition.getBeanClass(), beanDefinition);
        beanFactory.registerBeanDefinition(name, beanDefinition);
    }

    public void refreshBeanFactory() throws Exception {
        prepareBeanFactory((AbstractBeanFactory) beanFactory);
    }
}

