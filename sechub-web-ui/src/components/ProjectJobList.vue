<!-- SPDX-License-Identifier: MIT -->
<template>
  <ProjectDetailsFab
    :show-projects-details="showProjectsDetails"
    @on-toggle-details="toggleProjectDetails"
  />
  <v-container fluid>
    <v-row>
      <v-col :cols="12" :md="showProjectsDetails ? 8 : 12">

        <v-alert
          v-model="alert"
          closable
          color="error"
          density="compact"
          :title="$t('JOB_ERROR_TITLE')"
          type="warning"
          variant="tonal"
        >
          {{ error }}
        </v-alert>

        <v-card class="mr-auto" color="background_paper">
          <v-toolbar color="background_paper">
            <v-toolbar-title>{{ projectData?.projectId }}</v-toolbar-title>
            <!-- alternative to floating button ProjectDetailsFab
            <v-btn color="primary" icon="mdi-information" @click="toggleProjectDetails" />
            -->
            <v-btn v-tooltip="$t('PROJECT_DETAILS_TOOLTIP_NEW_SCAN')" icon="mdi-plus" @click="openNewScanPage()" />
            <v-btn v-tooltip="$t('PROJECT_DETAILS_TOOLTIP_REFRESH')" icon="mdi-refresh" @click="fetchProjectJobs(currentRequestParameters)" />
            <v-btn v-tooltip="$t('PROJECT_DETAILS_TOOLTIP_BACK_TO_PROJECTS_LIST')" icon="mdi-reply" @click="backToProjectsList" />
            <v-btn v-tooltip="$t('PROJECT_DETAILS_TOOLTIP_SETTINGS')" icon="mdi-pencil" @click="openSettingsDialog()" />
          </v-toolbar>

          <div v-if="(!jobs || jobs.length === 0 && !loading)">
            <v-list bg-color="background_paper" lines="two">
              <v-list-item v-if="error" class="ma-5 background-color" rounded="lg">{{ $t('ERROR_FETCHING_DATA') }}</v-list-item>
              <v-list-item v-else class="ma-5" rounded="lg">{{ $t('NO_JOBS_RUNNED') }}</v-list-item>
            </v-list>
            <v-dialog v-model="settingsDialog" max-height="400" max-width="500">
              <v-alert
                v-if="settingsError!=undefined"
                type="error"
              >
                {{ settingsError }}
              </v-alert>
              <v-card>
                <v-card-title>
                  <v-icon left size="small">mdi-pencil</v-icon>
                  {{ $t('PROJECT_SETTINGS_DIALOG_TITLE') }}
                </v-card-title>
                <v-card>
                  <v-responsive class="mx-auto" max-width="80%">
                    <v-text-field
                      v-model="settingsOwnerUserId"
                      :append-icon="settingsOwnerFieldIcon"
                      hide-details="auto"
                      :label="$t('PROJECT_SETTINGS_PROJECT_OWNER_LABEL')"
                      @click:append="onSettingsChangeOwnerClicked"
                      @vue:updated="onSettingsOwnerFieldChanged"
                    />
                  </v-responsive>
                </v-card>

                <v-card-actions>
                  <v-btn color="primary" @click="settingsDialog = false">{{ $t('USER_SETTINGS_REQUEST_NEW_API_TOKEN_DIALOG_CLOSE') }}</v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
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
                      <template #activator="{ props }">
                        <v-btn class="ma-2" v-bind="props">
                          {{ $t('JOB_TABLE_DOWNLOAD_REPORT') }}
                          <v-icon end icon="mdi-arrow-down" />
                        </v-btn>
                      </template>
                      <v-list>
                        <v-list-item @click="downloadJobReportHtml(job.jobUUID)">
                          <v-list-item-title>{{ $t('JOB_TABLE_DOWNLOAD_HTML_REPORT') }}</v-list-item-title>
                        </v-list-item>
                        <v-list-item @click="downloadJobReportJson(job.jobUUID)">
                          <v-list-item-title>{{ $t('JOB_TABLE_DOWNLOAD_JSON_REPORT') }}</v-list-item-title>
                        </v-list-item>
                      </v-list>
                    </v-menu>
                  </span>
                  </td>
                  <td>{{ job.jobUUID }}</td>
                  <td>
                    <AsyncButton
                      v-if="['RUNNING', 'STARTED', 'READY_TO_START'].includes(job.executionState || '')"
                      :id="job.jobUUID || ''"
                      color="error"
                      icon="mdi-close-circle-outline"
                      @button-clicked="cancelJob"
                    />
                  </td>
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
          :project-data="projectData"
          :selected-user-data="undefined"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
  import { onMounted, onUnmounted, ref } from 'vue'
  import defaultClient from '@/services/defaultClient'
  import { useRoute, useRouter } from 'vue-router'
  import { useProjectStore } from '@/stores/projectStore'
  import { formatDate, getTrafficLightClass } from '@/utils/projectUtils'
  import { useI18n } from 'vue-i18n'
  import {
    ProjectData,
    SecHubJobInfoForUser,
    SecHubJobInfoForUserListPage,
    UserCancelsJobRequest,
    UserListsJobsForProjectRequest,
  } from '@/generated-sources/openapi'

  export default {
    name: 'ProjectComponent',

    setup () {
      const { t } = useI18n()
      const route = useRoute()
      const router = useRouter()
      const projectId = ref('')
      if ('id' in route.params) {
        projectId.value = route.params.id
      }
      const store = useProjectStore()

      const projectData = ref<ProjectData>({
        projectId: '',
        isOwned: false,
        assignedUsers: [],
        owner: {
          userId: '',
          emailAddress: '',
        },
      })

      const maxAttempts = 6 // exponent limit
      const baseDelay = 1000 // initial delay in milliseconds
      let timeOutId: number | undefined

      const currentRequestParameters: UserListsJobsForProjectRequest = {
        projectId: projectId.value,
        size: '10',
        page: '0',
      }

      const jobsObject = ref<SecHubJobInfoForUserListPage>({})
      const jobs = ref<SecHubJobInfoForUser[] | undefined>([])
      const loading = ref(true)
      const error = ref<string | undefined>(undefined)
      const alert = ref(false)
      const showProjectsDetails = ref(true)

      const settingsDialog = ref(false)
      const settingsOwnerUserId = ref('')
      const settingsOwnerFieldIcon = ref('')
      const settingsError = ref<string | undefined>(undefined)

      async function fetchProjectJobs (requestParameters: UserListsJobsForProjectRequest) {
        try {
          jobsObject.value = await defaultClient.withOtherApi.userListsJobsForProject(requestParameters)
          jobs.value = jobsObject.value.content
        } catch (err) {
          alert.value = true
          error.value = t('JOB_ERROR_FETCHING_JOBS_FOR_PROJECT')
          console.error(t('JOB_ERROR_FETCHING_JOBS_FOR_PROJECT'), err)
        } finally {
          loading.value = false
        }
      }

      async function pollProjectJobs (attemptCount = 1) {
        await fetchProjectJobs(currentRequestParameters)
        if (attemptCount > maxAttempts) {
          attemptCount = maxAttempts
        }

        if (!error.value) {
          const delayMillis = baseDelay * Math.pow(1.5, attemptCount)
          timeOutId = setTimeout(() => pollProjectJobs(attemptCount + 1), delayMillis)
        }
      }

      async function downloadJobReportJson (jobUUID: string | undefined) {
        if (!jobUUID) {
          return
        }

        try {
          const response = await defaultClient.withExecutionApi.userDownloadJobReport({
            projectId: projectId.value,
            jobUUID,
          })
          const prettyJson = JSON.stringify(response, null, 2)
          downloadFile(new Blob([prettyJson], { type: 'application/json' }), `sechub_report_${projectId.value}_${jobUUID}.json`)
        } catch (err) {
          const errMsg = t('JOB_ERROR_REPORT_JSON_DONLOAD_FAILED' + jobUUID)
          handleError(errMsg, err)
        }
      }

      async function downloadJobReportHtml (jobUUID: string | undefined) {
        if (!jobUUID) {
          return
        }

        try {
          const response = await defaultClient.withExecutionApi.userDownloadJobReportHtml({
            projectId: projectId.value,
            jobUUID,
          })
          downloadFile(new Blob([response], { type: 'text/html' }), `sechub_report_${projectId.value}_${jobUUID}.html`)
        } catch (err) {
          const errMsg = t('JOB_ERROR_REPORT_HTML_DONLOAD_FAILED' + jobUUID)
          handleError(errMsg, err)
        }
      }

      function downloadFile (blob: Blob, fileName: string): void {
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      }

      async function cancelJob (jobUUID: string | undefined) {
        if (!jobUUID) {
          return
        }
        const requestParameter: UserCancelsJobRequest = {
          jobUUID,
        }
        try {
          await defaultClient.withJobManagementApi.userCancelsJob(requestParameter)
        } catch (err) {
          const errMsg = (t('JOB_ERROR_CANCEL_JOB_FAILED') + jobUUID)
          handleError(errMsg, err)
        }
        fetchProjectJobs(currentRequestParameters)
      }

      function onPageChange (page: number) {
        // the API page starts by 0 while vue pagination starts with 1
        currentRequestParameters.page = (page - 1).toString()
        fetchProjectJobs(currentRequestParameters)
      }

      function openNewScanPage () {
        router.push({
          name: `/projects/[id]/scan`,
          params: {
            id: projectId.value,
          },
        })
      }

      function backToProjectsList () {
        router.go(-1)
      }

      function handleError (errMsg: string, err : unknown) {
        alert.value = true
        error.value = errMsg
        console.error(errMsg, err)
      }

      function handleSettingsError (errMsg: string, err : unknown) {
        settingsError.value = errMsg
        console.error(errMsg, err)

        setTimeout(() => {
          settingsError.value = undefined
        }, 2000)
      }

      function clearSettingsErrors () {
        settingsError.value = undefined
      }

      function openSettingsDialog () {
        // setup settings variables to current state of project
        settingsOwnerFieldIcon.value = 'mdi-check'
        settingsOwnerUserId.value = projectData.value.owner.userId

        // show dialog
        settingsDialog.value = true
      }

      async function onSettingsOwnerFieldChanged () {
        if (settingsOwnerUserId.value === projectData.value.owner.userId) {
          settingsOwnerFieldIcon.value = 'mdi-check'
        } else {
          settingsOwnerFieldIcon.value = 'mdi-send'
        }
      }

      async function onSettingsChangeOwnerClicked () {
        if (settingsOwnerUserId.value === projectData.value.owner.userId) {
          return
        }
        try {
          await defaultClient.withProjectApi.adminOrOwnerChangesProjectOwner({
            projectId: projectId.value,
            userId: settingsOwnerUserId.value,
          })
          // change was successful so update in web ui:
          projectData.value.owner.userId = settingsOwnerUserId.value
          onSettingsOwnerFieldChanged()
          clearSettingsErrors()
        } catch (err) {
          const errMsg = t('PROJECT_SETTINGS_PROJECT_OWNER_CHANGE_FAILED')
          handleSettingsError(errMsg, err)
          settingsOwnerUserId.value = projectData.value.owner.userId
        }
      }

      onMounted(async () => {
        const projectFromStore = await store.getProjectById(projectId.value)
        if (!projectFromStore) {
          router.push({
            path: '/projects',
          })
        } else {
          projectData.value = projectFromStore
          pollProjectJobs()
        }
      })

      onUnmounted(() => {
        clearTimeout(timeOutId)
      })

      return {
        projectData,
        jobsObject,
        jobs,
        loading,
        error,
        alert,
        currentRequestParameters,
        showProjectsDetails,
        fetchProjectJobs,
        downloadJobReportJson,
        downloadJobReportHtml,
        cancelJob,
        onPageChange,
        openNewScanPage,
        backToProjectsList,
        settingsDialog,
        openSettingsDialog,
        settingsOwnerUserId,
        onSettingsOwnerFieldChanged,
        settingsOwnerFieldIcon,
        onSettingsChangeOwnerClicked,
        settingsError,
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
