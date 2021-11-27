package org.apache.shardingsphere.elasticjob.lite.ui.web.controller;

import org.apache.shardingsphere.elasticjob.lite.ui.domain.DubboJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.ui.domain.RegistryCenterConfiguration;
import org.apache.shardingsphere.elasticjob.lite.ui.service.DubboJobConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.ui.web.response.ResponseResult;
import org.apache.shardingsphere.elasticjob.lite.ui.web.response.ResponseResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;

/**
 * dubbo job服务controller
 *
 * @author songhui
 * @date 2021/11/23 10:17
 */
@RestController
@RequestMapping("/api/dubbo-job")
public class DubboJobController {

    @Autowired
    private DubboJobConfigurationService dubboJobConfigurationService;

    /**
     * 加载dubbo job服务列表
     *
     * @param request HTTP request
     * @return ResponseResult<Collection < DubboJobConfiguration>>
     */
    @GetMapping("/load")
    public ResponseResult<Collection<DubboJobConfiguration>> load(final HttpServletRequest request) {

        return ResponseResultUtil.build(dubboJobConfigurationService.loadAll().getDubboJobConfigurations());
    }

    /**
     * 添加dubbo job服务
     *
     * @param config DubboJobConfiguration
     * @return ResponseResult<Boolean>
     */
    @PostMapping("/add")
    public ResponseResult<Boolean> add(@RequestBody final DubboJobConfiguration config) {
        return ResponseResultUtil.build(dubboJobConfigurationService.add(config));
    }

    /**
     * 删除dubbo job服务
     *
     * @param config dubbo job
     */
    @DeleteMapping
    public ResponseResult delete(@RequestBody final DubboJobConfiguration config) {

        return ResponseResultUtil.build(dubboJobConfigurationService.delete(config.getName()));
    }

    /***
     *连接测试
     * @param config 配置
     * @param request  HttpServletRequest
     * @return boolean
     */
    @PostMapping(value = "/connect")
    public ResponseResult<Object> connect(@RequestBody final DubboJobConfiguration config,
                                          final HttpServletRequest request)  {

        return ResponseResultUtil.build(dubboJobConfigurationService.connect(config));
    }

}
