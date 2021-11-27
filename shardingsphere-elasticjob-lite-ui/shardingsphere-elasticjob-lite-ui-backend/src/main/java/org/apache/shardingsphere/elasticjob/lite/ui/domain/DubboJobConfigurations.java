package org.apache.shardingsphere.elasticjob.lite.ui.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * dubbo job配置集合
 *
 * @author songhui
 * @date 2021/11/23 15:54
 */
@Getter
@Setter
public class DubboJobConfigurations {

    private Set<DubboJobConfiguration> dubboJobConfigurations = new LinkedHashSet<>();
}
