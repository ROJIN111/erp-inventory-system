<template>
  <div class="product-list-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <div class="card-title">商品列表</div>
            <div class="card-subtitle">在商品主数据中直接维护安全库存区间，预警模块会按阈值自动生成消息。</div>
          </div>

          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品名称、SKU 或分类"
              clearable
              style="width: 280px"
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
            <el-button v-if="hasPermission('warning:config')" @click="handleGoWarningConfig">
              <el-icon><Bell /></el-icon>
              预警配置
            </el-button>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button v-if="hasPermission('product:create')" type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增商品
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="180" />
        <el-table-column prop="sku" label="SKU" width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="单价" width="120" align="right">
          <template #default="{ row }">
            {{ formatCurrency(row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="90" align="center" />
        <el-table-column prop="stock" label="当前库存" width="100" align="center" />
        <el-table-column label="安全库存区间" width="160" align="center">
          <template #default="{ row }">
            {{ Number(row.minStock ?? 10) }} - {{ Number(row.maxStock ?? 1000) }}
          </template>
        </el-table-column>
        <el-table-column label="预警状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getWarningState(row).type" effect="plain">
              {{ getWarningState(row).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'" effect="plain">
              {{ Number(row.status) === 1 ? '正常' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="hasPermission('product:update')" type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="hasPermission('product:delete')" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Download, Plus, Search } from '@element-plus/icons-vue'
import { productApi, type Product } from '@/api/product'
import { hasPermission } from '@/utils/auth'
import { downloadWithAuth } from '@/utils/download'

const router = useRouter()
const loading = ref(false)
const tableData = ref<Product[]>([])
const searchKeyword = ref('')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

function formatCurrency(value?: number) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function getWarningState(row: Product) {
  const stock = Number(row.stock || 0)
  const minStock = Number(row.minStock ?? 10)
  const maxStock = Number(row.maxStock ?? 1000)
  if (stock < minStock) {
    return { type: 'danger' as const, text: '库存过低' }
  }
  if (stock > maxStock) {
    return { type: 'warning' as const, text: '库存过高' }
  }
  return { type: 'success' as const, text: '正常' }
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await productApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = Number(res.data.total || 0)
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  void fetchData()
}

async function handleExport() {
  try {
    await downloadWithAuth('/api/export/products', 'products.xlsx')
    ElMessage.success('商品列表导出成功')
  } catch (error) {
    console.error('导出商品失败:', error)
    ElMessage.error('导出商品失败')
  }
}

function handleGoWarningConfig() {
  router.push('/warning/config')
}

function handleAdd() {
  router.push('/product/add')
}

function handleView(row: Product) {
  router.push(`/product/view/${row.id}`)
}

function handleEdit(row: Product) {
  router.push(`/product/edit/${row.id}`)
}

async function handleDelete(row: Product) {
  try {
    await ElMessageBox.confirm(`确认删除商品“${row.name}”吗？`, '删除商品', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
    const res: any = await productApi.delete(Number(row.id))
    if (res.code === 200) {
      ElMessage.success('商品已删除')
      if (tableData.value.length === 1 && pagination.pageNum > 1) {
        pagination.pageNum -= 1
      }
      await fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
    }
  }
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  void fetchData()
}

function handleCurrentChange(page: number) {
  pagination.pageNum = page
  void fetchData()
}

onMounted(() => {
  void fetchData()
})
</script>

<style scoped>
.product-list-page {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .card-header {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
