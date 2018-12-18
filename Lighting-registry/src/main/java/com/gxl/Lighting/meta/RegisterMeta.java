package com.gxl.Lighting.meta;


import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import com.gxl.Lighting.rpc.param.UnRegisterCommandParam;

/**
 * 服务注册后生成的 元数据
 */
public class RegisterMeta {

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

    public static RegisterMeta newMeta(RegisterCommandParam param){
        RegisterMeta meta = new RegisterMeta();
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServiceName(param.getServiceName());
        serviceMeta.setVersion(param.getVersion());
        String address = param.getAddress();
        meta.setAddress(address);
        meta.setServiceMeta(serviceMeta);
        return meta;
    }

    public static RegisterMeta newMeta(UnRegisterCommandParam param){
        RegisterMeta meta = new RegisterMeta();
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServiceName(param.getServiceName());
        serviceMeta.setVersion(param.getVersion());
        String address = param.getAddress();
        meta.setAddress(address);
        meta.setServiceMeta(serviceMeta);
        return meta;
    }
}
