package com.uc.firegroup.service.tools.msg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.swagger.models.auth.In;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uc.external.bilin.res.MqCallbackMessage;
import com.uc.framework.App;
import com.uc.framework.logger.Logs;
import com.uc.framework.resources.PackageScans;

/***
 * 
 * title: message组件处理器
 *
 * @author HadLuo
 * @date 2020-9-12 15:24:39
 */
@Component
public class MessageProcessor implements InitializingBean, BeanFactoryAware {
    /** 扫描message路径 */
    private String scan = "com.uc.firegroup.service";
    /** Spring context */
    private DefaultListableBeanFactory beanFactory;

    class Entry {
        Class<?> clazz;
        Method method;
        Class<?> convertClass;

        public Entry(Class<?> clazz, Method method, Class<?> convertClass) {
            super();
            this.clazz = clazz;
            this.convertClass = convertClass;
            this.method = method;
        }

    }

    private Map<Integer, Entry> entrys = new ConcurrentHashMap<Integer, Entry>();

    public void invoke(MqCallbackMessage message, String topic) throws Throwable {
        Entry entry = entrys.get(message.getType());
        if (entry == null) {
            // 没有注册的消息 ， 直接丢弃
//            Logs.e(getClass(),
//                    "[不关心的消息，直接丢弃]>>code=" + message.getType() + ",message=" + JSON.toJSONString(message));
            return;
        }
        Logs.e(getClass(), "[kafka msg]>>Topic:" + topic + ",Message:" + JSON.toJSONString(message));
        // 执行方法
        try {
            if (entry.convertClass == null || entry.convertClass == Object.class) {
                entry.method.invoke(App.getBean(entry.clazz), message);
            } else {
                if (message.getData() instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) message.getData();
                    entry.method.invoke(App.getBean(entry.clazz),
                            jsonObject.toJavaObject(entry.convertClass));
                } else if (message.getData() instanceof JSONArray) {
                    JSONArray jSONArray = (JSONArray) message.getData();
                    entry.method.invoke(App.getBean(entry.clazz), jSONArray.toJavaList(entry.convertClass));
                }

            }
        } catch (InvocationTargetException e) {
            // 抛出业务执行异常
            throw e.getTargetException();
        }
    }

    public String getScan() {
        return scan;
    }

    public void setScan(String scan) {
        this.scan = scan;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Set<Class<?>> classes = PackageScans.findPackageClass(scan, Message.class);
        if (CollectionUtils.isEmpty(classes)) {
            return;
        }
        for (Class<?> clazz : classes) {
            // find 所有方法
            Type type;
            Entry entry;
            boolean usefully = false;
            for (Method method : clazz.getDeclaredMethods()) {
                type = method.getAnnotation(Type.class);
                if (type != null) {
                    if (type.value() <= 0) {
                        throw new RuntimeException("message 组件 type 值不能小于等于0");
                    }
                    // 构造entry
                    entry = entrys.get(type.value());
                    if (entry == null) {
                        entry = new Entry(clazz, method, type.clazz());
                        entrys.put(type.value(), entry);
                        usefully = true;
                    } else {
                        throw new RuntimeException(
                                "message 组件 type 值必须唯一:   m1=" + method + ",m2=" + entry.method);
                    }
                }
            }
            if (usefully) {
                // 注入到spring
                BeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClassName(clazz.getName());
                beanFactory.registerBeanDefinition(clazz.getName(), beanDefinition);
                Logs.console().i(getClass(), "scan message 组件>> clazz=" + clazz.getName());
            }

        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    public boolean containsType(Integer type){
        return entrys.keySet().contains(type);
    }
}
