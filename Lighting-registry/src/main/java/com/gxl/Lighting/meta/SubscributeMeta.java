package com.gxl.Lighting.meta;


import com.gxl.Lighting.rpc.param.SubscributeCommandParam;
import com.gxl.Lighting.rpc.param.UnSubscributeCommandParam;
import com.gxl.Lighting.util.StringUtil;

import java.util.Arrays;

/**
 * 订阅者元数据
 */
public class SubscributeMeta {

    /**
     * 订阅者的地址
     */
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

    public static SubscributeMeta newMeta(SubscributeCommandParam param){
        SubscributeMeta meta = new SubscributeMeta();
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

    /**
     * 这里没有版本号
     * @param param
     * @return
     */
    public static SubscributeMeta newMeta(UnSubscributeCommandParam param){
        SubscributeMeta meta = new SubscributeMeta();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubscributeMeta that = (SubscributeMeta) o;

        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(serviceMeta, that.serviceMeta);
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(serviceMeta);
        return result;
    }
}
