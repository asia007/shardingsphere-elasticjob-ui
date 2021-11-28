package org.apache.shardingsphere.elasticjob.lite.client.job;

import com.alibaba.fastjson.JSON;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.client.dto.DubboJobConfig;
import org.apache.shardingsphere.elasticjob.lite.client.registry.DubboJobRegistryCenterProperties;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * dubbo job服务
 * 所有执行的任务都不需要分片，因为业务处理都是交给dubbo服务来处理
 *
 * @author songhui
 * @date 2021/11/27 15:50
 */
@Service
public class DubboJobService {

    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Value("${dubbo.application.name}")
    private String dubboAppName;

    /**
     * 创建定时任务并执行
     *
     * @param dubboJobConfig
     */
    public void createJob(DubboJobConfig dubboJobConfig) {
        //  定时调度作业
        new ScheduleJobBootstrap(zookeeperRegistryCenter, new DubboJob(dubboAppName),
                createJobConfiguration(dubboJobConfig)).schedule();
    }

    /**
     * 创建job的配置，将dubbo调用配置存放在job的参数中就不用每次都查询dubbo配置
     *
     * @param dubboJobConfig dubbo配置
     * @return JobConfiguration
     */
    private JobConfiguration createJobConfiguration(DubboJobConfig dubboJobConfig) {

        return JobConfiguration.newBuilder(dubboJobConfig.getName(), 1).cron(dubboJobConfig.getCorn()).description(dubboJobConfig.getDesc()).jobParameter(JSON.toJSONString(dubboJobConfig)).build();
    }
}
