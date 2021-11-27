package org.apache.shardingsphere.elasticjob.lite.ui.service;

import org.apache.shardingsphere.elasticjob.lite.ui.domain.DubboJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.ui.domain.DubboJobConfigurations;

import java.io.IOException;
import java.util.Collection;

/**
 * dubbo服务job的注册service
 *
 * @author songhui
 * @date 2021/11/23 10:27
 */
public interface DubboJobConfigurationService {

    /**
     * 加载所有的dubbo job服务列表
     *
     * @return
     */
    DubboJobConfigurations loadAll();

    /**
     * 添加dubbo job服务
     *
     * @param dubboJobConfiguration DubboJobConfiguration
     * @return boolean
     */
    boolean add(DubboJobConfiguration dubboJobConfiguration);

    /**
     * 删除dubbo job
     *
     * @param name 名称
     * @return boolean
     */
    boolean delete(String name);

    /**
     * 连接测试
     *
     * @param dubboJobConfiguration 配置
     * @return boolean
     */
    Object connect(DubboJobConfiguration dubboJobConfiguration) ;
}
