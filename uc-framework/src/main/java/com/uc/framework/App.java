package com.uc.framework;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

/**
 * App
 * 
 * @author HadLuo
 * @date 2020-9-2 11:15:54
 */
@Component
public class App implements BeanFactoryAware {

    private static BeanFactory beanFactory;

    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Preconditions.checkNotNull(beanFactory);
        App.beanFactory = beanFactory;
    }

    public static Object getBean(String name) throws BeansException {
        // TODO Auto-generated method stub
        return beanFactory.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        // TODO Auto-generated method stub
        return beanFactory.getBean(name, requiredType);
    }

    public static Object getBean(String name, Object... args) throws BeansException {
        // TODO Auto-generated method stub
        return beanFactory.getBean(name, args);
    }

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        // TODO Auto-generated method stub
        return beanFactory.getBean(requiredType);
    }

    public static <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        // TODO Auto-generated method stub
        return beanFactory.getBean(requiredType, args);
    }

    public boolean containsBean(String name) {
        // TODO Auto-generated method stub
        return beanFactory.containsBean(name);
    }

    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        // TODO Auto-generated method stub
        return beanFactory.isSingleton(name);
    }

    public static boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        // TODO Auto-generated method stub
        return beanFactory.isPrototype(name);
    }

    public static boolean isTypeMatch(String name, ResolvableType typeToMatch)
            throws NoSuchBeanDefinitionException {
        // TODO Auto-generated method stub
        return beanFactory.isTypeMatch(name, typeToMatch);
    }

    public static boolean isTypeMatch(String name, Class<?> typeToMatch)
            throws NoSuchBeanDefinitionException {
        // TODO Auto-generated method stub
        return beanFactory.isTypeMatch(name, typeToMatch);
    }

    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        // TODO Auto-generated method stub
        return beanFactory.getType(name);
    }

    public static Class<?> getType(String name, boolean allowFactoryBeanInit)
            throws NoSuchBeanDefinitionException {
        // TODO Auto-generated method stub
        return beanFactory.getType(name, allowFactoryBeanInit);
    }

    public static String[] getAliases(String name) {
        // TODO Auto-generated method stub
        return beanFactory.getAliases(name);
    }

}
