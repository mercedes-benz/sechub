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
      'SecHub orchestrates tools via an unified API, simplifying integration for projects without the need for different vendor plugins.',
    icon: ServerStackIcon
  },
  {
    title: 'Build Pipeline Integration',
    description:
      'The SecHub client written in Go handles the server communication. It easily integrates into any build system for robust security.',
    icon: PuzzlePieceIcon
  },
  {
    title: 'License Flexibility',
    description:
      'SecHub is MIT licensed. This ensures freedom in use, modification and distribution, fostering collaboration and adoption across all kind of projects.',
    icon: CheckBadgeIcon
  },
  {
    title: 'Comprehensive Documentation',
    description:
      'Extensive documentation covers Getting Started, Architecture, Client, RestAPI, Operations and more. Available on our documentation.',
    icon: BookOpenIcon
  },
  {
    title: 'Integration with Build Systems',
    description:
      'Seamless integration with every build system and multiple developer IDEs adds security to your code without additional complexity.',
    icon: WrenchScrewdriverIcon
  },
  {
    title: 'Product Delegation Server (PDS)',
    description:
      "Explore SecHub's PDS, a vital component providing you the choice of many existing securtiy tools for use with SecHub. It also allows an easy integration of new securtiy tools.",
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
          Users interact with the SecHub Server, eliminating the need for projects to integrate
          vendor plugins for each security tool.
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
