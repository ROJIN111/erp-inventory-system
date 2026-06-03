<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth">
      <Sidebar @toggle="handleSidebarToggle" />
    </el-aside>
    <el-container>
      <el-header>
        <Navbar />
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'

const isCollapsed = ref(false)
const sidebarWidth = computed(() => isCollapsed.value ? '64px' : '200px')

const handleSidebarToggle = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  transition: all 0.3s;
}

.el-aside {
  background-color: #304156;
  color: #fff;
  overflow-x: hidden;
  transition: width 0.3s;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  transition: all 0.3s;
}
</style>
