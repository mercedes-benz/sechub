<!-- SPDX-License-Identifier: MIT -->
<template>

    <!-- Finding overview -->
    <v-table 
    class="background-color"
    fixed-header
    >
        <tbody class="sechub-primary-color">
        <tr>
            <th>{{ $t('REPORT_DESCRIPTION_LOCATION')}}</th>
            <th>{{ $t('REPORT_DESCRIPTION_ATTACK_VECTOR')}}</th>
            <th>{{ $t('REPORT_DESCRIPTION_EVIDENCE')}}</th>
        </tr>
        </tbody>
        
        <tbody>
        <tr>
            <td>{{ webItem.request?.target }}</td>
            <td>{{ webItem.attack?.vector }}</td>
            <td>{{ webItem.attack?.evidence?.snippet }}</td>
        </tr>
        </tbody>
    </v-table>


        <!-- Expandable report details table -->
        <div>
        <v-table
        class="background-color sechub-report-expandable-element"
        fixed-header
        >
          <tbody >
              <tr>
                  <v-btn
                  :append-icon="isExpanded.reportDetails ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                  :text="isExpanded.reportDetails ? $t('REPORT_DETAILS_HIDE') : $t('REPORT_DETAILS_SHOW')"
                  class="text-none background-color ma-2"
                  color="primary"
                  variant="text"
                  @click="toggleExpand('reportDetails')"
                  >
                </v-btn>
              </tr>
            </tbody>

            <!-- Webscan report details request -->
            <tbody v-if="isExpanded.reportDetails">
              <tr>
                <th class="sechub-primary-color">
                  {{ $t('REPORT_DETAILS_WEBSCAN_REQUEST') }}
                </th>
                <th></th>
              </tr>
              <tr>
                <td> {{ $t('REPORT_DETAILS_WEBSCAN_METHOD') }}</td>
                <td>{{ webItem.request?.method }} {{ webItem.request?.target }}
                  <div>
                    {{ webItem.request?.protocol}} {{ webItem.request?.version }}
                  </div>
                </td>
              </tr>
            </tbody>

            <tbody v-if="isExpanded.reportDetails"
            class="background-color-light ">
              <tr>
                <td> {{ $t('REPORT_DESCRIPTION_ATTACK_VECTOR') }}</td>
                <td>{{ webItem.attack?.vector }}</td>
              </tr>
            </tbody>

            <tbody v-if="isExpanded.reportDetails">
              <tr>
                <td> {{ $t('REPORT_DETAILS_WEBSCAN_HEADERS') }}</td>
                <td>
                  <v-list lines="two"
                  class="background-color">
                    <v-list-item
                    class="background-color ma-0 pa-0"
                      v-for="(header, i) in webItem.request?.headers">
                      <spa>{{ i }}</spa>: <span>{{ header }}</span>

                    </v-list-item>
                  </v-list>
                </td>
              </tr>
            </tbody>

            <tbody v-if="isExpanded.reportDetails"
            class="background-color-light ">
              <tr>
                <td> {{ $t('REPORT_DETAILS_WEBSCAN_BODY') }}</td>
                <td>
                  <pre>
                    {{ formatJson(webItem.request?.body?.text || '{}') }}
                  </pre>
                </td>
              </tr>
            </tbody>
            
            <!-- Webscan report details resposne -->
            <tbody v-if="isExpanded.reportDetails">
              <tr>
                <th class="sechub-primary-color">
                  {{ $t('REPORT_DETAILS_WEBSCAN_RESPONSE') }}
                </th>
                <th></th>
              </tr>
              <tr>
                <td> {{ $t('REPORT_DETAILS_WEBSCAN_RESPONSE') }}</td>
                <td>{{ webItem.response?.statusCode }} {{ webItem.response?.reasonPhrase}}
                  <div>
                    {{ webItem.response?.protocol}} {{ webItem.response?.version }}
                  </div>
                </td>
              </tr>
            </tbody>

            <tbody v-if="isExpanded.reportDetails"
            class="background-color-light ">
              <tr>
                <td> {{ $t('REPORT_DESCRIPTION_EVIDENCE') }}</td>
                <td>
                  {{ webItem.attack?.evidence?.snippet }}
                </td>
              </tr>
            </tbody>

            <tbody v-if="isExpanded.reportDetails">
              <tr>
                <td> {{ $t('REPORT_DETAILS_WEBSCAN_HEADERS') }}</td>
                <td>
                  <v-list lines="two"
                  class="background-color">
                    <v-list-item
                    class="background-color ma-0 pa-0"
                      v-for="(header, i) in webItem.response?.headers">
                      <spa>{{ i }}</spa>: <span>{{ header }}</span>

                    </v-list-item>
                  </v-list>
                </td>
              </tr>
            </tbody>

            <tbody v-if="isExpanded.reportDetails"
            class="background-color-light ">
              <tr>
                <td> {{ $t('REPORT_DETAILS_WEBSCAN_BODY') }}</td>
                <td>
                  <pre>
                  {{ formatJson(webItem.response?.body?.text || '{}') }}
                  </pre>
                </td>
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
  import { SecHubFinding, SecHubReportWeb } from '@/generated-sources/openapi'
  import '@/styles/sechub.scss'

interface Props {
  item: SecHubFinding
}

interface ExpandedState {
  reportDetails: boolean;
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

    const webItem = ref<SecHubReportWeb>({})
    webItem.value = item.value.web || {}

    const isExpanded = ref<ExpandedState>({
      reportDetails: false,
      solution: false,
      description: false,
    })

    const toggleExpand = (table: keyof ExpandedState) => {
      isExpanded.value[table] = !isExpanded.value[table]
    }

    function formatJson(jsonString: string) {
      console.log(jsonString)
      try {
        const jsonObj = JSON.parse(jsonString)
        return JSON.stringify(jsonObj, null, 2)
      } catch (error) {
        return jsonString
      }
    }

    return {
        getTrafficLightClass,
        toggleExpand,
        formatJson,
        item,
        webItem,
        isExpanded,
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