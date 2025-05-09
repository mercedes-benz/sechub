<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-dialog v-model="localVisible" max-width="50%">

    <v-alert
      v-if="successRemoveAlert"
      closable
      color="success"
      density="compact"
      type="info"
      @click:close="successRemoveAlert=false"
    >
      {{ $t('MARK_FALSE_POSITIVE_WEBSCAN_CALCULATED_PATTERN_DELETE') }}
    </v-alert>

    <v-alert
      v-if="infoNoCWEIDAlert"
      closable
      color="primary"
      density="compact"
      :text="infoNoCWEIDAlertMessage"
      type="info"
      @click:close="infoNoCWEIDAlert=false"
    />

    <v-card>
      <v-card-title>
        {{ $t('MARK_FALSE_POSITIVE_TITLE') }}
        <v-tooltip
          v-if="isWebScan"
          v-model="showInfoToggle"
          location="right"
        >
          <template #activator="{ props }">
            <div v-bind="props" class="d-inline-block">
              <v-icon
                color="primary"
                icon="mdi-information-outline"
                variant="text"
              />
            </div>
          </template>
          <span>{{ $t('MARK_FALSE_POSITIVE_WEBSCAN_INFORMATION_TOOLTIP') }}</span>
        </v-tooltip>
      </v-card-title>

      <v-card
        v-if="isWebScan"
        class="mt-4"
        variant="flat"
      >
        <v-table class="false-positive-webscan-pattern-table">
          <thead class="sechub-primary-color-report">
            <tr>
              <th>{{ $t('MARK_FALSE_POSITIVE_WEBSCAN_CALCULATED_METHODS') }}</th>
              <th>{{ $t('MARK_FALSE_POSITIVE_WEBSCAN_CALCULATED_PATTERN') }}</th>
              <th>{{ $t('MARK_FALSE_POSITIVE_WEBSCAN_CALCULATED_CWEID') }}</th>
              <th />
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(falsePositive, i) in webScanFalsePositives"
              :key="i"
              class="background-color"
            >
              <td>
                {{ falsePositive.methods ? (falsePositive.methods.length ? falsePositive.methods.join(', ') : $t('MARK_FALSE_POSITIVE_WEBSCAN_ANY_METHOD')) : '' }}
                <v-btn
                  v-tooltip="$t('MARK_FALSE_POSITIVE_WEBSCAN_EDIT_TOOLTIP')"
                  color="primary"
                  icon="mdi-pencil"
                  size="small"
                  variant="text"
                  @click="modifyMethods(falsePositive)"
                />
              </td>
              <td>
                <v-text-field
                  v-if="falsePositive.urlPattern"
                  v-model="falsePositive.urlPattern"
                  class="mt-2"
                  hide-details
                  variant="outlined"
                />
              </td>
              <td>{{ falsePositive.cweId }}</td>
              <td>
                <v-btn
                  v-tooltip="$t('MARK_FALSE_POSITIVE_WEBSCAN_TOOLTIP_DELETE')"
                  color="primary"
                  icon="mdi-close"
                  size="small"
                  variant="text"
                  @click="deleteFalsePositiveEntry(falsePositive)"
                />
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-card>

      <v-card
        variant="flat"
      >
        <v-radio-group
          v-model="radios"
          hide-details
        >
          <v-radio
            v-for="i in 5"
            :key="i"
            color="primary"
            :label="$t(`MARK_FALSE_POSITIVE_RADIO_${i}`)"
            :value="$t(`MARK_FALSE_POSITIVE_RADIO_${i}`)"
          />
        </v-radio-group>

        <v-textarea
          v-model="comment"
          class="ma-5"
          counter
          label="Additional Description"
          maxlength="120"
          single-line
          variant="outlined"
        />
      </v-card>

      <v-card-actions>
        <v-btn @click="closeDialog">
          {{ $t('CONFIRM_DIALOG_BUTTON_CANCEL') }}
        </v-btn>
        <v-btn color="primary" @click="markAsFalsePositive">
          {{ $t('CONFIRM_DIALOG_BUTTON_OK') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <FalsePositivesMethodsDialog
    :false-positive="currentFalsePositive"
    :visible="webScanMethodsDialog"
    @close="webScanMethodsDialog=false"
  />
</template>

  <script lang="ts">
  import { defineComponent, ref, toRefs, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useMarkAsFalsePositive } from '@/composables/useMarkFalsePositives'
  import falsePositiveCreationService from '@/services/falsePositiveCreationService'
  import {
    FalsePositives,
    SecHubFinding,
    WebscanFalsePositiveProjectData,
  } from '@/generated-sources/openapi'
  import '@/styles/sechub.scss'

  export default defineComponent({
    props: {
      visible: {
        type: Boolean,
        required: true,
      },
      projectId: {
        type: String,
        required: true,
      },
      jobUUID: {
        type: String,
        required: true,
      },
      selectedFindings: {
        type: Array<SecHubFinding>,
        required: true,
      },
      isWebScan: {
        type: Boolean,
        required: true,
      },
    },

    emits: ['close', 'errorAlert', 'successAlert'],

    setup (props, { emit }) {
      const { visible, projectId, selectedFindings, jobUUID, isWebScan } = toRefs(props)
      const localVisible = ref(visible)
      const { t } = useI18n()
      const showInfoToggle = ref(false)
      const successRemoveAlert = ref(false)
      const comment = ref('')
      const radios = ref(t('MARK_FALSE_POSITIVE_RADIO_1'))

      // constants for false positives web methods
      const webScanFalsePositives = ref([] as WebscanFalsePositiveProjectData [])
      const webScanMethodsDialog = ref(false)
      const infoNoCWEIDAlert = ref(false)
      const infoNoCWEIDAlertMessage = ref('')
      const currentFalsePositive = ref<WebscanFalsePositiveProjectData>({
        cweId: 0,
        urlPattern: '',
        methods: [],
      })

      watch(localVisible, newValue => {
        if (newValue && isWebScan.value) {
          const { calculatedFalsePositives, findingsWithNoCWEID } = falsePositiveCreationService.calculateWebScanFalsePositivesProjectData(selectedFindings.value)
          webScanFalsePositives.value = calculatedFalsePositives
          if (findingsWithNoCWEID.length > 0) {
            infoNoCWEIDAlert.value = true
            infoNoCWEIDAlertMessage.value = t('MARK_FALSE_POSITIVE_WEBSCAN_NO_CWEID') + findingsWithNoCWEID.join(', ')
          } else {
            infoNoCWEIDAlert.value = false
            infoNoCWEIDAlertMessage.value = ''
          }
        }
      })

      async function markAsFalsePositive () {
        let falsePositive: FalsePositives = {}
        if (isWebScan.value) {
          falsePositive = falsePositiveCreationService.createWebScanFalsePositives(webScanFalsePositives.value, jobUUID.value, radios.value, comment.value)
        } else {
          falsePositive = falsePositiveCreationService.createFalsePositives(selectedFindings.value, jobUUID.value, radios.value, comment.value)
        }

        const isSuccess = await useMarkAsFalsePositive(projectId.value, falsePositive)
        if (isSuccess) {
          emit('successAlert')
          emit('close', true)
        } else {
          emit('errorAlert')
          emit('close', false)
        }
      }

      function closeDialog () {
        emit('close')
      }

      function deleteFalsePositiveEntry (data: WebscanFalsePositiveProjectData) {
        webScanFalsePositives.value = webScanFalsePositives.value.filter(entry => entry !== data)
        successRemoveAlert.value = true
        setTimeout(() => {
          successRemoveAlert.value = false
        }, 1000)
      }

      const modifyMethods = (falsePositive: WebscanFalsePositiveProjectData) => {
        currentFalsePositive.value = falsePositive
        webScanMethodsDialog.value = true
      }

      return {
        localVisible,
        comment,
        radios,
        webScanFalsePositives,
        webScanMethodsDialog,
        currentFalsePositive,
        showInfoToggle,
        successRemoveAlert,
        infoNoCWEIDAlert,
        infoNoCWEIDAlertMessage,
        t,
        closeDialog,
        markAsFalsePositive,
        deleteFalsePositiveEntry,
        modifyMethods,
      }
    },
  })
  </script>

  <style scoped>
  .false-positive-webscan-pattern-table .v-table__wrapper > table > tbody > tr:not(:last-child) > td,
  .v-table .v-table__wrapper > table > tbody > tr:not(:last-child) > th {
    border-bottom: none;
  }

  .false-positive-webscan-pattern-table {
    max-height: 200px;
    margin-bottom: 50px;
  }
  </style>
