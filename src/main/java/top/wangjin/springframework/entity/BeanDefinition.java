package top.wangjin.springframework.entity;

/**
 * 从配置文件中读取的Bean的定义
 *
 * @author wangjin
 */
public class BeanDefinition {
    //Bean的实例
    private Object bean;
    //Bean所对应的类
    private Class beanClass;
    //Bean所对应的类名
    private String beanClassName;
    //是否是单例模式
    private Boolean singleton;
    //Bean的属性
    private PropertyValues propertyValues;

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
        try {
            this.beanClass = Class.forName(beanClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public PropertyValues getPropertyValues() {
        if(propertyValues == null) {
            propertyValues = new PropertyValues();
        }
        return propertyValues;
    }

    public void setPropertyValues(PropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }

    public Boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(Boolean singleton) {
        this.singleton = singleton;
    }
}
