<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-row>
    <v-col cols="12" md="8">
      <v-card class="mr-auto" color="background_paper">
        <v-toolbar color="background_paper">
          <v-toolbar-title>
            {{ $t('USER_PROFILE_SETTINGS') }}
          </v-toolbar-title>
          <template #prepend>
            <v-btn
              icon="mdi-arrow-left"
              @click="router.go(-1)"
            />
          </template>
        </v-toolbar>
      </v-card>

      <v-card variant="flat">
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

      <v-card
        class="user-detail-card"
        variant="flat"
      >
        <v-card-item>
          <v-card-title>{{ $t('USER_SETTINGS') }}</v-card-title>
        </v-card-item>
        <v-list lines="one">
          <v-list-item v-if="false">
            <v-container>
              <v-btn
                class="custom-btn"
                variant="tonal"
              >
                {{ $t('USER_SETTINGS_CHANGE_PASSWORD') }}
              </v-btn>
            </v-container>
          </v-list-item>
          <v-list-item>
            <v-container>
              <v-btn
                class="sechub-dialog-close-btn"
                color="primary"
                :loading="isRefreshingApiToken"
                variant="tonal"
                @click="refreshApiToken()"
              >
                {{ $t('USER_SETTINGS_REQUEST_NEW_API_TOKEN') }}
              </v-btn>

              <v-dialog
                v-model="refreshApiTokenDialog"
                max-width="500"
              >
                <v-card>
                  <v-card-title>{{ $t('USER_SETTINGS_REQUEST_NEW_API_TOKEN_DIALOG_TITLE') }}</v-card-title>
                  <v-card-text>{{ $t('USER_SETTINGS_REQUEST_NEW_API_TOKEN_DIALOG_TEXT') }}</v-card-text>
                  <v-card-actions>
                    <v-btn
                      class="sechub-dialog-close-btn"
                      color="primary"
                      @click="refreshApiTokenDialog = false"
                    >
                      {{ $t('DIALOG_BUTTON_CLOSE') }}</v-btn>
                  </v-card-actions>
                </v-card>
              </v-dialog>
            </v-container>
          </v-list-item>
        </v-list>
      </v-card>

      <v-card
        class="user-detail-card"
        variant="flat"
      >
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
              <v-list-item-subtitle><a :href="userSupportWebsite" target="_blank">{{ userSupportWebsite }}</a></v-list-item-subtitle>
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
              <v-list-item-subtitle><a :href="'mailto:' + userSupportEmail">{{ userSupportEmail }}</a></v-list-item-subtitle>
            </v-container>
          </v-list-item>
        </v-list>
      </v-card>
    </v-col>
  </v-row>
</template>

<script lang="ts">
  import defaultClient from '@/services/defaultClient'
  import { defineComponent } from 'vue'
  import type { UserDetailInformation } from 'sechub-openapi-typescript'
  import { useConfig } from '@/config'
  import { useRouter } from 'vue-router'
  import { useUserDetailInformationStore } from '@/stores/userDetailInformationStore'
  import '@/styles/sechub.scss'

  export default defineComponent({
    name: 'UserDetailInformation',

    setup () {
      const router = useRouter()
      const config = useConfig()
      const store = useUserDetailInformationStore()

      const userId = ref('')
      const email = ref('')
      const isRefreshingApiToken = ref(false)
      const refreshApiTokenDialog = ref(false)

      const userSupportEmail = ref('')
      const userSupportWebsite = ref('')
      userSupportEmail.value = config.value.SECHUB_USER_SUPPORT_EMAIL
      userSupportWebsite.value = config.value.SECHUB_USER_SUPPORT_WEBSITE

      userFetchUserDetailInformationFromStore()

      async function userFetchUserDetailInformationFromStore () {
        const userDetail: UserDetailInformation = store.getUserDetailInformation()

        if (userDetail) {
          userId.value = userDetail.userId || ''
          email.value = userDetail.email || ''
        }
      }

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
        router,
        refreshApiToken,
        isRefreshingApiToken,
        refreshApiTokenDialog,
        userSupportEmail,
        userSupportWebsite,
      }
    },
  })
</script>

<style scoped>
  .v-list-item__spacer {
    display: none !important;
    width: 0;
  }
</style>
