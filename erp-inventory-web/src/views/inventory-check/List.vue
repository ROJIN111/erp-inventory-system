<template>
  <div class="inventory-check-list" v-loading="pageLoading || dashboardLoading">
    <el-card shadow="never" class="flow-card">
      <div class="flow-header">
        <div>
          <div class="panel-title">盘点闭环总览</div>
          <div class="panel-subtitle">盘点不是改单据，而是完成账实核对、差异追溯、审批调整和复盘跟踪。</div>
        </div>
        <div class="flow-tip">差异率 {{ dashboard.summary.differenceRate }}%</div>
      </div>
      <div class="flow-steps">
        <div v-for="item in flowSteps" :key="item.step" class="flow-step">
          <div class="flow-index">{{ item.step }}</div>
          <div class="flow-name">{{ item.title }}</div>
          <div class="flow-desc">{{ item.desc }}</div>
        </div>
      </div>
    </el-card>

    <div class="summary-grid">
      <el-card v-for="card in summaryCards" :key="card.key" shadow="hover" class="summary-card">
        <div class="summary-label">{{ card.label }}</div>
        <div class="summary-value" :class="card.tone">{{ card.value }}</div>
        <div class="summary-tip">{{ card.tip }}</div>
      </el-card>
    </div>

    <el-row :gutter="20" class="visual-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="section-header">
              <div>
                <div class="panel-title">库存变化趋势</div>
                <div class="panel-subtitle">按真实库存流水汇总，直接展示变化量和变化时间。</div>
              </div>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="section-header">
              <div>
                <div class="panel-title">差异原因分布</div>
                <div class="panel-subtitle">帮助判断问题来自流程、记录还是客观损耗。</div>
              </div>
            </div>
          </template>
          <div ref="reasonChartRef" class="chart-container chart-container-small"></div>
          <div class="mode-wrap">
            <div class="mode-title">盘点方式分布</div>
            <div class="mode-tags">
              <el-tag
                v-for="item in dashboard.modeDistribution"
                :key="item.key"
                class="mode-tag"
                effect="plain"
              >
                {{ item.label }} {{ item.value }}
              </el-tag>
              <span v-if="!dashboard.modeDistribution.length" class="empty-text">暂无盘点数据</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="timeline-card">
      <template #header>
        <div class="section-header">
          <div>
            <div class="panel-title">最近库存变化时间线</div>
            <div class="panel-subtitle">来源于入库、出库和盘点审核后的库存流水，可用来判断盘点前后是否发生过库存漂移。</div>
          </div>
        </div>
      </template>
      <el-empty v-if="!dashboard.recentTransactions.length" description="暂无库存变化记录" />
      <el-timeline v-else class="timeline-list">
        <el-timeline-item
          v-for="item in dashboard.recentTransactions"
          :key="item.id || item.transactionNo"
          :timestamp="item.createTime"
          :type="getTransactionTimelineType(item.transactionType)"
        >
          <div class="timeline-content">
            <div class="timeline-top">
              <span class="timeline-product">{{ item.productName || '未知商品' }}</span>
              <span :class="['timeline-change', getTransactionChangeClass(item.transactionType)]">
                {{ formatTransactionChange(item) }}
              </span>
            </div>
            <div class="timeline-meta">
              <span>{{ getTransactionTypeText(item.transactionType) }}</span>
              <span>库存 {{ item.beforeStock ?? 0 }} → {{ item.afterStock ?? 0 }}</span>
              <span>经手 {{ item.operatorName || '--' }}</span>
              <span>{{ item.relatedNo || '--' }}</span>
            </div>
            <div v-if="item.remark" class="timeline-remark">{{ item.remark }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="table-header">
          <div>
            <div class="panel-title">盘点单列表</div>
            <div class="panel-subtitle">看清每张盘点单的范围、方式、差异规模和审批结果。</div>
          </div>
          <div class="table-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索盘点单号、盘点范围或备注"
              clearable
              style="width: 320px"
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增盘点
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="pageLoading">
        <el-table-column prop="checkNo" label="盘点单号" width="190" />
        <el-table-column prop="scopeName" label="盘点范围" min-width="180" />
        <el-table-column label="盘点方式" width="120">
          <template #default="{ row }">
            <el-tag :type="getModeTagType(row.checkMode)">{{ getCheckModeText(row.checkMode) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="盲盘" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.blindCheck === 1 ? 'warning' : 'info'" effect="plain">
              {{ row.blindCheck === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkDate" label="盘点日期" width="170" />
        <el-table-column prop="snapshotTime" label="快照时间" width="170" />
        <el-table-column label="差异摘要" min-width="260">
          <template #default="{ row }">
            <div class="metric-cell">
              <span>{{ row.totalItems || 0 }} 项</span>
              <span class="metric-warn">差异 {{ row.differenceItems || 0 }}</span>
              <span class="metric-gain">盘盈 +{{ row.profitQuantity || 0 }}</span>
              <span class="metric-loss">盘亏 -{{ row.lossQuantity || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="净调整" width="110" align="center">
          <template #default="{ row }">
            <span :class="getNetChangeClass(row.netChangeQuantity)">
              {{ formatSignedNumber(row.netChangeQuantity || 0) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewTime" label="审核时间" width="170" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 0" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link @click="handleSubmit(row)">提交审核</el-button>
            <el-button v-if="row.status === 1" type="success" link @click="handleAudit(row)">审核</el-button>
            <el-button v-if="row.status === 0 || row.status === 1" type="warning" link @click="handleCancel(row)">
              作废
            </el-button>
            <el-button v-if="row.status === 0" type="danger" link @click="handleDelete(row)">删除</el-button>
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
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  inventoryCheckApi,
  type InventoryCheck,
  type InventoryCheckDashboard,
  type InventoryTransactionRecord
} from '@/api/inventoryCheck'

const router = useRouter()

const pageLoading = ref(false)
const dashboardLoading = ref(false)
const tableData = ref<InventoryCheck[]>([])
const searchKeyword = ref('')
const trendChartRef = ref<HTMLDivElement | null>(null)
const reasonChartRef = ref<HTMLDivElement | null>(null)

let trendChart: echarts.ECharts | null = null
let reasonChart: echarts.ECharts | null = null

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const dashboard = ref<InventoryCheckDashboard>({
  summary: {
    totalChecks: 0,
    pendingChecks: 0,
    approvedChecks: 0,
    differenceChecks: 0,
    totalItems: 0,
    differenceItems: 0,
    pendingDifferenceItems: 0,
    confirmedProfitQuantity: 0,
    confirmedLossQuantity: 0,
    confirmedNetChangeQuantity: 0,
    differenceRate: 0
  },
  modeDistribution: [],
  reasonDistribution: [],
  trend: [],
  recentTransactions: []
})

const flowSteps = [
  { step: '01', title: '账实核对', desc: '记录盘点范围内的账面库存与实盘库存差异。' },
  { step: '02', title: '差异追溯', desc: '对盘盈盘亏逐项归因，区分流程、记录或损耗问题。' },
  { step: '03', title: '审批调整', desc: '审核通过后按差异量调整库存，并生成库存流水。' },
  { step: '04', title: '复盘优化', desc: '复看高风险商品和重复差异，持续优化仓储流程。' }
]

const summaryCards = computed(() => {
  const summary = dashboard.value.summary
  return [
    {
      key: 'totalChecks',
      label: '累计盘点单',
      value: summary.totalChecks,
      tip: `已审核 ${summary.approvedChecks} 单`,
      tone: 'tone-neutral'
    },
    {
      key: 'pendingChecks',
      label: '待审核盘点',
      value: summary.pendingChecks,
      tip: `待处理差异 ${summary.pendingDifferenceItems} 项`,
      tone: 'tone-warning'
    },
    {
      key: 'differenceChecks',
      label: '出现差异单据',
      value: summary.differenceChecks,
      tip: `差异项 ${summary.differenceItems} / 总项数 ${summary.totalItems}`,
      tone: 'tone-danger'
    },
    {
      key: 'confirmedNetChangeQuantity',
      label: '已确认净调整',
      value: formatSignedNumber(summary.confirmedNetChangeQuantity),
      tip: `盘盈 +${summary.confirmedProfitQuantity} / 盘亏 -${summary.confirmedLossQuantity}`,
      tone: summary.confirmedNetChangeQuantity >= 0 ? 'tone-success' : 'tone-danger'
    }
  ]
})

const statusTypeMap: Record<number, string> = {
  0: 'info',
  1: 'warning',
  2: 'success',
  3: 'danger'
}

const statusTextMap: Record<number, string> = {
  0: '草稿',
  1: '待审核',
  2: '已审核',
  3: '已作废'
}

function getStatusType(status?: number) {
  return statusTypeMap[status ?? 0] || 'info'
}

function getStatusText(status?: number) {
  return statusTextMap[status ?? -1] || '未知'
}

function getCheckModeText(mode?: number) {
  const map: Record<number, string> = {
    1: '静态盘点',
    2: '动态盘点',
    3: '循环盘点',
    4: '抽盘'
  }
  return map[mode ?? 0] || '未设置'
}

function getModeTagType(mode?: number) {
  const map: Record<number, string> = {
    1: 'success',
    2: 'warning',
    3: 'primary',
    4: 'info'
  }
  return map[mode ?? 0] || 'info'
}

function getTransactionTypeText(type?: number) {
  const map: Record<number, string> = {
    1: '入库增加',
    2: '出库减少',
    3: '盘盈调整',
    4: '盘亏调整'
  }
  return map[type ?? 0] || '未知变动'
}

function getTransactionTimelineType(type?: number) {
  const map: Record<number, string> = {
    1: 'success',
    2: 'danger',
    3: 'primary',
    4: 'warning'
  }
  return map[type ?? 0] || 'info'
}

function getTransactionChangeClass(type?: number) {
  if (type === 1 || type === 3) {
    return 'timeline-change-positive'
  }
  if (type === 2 || type === 4) {
    return 'timeline-change-negative'
  }
  return ''
}

function formatSignedNumber(value: number) {
  if (value > 0) {
    return `+${value}`
  }
  return `${value}`
}

function getNetChangeClass(value?: number) {
  if ((value ?? 0) > 0) {
    return 'net-change-positive'
  }
  if ((value ?? 0) < 0) {
    return 'net-change-negative'
  }
  return 'net-change-neutral'
}

function formatTransactionChange(item: InventoryTransactionRecord) {
  const quantity = item.quantity ?? 0
  const prefix = item.transactionType === 1 || item.transactionType === 3 ? '+' : '-'
  return `${prefix}${quantity}`
}

async function fetchPageData() {
  pageLoading.value = true
  try {
    const res = await inventoryCheckApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value.trim() || undefined
    })
    tableData.value = res.data.records || []
    pagination.total = Number(res.data.total || 0)
  } finally {
    pageLoading.value = false
  }
}

async function fetchDashboardData() {
  dashboardLoading.value = true
  try {
    const res = await inventoryCheckApi.dashboard()
    dashboard.value = res.data || dashboard.value
    await nextTick()
    renderCharts()
  } finally {
    dashboardLoading.value = false
  }
}

async function refreshAll() {
  await Promise.all([fetchPageData(), fetchDashboardData()])
}

function renderCharts() {
  renderTrendChart()
  renderReasonChart()
}

function renderTrendChart() {
  if (!trendChartRef.value) {
    return
  }
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const trend = dashboard.value.trend || []
  trendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: trend.map(item => item.label)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '入库',
        type: 'bar',
        stack: 'inventory',
        itemStyle: { color: '#3f9b6d' },
        data: trend.map(item => item.inbound)
      },
      {
        name: '出库',
        type: 'bar',
        stack: 'inventory',
        itemStyle: { color: '#dd5a3d' },
        data: trend.map(item => -item.outbound)
      },
      {
        name: '盘盈',
        type: 'bar',
        stack: 'inventory',
        itemStyle: { color: '#3f7af8' },
        data: trend.map(item => item.checkGain)
      },
      {
        name: '盘亏',
        type: 'bar',
        stack: 'inventory',
        itemStyle: { color: '#f0a23b' },
        data: trend.map(item => -item.checkLoss)
      },
      {
        name: '净变化',
        type: 'line',
        smooth: true,
        symbolSize: 8,
        itemStyle: { color: '#222222' },
        data: trend.map(item => item.netChange)
      }
    ]
  })
}

function renderReasonChart() {
  if (!reasonChartRef.value) {
    return
  }
  if (!reasonChart) {
    reasonChart = echarts.init(reasonChartRef.value)
  }

  const reasonData = dashboard.value.reasonDistribution || []
  reasonChart.setOption({
    tooltip: {
      trigger: 'item'
    },
    legend: {
      bottom: 0
    },
    series: [
      {
        name: '差异原因',
        type: 'pie',
        radius: ['46%', '72%'],
        center: ['50%', '42%'],
        data: reasonData.length
          ? reasonData.map(item => ({ name: item.label, value: item.value }))
          : [{ name: '暂无差异', value: 1 }],
        label: {
          formatter: '{b}\n{d}%'
        }
      }
    ]
  })
}

function handleResize() {
  trendChart?.resize()
  reasonChart?.resize()
}

function handleSearch() {
  pagination.pageNum = 1
  fetchPageData()
}

function handleReset() {
  searchKeyword.value = ''
  pagination.pageNum = 1
  fetchPageData()
}

function handleAdd() {
  router.push('/inventory-check/add')
}

function handleView(row: InventoryCheck) {
  router.push(`/inventory-check/view/${row.id}`)
}

function handleEdit(row: InventoryCheck) {
  router.push(`/inventory-check/edit/${row.id}`)
}

async function handleSubmit(row: InventoryCheck) {
  try {
    await ElMessageBox.confirm('提交后将进入差异审批流程，草稿数据不再允许直接修改。', '提交盘点单', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await inventoryCheckApi.submit(Number(row.id))
    ElMessage.success('盘点单已提交审核')
    await refreshAll()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error(error)
    }
  }
}

async function handleAudit(row: InventoryCheck) {
  try {
    await ElMessageBox.confirm('审核通过后会按差异量调整库存，并生成带时间戳的库存流水。', '审核盘点单', {
      confirmButtonText: '审核通过',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await inventoryCheckApi.audit(Number(row.id))
    ElMessage.success('盘点单审核完成')
    await refreshAll()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error(error)
    }
  }
}

async function handleCancel(row: InventoryCheck) {
  try {
    await ElMessageBox.confirm('作废后该盘点单不再参与审批和库存调整。', '作废盘点单', {
      confirmButtonText: '作废',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await inventoryCheckApi.cancel(Number(row.id))
    ElMessage.success('盘点单已作废')
    await refreshAll()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error(error)
    }
  }
}

async function handleDelete(row: InventoryCheck) {
  try {
    await ElMessageBox.confirm('仅草稿盘点单可以删除，删除后明细会一并移除。', '删除盘点单', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await inventoryCheckApi.delete(Number(row.id))
    ElMessage.success('盘点单已删除')
    await refreshAll()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error(error)
    }
  }
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchPageData()
}

function handleCurrentChange(page: number) {
  pagination.pageNum = page
  fetchPageData()
}

onMounted(async () => {
  await refreshAll()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  trendChart?.dispose()
  reasonChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.inventory-check-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
}

.flow-card,
.chart-card,
.timeline-card,
.table-card {
  border-radius: 18px;
  border: 1px solid #e8ecf2;
}

.flow-card {
  background:
    radial-gradient(circle at top right, rgba(44, 123, 229, 0.08), transparent 28%),
    linear-gradient(135deg, #ffffff 0%, #f5f8ff 100%);
}

.flow-header,
.table-header,
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.panel-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.panel-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.flow-tip {
  padding: 10px 14px;
  border-radius: 999px;
  background: #eef4ff;
  color: #2459d3;
  font-weight: 600;
}

.flow-steps {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.flow-step {
  padding: 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(201, 213, 225, 0.9);
}

.flow-index {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: #3157c6;
}

.flow-name {
  margin-top: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.flow-desc {
  margin-top: 8px;
  line-height: 1.6;
  color: #4b5563;
  font-size: 13px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.summary-card {
  border-radius: 18px;
  border: 1px solid #e8ecf2;
}

.summary-label {
  font-size: 13px;
  color: #6b7280;
}

.summary-value {
  margin-top: 10px;
  font-size: 32px;
  font-weight: 700;
}

.summary-tip {
  margin-top: 10px;
  font-size: 13px;
  color: #6b7280;
}

.tone-neutral {
  color: #22304a;
}

.tone-warning {
  color: #b7791f;
}

.tone-danger {
  color: #c2410c;
}

.tone-success {
  color: #13795b;
}

.visual-row {
  margin: 0;
}

.chart-container {
  width: 100%;
  height: 340px;
}

.chart-container-small {
  height: 250px;
}

.mode-wrap {
  margin-top: 12px;
  border-top: 1px dashed #d8dee8;
  padding-top: 14px;
}

.mode-title {
  font-size: 13px;
  color: #6b7280;
}

.mode-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.mode-tag {
  margin: 0;
}

.empty-text {
  font-size: 13px;
  color: #9ca3af;
}

.timeline-list {
  max-height: 320px;
  overflow: auto;
  padding-right: 10px;
}

.timeline-content {
  padding-bottom: 6px;
}

.timeline-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.timeline-product {
  font-weight: 700;
  color: #1f2937;
}

.timeline-change {
  font-weight: 700;
}

.timeline-change-positive {
  color: #147a58;
}

.timeline-change-negative {
  color: #c2410c;
}

.timeline-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
  font-size: 12px;
  color: #6b7280;
}

.timeline-remark {
  margin-top: 8px;
  line-height: 1.6;
  color: #4b5563;
  font-size: 13px;
}

.table-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.metric-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  line-height: 1.8;
}

.metric-warn {
  color: #b7791f;
  font-weight: 600;
}

.metric-gain {
  color: #147a58;
  font-weight: 600;
}

.metric-loss {
  color: #c2410c;
  font-weight: 600;
}

.net-change-positive {
  color: #147a58;
  font-weight: 700;
}

.net-change-negative {
  color: #c2410c;
  font-weight: 700;
}

.net-change-neutral {
  color: #6b7280;
  font-weight: 700;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .flow-steps {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .inventory-check-list {
    padding: 12px;
  }

  .summary-grid,
  .flow-steps {
    grid-template-columns: 1fr;
  }

  .flow-header,
  .table-header,
  .section-header {
    flex-direction: column;
  }

  .table-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .chart-container {
    height: 280px;
  }
}
</style>


