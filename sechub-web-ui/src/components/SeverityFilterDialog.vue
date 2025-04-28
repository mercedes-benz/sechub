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
        <v-card-text>
          <div v-for="(severity, i) in severities" 
          :key="i">
            <v-checkbox
              v-model="selected"
              :label="severity"
              :value="severity"
              hide-details
            />
          </div>
        </v-card-text>
        <v-card-actions>
          <v-btn
            class="sechub-dialog-close-btn"
            color="primary"
            @click="newFilter"
          >
            {{ $t('DIALOG_BUTTON_SAVE') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, toRefs } from 'vue'
import '@/styles/sechub.scss'

export default defineComponent({
  props: {
    visible: {
      type: Boolean,
      required: true,
    },
    severities: {
      type:  Array as () => string[],
      required: true,
    },
  },

  emits: ['filter'],

  setup (props, { emit }) {
    const { visible, severities } = toRefs(props)
    const localVisible = ref(visible)
    const selected = ref([])

    function newFilter () {
      emit('filter', selected.value)
    }

    return {
      localVisible,
      selected,
      severities,
      newFilter,
    }
  },
})
</script>