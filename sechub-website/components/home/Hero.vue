<!-- SPDX-License-Identifier: MIT -->
<script setup lang="ts">
import { PlayIcon } from '@heroicons/vue/20/solid';
</script>

<template>
  <section class="overflow-hidden pt-16 lg:pb-32 xl:pb-36">
    <div class="container">
      <div class="lg:grid lg:grid-cols-12 lg:gap-x-8 lg:gap-y-20">
        <div class="relative z-10 mx-auto max-w-2xl lg:col-span-7 lg:max-w-none lg:pt-6 xl:col-span-6">
          <h1 class="text-4xl font-medium tracking-tight text-gray-900">SecHub Tool</h1>
          <p class="mt-6 text-lg text-gray-600">
            The free and open-source security platform SecHub, provides a central API to test software with different
            security tools. Many free and open-source as well proprietary security tools are supported by SecHub.
          </p>
          <div class="mt-8 flex flex-wrap gap-x-6 gap-y-4">
            <NuxtLink class="button is-outlined !w-auto" to="https://www.youtube.com/">
              <PlayIcon class="size-4 flex-none" />
              <span class="ml-2">See in action</span>
            </NuxtLink>
          </div>
        </div>
        <div class="relative mt-10 sm:mt-20 lg:col-span-5 lg:row-span-2 lg:mt-0 xl:col-span-6">
          <AbstractDoubleCircularAnimation />
          <div
            class="pb-10 [mask-image:linear-gradient(to_bottom,white_60%,transparent)] sm:mx-0 lg:absolute lg:-inset-x-10 lg:-bottom-20 lg:-top-10 lg:h-auto lg:px-0 lg:pt-10 xl:-bottom-32"
          >
            <div class="relative mx-auto max-w-64 lg:max-w-80">
              <img src="/logo.svg" alt="SecHub Logo" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
