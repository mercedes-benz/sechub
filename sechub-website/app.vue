<script setup lang="ts">
useSeoMeta({
  title: 'SecHub | Your security guardian.',
  description:
    'SecHub is a free and open-source security platform that provides a central API to test software with different security tools.',
  ogImage: '/og.jpg'
});
</script>

<template>
  <Header />
  <main>
    <HomeHero />
    <HomeFeatures />
    <HomeDownload />
  </main>
  <Footer />
</template>

<style lang="scss">
html {
  scroll-behavior: smooth;
}

.button {
  @apply inline-flex w-full justify-center rounded-md bg-fern-600 px-4 py-2 text-sm font-medium text-white hover:bg-fern-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-white/75;
  &.is-outlined {
    @apply border border-fern-600 bg-transparent text-fern-600 hover:bg-fern-50;
  }
}

.container {
  @apply mx-auto max-w-7xl px-4 sm:px-6 lg:px-8;
}

.menu-item {
  @apply relative -mx-3 -my-2 rounded-lg px-3 py-2 text-sm text-gray-700 transition-colors delay-150 hover:text-fern-600 hover:delay-0;
}
</style>
