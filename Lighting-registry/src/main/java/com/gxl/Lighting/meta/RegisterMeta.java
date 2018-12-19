package com.gxl.Lighting.meta;


import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import com.gxl.Lighting.rpc.param.UnRegisterCommandParam;
import com.gxl.Lighting.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务注册后生成的 元数据
 */
public class RegisterMeta {

    private String address;

    private ServiceMeta[] serviceMeta;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ServiceMeta[] getServiceMeta() {
        return serviceMeta;
    }

    public void setServiceMeta(ServiceMeta[] serviceMeta) {
        this.serviceMeta = serviceMeta;
    }

    public static RegisterMeta newMeta(RegisterCommandParam param) {
        RegisterMeta meta = new RegisterMeta();
        String serviceName = param.getServiceName();
        String[] services = StringUtil.split(serviceName, ",");

        ServiceMeta[] serviceMetas = new ServiceMeta[services.length];
        for (int i = 0; i < serviceMetas.length; i++) {
            ServiceMeta serviceMeta = new ServiceMeta();
            serviceMeta.setServiceName(services[i]);
            //本来每个服务应该有自己的 版本的  这里暂时不考虑版本的因素  考虑也只是使用, 分隔 实现起来 跟多service差不多
            serviceMeta.setVersion(param.getVersion());
            serviceMetas[i] = serviceMeta;
        }
        String address = param.getAddress();
        meta.setAddress(address);
        meta.setServiceMeta(serviceMetas);
        return meta;
    }

    public static RegisterMeta newMeta(UnRegisterCommandParam param) {
        RegisterMeta meta = new RegisterMeta();
        String serviceName = param.getServiceName();
        String[] services = StringUtil.split(serviceName, ",");

        ServiceMeta[] serviceMetas = new ServiceMeta[services.length];
        for (int i = 0; i < serviceMetas.length; i++) {
            ServiceMeta serviceMeta = new ServiceMeta();
            serviceMeta.setServiceName(services[i]);
            //本来每个服务应该有自己的 版本的  这里暂时不考虑版本的因素  考虑也只是使用, 分隔 实现起来 跟多service差不多
            serviceMeta.setVersion(param.getVersion());
            serviceMetas[i] = serviceMeta;
        }
        String address = param.getAddress();
        meta.setAddress(address);
        meta.setServiceMeta(serviceMetas);
        return meta;
    }
}
