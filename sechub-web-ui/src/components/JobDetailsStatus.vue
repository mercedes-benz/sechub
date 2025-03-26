<template>
    <v-card class="mr-auto" color="background_paper">
        <v-toolbar color="background_paper">
            <v-toolbar-title>
                {{ jobUUID }}
                <v-icon 
                icon="mdi-circle"
                class="ma-2"
                :class="getTrafficLightClass(report.trafficLight || '')">
                </v-icon>
            </v-toolbar-title>
            <v-btn icon="mdi-reply" @click="routerGoBack" />
        </v-toolbar>
    </v-card>

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
  import { useI18n } from 'vue-i18n'
  import { SecHubReport } from '@/generated-sources/openapi'
  import { getTrafficLightClass, getIcon, getIconColor } from '@/utils/projectUtils'
  import { useReportStore } from '../stores/reportStore'
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

      if ('id' in route.params) {
        projectId.value = route.params.id
      }

      if ('jobId' in route.params){
        jobUUID.value = route.params.jobId
      }

      function routerGoBack () {
        router.go(-1)
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

      return {
        projectId,
        jobUUID,
        report,
        routerGoBack,
        getTrafficLightClass,
        getIconColor,
        getIcon
      }
    },
  }
</script>
<style scoped>
  .v-card {
    margin-top: 25px;
  }
</style>