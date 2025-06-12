<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-dialog
    v-model="localVisible"
    max-width="50%"
  >

    <ConfirmationDialog
      :message="confirmationMessage"
      :on-cancel="cancelAction"
      :on-confirm="confirmAction"
      :title="confirmationTitle"
      :visible="confirmationVisible"
    />

    <v-alert
      v-if="settingsError!=undefined"
      closable
      color="error"
      density="compact"
      type="warning"
    >
      {{ settingsError }}
    </v-alert>

    <v-card>
      <v-card-title>
        <v-icon
          class="mr-2"
          icon="mdi-cog"
          left
          size="small"
        />
        {{ $t('PROJECT_SETTINGS_DIALOG_TITLE') }}
      </v-card-title>
      <v-card-text>

        <!-- Project owner card -->
        <v-card
          variant="flat"
        >
          <v-card-subtitle>{{ $t('PROJECT_SETTINGS_DIALOG_OWNER_MANAGEMENT') }}</v-card-subtitle>
          <v-table>
            <tbody>
              <tr v-if="isEditingOwner">
                <td>
                  <v-icon
                    icon="mdi-account"
                  />
                </td>
                <td class="user-role">{{ $t('PROJECT_SETTINGS_DIALOG_PROJECT_OWNER') }}</td>
                <td class="wide-column">
                  <v-text-field
                    v-model="newOwnerId"
                    :label="$t('PROJECT_SETTINGS_PROJECT_OWNER_LABEL')"
                    variant="underlined"
                  />
                </td>
                <td class="text-right">
                  <v-btn
                    :disabled="newOwnerId === projectData.owner.userId"
                    icon="mdi-check"
                    variant="text"
                    @click="onSettingsChangeOwnerClicked"
                  />
                  <v-btn
                    icon="mdi-close"
                    variant="text"
                    @click="editOwner"
                  />
                </td>
              </tr>
              <tr v-else>
                <td>
                  <v-icon

                    icon="mdi-account"
                  />
                </td>
                <td class="user-role">
                  <div>
                    {{ $t('PROJECT_SETTINGS_DIALOG_PROJECT_OWNER') }}
                  </div>
                </td>
                <td class="wide-column">
                  <div>{{ newOwnerId }}</div>
                </td>
                <td class="text-right">
                  <v-btn
                    v-tooltip="$t('PROJECT_SETTINGS_OWNER_CHANGE_TOOLTIP')"
                    icon="mdi-pencil"
                    variant="text"
                    @click="editOwner"
                  />
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card>

        <!-- Project users card -->
        <v-card
          variant="flat"
        >
          <v-card-subtitle
            class="mt-4"
          >{{ $t('PROJECT_SETTINGS_DIALOG_USER_MANAGEMENT') }}</v-card-subtitle>

          <v-table>
            <tbody>
              <tr
                v-for="(user, i) in projectData.assignedUsers"
                :key="i"
              >
                <td>
                  <v-icon
                    icon="mdi-account"
                  />
                </td>
                <td class="user-role">
                  <div v-if="user.userId !== projectData.owner.userId">
                    {{ $t('PROJECT_SETTINGS_DIALOG_PROJECT_MEMBER') }}
                  </div>
                  <div v-else>
                    {{ $t('PROJECT_SETTINGS_DIALOG_PROJECT_OWNER') }}
                  </div>
                </td>
                <td class="wide-column">
                  <div>{{ user.userId }}</div>
                </td>
                <td class="text-right">
                  <v-btn
                    v-tooltip="$t('PROJECT_SETTINGS_USER_REMOVE_TOOLTIP')"
                    icon="mdi-close"
                    variant="text"
                    @click="onUnassignUserClicked(user.userId)"
                  />
                </td>
              </tr>

              <tr v-if="isAddingMember">
                <td>
                  <v-icon
                    icon="mdi-account-plus"
                  />
                </td>
                <td class="user-role">project member</td>
                <td class="wide-column">
                  <v-text-field
                    v-model="newMemberId"
                    :label="$t('PROJECT_SETTINGS_PROJECT_USER_LABEL')"
                    variant="underlined"
                  />
                </td>
                <td class="text-right">
                  <v-btn
                    :disabled="newMemberId === ''"
                    icon="mdi-check"
                    variant="text"
                    @click="onAssignUserClicked()"
                  />
                  <v-btn
                    icon="mdi-close"
                    variant="text"
                    @click="editMember"
                  />
                </td>
              </tr>
              <tr v-else>
                <td>
                  <v-icon icon="mdi-account-plus" />
                </td>
                <td class="user-role" />
                <td class="wide-column" />
                <td class="text-right">
                  <v-btn
                    v-tooltip="$t('PROJECT_SETTINGS_USER_ADD_TOOLTIP')"
                    icon="mdi-plus"
                    variant="text"
                    @click="editMember"
                  />
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card>

      </v-card-text>
      <v-card-actions>
        <v-btn
          class="sechub-dialog-close-btn"
          color="primary"
          @click="closeDialog()"
        >
          {{ $t('DIALOG_BUTTON_CLOSE') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
  import { defineComponent } from 'vue'
  import defaultClient from '@/services/defaultClient'
  import { useI18n } from 'vue-i18n'
  import { ProjectData, UserDetailInformation } from '@/generated-sources/openapi'
  import { useUserDetailInformationStore } from '@/stores/userDetailInformationStore'
  import '@/styles/sechub.scss'
  import { handleApiError } from '@/services/apiErrorHandler'

  interface Props {
    projectData: ProjectData,
    visible: boolean,
  }

  export default defineComponent({
    props: {
      projectData: {
        type: Object,
        required: true,
      },
      visible: {
        type: Boolean,
        required: true,
      },
    },

    emits: ['close', 'projectChanged'],

    setup (props: Props, { emit }) {
      const { t } = useI18n()
      const { visible, projectData } = toRefs(props)
      const localVisible = ref(visible)
      const showToggle = ref(false)

      const userDetailInformation = ref<UserDetailInformation>({})
      const store = useUserDetailInformationStore()

      const projectId = ref(projectData.value.projectId)

      const newOwnerId = ref('')
      newOwnerId.value = projectData.value.owner.userId

      const isAddingMember = ref(false)
      const isEditingOwner = ref(false)
      const newMemberId = ref('')

      const settingsError = ref<string | undefined>(undefined)

      const confirmationVisible = ref(false)
      const confirmationMessage = ref('')
      const confirmationTitle = ref('')
      let pendingAction: (() => Promise<void>) | null = null

      fetchUserDetailFromStorage()

      async function fetchUserDetailFromStorage () {
        const user = store.getUserDetailInformation()
        if (user) {
          userDetailInformation.value = user
        }
      }

      function handleSettingsError (errMsg: string, err : unknown) {
        settingsError.value = errMsg
        console.error(errMsg, err)
      }

      function clearSettingsErrors () {
        settingsError.value = undefined
      }

      function showConfirmationDialog (title: string, message: string, action: () => Promise<void>) {
        confirmationTitle.value = title
        confirmationMessage.value = message
        pendingAction = action
        confirmationVisible.value = true
      }

      function confirmAction () {
        if (pendingAction) {
          pendingAction()
        }
        confirmationVisible.value = false
      }

      function cancelAction () {
        confirmationVisible.value = false
        pendingAction = null
      }

      async function closeDialog () {
        clearSettingsErrors()
        isAddingMember.value = false
        isEditingOwner.value = false
        emit('close')
      }

      function editMember () {
        isAddingMember.value = !isAddingMember.value
        newMemberId.value = ''
      }

      function editOwner (reset: boolean = true) {
        isEditingOwner.value = !isEditingOwner.value
        if (reset) {
          newOwnerId.value = projectData.value.owner.userId
        }
      }

      async function onSettingsChangeOwnerClicked () {
        clearSettingsErrors()

        const oldOwnerId = projectData.value.owner.userId
        if (newOwnerId.value === oldOwnerId) {
          return
        }

        const title = t('PROJECT_SETTINGS_CONFIRM_OWNER_CHANGE_TITLE')
        let message = t('PROJECT_SETTINGS_CONFIRM_OWNER_CHANGE_TEXT')
        if ((oldOwnerId === userDetailInformation.value.userId) && !(userDetailInformation.value.superAdmin)) {
          // current owner is no admin and changes ownership
          const additionalMessage = t('PROJECT_SETTINGS_CONFIRM_OWNER_CHANGE_TEXT_ADDITIONAL')
          message = message + '\n' + additionalMessage
        }

        showConfirmationDialog(
          title,
          message,
          async () => {
            try {
              await defaultClient.withProjectApi.adminOrOwnerChangesProjectOwner({
                projectId: projectId.value,
                userId: newOwnerId.value,
              })

              console.debug('Project owner for project', projectId, 'changed to', newOwnerId.value)

              emit('projectChanged')
              editOwner(false)
            } catch (err) {
              handleApiError(err)
              const errMsg = t('PROJECT_SETTINGS_PROJECT_OWNER_CHANGE_FAILED')
              handleSettingsError(errMsg, err)
              editOwner()
            }
          }
        )
      }

      async function onAssignUserClicked () {
        clearSettingsErrors()

        try {
          await defaultClient.withProjectApi.adminOrOwnerAssignUserToProject({
            projectId: projectId.value,
            userId: newMemberId.value,
          })
          emit('projectChanged')
          newMemberId.value = ''
        } catch (err) {
          handleApiError(err)
          const errMsg = t('PROJECT_SETTINGS_PROJECT_ASSIGN_USER_FAILED')
          handleSettingsError(errMsg, err)
        }
      }

      async function onUnassignUserClicked (userId: string) {
        clearSettingsErrors()

        const title = t('PROJECT_SETTINGS_CONFIRM_REMOVE_USER_TITLE')
        let message = ''
        if (userId === userDetailInformation.value.userId) {
          message = t('PROJECT_SETTINGS_CONFIRM_REMOVE_USER_TEXT_ALTERNATIVE')
        } else {
          message = t('PROJECT_SETTINGS_CONFIRM_REMOVE_USER_TEXT')
        }

        showConfirmationDialog(
          title,
          message,
          async () => {
            try {
              await defaultClient.withProjectApi.adminOrOwnerUnassignUserFromProject({
                projectId: projectId.value,
                userId,
              })
              emit('projectChanged')
            } catch (err) {
              handleApiError(err)
              const errMsg = t('PROJECT_SETTINGS_PROJECT_UNASSIGN_USER_FAILED')
              handleSettingsError(errMsg, err)
            }
          }
        )
      }

      return {
        localVisible,
        newOwnerId,
        settingsError,
        isAddingMember,
        isEditingOwner,
        newMemberId,
        confirmationTitle,
        confirmationVisible,
        confirmationMessage,
        userDetailInformation,
        showToggle,
        onSettingsChangeOwnerClicked,
        closeDialog,
        editMember,
        editOwner,
        onUnassignUserClicked,
        onAssignUserClicked,
        confirmAction,
        cancelAction,
      }
    },
  })
</script>
<style scoped>
.v-btn {
    color: rgb(var(--v-theme-primary)) !important;
    margin: 2px;
  }

  .wide-column{
    width: 50%;
  }
</style>
