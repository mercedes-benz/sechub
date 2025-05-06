<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-dialog
    v-model="localVisible"
    max-width="50%"
  >
    <v-card>
      <v-card-title>
        {{ $t('MARK_FALSE_POSITIVE_TITLE') }}
      </v-card-title>
      <v-card-subtitle
        class="ma-5"
      >
        {{ $t('MARK_FALSE_POSITIVE_SUBTITLE') }}
      </v-card-subtitle>

      <v-card
        variant="flat"
      >
        <v-table
          height="300px"
        >
          <thead class="sechub-primary-color-report">
            <tr>              <th>{{ $t('MARK_FALSE_POSITIVE_WEBSCAN_CALCULATED_PATTERN') }}</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="(falsPositive, i) in falsePositiveJobData"
              :key="i"
              class="background-color"
            >
              <td>
                <v-text-field
                  v-if="falsPositive.webScan?.urlPattern"
                  v-model="falsPositive.webScan.urlPattern"
                  variant="underlined"
                />
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-card>

      <v-card
        variant="flat"
      >
        <v-radio-group v-model="radios">
          <v-radio
            color="primary"
            :label="$t('MARK_FALSE_POSITIVE_RADIO_ONE')"
            :value="$t('MARK_FALSE_POSITIVE_RADIO_ONE')"
          />
          <v-radio
            color="primary"
            :label="$t('MARK_FALSE_POSITIVE_RADIO_TWO')"
            :value="$t('MARK_FALSE_POSITIVE_RADIO_TWO')"
          />
          <v-radio
            color="primary"
            :label="$t('MARK_FALSE_POSITIVE_RADIO_THREE')"
            :value="$t('MARK_FALSE_POSITIVE_RADIO_THREE')"
          />
          <v-radio
            color="primary"
            :label="$t('MARK_FALSE_POSITIVE_RADIO_FOUR')"
            :value="$t('MARK_FALSE_POSITIVE_RADIO_FOUR')"
          />
          <v-radio
            color="primary"
            :label="$t('MARK_FALSE_POSITIVE_RADIO_FIVE')"
            :value="$t('MARK_FALSE_POSITIVE_RADIO_FIVE')"
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
        <v-btn
          @click="closeDialog"
        >
          {{ $t('CONFIRM_DIALOG_BUTTON_CANCEL') }}
        </v-btn>
        <v-btn
          color="primary"
          @click="markAsFalsePositive"
        >
          {{ $t('CONFIRM_DIALOG_BUTTON_OK') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
  import { defineComponent, ref, toRefs } from 'vue'
  import { useI18n } from 'vue-i18n'
  import defaultClient from '@/services/defaultClient'
  import { FalsePositiveProjectData, FalsePositives, SecHubFinding, UserMarkFalsePositivesRequest, WebscanFalsePositiveProjectData } from '@/generated-sources/openapi'
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
    },

    emits: ['close', 'errorAlert', 'successAlert'],

    setup (props, { emit }) {
      const { visible, projectId, selectedFindings, jobUUID } = toRefs(props)
      const localVisible = ref(visible)
      const { t } = useI18n()
      const comment = ref('')
      const radios = ref(t('MARK_FALSE_POSITIVE_RADIO_ONE'))

      const falsePositiveJobData = ref([] as FalsePositiveProjectData[])

      // calculate the FalsePositives when open the Dialog
      watch(localVisible, newValue => {
        if (newValue) {
          falsePositiveJobData.value = []
          collectFalsePositiveJobData()
          console.log(falsePositiveJobData)
        }
      })

      async function markAsFalsePositive () {
        const falsePositives: FalsePositives = {
          apiVersion: '1.0',
          type: 'falsePositiveDataList',
          projectData: falsePositiveJobData.value,
        }

        const requestBody: UserMarkFalsePositivesRequest = {
          projectId: projectId.value,
          falsePositives,
        }

        console.log(JSON.stringify(requestBody))
        try {
          await defaultClient.withSechubExecutionApi.userMarkFalsePositives(requestBody)
          emit('successAlert')
          emit('close', true)
        } catch (err) {
          console.error(err)
          emit('errorAlert')
          emit('close', false)
        }
      }

      function collectFalsePositiveJobData () {
        const patterns: Array<string> = []

        selectedFindings.value.forEach(finding => {
          const methods: Array<string> = []

          const method = finding.web?.request?.method
          if (method) {
            methods.push(method)
          }

          let newPattern = finding.web?.request?.target
          if (newPattern) {
            newPattern = maskUrlParams(newPattern)
            if (!patterns.includes(newPattern)) {
              patterns.push(newPattern)
            } else {
              return
            }
          } else {
            return
          }

          const webScan: WebscanFalsePositiveProjectData = {
            cweId: finding.cweId || 0,
            urlPattern: newPattern,
            methods,
          }

          const id = jobUUID.value + '-' + finding.id

          const data: FalsePositiveProjectData = {
            id,
            comment: createComment(),
            webScan,
          }

          falsePositiveJobData.value.push(data)
        })
      }

      function createComment () {
        const radioComment = radios.value || ''
        const textAreaComment = comment.value || ''

        if (radioComment && textAreaComment) {
          return `${radioComment}, ${textAreaComment}`
        }

        return radioComment + textAreaComment
      }

      function maskUrlParams (url: string): string {
        try {
          const urlObj = new URL(url)
          urlObj.searchParams.forEach((_, key) => {
            urlObj.searchParams.set(key, '*')
          })
          return urlObj.toString()
        } catch (error) {
          console.error('Invalid URL:', error)
          return url
        }
      }

      function closeDialog () {
        emit('close')
      }

      return {
        localVisible,
        comment,
        radios,
        falsePositiveJobData,
        t,
        closeDialog,
        markAsFalsePositive,
      }
    },
  })
</script>
