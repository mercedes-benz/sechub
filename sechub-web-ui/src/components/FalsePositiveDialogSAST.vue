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
  import { FalsePositiveJobData, FalsePositives, SecHubFinding, UserMarkFalsePositivesRequest } from '@/generated-sources/openapi'
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

      async function markAsFalsePositive () {
        const falsePositiveJobData: Array<FalsePositiveJobData> = []
        selectedFindings.value.forEach(finding => {
          const data: FalsePositiveJobData = {
            findingId: finding.id,
            jobUUID: jobUUID.value,
            comment: createComment(),
          }
          falsePositiveJobData.push(data)
        })

        const falsePositives: FalsePositives = {
          apiVersion: '1.0',
          type: 'falsePositiveDataList',
          jobData: falsePositiveJobData,
        }

        const requestBody: UserMarkFalsePositivesRequest = {
          projectId: projectId.value,
          falsePositives,
        }

        try {
          await defaultClient.withSechubExecutionApi.userMarkFalsePositives(requestBody)
          emit('successAlert')
        } catch (err) {
          console.error(err)
          emit('errorAlert')
        }

        closeDialog()
      }

      function createComment () {
        const radioComment = radios.value || ''
        const textAreaComment = comment.value || ''

        if (radioComment && textAreaComment) {
          return `${radioComment}, ${textAreaComment}`
        }

        return radioComment + textAreaComment
      }

      function closeDialog () {
        emit('close')
      }

      return {
        localVisible,
        comment,
        radios,
        t,
        closeDialog,
        markAsFalsePositive,
      }
    },
  })
</script>
