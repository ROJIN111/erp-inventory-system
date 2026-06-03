<template>
  <div class="user-list-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <div class="card-title">用户列表</div>
            <div class="card-subtitle">管理员可以在这里查看角色分配、启停账号，并模拟不同岗位权限边界。</div>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="150" />
        <el-table-column prop="roleName" label="角色" width="180">
          <template #default="{ row }">
            <el-select
              v-if="hasPermission('user:update') && !isSuperAdmin(row)"
              v-model="row.roleId"
              size="small"
              style="width: 100%"
              @change="handleChangeRole(row)"
            >
              <el-option v-for="role in roleOptions" :key="role.id" :label="role.roleName" :value="role.id" />
            </el-select>
            <span v-else>{{ row.roleName || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="hasPermission('user:update') && !isSuperAdmin(row)"
              type="primary"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              v-if="hasPermission('user:delete') && !isSuperAdmin(row)"
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <el-tag v-if="isSuperAdmin(row)" type="success" size="small">
              超级管理员
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleApi, type RoleItem } from '@/api/role'
import { userApi, type UserListItem } from '@/api/user'
import { hasPermission } from '@/utils/auth'

const loading = ref(false)
const tableData = ref<UserListItem[]>([])
const roleOptions = ref<RoleItem[]>([])

function isSuperAdmin(row: UserListItem): boolean {
  return row.id === 1 || row.username === 'admin'
}

async function fetchRoles() {
  if (!hasPermission('role:view')) {
    roleOptions.value = []
    return
  }
  const res: any = await roleApi.list()
  roleOptions.value = res.data || []
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await userApi.list()
    tableData.value = res.data || []
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleToggleStatus(row: UserListItem) {
  try {
    await userApi.update({
      id: row.id,
      status: row.status === 1 ? 0 : 1
    })
    ElMessage.success('用户状态已更新')
    await fetchData()
  } catch (error) {
    console.error('更新用户状态失败:', error)
    ElMessage.error('更新用户状态失败')
  }
}

async function handleChangeRole(row: UserListItem) {
  try {
    await userApi.update({
      id: row.id,
      roleId: row.roleId
    })
    ElMessage.success('用户角色已更新')
    await fetchData()
  } catch (error) {
    console.error('更新用户角色失败:', error)
    ElMessage.error('更新用户角色失败')
  }
}

async function handleDelete(row: UserListItem) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await userApi.delete(row.id!)
    ElMessage.success('用户已删除')
    await fetchData()
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除用户失败:', error)
      ElMessage.error(error.message || '删除用户失败')
    }
  }
}

onMounted(() => {
  void Promise.all([fetchRoles(), fetchData()])
})
</script>

<style scoped>
.user-list-page {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.card-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}
</style>
