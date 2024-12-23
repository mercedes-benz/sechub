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
                <v-btn
                  append-icon="mdi-download"
                  rounded
                  :text="$t('SCAN_CREATE_SCAN_CONFIGURATION')"
                  variant="outlined"
                />
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
  import { buildSecHubConfiguration } from '@/utils/scanUtils'

  export default defineComponent({

    setup () {
      const { t } = useI18n()
      const route = useRoute()
      const router = useRouter()
      const projectId = ref('')
      if ('id' in route.params) {
        projectId.value = route.params.id
      }

      const error = ref('')
      const selectedFile = ref<File | null>(null)
      const selectedFileType = ref('')
      // todo: should be Map key, value = translation
      const selectedScanOptions = ref<string[]>([])
      const configuration = ref<SecHubConfiguration | null>(null)

      const validateScanReady = computed(() => {
        // todo: show errors when try to scan
        
        if (selectedScanOptions.value.length === 0) {
          return false
        } else if (selectedFile.value === null) {
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
        }
        console.log('Configuration', configuration.value)
        createScan()
      }

      function createScan () {
      // userCreateNewJob
      }

      return {
        projectId,
        scanOptions: [t('SCAN_CREATE_CODE_SCAN'), t('SCAN_CREATE_SECRET_SCAN')] as string[],
        selectedScanOptions,
        validateScanReady,
        selectedFile,
        selectedFileType,
        error,
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
