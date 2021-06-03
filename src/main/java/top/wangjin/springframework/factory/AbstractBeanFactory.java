package top.wangjin.springframework.factory;

import top.wangjin.springframework.entity.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现了工厂类，重写了根据名称返回Bean实例的方法，具体Bean的创建由它的子类实现
 *
 * @author wangjin
 */
public abstract class AbstractBeanFactory implements BeanFactory {

    //根据名称来获取Bean，因此我们选择Map结构来作为实际的容器
    ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 根据Bean的名称获取实例
     * @param name bean的名字
     * @return
     * @throws Exception
     */
    @Override
    public Object getBean(String name) throws Exception {
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        if(beanDefinition == null) return null;
        if(!beanDefinition.isSingleton() || beanDefinition.getBean() == null) {
            return doCreateBean(beanDefinition);
        } else {
            return doCreateBean(beanDefinition);
        }
    }

    /**
     * 根据Bean对应的类的对象获取实例
     * @param clazz bean的类对象
     * @return
     * @throws Exception
     */
    @Override
    public Object getBean(Class clazz) throws Exception {
        BeanDefinition beanDefinition = null;
        //获取beanDefinitionMap容器中所有映射关系的集合，并遍历
        for(Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            //获取每组映射的Bean所对应的类
            Class tmpClass = entry.getValue().getBeanClass();
            if(tmpClass == clazz || clazz.isAssignableFrom(tmpClass)) {
                //得到对应的BeanDefinition
                beanDefinition = entry.getValue();
            }
        }
        if(beanDefinition == null) {
            return null;
        }
        if(!beanDefinition.isSingleton() || beanDefinition.getBean() == null) {
            return doCreateBean(beanDefinition);
        } else {
            return beanDefinition.getBean();
        }
    }

    /**
     * 向容器中注册BeanDefinition
     * @param name bean的名字
     * @param beanDefinition bean的定义对象
     */
    @Override
    public void registerBeanDefinition(String name, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(name, beanDefinition);
    }

    /**
     * 创建Bean实例
     * @param beanDefinition Bean定义对象
     * @return Bean实例对象
     * @throws Exception 可能出现的异常
     */
    abstract Object doCreateBean(BeanDefinition beanDefinition) throws Exception;


    public void populateBeans() throws Exception {
        for(Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            doCreateBean(entry.getValue());
        }
    }
}
