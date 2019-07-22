// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.usecase;

import com.daimler.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;

public class UseCaseAsciiDocFactory {

	private UseCaseAsciiDocFactory() {
		
	}
	
	public static String createLinkId(UseCaseEntry entry) {
		return "section-usecase-"+entry.getId();
	}
	
	public static String createLinkId(UseCaseRestDocEntry entry) {
		return "section-ucrestdoc-" + entry.usecaseEntry.getId() + "-" + entry.variant;
	}

	public static String createLinkIdToUseCaseRestDoc(UseCaseEntry entry) {
		return "section-ucrestdoc-uc-" + entry.getId();
	}

	public static String createAnker(UseCaseEntry entry) {
		return "[["+createLinkId(entry)+"]]";
	}

	public static String createAnkerUseCaseRestDoc(UseCaseEntry useCase) {
		return "[[" + createLinkIdToUseCaseRestDoc(useCase) + "]]";
	}

	public static String createAnker(UseCaseRestDocEntry entry) {
		return "[[" + createLinkId(entry) + "]]";
	}

	public static String createLinkToUseCase(UseCaseEntry entry) {
		return "<<" + createLinkId(entry)+"," + entry.getId()+"-"+entry.getTitle()+" >>";
	}
	
	public static String createLinkToUseCase(UseCaseRestDocEntry entry) {
		return createLinkToUseCase(entry.usecaseEntry);
	}

	public static String createLinkToUseCaseRestDoc(UseCaseRestDocEntry docEntry) {
		UseCaseEntry entry = docEntry.usecaseEntry;
		return createLinkToUseCaseRestDoc(entry,"REST API documentation for usecase "+ entry.getId()+"-"+entry.getTitle());
	}
	public static String createLinkToUseCaseRestDoc(UseCaseEntry entry, String label) {
		return "<<"+createLinkIdToUseCaseRestDoc(entry)+","+label+" >>";
	}

}
