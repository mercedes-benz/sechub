// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.api.internal.gen.model.CodeExample;
import com.mercedesbenz.sechub.api.internal.gen.model.Reference;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.api.internal.gen.model.TextBlock;

@Component
public class FallbackExplanationReponseFactory {

    public SecHubExplanationResponse createExplanationResponse(SecHubExplanationInput input) {

        SecHubExplanationResponse defaultResponse = new SecHubExplanationResponse();

        // Create and set FindingExplanation
        TextBlock findingExplanation = new TextBlock();
        String findingName = input.getFindingName();
        if (findingName == null || findingName.isBlank()) {
            findingName = "Unknown finding";
        }
        findingExplanation.setTitle(findingName);
        String findingDescription = input.getFindingDescription();
        if (findingDescription == null || findingDescription.isBlank()) {
            findingDescription = "No description available";
            if (input.getCweId() != null) {
                findingDescription += " for CWE-" + input.getCweId();
            }
        }
        findingExplanation.setContent(findingDescription);
        defaultResponse.setFindingExplanation(findingExplanation);

        // Create and set PotentialImpact
        TextBlock potentialImpact = new TextBlock();
        potentialImpact.setTitle("Potential Impact");
        potentialImpact.setContent("No explicit information available. Please look at references");
        defaultResponse.setPotentialImpact(potentialImpact);

        // Create and set Recommendations
        List<TextBlock> recommendations = new ArrayList<>();

        defaultResponse.setRecommendations(recommendations);

        // Create and set CodeExample
        CodeExample codeExample = new CodeExample();
        defaultResponse.setCodeExample(codeExample);

        // Create and set References
        List<Reference> references = new ArrayList<>();

        Integer cweId = input.getCweId();

        if (cweId != null) {
            String refTitle = "CWE-" + cweId + " - " + findingName;
            Reference reference2 = new Reference();
            reference2.setTitle(refTitle);
            reference2.setUrl("https://cwe.mitre.org/data/definitions/" + cweId + ".html");
            references.add(reference2);
        }

        defaultResponse.setReferences(references);

        return defaultResponse;
    }

}
