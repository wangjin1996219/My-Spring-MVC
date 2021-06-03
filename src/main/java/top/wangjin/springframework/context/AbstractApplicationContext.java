package top.wangjin.springframework.context;

import top.wangjin.springframework.factory.BeanFactory;

/**
 * 实现ApplicationContext中获取实例的两个方法,运用代理模式（静态代理）的思想，内部保存一个BeanFactory实例来实现其功能
 */
public abstract class AbstractApplicationContext implements ApplicationContext {

    BeanFactory beanFactory;

    @Override
    public Object getBean(Class clazz) throws Exception {
        return beanFactory.getBean(clazz);
    }

    @Override
    public Object getBean(String beanName) throws Exception {
        return beanFactory.getBean(beanName);
    }
}
