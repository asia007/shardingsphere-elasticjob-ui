package org.apache.shardingsphere.elasticjob.lite.client.registry;

/**
 * dubbo job的节点路径
 *
 * @author songhui
 * @date 2021/11/27 15:41
 */
public class DubboJobNodePath {
    /**
     * dubbo job主节点
     */
    public static final String ELASTIC_DUBBO_JOB_NODE = "elasticDubboJob";

    /**
     * 获取节点全路径
     *
     * @param name 名字
     * @return 获取节点全路径
     */
    public static String getFullNode(String name) {
        return String.format("/%s/%s", ELASTIC_DUBBO_JOB_NODE, name);
    }

    /**
     * job节点
     *
     * @param name 名称
     * @return job节点
     */
    public static String getJobNode(String name) {
        return String.format("/%s/%s", ELASTIC_DUBBO_JOB_NODE, name);
    }

    /**
     * 根节点
     *
     * @return 根节点
     */
    public static String getRootNode() {
        return String.format("/%s", ELASTIC_DUBBO_JOB_NODE);
    }

}
