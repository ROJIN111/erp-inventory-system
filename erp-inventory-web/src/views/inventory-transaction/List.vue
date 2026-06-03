<template>
  <div class="transaction-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <div class="page-title">库存流水账</div>
            <div class="page-subtitle">按商品、分类、经手人和关联单据追踪库存变化，既能回溯原因，也能直接跳到原始业务单据。</div>
          </div>
          <div class="page-actions">
            <el-button type="primary" :icon="Download" :loading="exporting" @click="handleExport">
              导出筛选结果
            </el-button>
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

      <div class="toolbar">
        <el-select v-model="queryParams.productId" clearable filterable placeholder="选择商品" style="width: 220px">
          <el-option
            v-for="product in productList"
            :key="product.id"
            :label="product.name || `商品 #${product.id}`"
            :value="product.id"
          />
        </el-select>

        <el-select v-model="queryParams.category" clearable filterable placeholder="商品分类" style="width: 180px">
          <el-option v-for="category in categoryOptions" :key="category" :label="category" :value="category" />
        </el-select>

        <el-select v-model="queryParams.transactionType" clearable placeholder="交易类型" style="width: 160px">
          <el-option label="入库" :value="1" />
          <el-option label="出库" :value="2" />
          <el-option label="盘盈调整" :value="3" />
          <el-option label="盘亏调整" :value="4" />
        </el-select>

        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          style="width: 360px"
        />
      </div>

      <div class="toolbar">
        <el-input v-model="queryParams.sku" clearable placeholder="SKU" style="width: 180px" />
        <el-input v-model="queryParams.relatedNo" clearable placeholder="关联单据号" style="width: 200px" />
        <el-input v-model="queryParams.operatorName" clearable placeholder="经手人" style="width: 160px" />
        <el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="搜索流水号、商品名、备注"
          style="width: 260px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <div class="shortcut-row">
        <span class="shortcut-label">快捷时间</span>
        <el-button text @click="applyQuickRange('today')">今天</el-button>
        <el-button text @click="applyQuickRange('sevenDays')">近 7 天</el-button>
        <el-button text @click="applyQuickRange('thirtyDays')">近 30 天</el-button>
        <el-button text @click="applyQuickRange('clear')">清空时间</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe row-key="id">
        <el-table-column prop="transactionNo" label="流水号" min-width="190" />
        <el-table-column label="商品名称" min-width="190">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewProduct(row)">
              {{ row.productName || '未知商品' }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="sku" label="SKU" width="150" />
        <el-table-column label="分类" width="140">
          <template #default="{ row }">
            {{ resolveCategory(row) }}
          </template>
        </el-table-column>
        <el-table-column label="交易类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTransactionTypeTag(row.transactionType)" effect="plain">
              {{ getTransactionTypeText(row.transactionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动量" width="110" align="right">
          <template #default="{ row }">
            <span :class="['change-text', isIncrease(row.transactionType) ? 'is-increase' : 'is-decrease']">
              {{ formatQuantity(row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeStock" label="变动前库存" width="120" align="right" />
        <el-table-column prop="afterStock" label="变动后库存" width="120" align="right" />
        <el-table-column label="关联单据" min-width="190">
          <template #default="{ row }">
            <el-button v-if="row.relatedNo" link type="primary" @click="handleOpenRelated(row)">
              {{ resolveRelatedNo(row) }}
            </el-button>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="经手人" width="130" />
        <el-table-column prop="createTime" label="发生时间" width="170" />
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        class="pagination"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { inventoryTransactionApi, type InventoryTransaction, type InventoryTransactionQuery } from '@/api/inventoryTransaction'
import { productApi, type Product } from '@/api/product'

type QuickRange = 'today' | 'sevenDays' | 'thirtyDays' | 'clear'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const exporting = ref(false)
const tableData = ref<InventoryTransaction[]>([])
const total = ref(0)
const productList = ref<Product[]>([])
const dateRange = ref<string[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  productId: undefined as number | undefined,
  transactionType: undefined as number | undefined,
  sku: '',
  category: '',
  relatedNo: '',
  operatorName: '',
  keyword: ''
})

const categoryOptions = computed(() => {
  const categories = new Set<string>()
  productList.value.forEach(product => {
    if (product.category) {
      categories.add(product.category)
    }
  })
  return Array.from(categories)
})

const productCategoryMap = computed(() => {
  const map = new Map<number, string>()
  productList.value.forEach(product => {
    if (typeof product.id === 'number' && product.category) {
      map.set(product.id, product.category)
    }
  })
  return map
})

const summaryCards = computed(() => {
  const increaseQuantity = tableData.value
    .filter(item => isIncrease(item.transactionType))
    .reduce((sum, item) => sum + Number(item.quantity || 0), 0)

  const decreaseQuantity = tableData.value
    .filter(item => !isIncrease(item.transactionType))
    .reduce((sum, item) => sum + Number(item.quantity || 0), 0)

  const productCount = new Set(
    tableData.value.map(item => item.productId).filter((value): value is number => typeof value === 'number')
  ).size

  const operatorCount = new Set(
    tableData.value.map(item => item.operatorName).filter((value): value is string => Boolean(value))
  ).size

  return [
    {
      key: 'total',
      label: '命中记录数',
      value: `${total.value}`,
      tip: `涉及商品 ${productCount} 个`,
      tone: 'tone-primary'
    },
    {
      key: 'increase',
      label: '本页增加量',
      value: `${increaseQuantity}`,
      tip: '入库与盘盈调整累计增加',
      tone: 'tone-success'
    },
    {
      key: 'decrease',
      label: '本页减少量',
      value: `${decreaseQuantity}`,
      tip: '出库与盘亏调整累计减少',
      tone: 'tone-danger'
    },
    {
      key: 'operators',
      label: '经手人数',
      value: `${operatorCount}`,
      tip: '本页出现过库存变动的操作人',
      tone: 'tone-neutral'
    }
  ]
})

function buildQueryParams(): InventoryTransactionQuery {
  return {
    pageNum: queryParams.pageNum,
    pageSize: queryParams.pageSize,
    productId: queryParams.productId,
    transactionType: queryParams.transactionType,
    sku: queryParams.sku || undefined,
    category: queryParams.category || undefined,
    relatedNo: queryParams.relatedNo || undefined,
    operatorName: queryParams.operatorName || undefined,
    keyword: queryParams.keyword || undefined,
    startTime: dateRange.value[0],
    endTime: dateRange.value[1]
  }
}

function applyRouteQuery() {
  const { query } = route
  const productId = Number(query.productId)
  const transactionType = Number(query.transactionType)

  queryParams.pageNum = Number(query.pageNum || 1)
  queryParams.pageSize = Number(query.pageSize || 10)
  queryParams.productId = Number.isFinite(productId) && productId > 0 ? productId : undefined
  queryParams.transactionType = Number.isFinite(transactionType) ? transactionType : undefined
  queryParams.sku = typeof query.sku === 'string' ? query.sku : ''
  queryParams.category = typeof query.category === 'string' ? query.category : ''
  queryParams.relatedNo = typeof query.relatedNo === 'string' ? query.relatedNo : ''
  queryParams.operatorName = typeof query.operatorName === 'string' ? query.operatorName : ''
  queryParams.keyword = typeof query.keyword === 'string' ? query.keyword : ''

  if (typeof query.startTime === 'string' && typeof query.endTime === 'string') {
    dateRange.value = [query.startTime, query.endTime]
  } else {
    dateRange.value = []
  }
}

async function fetchProducts() {
  try {
    const res: any = await productApi.list()
    productList.value = Array.isArray(res.data) ? res.data : []
  } catch {
    ElMessage.error('获取商品列表失败')
  }
}

async function fetchTransactions() {
  loading.value = true
  try {
    const res: any = await inventoryTransactionApi.page(buildQueryParams())
    tableData.value = res.data.records || []
    total.value = Number(res.data.total || 0)
  } catch (error: any) {
    ElMessage.error(error?.message || '获取库存流水失败')
  } finally {
    loading.value = false
  }
}

function getTransactionTypeText(type?: number) {
  const map: Record<number, string> = {
    1: '入库',
    2: '出库',
    3: '盘盈调整',
    4: '盘亏调整'
  }
  return type ? map[type] || '未知类型' : '未知类型'
}

function getTransactionTypeTag(type?: number) {
  const map: Record<number, 'success' | 'danger' | 'primary' | 'warning'> = {
    1: 'success',
    2: 'danger',
    3: 'primary',
    4: 'warning'
  }
  return type ? map[type] || 'primary' : 'primary'
}

function isIncrease(type?: number) {
  return type === 1 || type === 3
}

function formatQuantity(row: InventoryTransaction) {
  const quantity = Number(row.quantity || 0)
  return `${isIncrease(row.transactionType) ? '+' : '-'}${quantity}`
}

function resolveRelatedNo(row: InventoryTransaction) {
  if (!row.relatedNo) {
    return '--'
  }

  const typeLabel = {
    1: '入库单',
    2: '出库单',
    3: '盘点单',
    4: '盘点单'
  }[row.transactionType || 0]

  return typeLabel ? `${typeLabel} ${row.relatedNo}` : row.relatedNo
}

function resolveCategory(row: InventoryTransaction) {
  if (!row.productId) {
    return '--'
  }
  return productCategoryMap.value.get(row.productId) || '--'
}

function applyQuickRange(range: QuickRange) {
  if (range === 'clear') {
    dateRange.value = []
    return
  }

  const now = new Date()
  const start = new Date(now)

  if (range === 'today') {
    start.setHours(0, 0, 0, 0)
  }
  if (range === 'sevenDays') {
    start.setDate(start.getDate() - 6)
    start.setHours(0, 0, 0, 0)
  }
  if (range === 'thirtyDays') {
    start.setDate(start.getDate() - 29)
    start.setHours(0, 0, 0, 0)
  }

  dateRange.value = [formatDateTime(start), formatDateTime(now)]
}

function formatDateTime(date: Date) {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes()
  )}:${pad(date.getSeconds())}`
}

function handleSearch() {
  queryParams.pageNum = 1
  void fetchTransactions()
}

function handleReset() {
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  queryParams.productId = undefined
  queryParams.transactionType = undefined
  queryParams.sku = ''
  queryParams.category = ''
  queryParams.relatedNo = ''
  queryParams.operatorName = ''
  queryParams.keyword = ''
  dateRange.value = []
  void fetchTransactions()
}

async function handleExport() {
  exporting.value = true
  try {
    await inventoryTransactionApi.export(buildQueryParams())
    ElMessage.success('库存流水导出成功')
  } catch (error: any) {
    ElMessage.error(error?.message || '导出库存流水失败')
  } finally {
    exporting.value = false
  }
}

function handleSizeChange(size: number) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  void fetchTransactions()
}

function handleCurrentChange(page: number) {
  queryParams.pageNum = page
  void fetchTransactions()
}

function handleViewProduct(row: InventoryTransaction) {
  if (!row.productId) {
    return
  }
  router.push(`/product/view/${row.productId}`)
}

function handleOpenRelated(row: InventoryTransaction) {
  if (!row.relatedNo) {
    return
  }

  if (row.transactionType === 1) {
    router.push({
      path: '/inbound/list',
      query: {
        keyword: row.relatedNo,
        highlight: row.relatedNo
      }
    })
    return
  }

  if (row.transactionType === 2) {
    router.push({
      path: '/outbound/list',
      query: {
        keyword: row.relatedNo,
        highlight: row.relatedNo
      }
    })
    return
  }

  if ((row.transactionType === 3 || row.transactionType === 4) && row.relatedId) {
    router.push(`/inventory-check/view/${row.relatedId}`)
  }
}

async function initPage() {
  applyRouteQuery()
  await Promise.all([fetchProducts(), fetchTransactions()])
}

onMounted(() => {
  void initPage()
})
</script>

<style scoped>
.transaction-page {
  padding: 20px;
}

.page-card,
.summary-card {
  border-radius: 16px;
}

.page-header,
.page-actions,
.toolbar,
.shortcut-row {
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
  color: #1f2937;
  font-size: 20px;
  font-weight: 700;
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

.toolbar {
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.shortcut-row {
  gap: 10px;
  margin-bottom: 16px;
  color: #6b7280;
  font-size: 13px;
}

.shortcut-label {
  color: #4b5563;
  font-weight: 600;
}

.change-text {
  font-weight: 700;
}

.is-increase {
  color: #147a58;
}

.is-decrease {
  color: #c2410c;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.tone-primary {
  color: #1d4ed8;
}

.tone-success {
  color: #147a58;
}

.tone-danger {
  color: #c2410c;
}

.tone-neutral {
  color: #22304a;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .transaction-page {
    padding: 12px;
  }

  .page-header,
  .toolbar,
  .shortcut-row {
    flex-direction: column;
    align-items: stretch;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>

