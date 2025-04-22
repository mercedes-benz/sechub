<template>
  <div />
</template>
<script lang="ts">
// this is only a placeholder site to enable auto routing to /login
// /login should be forwarded to the backend server by the webserver (local dev -> vite, deployment -> nginx)

  export default {

    setup () {
      window.location.href = '/login' // trigger the proxy and forward to the backend
    },
  }

</script>
