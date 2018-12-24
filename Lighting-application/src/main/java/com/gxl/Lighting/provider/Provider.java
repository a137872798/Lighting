package com.gxl.Lighting.provider;

import com.gxl.Lighting.Application;
import java.net.UnknownHostException;

/**
 * 服务提供者接口
 */
public interface Provider extends Application {

    void setRef(Object object);

    void setRefs(Object... objects);

    void addPublishService(Class<?> o);

    void addPublishServices(Class<?>... o);

    void publish();

    void removePublishService(Class<?> o);

    void removePublishServices(Class<?>... o);
}

