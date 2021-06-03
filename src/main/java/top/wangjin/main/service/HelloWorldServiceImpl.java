package top.wangjin.main.service;

import top.wangjin.springframework.annotation.Component;
import top.wangjin.springframework.annotation.Scope;
import top.wangjin.springframework.annotation.Value;

/**
 * 需要注入到SpringIOC中的Bean
 */
@Component(name = "helloWorldService")
@Scope("prototype")
public class HelloWorldServiceImpl implements HelloWorldService {
    @Value("Hello, world")
    private String text;

    @Override
    public void saySomething() {
        System.out.println(text);
    }

    @Override
    public String getString() {
        return text;
    }
}
