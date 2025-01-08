// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import java.lang.annotation.Annotation;

import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.snippet.Snippet;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetDetails;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;

public class RestDocumentationTest {

    private RestDocumentationDetails details;

    public static RestDocumentationTest defineRestService() {
        return new RestDocumentationTest();
    }

    private RestDocumentationTest() {
        this.details = new RestDocumentationDetails();
    }

    public RestDocumentationDetails with() {
        return details;
    }

    public RestDocumentationResultHandler document(Snippet... snippets) {
        /* assert */

        /* create details */
        ResourceSnippetDetails resourceSnippetDetails = ResourceSnippetParameters.builder().summary(details.summary).description(details.description)
                .requestSchema(details.requestSchema).responseSchema(details.responseSchema).tag(details.tag);

        /* use wrapper from com.epages in correct way */
        return MockMvcRestDocumentationWrapper.document(details.id, resourceSnippetDetails, snippets);
    }

    public class RestDocumentationDetails {
        private String tag;
        private String description;
        private String id;
        private String summary;
        private Schema requestSchema;
        private Schema responseSchema;

        private RestDocumentationDetails() {

        }

        /**
         * Will setup documentation details (identifier, summary, description)
         * automatically for given use case.
         *
         * @param useCase
         * @param string
         * @return documentation details
         */
        public RestDocumentationDetails useCaseData(Class<? extends Annotation> useCase) {
            return useCaseData(useCase, null);
        }

        /**
         * Will setup documentation details (identifier, summary, description)
         * automatically for given use case.
         *
         * @param useCase
         * @param string
         * @return documentation details
         */
        public RestDocumentationDetails useCaseData(Class<? extends Annotation> useCase, String variant) {
            identifier(RestDocFactory.createPath(useCase, variant));
            summary(RestDocFactory.createSummary(useCase));
            description(RestDocFactory.createDescription(useCase));

            return this;
        }

        public RestDocumentationDetails identifier(String id) {
            this.id = id;
            return this;
        }

        public RestDocumentationDetails summary(String summary) {
            this.summary = summary;
            return this;
        }

        public RestDocumentationDetails tag(String tag) {
            this.tag = tag;
            return this;
        }

        public RestDocumentationDetails description(String description) {
            this.description = description;
            return this;
        }

        public RestDocumentationDetails requestSchema(Schema schema) {
            this.requestSchema = schema;
            return this;
        }

        public RestDocumentationDetails responseSchema(Schema schema) {
            this.responseSchema = schema;
            return this;
        }

        public RestDocumentationTest and() {
            return RestDocumentationTest.this;
        }

    }

}
