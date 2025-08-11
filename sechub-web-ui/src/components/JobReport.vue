<!-- SPDX-License-Identifier: MIT -->
<template>
  <JobReportToolBar
    :job-u-u-i-d="jobUUID"
    :project-id="projectId"
    :scan-type="scantype"
    :traffic-light="report.trafficLight || ''"
  />

  <SeverityFilterDialog
    :severities="availableSeverities"
    :visible="showSeverityFilter"
    @filter="filterBySeverity"
  />

  <FalsePositiveDialog
    :is-web-scan="(scantype === 'webscan')"
    :job-u-u-i-d="jobUUID"
    :project-id="projectId"
    :selected-findings="selectedFindingsForFalsePositives"
    :visible="showMarkFalsePositiveDialog"
    @close="closeFalsePositiveDialog"
    @error-alert="errorAlert=true"
    @success-alert="successAlert=true"
  />

  <AiExplanationDialog
    :ai-explanation="explanation"
    :visible="showAiExplanationDialog"
    @close="showAiExplanationDialog = false"
  />

  <v-alert
    v-if="errorAlert"
    closable
    color="error"
    density="compact"
    type="warning"
    @click:close="errorAlert=false"
  >
    {{ $t('MARK_FALSE_POSITIVE_MESSAGE_ERROR') }}
  </v-alert>

  <v-alert
    v-if="successAlert"
    closable
    color="success"
    density="compact"
    type="info"
    @click:close="successAlert=false"
  >
    {{ $t('MARK_FALSE_POSITIVE_MESSAGE_SUCCESS') }}
  </v-alert>

  <v-data-table
    v-model="selectedFindings"
    v-model:filter="filter"
    :filter-keys="['severity']"
    :headers="headers"
    item-key="id"
    :items="filteredFindingsBySeverity"
    show-expand
    show-select
  >

    <template #top>
      <div>
        <v-btn
          class="ma-4"
          :disabled="!isAnyFindingSelected"
          @click="openFalsePositiveDialog"
        >
          {{ $t('MARK_FALSE_POSITIVE_BUTTON') }}
          <v-icon
            color="primary"
            end
            icon="mdi-alert-remove-outline"
          />
        </v-btn>
      </div>
    </template>

    <template #header.severity>
      <div>
        {{ $t('REPORT_DESCRIPTION_SEVERITY') }}
        <v-btn
          class="ma-2"
          color="primary"
          icon="mdi-filter-variant"
          size="small"
          variant="text"
          @click="openSeverityFilter()"
        />
      </div>
    </template>

    <template #item.id="{ value }">
      <div>
        <span>{{ value }}</span>
        <v-btn
          v-if="isAlreadyFalsePositive(value)"
          v-tooltip="$t('MARK_FALSE_POSITIVE_FINDING_IS_FALSE_POSITIVE')"
          class="ml-4 non-clickable-btn"
          :color="calculateColor('INFO')"
          :icon="calculateIcon('INFO')"
          variant="text"
        />
      </div>
    </template>

    <template #item.severity="{ value }">
      <div>
        <v-icon
          class="ma-2"
          :color="calculateColor(value)"
          :icon="calculateIcon(value)"
          left
        />
        <span>{{ value }}</span>
      </div>
    </template>

    <template #item.cweId="{ value }">
      <div>
        <a :href="`https://cwe.mitre.org/data/definitions/${value}.html`">CWE-{{ value }}</a>
      </div>
    </template>

    <template #item.actions="{ item }">
      <v-row justify="end">
        <v-col cols="auto">
          <v-btn
            v-tooltip="$t('REPORT_EXPLAIN_FINDING')"
            :color="calculateColor('INFO')"
            icon="mdi-creation-outline"
            size="small"
            variant="text"
            @click="openAiExplanation(item)"
          />
          <!-- or @click="explainByAi(item)"-->
        </v-col>
      </v-row>
    </template>

    <template #item.data-table-expand="{ internalItem, isExpanded, toggleExpand }">
      <v-btn
        :append-icon="isExpanded(internalItem) ? 'mdi-chevron-up' : 'mdi-chevron-down'"
        class="text-none"
        color="primary"
        :text="isExpanded(internalItem) ? $t('REPORT_COLLAPS_FINDING') : $t('REPORT_SHOW_FINDING')"
        variant="text"
        @click="toggleExpand(internalItem)"
      />
    </template>

    <template #expanded-row="{ columns, item }">
      <tr>
        <td class="py-2" :colspan="columns.length">
          <v-sheet v-if="item.type !== 'webScan'" rounded="lg">
            <JobReportCodescanDetails
              :item="item"
            />
          </v-sheet>
          <v-sheet v-else rounded="lg">
            <JobReportWebscanDetails
              :item="item"
            />
          </v-sheet>
        </td>
      </tr>
    </template>
  </v-data-table>
</template>

<script lang="ts">
  import { useRoute, useRouter } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { SecHubFinding, SecHubReport } from 'sechub-openapi-ts-client'
  import { calculateColor, calculateIcon } from '@/utils/projectUtils'
  import { useReportStore } from '@/stores/reportStore'
  import { TmpFalsePositives, useTmpFalsePositivesStore } from '@/stores/tempFalsePositivesStore'
  import '@/styles/sechub.scss'

  type RouteParams = {
    id?: string;
    jobId?: string;
  };

  export default {
    name: 'JobReport',

    setup () {
      const { t } = useI18n()
      const route = useRoute()
      const router = useRouter()
      const store = useReportStore()
      const tempFalsePositivesStore = useTmpFalsePositivesStore()

      const params = route.params as RouteParams

      const projectId = ref(params.id || '')
      const jobUUID = ref(params.jobId || '')
      const scantype = ref((route.query.scantype as string) || '')

      const report = ref<SecHubReport>({})
      const markedFalsePositives = ref<TmpFalsePositives>()

      const headers = [
        { title: 'ID', key: 'id', sortable: true },
        { title: t('REPORT_DESCRIPTION_SEVERITY'), key: 'severity', sortable: false },
        { title: 'CWE', key: 'cweId' },
        { title: t('REPORT_DESCRIPTION_NAME'), key: 'name', sortable: false },
        { text: 'Actions', value: 'actions', sortable: false }, // ADD THIS LINE
      ]

      const showSeverityFilter = ref(false)
      const filter = ref('')
      const severityFilter = ref([] as string[])
      const selectedFindings = ref([] as number[])
      const selectedFindingsForFalsePositives = ref([] as SecHubFinding[])
      const showMarkFalsePositiveDialog = ref(false)
      const errorAlert = ref(false)
      const successAlert = ref(false)
      const showWebScanMarkFPButtonToggle = ref(false)

      const severityOrder = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']

      const filteredFindingsByScantype = computed(() => {
        return report.value.result?.findings?.filter(finding => finding.type?.toLocaleLowerCase() === scantype.value) || []
      })

      const sortedFindingsBySeverity = computed<SecHubFinding[]>(() => {
        return filteredFindingsByScantype.value.slice().sort((a, b) => {
          return severityOrder.indexOf(a.severity || 'INFO') - severityOrder.indexOf(b.severity || 'INFO')
        })
      })

      const filteredFindingsBySeverity = computed<SecHubFinding[]>(() => {
        if (!severityFilter.value.length) {
          return sortedFindingsBySeverity.value
        }
        return sortedFindingsBySeverity.value.filter(finding => severityFilter.value.includes(finding.severity || 'INFO'))
      })

      const isAnyFindingSelected = computed(() => selectedFindings.value.length > 0)

      const availableSeverities = computed(() => {
        const severities = new Set<string>()
        filteredFindingsByScantype.value.forEach(finding => {
          if (finding.severity) {
            severities.add(finding.severity)
          }
        })
        return Array.from(severities).sort((a, b) => severityOrder.indexOf(a) - severityOrder.indexOf(b))
      })

      onMounted(async () => {
        const reportFromStore = store.getReportByUUID(jobUUID.value)
        if (!reportFromStore) {
          router.push({ path: '/projects' })
        } else {
          report.value = reportFromStore
        }
        loadFalsePositivesFromStore()
      })

      function filterBySeverity (filter = severityFilter.value) {
        severityFilter.value = filter
        showSeverityFilter.value = false
      }

      function openSeverityFilter () {
        showSeverityFilter.value = true
      }

      function openFalsePositiveDialog () {
        const allFindings = report.value.result?.findings || []
        selectedFindingsForFalsePositives.value = selectedFindings.value.map(selected => {
          return allFindings.find(finding => finding.id === selected) || {}
        })
        showMarkFalsePositiveDialog.value = true
      }

      async function closeFalsePositiveDialog (success: boolean) {
        showMarkFalsePositiveDialog.value = false
        if (success) {
          if (scantype.value === 'webscan') {
            selectedFindings.value = []
          } else {
            const combinedArray = markedFalsePositives.value?.findingIds
              ? [...new Set([...markedFalsePositives.value.findingIds, ...selectedFindings.value])]
              : selectedFindings.value
            const newFalsePositives: TmpFalsePositives = {
              jobUUID: jobUUID.value,
              findingIds: combinedArray,
            }
            tempFalsePositivesStore.storeFalsePositives(newFalsePositives)
            loadFalsePositivesFromStore()
          }
        }
      }

      function isAlreadyFalsePositive (item: number) {
        return markedFalsePositives.value?.findingIds.includes(item) || false
      }

      function loadFalsePositivesFromStore () {
        const fpFromStorage = tempFalsePositivesStore.getFalsePositivesByUUID(jobUUID.value)
        if (fpFromStorage) {
          markedFalsePositives.value = fpFromStorage
          selectedFindings.value = fpFromStorage.findingIds
        }
      }

      const explanation = ref({})
      const showAiExplanationDialog = ref(false)
      function explainByAI (item: SecHubFinding) {
        const data = {
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
              content: 'https://cwe.mitre.org/data/definitions/22.html',
            },
            {
              title: 'Snyk Path Traversal',
              content: 'https://snyk.io/learn/path-traversal/',
            },
          ],
        }

        explanation.value = data
        showAiExplanationDialog.value = true
      }

      function openAiExplanation (item: SecHubFinding) {
        const routeData = router.resolve({
          name: '/projects/[id]/jobs/[jobId]/findings/[findingId]/',
          params: {
            id: projectId.value,
            jobId: jobUUID.value,
            findingId: item.id?.toString() || '0',
          },
        })
        window.open(routeData.href, '_blank')
      }

      return {
        projectId,
        jobUUID,
        report,
        scantype,
        headers,
        errorAlert,
        successAlert,
        showSeverityFilter,
        showMarkFalsePositiveDialog,
        showWebScanMarkFPButtonToggle,
        selectedFindings,
        selectedFindingsForFalsePositives,
        filter,
        filteredFindingsBySeverity,
        availableSeverities,
        filterBySeverity,
        calculateColor,
        calculateIcon,
        openSeverityFilter,
        isAnyFindingSelected,
        openFalsePositiveDialog,
        closeFalsePositiveDialog,
        isAlreadyFalsePositive,
        explainByAI,
        explanation,
        showAiExplanationDialog,
        openAiExplanation,
      }
    },
  }
</script>

<style scoped>
.non-clickable-btn:hover {
  cursor: default;
}
</style>
