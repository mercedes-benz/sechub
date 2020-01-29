// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.usecase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

import com.daimler.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;
import com.daimler.sechub.docgen.util.TextFileWriter;
import com.daimler.sechub.sharedkernel.usecases.UseCaseGroup;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc.SpringRestDocOutput;

public class UseCaseRestDocModelAsciiDocGenerator {

	private TextFileWriter writer;

	// [options="header",cols="1,1,1"]
	// |===
	// |HeadA |HeadB |HeadC
	// //----------------------
	// |Row1A |Row1B |Row1C
	// |Row2A |Row2B |Row2Cs
	// |Row3A |Row3B |Row3C
	// |===
	public String generateAsciidoc(TextFileWriter writer, UseCaseRestDocModel model, boolean technical, UseCaseIdentifier ... usecases) {
		Objects.requireNonNull(writer);
		Objects.requireNonNull(model);

		this.writer=writer;
		Context context = new Context();
		for (UseCaseIdentifier identifier: usecases) {
			context.usecasesToInspect.add(identifier.name());
		}

		int h = 3;
		context.addLine("[[section-ucrestdoc]]");
		context.addLine(headline(h++) + "Rest API documentation");
		context.addLine("");
		context.addLine("ifdef::techdoc[]");
		context.addLine("TIP: The complete documentation about REST API is generated. If you want to change content, please search for ");
		context.addLine("     `@UseCaseRestDoc` references in source code and make necessary changes inside code!");
		context.addLine("");
		context.addLine("endif::techdoc[]");
		List<List<UseCaseRestDocEntry>> found = generateOverview(model, context, h);
		generateUseCaseAndRestDocDetails(context, h, found);
		return context.getAsciiDoc();
	}

	private void generateUseCaseAndRestDocDetails(Context context, int h, List<List<UseCaseRestDocEntry>> found) {
		Set<String> avoidDoubleGeneration = new LinkedHashSet<>();
		for (List<UseCaseRestDocEntry> entries :found) {

			UseCaseRestDocEntry first = entries.iterator().next();
			if (avoidDoubleGeneration.contains(first.identifier)) {
				continue;
			}
			avoidDoubleGeneration.add(first.identifier);
			context.addLine(headline(h)+first.usecaseEntry.getTitle());
			context.addLine(UseCaseAsciiDocFactory.createAnkerUseCaseRestDoc(first.usecaseEntry));

			context.addLine("REST API for usecase "+UseCaseAsciiDocFactory.createLinkToUseCase(first));

			for (UseCaseRestDocEntry entry: entries) {
				boolean multipleVariants = entries.size()>1;
				generateDetail(context, h, entry,multipleVariants);
			}
		}
	}

	private List<List<UseCaseRestDocEntry>> generateOverview(UseCaseRestDocModel model, Context context, int h) {
		context.addLine(headline(h) + "Overview");
		List<List<UseCaseRestDocEntry>> found = new ArrayList<>();
		for (UseCaseGroup group : UseCaseGroup.values()) {
			SortedSet<UseCaseEntry> entriesForGroup = model.getUseCaseModel().getUseCasesInsideGroup(group);
			if (entriesForGroup.isEmpty()) {
				continue;
			}
			StringBuilder linksToRestDocs = new StringBuilder();
			createEntriesForGroup(model, context, found, entriesForGroup, linksToRestDocs);
			boolean foundAtLeastOneRestDocForGroup=linksToRestDocs.length()>0;
			if (!foundAtLeastOneRestDocForGroup) {
				continue;
			}
			context.addLine(headline(h + 1) + group.getTitle());
			context.addLine(group.getDescription());
			context.addLine("");
			context.addLine(linksToRestDocs.toString());
			context.addLine("");
		}
		return found;
	}

	private void createEntriesForGroup(UseCaseRestDocModel model, Context context,
			List<List<UseCaseRestDocEntry>> found, SortedSet<UseCaseEntry> entriesForGroup,
			StringBuilder linksToRestDocs) {
		for (UseCaseEntry entry : entriesForGroup) {
			if (! context.usecasesToInspect.contains(entry.getIdentifierEnumName())) {
				continue;
			}
			entry.getIdentifierEnumName();
			List<UseCaseRestDocEntry> restDocEntries = model.getRestDocEntries(entry);
			if (restDocEntries == null || restDocEntries.isEmpty()) {
				continue; /* use case has no rest doc so ignore*/
			}
			linksToRestDocs.append("- " + UseCaseAsciiDocFactory.createLinkToUseCaseRestDoc(entry, "REST API for "+entry.getId()+"-"+entry.getTitle())+"\n");
			found.add(restDocEntries);
		}
	}

	private void generateDetail(Context context, int h, UseCaseRestDocEntry entry, boolean multipleVariants) {
		context.addLine("");
		context.addLine(UseCaseAsciiDocFactory.createAnker(entry));
		if (multipleVariants) {
			if (entry.variantOriginValue.equals(UseCaseRestDoc.DEFAULT_VARIANT)){
				context.addLine(headline(h+1)+ "Standard");
			}else {
				context.addLine(headline(h+1)+ entry.variantOriginValue);
			}

		}

		File[] files = entry.copiedRestDocFolder.listFiles();
		addHeadersWhenNecessary(files);

		File[] wantedFilesOrdered = orderWantedFiles(files,entry.wanted);


		for (File file: wantedFilesOrdered) {
			context.addLine("");
			String prettyFileName = createPrettyPrintedFileName(file);
			context.addLine("*"+prettyFileName+"*\n");
			context.addLine("include::"+entry.path+"/"+file.getName()+"[]");
		}

	}

	private void addHeadersWhenNecessary(File[] files) {
		/* headers are automatically added by writer:*/
		for (File file: files) {
			try {
				writer.addMissingHeaders(file);
			} catch (IOException e) {
				throw new IllegalStateException("Cannot add missing headers", e);
			}
		}
	}

	private File[] orderWantedFiles(File[] files, SpringRestDocOutput[] wanted) {
		List<File> orderedWanted = new ArrayList<>();
		for (SpringRestDocOutput want: wanted) {
			for (File file:files) {
				if (want.isWanted(file)){
					orderedWanted.add(file);
				}
			}
		}
		return orderedWanted.toArray(new File[orderedWanted.size()]);
	}

	private String createPrettyPrintedFileName(File file) {
		String prettyFileName = file.getName();
		prettyFileName=prettyFileName.substring(0, prettyFileName.length()-5);//remove ".adoc"  e.g. curl-request.adoc ->curl-request
		prettyFileName=prettyFileName.replaceAll("-", " "); // e.g.curl-request -> curl request
		char c = prettyFileName.charAt(0);
		prettyFileName= Character.toUpperCase(c)+prettyFileName.substring(1); // E.g. curl request -> Curl request
		return prettyFileName;
	}

	private String headline(int nr) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");// always add a new line - to prevent layout problems
		for (int i = 0; i < nr; i++) {
			sb.append("=");
		}
		return sb.toString() + " ";
	}

	private class Context {

		public Set<String> usecasesToInspect = new LinkedHashSet<>();
		StringBuilder sb = new StringBuilder();

		void addLine(String text) {
			sb.append(text).append('\n');
		}


		public String getAsciiDoc() {
			return sb.toString();
		}
	}

}
