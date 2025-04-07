<!-- SPDX-License-Identifier: MIT -->
<template>

  <JobReportToolBar
    :job-u-u-i-d="jobUUID"
    :project-id="projectId"
    :scan-type="scantype"
    :traffic-light="report.trafficLight || ''"
  />

  <v-data-table
    :group-by="groupBy"
    :headers="headers"
    item-key="id"
    :items="sortedFindings"
    show-expand
  >

    <template #group-header="{ item, columns, toggleGroup, isGroupOpen }">
      <tr>
        <td :colspan="columns.length">
          <div class="d-flex align-center">
            <v-btn
              color="medium-emphasis"
              density="comfortable"
              :icon="isGroupOpen(item) ? '$expand' : '$next'"
              size="small"
              variant="outlined"
              @click="toggleGroup(item)"
            />

            <span class="ms-4">
              <div>
                <v-icon
                  class="ma-2"
                  :color="calculateColor(item.value)"
                  :icon="calculateIcon(item.value)"
                  left
                />
                <span>{{ item.value }}</span>
              </div>
            </span>
          </div>
        </td>
      </tr>
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
        { title: t('REPORT_DESCRIPTION_SEVERITY'), key: 'severity' },
        { title: 'CWE', key: 'cweId' },
        { title: t('REPORT_DESCRIPTION_NAME'), key: 'name' },
      ]

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

      const filteredFindings = computed(() => {
        if (report.value.result?.findings) {
          return report.value.result?.findings.filter(finding => finding.type?.toLocaleLowerCase() === scantype.value) || []
        } else {
          return report.value.result?.findings
        }
      })

      const severityOrder = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']

      const sortedFindings = computed<SecHubFinding[]>(() => {
        if (!filteredFindings.value) {
          return []
        }
        return [...filteredFindings.value].sort((a, b) => {
          return severityOrder.indexOf(a.severity || 'INFO') - severityOrder.indexOf(b.severity || 'INFO')
        })
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
            return 'mdi-help-circle'
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

      return {
        projectId,
        jobUUID,
        report,
        scantype,
        headers,
        groupBy,
        calculateColor,
        calculateIcon,
        sortedFindings,
      }
    },
  }

</script>
