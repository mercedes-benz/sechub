<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-dialog
    v-model="localVisible"
    max-width="400"
  >
    <v-card>
      <v-card-title>
        {{ title }}
      </v-card-title>
      <v-card-text>
        <span>{{ message }}</span>
      </v-card-text>
      <v-card-actions>
        <v-btn
          @click="cancel"
        >
          {{ $t('CONFIRM_DIALOG_BUTTON_CANCEL') }}
        </v-btn>
        <v-btn
          color="primary"
          @click="confirm"
        >
          {{ $t('CONFIRM_DIALOG_BUTTON_OK') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
  import { defineComponent, toRefs } from 'vue'
  import { useI18n } from 'vue-i18n'

  export default defineComponent({

    props: {
      title: {
        type: String,
        required: true,
      },
      visible: {
        type: Boolean,
        required: true,
      },
      message: {
        type: String,
        required: true,
      },
      onConfirm: {
        type: Function,
        required: true,
      },
      onCancel: {
        type: Function,
        required: true,
      },
    },

    setup (props) {
      const { visible, onConfirm, onCancel } = toRefs(props)
      const { t } = useI18n()

      const localVisible = ref(visible)

      function confirm () {
        onConfirm.value()
      }

      function cancel () {
        onCancel.value()
      }

      return {
        localVisible,
        confirm,
        cancel,
        t,
      }
    },
  })
</script>
