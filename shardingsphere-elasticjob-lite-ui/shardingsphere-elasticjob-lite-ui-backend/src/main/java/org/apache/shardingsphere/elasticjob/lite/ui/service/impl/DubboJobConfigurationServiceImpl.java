package org.apache.shardingsphere.elasticjob.lite.ui.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.client.dto.DubboJobConfig;
import org.apache.shardingsphere.elasticjob.lite.client.dubbo.DubboGenericService;
import org.apache.shardingsphere.elasticjob.lite.client.registry.DubboJobNodePath;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.ref.PhantomReference;
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


    @Override
    public DubboJobConfigurations loadAll() {

        CoordinatorRegistryCenter coordinatorRegistryCenter = coordinatorRegistryCenter();
        List<String> children = coordinatorRegistryCenter.getChildrenKeys(DubboJobNodePath.getRootNode());
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
        if (coordinatorRegistryCenter().isExisted(DubboJobNodePath.getJobNode(dubboJobConfiguration.getName()))) {
            throw new JobConsoleException(JobConsoleException.INVALID_PARAM, "作业名称已存在");
        }
        //写入zk
        CoordinatorRegistryCenter coordinatorRegistryCenter = coordinatorRegistryCenter();
        coordinatorRegistryCenter.persist(DubboJobNodePath.getFullNode(dubboJobConfiguration.getName()),
                YamlEngine.marshal(dubboJobConfiguration));
        return true;
    }

    /**
     * 校验参数
     *
     * @param dubboJobConfigurationDTO 配置
     */
    private void checkArg(DubboJobConfiguration dubboJobConfigurationDTO) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfigurationDTO.getName()), "作业名称不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfigurationDTO.getCorn()), "作业调度频率不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfigurationDTO.getZkAddressList()), "dubbo" +
                "服务ZK注册中心地址不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfigurationDTO.getInterfaceName()), "dubbo" +
                "服务类地址不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dubboJobConfigurationDTO.getMethod()), "dubbo服务调用方法不能为空！");
    }

    @Override
    public boolean delete(String name) {

        //删除前检查job状态
        JobBriefInfo jobBriefInfo = jobAPIService.getJobStatisticsAPI().getJobBriefInfo(name);

        if (jobBriefInfo != null && (JobBriefInfo.JobStatus.OK.equals(jobBriefInfo.getStatus()) || JobBriefInfo.JobStatus.SHARDING_FLAG.equals(jobBriefInfo.getStatus()))) {
            throw new JobConsoleException(JobConsoleException.SERVER_ERROR, "当前作业状态不支持删除操作！");
        }
        //查询job
        DubboJobConfiguration dubboJobConfiguration = getDubboConfig(name);
        DubboGenericService.destroy(dubboAppName, createDubboJobConfig(dubboJobConfiguration));
        coordinatorRegistryCenter().remove(DubboJobNodePath.getJobNode(name));
        return true;
    }

    /***
     * 获取job配置信息
     * @param name 名称
     * @return DubboJobConfiguration
     */
    private DubboJobConfiguration getDubboConfig(String name) {
        String config = coordinatorRegistryCenter().get(DubboJobNodePath.getFullNode(name));
        if (StringUtils.isEmpty(config)) {
            return null;
        }
        return YamlEngine.unmarshal(config, DubboJobConfiguration.class);
    }

    @Override
    public Object connect(DubboJobConfiguration dubboJobConfiguration) {

        DubboJobConfig dubboJobConfig = createDubboJobConfig(dubboJobConfiguration);
        return DubboGenericService.invoke(dubboAppName, dubboJobConfig);
    }

    /**
     * 创建dubbo job配置
     *
     * @param dubboJobConfiguration DubboJobConfiguration
     * @return DubboJobConfig
     */
    private DubboJobConfig createDubboJobConfig(DubboJobConfiguration dubboJobConfiguration) {

        return new DubboJobConfig(dubboJobConfiguration.getZkAddressList(),
                dubboJobConfiguration.getGroup(), dubboJobConfiguration.getVersion(),
                dubboJobConfiguration.getTimeout(), dubboJobConfiguration.getInterfaceName(),
                dubboJobConfiguration.getMethod(), dubboJobConfiguration.getArgs());
    }
}
