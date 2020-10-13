package com.uc.firegroup.service.tools.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.uc.framework.App;
import com.uc.framework.natives.MethodLevelWrapper;
import com.uc.framework.thread.AsyncTask;

@Component
public class AppEventProcessor implements InitializingBean, DisposableBean, BeanPostProcessor {

    static class EventObject {
        /** 事件类型 */
        int type;
        /** 事件 参数 */
        Object[] source;

        public EventObject(int type, Object[] source) {
            super();
            this.type = type;
            this.source = source;
        }

        public EventObject(int type) {
            super();
            this.type = type;
        }
    }

    private ArrayBlockingQueue<EventObject> queues;

    private Thread loopThread;

    private Map<Integer, List<MethodLevelWrapper>> observer = new ConcurrentHashMap<>();

    /***
     * 
     * title: 发送事件
     *
     * @param <T>
     * @param type
     * @param source
     * @author HadLuo 2020-9-17 14:13:56
     */
    public static <T> void sendEvent(int type, @SuppressWarnings("unchecked") T... source) {
        try {
            App.getBean(AppEventProcessor.class).queues.put(new EventObject(type, source));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /***
     * 
     * title: 发送事件
     *
     * @param <T>
     * @param type
     * @author HadLuo 2020-9-17 14:14:11
     */
    public static <T> void sendEvent(int type) {
        try {
            App.getBean(AppEventProcessor.class).queues.put(new EventObject(type));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        queues = new ArrayBlockingQueue<EventObject>(200);
        // 开启 监听
        loopThread = new Thread(Thread.currentThread().getThreadGroup(), new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        EventObject eventObject = queues.take();
                        if (eventObject != null) {
                            // 执行 观察者们
                            List<MethodLevelWrapper> invokes = observer.get(eventObject.type);
                            for (MethodLevelWrapper invoke : invokes) {
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (eventObject.source == null) {
                                                invoke.getMethod().invoke(App.getBean(invoke.getClazz()));
                                            } else {
                                                invoke.getMethod().invoke(App.getBean(invoke.getClazz()),
                                                        eventObject.source);
                                            }
                                        } catch (BeansException e) {
                                            e.printStackTrace();
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        } catch (IllegalArgumentException e) {
                                            e.printStackTrace();
                                        } catch (InvocationTargetException e) {
                                            throw new RuntimeException(e.getTargetException());
                                        }
                                    }
                                });
                            }
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }, "AppEvent-Thread-0");
        loopThread.start();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        for (Method m : bean.getClass().getDeclaredMethods()) {
            Event appEvent = m.getAnnotation(Event.class);
            if (appEvent != null) {
                List<MethodLevelWrapper> datas = observer.get(appEvent.value());
                if (datas == null) {
                    datas = new ArrayList<MethodLevelWrapper>();
                    observer.put(appEvent.value(), datas);
                }
                datas.add(MethodLevelWrapper.getWrapper(m, bean.getClass()));
            }
        }
        return bean;
    }

    @Override
    public void destroy() throws Exception {
        if (queues != null) {
            queues.clear();
        }
        if (loopThread != null) {
            loopThread.interrupt();
        }
        loopThread = null;

    }

}
