package com.mercedesbenz.sechub.assistant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.api.internal.gen.model.CodeExample;
import com.mercedesbenz.sechub.api.internal.gen.model.Reference;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.api.internal.gen.model.TextBlock;

@Service
public class FindingAssistantService {

    public SecHubExplanationResponse createSecHubExplanationResponse(String projectId, UUID jobUUID, int findingId) {
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

        return response;
    }

}
