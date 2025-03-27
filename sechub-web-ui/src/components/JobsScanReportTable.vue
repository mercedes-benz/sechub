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
            <td>{{ item.code?.calls }}</td>
            <td>{{ item.code?.location }}</td>
            <td>{{ item.code?.line}}</td>
            <td>{{ item.code?.column}}</td>
            <td>{{ item.code?.relevantPart}}</td>
            <td>{{ item.code?.source}}</td>
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
  import { useRouter } from 'vue-router'
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
    const router = useRouter()

    function routerGoBack () {
        router.go(-1)
      }

    return {
        getTrafficLightClass,
        routerGoBack,
        item,
    }
  },
})
</script>