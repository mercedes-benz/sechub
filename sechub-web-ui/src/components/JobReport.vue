<template>
  <JobReportToolBar
    :job-u-u-i-d="jobUUID"
    :project-id="projectId"
    :scan-type="scantype"
    :traffic-light="report.trafficLight || ''"
  />

  <SeverityFilterDialog
    :visible="showSeverityFilter"
    :severities="availableSeverities"
    @filter="filterBySeverity"
  />


  <v-data-table
    v-model="selectedFindings"
    v-model:filter="filter"
    :filter-keys="['severity']"
    :items="filteredFindingsBySeverity"
    :headers="headers"
    item-key="id"
    show-expand
    show-select
    >
    <template v-slot:header.severity>
      <div>
        {{ $t('REPORT_DESCRIPTION_SEVERITY') }}
        <v-btn icon="mdi-filter-variant"
          class="ma-2"
          variant="text"
          size="small"
          color="primary"
          @click="openSeverityFilter()"
        >
        </v-btn>
      </div>
    </template>

    <template v-slot:top>
      <div>
        <div>

        </div>
        <v-btn
          v-if="isAnyFindingSelected"
          class="ma-2"
          variant="text"
          size="small"
          color="primary"
          @click="markAsFalsePositive"
        >
          {{ $t('REPORT_MARK_FALSE_POSITIVE_BUTTON') }}
        </v-btn>
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
import '@/styles/sechub.scss'

export default {
  name: 'JobReport',

  setup () {
    const { t } = useI18n()
    const route = useRoute()
    const router = useRouter()
    const store = useReportStore()

    const projectId = ref('')
    const jobUUID = ref('')

    const report = ref<SecHubReport>({})

    const headers = [
      { title: 'ID', key: 'id', sortable: true },
      { title: t('REPORT_DESCRIPTION_SEVERITY'), key: 'severity', sortable: false },
      { title: 'CWE', key: 'cweId' },
      { title: t('REPORT_DESCRIPTION_NAME'), key: 'name', sortable: false },
    ]

    const showSeverityFilter = ref(false)
    const severityFilter = ref([] as string[])
    const selectedFindings = ref<SecHubFinding[]>([])
    const filter = ref('')

    const groupBy = ref([{ key: 'severity', order: false }])

    if ('id' in route.params) {
      projectId.value = route.params.id
    }

    if ('jobId' in route.params) {
      jobUUID.value = route.params.jobId
    }

    const query = route.query.scantype as string
    const scantype = ref('')
    scantype.value = query

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

    const isAnyFindingSelected = computed(() => {
      return selectedFindings.value.length > 0
    })

    const availableSeverities = computed(() => {
      const severities = new Set<any>()
      if(!filteredFindingsByScantype.value){
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

    function filterBySeverity(filter = severityFilter.value){
      severityFilter.value = filter
      showSeverityFilter.value = false
    }

    function openSeverityFilter(){
      showSeverityFilter.value = true
    }

    function markAsFalsePositive() {
      // open Dialog
    }

    return {
      projectId,
      jobUUID,
      report,
      scantype,
      headers,
      groupBy,
      calculateColor,
      calculateIcon,
      showSeverityFilter,
      severityFilter,
      selectedFindings,
      filter,
      filteredFindingsBySeverity,
      sortedFindingsBySeverity,
      availableSeverities,
      filterBySeverity,
      openSeverityFilter,
      isAnyFindingSelected,
      markAsFalsePositive,
    }
  },
}
</script>