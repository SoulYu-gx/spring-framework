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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import org.springframework.lang.Nullable;
import org.springframework.util.xml.XmlValidationModeDetector;

/**
 * Spring's default {@link DocumentLoader} implementation.
 *
 * <p>Simply loads {@link Document documents} using the standard JAXP-configured
 * XML parser. If you want to change the {@link DocumentBuilder} that is used to
 * load documents, then one strategy is to define a corresponding Java system property
 * when starting your JVM. For example, to use the Oracle {@link DocumentBuilder},
 * you might start your application like as follows:
 *
 * <pre code="class">java -Djavax.xml.parsers.DocumentBuilderFactory=oracle.xml.jaxp.JXDocumentBuilderFactory MyMainClass</pre>
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class DefaultDocumentLoader implements DocumentLoader {

    /**
     * JAXP attribute used to configure the schema language for validation.
     */
    private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /**
     * JAXP attribute value indicating the XSD schema language.
     */
    private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";


    private static final Log logger = LogFactory.getLog(DefaultDocumentLoader.class);


    /**
     * 从输入源中加载document
     *
     * @param inputSource    输入源
     * @param entityResolver 试题解析器
     * @param errorHandler
     * @param validationMode 校验模式
     *                       {@link org.springframework.util.xml.XmlValidationModeDetector#VALIDATION_DTD DTD}
     *                       or {@link org.springframework.util.xml.XmlValidationModeDetector#VALIDATION_XSD XSD})
     * @param namespaceAware 命名空间
     * @return the loaded {@link Document document}
     * @throws Exception if an error occurs
     */
    @Override
    public Document loadDocument(InputSource inputSource, EntityResolver entityResolver,
                                 ErrorHandler errorHandler, int validationMode, boolean namespaceAware) throws Exception {

        DocumentBuilderFactory factory = createDocumentBuilderFactory(validationMode, namespaceAware);
        if (logger.isTraceEnabled()) {
            logger.trace("Using JAXP provider [" + factory.getClass().getName() + "]");
        }
        DocumentBuilder builder = createDocumentBuilder(factory, entityResolver, errorHandler);
        return builder.parse(inputSource);
    }

    /**
     * 创建文档生成器工厂
     *
     * @param validationMode xml验证模式
     *                       {@link XmlValidationModeDetector#VALIDATION_DTD DTD}
     *                       or {@link XmlValidationModeDetector#VALIDATION_XSD XSD})
     * @param namespaceAware 是否开启命名空间
     * @return 生成器工厂
     * @throws ParserConfigurationException if we failed to build a proper DocumentBuilderFactory
     */
    protected DocumentBuilderFactory createDocumentBuilderFactory(int validationMode, boolean namespaceAware)
            throws ParserConfigurationException {

        // 实例化文档生成器工厂
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaceAware);

        if (validationMode != XmlValidationModeDetector.VALIDATION_NONE) {
            factory.setValidating(true);
            if (validationMode == XmlValidationModeDetector.VALIDATION_XSD) {
                // 如果验证方式xsd模式，强行开启命名空间感知
                factory.setNamespaceAware(true);
                try {
                    factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
                } catch (IllegalArgumentException ex) {
                    ParserConfigurationException pcex = new ParserConfigurationException(
                            "Unable to validate using XSD: Your JAXP provider [" + factory +
                                    "] does not support XML Schema. Are you running on Java 1.4 with Apache Crimson? " +
                                    "Upgrade to Apache Xerces (or Java 1.5) for full XSD support.");
                    pcex.initCause(ex);
                    throw pcex;
                }
            }
        }

        return factory;
    }

    /**
     * 通过工厂创建一个文档加载对象
     *
     * @param factory        文档加载器工厂
     * @param entityResolver 试题解析器
     * @param errorHandler
     * @return the JAXP DocumentBuilder
     * @throws ParserConfigurationException if thrown by JAXP methods
     */
    protected DocumentBuilder createDocumentBuilder(DocumentBuilderFactory factory,
                                                    @Nullable EntityResolver entityResolver, @Nullable ErrorHandler errorHandler)
            throws ParserConfigurationException {

        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        if (entityResolver != null) {
            docBuilder.setEntityResolver(entityResolver);
        }
        if (errorHandler != null) {
            docBuilder.setErrorHandler(errorHandler);
        }
        return docBuilder;
    }

}
