// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.api.internal.gen.model.CodeExample.*;
import static com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse.*;
import static com.mercedesbenz.sechub.api.internal.gen.model.TextBlock.*;
import static com.mercedesbenz.sechub.restdoc.RestDocumentationTest.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.api.internal.gen.model.CodeExample;
import com.mercedesbenz.sechub.api.internal.gen.model.Reference;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.api.internal.gen.model.TextBlock;
import com.mercedesbenz.sechub.assistant.AssistantRestController;
import com.mercedesbenz.sechub.assistant.FindingAssistantService;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.assistant.UseCaseUserRequestFindingExplanation;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { AssistantRestController.class })
@Import(TestRestDocSecurityConfiguration.class)
@WithMockUser
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class AssistantRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final String PROJECT1_ID = "project1";

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindingAssistantService findingAssistantService;

    @Before
    public void before() {
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserRequestFindingExplanation.class)
    public void restdoc_request_finding_explanation() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.fromString("f1d02a9d-5e1b-4f52-99e5-401854ccf936");
        Integer findingId = 42;

        String apiEndpoint = https(PORT_USED).buildExplainFinding(PROJECT_ID.pathElement(), JOB_UUID.pathElement(), FINDING_ID.pathElement());

        Class<? extends Annotation> useCase = UseCaseUserRequestFindingExplanation.class;

        SecHubExplanationResponse response = new SecHubExplanationResponse();
        // Create and set FindingExplanation
        TextBlock findingExplanation = new TextBlock();
        findingExplanation.setTitle("Absolute Path Traversal Vulnerability");
        findingExplanation.setContent(
                "This finding indicates an 'Absolute Path Traversal' vulnerability in the `AsciidocGenerator.java` file. The application constructs a file path using user-supplied input (`args[0]`) without proper validation. An attacker could provide an absolute path (e.g., `/etc/passwd` on Linux or `C:\\Windows\\System32\\drivers\\etc\\hosts` on Windows) as input, allowing them to access arbitrary files on the system, potentially bypassing intended security restrictions [3, 7].");
        response.setFindingExplanation(findingExplanation);

        // Create and set PotentialImpact
        TextBlock potentialImpact = new TextBlock();
        potentialImpact.setTitle("Potential Impact");
        potentialImpact.setContent(
                "If exploited, this vulnerability could allow an attacker to read sensitive files on the server, including configuration files, source code, or even password files. This could lead to information disclosure, privilege escalation, or other malicious activities [1, 5].");
        response.setPotentialImpact(potentialImpact);

        // Create and set Recommendations
        List<TextBlock> recommendations = new ArrayList<>();

        TextBlock recommendation1 = new TextBlock();
        recommendation1.setTitle("Validate and Sanitize User Input");
        recommendation1.setContent(
                "Always validate and sanitize user-supplied input before using it to construct file paths. In this case, ensure that the `path` variable does not contain an absolute path. You can check if the path starts with a drive letter (e.g., `C:\\`) on Windows or a forward slash (`/`) on Unix-like systems [1].");
        recommendations.add(recommendation1);

        TextBlock recommendation2 = new TextBlock();
        recommendation2.setTitle("Use Relative Paths and a Base Directory");
        recommendation2.setContent(
                "Instead of allowing absolute paths, restrict user input to relative paths within a designated base directory. Construct the full file path by combining the base directory with the user-provided relative path. This limits the attacker's ability to access files outside the intended directory [1].");
        recommendations.add(recommendation2);

        TextBlock recommendation3 = new TextBlock();
        recommendation3.setTitle("Normalize the Path");
        recommendation3.setContent(
                "Normalize the constructed file path to remove any directory traversal sequences (e.g., `../`). This can be achieved using the `java.nio.file.Path.normalize()` method. After normalization, verify that the path still resides within the allowed base directory [1, 6].");
        recommendations.add(recommendation3);

        response.setRecommendations(recommendations);

        // Create and set CodeExample
        CodeExample codeExample = new CodeExample();
        codeExample.setVulnerableExample(
                "public static void main(String[] args) throws Exception {\n  String path = args[0];\n  File documentsGenFolder = new File(path);\n  //Potentially dangerous operation with documentsGenFolder\n}");
        codeExample.setSecureExample(
                "public static void main(String[] args) throws Exception {\n  String basePath = \"/safe/base/directory\";\n  String userPath = args[0];\n\n  // Validate that userPath is not an absolute path\n  if (new File(userPath).isAbsolute()) {\n    System.err.println(\"Error: Absolute paths are not allowed.\");\n    return;\n  }\n\n  Path combinedPath = Paths.get(basePath, userPath).normalize();\n\n  // Ensure the combined path is still within the base directory\n  if (!combinedPath.startsWith(basePath)) {\n    System.err.println(\"Error: Path traversal detected.\");\n    return;\n  }\n\n  File documentsGenFolder = combinedPath.toFile();\n  //Safe operation with documentsGenFolder\n}");

        TextBlock explanation = new TextBlock();
        explanation.setTitle("Code Example Explanation");
        explanation.setContent(
                "The vulnerable example directly uses user-provided input to create a `File` object, allowing an attacker to specify an arbitrary file path. The secure example first defines a base directory and combines it with the user-provided path using `Paths.get()`. It then normalizes the path and verifies that it remains within the base directory before creating the `File` object. This prevents path traversal attacks by ensuring that the application only accesses files within the intended directory [2, 6].");
        codeExample.setExplanation(explanation);
        response.setCodeExample(codeExample);

        // Create and set References
        List<Reference> references = new ArrayList<>();

        Reference reference1 = new Reference();
        reference1.setTitle("OWASP Path Traversal");
        reference1.setUrl("https://owasp.org/www-community/attacks/Path_Traversal");
        references.add(reference1);

        Reference reference2 = new Reference();
        reference2.setTitle("CWE-22: Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')");
        reference2.setUrl("https://cwe.mitre.org/data/definitions/22.html");
        references.add(reference2);

        Reference reference3 = new Reference();
        reference3.setTitle("Snyk Path Traversal");
        reference3.setUrl("https://snyk.io/learn/path-traversal/");
        references.add(reference3);

        response.setReferences(references);
        when(findingAssistantService.createSecHubExplanationResponse(PROJECT1_ID, jobUUID, findingId)).thenReturn(response);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
            get(apiEndpoint, PROJECT1_ID, jobUUID, findingId).
            header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
        ).
        andExpect(status().isOk()).
        /*andDo(print()).*/
        andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                    requestSchema(TestOpenApiSchema.ASSISTANT.getSchema()).
                and().
                document(
                            requestHeaders(

                            ),
                            responseFields(
                                    fieldWithPath(JSON_PROPERTY_FINDING_EXPLANATION).description("The explanation of the security finding"),
                                    fieldWithPath(JSON_PROPERTY_FINDING_EXPLANATION+"."+JSON_PROPERTY_TITLE).description("The title of the explanation section"),
                                    fieldWithPath(JSON_PROPERTY_FINDING_EXPLANATION+"."+JSON_PROPERTY_CONTENT).description("The text content of the explanation section"),

                                    fieldWithPath(JSON_PROPERTY_POTENTIAL_IMPACT).description("The potential impact of the security finding"),
                                    fieldWithPath(JSON_PROPERTY_POTENTIAL_IMPACT+"."+JSON_PROPERTY_TITLE).description("The title of the potential impact section"),
                                    fieldWithPath(JSON_PROPERTY_POTENTIAL_IMPACT+"."+JSON_PROPERTY_CONTENT).description("The text content of the potential impact section"),

                                    fieldWithPath(JSON_PROPERTY_RECOMMENDATIONS).description("A list of recommendations on how to handle the security finding"),
                                    fieldWithPath(JSON_PROPERTY_RECOMMENDATIONS+"[]."+JSON_PROPERTY_TITLE).description("The title of a recommendation on how to handle the security finding"),
                                    fieldWithPath(JSON_PROPERTY_RECOMMENDATIONS+"[]."+JSON_PROPERTY_CONTENT).description("The content of a recommendation on how to handle the security finding"),

                                    fieldWithPath(JSON_PROPERTY_CODE_EXAMPLE).description("Vulnerable and secure code snippets of the security finding with an explanation"),
                                    fieldWithPath(JSON_PROPERTY_CODE_EXAMPLE+"."+JSON_PROPERTY_VULNERABLE_EXAMPLE).description("Vulnerable code snippet of the security finding"),
                                    fieldWithPath(JSON_PROPERTY_CODE_EXAMPLE+"."+JSON_PROPERTY_SECURE_EXAMPLE).description("Secure code snippet of the security finding"),
                                    fieldWithPath(JSON_PROPERTY_CODE_EXAMPLE+"."+JSON_PROPERTY_EXPLANATION).description("Explanation of the code snippet related to the security finding"),
                                    fieldWithPath(JSON_PROPERTY_CODE_EXAMPLE+"."+JSON_PROPERTY_EXPLANATION+"."+JSON_PROPERTY_TITLE).description("The title of the explanation of the code snippet related to the security finding"),
                                    fieldWithPath(JSON_PROPERTY_CODE_EXAMPLE+"."+JSON_PROPERTY_EXPLANATION+"."+JSON_PROPERTY_CONTENT).description("The content of the explanation of the code snippet related to the security finding"),

                                    fieldWithPath(JSON_PROPERTY_REFERENCES).description("References for further reading on the security finding"),
                                    fieldWithPath(JSON_PROPERTY_REFERENCES+"[]."+Reference.JSON_PROPERTY_TITLE).description("The title of the reference for the security finding"),
                                    fieldWithPath(JSON_PROPERTY_REFERENCES+"[]."+Reference.JSON_PROPERTY_URL).description("The URL of the reference for the security finding")

                            ),
                            pathParameters(
                                    parameterWithName(PROJECT_ID.paramName()).description("The projectId of the project the job of the finding the user requests a explanation"),
                                    parameterWithName(JOB_UUID.paramName()).description("The jobUUID of the job of the finding the user requests a explanation"),
                                    parameterWithName(FINDING_ID.paramName()).description("The findingId of the finding the user requests a explanation")

                         )
                ));

        /* @formatter:on */
    }

}
