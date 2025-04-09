<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-table
    class="background-color"
    fixed-header
  >
    <tbody class="sechub-primary-color-report">
      <tr>
        <th>{{ $t('REPORT_DESCRIPTION_LOCATION') }}</th>
        <th>{{ $t('REPORT_DESCRIPTION_LINE') }}</th>
        <th>{{ $t('REPORT_DESCRIPTION_COLUMN') }}</th>
        <th>{{ $t('REPORT_DESCRIPTION_RELEVANT_PART') }}</th>
        <th>{{ $t('REPORT_DESCRIPTION_SOURCE') }}</th>
      </tr>
    </tbody>

    <tbody>
      <tr>
        <td>{{ relevantCall.location }}</td>
        <td>{{ relevantCall.line }}</td>
        <td>{{ relevantCall.column }}</td>
        <td>{{ relevantCall.relevantPart }}</td>
        <td>{{ relevantCall.source }}</td>
      </tr>
    </tbody>
  </v-table>

  <!-- Call stack table -->
  <di>
    <v-table
      v-if="item.code?.calls"
      class="background-color sechub-report-expandable-element"
      fixed-header
    >
      <tbody class="sechub-primary-color-report">
        <tr>
          <v-btn
            :append-icon="isExpanded.callstack ? 'mdi-chevron-up' : 'mdi-chevron-down'"
            class="text-none background-color ma-2"
            color="primary"
            :text="isExpanded.callstack ? $t('REPORT_CALLSTACK_HIDE') : $t('REPORT_CALLSTACK_SHOW')"
            variant="text"
            @click="toggleExpand('callstack')"
          />
        </tr>
      </tbody>
      <tbody v-if="isExpanded.callstack" class="sechub-primary-color-report">
        <tr>
          <th>{{ $t('REPORT_DESCRIPTION_LOCATION') }}</th>
          <th>{{ $t('REPORT_DESCRIPTION_LINE') }}</th>
          <th>{{ $t('REPORT_DESCRIPTION_COLUMN') }}</th>
          <th>{{ $t('REPORT_DESCRIPTION_RELEVANT_PART') }}</th>
          <th>{{ $t('REPORT_DESCRIPTION_SOURCE') }}</th>
        </tr>
      </tbody>
      <tbody v-if="isExpanded.callstack">
        <template v-if="item.code?.calls">
          <JobReportCodescanCallsRecursive
            :call="item.code.calls"
          />
        </template>
      </tbody>
    </v-table>
  </di>

  <!-- Revision Table -->
  <div>
    <v-table
      v-if="item.revision?.id"
      class="background-color sechub-report-expandable-element"
      fixed-header
    >
      <tbody class="sechub-primary-color-report">
        <tr>
          <v-btn
            :append-icon="isExpanded.revision ? 'mdi-chevron-up' : 'mdi-chevron-down'"
            class="text-none background-color ma-2"
            color="primary"
            :text="isExpanded.revision ? $t('REPORT_REVISION_HIDE') : $t('REPORT_REVISION_SHOW')"
            variant="text"
            @click="toggleExpand('revision')"
          />
        </tr>
      </tbody>
      <tbody v-if="isExpanded.revision">
        <tr>
          <td> {{ item.revision?.id }} </td>
        </tr>
      </tbody>
    </v-table>
  </div>

  <!-- Description Table -->
  <div>
    <v-table
      v-if="item.description"
      class="background-color sechub-report-expandable-element"
      fixed-header
    >
      <tbody class="sechub-primary-color-report">
        <tr>
          <v-btn
            :append-icon="isExpanded.description ? 'mdi-chevron-up' : 'mdi-chevron-down'"
            class="text-none background-color ma-2"
            color="primary"
            :text="isExpanded.description ? $t('REPORT_DESCRIPTION_HIDE') : $t('REPORT_DESCRIPTION_SHOW')"
            variant="text"
            @click="toggleExpand('description')"
          />
        </tr>
      </tbody>
      <tbody v-if="isExpanded.description">
        <tr>
          <td> {{ item.description }} </td>
        </tr>
      </tbody>
    </v-table>
  </div>

  <!-- Solution Table -->
  <div>
    <v-table
      class="background-color sechub-report-expandable-element"
      fixed-header
    >
      <tbody class="sechub-primary-color-report">
        <tr>
          <v-btn
            :append-icon="isExpanded.solution ? 'mdi-chevron-up' : 'mdi-chevron-down'"
            class="text-none background-color ma-2"
            color="primary"
            :text="isExpanded.solution ? $t('REPORT_SOLUTION_HIDE') : $t('REPORT_SOLUTION_SHOW')"
            variant="text"
            @click="toggleExpand('solution')"
          />
        </tr>
      </tbody>
      <tbody v-if="isExpanded.solution">
        <tr>
          <td v-if="item.solution"> {{ item.solution }} </td>
          <td v-else>{{ $t('REPORT_DESCRIPTION_SOLUTION_EMPTY') }}
            <a :href="`https://cwe.mitre.org/data/definitions/${item.cweId}.html`">CWE-{{ item.cweId }}</a>
          </td>
        </tr>
      </tbody>
    </v-table>
  </div>
</template>
<script lang="ts">
  import { defineComponent, toRefs } from 'vue'
  import { getTrafficLightClass } from '@/utils/projectUtils'
  import { SecHubFinding } from '@/generated-sources/openapi'
  import '@/styles/sechub.scss'

  interface Props {
    item: SecHubFinding
  }

  interface ExpandedState {
    callstack: boolean;
    revision: boolean;
    solution: boolean;
    description: boolean;
  }

  export default defineComponent({
    props: {
      item: {
        type: Object,
        required: true,
      },
    },

    setup (props: Props) {
      const { item } = toRefs(props)

      const isExpanded = ref<ExpandedState>({
        callstack: false,
        revision: false,
        solution: false,
        description: false,
      })

      const toggleExpand = (table: keyof ExpandedState) => {
        isExpanded.value[table] = !isExpanded.value[table]
      }

      const getLastCall = (call: any) => {
        while (call.calls) {
          call = call.calls
        }
        return call
      }

      const relevantCall = ref(item.value.code?.calls ? getLastCall(item.value.code.calls) : item.value.code)

      return {
        getTrafficLightClass,
        toggleExpand,
        isExpanded,
        relevantCall,
      }
    },
  })
</script>
<style scoped>
pre {
  word-wrap: break-word;
  width: 100%;
  margin: 0;
  display: inline;
  text-align: left;
  white-space: pre-line;
}
</style>
