<!-- SPDX-License-Identifier: MIT -->
<template>
  <JobReportToolBar
    :job-u-u-i-d="jobUUID"
    :project-id="projectId"
    :scan-type="''"
    :traffic-light="report.trafficLight || ''"
  />

  <v-card>
    <v-card-item>
      <v-card-title>
        {{ $t('REPORT_METADATA_SCAN_STATUS') }}:
        <v-icon :color="getIconColor(report.status || '')">
          {{ getIcon(report.status || '') }}
        </v-icon>
      </v-card-title>
    </v-card-item>
  </v-card>

  <v-card>
    <v-card-item>
      <v-card-title>{{ $t('REPORT_METADATA_EXECUTED') }}:</v-card-title>
    </v-card-item>
    <v-table>

      <tbody>
        <tr
          v-for="executed in report.metaData?.executed"
          :key="executed"
        >
          <td><v-icon class="mr-3">
            mdi-check
          </v-icon>{{ executed }}</td>
        </tr>
      </tbody>
    </v-table>
  </v-card>
  <v-card>
    <v-card-item>
      <v-card-title>{{ $t('REPORT_METADATA_MESSAGES') }}:</v-card-title>
    </v-card-item>
    <v-table>

      <tbody>
        <tr
          v-for="message in report.messages"
          :key="message.text"
        >
          <td><v-icon class="mr-3" :color="getIconColor(message.type || '')">
            {{ getIcon(message.type || '') }}
          </v-icon>{{ message.text }}</td>
        </tr>
      </tbody>
    </v-table>
  </v-card>
</template>
<script lang="ts">
  import { useRoute, useRouter } from 'vue-router'
  import { SecHubReport } from 'sechub-openapi-ts-client'
  import { getIconColorFromScanStatus, getIconFromScanStatus } from '@/utils/projectUtils'
  import { useReportStore } from '@/stores/reportStore'
  import '@/styles/sechub.scss'

  export default {
    name: 'JobReportStatus',

    setup () {
      const route = useRoute()
      const router = useRouter()
      const store = useReportStore()

      const projectId = ref('')
      const jobUUID = ref('')

      const report = ref<SecHubReport>({})

      if ('id' in route.params) {
        projectId.value = route.params.id
      }

      if ('jobId' in route.params) {
        jobUUID.value = route.params.jobId
      }

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

      return {
        projectId,
        jobUUID,
        report,
        getIconColor: getIconColorFromScanStatus,
        getIcon: getIconFromScanStatus,
      }
    },
  }
</script>
<style scoped>
  .v-card {
    margin-bottom: 25px;
  }
</style>
