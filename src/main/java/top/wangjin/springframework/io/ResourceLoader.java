package top.wangjin.springframework.io;

import java.net.URL;

/**
 * 资源加载器，通过路径加载资源
 *
 * @author wangjin
 */
public class ResourceLoader {
    /**
     * 资源加载器，通过路径加载资源
     * @param location
     * @return
     */
    public Resource getResource(String location) {
        URL url = this.getClass().getClassLoader().getResource(location);
        return new UrlResource(url);
    }
}
