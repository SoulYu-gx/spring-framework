/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link BeanDefinitionDocumentReader} interface that
 * reads bean definitions according to the "spring-beans" DTD and XSD format
 * (Spring's default XML bean definition format).
 *
 * <p>The structure, elements, and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). {@code <beans>} does not need to be the root
 * element of the XML document: this class will parse all bean definition elements
 * in the XML file, regardless of the actual root element.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 18.12.2003
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

    public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

    public static final String NESTED_BEANS_ELEMENT = "beans";

    public static final String ALIAS_ELEMENT = "alias";

    public static final String NAME_ATTRIBUTE = "name";

    public static final String ALIAS_ATTRIBUTE = "alias";

    public static final String IMPORT_ELEMENT = "import";

    public static final String RESOURCE_ATTRIBUTE = "resource";

    public static final String PROFILE_ATTRIBUTE = "profile";


    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * xml阅读器上下文
     *
     * @see DefaultBeanDefinitionDocumentReader#registerBeanDefinitions(org.w3c.dom.Document, org.springframework.beans.factory.xml.XmlReaderContext)
     */
    @Nullable
    private XmlReaderContext readerContext;

    @Nullable
    private BeanDefinitionParserDelegate delegate;


    /**
     * 加载bean定义，并注册到注册表
     */
    @Override
    public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
        this.readerContext = readerContext;
        // 实际加载bean定义，并注册到注册表
        doRegisterBeanDefinitions(doc.getDocumentElement());
    }

    /**
     * Return the descriptor for the XML resource that this parser works on.
     */
    protected final XmlReaderContext getReaderContext() {
        Assert.state(this.readerContext != null, "No XmlReaderContext available");
        return this.readerContext;
    }

    /**
     * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor}
     * to pull the source metadata from the supplied {@link Element}.
     */
    @Nullable
    protected Object extractSource(Element ele) {
        return getReaderContext().extractSource(ele);
    }


    /**
     * 从xml beans 标签中解析bean，加载bean定义。
     *
     * @param root xml的标签
     */
    @SuppressWarnings("deprecation")  // for Environment.acceptsProfiles(String...)
    protected void doRegisterBeanDefinitions(Element root) {
        /**
         * 注释翻译：
         * <p>
         *     任何嵌套的 <beans> 元素都将导致此方法中的递归。
         *     为了正确传播和保留 <beans> default- 属性，请跟踪当前（父）委托，该委托可能为空。
         *     出于回退目的，创建带有对父级的引用的新（子）委托，然后最终将 this.delegate 重置回其原始（父级）引用。
         *     这种行为模拟了一堆委托，而实际上并不需要一个。
         * </p>
         */
        // 将默认的委托器赋给 parent 引用。上一次的委托器
        BeanDefinitionParserDelegate parent = this.delegate;
        // 获取到前面创建的XmlReaderContext，并创建"bean定义解析器"委托
        this.delegate = createDelegate(getReaderContext(), root, parent);

        if (this.delegate.isDefaultNamespace(root)) {
            // 获取环境
            String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
            if (StringUtils.hasText(profileSpec)) {
                String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
                        profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
                // We cannot use Profiles.of(...) since profile expressions are not supported
                // in XML config. See SPR-12458 for details.
                if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec +
                                "] not matching: " + getReaderContext().getResource());
                    }
                    return;
                }
            }
        }

        // 解析前的处理。拓展点
        preProcessXml(root);
        parseBeanDefinitions(root, this.delegate);
        // 解析后的处理，拓展点
        postProcessXml(root);

        // 实际上父委托使用是一个
        this.delegate = parent;
    }

    /**
     * 创建bean定义解析器的委托
     *
     * @param readerContext  阅读器上下文
     * @param root           当前根标签
     * @param parentDelegate 父委托器，上一次的委托器
     * @return
     */
    protected BeanDefinitionParserDelegate createDelegate(
            XmlReaderContext readerContext, Element root, @Nullable BeanDefinitionParserDelegate parentDelegate) {
        // 实例化bean定义解析器委托
        BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
        // 初始化
        delegate.initDefaults(root, parentDelegate);
        return delegate;
    }

    /**
     * 解析<beans> 标签下的bean定义
     *      "import", "alias", "bean".
     *
     * @param root xml根标签
     * @param delegate the DOM root element of the document
     */
    protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
        if (delegate.isDefaultNamespace(root)) {
            // 获取子节点
            NodeList nl = root.getChildNodes();
            // 遍历循环解析bean
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node instanceof Element) {
                    Element ele = (Element) node;
                    if (delegate.isDefaultNamespace(ele)) {
                        // 处理默认命名空间的元素
                        parseDefaultElement(ele, delegate);
                    } else {
                        // 自定义命名空间
                        delegate.parseCustomElement(ele);
                    }
                }
            }
        } else {
            // 自定义命名空间
            delegate.parseCustomElement(root);
        }
    }

    private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
        if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) { // import标签
            importBeanDefinitionResource(ele);
        } else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) { // alias标签
            processAliasRegistration(ele);
        } else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) { // bean标签
            // 处理bean的定义
            processBeanDefinition(ele, delegate);
        } else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) { // beans标签
            // recurse
            doRegisterBeanDefinitions(ele);
        }
    }

    /**
     * Parse an "import" element and load the bean definitions
     * from the given resource into the bean factory.
     */
    protected void importBeanDefinitionResource(Element ele) {
        String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
        if (!StringUtils.hasText(location)) {
            getReaderContext().error("Resource location must not be empty", ele);
            return;
        }

        // Resolve system properties: e.g. "${user.dir}"
        location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);

        Set<Resource> actualResources = new LinkedHashSet<>(4);

        // Discover whether the location is an absolute or relative URI
        boolean absoluteLocation = false;
        try {
            absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
        } catch (URISyntaxException ex) {
            // cannot convert to an URI, considering the location relative
            // unless it is the well-known Spring prefix "classpath*:"
        }

        // Absolute or relative?
        if (absoluteLocation) {
            try {
                int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
                if (logger.isTraceEnabled()) {
                    logger.trace("Imported " + importCount + " bean definitions from URL location [" + location + "]");
                }
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error(
                        "Failed to import bean definitions from URL location [" + location + "]", ele, ex);
            }
        } else {
            // No URL -> considering resource location as relative to the current file.
            try {
                int importCount;
                Resource relativeResource = getReaderContext().getResource().createRelative(location);
                if (relativeResource.exists()) {
                    importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
                    actualResources.add(relativeResource);
                } else {
                    String baseLocation = getReaderContext().getResource().getURL().toString();
                    importCount = getReaderContext().getReader().loadBeanDefinitions(
                            StringUtils.applyRelativePath(baseLocation, location), actualResources);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Imported " + importCount + " bean definitions from relative location [" + location + "]");
                }
            } catch (IOException ex) {
                getReaderContext().error("Failed to resolve current resource location", ele, ex);
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error(
                        "Failed to import bean definitions from relative location [" + location + "]", ele, ex);
            }
        }
        Resource[] actResArray = actualResources.toArray(new Resource[0]);
        getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
    }

    /**
     * Process the given alias element, registering the alias with the registry.
     */
    protected void processAliasRegistration(Element ele) {
        String name = ele.getAttribute(NAME_ATTRIBUTE);
        String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
        boolean valid = true;
        if (!StringUtils.hasText(name)) {
            getReaderContext().error("Name must not be empty", ele);
            valid = false;
        }
        if (!StringUtils.hasText(alias)) {
            getReaderContext().error("Alias must not be empty", ele);
            valid = false;
        }
        if (valid) {
            try {
                getReaderContext().getRegistry().registerAlias(name, alias);
            } catch (Exception ex) {
                getReaderContext().error("Failed to register alias '" + alias +
                        "' for bean with name '" + name + "'", ele, ex);
            }
            getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
        }
    }

    /**
     * 解析指定bean元素，并注册进注册表
     * @param ele xml二级子标签元素
     * @param delegate 当前bean的委托器
     */
    protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
        // 用bean委托器解析bean定义的元素
        BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
        if (bdHolder != null) {
            bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
            try {
                // Register the final decorated instance.
                // 将bean 信息放到待注册区域
                BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error("Failed to register bean definition with name '" +
                        bdHolder.getBeanName() + "'", ele, ex);
            }
            // 发送注册事件 EmptyReaderEventListener，
            getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
        }
    }


    /**
     * Allow the XML to be extensible by processing any custom element types first,
     * before we start to process the bean definitions. This method is a natural
     * extension point for any other custom pre-processing of the XML.
     * <p>The default implementation is empty. Subclasses can override this method to
     * convert custom elements into standard Spring bean definitions, for example.
     * Implementors have access to the parser's bean definition reader and the
     * underlying XML resource, through the corresponding accessors.
     *
     * @see #getReaderContext()
     */
    protected void preProcessXml(Element root) {
    }

    /**
     * Allow the XML to be extensible by processing any custom element types last,
     * after we finished processing the bean definitions. This method is a natural
     * extension point for any other custom post-processing of the XML.
     * <p>The default implementation is empty. Subclasses can override this method to
     * convert custom elements into standard Spring bean definitions, for example.
     * Implementors have access to the parser's bean definition reader and the
     * underlying XML resource, through the corresponding accessors.
     *
     * @see #getReaderContext()
     */
    protected void postProcessXml(Element root) {
    }

}
