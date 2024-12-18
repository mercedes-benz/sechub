<!-- SPDX-License-Identifier: MIT -->
<template>
    <v-card>
        <v-card-item>
          <v-card-title>{{ $t('PROJECT_DETAILS_TITLE') }} {{ projectId }}</v-card-title>
        </v-card-item>
        <v-card-item>
          <v-card-title>{{ $t('PROJECT_DETAILS_OWNER') }}</v-card-title>
        </v-card-item>
        <v-card-text >
            {{ project?.owner }}
        </v-card-text>
        <div v-if="project?.assignedUsers">
            <v-card-item>
                <v-card-title>{{ $t('PROJECT_DETAILS_MEMBERS') }}</v-card-title>
            </v-card-item>
            <v-card-text >
                <div v-for="member in project.assignedUsers">{{ member }}</div>
            </v-card-text>
        </div>
        <div v-else>
            <v-card-text >
                {{ $t('PROJECT_DETAILS_NOT_OWNED') }}
            </v-card-text>
        </div>
        
    </v-card>
</template>
<script lang="ts">
  import { defineComponent, ref, toRefs } from 'vue'
  import { useProjectStore } from '@/stores/projectStore';
import { ProjectData } from '@/generated-sources/openapi';

  interface Props {
    projectId: string
  }

  export default defineComponent({
    props: {
        projectId: {
        type: String,
        required: true,
      },
    },
    setup (props: Props) {
      const { projectId } = toRefs(props)

      const store = useProjectStore();
      const project = ref<ProjectData | undefined>(undefined);
      
      onMounted(async () => {
        project.value = await store.getProjectById(projectId.value)
        });

      return {
        project,
      }
    },
  })
</script>