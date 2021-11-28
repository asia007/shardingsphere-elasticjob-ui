package org.apache.shardingsphere.elasticjob.lite.client.dubbo;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.apache.shardingsphere.elasticjob.lite.client.dto.DubboJobConfig;

/**
 * dubbo GenericService
 *
 * @author songhui
 * @date 2021/11/27 15:05
 */
public class DubboGenericService {

    /**
     * dubbo 泛化调用
     *
     * @param dubboAppName   调用dubbo的APP 名称
     * @param dubboJobConfig DubboJobConfigurationDTO dubbo配置信息
     * @return Object
     */
    public static Object invoke(String dubboAppName, DubboJobConfig dubboJobConfig) {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfig.getZkAddressList()), "dubbo" +
                "服务ZK注册中心地址不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfig.getInterfaceName()), "dubbo" +
                "服务类地址不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfig.getMethod()), "dubbo服务调用方法不能为空！");
        //查询job内容
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
        // 当前dubbo consumer的application配置，不设置会直接抛异常
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(dubboAppName);
        // 注册中心配置
        RegistryConfig registryConfig = new RegistryConfig();
        // 注册中心这里需要配置上注册中心协议，例如下面的zookeeper
        registryConfig.setAddress("zookeeper://" + dubboJobConfig.getZkAddressList());
        registryConfig.setGroup(dubboJobConfig.getGroup());
        reference.setApplication(applicationConfig);
        reference.setRegistry(registryConfig);
        // 设置调用的reference属性，下面只设置了协议、接口名、版本、超时时间
        reference.setProtocol("dubbo");
        reference.setInterface(dubboJobConfig.getInterfaceName());
        reference.setVersion(dubboJobConfig.getVersion());
        reference.setTimeout(dubboJobConfig.getTimeout());
        // 声明为泛化接口
        reference.setGeneric(true);
        reference.setCheck(true);
        // GenericService可以接住所有的实现
        GenericService genericService = reference.get();
        return invokeDubbo(genericService, dubboJobConfig);
    }


    /**
     * 调用
     *
     * @param genericService GenericService
     * @param dubboJobConfig DubboJobConfiguration
     */
    private static Object invokeDubbo(GenericService genericService, DubboJobConfig dubboJobConfig) {

        if (StringUtils.isEmpty(dubboJobConfig.getArgs())) {
            return genericService.$invoke(dubboJobConfig.getMethod(), null,
                    null);
        } else {
            String[] args = dubboJobConfig.getArgs().split(",");
            String[] argTypes = new String[args.length];
            Object[] argVals = new Object[args.length];
            int i = 0;
            for (String arg : args) {
                argTypes[i] = "java.lang.String";
                argVals[i] = arg;
                i++;
            }
            return genericService.$invoke(dubboJobConfig.getMethod(), argTypes, argVals);
        }
    }
}
