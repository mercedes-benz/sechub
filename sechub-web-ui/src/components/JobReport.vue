<!-- SPDX-License-Identifier: MIT -->
<template>

    <JobDetaillsToolBar
    :project-id="projectId"
    :job-u-u-i-d="jobUUID"
    :traffic-light="report.trafficLight || ''" />

    <v-data-table
    :group-by=groupBy
    :headers="headers"
    :items="report.result?.findings"
    item-key="id"
    :sort-by=sortById
    show-expand
  >
  
  <!-- START Optional grouping -->
  <template v-slot:group-header="{ item, columns, toggleGroup, isGroupOpen }">
      <tr>
        <td :colspan="columns.length">
          <div class="d-flex align-center">
            <v-btn
              :icon="isGroupOpen(item) ? '$expand' : '$next'"
              color="medium-emphasis"
              density="comfortable"
              size="small"
              variant="outlined"
              @click="toggleGroup(item)"
            ></v-btn>

            <span class="ms-4"> 
              <div>
                <v-icon
                :color="calculateColor(item.value)"
                :icon="calculateIcon(item.value)"
                left
                class="ma-2">
                </v-icon>
                <span>{{ item.value }}</span>
              </div>
            </span>
          </div>
        </td>
      </tr>
    </template>
     <!-- END Optional grouping -->

  <template v-slot:item.severity="{ value }">
    <div>
      <v-icon
      :color="calculateColor(value)"
      :icon="calculateIcon(value)"
      left
      class="ma-2">
      </v-icon>
      <span>{{ value }}</span>
    </div>
    </template>

  <template v-slot:item.data-table-expand="{ internalItem, isExpanded, toggleExpand }">
      <v-btn
        :append-icon="isExpanded(internalItem) ? 'mdi-chevron-up' : 'mdi-chevron-down'"
        :text="isExpanded(internalItem) ? 'Collapse' : 'More info'"
        class="text-none"
        color="primary"
        size="small"
        variant="text"
        @click="toggleExpand(internalItem)"
      ></v-btn>
    </template>

    <template v-slot:expanded-row="{ columns, item }">
      <tr>
        <td :colspan="columns.length" class="py-2">
          <v-sheet v-if="item.type !== 'webScan'" rounded="lg" >
            <JobReportCodescanDetails 
            :item="item"/>
          </v-sheet>
          <v-sheet v-else rounded="lg" >
            <JobReportWebscanDetails
            :item="item"/>
          </v-sheet>
        </td>
      </tr>
    </template>
  </v-data-table>
</template>
<script lang="ts">
  import { useRoute, useRouter } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { SecHubReport } from '@/generated-sources/openapi'
  import { useReportStore } from '@/stores/reportStore'
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

      const headers = [
        { title: 'ID', key: 'id', sortable: true },
        { title: 'Severity', key: 'severity' },
        { title: 'CWE', key: 'cweId' },
        { title: 'Type', key: 'type' },
        { title: 'Description', key: 'description', sortable: false },
    ]
    const sortById = ref([{ key: 'id', order: true }])
    const groupBy = ref([{ key: 'severity', order: true }])

      if ('id' in route.params) {
        projectId.value = route.params.id
      }

      if ('jobId' in route.params){
        jobUUID.value = route.params.jobId
      }

      onMounted(async () => {
        const reportFromStore = await store.getReportByUUID(jobUUID.value)
        if (!reportFromStore) {
            router.push({
                path: '/projects',
            })
            } else {
                report.value = reportFromStore
            }
        })
      
      function calculateIcon(severity :string){
        switch (severity) {
          case 'CRITICAL':
          case 'HIGH':
              return 'mdi-alert-circle-outline';
          case 'MEDIUM':
            return 'mdi-alert-circle-outline';
          case 'LOW':
          case 'INFO':
            return 'mdi-information-outline';
          default:
            return 'mdi-help-circle';
        }
      }

      function calculateColor(severity: string){
        switch (severity) {
          case 'CRITICAL':
          case 'HIGH':
              return 'error';
          case 'MEDIUM':
            return 'warning';
          case 'LOW':
            return 'success'
          case 'INFO':
            return 'primary';
          default:
            return 'layer_01';
        }
      }

      return {
        projectId,
        jobUUID,
        report,
        headers,
        sortById,
        groupBy,
        calculateColor,
        calculateIcon
      }
    },
  }

</script>
