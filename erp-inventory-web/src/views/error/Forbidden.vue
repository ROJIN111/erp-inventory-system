<template>
  <div class="forbidden-page">
    <el-result
      icon="warning"
      title="无权限访问"
      sub-title="当前账号没有访问这个页面的权限，请切换账号或联系管理员授权。"
    >
      <template #extra>
        <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.forbidden-page {
  min-height: calc(100vh - 80px);
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
