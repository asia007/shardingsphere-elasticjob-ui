package org.apache.shardingsphere.elasticjob.lite.ui.cache;

import org.apache.shardingsphere.elasticjob.lite.ui.domain.DubboJobConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * dubboJob本地缓存
 *
 * @author songhui
 * @date 2021/11/24 15:15
 */
public class DubboJobLocalCache {

    /**
     * dubboJob本地缓存
     */
    public static Map<String, DubboJobConfiguration> DUBBO_JOB_CONFIGURATION_MAP = new HashMap<>();
}
