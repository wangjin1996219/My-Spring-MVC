package top.wangjin.springframework.entity;

import java.util.ArrayList;
import java.util.List;
/**
 * 键值对组，表示注入对象的属性（不过这个键值对是每个属性的对象：propertyValue）
 *
 * @author wangjin
 */
public class PropertyValues {

    private final List<PropertyValue> propertyValueList = new ArrayList<>();

    public PropertyValues() {}

    public void addPropertyValue(PropertyValue propertyValue) {
        propertyValueList.add(propertyValue);
    }

    public List<PropertyValue> getPropertyValues() {
        return propertyValueList;
    }

}
