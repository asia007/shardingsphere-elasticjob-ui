package org.apache.shardingsphere.elasticjob.lite.client.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.lite.client.dto.DubboJobConfig;
import org.apache.shardingsphere.elasticjob.lite.client.job.DubboJobService;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dubbo job的注册中心操作
 *
 * @author songhui
 * @date 2021/11/27 14:32
 */
@Component
@Slf4j
public class DubboJobRegistryOperate {

    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Autowired
    private DubboJobService dubboJobService;

    /**
     * 监听dubbo job在ZK中的数据变化，当有新的Dubbo job节点创建时则创建新job
     */
    @PostConstruct
    public void createCuratorCacheListener() {

        log.info("开启dubbo job的节点监听");
        //当前节点
        CuratorCache curatorCache =
                CuratorCache.builder(zookeeperRegistryCenter.getClient(), DubboJobNodePath.getRootNode()).build();
        //监听子节点，不监听当前节点
        CuratorCacheListener pathCacheListener = CuratorCacheListener
                .builder()
                .forPathChildrenCache(DubboJobNodePath.getRootNode(), zookeeperRegistryCenter.getClient(),
                        new PathChildrenCacheListener() {
                            @Override
                            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                                    //有节点增加时
                                    log.info("有新的dubbo job创建，需要创建新的dubbo定时任务");
                                    String config = zookeeperRegistryCenter.get(event.getData().getPath());
                                    if (!StringUtils.isEmpty(config)) {
                                        DubboJobConfig dubboJobConfig = YamlEngine.unmarshal(config,
                                                DubboJobConfig.class);
                                        dubboJobService.createJob(dubboJobConfig);
                                    }
                                }
                            }
                        })
                .build();
        curatorCache.listenable().addListener(pathCacheListener);
        curatorCache.start();
    }

    /**
     * 服务启动时加载一次所有的任务
     */
    @PostConstruct
    public void loadAllDubboJob() {

        List<DubboJobConfig> dubboJobConfigList = loadAllDubboJobConfig();
        if (!dubboJobConfigList.isEmpty()) {
            dubboJobConfigList.forEach(dubboJobConfig -> {
                dubboJobService.createJob(dubboJobConfig);
                log.info("服务启动初始化定时任务：" + dubboJobConfig.getName() + "完成");
            });
        }
    }

    /**
     * 查询所有dubbo job的配置
     *
     * @return List<DubboJobConfig>
     */
    private List<DubboJobConfig> loadAllDubboJobConfig() {
        List<String> children = zookeeperRegistryCenter.getChildrenKeys(DubboJobNodePath.getRootNode());
        List<DubboJobConfig> dubboJobConfigList = new ArrayList<>();
        children.forEach(jobName -> {
            DubboJobConfig dubboJobConfig = getDubboConfig(jobName);
            if (dubboJobConfig != null) {
                dubboJobConfigList.add(dubboJobConfig);
            }
        });
        return dubboJobConfigList;
    }

    /***
     * 获取job配置信息
     * @param name 名称
     * @return DubboJobConfiguration
     */
    private DubboJobConfig getDubboConfig(String name) {
        String config = zookeeperRegistryCenter.get(DubboJobNodePath.getFullNode(name));
        if (StringUtils.isEmpty(config)) {
            return null;
        }
        return YamlEngine.unmarshal(config, DubboJobConfig.class);
    }

}
