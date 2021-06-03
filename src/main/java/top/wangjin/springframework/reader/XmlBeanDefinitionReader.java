package top.wangjin.springframework.reader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import top.wangjin.springframework.annotation.*;
import top.wangjin.springframework.entity.BeanDefinition;
import top.wangjin.springframework.entity.BeanReference;
import top.wangjin.springframework.entity.PropertyValue;
import top.wangjin.springframework.io.ResourceLoader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * XML配置文件形式的Bean定义读取类
 *
 * @author wangjin
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

    public XmlBeanDefinitionReader(ResourceLoader resourceLoader) {
        super(resourceLoader);
    }

    /**
     * 根据配置文件的路径获取BeanDefinition
     *
     * @param location 配置文件路径
     * @throws Exception
     */
    @Override
    public void loadBeanDefinitions(String location) throws Exception {
        //根据配置文件的路径，获取配置文件的文件输入流
        InputStream inputStream = getResourceLoader().getResource(location).getInputStream();
        doLoadBeanDefinitions(inputStream);
    }

    /**
     * 根据配置文件的输入流解析出BeanDefinition，并注册到临时Map中
     *
     * @param inputStream
     * @throws Exception
     */
    protected void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        // 解析xml document并注册bean
        registerBeanDefinitions(document);
        inputStream.close();
    }

    /**
     * 注册BeanDefinition
     *
     * @param document
     */
    public void registerBeanDefinitions(Document document) {
        Element root = document.getDocumentElement();
        // 从文件根递归解析
        parseBeanDefinitions(root);
    }

    /**
     * 从配置文件的根root，递归地将每个节点解析成BeanDefinition
     *
     * @param root
     */
    protected void parseBeanDefinitions(Element root) {
        NodeList nodeList = root.getChildNodes();
        //先判断是否是注解配置：
        //1.是，选用注解解析成BeanDefinition
        //2.否，选用XML文件解析成BeanDefinition
        String basePackage = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                Element ele = (Element) nodeList.item(i);
                if (ele.getTagName().equals("component-scan")) {
                    basePackage = ele.getAttribute("base-package");
                    break;
                }
            }
        }
        if (basePackage != null) {
            parseAnnotation(basePackage);
            return;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                processBeanDefinition((Element) node);
            }
        }
    }

    protected void parseAnnotation(String basePackage) {
        //获取basePackage包下所有类，放入Set集合中
        Set<Class<?>> classes = getClasses(basePackage);
        for (Class clazz : classes) {
            processAnnotationBeanDefinition(clazz);
        }
    }

    /**
     * 根据注解来解析BeanDefinition，并将其注册到注册表Map中
     *
     * @param clazz
     */
    protected void processAnnotationBeanDefinition(Class<?> clazz) {
        //Component注解表示需要注入到SpringIOC中
        if (clazz.isAnnotationPresent(Component.class)) {
            //通过反射的方式获取注解的值
            String name = clazz.getAnnotation(Component.class).name();
            if (name == null || name.length() == 0) {
                name = clazz.getName();
            }
            String className = clazz.getName();
            //确定Bean是Singleton还是prototype
            boolean singleton = true;
            if (clazz.isAnnotationPresent(Scope.class) && "prototype".equals(clazz.getAnnotation(Scope.class).value())) {
                singleton = false;
            }
            BeanDefinition beanDefinition = new BeanDefinition();
            //给Bean的属性赋值
            processAnnotationProperty(clazz, beanDefinition);
            //给BeanDefinition实体类赋值
            beanDefinition.setBeanClassName(className);
            beanDefinition.setSingleton(singleton);
            //将BeanDefinition注册到注册表Map中
            getRegistry().put(name, beanDefinition);
        }
    }

    /**
     * 给Bean的属性赋值
     *
     * @param clazz
     * @param beanDefinition
     */
    public static void processAnnotationProperty(Class<?> clazz, BeanDefinition beanDefinition) {
        //获取Bean所对应类的字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            //获取属性的名称
            String name = field.getName();
            // 属性上的注解有两种情况：
            // 1.如果属性上添加了Value注解，属于基本类型，将Value上的值赋给该属性
            // 2.如果属性上添加了Autowired注解，属于引用类型，又分为两种情况：
            // a.如果属性上有Qualifier注解，根据注解值注入beanDefinitionMap
            // b.如果属性上没有Qualifier注解，就根据Autowired注解，将对应类型的类名注入beanDefinitionMap
            if (field.isAnnotationPresent(Value.class)) {
                Value valueAnnotation = field.getAnnotation(Value.class);
                String value = valueAnnotation.value();
                if (value != null && value.length() > 0) {
                    // 优先进行值注入
                    beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, value));
                }
            } else if (field.isAnnotationPresent(Autowired.class)) {
                if (field.isAnnotationPresent(Qualifier.class)) {
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    String ref = qualifier.value();
                    if (ref == null || ref.length() == 0) {
                        throw new IllegalArgumentException("the value of Qualifier should not be null!");
                    }
                    BeanReference beanReference = new BeanReference(ref);
                    beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, beanReference));
                } else {
                    String ref = field.getType().getName();
                    BeanReference beanReference = new BeanReference(ref);
                    beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, beanReference));
                }
            }
        }

    }

    /**
     * 获取packageName包下所有的类，并返回一个Set集合
     *
     * @param packageName
     * @return
     */
    protected Set<Class<?>> getClasses(String packageName) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        boolean recursive = true;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(
                    packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件，并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath,
                            recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection())
                                .getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx)
                                            .replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class")
                                            && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(
                                                packageName.length() + 1, name
                                                        .length() - 6);
                                        try {
                                            // 添加到classes
                                            classes.add(Class
                                                    .forName(packageName + '.'
                                                            + className));
                                        } catch (ClassNotFoundException e) {
                                            // log
                                            // .error("添加用户自定义视图类错误 找不到此类的.class文件");
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        // log.error("在扫描用户定义视图时从jar包获取文件出错");
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 以文件的形式获取包下所有的Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private void findAndAddClassesInPackageByFile(String packageName,
                                                  String packagePath, final boolean recursive, Set<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory())
                        || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "."
                                + file.getName(), file.getAbsolutePath(), recursive,
                        classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0,
                        file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    //classes.add(Class.forName(packageName + '.' + className));
                    //经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    // log.error("添加用户自定义视图类错误 找不到此类的.class文件");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 对于BeanDefinition的实际解析过程，也就是获取BeanDefinition实体类的每个属性
     *
     * @param ele
     */
    protected void processBeanDefinition(Element ele) {
        String name = ele.getAttribute("id");
        String className = ele.getAttribute("class");
        boolean singleton = true;
        if (ele.hasAttribute("scope") && "prototype".equals(ele.getAttribute("scope"))) {
            singleton = false;
        }
        BeanDefinition beanDefinition = new BeanDefinition();
        processProperty(ele, beanDefinition);
        beanDefinition.setBeanClassName(className);
        beanDefinition.setSingleton(singleton);
        getRegistry().put(name, beanDefinition);
    }

    /**
     * 给Bean的属性赋值：
     * 1.当是基本类型时直接赋值
     * 2.当是引用类型时不是立即初始化，而是创建一个只有名称的引用对象，
     * 因为BeanDefinition是在读取配置文件时就被创建的，这时还没有任何Bean被初始化，BeanReference仅仅是一个记录而已
     *
     * @param ele
     * @param beanDefinition
     */
    private void processProperty(Element ele, BeanDefinition beanDefinition) {
        NodeList propertyNode = ele.getElementsByTagName("property");
        for (int i = 0; i < propertyNode.getLength(); i++) {
            Node node = propertyNode.item(i);
            if (node instanceof Element) {
                Element propertyEle = (Element) node;
                String name = propertyEle.getAttribute("name");
                String value = propertyEle.getAttribute("value");
                if (value != null && value.length() > 0) {
                    // 优先进行值注入
                    beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, value));
                } else {
                    String ref = propertyEle.getAttribute("ref");
                    if (ref == null || ref.length() == 0) {
                        throw new IllegalArgumentException("Configuration problem: <property> element for property '" + name + "' must specify a ref or value");
                    }
                    BeanReference beanReference = new BeanReference(ref);
                    beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, beanReference));
                }
            }
        }
    }

}
