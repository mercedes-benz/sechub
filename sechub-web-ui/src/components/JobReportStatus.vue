<!-- SPDX-License-Identifier: MIT -->
<template>
    <JobDetaillsToolBar
    :scan-type="''"
    :project-id="projectId"
    :job-u-u-i-d="jobUUID"
    :traffic-light="report.trafficLight || ''" />
    
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
            <v-card-title>{{ $t('REPORT_METADATA_MESSAGES') }}</v-card-title>
        </v-card-item>
        <v-table>

            <tbody>
                <tr v-for="message in report.messages">
                    <td> 
                        <v-icon :color="getIconColor(message.type || '')">
                        {{ getIcon(message.type || '') }}
                        </v-icon>
                    </td>
                    <td >{{ message.text }}</td>
                </tr>
            </tbody>
        </v-table>
    </v-card>
</template>
<script lang="ts">
  import { useRoute, useRouter } from 'vue-router'
  import { SecHubReport } from '@/generated-sources/openapi'
  import { getIcon, getIconColor } from '@/utils/projectUtils'
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

      if ('jobId' in route.params){
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
        getIconColor,
        getIcon
      }
    },
  }
</script>
<style scoped>
  .v-card {
    margin-bottom: 25px;
  }
</style>