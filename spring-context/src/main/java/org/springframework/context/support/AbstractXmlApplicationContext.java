/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * Convenient base class for {@link org.springframework.context.ApplicationContext}
 * implementations, drawing configuration from XML documents containing bean definitions
 * understood by an {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
 *
 * <p>Subclasses just have to implement the {@link #getConfigResources} and/or
 * the {@link #getConfigLocations} method. Furthermore, they might override
 * the {@link #getResourceByPath} hook to interpret relative paths in an
 * environment-specific fashion, and/or {@link #getResourcePatternResolver}
 * for extended pattern resolution.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConfigResources
 * @see #getConfigLocations
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 */
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableConfigApplicationContext {

    private boolean validating = true;


    /**
     * Create a new AbstractXmlApplicationContext with no parent.
     */
    public AbstractXmlApplicationContext() {
    }

    /**
     * Create a new AbstractXmlApplicationContext with the given parent context.
     *
     * @param parent the parent context
     */
    public AbstractXmlApplicationContext(@Nullable ApplicationContext parent) {
        super(parent);
    }


    /**
     * Set whether to use XML validation. Default is {@code true}.
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }


    /**
     * 加载bean定义，放入bean工厂注册表中
     * 重写父类方法 {@link AbstractRefreshableApplicationContext#loadBeanDefinitions(org.springframework.beans.factory.support.DefaultListableBeanFactory)}
     * DefaultListableBeanFactory
     *
     * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
     * @see #initBeanDefinitionReader
     * @see #loadBeanDefinitions
     */
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
        // 创建xml bean定义阅读器，
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

        // 覆盖阅读器的环境为当前上下文环境
        beanDefinitionReader.setEnvironment(this.getEnvironment());
        // 覆盖资源解析器为当前对象（当前上下文对象）
        beanDefinitionReader.setResourceLoader(this);
        // 设置试题解析器
        beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

        // 初始化bean定义阅读区（开启xml验证）
        initBeanDefinitionReader(beanDefinitionReader);
        // 从用户自定义xml文件中加载bean定义
        loadBeanDefinitions(beanDefinitionReader);
    }

    /**
     * 初始化加载上下文中bean的bean定义阅读器
     * <p>可以在子类中覆盖，例如用于关闭 XML 验证或使用不同的 XmlBeanDefinitionParser 实现。
     *
     * @param reader
     * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader#setDocumentReaderClass
     */
    protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
        // 开启xml验证
        reader.setValidating(this.validating);
    }

    /**
     * 利用指定的 XmlBeanDefinitionReader 从用户xml文件中加载bean定义
     * <p>bean 工厂的生命周期由 {@link #refreshBeanFactory} 方法处理；因此这个方法只是应该加载和/或注册 bean 定义。
     *
     * @param reader xml bean定义阅读起，上游创建
     * @throws BeansException in case of bean registration errors
     * @throws IOException    if the required XML document isn't found
     * @see #refreshBeanFactory
     * @see #getConfigLocations
     * @see #getResources
     * @see #getResourcePatternResolver
     */
    protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
        // 获取资源配置
        Resource[] configResources = getConfigResources();
        if (configResources != null) {
            reader.loadBeanDefinitions(configResources);
        }

        // 获取配置信息，refresh方法之前已在上下文设置好
        String[] configLocations = getConfigLocations();
        if (configLocations != null) {
            // 使用阅读器加载bean 定义
            reader.loadBeanDefinitions(configLocations);
        }
    }

    /**
     * Return an array of Resource objects, referring to the XML bean definition
     * files that this context should be built with.
     * <p>The default implementation returns {@code null}. Subclasses can override
     * this to provide pre-built Resource objects rather than location Strings.
     *
     * @return an array of Resource objects, or {@code null} if none
     * @see #getConfigLocations()
     */
    @Nullable
    protected Resource[] getConfigResources() {
        return null;
    }

}
