package org.apache.shardingsphere.elasticjob.lite.ui.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.EchoService;
import org.apache.dubbo.rpc.service.GenericService;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.reg.RegistryCenterFactory;
import org.apache.shardingsphere.elasticjob.lite.ui.domain.DubboJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.ui.domain.DubboJobConfigurations;
import org.apache.shardingsphere.elasticjob.lite.ui.domain.RegistryCenterConfiguration;
import org.apache.shardingsphere.elasticjob.lite.ui.exception.JobConsoleException;
import org.apache.shardingsphere.elasticjob.lite.ui.service.DubboJobConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.ui.service.JobAPIService;
import org.apache.shardingsphere.elasticjob.lite.ui.util.SessionRegistryCenterConfiguration;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * dubbo job 服务实现
 *
 * @author songhui
 * @date 2021/11/23 10:44
 */
@Service
@Slf4j
public class DubboJobConfigurationServiceImpl implements DubboJobConfigurationService {

    @Value("${dubbo.application.name}")
    private String dubboAppName;

    @Autowired
    private JobAPIService jobAPIService;

    /**
     * dubbo job主节点
     */
    private static final String ELASTIC_DUBBO_JOB_NODE = "elasticDubboJob";
    /**
     * 节点配置
     */
    private static final String ELASTIC_DUBBO_JOB_NODE_CONFIG = "config";

    /**
     * 创建CoordinatorRegistryCenter
     *
     * @return CoordinatorRegistryCenter
     */
    private CoordinatorRegistryCenter coordinatorRegistryCenter() {
        RegistryCenterConfiguration regCenterConfig =
                SessionRegistryCenterConfiguration.getRegistryCenterConfiguration();
        if (regCenterConfig == null) {
            throw new JobConsoleException(JobConsoleException.SERVER_ERROR, "请先连接dubbo作的ZK注册空间");
        }
        return RegistryCenterFactory.createCoordinatorRegistryCenter(regCenterConfig.getZkAddressList(),
                regCenterConfig.getNamespace(), regCenterConfig.getDigest());
    }

    /**
     * 获取节点全路径
     *
     * @param name 名字
     * @return 获取节点全路径
     */
    private String getFullNode(String name) {
        return String.format("/%s/%s/%s", ELASTIC_DUBBO_JOB_NODE, name, ELASTIC_DUBBO_JOB_NODE_CONFIG);
    }

    /**
     * job节点
     *
     * @param name 名称
     * @return job节点
     */
    private String getJobNode(String name) {
        return String.format("/%s/%s", ELASTIC_DUBBO_JOB_NODE, name);
    }

    /**
     * 根节点
     *
     * @return 根节点
     */
    private String getRootNode() {
        return String.format("/%s", ELASTIC_DUBBO_JOB_NODE);
    }

    @Override
    public DubboJobConfigurations loadAll() {

        CoordinatorRegistryCenter coordinatorRegistryCenter = coordinatorRegistryCenter();
        List<String> children = coordinatorRegistryCenter.getChildrenKeys(getRootNode());
        DubboJobConfigurations dubboJobConfigurations = new DubboJobConfigurations();
        children.forEach(jobName -> {
            DubboJobConfiguration dubboJobConfiguration = getDubboConfig(jobName);
            if (dubboJobConfiguration != null) {
                dubboJobConfigurations.getDubboJobConfigurations().add(dubboJobConfiguration);
            }
        });
        return dubboJobConfigurations;
    }

    @Override
    public boolean add(DubboJobConfiguration dubboJobConfiguration) {

        checkArg(dubboJobConfiguration);
        //重名校验
        if (coordinatorRegistryCenter().isExisted(getJobNode(dubboJobConfiguration.getName()))) {
            throw new JobConsoleException(JobConsoleException.INVALID_PARAM, "作业名称已存在");
        }
        //写入zk
        CoordinatorRegistryCenter coordinatorRegistryCenter = coordinatorRegistryCenter();
        coordinatorRegistryCenter.persist(getFullNode(dubboJobConfiguration.getName()),
                YamlEngine.marshal(dubboJobConfiguration));
        return true;
    }

    /**
     * 校验参数
     *
     * @param dubboJobConfiguration 配置
     */
    private void checkArg(DubboJobConfiguration dubboJobConfiguration) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfiguration.getName()), "作业名称不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfiguration.getCorn()), "作业调度频率不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfiguration.getZkAddressList()), "dubbo" +
                "服务ZK注册中心地址不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfiguration.getInterfaceName()), "dubbo" +
                "服务类地址不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfiguration.getMethod()), "dubbo服务调用方法不能为空！");
    }

    @Override
    public boolean delete(String name) {

        //删除前检查job状态
        JobBriefInfo jobBriefInfo = jobAPIService.getJobStatisticsAPI().getJobBriefInfo(name);

        if (jobBriefInfo != null && (JobBriefInfo.JobStatus.OK.equals(jobBriefInfo.getStatus()) || JobBriefInfo.JobStatus.SHARDING_FLAG.equals(jobBriefInfo.getStatus()))) {
            throw new JobConsoleException(JobConsoleException.SERVER_ERROR, "当前作业状态不支持删除操作！");
        }
        coordinatorRegistryCenter().remove(getJobNode(name));
        return true;
    }

    /***
     * 获取job配置信息
     * @param name 名称
     * @return DubboJobConfiguration
     */
    private DubboJobConfiguration getDubboConfig(String name) {
        String config = coordinatorRegistryCenter().get(getFullNode(name));
        if (StringUtils.isEmpty(config)) {
            return null;
        }
        return YamlEngine.unmarshal(config, DubboJobConfiguration.class);
    }

    @Override
    public Object connect(DubboJobConfiguration dubboJobConfiguration) {

        checkArg(dubboJobConfiguration);
        //查询job内容
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
        // 当前dubbo consumer的application配置，不设置会直接抛异常
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(dubboAppName);
        // 注册中心配置
        RegistryConfig registryConfig = new RegistryConfig();
        // 注册中心这里需要配置上注册中心协议，例如下面的zookeeper
        registryConfig.setAddress("zookeeper://" + dubboJobConfiguration.getZkAddressList());
        registryConfig.setGroup(dubboJobConfiguration.getGroup());
        reference.setApplication(applicationConfig);
        reference.setRegistry(registryConfig);
        // 设置调用的reference属性，下面只设置了协议、接口名、版本、超时时间
        reference.setProtocol("dubbo");
        reference.setInterface(dubboJobConfiguration.getInterfaceName());
        reference.setVersion(dubboJobConfiguration.getVersion());
        reference.setTimeout(dubboJobConfiguration.getTimeout());
        // 声明为泛化接口
        reference.setGeneric(true);
        reference.setCheck(true);
        // GenericService可以接住所有的实现
        GenericService genericService = reference.get();
        return invokeDubbo(genericService, dubboJobConfiguration);
    }

    /**
     * 调用
     *
     * @param genericService        GenericService
     * @param dubboJobConfiguration DubboJobConfiguration
     */
    private Object invokeDubbo(GenericService genericService, DubboJobConfiguration dubboJobConfiguration) {

        if (StringUtils.isEmpty(dubboJobConfiguration.getArgs())) {
            return genericService.$invoke(dubboJobConfiguration.getMethod(), null,
                    null);
        } else {
            String[] args = dubboJobConfiguration.getArgs().split(",");
            String[] argTypes = new String[args.length];
            Object[] argVals = new Object[args.length];
            int i = 0;
            for (String arg : args) {
                argTypes[i] = "java.lang.String";
                argVals[i] = arg;
                i++;
            }
            return genericService.$invoke(dubboJobConfiguration.getMethod(), argTypes, argVals);
        }
    }
}
