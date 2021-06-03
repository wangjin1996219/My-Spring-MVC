package top.wangjin.springframework.reader;

/**
 * BeanDefinition读取接口
 *
 * @author wangjin
 */
public interface BeanDefinitionReader {

    /**
     * 从某个位置读取Bean的配置
     * @param location 配置文件路径
     * @throws Exception 可能出现的异常
     */
    void loadBeanDefinitions(String location) throws Exception;

}
