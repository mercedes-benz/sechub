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
                    <v-btn class="ma-2">{{ $t('JOB_TABLE_DOWNLOAD_REPORT') }}
                      <v-icon end icon="mdi-arrow-down" />
                    </v-btn>
                  </span>
                  </td>
                  <td>{{ job.jobUUID }}</td>
                </tr>
              </tbody>
            </v-table>
          </div>
          <Pagination
            :current-page="jobsObject.page || 0 + 1"
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
  import { onMounted, ref } from 'vue'
  import defaultClient from '@/services/defaultClient'
  import { useRoute } from 'vue-router'
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

      const currentRequestParameters : UserListJobsForProjectRequest = {
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

      onMounted(async () => {
        fetchProjectJobs(currentRequestParameters)
        // polling every 10 sec. for job states
        setInterval(() => fetchProjectJobs(currentRequestParameters), 10000)
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
.background-color{
  background-color: rgb(var(--v-theme-layer_01)) !important;
}
.traffic-light-none{
  color: rgb(var(--v-theme-layer_01)) !important;
}
.traffic-light-red{
  color: rgb(var(--v-theme-error)) !important;
}
.traffic-light-green{
  color: rgb(var(--v-theme-success)) !important;
}
.traffic-light-yellow{
  color: rgb(var(--v-theme-warning)) !important;
}
</style>
