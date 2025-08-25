// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.assistant.SecHubExplanationInput;

@Component
public class DefaultAIExplanationPromptDataGenerator implements AIExplanationPromptDataGenerator {

    private final static String ACCEPTED_EXAMPLE = """
                {
                  "findingExplanation" : {
                    "title" : "Explanation of the security finding",
                    "content" : "CWE-79: Improper Neutralization of Input During Web Page Generation ('Cross-site Scripting'). In the provided file TestSQLInjection.java, the source indicates that user-supplied data (paramFromWeb) is directly included in an SQL query without any form of validation or sanitization. This kind of coding practice can lead to cross-site scripting (XSS) vulnerabilities if untrusted input is included in web page generation."
                  },
                  "potentialImpact" : {
                    "title" : "Potential impact of the security finding",
                    "content" : "Exploitation of this vulnerability can allow an attacker to execute arbitrary scripts in the context of the user's browser, steal cookies, session tokens, or other sensitive information, and perform actions on behalf of the user."
                  },
                  "recommendations" : [ {
                    "title" : "Use Prepared Statements",
                    "content" : "Instead of directly including user-supplied data in SQL queries, use prepared statements to ensure that input is handled as data and not executable code."
                  }, {
                    "title" : "Validate and Sanitize Input",
                    "content" : "Implement input validation and sanitization to ensure that user-supplied data is safe and complies with expected formats before being used in SQL queries or web page generation."
                  }, {
                    "title" : "Use ORM Frameworks",
                    "content" : "Consider using Object-Relational Mapping (ORM) frameworks which inherently handle SQL injections by abstracting direct SQL query manipulation."
                  } ],
                  "codeExample" : {
                    "vulnerableExample" : "String sql = \"select * from TABLE1 t where t.userid=\" + paramFromWeb;",
                    "secureExample" : "String sql = \"select * from TABLE1 t where t.userid=?\"; PreparedStatement stmt = connection.prepareStatement(sql); stmt.setString(1, paramFromWeb);",
                    "explanation" : {
                      "title" : "Explanation of the code examples",
                      "content" : "The vulnerable example directly includes user input in the SQL query, which can be manipulated by an attacker. The secure example uses a prepared statement that treats user input as a parameter, preventing it from being executed as part of the SQL query."
                    }
                  },
                  "references" : [ {
                    "title" : "OWASP SQL Injection Prevention Cheat Sheet",
                    "url" : "https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html"
                  }, {
                    "title" : "OWASP Data Validation",
                    "url" : "https://owasp.org/www-project-cheat-sheets/cheatsheets/Data_Validation_Cheat_Sheet.html"
                  } ]
                }
            """;

    private final static String RESPONSE_JSON_SCHEMA = """
            {
              "title": "SecHubExplanationResponse",
              "type": "object",
              "properties": {
                "findingExplanation": {
                  "description": "Explanation of the security finding",
                  "type": "object",
                  "$ref": "#/components/schemas/TextBlock"
                },
                "potentialImpact": {
                  "description": "Potential impact of the security finding",
                  "type": "object",
                  "$ref": "#/components/schemas/TextBlock"
                },
                "recommendations": {
                  "description": "List of recommendations for the security finding",
                  "type": "array",
                  "items": {
                    "type": "object",
                    "$ref": "#/components/schemas/TextBlock"
                  }
                },
                "codeExample": {
                  "description": "Code example with vulnerable and secure code snippets of the security finding",
                  "type": "object",
                  "$ref": "#/components/schemas/CodeExample"
                },
                "references": {
                  "description": "References for futher reading on the security finding",
                  "type": "array",
                  "items": {
                    "type": "object",
                    "$ref": "#/components/schemas/Reference"
                  }
                }
              },
              "components": {
                "schemas": {
                  "TextBlock": {
                    "title": "TextBlock",
                    "type": "object",
                    "properties": {
                      "title": {
                        "description": "Title of the text block",
                        "type": "string"
                      },
                      "content": {
                        "description": "Content of the text block",
                        "type": "string"
                      }
                    }
                  },
                  "Reference": {
                    "title": "Reference",
                    "type": "object",
                    "properties": {
                      "title": {
                        "description": "Title of the reference",
                        "type": "string"
                      },
                      "url": {
                        "description": "URL of the reference",
                        "type": "string"
                      }
                    }
                  },
                  "CodeExample": {
                    "title": "CodeExample",
                    "type": "object",
                    "properties": {
                      "vulnerableExample": {
                        "description": "Code example with a vulnerable code snippet for the security finding",
                        "type": "string"
                      },
                      "secureExample": {
                        "description": "Code example with a secure code snippet for the security finding",
                        "type": "string"
                      },
                      "explanation": {
                        "description": "Explanation of the code exmaples regarding the security finding",
                        "type": "object",
                        "$ref": "#/components/schemas/TextBlock"
                      }
                    }
                  }
                }
              }
            }
            """;

    @Override
    public AIPromptData createExplanationPromptData(SecHubExplanationInput input) {

        String system = """
                You are an expert AI system designed to provide comprehensive support in identifying and addressing code vulnerabilities.
                You will receive context information that includes detailed descriptions of vulnerabilities based on the MITRE database.

                The provided information includes:
                - CWE ID: A unique identifier from the MITRE database for the specific type of vulnerability.
                - Source: A specific line of code or context where the vulnerability was found.

                Your task is to analyze this information and generate a clear, step-by-step solution for fixing the identified vulnerability.
                The recommendations should provide specific, actionable instructions that directly address the vulnerability at the code level.
                Focus on concrete changes to the codebase, configurations, or relevant controls needed to fix the vulnerability.
                Avoid general advice such as "Educate Developers" or "Raise Awareness"; instead, focus on changes that can be immediately applied to mitigate the risk in this specific context.

                The returned result shall be in json format.

                TextBlock: contains always a title and a description. Inside the description there shall be the details.

                Here is the JSON schema for the result JSON which will be given to user:

                %s

                Here is an example how the wanted output can look like (it is an example for a sql injection):

                %s

                Return a valid (plain text) json content not the schema which looks like the given example. Ensure the result is valid to given JSON schema.
                """
                .formatted(RESPONSE_JSON_SCHEMA, ACCEPTED_EXAMPLE);

        String user = """
                Give me support for this kind of security finding:
                - CWE ID:%s
                - FILE:%s
                - SOURCE:%s
                """.formatted(input.getCweId(), input.getFileName(), input.getRelevantSource());

        AIPromptData data = new AIPromptData();
        data.setUser(user);
        data.setSystem(system);

        return data;
    }

}
