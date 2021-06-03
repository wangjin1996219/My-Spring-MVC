package top.wangjin.springframework.entity;

/**
 * 单个键值对，表示注入对象的属性
 *
 * @author wangjin
 */
public class PropertyValue {
    //Bean的属性名
    private final String name;
    //Bean的属性值（可能是基本类型也可能是引用类型）
    private final Object value;

    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

}
