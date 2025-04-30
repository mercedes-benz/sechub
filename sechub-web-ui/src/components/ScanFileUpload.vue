<!-- SPDX-License-Identifier: MIT -->
<template>

  <v-alert
    v-model="alert"
    closable
    color="error"
    :title="$t('SCAN_CREATE_FILE_UPLOAD_INPUT_ERROR_TITLE')"
    type="warning"
  >
    {{ error }}
  </v-alert>

  <v-radio-group
    v-model="selectedRadio"
    @update:model-value="clearSelectedFile"
  >
    <v-radio
      color="primary"
      :label="$t('SCAN_CREATE_SOURCE_CODE')"
      :value="1"
    />
    <v-radio
      color="primary"
      :label="$t('SCAN_CREATE_BINARIES')"
      :value="2"
    />
  </v-radio-group>

  <v-file-input
    v-model="file"
    :accept="fileAccept"
    base-color="primary"
    :clearable="false"
    :label="$t('SCAN_CREATE_FILE_UPLOAD_INPUT')"
    max-width="1000px"
    :multiple="false"
    prepend-icon="mdi-upload"
    variant="outlined"
    @update:model-value="onFileChange"
  >
    <template #selection="{ }">
      <v-chip
        class="ma-2"
        outlined
      >
        {{ file?.name }}
      </v-chip>
    </template>
  </v-file-input>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { FILETYPE_BINARIES, FILETYPE_SOURCES } from '@/utils/applicationConstants'

  export default defineComponent({
    emits: ['onFileUpdate'],
    setup (props, { emit }) {
      const { t } = useI18n()
      const file = ref<File | null>(null)
      const selectedRadio = ref(1)
      const error = ref('')
      const alert = ref(false)

      const fileAccept = computed(() => {
        // files allowed: .zip and .tar
        // todo: when drag and drop check files?
        return selectedRadio.value === 1 ? '.zip' : '.tar'
      })

      function onFileChange () {
        let fileType = ''
        let errorMessage = ''
        let validType = false

        switch (selectedRadio.value) {
          case 1:
            fileType = FILETYPE_SOURCES
            validType = file.value?.type === 'application/zip'
            errorMessage = t('SCAN_CREATE_FILE_UPLOAD_INPUT_ERROR_ZIP')
            break
          case 2:
            fileType = FILETYPE_BINARIES
            validType = file.value?.type === 'application/x-tar'
            errorMessage = t('SCAN_CREATE_FILE_UPLOAD_INPUT_ERROR_TAR')
            break
        }

        if (file.value && !validType) {
          error.value = errorMessage
          file.value = null
          alert.value = true
        } else {
          alert.value = false
        }

        // calls function from parent component (emit)
        emit('onFileUpdate', file.value, fileType)
      }

      function clearSelectedFile () {
        if (file.value !== null) {
          file.value = null
          onFileChange()
        }
      }

      return {
        file,
        selectedRadio,
        fileAccept,
        alert,
        error,
        onFileChange,
        clearSelectedFile,
      }
    },
  })
</script>
