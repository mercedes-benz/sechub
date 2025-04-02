<!-- SPDX-License-Identifier: MIT -->
<template>
    <v-table 
    class="background-color"
    fixed-header
    >
        <tbody class="sechub-primary-color">
        <tr>
          <th> TO BE IMPLEMENTED </th>
            <th>{{ $t('REPORT_DESCRIPTION_LOCATION')}}</th>
            <th>{{ $t('REPORT_DESCRIPTION_ATTACK_VECTOR')}}</th>
            <th>{{ $t('REPORT_DESCRIPTION_EVIDENCE')}}</th>
        </tr>
        </tbody>
        
        <tbody>
        <tr>
          <td> tod be imple </td>
            <td>{{ item.web?.request?.target }}</td>
            <td>{{ item.web?.attack?.vector }}</td>
            <td>{{ item.web?.attack?.evidence?.snippet }}</td>
        </tr>
        </tbody>
    </v-table>

    <v-table 
    class="background-color"
    fixed-header
    >
      <tbody class="sechub-primary-color">
          <tr>
              <th>{{ $t('REPORT_DESCRIPTION_DETAILS')}}</th>
          </tr>
        </tbody>
        <tbody>
          <tr>
            <td>{{ item.web?.request?.method }} {{ item.web?.request?.target }}
              <span> {{ item.web?.request?.protocol}}</span>
            </td>
          </tr>
        </tbody>
    </v-table>

    <v-table 
    class="background-color"
    fixed-header
    >
      <tbody class="sechub-primary-color">
          <tr>
              <th>{{ $t('REPORT_DESCRIPTION_SOLUTION')}}</th>
          </tr>
        </tbody>
        <tbody>
            <tr>
              <td v-if="item.solution"> {{ item.solution }} </td>
              <td v-else>{{ $t('REPORT_DESCRIPTION_SOLUTION_EMPTY')}} 
                <a :href="`https://cwe.mitre.org/data/definitions/${item.cweId}.html`">CWE-{{ item.cweId }}</a>
              </td>
            </tr>
        </tbody>
    </v-table>
</template>
<script lang="ts">
  import { defineComponent, toRefs } from 'vue'
  import { getTrafficLightClass } from '@/utils/projectUtils'
  import { SecHubFinding } from '@/generated-sources/openapi'
  import '@/styles/sechub.scss'

interface Props {
  item: SecHubFinding
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

    return {
        getTrafficLightClass,
        item,
    }
  },
})
</script>