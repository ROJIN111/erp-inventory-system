<template>
  <div class="inbound-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>入库单列表</span>
          <div class="header-right">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索入库单号、供应商"
              style="width: 300px; margin-right: 10px"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button v-if="hasPermission('inbound:create')" type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增入库
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" :row-class-name="getRowClassName">
        <el-table-column prop="orderNo" label="入库单号" width="180" />
        <el-table-column prop="productName" label="商品名称" width="150" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已完成' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0 && hasPermission('inbound:audit')"
              type="success"
              size="small"
              @click="handleAudit(row)"
            >
              审核
            </el-button>
            <el-button v-if="hasPermission('inbound:delete')" type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download } from '@element-plus/icons-vue'
import { inboundApi } from '@/api/inbound'
import { hasPermission } from '@/utils/auth'
import { downloadWithAuth } from '@/utils/download'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await inboundApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取入库单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleExport = async () => {
  try {
    await downloadWithAuth('/api/export/inbound', 'inbound-orders.xlsx')
    ElMessage.success('入库单导出成功')
  } catch (error) {
    console.error('导出入库单失败:', error)
    ElMessage.error('导出入库单失败')
  }
}

const handleAdd = () => {
  router.push('/inbound/add')
}

const getRowClassName = ({ row }) => {
  if (route.query.highlight && row.orderNo === route.query.highlight) {
    return 'highlight-row'
  }
  return ''
}

const handleAudit = async (row) => {
  try {
    await ElMessageBox.confirm('确定要审核通过该入库单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await inboundApi.audit(row.id)
    if (res.code === 200) {
      ElMessage.success('审核成功')
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核入库单失败:', error)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该入库单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await inboundApi.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除入库单失败:', error)
    }
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchData()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  fetchData()
}

watch(
  () => route.query.keyword,
  keyword => {
    searchKeyword.value = typeof keyword === 'string' ? keyword : ''
    pagination.pageNum = 1
    fetchData()
  },
  { immediate: true }
)
</script>

<style scoped>
.inbound-list {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.highlight-row) {
  background: rgba(59, 130, 246, 0.12);
}
</style>
