<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-dialog v-model="localVisible" max-width="80%" persistent>
    <v-card>
      <v-card-title>
        {{ $t('REPORT_EXPLAIN_FINDING') }}
      </v-card-title>
      <v-card-text>
        {{ localAiExplanation ? localAiExplanation : $t('REPORT_EXPLAIN_NO_EXPLANATION') }}
      </v-card-text>
      <v-card-actions>
        <v-btn @click="closeDialog">
          {{ $t('CONFIRM_DIALOG_BUTTON_CANCEL') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
  import { defineComponent } from 'vue'

  interface Props {
    aiExplanation: Object,
    visible: boolean,
  }

  export default defineComponent({
    props: {
      visible: {
        type: Boolean,
        required: true,
      },
      aiExplanation: {
        type: Object,
        required: true,
      },
    },

    emits: ['close'],

    setup (props: Props, { emit }) {
      const { aiExplanation, visible } = toRefs(props)
      const localVisible = ref(visible)
      const localAiExplanation = ref(aiExplanation)

      console.log(aiExplanation.value)

      const closeDialog = () => {
        emit('close')
      }

      return {
        localVisible,
        closeDialog,
        localAiExplanation,
      }
    },
  })
</script>
