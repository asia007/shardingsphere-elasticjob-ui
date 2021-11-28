package org.apache.shardingsphere.elasticjob.lite.client.job;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.lite.client.dto.DubboJobConfig;
import org.apache.shardingsphere.elasticjob.lite.client.dubbo.DubboGenericService;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

/**
 * dubbo 执行的job
 *
 * @author songhui
 * @date 2021/11/27 15:50
 */
@Slf4j
public class DubboJob implements SimpleJob {

    private String dubboAppName;

    public DubboJob(String dubboAppName) {
        this.dubboAppName = dubboAppName;
    }

    @Override
    public void execute(ShardingContext shardingContext) {

        try {
            if (!StringUtils.isEmpty(shardingContext.getJobParameter())) {
                DubboJobConfig dubboJobConfig = JSON.parseObject(shardingContext.getJobParameter(),
                        DubboJobConfig.class);
                DubboGenericService.invoke(dubboAppName, dubboJobConfig);
            }
        } catch (Exception e) {
            log.error("执行job" + shardingContext.getJobName() + "出现异常", e);
        }
    }
}
