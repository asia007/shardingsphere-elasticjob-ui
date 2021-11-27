package org.apache.shardingsphere.elasticjob.lite.ui.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * dubbo服务job配置DTO
 *
 * @author songhui
 * @date 2021/11/23 10:28
 */
@Getter
@Setter
@NoArgsConstructor
public class DubboJobConfiguration implements Serializable {

    private static final long serialVersionUID = 6161901443422319251L;

    /**
     * 服务名称
     */
    private String name;
    /**
     * 调度频率
     */
    private String corn;

    /**
     * 作业描述
     */
    private String desc;

    /**
     * dubbo服务ZK地址，例zookeeper://127.0.0.1:2181多个以;分隔
     */
    private String zkAddressList;

    /**
     * dubbo的group
     */
    private String group;
    /**
     * dubbo的版本号
     */
    private String version;

    /**
     * dubbo服务超时时间，单为ms，不设置则为3秒
     */
    private Integer timeout = 3000;

    /**
     * dubbo接口全类名
     */
    private String interfaceName;
    /**
     * dubbo接口方法名
     */
    private String method;

    /**
     * 参数，只支持多个以,分隔，不会进行校验
     */
    private String args;

}
