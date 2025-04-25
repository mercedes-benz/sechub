<!-- SPDX-License-Identifier: MIT -->
<template>
  <div class="pa-4 text-center">
    <v-dialog
      v-model="localVisible"
        max-width="50%"
        width="33%"
    >
      <v-card>
        <v-card-title>
            <v-icon
            icon="mdi-filter-variant"
            class="mr-2"
            left
            size="small"
            />
            {{ $t('REPORT_FINDING_SEVERITY_FILTER_TITLE') }}
        </v-card-title>
        <v-card-text >

          <v-checkbox
            v-model="selected"
            label="Critical"
            value="CRITICAL"
            hide-details
          />

          <v-checkbox
            v-model="selected"
            label="High"
            value="HIGH"
            hide-details
          />

          <v-checkbox
            v-model="selected"
            label="Medium"
            value="MEDIUM"
            hide-details
          />

          <v-checkbox
            v-model="selected"
            label="Low"
            value="LOW"
            hide-details
          />

          <v-checkbox
            v-model="selected"
            label="Info"
            value="INFO"
          />

        </v-card-text>
        <v-card-actions>
            <v-btn
            class="sechub-dialog-close-btn"
            color="primary"
            @click="newFilter()"
            >
            {{ $t('DIALOG_BUTTON_SAVE') }}
            </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue'
  import '@/styles/sechub.scss'

  export default defineComponent({
    props: {
      visible: {
        type: Boolean,
        required: true,
      },
    },

    emits: ['filter'],

    setup (props, { emit }) {
      const { visible } = toRefs(props)
      const localVisible = ref(visible)

      const selected = ref([])

      function newFilter () {
        emit('filter', selected.value)
      }

      return {
        localVisible,
        selected,
        newFilter,
      }
    },
  })
</script>