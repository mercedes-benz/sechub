<!-- SPDX-License-Identifier: MIT -->
<template>
  <div class="text-center">
    <v-container>
      <v-row>
        <v-col class="ma-0 pa-0">
          <v-container class="ma-0 pa-0">
            <v-pagination
              v-model="localCurrentPage"
              :length="totalPages"
              :total-visible="7"
              @update:model-value="onPageChange"
            />
          </v-container>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, toRefs, watch } from 'vue'

  interface Props {
    currentPage: number
    totalPages: number
  }

  export default defineComponent({
    props: {
      currentPage: {
        type: Number,
        required: true,
      },
      totalPages: {
        type: Number,
        required: true,
      },
    },
    emits: ['pageChanged'],
    setup (props: Props, { emit }) {
      const { currentPage } = toRefs(props)
      const localCurrentPage = ref(currentPage.value)

      watch(currentPage, newVal => {
        localCurrentPage.value = newVal
      })

      function onPageChange (page: number) {
        // calls function from parent component (emit)
        emit('pageChanged', page)
      }

      return {
        localCurrentPage,
        onPageChange,
      }
    },
  })
</script>
