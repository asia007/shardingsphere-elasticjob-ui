package org.apache.shardingsphere.elasticjob.lite.client.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * dubbo job 配置
 *
 * @author songhui
 * @date 2021/11/28 10:26
 */
@Configuration
@Slf4j
public class DubboJobRegistryConfig {

    @Autowired
    private DubboJobRegistryCenterProperties dubboJobRegistryCenterProperties;

    /**
     * 注册中心
     *
     * @return CoordinatorRegistryCenter
     */
    @Bean(name = "zookeeperRegistryCenter")
    public ZookeeperRegistryCenter createRegistryCenter() {
        ZookeeperConfiguration zookeeperConfiguration =
                new ZookeeperConfiguration(dubboJobRegistryCenterProperties.getZkAddressList(),
                        dubboJobRegistryCenterProperties.getNamespace());
        zookeeperConfiguration.setDigest(dubboJobRegistryCenterProperties.getDigest());
        ZookeeperRegistryCenter regCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        regCenter.init();
        log.info("初始化dubbo job的注册中心");
        return regCenter;
    }

}
