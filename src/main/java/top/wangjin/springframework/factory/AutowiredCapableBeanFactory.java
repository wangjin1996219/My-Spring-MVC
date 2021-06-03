package top.wangjin.springframework.factory;

import top.wangjin.springframework.entity.BeanDefinition;
import top.wangjin.springframework.entity.BeanReference;
import top.wangjin.springframework.entity.PropertyValue;

import java.lang.reflect.Field;

public class AutowiredCapableBeanFactory extends AbstractBeanFactory {
    /**
     * 具体创建Bean的方法
     * @param beanDefinition Bean定义对象
     * @return
     * @throws Exception
     */
    @Override
    Object doCreateBean(BeanDefinition beanDefinition) throws Exception {
        //如果对象是单例的并且已经创建完成，直接返回
        if(beanDefinition.isSingleton() && beanDefinition.getBean() != null) {
            return beanDefinition.getBean();
        }
        //否则通过反射的方式，获取Bean的实例
        Object bean = beanDefinition.getBeanClass().newInstance();
        //根据Bean属性，如果是单例的，也就是第一次创建该对象时，将Bean set进beanDefinition，便于下次直接获取
        if(beanDefinition.isSingleton()) {
            beanDefinition.setBean(bean);
        }
        //为新创建的Bean注入属性
        applyPropertyValues(bean, beanDefinition);
        return bean;
    }

    /**
     * 为新创建的Bean注入属性
     * @param bean 待注入属性的bean
     * @param beanDefinition bean的定义
     * @throws Exception 反射异常
     */
    void applyPropertyValues(Object bean, BeanDefinition beanDefinition) throws Exception {
        for(PropertyValue propertyValue : beanDefinition.getPropertyValues().getPropertyValues()) {
            Field field = bean.getClass().getDeclaredField(propertyValue.getName());
            Object value = propertyValue.getValue();
            if(value instanceof BeanReference) {
                BeanReference beanReference = (BeanReference) propertyValue.getValue();
                // 优先按照自定义名称匹配（字段上有Qualifier注解，按照注解值注入beanDefinitionMap的情况）
                BeanDefinition refDefinition = beanDefinitionMap.get(beanReference.getName());
                if(refDefinition != null) {
                    if(!refDefinition.isSingleton() || refDefinition.getBean() == null) {
                        value = doCreateBean(refDefinition);
                    } else {
                        value = refDefinition.getBean();
                    }
                } else {
                    // 按照类型匹配，返回第一个匹配的（字段上没有Qualifier注解，按照类型名称注入beanDefinitionMap的情况）
                    Class clazz = Class.forName(beanReference.getName());
                    for(BeanDefinition definition : beanDefinitionMap.values()) {
                        if(clazz.isAssignableFrom(definition.getBeanClass())) {
                            if(!definition.isSingleton() || definition.getBean() == null) {
                                value = doCreateBean(definition);
                            } else {
                                value = definition.getBean();
                            }
                        }
                    }
                }

            }
            if(value == null) {
                throw new RuntimeException("无法注入");
            }
            field.setAccessible(true);
            field.set(bean, value);
        }
    }
}
