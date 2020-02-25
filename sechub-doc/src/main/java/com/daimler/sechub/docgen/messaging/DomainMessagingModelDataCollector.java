// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.messaging;

import static com.daimler.sechub.docgen.GeneratorConstants.*;
import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.docgen.messaging.DomainMessagingModel.Domain;
import com.daimler.sechub.docgen.messaging.DomainMessagingModel.DomainPart;
import com.daimler.sechub.docgen.reflections.Reflections;
import com.daimler.sechub.sharedkernel.messaging.DomainMessaging;

/**
 * Collector - inspired by
 * https://github.com/de-jcup/code2doc/blob/master/code2doc-core/src/main/java/de/jcup/code2doc/core/internal/collect/TechInfoLinkAnnotationDataCollector.java
 *
 * @author Albert Tregnaghi
 */
public class DomainMessagingModelDataCollector {

	private static final String COM_DAIMLER_SECHUB_DOMAIN = "com.daimler.sechub.domain";

	private static final Logger LOG = LoggerFactory.getLogger(DomainMessagingModelDataCollector.class);

	private Reflections reflections;

	public DomainMessagingModelDataCollector(Reflections reflections) {
		notNull(reflections, "reflections must not be null!");
		this.reflections = reflections;
	}

	@SuppressWarnings("unchecked")
	public DomainMessagingModel collect() {
		if (DEBUG) {
			LOG.info("start collecting");
		}
		DomainMessagingModel model = new DomainMessagingModel();
		Set<Class<?>> toCollect = reflections.getTypesAnnotatedWith(DomainMessaging.class);
		if (DEBUG) {
			LOG.info("> will collect for:{} - {}", toCollect.size(),toCollect);
		}
		for (Class<?> clazz : toCollect) {
			collectAnnotationInfo(model, (Class<Annotation>) clazz);
		}

		return model;
	}

	private <T extends Annotation> void collectAnnotationInfo(DomainMessagingModel model, Class<T> annoClass) {
		if (DEBUG) {
			LOG.info("start collecting annotation info:{}", annoClass);
		}
		Set<Method> methodsAnnotated = reflections.getMethodsAnnotatedWith(annoClass);
		if (DEBUG) {
			LOG.info("found methods annotated with:{} - {}", annoClass,methodsAnnotated);
		}
		for (Method method : methodsAnnotated) {
			T[] annosFound = method.getAnnotationsByType(annoClass);
			Domain domain = model.ensureDomain(createDomainName(method));
			DomainPart domainPart = domain.ensureDomainPart(createDomainPartName(method));
			for (T message : annosFound) {
				domainPart.addMessageInfo(message);
			}
		}
	}

	static String createDomainPartName(Method method) {
		return method.getDeclaringClass().getSimpleName();
	}

	/* something like com.daimler.sechub.domain.xyz.abc is represented as xyz only*/
	static String createDomainName(Method method) {
		String fullName = method.getDeclaringClass().getPackage().getName();
		String name = fullName;
		int index = name.indexOf(COM_DAIMLER_SECHUB_DOMAIN);
		int amountOfcharsToSkip = COM_DAIMLER_SECHUB_DOMAIN.length()+1;
		if (index!=-1 && name.length()>amountOfcharsToSkip) {
			name = name.substring(amountOfcharsToSkip);
		}
		index = name.indexOf('.');
		if (index!=-1) {
			name=name.substring(0, index);
		}
		if (name.trim().isEmpty()) {
			name=fullName;
		}
		return name;
	}

}