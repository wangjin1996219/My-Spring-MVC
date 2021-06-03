package top.wangjin.springframework.io;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 通过Url的方式获取资源
 *
 * @author wangjin
 */
public class UrlResource implements Resource {

    private URL url;

    public UrlResource(URL url) {
        this.url = url;
    }

    /**
     * 有了URLConnection对象后，可以通过getInputStream()来获取一个InputStream，由此读取URL所引用的资源数据
     * @return
     * @throws Exception
     */
    public InputStream getInputStream() throws Exception {
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        return urlConnection.getInputStream();
    }
}
