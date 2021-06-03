package top.wangjin.springframework.context;

/**
 * 应用程序上下文接口
 *
 * @author wangjin
 */
public interface ApplicationContext {

    /**
     * 通过类名从SpringIOC中获取实例
     * @param clazz
     * @return
     * @throws Exception
     */
    Object getBean(Class clazz) throws Exception;

    /**
     * 通过实例的名称从SpringIOC中获取对象
     * @param beanName
     * @return
     * @throws Exception
     */
    Object getBean(String beanName) throws Exception;

}
