<template>
    <v-app-bar :elevation="2">
        <v-avatar :tile="true">
            <v-img
            src="@/assets/sechub-logo.png"
            />
        </v-avatar>
        <v-app-bar-title>SecHub</v-app-bar-title>

        <template v-slot:append>
            <v-btn icon="mdi-account"></v-btn>

            <v-btn icon="mdi-logout-variant"></v-btn>

            <v-btn icon="mdi-forum-outline"></v-btn>
        </template>
    </v-app-bar>
</template>