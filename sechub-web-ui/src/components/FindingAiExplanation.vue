<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-toolbar color="background_paper">
    <v-toolbar-title>{{ projectId }}</v-toolbar-title>
  </v-toolbar>

  <v-toolbar color="background_paper">
    <v-toolbar-title>
      <span>{{ report.jobUUID }}</span>
      <span
        class="ml-6 sechub-primary-color"
      >{{ finding.name }} - (ID: {{ finding.id }}) </span>
      <span :class="['ml-6' ]">
        <v-icon
          :color="calculateColor(finding.severity || '')"
          :icon="calculateIcon(finding.severity || '')"
        />
      </span>

    </v-toolbar-title>
    <template #prepend>
      <v-icon
        :class="['traffic-light-toolbar', getTrafficLightClass(report.trafficLight || '')]"
        icon="mdi-circle"
        size="x-large"
      />
    </template>
  </v-toolbar>

  <v-card
    v-if="!explanation.findingExplanation"
    class="background-color mt-5"
    variant="flat"
  >
    <v-card-text>
      {{ $t('FINDINGS_AI_EXPLANATION_NO_DATA') }}
    </v-card-text>
  </v-card>

  <div v-else>

    <v-card
      v-if="explanation.findingExplanation"
      class="background-color mt-5"
      variant="flat"
    >
      <v-card-title>
        {{ explanation.findingExplanation.title }}
      </v-card-title>
      <v-card-text>
        {{ explanation.findingExplanation.content }}
      </v-card-text>
    </v-card>
    <v-card
      v-else
      class="background-color mt-5"
      variant="flat"
    >
      <v-card-text>
        {{ $t('FINDINGS_AI_EXPLANATION_NO_DATA_EXPLANATION') }}
      </v-card-text>
    </v-card>

    <v-card
      v-if="explanation.potentialImpact"
      class="background-color mt-5"
      variant="flat"
    >
      <v-card-title>
        {{ explanation.potentialImpact.title }}
      </v-card-title>
      <v-card-text>
        {{ explanation.potentialImpact.content }}
      </v-card-text>
    </v-card>
    <v-card
      v-else
      class="background-color mt-5"
      variant="flat"
    >
      <v-card-text>
        {{ $t('FINDINGS_AI_EXPLANATION_NO_DATA_POTENTIAL_IMPACT') }}
      </v-card-text>
    </v-card>

    <v-card
      v-if="explanation.recommendations"
      class="background-color mt-5"
      variant="flat"
    >
      <v-card-title>
        {{ $t('FINDINGS_AI_EXPLANATION_RECOMMENDATIONS') }}
      </v-card-title>
      <v-card-text>
        <v-list>
          <v-list-item v-for="(recommendation, index) in explanation.recommendations" :key="index">
            <v-list-item-title>{{ recommendation.title }}</v-list-item-title>
            <v-list-item-subtitle>{{ recommendation.content }}</v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-card-text>
    </v-card>
    <v-card
      v-else
      class="background-color mt-5"
      variant="flat"
    >
      <v-card-text>
        {{ $t('FINDINGS_AI_EXPLANATION_NO_DATA_RECOMMENDATIONS') }}
      </v-card-text>
    </v-card>

    <v-card
      v-if="explanation.codeExample"
      class="background-color mt-5"
      variant="flat"
    >
      <v-card-title>
        {{ $t('FINDINGS_AI_EXPLANATION_CODE_EXAMPLE') }}
      </v-card-title>
      <v-card-subtitle>
        {{ $t('FINDINGS_AI_EXPLANATION_VULNERABLE_EXAMPLE') }}
      </v-card-subtitle>
      <v-card-text>
        <VCodeBlock
          :code="explanation.codeExample.vulnerableExample"
          prismjs
          theme="tomorrow"
        />
      </v-card-text>
      <v-card-subtitle>
        {{ $t('FINDINGS_AI_EXPLANATION_SECURE_EXAMPLE') }}
      </v-card-subtitle>
      <v-card-text>
        <VCodeBlock
          :code="explanation.codeExample.secureExample"
          prismjs
          theme="tomorrow"
        />
      </v-card-text>
      <v-card-title v-if="explanation.codeExample.explanation">
        {{ explanation.codeExample.explanation.title }}
      </v-card-title>
      <v-card-text v-if="explanation.codeExample.explanation">
        {{ explanation.codeExample.explanation.content }}
      </v-card-text>
    </v-card>
    <v-card
      v-else
      class="background-color mt-5"
      variant="flat"
    >
      <v-card-text>
        {{ $t('FINDINGS_AI_EXPLANATION_NO_DATA_CODE_EXAMPLE') }}
      </v-card-text>
    </v-card>

    <v-card
      v-if="explanation.references"
      class="background-color mt-5"
      variant="flat"
    >
      <v-card-title>
        {{ $t('FINDINGS_AI_EXPLANATION_REFERENCES') }}
      </v-card-title>
      <v-card-text>
        <v-list>
          <v-list-item v-for="(reference, index) in explanation.references" :key="index">
            <v-list-item-subtitle>
              <a :href="reference.url" rel="noopener noreferrer" target="_blank">
                {{ reference.title }}
              </a>
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-card-text>
      <v-card-text>
        <v-text>
          {{ $t('FINDINGS_AI_EXPLANATION_HINT') }}
        </v-text>
      </v-card-text>
    </v-card>
    <v-card
      v-else
      class="background-color mt-5"
      variant="flat"
    >
      <v-card-text>
        {{ $t('FINDINGS_AI_EXPLANATION_NO_DATA') }}
      </v-card-text>
    </v-card>
  </div>

</template>
<script lang="ts">
  import { SecHubExplanationResponse, SecHubFinding, SecHubReport } from 'sechub-openapi-ts-client'
  import { defineComponent } from 'vue'
  import { useRoute } from 'vue-router'
  import { useReportStore } from '@/stores/reportStore'
  import { calculateColor, calculateIcon, getTrafficLightClass } from '@/utils/projectUtils'
  import defaultClient from '@/services/defaultClient'
  import { handleApiError } from '@/services/apiErrorHandler'
  import '@/styles/sechub.scss'

  type RouteParams = {
    id?: string;
    jobId?: string;
    findingId?: string;
  };

  export default defineComponent({
    name: 'FindingAiExplanation',

    setup () {
      const route = useRoute()
      const params = route.params as RouteParams
      const projectId = ref(params.id || '')
      const jobUUID = ref(params.jobId || '')
      const findingId = ref(params.findingId || '')

      const store = useReportStore()
      const report = ref<SecHubReport>({})
      const finding = ref<SecHubFinding>({})

      // Example explanation data
      const reportFromStore = store.getReportByUUID(jobUUID.value)
      if (reportFromStore) {
        report.value = reportFromStore
        if (report.value.result?.findings) {
          const findingsAsNumber = parseInt(findingId.value, 10)
          finding.value = report.value.result.findings.find(f => f.id === findingsAsNumber) || {}
        }
      } else {
        console.error('Report not found in store')
      }

      const explanation = ref<SecHubExplanationResponse>({})
      explainByAI()

      async function explainByAI () {
        const findingIdAsNumber = parseInt(findingId.value, 10)
        try {
          const response = await defaultClient.withAssistanceApi.userRequestFindingExplanation({
            projectId: projectId.value,
            jobUUID: jobUUID.value,
            findingId: findingIdAsNumber,
          })
          explanation.value = response
        } catch (error) {
          handleApiError(error)
          console.error('Error fetching AI explanation:', error)
        }
      }

      return {
        explanation,
        projectId,
        jobUUID,
        finding,
        report,
        getTrafficLightClass,
        calculateColor,
        calculateIcon,
      }
    },
  })
</script>
