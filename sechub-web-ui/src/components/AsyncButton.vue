<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-btn
    :color="color"
    density="comfortable"
    :disabled="loading"
    :icon="icon"
    @click="buttonClicked(id)"
  />
</template>
<script lang="ts">
  import { defineComponent } from 'vue'

  interface Props {
    id: string
    color: string
    icon: string
  }

  export default defineComponent({

    props: {
      id: {
        type: String,
        required: true,
      },
      color: {
        type: String,
      },
      icon: {
        type: String,
      },
    },

    emits: ['buttonClicked'],

    setup (props: Props, { emit }) {
      const loading = ref(false)

      async function buttonClicked (id : string | undefined) {
        if (!id) {
          return
        }

        loading.value = true
        try {
          // workaround from https://github.com/vuejs/rfcs/issues/586
          await new Promise(() => {
            emit('buttonClicked', id)
          })
        } catch (err) {
          console.error('', err)
        } finally {
          loading.value = false
        }
      }

      return {
        loading,
        buttonClicked,
      }
    },
  })
</script>
