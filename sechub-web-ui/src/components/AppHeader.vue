<!-- SPDX-License-Identifier: MIT -->
<template>
    <v-app-bar>

        <img
        class="ma-2 pa-1"
        src="@/assets/sechub-logo.svg"
        contain
        height="100%"
        ></img>

        <v-container fill-height fluid>
            <v-row align:center
                justify="center">
                <v-col class="pa-0">
                    <div>Welcome</div>
                </v-col>
            </v-row>
            <v-row align:center
                justify="center">
                <v-col class="pa-0">               
                     <v-app-bar-title> {{ username }}</v-app-bar-title>
                </v-col>
            </v-row>
        </v-container>

        <!-- Search Bar
        <v-spacer></v-spacer>

        <v-responsive
        class="mx-auto">
            <v-text-field hide-details solo single-line class="ml-5" 
            rounded
            light variant="outlined" 
            label="search"
            prepend-inner-icon="mdi-magnify" />
        </v-responsive>
        -->

        <template v-slot:append>
            <v-btn icon="mdi-account"></v-btn>

            <v-btn icon="mdi-logout-variant"></v-btn>

            <v-btn icon="mdi-forum-outline"></v-btn>
        </template>
    </v-app-bar>
</template>

<script setup lang="ts">
    const username="Erika Mustermann"
</script>