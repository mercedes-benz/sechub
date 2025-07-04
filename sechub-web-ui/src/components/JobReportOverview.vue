<!-- SPDX-License-Identifier: MIT -->
<template>

  <v-card class="mr-auto" color="background_paper">
    <ProjectToolbar
      :project-id="projectId"
    />
    <v-toolbar color="background_paper">
      <v-toolbar-title>
        {{ jobUUID }}
        <v-btn
          class="ma-2"
          :color="getButtonColor(report.status || '')"
          variant="tonal"
          @click="routeTo('status')"
        >
          {{ report.status }}
          <v-icon end icon="mdi-information-outline" />
        </v-btn>
      </v-toolbar-title>
      <template #prepend>
        <v-icon
          :class="['traffic-light-toolbar', getTrafficLightClass(report.trafficLight || '') ]"
          icon="mdi-circle"
          size="x-large"
        />
      </template>

    </v-toolbar>

    <div v-if="(report.status === 'FAILED')">
      <v-list bg-color="background_paper" lines="two">
        <v-list-item>{{ $t('REPORT_SCAN_NOT_SUCCESSFUL') }}</v-list-item>
      </v-list>
    </div>

    <div v-else-if="(!report || scanTypeMap.size === 0)">
      <v-list bg-color="background_paper" lines="two">
        <v-list-item>{{ $t('REPORT_SCAN_NO_FINDINGS') }}</v-list-item>
      </v-list>
    </div>

    <div v-else>

      <v-table
        class="background-color clickable-column"
        fixed-header
        height="90%"
      >
        <thead>
          <tr>
            <th class="background-color">{{ $t('REPORT_SCAN_TYPE') }}</th>
            <th class="background-color">{{ $t('REPORT_TOTAL_FINDINGS') }}</th>
            <th class="background-color">{{ $t('REPORT_CRITICAL_FINDINGS') }}</th>
            <th class="background-color">{{ $t('REPORT_HIGH_FINDINGS') }}</th>
            <th class="background-color">{{ $t('REPORT_MEDIUM_FINDINGS') }}</th>
            <th class="background-color">{{ $t('REPORT_LOW_FINDINGS') }}</th>
            <th class="background-color">{{ $t('REPORT_INFO_FINDINGS') }}</th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="[key, scanType] in scanTypeMap"
            :key="key"
            class="background-color clickable-column"
            @click="routeTo(key)"
          >
            <td>{{ key }}</td>
            <td>{{ scanType.total }}</td>
            <td>{{ scanType.critical }}</td>
            <td>{{ scanType.high }}</td>
            <td>{{ scanType.medium }}</td>
            <td>{{ scanType.low }}</td>
            <td>{{ scanType.info }}</td>
          </tr>
        </tbody>
      </v-table>
    </div>
  </v-card>

</template>
<script lang="ts">
  import { useRoute, useRouter } from 'vue-router'
  import { SecHubReport, SecHubReportScanTypeSummary } from 'sechub-openapi-typescript'
  import { getTrafficLightClass } from '@/utils/projectUtils'
  import { useReportStore } from '@/stores/reportStore'
  import { useFetchReport } from '@/composables/useReport'
  import { useI18n } from 'vue-i18n'
  import '@/styles/sechub.scss'

  export default {
    name: 'JobDetail',

    setup () {
      const { t } = useI18n()
      const route = useRoute()
      const router = useRouter()
      const store = useReportStore()

      const projectId = ref('')
      const jobUUID = ref('')

      const report = ref<SecHubReport>({})
      const scanTypeMap = new Map<string, SecHubReportScanTypeSummary>()

      if ('id' in route.params) {
        projectId.value = route.params.id
      }

      if ('jobId' in route.params) {
        jobUUID.value = route.params.jobId
      }

      fetchReport()

      async function fetchReport () {
        // load cached report from store
        const reportFromStore = store.getReportByUUID(jobUUID.value)
        if (reportFromStore) {
          report.value = reportFromStore
        } else {
          // fetch report from server
          const { report: fetchedReport, error: fetchedError } = await useFetchReport(projectId.value, jobUUID.value)

          report.value = fetchedReport.value

          if (fetchedError.value) {
            console.error(t(fetchedError.value) + jobUUID.value)
          }
        }

        collectSummaries()
      }

      function getButtonColor (title: string) {
        return title === 'FAILED' ? 'error' : 'layer-01'
      }

      function collectSummaries () {
        const metaData = report.value.metaData
        if (!metaData) {
          return
        }

        if (metaData.summary?.codeScan) {
          scanTypeMap.set('codescan', metaData.summary.codeScan)
        }

        if (metaData.summary?.webScan) {
          scanTypeMap.set('webscan', metaData.summary.webScan)
        }

        if (metaData.summary?.infraScan) {
          scanTypeMap.set('infrascan', metaData.summary.infraScan)
        }

        if (metaData.summary?.licenseScan) {
          scanTypeMap.set('licensescan', metaData.summary.licenseScan)
        }

        if (metaData.summary?.secretScan) {
          scanTypeMap.set('secretscan', metaData.summary.secretScan)
        }

        if (metaData.summary?.iacScan) {
          scanTypeMap.set('iacscan', metaData.summary.iacScan)
        }
      }

      function routeTo (route: string) {
        if (route === 'status') {
          router.push({
            path: `/projects/${projectId.value}/jobs/${jobUUID.value}/status`,
          })
        } else {
          router.push({
            name: '/projects/[id]/jobs/[jobId]/scanreport',
            params: {
              id: projectId.value,
              jobId: jobUUID.value,
            },
            query: { scantype: route },
          })
        }
      }

      return {
        projectId,
        jobUUID,
        report,
        scanTypeMap,
        routeTo,
        getTrafficLightClass,
        getButtonColor,
      }
    },
  }
</script>
<style scoped>
.clickable-column:hover {
    cursor: pointer;
    background-color: rgb(var(--v-theme-layer_02)) !important;
}
</style>
