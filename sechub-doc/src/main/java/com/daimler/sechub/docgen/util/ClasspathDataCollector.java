// SPDX-License-Identifier: MIT

package com.daimler.sechub.docgen.util;

import java.util.List;

import com.daimler.sechub.docgen.DocAnnotationData;
import com.daimler.sechub.docgen.MustBeDocumentedDataCollector;
import com.daimler.sechub.docgen.PDSMustBeDocumentedDataCollector;
import com.daimler.sechub.docgen.messaging.DomainMessagingModel;
import com.daimler.sechub.docgen.messaging.DomainMessagingModelDataCollector;
import com.daimler.sechub.docgen.reflections.Reflections;
import com.daimler.sechub.docgen.spring.MockedAdaptersSpringValueDataCollector;
import com.daimler.sechub.docgen.usecase.PDSUseCaseModelDataCollector;
import com.daimler.sechub.docgen.usecase.UseCaseModel;
import com.daimler.sechub.docgen.usecase.UseCaseModelDataCollector;
import com.daimler.sechub.docgen.usecase.UseCaseRestDocModel;
import com.daimler.sechub.docgen.usecase.UseCaseRestDocModelDataCollector;

/**
 * Collector for information àbout used annoations ins projects.<br>
 * Inspired by https://github.com/de-jcup/code2doc/blob/master/code2doc-core/src/main/java/de/jcup/code2doc/core/internal/collect/ClasspathDataCollector.java
 * 
 * @author Albert Tregnaghi
 *
 */
public class ClasspathDataCollector {

	private Reflections reflections;
	private MustBeDocumentedDataCollector mustbeDocumentedDataCollector;
	private MockedAdaptersSpringValueDataCollector mockedAdaptersSprintValueDataCollector;
	private DomainMessagingModelDataCollector domainMessagingModelDataCollector;
	private UseCaseModelDataCollector useCaseModelDataCollector;
	private PDSUseCaseModelDataCollector pdsUseCaseModelDataCollector;
	private UseCaseRestDocModelDataCollector useCaseRestDocModelDataCollector;
    private PDSMustBeDocumentedDataCollector pdsMustbeDocumentedDataCollector;
	
	public ClasspathDataCollector(){
		/* @formatter:off*/
		reflections = ReflectionsFactory.create();
		pdsMustbeDocumentedDataCollector = new PDSMustBeDocumentedDataCollector(reflections);
		mustbeDocumentedDataCollector = new MustBeDocumentedDataCollector(reflections);
		mockedAdaptersSprintValueDataCollector = new MockedAdaptersSpringValueDataCollector(reflections);
		domainMessagingModelDataCollector= new DomainMessagingModelDataCollector(reflections);
		useCaseModelDataCollector = new UseCaseModelDataCollector(reflections);
		useCaseRestDocModelDataCollector = new UseCaseRestDocModelDataCollector(reflections);
		pdsUseCaseModelDataCollector = new PDSUseCaseModelDataCollector(reflections);
	}
	
	public List<DocAnnotationData> fetchMustBeDocumentParts(){
		return mustbeDocumentedDataCollector.collect();
	}
	
	public List<DocAnnotationData> fetchPDSMustBeDocumentParts(){
        return pdsMustbeDocumentedDataCollector.collect();
    }
	
	public List<DocAnnotationData> fetchMockAdapterSpringValueDocumentationParts(){
		return mockedAdaptersSprintValueDataCollector.collect();
	}

	public DomainMessagingModel fetchDomainMessagingModel() {
		return domainMessagingModelDataCollector.collect();
	}

	public UseCaseModel fetchUseCaseModel() {
		return useCaseModelDataCollector.collect();
	}
	
	public UseCaseModel fetchPDSUseCaseModel() {
        return pdsUseCaseModelDataCollector.collect();
    }

	public UseCaseRestDocModel fetchUseCaseRestDocModel(UseCaseModel model) {
		return useCaseRestDocModelDataCollector.collect(model);
	}
	
}