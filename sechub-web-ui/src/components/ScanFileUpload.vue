<!-- SPDX-License-Identifier: MIT -->
<template>
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

  export default defineComponent({
    emits: ['onFileUpdate'],
    setup (props, { emit }) {
      const file = ref<File | null>(null)
      const selectedRadio = ref(1)

      const fileAccept = computed(() => {
        // files allowed: .zip and .tar
        // todo: when drag and drop check files?
        return selectedRadio.value === 1 ? '.zip' : '.tar'
      })

      function onFileChange () {
        let fileType = ''
        switch (selectedRadio.value) {
          case 1:
            fileType = 'sources'
            break
          case 2:
            fileType = 'binaries'
            break
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
        onFileChange,
        clearSelectedFile,
      }
    },
  })
</script>
