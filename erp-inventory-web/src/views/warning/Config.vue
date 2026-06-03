<template>
  <div class="warning-config-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <div class="page-title">库存预警配置</div>
            <div class="page-subtitle">按商品维护最低库存和最高库存阈值，库存变化后系统会自动同步预警状态。</div>
          </div>
          <div class="page-actions">
            <el-button @click="handleOpenMessages">查看预警消息</el-button>
            <el-button type="primary" :loading="checking" @click="handleCheckWarnings">同步预警状态</el-button>
          </div>
        </div>
      </template>

      <div class="summary-grid">
        <el-card v-for="card in summaryCards" :key="card.key" shadow="hover" class="summary-card">
          <div class="summary-label">{{ card.label }}</div>
          <div class="summary-value" :class="card.tone">{{ card.value }}</div>
          <div class="summary-tip">{{ card.tip }}</div>
        </el-card>
      </div>

      <el-alert
        title="配置逻辑"
        type="info"
        show-icon
        :closable="false"
        class="logic-alert"
      >
        <template #default>
          低于最低库存会生成缺货预警，高于最高库存会生成积压预警。阈值保存后，当前商品的预警消息会立刻重新计算。
        </template>
      </el-alert>

      <div class="toolbar">
        <el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="搜索商品名称、SKU 或分类"
          style="width: 320px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">查询</el-button>
          </template>
        </el-input>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="name" label="商品名称" min-width="180" />
        <el-table-column prop="sku" label="SKU" width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="stock" label="当前库存" width="110" align="center" />
        <el-table-column label="最低库存" width="150" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.minStock" :min="0" :precision="0" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="最高库存" width="150" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.maxStock" :min="0" :precision="0" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="预警状态" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="getWarningTagType(row)" effect="plain">
              {{ getWarningText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="建议动作" min-width="220">
          <template #default="{ row }">
            {{ getWarningAdvice(row) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :loading="savingIds.includes(Number(row.id))" @click="handleSaveRow(row)">
              保存阈值
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi, type Product } from '@/api/product'
import { statisticsApi } from '@/api/statistics'
import { warningApi, type WarningSummary } from '@/api/warning'

type WarningProduct = Product & {
  stock: number
  minStock: number
  maxStock: number
}

interface InventorySummary {
  warningCount: number
  lowStockCount: number
  highStockCount: number
}

const router = useRouter()
const loading = ref(false)
const checking = ref(false)
const tableData = ref<WarningProduct[]>([])
const total = ref(0)
const savingIds = ref<number[]>([])

const warningSummary = ref<WarningSummary>({
  totalWarnings: 0,
  pendingWarnings: 0,
  handledWarnings: 0,
  lowWarnings: 0,
  highWarnings: 0,
  affectedProducts: 0
})

const inventorySummary = ref<InventorySummary>({
  warningCount: 0,
  lowStockCount: 0,
  highStockCount: 0
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

const summaryCards = computed(() => [
  {
    key: 'warningCount',
    label: '风险商品数',
    value: `${inventorySummary.value.warningCount || 0}`,
    tip: '当前库存超出阈值区间的商品数量',
    tone: (inventorySummary.value.warningCount || 0) > 0 ? 'tone-danger' : 'tone-success'
  },
  {
    key: 'lowStockCount',
    label: '低库存商品',
    value: `${inventorySummary.value.lowStockCount || 0}`,
    tip: '低于最低库存阈值',
    tone: (inventorySummary.value.lowStockCount || 0) > 0 ? 'tone-danger' : 'tone-neutral'
  },
  {
    key: 'highStockCount',
    label: '高库存商品',
    value: `${inventorySummary.value.highStockCount || 0}`,
    tip: '高于最高库存阈值',
    tone: (inventorySummary.value.highStockCount || 0) > 0 ? 'tone-warning' : 'tone-neutral'
  },
  {
    key: 'pendingWarnings',
    label: '未处理消息',
    value: `${warningSummary.value.pendingWarnings || 0}`,
    tip: '同步后会更新预警消息队列',
    tone: (warningSummary.value.pendingWarnings || 0) > 0 ? 'tone-warning' : 'tone-success'
  }
])

async function fetchTableData() {
  loading.value = true
  try {
    const res = await productApi.page({
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      keyword: queryParams.keyword || undefined
    })
    tableData.value = (res.data.records || []).map((item: Product) => ({
      ...item,
      stock: Number(item.stock || 0),
      minStock: Number(item.minStock ?? 10),
      maxStock: Number(item.maxStock ?? 1000)
    }))
    total.value = Number(res.data.total || 0)
  } finally {
    loading.value = false
  }
}

async function fetchSummaries() {
  const [warningRes, inventoryRes] = await Promise.all([warningApi.summary(), statisticsApi.getInventoryStats()])
  warningSummary.value = warningRes.data || warningSummary.value
  inventorySummary.value = {
    warningCount: Number(inventoryRes.data?.warningCount || 0),
    lowStockCount: Number(inventoryRes.data?.lowStockCount || 0),
    highStockCount: Number(inventoryRes.data?.highStockCount || 0)
  }
}

async function refreshAll() {
  await Promise.all([fetchTableData(), fetchSummaries()])
}

function getWarningState(row: WarningProduct) {
  if (row.stock < row.minStock) {
    return 1
  }
  if (row.stock > row.maxStock) {
    return 2
  }
  return 0
}

function getWarningTagType(row: WarningProduct) {
  const state = getWarningState(row)
  if (state === 1) {
    return 'danger'
  }
  if (state === 2) {
    return 'warning'
  }
  return 'success'
}

function getWarningText(row: WarningProduct) {
  const state = getWarningState(row)
  if (state === 1) {
    return '库存过低'
  }
  if (state === 2) {
    return '库存过高'
  }
  return '正常'
}

function getWarningAdvice(row: WarningProduct) {
  const state = getWarningState(row)
  if (state === 1) {
    return `建议补货，至少补到 ${row.minStock}`
  }
  if (state === 2) {
    return '建议促销、调拨或下调采购频率'
  }
  return '当前库存位于安全区间'
}

async function handleSaveRow(row: WarningProduct) {
  const rowId = Number(row.id)
  if (!rowId) {
    return
  }
  if (row.minStock < 0 || row.maxStock < 0) {
    ElMessage.warning('预警阈值不能小于 0')
    return
  }
  if (row.minStock > row.maxStock) {
    ElMessage.warning('最低库存不能大于最高库存')
    return
  }

  savingIds.value = [...savingIds.value, rowId]
  try {
    await productApi.update({
      id: rowId,
      minStock: row.minStock,
      maxStock: row.maxStock
    })
    ElMessage.success('预警阈值已保存')
    await refreshAll()
  } finally {
    savingIds.value = savingIds.value.filter(id => id !== rowId)
  }
}

async function handleCheckWarnings() {
  checking.value = true
  try {
    await warningApi.check()
    ElMessage.success('预警状态已同步')
    await refreshAll()
  } finally {
    checking.value = false
  }
}

function handleOpenMessages() {
  router.push('/warning/list')
}

function handleSearch() {
  queryParams.pageNum = 1
  void fetchTableData()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.pageNum = 1
  void fetchTableData()
}

function handleSizeChange(size: number) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  void fetchTableData()
}

function handleCurrentChange(page: number) {
  queryParams.pageNum = page
  void fetchTableData()
}

onMounted(() => {
  void refreshAll()
})
</script>

<style scoped>
.warning-config-page {
  padding: 20px;
}

.page-card,
.summary-card {
  border-radius: 16px;
}

.page-header,
.page-actions,
.toolbar {
  display: flex;
  align-items: center;
}

.page-header {
  justify-content: space-between;
  gap: 16px;
}

.page-actions,
.toolbar {
  gap: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.page-subtitle {
  margin-top: 6px;
  color: #6b7280;
  font-size: 13px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.summary-label {
  color: #6b7280;
  font-size: 13px;
}

.summary-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
}

.summary-tip {
  margin-top: 10px;
  color: #6b7280;
  font-size: 13px;
}

.logic-alert {
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.tone-neutral {
  color: #22304a;
}

.tone-success {
  color: #147a58;
}

.tone-warning {
  color: #b7791f;
}

.tone-danger {
  color: #c2410c;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .warning-config-page {
    padding: 12px;
  }

  .page-header,
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>

