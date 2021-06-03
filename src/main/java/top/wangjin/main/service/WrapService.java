package top.wangjin.main.service;

import top.wangjin.springframework.annotation.Autowired;
import top.wangjin.springframework.annotation.Component;
import top.wangjin.springframework.annotation.Qualifier;

/**
 * 需要注入到SpringIOC中的Bean
 */
@Component(name = "wrapService")
public class WrapService {
    @Autowired
    @Qualifier("helloWorldService")
    private HelloWorldService helloWorldService;

    public void say() {
        helloWorldService.saySomething();
    }
}

