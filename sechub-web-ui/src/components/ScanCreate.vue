<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-container fluid>
    <v-row>
      <v-col :cols="12" :md="8">
        <v-card class="mr-auto" color="background_paper">
          <v-toolbar color="background_paper">
            <v-toolbar-title>{{ projectId }}</v-toolbar-title>
              <v-btn icon="mdi-reply" @click="backToProjectOverview"/>
          </v-toolbar>

          <div class="background-color">
            <v-sheet class="background-color">
              <h2 class="background-color text-h5 pa-5">{{ $t('SCAN_CREATE_TITLE') }}</h2>
            </v-sheet>
  
            <v-card 
            class="background-color ma-5"
            variant="plain">
              <v-card-title>{{ $t('SCAN_CREATE_SELECT_SCAN_TYPE') }}</v-card-title>
              <ScanTypeSelect 
              :scan-options="scanOptions"
              :selected-scan-options="selectedScanOptions"
              @on-toggle-selection="toggleSelection"
              />
          </v-card>

          <v-card
            class="background-color ma-5"
            variant="plain">
              <v-card-title>{{ $t('SCAN_CREATE_FILE_UPLOAD') }}</v-card-title>
              <ScanFileUpload 
              @on-file-update="updateFileselection"/>
          </v-card>

          <v-card
            class="background-color ma-5"
            variant="plain">
            <template #append v-slot:actions>
              <v-btn
                color="primary"
                text="Scan"
                variant="outlined"
                rounded
                class="me-2"
                :disabled="!validateScanReady"
              ></v-btn>
              <v-btn
                text="Scan Configuration"
                variant="outlined"
                append-icon="mdi-download"
                rounded
              ></v-btn>
            </template>
          </v-card>

        </div>
        </v-card>
      </v-col>
      <v-col cols="12" md="4" />
    </v-row>
  </v-container>
</template>
<script >
  import { file } from '@babel/types';
import { defineComponent } from 'vue'
  import { useRoute } from 'vue-router';


export default defineComponent({

  setup(){      
    const route = useRoute()
    const router = useRouter()
    const projectId = route.params.id
    const error = ref("")
    const file = ref(null)
    const selectedScanOptions = ref([])

    function backToProjectOverview(){
      router.go(-1)
    }

    const validateScanReady = computed(() => {
      // TODO buggy
      console.log(file)
      let b = true;
      if (selectedScanOptions.value.length === 0) {
        error.value = "You need to select at least one scan type.";
        b = false;
      } else if (file.value === undefined) {
        error.value = "You need to attach a file.";
        b = false;
      } else {
        error.value = ""; 
      }
      return b;
    });

    return {
      projectId,
      scanOptions: ['Code Scan', 'License Scan'],
      selectedScanOptions,
      validateScanReady,
      backToProjectOverview
    }
  },

  methods: {
    toggleSelection(item) {
      const index = this.selectedScanOptions.indexOf(item)
      if(index === -1){
        this.selectedScanOptions.push(item)
      } else {
        this.selectedScanOptions.slice(index, 1)
      }
    },

    updateFileselection(newFile){
      file.value = newFile
    }
  },
})
</script>
<style scoped>
.background-color{
  background-color: rgb(var(--v-theme-layer_01)) !important;
}
</style>