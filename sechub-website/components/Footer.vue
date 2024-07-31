<script lang="ts" setup></script>

<template>
  <footer class="border-t border-gray-200">
    <div class="container">
      <div class="flex flex-col items-start justify-between gap-y-12 pb-6 pt-16 lg:flex-row lg:items-center lg:py-16">
        <div>
          <div class="flex items-center text-gray-900">
            <img src="/logo.svg" class="h-10 w-8 flex-none object-contain" alt="SecHub Logo" loading="lazy" />
            <div class="ml-4">
              <p class="text-base font-semibold">SecHub</p>
              <p class="text-sm">Your security guardian.</p>
            </div>
          </div>

          <p class="mt-6 text-sm text-gray-500">© Copyright {{ new Date().getFullYear() }}. All rights reserved.</p>
        </div>
        <NuxtLink
          class="group relative -mx-4 flex items-center self-stretch p-4 transition-colors hover:bg-gray-100 sm:self-auto sm:rounded-2xl lg:mx-0 lg:self-auto lg:p-6"
          to="https://github.com/mercedes-benz/sechub/blob/develop/CONTRIBUTING.md"
          target="_blank"
        >
          <div class="lg:w-64">
            <p class="text-base font-semibold text-gray-900">You want to innovate SecHub?</p>
            <p class="mt-1 text-sm text-gray-700">
              We attach great importance to open and transparent communication for all parts of the community.
            </p>
          </div>
        </NuxtLink>
      </div>
    </div>
  </footer>
</template>
