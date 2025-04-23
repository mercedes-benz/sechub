<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-row>
    <v-col :cols="12" :md="8">
      <v-card class="mr-auto" color="background_paper">
        <v-toolbar color="background_paper">
          <v-toolbar-title>{{ projectId }}</v-toolbar-title>
          <template #prepend>
            <v-btn
              icon="mdi-arrow-left"
              @click="backToProjectOverview()"
            />
          </template>
        </v-toolbar>
        <div class="background-color">
          <v-sheet class="background-color">
            <h2 class="background-color text-h5 pa-5">{{ $t('SCAN_CREATE_TITLE') }}</h2>
          </v-sheet>

          <v-alert
            v-model="alert"
            closable
            color="error"
            :title="$t('SCAN_ERROR_ALERT_TITLE')"
            type="warning"
            variant="tonal"
            @click:close="clearErrors"
          >
            <ul>
              <li v-for="error in errors" :key="error">{{ error }}</li>
            </ul>
          </v-alert>

          <v-card
            class="background-color ma-5"
            variant="flat"
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
            variant="flat"
          >
            <v-card-title>{{ $t('SCAN_CREATE_FILE_UPLOAD') }}</v-card-title>
            <ScanFileUpload
              @on-file-update="updateFileselection"
            />
            <v-card-text v-if="isLoading">
              {{ $t('SCAN_CREATE_FILE_UPLOAD_PROGRESS') }}
            </v-card-text>
            <v-progress-circular
              v-if="isLoading"
              color="primary"
              indeterminate
            />
          </v-card>

          <v-card
            class="background-color ma-5"
            variant="flat"
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
</template>
<script lang="ts">
  import { defineComponent } from 'vue'
  import { useRoute } from 'vue-router'
  import { SecHubConfiguration } from '@/generated-sources/openapi'
  import { buildSecHubConfiguration, isFileSizeValid } from '@/utils/scanConfigUtils'
  import defaultClient from '@/services/defaultClient'
  import { CODE_SCAN_IDENTIFIER, SECRET_SCAN_IDENTIFER } from '@/utils/applicationConstants'
  import '@/styles/sechub.scss'

  export default defineComponent({

    setup () {
      // routing and translation methods
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
      const isLoading = ref(false)
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
        const { errorMessage, isValid } = isFileSizeValid(newFile, fileType)

        if (isValid) {
          selectedFile.value = newFile
          selectedFileType.value = fileType
          return
        }

        selectedFile.value = null
        errors.value.push(errorMessage)
        alert.value = true
      }

      function buildScanConfiguration () {
        if (!validateScanReady.value) {
          return
        }

        if (selectedFile.value !== null) {
          configuration.value = buildSecHubConfiguration(selectedScanOptions.value, selectedFileType.value, projectId.value)
          createScan()
        }
      }

      async function createScan () {
        if (selectedFile.value !== null) {
          isLoading.value = true
          errors.value = await defaultClient.withScanService.scan(configuration.value, projectId.value, selectedFile.value)
        }
        isLoading.value = false
        if (errors.value.length > 0) {
          alert.value = true
        } else {
          backToProjectOverview()
        }
      }

      function clearErrors () {
        errors.value = []
        alert.value = false
      }

      return {
        projectId,
        scanOptions: [CODE_SCAN_IDENTIFIER, SECRET_SCAN_IDENTIFER] as string[],
        selectedScanOptions,
        validateScanReady,
        selectedFile,
        selectedFileType,
        errors,
        alert,
        isLoading,
        clearErrors,
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
