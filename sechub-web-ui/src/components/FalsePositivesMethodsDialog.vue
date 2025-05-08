<template>
  <v-dialog v-model="visible" persistent max-width="600px">
    <v-card>
      <v-card-title>
        {{ $t('MARK_FALSE_POSITIVE_METHODS_SELECTION_TITLE') }}
      </v-card-title>
      <v-card-text>
        <v-checkbox
          v-for="method in httpMethods"
          :key="method"
          v-model="falsePositive.methods"
          :value="method"
          :label="method"
          hide-details
        ></v-checkbox>
      </v-card-text>
      <v-card-actions>
        <v-btn @click="cancel">
            {{ $t('CONFIRM_DIALOG_BUTTON_CANCEL') }}
          </v-btn>
          <v-btn color="primary" @click="closeDialog">
            {{ $t('CONFIRM_DIALOG_BUTTON_OK') }}
          </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue';
import { WebscanFalsePositiveProjectData } from '@/generated-sources/openapi'

interface Props {
  falsePositive: WebscanFalsePositiveProjectData,
  visible: boolean,
}

export default defineComponent({
  props: {
      visible: {
      type: Boolean,
      required: true,
    },
    falsePositive: {
      type: Object,
      required: true,
    },
  },

  emits: ['close'],

  setup(props, { emit }) {
    const httpMethods = ['GET', 'POST', 'PUT', 'DELETE'];
    const { falsePositive, visible } = toRefs(props);
    const oldMethods = falsePositive.value.methods

    watch(visible, (newValue) => {
        if (newValue){
          oldMethods.value = falsePositive.value.methods
        }
      });

    const closeDialog = () => {
      emit('close');
    };

    const cancel = () =>{
      falsePositive.value.methods = oldMethods.value
      closeDialog()
    }

    return {
      httpMethods,
      falsePositive,
      closeDialog,
      cancel,
    };
  },
});
</script>