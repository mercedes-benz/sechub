// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.usecase;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.daimler.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;
import com.daimler.sechub.docgen.usecase.UseCaseModel.UseCaseEntry.UseCaseEntryStep;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.UseCaseGroup;

public class UseCaseModelAsciiDocGenerator {

	// [options="header",cols="1,1,1"]
	// |===
	// |HeadA |HeadB |HeadC
	// //----------------------
	// |Row1A |Row1B |Row1C
	// |Row2A |Row2B |Row2Cs
	// |Row3A |Row3B |Row3C
	// |===

	public String generateAsciidoc(UseCaseModel model) {
		Context context = new Context();
		int h = 3;
		context.addLine("[[section-usecases]]");
		context.addLine(headline(h++) + "Use cases");
		context.addLine("");
		context.addLine("ifdef::techdoc[]");
		context.addLine(
				"TIP: The complete documentation about use cases is generated. If you want to change content, please search for ");
		context.addLine("     `@UseCaseDefinition` references in source code and make necessary changes inside code!");
		context.addLine("endif::techdoc[]");
		context.addLine("");
		generateOverview(model, context, h);
		generateDetails(model, context, h);

		return context.getAsciiDoc();
	}

	private void generateDetails(UseCaseModel model, Context context, int h) {
		for (UseCaseEntry entry : model.getUseCases()) {
			generateUseCase(context, h, entry);
		}
	}

	private void generateOverview(UseCaseModel model, Context context, int h) {
		context.addLine(headline(h) + "Overview about usecase groups");
		for (UseCaseGroup group : UseCaseGroup.values()) {
			context.addLine(headline(h + 1) + group.getTitle());
			context.addLine(group.getDescription());
			context.addLine("");
			SortedSet<UseCaseEntry> entries = model.getUseCasesInsideGroup(group);
			for (UseCaseEntry entry : entries) {
				context.addLine("- <<" + UseCaseAsciiDocFactory.createLinkId(entry) + "," + entry.getId() + "-"
						+ entry.getTitle() + ">>\n");
			}
			context.addLine("");
		}
	}

	private void generateUseCase(Context context, int h, UseCaseEntry entry) {
		context.addLine(UseCaseAsciiDocFactory.createAnker(entry));
		context.addLine(headline(h) + entry.getId() + "-" + entry.getTitle());
		context.addLine(entry.getDescription());
		context.addLine("");

		context.addLine("ifdef::techdoc[]");
		context.addLine("*Technical information*");
		context.addLine("");
		context.addLine("You will find relevant code parts by searching for references of `@"
				+ entry.getAnnotationName() + "`");
		context.addLine("");
		context.addLine("endif::techdoc[]");

		context.addLine("*Steps*");
		generateUseCaseStepsTable(context, entry);
		generateLinkToRestAPIDoc(context, entry);
	}

	private void generateLinkToRestAPIDoc(Context context, UseCaseEntry entry) {
		if (entry.getRestDocEntries().isEmpty()) {
			return;
		}
		UseCaseRestDocEntry docEntry = entry.getRestDocEntries().iterator().next();
		context.addLine("ifdef::techdoc[]");
		context.addLine(UseCaseAsciiDocFactory.createLinkToUseCaseRestDoc(docEntry) + "\n");
		context.addLine("endif::techdoc[]");
	}

	private void generateUseCaseStepsTable(Context context, UseCaseEntry entry) {
		context.addLine("[options=\"header\",cols=\"5,20,20,5,50\"]");
		context.addLine("|===");
		context.addLine("|Nr |Title |Role(s)|Next|Description");
		context.addLine("//----------------------");
		generateUseCaseStepsTableEntries(context, entry);
		context.addLine("|===");
		context.addLine("");
	}

	private void generateUseCaseStepsTableEntries(Context context, UseCaseEntry entry) {
		List<UseCaseEntryStep> steps = entry.getSteps();
		Collections.sort(steps);
		Collections.reverse(steps);

		StringBuilder rows = new StringBuilder();
		int lastStep = 0;
		for (UseCaseEntryStep step : steps) {
			StringBuilder sb = new StringBuilder();

			sb.append("|");
			sb.append(step.getNumber()); /* nr */
			sb.append("|");
			sb.append(step.getTitle()); /* title */
			sb.append("|");
			sb.append(createRolesString(step)); /* roles */
			sb.append("|");
			sb.append(createNextStep(lastStep, step)); /* next */
			sb.append("|");
			sb.append(step.getDescription()); /* description */
			sb.append("\n");
			sb.append("\n");
			sb.append("ifdef::techdoc[]\n");
			sb.append("__This step is defined at " + step.getLocation() + "__");
			sb.append("\n");
			sb.append("endif::techdoc[]");
			sb.append("\n");
			String row = sb.toString();
			rows.insert(0, row);

			lastStep = step.getNumber();
		}
		context.sb.append(rows);
	}

	private String createNextStep(int lastStepNumber, UseCaseEntryStep step) {
		StringBuilder visibleNext = new StringBuilder();
		int[] next = step.getNext();
		if (next.length > 0) {
			int counter = 0;
			for (int n : next) {
				counter++;
				if (n == Step.NO_NEXT_STEP) {
					// means no further steps! (as described in @Step annotation )
					continue;
				}
				visibleNext.append(n);
				if (counter < next.length) {
					visibleNext.append(", ");
				}
			}
		} else {
			if (lastStepNumber > 0) {
				visibleNext.append(lastStepNumber);
			}
		}
		return visibleNext.toString();
	}

	private String createRolesString(UseCaseEntryStep step) {
		StringBuilder sbx = new StringBuilder();
		Set<String> rolesAllowed = step.getRolesAllowed();
		if (!rolesAllowed.isEmpty()) {
			for (Iterator<String> ri = rolesAllowed.iterator(); ri.hasNext();) {
				String role = ri.next();
				sbx.append(role);
				if (ri.hasNext()) {
					sbx.append(", ");
				}
			}
		}
		return sbx.toString();
	}

	private String headline(int nr) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nr; i++) {
			sb.append("=");
		}
		return sb.toString() + " ";
	}

	private class Context {

		StringBuilder sb = new StringBuilder();

		void addLine(String text) {
			sb.append(text).append('\n');
		}

		public String getAsciiDoc() {
			return sb.toString();
		}
	}

}
