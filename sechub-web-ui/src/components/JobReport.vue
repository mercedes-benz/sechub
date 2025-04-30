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

  <FalsePositiveDialogSAST
    v-if="scantype != 'webscan'"
    :job-u-u-i-d="jobUUID"
    :project-id="projectId"
    :selected-findings="selectedFindingsForFalsePositives"
    :visible="showMarkFalsePositiveDialog"
    @close="closeFalsePositiveDialog"
    @error-alert="errorAlert=true"
    @success-alert="successAlert=true"
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
          v-if="(scantype != 'webscan')"
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
        <v-tooltip
          v-else-if="(scantype == 'webscan')"
          v-model="showWebScanMarkFPButtonToggle"
          location="right"
        >
          <template #activator="{ props }">
            <div v-bind="props" class="d-inline-block">
              <v-btn
                class="ma-4"
                :disabled="true"
              >{{ $t('MARK_FALSE_POSITIVE_BUTTON') }}
                <v-icon
                  color="primary"
                  end
                  icon="mdi-alert-remove-outline"
                />
              </v-btn>
            </div>
          </template>
          <span>{{ $t('MARK_FALSE_POSITIVE_BUTTON_COMING_SOON') }}</span>
        </v-tooltip>
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
  import { SecHubFinding, SecHubReport } from '@/generated-sources/openapi'
  import { useReportStore } from '@/stores/reportStore'
  import { TmpFalsePositives, useTmpFalsePositivesStore } from '@/stores/tempFalsePositivesStore'
  import '@/styles/sechub.scss'

  export default {
    name: 'JobReport',

    setup () {
      const { t } = useI18n()
      const route = useRoute()
      const router = useRouter()
      const store = useReportStore()
      const tempFalsePositivesStore = useTmpFalsePositivesStore()

      const projectId = ref('')
      const jobUUID = ref('')

      const report = ref<SecHubReport>({})
      const markedFalsePositives = ref<TmpFalsePositives>()

      const headers = [
        { title: 'ID', key: 'id', sortable: true },
        { title: t('REPORT_DESCRIPTION_SEVERITY'), key: 'severity', sortable: false },
        { title: 'CWE', key: 'cweId' },
        { title: t('REPORT_DESCRIPTION_NAME'), key: 'name', sortable: false },
      ]

      // severity filter constants
      const showSeverityFilter = ref(false)
      const filter = ref('')
      const severityFilter = ref([] as string[])

      // in selectedFindings only the id is saved, because it is binded to the v-data-table and id is the item key
      // to hand over the items for false positive handling, we need to transfer the whole SecHub finding
      const selectedFindings = ref([] as number[])
      const selectedFindingsForFalsePositives = ref([] as SecHubFinding[])
      const showMarkFalsePositiveDialog = ref(false)
      const errorAlert = ref(false)
      const successAlert = ref(false)

      const showWebScanMarkFPButtonToggle = ref(false)

      if ('id' in route.params) {
        projectId.value = route.params.id
      }

      if ('jobId' in route.params) {
        jobUUID.value = route.params.jobId
      }

      const query = route.query.scantype as string
      const scantype = ref('')
      scantype.value = query

      loadFalsePositivesFromStore()

      // preparing findings for presentation (order and filter)
      const filteredFindingsByScantype = computed(() => {
        if (report.value.result?.findings) {
          return report.value.result?.findings.filter(finding => finding.type?.toLocaleLowerCase() === scantype.value) || []
        } else {
          return report.value.result?.findings
        }
      })

      const severityOrder = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']

      const sortedFindingsBySeverity = computed<SecHubFinding[]>(() => {
        if (!filteredFindingsByScantype.value) {
          return []
        }
        return [...filteredFindingsByScantype.value].sort((a, b) => {
          return severityOrder.indexOf(a.severity || 'INFO') - severityOrder.indexOf(b.severity || 'INFO')
        })
      })

      const filteredFindingsBySeverity = computed<SecHubFinding[]>(() => {
        if (!severityFilter.value || severityFilter.value.length === 0) {
          return sortedFindingsBySeverity.value
        }
        return sortedFindingsBySeverity.value.filter(finding => severityFilter.value.includes(finding.severity || 'INFO'))
      })
      // end of preparing findings for presentation

      const isAnyFindingSelected = computed(() => {
        return selectedFindings.value.length > 0
      })

      const availableSeverities = computed(() => {
        const severities = new Set<any>()
        if (!filteredFindingsByScantype.value) {
          return Array.from(severities)
        }
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
          router.push({
            path: '/projects',
          })
        } else {
          report.value = reportFromStore
        }
      })

      function calculateIcon (severity :string) {
        switch (severity) {
          case 'CRITICAL':
          case 'HIGH':
            return 'mdi-alert-circle-outline'
          case 'MEDIUM':
            return 'mdi-alert-circle-outline'
          case 'LOW':
          case 'INFO':
            return 'mdi-information-outline'
          default:
            return ''
        }
      }

      function calculateColor (severity: string) {
        switch (severity) {
          case 'CRITICAL':
          case 'HIGH':
            return 'error'
          case 'MEDIUM':
            return 'warning'
          case 'LOW':
            return 'success'
          case 'INFO':
            return 'primary'
          default:
            return 'layer_01'
        }
      }

      function filterBySeverity (filter = severityFilter.value) {
        severityFilter.value = filter
        showSeverityFilter.value = false
      }

      function openSeverityFilter () {
        showSeverityFilter.value = true
      }

      function openFalsePositiveDialog () {
        const allFindings = report.value.result?.findings
        if (!allFindings) {
          selectedFindingsForFalsePositives.value = []
        } else {
          selectedFindingsForFalsePositives.value = selectedFindings.value.map(selected => {
            return allFindings.find(finding => finding.id === selected) || {}
          })
        }
        showMarkFalsePositiveDialog.value = true
      }

      async function closeFalsePositiveDialog () {
        showMarkFalsePositiveDialog.value = false

        if (successAlert.value) {
          let combinedArray
          if (markedFalsePositives.value?.findingIds) {
            combinedArray = [...new Set([...markedFalsePositives.value?.findingIds, ...selectedFindings.value])]
          } else {
            combinedArray = selectedFindings.value
          }
          const newFalsePositives: TmpFalsePositives = {
            jobUUID: jobUUID.value,
            findingIds: combinedArray,
          }
          tempFalsePositivesStore.storeFalsePositives(newFalsePositives)
          loadFalsePositivesFromStore()
        }
      }

      function isAlreadyFalsePositive (item: number) {
        if (markedFalsePositives.value) {
          return !!markedFalsePositives.value.findingIds.includes(item)
        }
        return false
      }

      function loadFalsePositivesFromStore () {
        const fpFromStorage = tempFalsePositivesStore.getFalsePositivesByUUID(jobUUID.value)
        if (fpFromStorage) {
          markedFalsePositives.value = fpFromStorage
          selectedFindings.value = fpFromStorage.findingIds
        }
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
      }
    },
  }
</script>
<style scoped>
.non-clickable-btn:hover {
  cursor: default;
}
</style>
