<!-- SPDX-License-Identifier: MIT -->
<template>
  <ProjectDetailsFab
    :show-projects-details="showProjectsDetails"
    @on-toggle-details="toggleProjectDetails"
  />
  <v-row>
    <v-col :cols="12" :md="showProjectsDetails ? 8 : 12">

      <v-alert
        v-model="alert"
        closable
        color="error"
        density="compact"
        :title="$t('API_ERROR_TITLE')"
        type="warning"
      >
        {{ error }}
      </v-alert>

      <v-card class="mr-auto" color="background_paper">
        <v-toolbar color="background_paper">
          <v-toolbar-title>{{ projectData?.projectId }}</v-toolbar-title>
          <template #prepend>
            <v-btn
              v-tooltip="$t('PROJECT_DETAILS_TOOLTIP_BACK_TO_PROJECTS_LIST')"
              icon="mdi-arrow-left"
              @click="router.go(-1)"
            />
            <v-btn v-tooltip="$t('PROJECT_DETAILS_TOOLTIP_REFRESH')" icon="mdi-refresh" @click="refreshProjectData,fetchProjectJobs(currentRequestParameters)" />
          </template>

          <!-- to edit project settings user must be superadmin or owner -->
          <v-btn v-if="user.superAdmin || projectData.isOwned" v-tooltip="$t('PROJECT_DETAILS_TOOLTIP_SETTINGS')" icon="mdi-pencil" @click="settingsDialog=true" />
          <!-- to scan user must be superadmin or project member, (non-owner can not see assigned users) -->
          <v-btn v-if="user.superAdmin || !projectData.isOwned || projectData.assignedUsers?.some(u => u.userId === user.userId)" v-tooltip="$t('PROJECT_DETAILS_TOOLTIP_NEW_SCAN')" icon="mdi-plus" @click="openNewScanPage()" />
        </v-toolbar>

        <ProjectSettingsDialog
          v-if="!loading"
          :project-data="projectData"
          :visible="settingsDialog"
          @close="settingsDialog=false,fetchProjectJobs(currentRequestParameters)"
          @project-changed="refreshProjectData"
        />

        <div v-if="(!jobs || jobs.length === 0 && !loading)">
          <v-list bg-color="background_paper" lines="two">
            <v-list-item class="ma-5 background-color" color="layer_01" rounded="lg">
              {{ getJobMessage }}
            </v-list-item>
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
                <th class="background-color" />
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
                      <v-btn
                        class="ma-2"
                        v-bind="props"
                      >
                        {{ $t('JOB_TABLE_DOWNLOAD_REPORT') }}
                        <v-icon
                          color="primary"
                          end
                          icon="mdi-download-circle-outline"
                        />
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
                <td><v-btn
                  :disabled="job.executionResult !== 'OK'"
                  @click="viewJobReport(job.jobUUID || '')"
                >
                  {{ job.jobUUID }}
                  <v-icon
                    color="primary"
                    end
                    icon="mdi-eye-circle-outline"
                  />
                </v-btn>
                </td>
                <td>
                  <AsyncButton
                    v-if="['RUNNING', 'STARTED', 'READY_TO_START'].includes(job.executionState || '')"
                    :id="job.jobUUID || ''"
                    color="error"
                    icon="mdi-close-circle-outline"
                    @button-clicked="cancelJob"
                  />
                  <v-btn
                    v-else
                    v-tooltip="$t('PROJECT_COPY_JOB_UUID')"
                    icon="mdi-content-copy"
                    size="small"
                    @click="copyToClipboard(job.jobUUID || '')"
                  />
                </td>
              </tr>
            </tbody>
          </v-table>
        </div>
        <!-- we need to add 1 because our page starts at 0 while pagination starts with 1 -->
        <Pagination
          :current-page="(jobsObject.page || 0) + 1"
          :total-pages="jobsObject.totalPages || 1"
          @page-changed="onPageChange"
        />
      </v-card>
    </v-col>
    <v-col v-if="showProjectsDetails" cols="12" md="4">
      <ProjectDetails
        :project-data="projectData"
      />
    </v-col>
  </v-row>
</template>

<script lang="ts">
  import { onMounted, onUnmounted, ref } from 'vue'
  import defaultClient from '@/services/defaultClient'
  import { useRoute, useRouter } from 'vue-router'
  import { useProjectStore } from '@/stores/projectStore'
  import { useUserDetailInformationStore } from '@/stores/userDetailInformationStore'
  import { formatDate, getTrafficLightClass } from '@/utils/projectUtils'
  import { useI18n } from 'vue-i18n'
  import {
    ProjectData,
    SecHubJobInfoForUser,
    SecHubJobInfoForUserListPage,
    UserCancelsJobRequest,
    UserListsJobsForProjectRequest,
  } from '@/generated-sources/openapi/'
  import '@/styles/sechub.scss'
  import { useFetchProjects } from '@/composables/useProjects'
  import { handleApiError } from '@/services/apiErrorHandler'

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
      const userStore = useUserDetailInformationStore()
      const user = userStore.getUserDetailInformation()

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

      const getJobMessage = computed(() => {
        if (error.value) {
          return t('ERROR_FETCHING_DATA')
        } else if (jobs.value?.length === 0) {
          if (projectData.value.isOwned && !projectData.value.assignedUsers?.some(u => u.userId === user.userId) && !user.superAdmin) {
            return t('NON_PROJECT_MEMBER')
          } else {
            return t('NO_JOBS_RUNNED')
          }
        }
        return ''
      })

      async function fetchProjectJobs (requestParameters: UserListsJobsForProjectRequest) {
        // if user is only the owner of a project (no member and no superadmin), he can not see jobs
        if (!user.superAdmin && (projectData.value.isOwned && !projectData.value.assignedUsers?.some(u => u.userId === user.userId))) {
          // set everything empty
          jobsObject.value = {}
          jobs.value = []
          loading.value = false
          return
        }

        try {
          jobsObject.value = await defaultClient.withOtherApi.userListsJobsForProject(requestParameters)
          jobs.value = jobsObject.value.content
        } catch (err) {
          handleApiError(err)
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
          handleApiError(err)
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
          handleApiError(err)
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
          handleApiError(err)
          const errMsg = (t('JOB_ERROR_CANCEL_JOB_FAILED') + jobUUID)
          handleError(errMsg, err)
        }
        fetchProjectJobs(currentRequestParameters)
      }

      const copyToClipboard = (uuid: string) => {
        navigator.clipboard.writeText(uuid)
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

      function viewJobReport (jobId: string) {
        router.push({
          path: `/projects/${projectId.value}/jobs/${jobId}`,
        })
      }

      function handleError (errMsg: string, err : unknown) {
        alert.value = true
        error.value = errMsg
        console.error(errMsg, err)
      }

      async function refreshProjectData () {
        // We know the new user id, but not the new user email address.
        // Because of missing other REST API endpoints, we must reload all projects data
        await useFetchProjects()

        const newLoadedProject = store.getProjectById(projectId.value)
        if (newLoadedProject !== undefined) {
          projectData.value = newLoadedProject
        }
        console.debug('Project data has been refreshed.')
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
        user,
        router,
        jobsObject,
        jobs,
        loading,
        error,
        alert,
        getJobMessage,
        currentRequestParameters,
        showProjectsDetails,
        fetchProjectJobs,
        downloadJobReportJson,
        downloadJobReportHtml,
        cancelJob,
        onPageChange,
        openNewScanPage,
        viewJobReport,
        settingsDialog,
        refreshProjectData,
        copyToClipboard,
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
