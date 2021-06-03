package top.wangjin.springframework.web;

import top.wangjin.springframework.annotation.Controller;
import top.wangjin.springframework.annotation.RequestMapping;
import top.wangjin.springframework.context.ClassPathXmlApplicationContext;
import top.wangjin.springframework.entity.BeanDefinition;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 注册为Servlet类，负责处理所有URL请求
 * 三个重要方法：init、doGet和doPost方法
 */
public class DispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String, Method> handlerMapping = new HashMap<>();

    private HashSet<Class> classes = new HashSet<>();

    private Map<String, Object> controllerMap = new HashMap<>();

    private ClassPathXmlApplicationContext xmlApplicationContext;

    @Override
    public void init(ServletConfig config) {
        try {
            //获取SpringIOC容器
            xmlApplicationContext = new ClassPathXmlApplicationContext("application-annotation.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        doScanner(properties.getProperty("scanPackage"));
        doInstance();
        initHandlerMapping();
    }

    /**
     * 处理请求HttpServletRequest，并返回结果HttpServletResponse
     * @param req
     * @param resp
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            //处理请求
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
        }
    }

    /**
     * 实际处理请求的逻辑
     * @param request
     * @param response
     * @throws Exception
     */
    public void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (handlerMapping.isEmpty()) return;
        //获取请求的URL
        String url = request.getRequestURI();
        //获取项目的相对路径
        String contextPath = request.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        //如果请求的URL不存在，返回404错误
        if (!handlerMapping.containsKey(url)) {
            response.getWriter().write("404 NOT FOUND!");
            return;
        }
        //获取要请求的方法
        Method method = handlerMapping.get(url);
        //获取请求方法的参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求中的参数键值对
        Map<String, String[]> parameterMap = request.getParameterMap();
        //存放请求中的参数
        Object[] paramValues = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            String requestParam = parameterTypes[i].getSimpleName();
            if (requestParam.equals("HttpServletRequest")) {
                paramValues[i] = request;
                continue;
            }
            if (requestParam.equals("HttpServletResponse")) {
                paramValues[i] = response;
                continue;
            }
            if (requestParam.equals("String")) {
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    //获取实际的请求值
                    String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                    paramValues[i] = value;
                }
            }
        }
        try {
            //通过方法名和参数，反射调用方法
            method.invoke(controllerMap.get(url), paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取web.xml文件中DispatcherServlet的配置文件路径
     * @param location
     */
    private void doLoadConfig(String location) {
        //把web.xml中的contextConfigLocation对应value值的文件加载到流里面
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);

        try {
            //用Properties文件加载文件里的内容
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取DispatcherServlet要扫描包的范围，并将范围内所有类放入classNames集合中
     * @param packageName
     */
    private void doScanner(String packageName) {
        //把所有的.替换成/
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                //递归读取包
                doScanner(packageName + "." + file.getName());
            } else {
                String className = packageName + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     *
     */
    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        //遍历集合中的所有类
        for (String className : classNames) {
            try {
                //把类搞出来,反射来实例化(只有加@Controller需要实例化)
                Class clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    classes.add(clazz);
                    BeanDefinition definition = new BeanDefinition();
                    definition.setSingleton(true);
                    definition.setBeanClassName(clazz.getName());
                    xmlApplicationContext.addNewBeanDefinition(clazz.getName(), definition);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            xmlApplicationContext.refreshBeanFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取RequestMapping这个注解的值，并且拼接出完整的URL:
     * 1.将URL与方法的映射存储在handlerMapping这个map中
     * 2.将URL与类的映射存储在controllerMap中
     */
    private void initHandlerMapping() {
        if (classes.isEmpty()) return;
        try {
            for (Class<?> clazz : classes) {
                String baseUrl = "";
                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    baseUrl = clazz.getAnnotation(RequestMapping.class).value();
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(RequestMapping.class)) continue;
                    String url = method.getAnnotation(RequestMapping.class).value();
                    url = (baseUrl + "/" + url).replaceAll("/+", "/");
                    handlerMapping.put(url, method);
                    controllerMap.put(url, xmlApplicationContext.getBean(clazz));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
