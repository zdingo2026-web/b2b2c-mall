<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('App Launch')
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
/* Global styles */
page {
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 28rpx;
  color: #1E293B;
  background-color: #F9FAFB;
  -webkit-font-smoothing: antialiased;
}

/* Common utility classes */
.container {
  padding: 24rpx;
}

.text-primary {
  color: #2563EB;
}

.text-price {
  color: #E11148;
  font-weight: bold;
}
</style>
