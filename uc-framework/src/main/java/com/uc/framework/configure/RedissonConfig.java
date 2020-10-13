package com.uc.framework.configure;

import java.util.ArrayList;
import java.util.List;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 
 * title: redisson集群实现方案
 *
 * @author HadLuo
 * @date 2020-9-15 10:07:55
 */
@Configuration
public class RedissonConfig {

    @Autowired
    private RedisConfigProperties redisConfigProperties;

    @Bean
    public RedissonClient redissonClient() {

        if (redisConfigProperties.getCluster() != null) {
            // redisson版本是3.5，集群的ip前面要加上“redis://”，不然会报错，3.2版本可不加
            List<String> clusterNodes = new ArrayList<>();
            for (int i = 0; i < redisConfigProperties.getCluster().getNodes().size(); i++) {
                clusterNodes.add("redis://" + redisConfigProperties.getCluster().getNodes().get(i));
            }
            Config config = new Config();
            // 添加集群地址
            ClusterServersConfig clusterServersConfig = config.useClusterServers()
                    .addNodeAddress(clusterNodes.toArray(new String[clusterNodes.size()]));
            // 设置密码
            clusterServersConfig.setPassword(redisConfigProperties.getPassword());
            return Redisson.create(config);
        } else {
            Config config = new Config();
            config.setTransportMode(TransportMode.NIO);
            config.setCodec(JsonJacksonCodec.INSTANCE);
            config.useSingleServer()
                    .setAddress("redis://" + redisConfigProperties.getHost() + ":"
                            + redisConfigProperties.getPort())
                    // 这里一定要处理一下无密码问题
                    .setPassword(StringUtils.isEmpty(redisConfigProperties.getPassword()) ? null
                            : redisConfigProperties.getPassword())
                    .setDatabase(0);
            return Redisson.create(config);
        }
    }

}
