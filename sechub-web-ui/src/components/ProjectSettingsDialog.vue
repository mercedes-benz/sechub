<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-dialog v-model="showDialog" max-height="400" max-width="500">
    <v-alert
      v-if="settingsError!=undefined"
      type="error"
    >
      {{ settingsError }}
    </v-alert>
    <v-card>
      <v-card-title>
        <v-icon left size="small">mdi-pencil</v-icon>
        {{ $t('PROJECT_SETTINGS_DIALOG_TITLE') }}
      </v-card-title>
      <v-card>
        <v-responsive class="mx-auto" max-width="80%">
          <v-text-field
            v-model="settingsOwnerUserId"
            :append-icon="settingsOwnerFieldIcon"
            clearable
            hide-details="auto"
            :label="$t('PROJECT_SETTINGS_PROJECT_OWNER_LABEL')"
            @click:append="onSettingsChangeOwnerClicked"
            @vue:updated="onSettingsOwnerFieldChanged"
          />
        </v-responsive>
      </v-card>

      <v-card-actions>
        <v-btn color="primary" @click="closeDialog()">{{ $t('USER_SETTINGS_REQUEST_NEW_API_TOKEN_DIALOG_CLOSE') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
  import { defineComponent } from 'vue'
  import defaultClient from '@/services/defaultClient'
  import { useI18n } from 'vue-i18n'

  interface Props {
    projectId: string,
    currentOwnerUserId: string,
    visible: boolean,
  }
  export default defineComponent({
    props: {
      projectId: {
        type: String,
        required: true,
      },

      currentOwnerUserId: {
        type: String,
        required: true,
      },

      visible: {
        type: Boolean,
        required: true,
      },
    },

    emits: ['close', 'projectOwnerChanged'],

    setup (props: Props, { emit }) {
      const { visible } = toRefs(props)

      const { t } = useI18n()
      const projectId = ref(props.projectId)

      const showDialog = visible

      const settingsOwnerFieldIcon = ref('mdi-check')
      const settingsError = ref<string | undefined>(undefined)

      let originOwnerUserId = props.currentOwnerUserId
      const settingsOwnerUserId = ref('')

      settingsOwnerUserId.value = props.currentOwnerUserId

      function handleSettingsError (errMsg: string, err : unknown) {
        settingsError.value = errMsg
        console.error(errMsg, err)

        setTimeout(() => {
          settingsError.value = undefined
        }, 2000)
      }

      function clearSettingsErrors () {
        settingsError.value = undefined
      }

      async function onSettingsOwnerFieldChanged () {
        updateOwnerFieldIcon()
      }

      function updateOwnerFieldIcon () {
        if (settingsOwnerUserId.value === originOwnerUserId) {
          settingsOwnerFieldIcon.value = 'mdi-check'
        } else {
          settingsOwnerFieldIcon.value = 'mdi-send'
        }
      }

      async function onSettingsChangeOwnerClicked () {
        if (settingsOwnerUserId.value === originOwnerUserId) {
          return
        }
        try {
          await defaultClient.withProjectApi.adminOrOwnerChangesProjectOwner({
            projectId: projectId.value,
            userId: settingsOwnerUserId.value,
          })

          originOwnerUserId = settingsOwnerUserId.value

          updateOwnerFieldIcon()
          clearSettingsErrors()

          emit('projectOwnerChanged', originOwnerUserId)
        } catch (err) {
          const errMsg = t('PROJECT_SETTINGS_PROJECT_OWNER_CHANGE_FAILED')
          handleSettingsError(errMsg, err)
          settingsOwnerUserId.value = props.currentOwnerUserId
        }
      }

      async function closeDialog () {
        // reset dialog values to real values before close:
        settingsOwnerUserId.value = originOwnerUserId

        emit('close')
      }

      return {
        showDialog,
        settingsOwnerUserId,
        onSettingsOwnerFieldChanged,
        settingsOwnerFieldIcon,
        onSettingsChangeOwnerClicked,
        settingsError,
        closeDialog,
      }
    },
  })
</script>
