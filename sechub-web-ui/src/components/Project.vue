<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-card class="mr-auto" color="background_paper" width="70%">
    <v-toolbar color="background_paper" width="70%">
      <v-toolbar-title>{{ projectId }}</v-toolbar-title>
    </v-toolbar>

    <div v-if="jobs.length === 0 && !loading">
      <v-list bg-color="background_paper" lines="two">
        <v-list-item v-if="error" class="ma-5 background-color" rounded="lg">{{ $t('ERROR_FETCHING_DATA') }}</v-list-item>
        <v-list-item v-else class="ma-5" rounded="lg">{{ $t('NO_JOBS_RUNNED') }}</v-list-item>
      </v-list>
    </div>

    <v-table
      v-else
      class="background-color"
      fixed-header
      height="300px"
    >
      <thead>
        <tr>
          <th v-for="(header, index) in getHeaders()" :key="index" class="text-left background-color">{{ header }}</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="job in jobs"
          :key="job.jobUUID"
          class="background-color"
        >
          <td>{{ job.executionState }}</td>
          <td>{{ job.executionResult }}</td>
          <td>{{ formatDate(job.created) }}</td>
          <td><v-icon :class="getTrafficLightClass(job.trafficLight)" icon="mdi-circle" /></td>
          <td><span v-if="job.executionResult === 'OK'">
            <v-btn class="ma-2">{{ $t('JOB_TABLE_DOWNLOAD_REPORT') }}
              <v-icon end icon="mdi-arrow-down" />
            </v-btn>
          </span>
          </td>
        </tr>
      </tbody>
    </v-table>
  </v-card>
</template>

  <script>
  import { onMounted, ref, inject } from 'vue'
  import defaultClient from '@/services/defaultClient'
  import { useI18n } from 'vue-i18n'
  import { useRoute } from 'vue-router'

  export default {
    name: 'ProjectComponent',

    setup (props) {
      // loads projectId from route
      const route = useRoute()
      const projectId = route.params.id

      const requestParameters = {
        projectId,
        size: 10,
      }

      const jobsObject = ref({})
      const jobs = ref([])
      const loading = ref(true)
      const error = ref(null)

      onMounted(async () => {
        try {
          jobsObject.value = await defaultClient.withOtherApi.userListJobsForProject(requestParameters)
          jobs.value = jobsObject.value.content
        } catch (err) {
          error.value = 'ProjectAPI error fetching jobs for project.'
          console.error('ProjectAPI error fetching jobs for project:', err)
        } finally {
          loading.value = false
        }
      })

      return {
        projectId,
        jobsObject,
        jobs,
        loading,
        error,
      }
    },

    methods: {
      getHeaders () {
        const { t } = useI18n()
        return [t('HEADER_JOB_TABLE_STATUS'), t('HEADER_JOB_TABLE_RESULT'), t('HEADER_JOB_TABLE_DATE'), t('HEADER_JOB_TABLE_TRAFFIC_LIGHT'), t('HEADER_JOB_TABLE_REPORT')]
      },

      formatDate (dateString) {
        const date = new Date(dateString)
        const day = String(date.getDate()).padStart(2, '0')
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const year = date.getFullYear()
        const time = date.toTimeString().split(' ')[0]
        return `${day}.${month}.${year} ${time}`
      },

      getTrafficLightClass (value) {
        switch (value) {
          case 'OFF':
            return 'traffic-light-off'
          case 'RED':
            return 'traffic-light-red'
          case 'GREEN':
            return 'traffic-light-green'
          case 'YELLOW':
            return 'traffic-light-yellow'
          default:
            return 'traffic-light-none'
        }
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
