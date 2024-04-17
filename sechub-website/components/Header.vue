<script lang="ts" setup>
import { Menu, MenuButton, MenuItems, MenuItem } from '@headlessui/vue';
import { Bars3Icon, ChevronDownIcon } from '@heroicons/vue/20/solid';

const documentationItems = [
  {
    title: 'Getting Started',
    href: 'https://mercedes-benz.github.io/sechub/latest/sechub-getting-started.html'
  },
  {
    title: 'Architecture',
    href: 'https://mercedes-benz.github.io/sechub/latest/sechub-architecture.html'
  },
  {
    title: 'Technical',
    href: 'https://mercedes-benz.github.io/sechub/latest/sechub-techdoc.html'
  },
  {
    title: 'Rest API',
    href: 'https://mercedes-benz.github.io/sechub/latest/sechub-restapi.html'
  },
  {
    title: 'Client',
    href: 'https://mercedes-benz.github.io/sechub/latest/sechub-client.html'
  },
  {
    title: 'Operations',
    href: 'https://mercedes-benz.github.io/sechub/latest/sechub-operations.html'
  },
  {
    title: 'Product Delegation Server (PDS)',
    href: 'https://mercedes-benz.github.io/sechub/latest/sechub-product-delegation-server.html'
  },
  {
    title: 'Developer - Quickstart Guide',
    href: 'https://mercedes-benz.github.io/sechub/latest/sechub-developer-quickstart-guide.html'
  }
];

const items = [
  {
    title: 'Downloads',
    href: '#download'
  },
  {
    title: 'Collaboration',
    href: 'https://github.com/mercedes-benz/sechub/blob/develop/CONTRIBUTING.md'
  }
];
</script>

<template>
  <header>
    <nav>
      <div class="relative z-50 mx-auto flex max-w-7xl justify-between px-4 py-4 sm:px-6 lg:px-8">
        <div class="relative z-10 flex items-center gap-16">
          <NuxtLink aria-label="Home" to="/">
            <img src="/logo.svg" class="h-12 w-10 object-contain" alt="SecHub Logo" />
          </NuxtLink>

          <div class="hidden items-center lg:flex lg:gap-10">
            <Menu as="div" class="relative inline-block">
              <div>
                <MenuButton class="menu-item"> Documentation </MenuButton>
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
                  class="absolute left-0 mt-2 w-56 origin-top-left divide-y divide-gray-100 rounded-md bg-white shadow-lg ring-1 ring-black/5 focus:outline-none"
                >
                  <MenuItem v-for="link in documentationItems" :key="link.title">
                    <NuxtLink
                      :to="link.href"
                      target="_blank"
                      class="group flex w-full items-center rounded-md px-2 py-2 text-sm text-gray-700 transition-colors duration-300 hover:bg-fern-500 hover:text-white"
                    >
                      {{ link.title }}
                    </NuxtLink>
                  </MenuItem>
                </MenuItems>
              </transition>
            </Menu>
            <NuxtLink
              v-for="item in items"
              :key="item.title"
              :to="item.href"
              class="menu-item"
              :target="item.href.startsWith('https') ? '_blank' : undefined"
            >
              {{ item.title }}
            </NuxtLink>
          </div>
        </div>
        <div class="flex items-center gap-6">
          <div class="lg:hidden">
            <Menu as="div" class="relative">
              <div class="flex items-center">
                <MenuButton class="menu-item flex">
                  Docs
                  <ChevronDownIcon class="ml-1 h-5 w-5" aria-hidden="true" />
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
                  <MenuItem v-for="link in documentationItems" :key="link.title">
                    <NuxtLink
                      :to="link.href"
                      target="_blank"
                      class="group flex w-full items-center rounded-md px-2 py-2 text-sm text-gray-700 transition-colors duration-300 hover:bg-fern-500 hover:text-white"
                    >
                      {{ link.title }}
                    </NuxtLink>
                  </MenuItem>
                </MenuItems>
              </transition>
            </Menu>
          </div>
          <div class="lg:hidden">
            <Menu as="div" class="relative">
              <div class="flex items-center">
                <MenuButton>
                  <span class="sr-only">Open main menu</span>
                  <Bars3Icon class="size-6" />
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
                  <MenuItem v-for="item in items" :key="item.title">
                    <NuxtLink
                      :to="item.href"
                      class="group flex w-full items-center rounded-md px-2 py-2 text-sm text-gray-700 transition-colors duration-300 hover:bg-fern-500 hover:text-white"
                      :target="item.href.startsWith('https') ? '_blank' : undefined"
                    >
                      {{ item.title }}
                    </NuxtLink>
                  </MenuItem>
                </MenuItems>
              </transition>
            </Menu>
          </div>

          <a href="https://github.com/mercedes-benz/sechub" target="_blank">
            <img src="/github.svg" class="size-6 object-contain" alt="GitHub Logo" />
          </a>
        </div>
      </div>
    </nav>
  </header>
</template>
