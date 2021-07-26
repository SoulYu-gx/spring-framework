package com.soulyu.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;

/**
 * @Author soul.yu
 * @Date 2021/7/23 2:49 下午
 * @Version 1.0
 */
public class MyInstantiationAwareBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor {

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {

        System.out.println("PostProcessor 调用成功");
        return true;
    }
}
