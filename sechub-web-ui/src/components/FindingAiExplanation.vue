<template>
  <v-toolbar color="background_paper">
    <v-toolbar-title>{{ projectId }}</v-toolbar-title>
  </v-toolbar>

  <v-toolbar color="background_paper">
    <v-toolbar-title>
      <span>{{ report.jobUUID }}</span>
      <span
        class="ml-6 sechub-primary-color"
      >{{ finding.name }} - (ID: {{ finding.id }}) </span>
      <span :class="['ml-6' ]">
        <v-icon
          :color="calculateColor(finding.severity || '')"
          :icon="calculateIcon(finding.severity || '')"
        />
      </span>

    </v-toolbar-title>
    <template #prepend>
      <v-icon
        :class="['traffic-light-toolbar', getTrafficLightClass(report.trafficLight || '')]"
        icon="mdi-circle"
        size="x-large"
      />
    </template>
  </v-toolbar>

  <v-card>
    <v-card-title>
      {{ explanation.findingExplanation.title }}
    </v-card-title>
    <v-card-text>
      {{ explanation.findingExplanation.content }}
    </v-card-text>
  </v-card>

  <v-card>
    <v-card-title>
      {{ explanation.potentialImpact.title }}
    </v-card-title>
    <v-card-text>
      {{ explanation.potentialImpact.content }}
    </v-card-text>
  </v-card>

  <v-card>
    <v-card-title>
      Recommendations
    </v-card-title>
    <v-card-text>
      <v-list>
        <v-list-item v-for="(recommendation, index) in explanation.recommendations" :key="index">
          <v-list-item-title>{{ recommendation.title }}</v-list-item-title>
          <v-list-item-subtitle>{{ recommendation.content }}</v-list-item-subtitle>
        </v-list-item>
      </v-list>
    </v-card-text>
  </v-card>

  <v-card>
    <v-card-title>
      Example Code
    </v-card-title>
    <v-card-text>
      <v-card-subtitle>
        Vulnerable Example
      </v-card-subtitle>
      <v-code>
        {{ explanation.codeExample.vulnerableExample }}
      </v-code>
    </v-card-text>
    <v-card-text>
      <v-card-subtitle>
        Fixed Example
      </v-card-subtitle>
      <v-code>
        {{ explanation.codeExample.secureExample }}
      </v-code>
    </v-card-text>
    <v-card-text>
      {{ explanation.codeExample.explanation.content }}
    </v-card-text>
  </v-card>

  <v-card>
    <v-card-title>
      References
    </v-card-title>
    <v-list>
      <v-list-item v-for="(reference, index) in explanation.references" :key="index">
        <a :href="reference.content">{{ reference.title }}</a>
      </v-list-item>
    </v-list>
  </v-card>

</template>
<script lang="ts">
  import { SecHubFinding, SecHubReport } from 'sechub-openapi-ts-client'
  import { defineComponent } from 'vue'
  import { useRoute } from 'vue-router'
  import { useReportStore } from '@/stores/reportStore'
  import { calculateColor, calculateIcon, getTrafficLightClass } from '@/utils/projectUtils'
  import '@/styles/sechub.scss'

  type RouteParams = {
    id?: string;
    jobId?: string;
    findingId?: string;
  };

  export default defineComponent({
    name: 'FindingAiExplanation',

    setup () {
      const route = useRoute()
      const params = route.params as RouteParams
      const projectId = ref(params.id || '')
      const jobUUID = ref(params.jobId || '')
      const findingId = ref(params.findingId || '')

      const store = useReportStore()
      const report = ref<SecHubReport>({})
      const finding = ref<SecHubFinding>({})

      // Example explanation data
      const reportFromStore = store.getReportByUUID(jobUUID.value)
      if (reportFromStore) {
        report.value = reportFromStore
        if (report.value.result?.findings) {
          const findingsAsNumber = parseInt(findingId.value, 10)
          finding.value = report.value.result.findings.find(f => f.id === findingsAsNumber) || {}
        }
      } else {
        console.error('Report not found in store')
      }

      const explanation = ref({
        findingExplanation: {
          title: 'Absolute Path Traversal Vulnerability',
          content: "This finding indicates an 'Absolute Path Traversal' vulnerability in the `AsciidocGenerator.java` file. The application constructs a file path using user-supplied input (`args[0]`) without proper validation. An attacker could provide an absolute path (e.g., `/etc/passwd` on Linux or `C:\\Windows\\System32\\drivers\\etc\\hosts` on Windows) as input, allowing them to access arbitrary files on the system, potentially bypassing intended security restrictions [3, 7].",
        },
        potentialImpact: {
          title: 'Potential Impact',
          content: 'If exploited, this vulnerability could allow an attacker to read sensitive files on the server, including configuration files, source code, or even password files. This could lead to information disclosure, privilege escalation, or other malicious activities [1, 5].',
        },
        recommendations: [
          {
            title: 'Validate and Sanitize User Input',
            content: 'Always validate and sanitize user-supplied input before using it to construct file paths. In this case, ensure that the `path` variable does not contain an absolute path. You can check if the path starts with a drive letter (e.g., `C:\\`) on Windows or a forward slash (`/`) on Unix-like systems [1].',
          },
          {
            title: 'Use Relative Paths and a Base Directory',
            content: "Instead of allowing absolute paths, restrict user input to relative paths within a designated base directory. Construct the full file path by combining the base directory with the user-provided relative path. This limits the attacker's ability to access files outside the intended directory [1].",
          },
          {
            title: 'Normalize the Path',
            content: 'Normalize the constructed file path to remove any directory traversal sequences (e.g., `../`). This can be achieved using the `java.nio.file.Path.normalize()` method. After normalization, verify that the path still resides within the allowed base directory [1, 6].',
          },
        ],
        codeExample: {
          vulnerableExample: 'public static void main(String[] args) throws Exception {\n  String path = args[0];\n  File documentsGenFolder = new File(path);\n  //Potentially dangerous operation with documentsGenFolder\n}',
          secureExample: 'public static void main(String[] args) throws Exception {\n  String basePath = "/safe/base/directory";\n  String userPath = args[0];\n\n  // Validate that userPath is not an absolute path\n  if (new File(userPath).isAbsolute()) {\n    System.err.println("Error: Absolute paths are not allowed.");\n    return;\n  }\n\n  Path combinedPath = Paths.get(basePath, userPath).normalize();\n\n  // Ensure the combined path is still within the base directory\n  if (!combinedPath.startsWith(basePath)) {\n    System.err.println("Error: Path traversal detected.");\n    return;\n  }\n\n  File documentsGenFolder = combinedPath.toFile();\n  //Safe operation with documentsGenFolder\n}',
          explanation: {
            title: 'Code Example Explanation',
            content: 'The vulnerable example directly uses user-provided input to create a `File` object, allowing an attacker to specify an arbitrary file path. The secure example first defines a base directory and combines it with the user-provided path using `Paths.get()`. It then normalizes the path and verifies that it remains within the base directory before creating the `File` object. This prevents path traversal attacks by ensuring that the application only accesses files within the intended directory [2, 6].',
          },
        },
        references: [
          {
            title: 'OWASP Path Traversal',
            content: 'https://owasp.org/www-community/attacks/Path_Traversal',
          },
          {
            title: "CWE-22: Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')",
            content: 'https://cwe.mitre.org/data  import { RouteParams } from /definitions/22.html',
          },
          {
            title: 'Snyk Path Traversal',
            content: 'https://snyk.io/learn/path-traversal/',
          },
        ],
      })

      return {
        explanation,
        projectId,
        jobUUID,
        finding,
        report,
        getTrafficLightClass,
        calculateColor,
        calculateIcon,
      }
    },
  })
</script>
