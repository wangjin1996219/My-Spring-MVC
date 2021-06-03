package top.wangjin.main;

import top.wangjin.main.service.HelloWorldService;
import top.wangjin.main.service.WrapService;
import top.wangjin.springframework.context.ApplicationContext;
import top.wangjin.springframework.context.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) throws Exception {
        xmlTest();
        System.out.println("==========================================");
        annotationTest();
    }

    public static void xmlTest() throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application.xml");
        WrapService wrapService = (WrapService) applicationContext.getBean("wrapService");
        wrapService.say();
        HelloWorldService helloWorldService = (HelloWorldService) applicationContext.getBean("helloWorldService");
        HelloWorldService helloWorldService2 = (HelloWorldService) applicationContext.getBean("helloWorldService");
        System.out.println("prototype验证：相等" + (helloWorldService == helloWorldService2));
        WrapService wrapService2 = (WrapService) applicationContext.getBean("wrapService");
        System.out.println("singleton验证：相等" + (wrapService == wrapService2));
    }

    public static void annotationTest() throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-annotation.xml");
        WrapService wrapService = (WrapService) applicationContext.getBean("wrapService");
        wrapService.say();
        HelloWorldService helloWorldService = (HelloWorldService) applicationContext.getBean("helloWorldService");
        HelloWorldService helloWorldService2 = (HelloWorldService) applicationContext.getBean("helloWorldService");
        System.out.println("prototype验证：相等" + (helloWorldService == helloWorldService2));
        WrapService wrapService2 = (WrapService) applicationContext.getBean("wrapService");
        System.out.println("singleton验证：相等" + (wrapService == wrapService2));
    }

}
