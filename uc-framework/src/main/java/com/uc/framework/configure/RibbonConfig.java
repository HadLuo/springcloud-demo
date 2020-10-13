package com.uc.framework.configure;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

/**
 * ribbon 统一配置
 * 
 * @author HadLuo
 * @date 2020-9-8 16:43:26
 */
@Configuration
public class RibbonConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // 设置连接超时5秒
        requestFactory.setReadTimeout(5000); // 设置数据读取超时5秒
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        // 换上fastjson
        List<HttpMessageConverter<?>> httpMessageConverterList = restTemplate.getMessageConverters();
        Iterator<HttpMessageConverter<?>> iterator = httpMessageConverterList.iterator();
        if (iterator.hasNext()) {
            HttpMessageConverter<?> converter = iterator.next();
            // 原有的String是ISO-8859-1编码 去掉
            if (converter instanceof StringHttpMessageConverter) {
                iterator.remove();
            }

            // 由于系统中默认有jackson 在转换json时自动会启用 但是我们不想使用它 可以直接移除或者将fastjson放在首位
            if (converter instanceof GsonHttpMessageConverter
                    || converter instanceof MappingJackson2HttpMessageConverter) {
                iterator.remove();
            }
        }
        httpMessageConverterList.add(new StringHttpMessageConverter(Charset.forName("utf-8")));
        httpMessageConverterList.add(0, fastJsonHttpMessageConverter());

        return restTemplate;
    }

    @SuppressWarnings("deprecation")
    @Bean
    public FastJsonHttpMessageConverter fastJsonHttpMessageConverter() {
        // 需要定义一个convert转换消息的对象;
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        // 添加fastJson的配置信息;
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullBooleanAsFalse);
        // 全局时间配置
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fastJsonConfig.setCharset(Charset.forName("UTF-8"));
        // 处理中文乱码问题
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        // 在convert中添加配置信息.
        fastConverter.setSupportedMediaTypes(fastMediaTypes);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        return fastConverter;
    }
}
