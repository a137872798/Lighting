package com.gxl.Lighting.meta;


import com.gxl.Lighting.rpc.param.SubscributeCommandParam;
import com.gxl.Lighting.rpc.param.UnSubscributeCommandParam;

/**
 * 订阅者元数据
 */
public class SubscributeMeta {

    /**
     * 订阅者的地址
     */
    private String address;

    private ServiceMeta serviceMeta;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ServiceMeta getServiceMeta() {
        return serviceMeta;
    }

    public void setServiceMeta(ServiceMeta serviceMeta) {
        this.serviceMeta = serviceMeta;
    }

    public static SubscributeMeta newMeta(SubscributeCommandParam param){
        SubscributeMeta meta = new SubscributeMeta();
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setVersion(param.getVerison());
        serviceMeta.setServiceName(param.getSerivceName());
        meta.setAddress(param.getAddress());
        meta.setServiceMeta(serviceMeta);
        return meta;
    }

    /**
     * 这里没有版本号
     * @param param
     * @return
     */
    public static SubscributeMeta newMeta(UnSubscributeCommandParam param){
        SubscributeMeta meta = new SubscributeMeta();
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServiceName(param.getServiceName());
        meta.setAddress(param.getAddress());
        meta.setServiceMeta(serviceMeta);
        return meta;
    }

}
