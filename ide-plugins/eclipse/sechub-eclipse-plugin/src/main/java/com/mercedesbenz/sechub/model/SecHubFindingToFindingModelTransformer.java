// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubFinding;
import com.mercedesbenz.sechub.api.internal.gen.model.Severity;
import com.mercedesbenz.sechub.model.FindingNode.FindingNodeBuilder;

public class SecHubFindingToFindingModelTransformer {

	private static final Logger logger = LoggerFactory.getLogger(SecHubFindingToFindingModelTransformer.class);
	
	private static final String EMPTY = "";

	public FindingModel transform(List<SecHubFinding> findings) {
		/* build severity mapping */
		Map<Severity, List<FindingNode>> map = new LinkedHashMap<>();
		for (SecHubFinding finding : findings) {
			addNodesToMapForFinding(map, finding);

		}
		return createRootNodeWithChildren(map);
	}

	private void addNodesToMapForFinding(Map<Severity, List<FindingNode>> map, SecHubFinding finding) {
		if (finding==null) {
			return;
		}
		Severity severity = finding.getSeverity();

		List<FindingNode> list = map.computeIfAbsent(severity,
				SecHubFindingToFindingModelTransformer::createFindingNodeList);

		Integer id = finding.getId();
		if (id==null) {
			logger.warn("Finding id is null!");
			return;
		}
		int callStackStep = 1;
		String description = finding.getName();
		Integer cweId = finding.getCweId();

		SecHubCodeCallStack code = finding.getCode();
		if (code == null) {
			
			code = new SecHubCodeCallStack(); // fallback when no code call stack available
			
			code.setColumn(0); // must do this, otherwise NPE by auto boxing null values
			code.setLine(0);
		}

		/* @formatter:off */
		FindingNodeBuilder builder = FindingNode.builder().
				setId(id).
				setCweId(cweId).
				setCallStackStep(callStackStep++).
				setColumn(code.getColumn()).
				setLine(code.getLine()).
				setLocation(code.getLocation()).
				setSeverity(severity).
				setRelevantPart(code.getRelevantPart()).
				setSource(code.getSource()).
				setDescription(description);

		/* @formatter:on */
		FindingNode child = builder.build();
		list.add(child);

		FindingNode parent = child;
		while (code.getCalls() != null) {
			code = code.getCalls();
			/* @formatter:off */
			child = builder.
					setCallStackStep(callStackStep++).
					setColumn(code.getColumn()).
					setLine(code.getLine()).
					setLocation(code.getLocation()).
					setSource(code.getSource()).
					setRelevantPart(code.getRelevantPart()).
					setDescription(EMPTY).
					setSeverity(null).
				build();
			/* @formatter:on */
			parent.addChild(child);
			parent = child; // for next call this is the parent
		}
	}

	private FindingModel createRootNodeWithChildren(Map<Severity, List<FindingNode>> map) {
		FindingModel model = new FindingModel();

		/* Add first level nodes, to model - sorted by severity */
		List<Severity> severitySortedCriticalFirst = Arrays.asList(Severity.values());
		Collections.reverse(severitySortedCriticalFirst);
		for (Severity severity : severitySortedCriticalFirst) {
			List<FindingNode> listOrNull = map.get(severity);
			if (listOrNull == null) {
				continue;
			}
			for (FindingNode node : listOrNull) {
				model.getFindings().add(node);
			}
		}

		return model;
	}

	private static List<FindingNode> createFindingNodeList(Severity severity) {
		return new ArrayList<>();
	}
}
