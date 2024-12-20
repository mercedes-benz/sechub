<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-radio-group v-model="selectedRadio">
    <v-radio 
      :value="1" 
      label="Source Code"
      color="primary"></v-radio>
    <v-radio 
      :value="2" 
      label="Binaries"
      color="primary"></v-radio>
  </v-radio-group>
  
  <v-file-input
    v-model="file"
    :multiple="false"
    :label="$t('SCAN_CREATE_FILE_UPLOAD_INPUT')"
    prepend-icon="mdi-upload"
    max-width="1000px"
    variant="outlined"
    base-color="primary"
    :accept="fileAccept"
    @update:model-value="onFileChange"
  >
    <template v-slot:selection="{ fileNames }">
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
import { defineComponent, ref } from 'vue';

export default defineComponent({
  emits: ['onFileUpdate'],
  setup(props, { emit }) {
    const file = ref<File | null>(null);
    const selectedRadio = ref(1);

    const fileAccept = computed(() => {
      // todo: define allowed files in team
      return selectedRadio.value === 1 ? '.zip' : '.tar';
    });

    function onFileChange () {
        // calls function from parent component (emit)
        emit('onFileUpdate', file)
    }

    return {
      file,
      selectedRadio,
      fileAccept,
      onFileChange
    };
  },
});
</script>