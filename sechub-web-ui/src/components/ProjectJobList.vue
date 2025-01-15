<!-- SPDX-License-Identifier: MIT -->
<template>
  <ProjectDetailsFab
    :show-projects-details="showProjectsDetails"
    @on-toggle-details="toggleProjectDetails"
  />
  <v-container fluid>
    <v-row>
      <v-col :cols="12" :md="showProjectsDetails ? 8 : 12">
        <v-card class="mr-auto" color="background_paper">
          <v-toolbar color="background_paper">
            <v-toolbar-title>{{ projectId }}</v-toolbar-title>
            <!-- alternative to floating button ProjectDetailsFab
            <v-btn color="primary" icon="mdi-information" @click="toggleProjectDetails" />
            -->
            <v-btn icon="mdi-plus" @click="openNewScanPage()" />
            <v-btn icon="mdi-refresh" @click="fetchProjectJobs(currentRequestParameters)" />
            <v-btn icon="mdi-reply" @click="backToProjectsList" />
          </v-toolbar>

          <div v-if="jobs.length === 0 && !loading">
            <v-list bg-color="background_paper" lines="two">
              <v-list-item v-if="error" class="ma-5 background-color" rounded="lg">{{ $t('ERROR_FETCHING_DATA') }}</v-list-item>
              <v-list-item v-else class="ma-5" rounded="lg">{{ $t('NO_JOBS_RUNNED') }}</v-list-item>
            </v-list>
          </div>

          <div v-else>
            <v-table
              class="background-color"
              fixed-header
              height="90%"
            >
              <thead>
                <tr>
                  <th class="background-color">{{ $t('HEADER_JOB_TABLE_CREATED') }}</th>
                  <th class="background-color">{{ $t('HEADER_JOB_TABLE_STATUS') }}</th>
                  <th class="background-color">{{ $t('HEADER_JOB_TABLE_RESULT') }}</th>
                  <th class="text-center background-color">{{ $t('HEADER_JOB_TABLE_TRAFFIC_LIGHT') }}</th>
                  <th class="text-center background-color">{{ $t('HEADER_JOB_TABLE_REPORT') }}</th>
                  <th class="background-color">{{ $t('JOB_TABLE_DOWNLOAD_JOBUUID') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="job in jobs"
                  :key="job.jobUUID"
                  class="background-color"
                >
                  <td>{{ formatDate(job.created?.toString() || '') }}</td>
                  <td>{{ job.executionState }}</td>
                  <td>{{ job.executionResult }}</td>
                  <td class="text-center"><v-icon :class="getTrafficLightClass(job.trafficLight || '')" icon="mdi-circle" /></td>
                  <td class="text-center"><span v-if="job.executionResult === 'OK'">
                    <v-menu>
                      <template v-slot:activator="{ props }">
                        <v-btn class="ma-2" v-bind="props">
                          {{ $t('JOB_TABLE_DOWNLOAD_REPORT') }}
                          <v-icon end icon="mdi-arrow-down" />
                        </v-btn>
                      </template>
                      <v-list>
                        <v-list-item @click="downloadReportHtml(job.jobUUID)">
                          <v-list-item-title>{{ $t('JOB_TABLE_DOWNLOAD_HTML_REPORT') }}</v-list-item-title>
                        </v-list-item>
                        <v-list-item @click="downloadReportJson(job.jobUUID)">
                          <v-list-item-title>{{ $t('JOB_TABLE_DOWNLOAD_JSON_REPORT') }}</v-list-item-title>
                        </v-list-item>
                      </v-list>
                    </v-menu>
                  </span>
                  </td>
                  <td>{{ job.jobUUID }}</td>
                </tr>
              </tbody>
            </v-table>
          </div>
          <Pagination
            :current-page="jobsObject.page || 1"
            :total-pages="jobsObject.totalPages || 1"
            @page-changed="onPageChange"
          />
        </v-card>
      </v-col>
      <v-col v-if="showProjectsDetails" cols="12" md="4">
        <ProjectDetails
          :project-id="projectId"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
  import { onMounted, onUnmounted, ref } from 'vue'
  import defaultClient from '@/services/defaultClient'
  import { useRoute, useRouter } from 'vue-router'
  import { formatDate, getTrafficLightClass } from '@/utils/projectUtils'
  import {
    SecHubJobInfoForUser,
    SecHubJobInfoForUserListPage,
    UserListJobsForProjectRequest,
  } from '@/generated-sources/openapi'

  export default {
    name: 'ProjectComponent',

    setup () {
      // loads projectId from route
      const route = useRoute()
      const router = useRouter()
      const projectId = ref('')
      if ('id' in route.params) {
        projectId.value = route.params.id
      }

      const maxAttempts = 4 // Maximum number of retries for backoff
      const baseDelay = 1000 // Initial delay in milliseconds
      let timeOutId: number | undefined

      const currentRequestParameters: UserListJobsForProjectRequest = {
        projectId: projectId.value,
        size: '10',
        page: '0',
      }

      const jobsObject = ref<SecHubJobInfoForUserListPage>({})
      const jobs = ref<SecHubJobInfoForUser[] | undefined>([])
      const loading = ref(true)
      const error = ref<string | undefined>(undefined)
      const showProjectsDetails = ref(true)

      async function fetchProjectJobs (requestParameters: UserListJobsForProjectRequest) {
        try {
          jobsObject.value = await defaultClient.withOtherApi.userListJobsForProject(requestParameters)
          jobs.value = jobsObject.value.content
        } catch (err) {
          error.value = 'ProjectAPI error fetching jobs for project.'
          console.error('ProjectAPI error fetching jobs for project:', err)
        } finally {
          loading.value = false
        }
      }

      async function pollProjectJobs (attemptCount = 1) {
        await fetchProjectJobs(currentRequestParameters)
        if (attemptCount > maxAttempts) {
          attemptCount = 1
        }

        if (!error.value) {
          const delayMillis = baseDelay * Math.pow(1.5, attemptCount)
          timeOutId = setTimeout(() => pollProjectJobs(attemptCount + 1), delayMillis)
        }
      }

      async function downloadReportJson(jobUUID: string) {
        try {
          const response = await defaultClient.withExecutionApi.userDownloadJobReport({
            projectId: projectId.value,
            jobUUID
          })
          const prettyJson = JSON.stringify(response, null, 2);
          downloadFile(new Blob([prettyJson], {type: 'application/json'}), `sechub_report_${projectId.value}_${jobUUID}.json`)
        } catch (err) {
          error.value = 'Failed to download JSON report.'
          console.error('Failed to download JSON report:', err)
        }
      }

      async function downloadReportHtml(jobUUID: string) {
        try {
          const response = await defaultClient.withExecutionApi.userDownloadJobReportHtml({
            projectId: projectId.value,
            jobUUID
          })
          downloadFile(new Blob([response], {type: 'text/html'}), `sechub_report_${projectId.value}_${jobUUID}.html`)
        } catch (err) {
          error.value = 'Failed to download HTML report.'
          console.error('Failed to download HTML report:', err)
        }
      }

      function downloadFile(blob: Blob, fileName: string): void {
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      }

      function onPageChange (page: number) {
        // the API page starts by 0 while vue pagination starts with 1
        currentRequestParameters.page = (page - 1).toString()
        fetchProjectJobs(currentRequestParameters)
      }

      function openNewScanPage () {
        router.push({
          name: `/[id]/scan`,
          params: {
            id: projectId.value,
          },
        })
      }

      function backToProjectsList () {
        router.go(-1)
      }

      onMounted(() => {
        pollProjectJobs()
      })

      onUnmounted(() => {
        clearTimeout(timeOutId)
      })

      return {
        projectId,
        jobsObject,
        jobs,
        loading,
        error,
        currentRequestParameters,
        showProjectsDetails,
        fetchProjectJobs,
        downloadReportJson,
        downloadReportHtml,
        onPageChange,
        openNewScanPage,
        backToProjectsList,
      }
    },

    methods: {
      formatDate,
      getTrafficLightClass,
      toggleProjectDetails () {
        this.showProjectsDetails = !this.showProjectsDetails
      },
    },
  }
</script>

<style scoped>
.background-color {
  background-color: rgb(var(--v-theme-layer_01)) !important;
}
.traffic-light-none {
  color: rgb(var(--v-theme-layer_01)) !important;
}
.traffic-light-red {
  color: rgb(var(--v-theme-error)) !important;
}
.traffic-light-green {
  color: rgb(var(--v-theme-success)) !important;
}
.traffic-light-yellow {
  color: rgb(var(--v-theme-warning)) !important;
}
</style>
