package org.apache.shardingsphere.elasticjob.lite.client.registry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "elasticjob.zk.register")
public final class DubboJobRegistryCenterProperties {

    /**
     * 注册空间名称
     */
    private String name;
    /**
     * 注册空间ZK地址
     */
    private String zkAddressList;
    /**
     * 空间名称
     */
    private String namespace;
    /**
     * 认证
     */
    private String digest;
}
