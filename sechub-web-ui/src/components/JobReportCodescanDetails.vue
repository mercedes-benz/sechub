<!-- SPDX-License-Identifier: MIT -->
<template>
    <v-table 
    class="background-color"
    fixed-header
    >
        <tbody class="sechub-primary-color">
        <tr>
            <th>{{ $t('REPORT_DESCRIPTION_CALL')}}</th>
            <th>{{ $t('REPORT_DESCRIPTION_LOCATION')}}</th>
            <th>{{ $t('REPORT_DESCRIPTION_LINE')}}</th>
            <th>{{ $t('REPORT_DESCRIPTION_COLUMN')}}</th>
            <th>{{ $t('REPORT_DESCRIPTION_RELEVANT_PART')}}</th>
            <th>{{ $t('REPORT_DESCRIPTION_SOURCE')}}</th>
        </tr>
        </tbody>
        
        <tbody>
        <tr>
            <td v-if="item.code?.calls">{{ item.code?.calls }}</td>
            <td v-else>1</td>
            <td>{{ item.code?.location }}</td>
            <td>{{ item.code?.line}}</td>
            <td>{{ item.code?.column}}</td>
            <td>{{ item.code?.relevantPart}}</td>
            <td>{{ item.code?.source}}</td>
        </tr>
        </tbody>
    </v-table>

    <!-- Revision Table -->
    <div>
      <v-table v-if="item.revision?.id"
      class="background-color sechub-report-expandable-element"
      fixed-header>
      <tbody class="sechub-primary-color">
              <tr>
                <v-btn
                :append-icon="isExpanded.revision ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                :text="isExpanded.revision ? $t('REPORT_REVISION_HIDE') : $t('REPORT_REVISION_SHOW')"
                class="text-none background-color ma-2"
                color="primary"
                variant="text"
                @click="toggleExpand('revision')">
                </v-btn>
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
      class="background-color sechub-report-expandable-element"
      fixed-header
      >
        <tbody class="sechub-primary-color">
            <tr>
                <v-btn
                :append-icon="isExpanded.description ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                :text="isExpanded.description ? $t('REPORT_DESCRIPTION_HIDE') : $t('REPORT_DESCRIPTION_SHOW')"
                class="text-none background-color ma-2"
                color="primary"
                variant="text"
                @click="toggleExpand('description')">
                </v-btn>
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
      <tbody class="sechub-primary-color">
          <tr>
              <v-btn
              :append-icon="isExpanded.solution ? 'mdi-chevron-up' : 'mdi-chevron-down'"
              :text="isExpanded.solution ? $t('REPORT_SOLUTION_HIDE') : $t('REPORT_SOLUTION_SHOW')"
              class="text-none background-color ma-2"
              color="primary"
              variant="text"
              @click="toggleExpand('solution')">
              </v-btn>
          </tr>
        </tbody>
        <tbody v-if="isExpanded.solution">
            <tr>
              <td v-if="item.solution"> {{ item.solution }} </td>
              <td v-else>{{ $t('REPORT_DESCRIPTION_SOLUTION_EMPTY')}} 
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
  revision: boolean;
  solution: boolean;
  description: boolean;
}

export default defineComponent({
  props: {
    item: {
      type: Object,
      required: true,
    }
  },

  setup (props: Props, {}) {
    const { item } = toRefs(props)

    const isExpanded = ref<ExpandedState>({
      revision: false,
      solution: false,
      description: false,
    })

    const toggleExpand = (table: keyof ExpandedState) => {
      isExpanded.value[table] = !isExpanded.value[table]
    }

    return {
        getTrafficLightClass,
        toggleExpand,
        isExpanded,
        item,
    }
  },
})
</script>