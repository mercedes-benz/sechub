<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-card>
    <v-card-item>
      <v-card-title>{{ $t('USER_TITLE') }}</v-card-title>
    </v-card-item>
    <v-list lines="one">
      <v-list-item>
        <template #prepend>
          <v-icon class="v-list-item-icon" size="40">mdi-account-circle</v-icon>
        </template>
        <v-container>
          <v-list-item-title>{{ $t('USER_ID') }}</v-list-item-title>
          <v-list-item-subtitle>{{ userId }}</v-list-item-subtitle>
        </v-container>
      </v-list-item>
      <v-list-item>
        <template #prepend>
          <v-icon class="v-list-item-icon" size="40">mdi-email</v-icon>
        </template>
        <v-container>
          <v-list-item-title>{{ $t('USER_EMAIL') }}</v-list-item-title>
          <v-list-item-subtitle>{{ email }}</v-list-item-subtitle>
        </v-container>
      </v-list-item>
    </v-list>
  </v-card>

  <v-card>
    <v-card-item>
      <v-card-title>{{ $t('USER_SETTINGS') }}</v-card-title>
    </v-card-item>
    <v-list lines="one">
      <v-list-item>
        <v-container>
          <v-btn variant="tonal">
            {{ $t('USER_SETTINGS_CHANGE_PASSWORD') }}
          </v-btn>
        </v-container>
      </v-list-item>
      <v-list-item>
        <v-container>
          <v-btn :loading="isRefreshingApiToken" variant="tonal" @click="refreshApiToken()">
            {{ $t('USER_SETTINGS_REQUEST_NEW_API_TOKEN') }}
          </v-btn>

          <v-dialog v-model="refreshApiTokenDialog" max-width="500">
            <v-card>
              <v-card-title>{{ $t('USER_SETTINGS_REQUEST_NEW_API_TOKEN_DIALOG_TITLE') }}</v-card-title>
              <v-card-text>{{ $t('USER_SETTINGS_REQUEST_NEW_API_TOKEN_DIALOG_TEXT') }}</v-card-text>
              <v-card-actions>
                <v-btn color="primary" @click="refreshApiTokenDialog = false">{{ $t('USER_SETTINGS_REQUEST_NEW_API_TOKEN_DIALOG_CLOSE') }}</v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>
        </v-container>
      </v-list-item>
    </v-list>
  </v-card>

  <v-card>
    <v-card-item>
      <v-card-title>{{ $t('USER_SUPPORT_TITLE') }}</v-card-title>
    </v-card-item>
    <v-list lines="one">
      <v-list-item>
        <template #prepend>
          <v-icon class="v-list-item-icon" size="40">mdi-web</v-icon>
        </template>
        <v-container>
          <v-list-item-title>{{ $t('USER_SUPPORT_WEBSITE') }}</v-list-item-title>
          <v-list-item-subtitle><a href="https://www.sechub.com" target="_blank">www.sechub.com</a></v-list-item-subtitle>
        </v-container>
      </v-list-item>
      <v-list-item>
        <template #prepend>
          <v-icon class="v-list-item-icon" size="40">mdi-git</v-icon>
        </template>
        <v-container>
          <v-list-item-title>{{ $t('USER_SUPPORT_GITHUB') }}</v-list-item-title>
          <v-list-item-subtitle><a href="https://github.com/mercedes-benz/sechub" target="_blank">https://github.com/mercedes-benz/sechub</a></v-list-item-subtitle>
        </v-container>
      </v-list-item>
      <v-list-item>
        <template #prepend>
          <v-icon class="v-list-item-icon" size="40">mdi-email</v-icon>
        </template>
        <v-container>
          <v-list-item-title>{{ $t('USER_SUPPORT_EMAIL') }}</v-list-item-title>
          <v-list-item-subtitle><a href="mailto:dw_415_sechub-foss@mercedes-benz.com">dw_415_sechub-foss@mercedes-benz.com</a></v-list-item-subtitle>
        </v-container>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script lang="ts">
  import defaultClient from '@/services/defaultClient'
  import { defineComponent } from 'vue'
  import type { UserDetailInformation } from '@/generated-sources/openapi'

  export default defineComponent({
    name: 'UserDetailInformation',

    setup () {
      const userId = ref('')
      const email = ref('')
      const isRefreshingApiToken = ref(false)
      const refreshApiTokenDialog = ref(false)

      onMounted(async () => {
        try {
          const userDetailInformation: UserDetailInformation = await defaultClient.withUserSelfServiceApi.userFetchUserDetailInformation()
          userId.value = userDetailInformation.userId!
          email.value = userDetailInformation.email!
        } catch (error) {
          console.error(error)
        }
      })

      async function refreshApiToken (): Promise<void> {
        isRefreshingApiToken.value = true

        try {
          const emailValue = email.value
          if (emailValue) {
            await defaultClient.withSignUpApi.anonymousRefreshApiTokenByEmailAddress({
              emailAddress: emailValue,
            })
            refreshApiTokenDialog.value = true
          }
        } catch (error) {
          console.error('Failed to refresh API token:', error)
        } finally {
          isRefreshingApiToken.value = false
        }
      }

      return {
        userId,
        email,
        refreshApiToken,
        isRefreshingApiToken,
        refreshApiTokenDialog,
      }
    },
  })
</script>

<style scoped>
  .v-list-item__spacer {
    display: none !important;
    width: 0;
  }

  .v-card {
    margin-top: 25px;

    .v-btn {
      width: 300px;
    }
  }
</style>
