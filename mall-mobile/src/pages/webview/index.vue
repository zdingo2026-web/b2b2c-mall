<template>
  <view class="page-webview">
    <web-view :src="webUrl" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const webUrl = ref('')

const AGREEMENT_MAP: Record<string, string> = {
  'user-agreement': '/static/agreement/user-agreement.html',
  'privacy-policy': '/static/agreement/privacy-policy.html',
}

onLoad((query) => {
  const url = query?.url || ''
  if (AGREEMENT_MAP[url]) {
    webUrl.value = AGREEMENT_MAP[url]
  } else if (url.startsWith('http')) {
    webUrl.value = decodeURIComponent(url)
  } else {
    webUrl.value = ''
  }
})
</script>

<style scoped>
.page-webview {
  width: 100%;
  height: 100vh;
}
</style>
