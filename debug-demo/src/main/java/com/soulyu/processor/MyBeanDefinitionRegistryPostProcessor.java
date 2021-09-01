package com.soulyu.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * @Author soul.yu
 * @Date 2021/8/16 2:24 下午
 * @Version 1.0
 */
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 回调
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        String className = "com.soulyu.bean.MyBean";
        String beanName = "myBean";
        // 注册
        BeanDefinition bd = null;
        try {
            bd = BeanDefinitionReaderUtils.createBeanDefinition(null, className, null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // 设置属性值
        PropertyValue pv = new PropertyValue("a", new RuntimeBeanReference("a"));
        bd.getPropertyValues().addPropertyValue(pv);
        registry.registerBeanDefinition(beanName, bd);
    }
}