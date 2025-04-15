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
                    color="primary"
                    icon="mdi-account"
                  />
                </td>
                <td class="user-role">project owner</td>
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
                    color="primary"
                    icon="mdi-account"
                  />
                </td>
                <td class="user-role">project owner</td>
                <td class="wide-column">
                  {{ newOwnerId }}
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
                <td class="user-role">project member</td>
                <td class="wide-column">
                  <span>{{ user.userId }}</span>

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
          {{ $t('USER_SETTINGS_REQUEST_NEW_API_TOKEN_DIALOG_CLOSE') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
  import { defineComponent } from 'vue'
  import defaultClient from '@/services/defaultClient'
  import { useI18n } from 'vue-i18n'
  import { ProjectData } from '@/generated-sources/openapi'
  import '@/styles/sechub.scss'

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

      const projectId = ref(projectData.value.projectId)

      const newOwnerId = ref('')
      newOwnerId.value = projectData.value.owner.userId

      const isAddingMember = ref(false)
      const isEditingOwner = ref(false)
      const newMemberId = ref('')

      const settingsError = ref<string | undefined>(undefined)

      const confirmationVisible = ref(false)
      const confirmationMessage = ref('')
      let pendingAction: (() => Promise<void>) | null = null

      function handleSettingsError (errMsg: string, err : unknown) {
        settingsError.value = errMsg
        console.error(errMsg, err)
      }

      function clearSettingsErrors () {
        settingsError.value = undefined
      }

      function showConfirmationDialog (message: string, action: () => Promise<void>) {
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

      async function onSettingsChangeOwnerClicked () {
        clearSettingsErrors()

        if (newOwnerId.value === projectData.value.owner.userId) {
          return
        }

        showConfirmationDialog(
          t('PROJECT_SETTINGS_CONFIRM_OWNER_CHANGE'),
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
              const errMsg = t('PROJECT_SETTINGS_PROJECT_OWNER_CHANGE_FAILED')
              handleSettingsError(errMsg, err)
              editOwner()
            }
          }
        )
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
          const errMsg = t('PROJECT_SETTINGS_PROJECT_ASSIGN_USER_FAILED')
          handleSettingsError(errMsg, err)
        }
      }

      async function onUnassignUserClicked (userId: string) {
        clearSettingsErrors()

        showConfirmationDialog(
          t('PROJECT_SETTINGS_CONFIRM_REMOVE_USER'),
          async () => {
            try {
              await defaultClient.withProjectApi.adminOrOwnerUnassignUserFromProject({
                projectId: projectId.value,
                userId,
              })
              emit('projectChanged')
            } catch (err) {
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
        confirmationVisible,
        confirmationMessage,
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
