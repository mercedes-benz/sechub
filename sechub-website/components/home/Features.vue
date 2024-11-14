<!-- SPDX-License-Identifier: MIT -->
<script setup lang="ts">
import {
  ServerStackIcon,
  PuzzlePieceIcon,
  CheckBadgeIcon,
  BookOpenIcon,
  WrenchScrewdriverIcon,
  CpuChipIcon
} from '@heroicons/vue/20/solid';

const items = [
  {
    title: 'API Orchestration',
    description:
      'SecHub orchestrates tools via a unified API, simplifying integration for projects and build pipelines without the need for multiple plugins.',
    icon: ServerStackIcon
  },
  {
    title: 'Build Pipeline Integration',
    description:
      'SecHub client, in Go, scans synchronously, interrupting build pipelines if needed. Easily integrates into any build system for robust security.',
    icon: PuzzlePieceIcon
  },
  {
    title: 'License Flexibility',
    description:
      'SecHub is MIT licensed, allowing flexible usage, modification, and distribution, fostering collaboration and adoption across diverse projects.',
    icon: CheckBadgeIcon
  },
  {
    title: 'Comprehensive Documentation',
    description:
      'Extensive Asciidoc-format documentation covers Quickstart, Architecture, RestAPI, and more. Available on our documentation.',
    icon: BookOpenIcon
  },
  {
    title: 'Integration with Build Systems',
    description:
      'Seamless integration of SecHub client with every build system simplifies developer workflows, reinforcing security measures without additional complexity.',
    icon: WrenchScrewdriverIcon
  },
  {
    title: 'Product Delegation Server (PDS)',
    description:
      "Explore SecHub's PDS, a vital component providing insights into architecture and functionality, enhancing understanding of the overall security infrastructure.",
    icon: CpuChipIcon
  }
];
</script>

<template>
  <section aria-label="Features of SecHub" class="bg-fern-600 py-32">
    <div class="container">
      <div class="mx-auto max-w-2xl sm:text-center">
        <h2 class="text-3xl font-medium tracking-tight text-white">Your Security Guardian.</h2>
        <p class="mt-2 text-lg text-white">
          SecHub server orchestrates different security tools by one API layer.
          <br />
          Users interact with the SecHub Server, eliminating the need for projects/build pipelines to implement various
          plugins.
        </p>
      </div>

      <ul
        role="list"
        class="mx-auto mt-16 grid max-w-2xl grid-cols-1 gap-6 text-sm sm:mt-20 sm:grid-cols-2 md:gap-y-10 lg:max-w-none lg:grid-cols-3"
      >
        <li
          v-for="item in items"
          :key="item.title"
          class="rounded-2xl border border-fern-300 p-8 transition-colors duration-300 hover:bg-fern-500"
        >
          <Component :is="item.icon" class="size-10 text-white" />
          <h3 class="mt-6 font-semibold text-white">{{ item.title }}</h3>
          <p class="mt-2 text-white">{{ item.description }}</p>
        </li>
      </ul>
    </div>
  </section>
</template>
