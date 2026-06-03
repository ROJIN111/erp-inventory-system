<template>
  <div class="sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
    <div class="sidebar-toggle" @click="toggleSidebar">
      <el-icon v-if="isCollapsed"><DArrowRight /></el-icon>
      <el-icon v-else><DArrowLeft /></el-icon>
    </div>

    <div class="sidebar-logo" v-show="!isCollapsed">
      <h3>ERP 系统</h3>
    </div>

    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      :collapse="isCollapsed"
      router
    >
      <el-menu-item v-if="hasPermission('dashboard:view')" index="/dashboard">
        <el-icon><House /></el-icon>
        <template #title>首页</template>
      </el-menu-item>

      <el-sub-menu v-if="hasPermission('product:view')" index="product">
        <template #title>
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </template>
        <el-menu-item index="/product/list">商品列表</el-menu-item>
      </el-sub-menu>

      <el-sub-menu v-if="hasPermission('inbound:view')" index="inbound">
        <template #title>
          <el-icon><Download /></el-icon>
          <span>入库管理</span>
        </template>
        <el-menu-item index="/inbound/list">入库单列表</el-menu-item>
      </el-sub-menu>

      <el-sub-menu v-if="hasPermission('outbound:view')" index="outbound">
        <template #title>
          <el-icon><Upload /></el-icon>
          <span>出库管理</span>
        </template>
        <el-menu-item index="/outbound/list">出库单列表</el-menu-item>
      </el-sub-menu>

      <el-sub-menu v-if="hasPermission('inventory_check:view')" index="inventory-check">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>库存盘点</span>
        </template>
        <el-menu-item index="/inventory-check/list">盘点单列表</el-menu-item>
      </el-sub-menu>

      <el-menu-item v-if="hasPermission('inventory_flow:view')" index="/inventory-transaction">
        <el-icon><Document /></el-icon>
        <template #title>库存流水账</template>
      </el-menu-item>

      <el-sub-menu v-if="hasAnyPermission(['warning:view', 'warning:config'])" index="warning">
        <template #title>
          <el-icon><Bell /></el-icon>
          <span>库存预警</span>
        </template>
        <el-menu-item v-if="hasPermission('warning:config')" index="/warning/config">预警配置</el-menu-item>
        <el-menu-item v-if="hasPermission('warning:view')" index="/warning/list">预警消息</el-menu-item>
      </el-sub-menu>

      <el-sub-menu v-if="hasPermission('user:view')" index="user">
        <template #title>
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </template>
        <el-menu-item index="/user/list">用户列表</el-menu-item>
      </el-sub-menu>

      <el-menu-item v-if="hasPermission('log:view')" index="/log">
        <el-icon><Tickets /></el-icon>
        <template #title>操作日志</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { hasAnyPermission, hasPermission } from '@/utils/auth'
import {
  Bell,
  DArrowLeft,
  DArrowRight,
  Document,
  Download,
  Goods,
  House,
  Tickets,
  Upload,
  User
} from '@element-plus/icons-vue'

const emit = defineEmits<{
  toggle: []
}>()

const route = useRoute()
const activeMenu = computed(() => route.path)
const isCollapsed = ref(false)

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
  emit('toggle')
}
</script>

<style scoped>
.sidebar {
  height: 100%;
  overflow-y: auto;
  transition: width 0.3s;
  position: relative;
}

.sidebar-toggle {
  position: absolute;
  top: 50%;
  right: -12px;
  transform: translateY(-50%);
  width: 24px;
  height: 48px;
  background-color: #304156;
  border-radius: 0 4px 4px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #bfcbd9;
  z-index: 100;
  transition: color 0.3s;
}

.sidebar-toggle:hover {
  color: #409eff;
}

.sidebar-logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  background-color: #263445;
}

.sidebar-logo h3 {
  margin: 0;
}

.sidebar-menu {
  border: none;
}
</style>
