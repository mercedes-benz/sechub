<!-- SPDX-License-Identifier: MIT -->
<script setup>
import { Menu, MenuButton, MenuItems, MenuItem } from '@headlessui/vue';
import { ChevronDownIcon } from '@heroicons/vue/20/solid';

const links = [
  {
    name: 'Client',
    href: 'https://mercedes-benz.github.io/sechub/latest/client-download.html'
  },
  {
    name: 'Server',
    href: 'https://mercedes-benz.github.io/sechub/latest/server-download.html'
  },
  {
    name: 'Product Delegation Server',
    href: 'https://mercedes-benz.github.io/sechub/latest/pds-download.html'
  },
  {
    name: 'Kubernetes Images and Charts',
    href: 'https://github.com/mercedes-benz/sechub/packages'
  }
];
</script>

<template>
  <section aria-label="Download Sechub" id="download" class="relative bg-gray-100 py-32">
    <AbstractCircularAnimation />

    <div class="container">
      <div class="mx-auto max-w-md sm:text-center">
        <h2 class="text-3xl font-medium tracking-tight text-gray-900 sm:text-4xl">Start Using Today!</h2>
        <p class="mt-4 text-lg text-gray-700">
          Download SecHub for seamless security integration.<br>Orchestrates security tools through an unified API.<br>MIT License.
        </p>

        <Menu as="div" class="relative mt-8 inline-block text-left">
          <div>
            <MenuButton class="button">
              <a href="https://mercedes-benz.github.io/sechub/index-old.html">
                Downloads
              </a>
            </MenuButton>
          </div>
        </Menu>
        <!-- Re-enable this block once issue #3744 is solved
        <Menu as="div" class="relative mt-8 inline-block text-left">
          <div>
            <MenuButton class="button">
              Download
              <ChevronDownIcon class="-mr-1 ml-2 h-5 w-5 text-white hover:text-fern-100" aria-hidden="true" />
            </MenuButton>
          </div>

          <transition
            enter-active-class="transition duration-100 ease-out"
            enter-from-class="transform scale-95 opacity-0"
            enter-to-class="transform scale-100 opacity-100"
            leave-active-class="transition duration-75 ease-in"
            leave-from-class="transform scale-100 opacity-100"
            leave-to-class="transform scale-95 opacity-0"
          >
            <MenuItems
              class="absolute right-0 mt-2 w-56 origin-top-right divide-y divide-gray-100 rounded-md bg-white shadow-lg ring-1 ring-black/5 focus:outline-none"
            >
              <MenuItem v-slot="{ active }" v-for="link in links" :key="link.name">
                <NuxtLink
                  :to="link.href"
                  target="_blank"
                  class="group flex w-full rounded-md px-2 py-2 text-sm text-gray-700 transition-colors duration-300 hover:bg-fern-500 hover:text-white"
                >
                  {{ link.name }}
                </NuxtLink>
              </MenuItem>
            </MenuItems>
          </transition>
        </Menu>
        -->
      </div>
    </div>
  </section>
</template>
