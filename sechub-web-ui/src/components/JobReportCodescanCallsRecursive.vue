<template>
  <template v-if="call.calls">
    <tr>
      <td>{{ call.location }}</td>
      <td>{{ call.line }}</td>
      <td>{{ call.column }}</td>
      <td>{{ call.relevantPart }}</td>
      <td>{{ call.source }}</td>
    </tr>
    <JobReportCodescanCallsRecursive :call="call.calls" />
  </template>
  <template v-else>
    <tr>
      <td>{{ call.location }}</td>
      <td>{{ call.line }}</td>
      <td>{{ call.column }}</td>
      <td>{{ call.relevantPart }}</td>
      <td>{{ call.source }}</td>
    </tr>
  </template>
</template>
<script lang="ts">
  import { defineComponent } from 'vue'

  export default defineComponent({
    name: 'JobReportCodescanCallsRecursive',
    props: {
      call: {
        type: Object,
        required: true,
      },
    },
  })
</script>
