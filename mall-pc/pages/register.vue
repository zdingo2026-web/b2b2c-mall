<template>
  <div>
    <!-- Redirect to login page which has both login and register tabs -->
    <NuxtPage />
  </div>
</template>

<script setup lang="ts">
// Redirect to login - the login page handles both login and register
navigateTo('/login', { replace: true })
</script>
