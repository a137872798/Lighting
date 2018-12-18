package com.gxl.Lighting;

import com.gxl.Lighting.meta.RegisterMeta;

import java.util.List;

/**
 * 订阅对象的 回调监听器
 */
public interface NotifyListener {

    /**
     * 全量通知 首次注册时 就会触发 每次 订阅的服务发生变化 也会触发
     * 如果没有可使用服务了 这个列表就是空的 然后就清空client 的缓存
     * @param services
     */
    void notify(List<RegisterMeta> services);
}
