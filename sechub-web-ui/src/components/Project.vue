<!-- SPDX-License-Identifier: MIT -->
<template>
    <v-card class="mr-auto" color="background_paper" width="70%">
      <v-toolbar color="background_paper" width="70%">
        <v-toolbar-title>{{ jobsObject.projectId }}</v-toolbar-title>
      </v-toolbar>
  
      <div v-if="jobs.length === 0 && !loading">
        <v-list bg-color="background_paper" lines="two">
          <v-list-item v-if="error" class="ma-5" rounded="lg">{{ $t('ERROR_FETCHING_DATA') }}</v-list-item>
          <v-list-item v-else class="ma-5" rounded="lg">{{ $t('NO_JOBS_RUNNED') }}</v-list-item>
        </v-list>
      </div>
  
      <v-list v-else bg-color="background_paper" lines="two">
        <v-list-item
          v-for="(job, i) in jobs"
          :key="i"
          class="ma-5"
          rounded="lg"
          :value="job"
        >
          <template #prepend>
            <!-- Add any content you want to prepend here -->
          </template>
          <template #title>
            <span>{{ job.title }}</span>
          </template>
          <template #subtitle>
            <span>{{ job.subtitle }}</span>
          </template>
        </v-list-item>
      </v-list>
    </v-card>
  </template>
  
  <script>
  import { ref, onMounted } from 'vue';
  import defaultClient from '@/services/defaultClient';
  
  export default {
    name: 'ProjectComponent',
  
    setup() {
      // todo: should be handed over by store
      const projectId = "test-project";
      const requestParameters = {
        projectId: projectId,
      };
      const jobsObject = ref({});
      const jobs = ref([]);
      const loading = ref(true);
      const error = ref(null);
  
      onMounted(async () => {
        try {
          jobsObject.value = await defaultClient.withOtherApi.userListJobsForProject(requestParameters);
          // todo: seems like openap does not work correctly here
          // the response looks like {jobs: undefined, "projectId": "test-gosec"} but should be {"page":0,"totalPages":0,"projectId":"test-gosec","content":[]}
          jobs.value = jobsObject.content || [];
        } catch (err) {
          error.value = 'ProjectAPI error fetching jobs for project.';
          console.error('ProjectAPI error fetching jobs for project:', err);
        } finally {
          loading.value = false;
        }
      });
  
      return {
        projectId,
        jobsObject,
        jobs,
        loading,
        error,
      };
    },
  };
  </script>