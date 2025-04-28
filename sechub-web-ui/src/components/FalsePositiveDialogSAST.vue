<template>
    <v-dialog
    v-model="localVisible"
    max-width="50%"
    >
        <v-card>
            <v-card-title>
                Mark as false Positive
            </v-card-title>
            <v-radio-group v-model="radios">
                <v-radio 
                :label="$t('MARK_FALSE_POSITIVE_RADIO_ONE')"
                :value="$t('MARK_FALSE_POSITIVE_RADIO_ONE')">
                </v-radio>
                <v-radio 
                :label="$t('MARK_FALSE_POSITIVE_RADIO_TWO')"
                :value="$t('MARK_FALSE_POSITIVE_RADIO_TWO')">
                </v-radio>
                <v-radio 
                :label="$t('MARK_FALSE_POSITIVE_RADIO_THREE')"
                :value="$t('MARK_FALSE_POSITIVE_RADIO_THREE')">
                </v-radio>
                <v-radio 
                :label="$t('MARK_FALSE_POSITIVE_RADIO_FOUR')"
                :value="$t('MARK_FALSE_POSITIVE_RADIO_FOUR')">
                </v-radio>
                <v-radio 
                :label="$t('MARK_FALSE_POSITIVE_RADIO_FIVE')"
                :value="$t('MARK_FALSE_POSITIVE_RADIO_FIVE')">
                </v-radio>
            </v-radio-group>

            <v-textarea
            class="ma-5"
            label="Additional Description"
            maxlength="120"
            counter
            single-line
            variant="outlined"
            v-model="comment"
            ></v-textarea>

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
import  defaultClient from '@/services/defaultClient'
import { FalsePositives, UserMarkFalsePositivesRequest, FalsePositiveJobData, SecHubFinding } from '@/generated-sources/openapi'
import { useFetchReport } from '@/composables/useReport'
import '@/styles/sechub.scss'

export default defineComponent({
  props: {
    visible: {
      type: Boolean,
      required: true,
    },
    projectId:{
        type: String,
        required: true
    },
    jobUUID:{
        type: String,
        required: true
    },
    selectedFindings: {
        type: Array<SecHubFinding>,
        required: true
    },
  },

  emits: ['close'],

  setup (props, { emit }) {
    const { visible, projectId, selectedFindings, jobUUID } = toRefs(props)
    const localVisible = ref(visible)
    const { t } = useI18n()
    const comment = ref('')
    const radios = ref()

    async function markAsFalsePositive(){
        console.log(selectedFindings.value)

        let falsePositiveJobData: Array<FalsePositiveJobData> = []
        selectedFindings.value.forEach((finding) => {

            let data: FalsePositiveJobData = {
                findingId: finding.id,
                jobUUID: jobUUID.value,
                comment: createComment()
            }
            falsePositiveJobData.push(data)
        })

        const falsePositives: FalsePositives = {
            apiVersion: '1.0',
            type: "falsePositiveDataList",
            jobData: falsePositiveJobData,
        }

        const requestBody: UserMarkFalsePositivesRequest = {
            projectId: projectId.value,
            falsePositives: falsePositives
        }

        console.debug(JSON.stringify(requestBody))

        try{
          await defaultClient.withSechubExecutionApi.userMarkFalsePositives(requestBody)
          await useFetchReport(projectId.value, jobUUID.value)
          closeDialog()
        }catch(err){
            console.error(err)
        }
    }

    function createComment() {
        const radioComment = radios.value || '';
        const textAreaComment = comment.value || '';

        if (radioComment && textAreaComment) {
            return `${radioComment}, ${textAreaComment}`;
        }

        return radioComment + textAreaComment;
    }

    function closeDialog(){
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