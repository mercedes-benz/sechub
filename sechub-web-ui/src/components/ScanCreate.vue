<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-container fluid>
    <v-row>
      <v-col :cols="12" :md="8">
        <v-card class="mr-auto" color="background_paper">
          <v-toolbar color="background_paper">
            <v-toolbar-title>{{ projectId }}</v-toolbar-title>
            <v-btn icon="mdi-reply" @click="backToProjectOverview" />
          </v-toolbar>

          <div class="background-color">
            <v-sheet class="background-color">
              <h2 class="background-color text-h5 pa-5">{{ $t('SCAN_CREATE_TITLE') }}</h2>
            </v-sheet>

            <v-alert
              v-model="alert"
              closable
              color="error"
              density="compact"
              :title="$t('SCAN_ERROR_ALERT_TITLE')"
              type="warning"
              variant="tonal"
            >
              {{ errors.pop() }}

            </v-alert>

            <v-card
              class="background-color ma-5"
              variant="plain"
            >
              <v-card-title>{{ $t('SCAN_CREATE_SELECT_SCAN_TYPE') }}</v-card-title>
              <ScanTypeSelect
                :scan-options="scanOptions"
                :selected-scan-options="selectedScanOptions"
                @on-toggle-selection="toggleSelection"
              />
            </v-card>

            <v-card
              class="background-color ma-5"
              variant="plain"
            >
              <v-card-title>{{ $t('SCAN_CREATE_FILE_UPLOAD') }}</v-card-title>
              <ScanFileUpload
                @on-file-update="updateFileselection"
              />
            </v-card>

            <v-card
              class="background-color ma-5"
              variant="plain"
            >

              <template #append>
                <v-btn
                  class="me-2"
                  color="primary"
                  :disabled="!validateScanReady"
                  rounded
                  :text="$t('SCAN_CREATE_SCAN_START')"
                  variant="outlined"
                  @click="buildScanConfiguration"
                />
                <!-- todo make scan configuration downloadable
                <v-btn
                  append-icon="mdi-download"
                  rounded
                  :text="$t('SCAN_CREATE_SCAN_CONFIGURATION')"
                  variant="outlined"
                />
                -->
              </template>
            </v-card>

          </div>
        </v-card>
      </v-col>
      <v-col cols="12" md="4" />
    </v-row>
  </v-container>
</template>
<script lang="ts">
  import { defineComponent } from 'vue'
  import { useRoute } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { SecHubConfiguration } from '@/generated-sources/openapi'
  import { buildSecHubConfiguration, scan } from '@/utils/scanUtils'

  export default defineComponent({

    setup () {
      // routing and translation methods
      const { t } = useI18n()
      const route = useRoute()
      const router = useRouter()
      const projectId = ref('')
      if ('id' in route.params) {
        projectId.value = route.params.id
      }

      // UI references
      const errors = ref<string[]>([])
      const alert = ref(false)
      const selectedFile = ref<File | null>(null)
      const selectedFileType = ref('')
      // todo: should be Map key, value = translation
      const selectedScanOptions = ref<string[]>([])

      // sechub scan configuration
      const defaultConfig : SecHubConfiguration = {
        apiVersion: '1.0',
        projectId: projectId.value,
      }
      const configuration = ref<SecHubConfiguration>(defaultConfig)

      const validateScanReady = computed(() => {
        if (selectedScanOptions.value.length === 0) {
          return false
        } if (selectedFile.value === null) {
          return false
        }
        return true
      })

      function backToProjectOverview () {
        router.go(-1)
      }

      function updateFileselection (newFile : File, fileType : string) {
        selectedFile.value = newFile
        selectedFileType.value = fileType
      }

      function buildScanConfiguration () {
        if (!validateScanReady.value) {
          return
        }

        if (selectedFile.value !== null) {
          configuration.value = buildSecHubConfiguration(selectedScanOptions.value, selectedFile.value, selectedFileType.value, projectId.value)
          createScan()
        }
      }

      async function createScan () {
        if (selectedFile.value !== null) {
          errors.value = await scan(configuration.value, projectId.value, selectedFile.value)
        }
        if (errors.value.length > 0) {
          // todo only one error is displayed in alert
          alert.value = true
        } else {
          // todo success message?
          backToProjectOverview()
        }
      }

      return {
        projectId,
        scanOptions: [t('SCAN_CREATE_CODE_SCAN'), t('SCAN_CREATE_SECRET_SCAN')] as string[],
        selectedScanOptions,
        validateScanReady,
        selectedFile,
        selectedFileType,
        errors,
        alert,
        updateFileselection,
        backToProjectOverview,
        buildScanConfiguration,
      }
    },

    methods: {
      toggleSelection (item : string) {
        const index = this.selectedScanOptions.indexOf(item)
        if (index === -1) {
          this.selectedScanOptions.push(item)
        } else {
          this.selectedScanOptions.splice(index, 1)
        }
      },
    },
  })
</script>
<style scoped>
.background-color{
  background-color: rgb(var(--v-theme-layer_01)) !important;
}
</style>
