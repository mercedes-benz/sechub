<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-dialog v-model="localVisible" max-width="600px" persistent>
    <v-card>
      <v-card-title>
        {{ $t('MARK_FALSE_POSITIVE_METHODS_SELECTION_DIALOG_TITLE') }}
      </v-card-title>
      <v-card-text>
        <v-checkbox
          v-for="method in httpMethods"
          :key="method"
          v-model="selectedMethods"
          :disabled="anyMethods"
          hide-details
          :label="method"
          :value="method"
        />
        <v-checkbox
          v-model="anyMethods"
          hide-details
          :label="$t('MARK_FALSE_POSITIVE_WEBSCAN_ANY_METHOD')"
        />
      </v-card-text>
      <v-card-actions>
        <v-btn @click="cancel">
          {{ $t('CONFIRM_DIALOG_BUTTON_CANCEL') }}
        </v-btn>
        <v-btn color="primary" @click="saveMethods()">
          {{ $t('CONFIRM_DIALOG_BUTTON_OK') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
  import { defineComponent } from 'vue'
  import { WebscanFalsePositiveProjectData } from 'sechub-openapi-typescript'

  interface Props {
    falsePositive: WebscanFalsePositiveProjectData,
    visible: boolean,
  }

  export default defineComponent({
    props: {
      visible: {
        type: Boolean,
        required: true,
      },
      falsePositive: {
        type: Object,
        required: true,
      },
    },

    emits: ['close'],

    setup (props: Props, { emit }) {
      const httpMethods = ['GET', 'POST', 'PUT', 'DELETE']
      const anyMethods = ref(false)
      const { falsePositive, visible } = toRefs(props)
      const selectedMethods = ref(falsePositive.value.methods)
      const localVisible = ref(visible)

      watch(localVisible, newValue => {
        if (newValue) {
          selectedMethods.value = falsePositive.value.methods
        }
        if (selectedMethods.value) {
          if (selectedMethods.value.length === 0) {
            anyMethods.value = true
          }
        }
      })

      const closeDialog = () => {
        emit('close')
      }

      const saveMethods = () => {
        if (anyMethods.value) {
          falsePositive.value.methods = []
        } else {
          falsePositive.value.methods = selectedMethods.value
        }
        closeDialog()
      }

      const cancel = () => {
        closeDialog()
      }

      return {
        localVisible,
        httpMethods,
        selectedMethods,
        anyMethods,
        closeDialog,
        cancel,
        saveMethods,
      }
    },
  })
</script>
