<template>
  <div class="inventory-check-form" v-loading="loading">
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <div>
            <div class="page-title">{{ pageTitle }}</div>
            <div class="page-subtitle">{{ pageSubtitle }}</div>
          </div>
          <div class="header-actions">
            <el-button @click="handleBack">返回</el-button>
            <template v-if="!isView && form.status === 0">
              <el-button type="primary" :loading="saving" @click="handleSave('draft')">保存草稿</el-button>
              <el-button type="success" :loading="saving" @click="handleSave('submit')">保存并提交</el-button>
            </template>
            <el-button v-if="isView && form.status === 1" type="success" :loading="saving" @click="handleAudit">
              审核通过
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

      <el-alert class="logic-alert" :type="logicAlertType" :closable="false" show-icon :title="logicAlertTitle" />

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="check-form">
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <el-form-item v-if="isEdit || isView" label="盘点单号">
              <el-input v-model="form.checkNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="盘点日期" prop="checkDate">
              <el-date-picker
                v-model="form.checkDate"
                type="datetime"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择盘点日期"
                :disabled="isView"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :xs="24" :md="8">
            <el-form-item label="盘点方式" prop="checkMode">
              <el-select v-model="form.checkMode" placeholder="请选择盘点方式" :disabled="isView" style="width: 100%">
                <el-option v-for="item in modeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="盘点范围" prop="scopeName">
              <el-input v-model="form.scopeName" placeholder="例如：A仓一区 / 门店前场" :disabled="isView" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="盲盘标记">
              <el-radio-group v-model="form.blindCheck" :disabled="isView">
                <el-radio-button :label="0">普通盘点</el-radio-button>
                <el-radio-button :label="1">盲盘</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col v-if="isEdit || isView" :xs="24" :md="8">
            <el-form-item label="状态">
              <el-tag :type="getStatusType(form.status)">{{ getStatusText(form.status) }}</el-tag>
            </el-form-item>
          </el-col>
          <el-col v-if="isView" :xs="24" :md="8">
            <el-form-item label="快照时间">
              <el-input :model-value="form.snapshotTime || '--'" disabled />
            </el-form-item>
          </el-col>
          <el-col v-if="isView" :xs="24" :md="8">
            <el-form-item label="审核时间">
              <el-input :model-value="form.reviewTime || '--'" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="补充盘点背景、范围说明或特殊情况"
            :disabled="isView"
          />
        </el-form-item>
      </el-form>

      <div v-if="isView && analysisOverviewCards.length" class="analysis-wrap">
        <div class="analysis-overview-grid">
          <el-card v-for="card in analysisOverviewCards" :key="card.key" shadow="hover" class="analysis-overview-card">
            <div class="summary-label">{{ card.label }}</div>
            <div class="summary-value" :class="card.tone">{{ card.value }}</div>
            <div class="summary-tip">{{ card.tip }}</div>
          </el-card>
        </div>

        <el-row :gutter="20">
          <el-col :xs="24" :lg="16">
            <el-card shadow="never" class="analysis-card">
              <template #header>
                <div class="section-title">账面库存 vs 实盘库存</div>
              </template>
              <div ref="compareChartRef" class="chart-box"></div>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="8">
            <el-card shadow="never" class="analysis-card">
              <template #header>
                <div class="section-title">差异原因结构</div>
              </template>
              <div ref="reasonChartRef" class="chart-box chart-box-small"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" class="analysis-card ranking-card">
          <template #header>
            <div class="section-title">差异商品排名</div>
          </template>
          <el-empty
            v-if="!analysisData?.differenceRanking?.length"
            description="当前盘点单没有需要重点追踪的差异商品"
          />
          <el-table v-else :data="analysisData?.differenceRanking || []" border stripe size="small">
            <el-table-column type="index" label="#" width="60" align="center" />
            <el-table-column prop="productName" label="商品名称" min-width="180" />
            <el-table-column prop="sku" label="商品编码" width="150" />
            <el-table-column prop="systemStock" label="账面库存" width="110" align="center" />
            <el-table-column prop="actualStock" label="实盘库存" width="110" align="center" />
            <el-table-column label="差异" width="100" align="center">
              <template #default="{ row }">
                <span :class="differenceClass(row.difference)">{{ formatDifference(row.difference) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="reasonText" label="差异原因" width="160" />
            <el-table-column prop="remark" label="差异说明" min-width="220" show-overflow-tooltip />
          </el-table>
        </el-card>
      </div>

      <el-divider content-position="left">盘点明细</el-divider>
      <div class="item-toolbar">
        <div class="toolbar-info">
          <span>共 {{ form.items.length }} 个商品</span>
          <span class="toolbar-tip">{{ toolbarTip }}</span>
        </div>
        <div class="toolbar-actions">
          <el-button v-if="isBlindDraftLocked" @click="handleUnlockBlindCheck">完成盲盘并显示差异</el-button>
          <el-button v-if="!isView" type="primary" @click="handleAddItem">
            <el-icon><Plus /></el-icon>
            添加商品
          </el-button>
        </div>
      </div>

      <el-table :data="form.items" border stripe :row-class-name="getRowClassName" empty-text="请先添加盘点商品">
        <el-table-column label="商品名称" min-width="220">
          <template #default="{ row, $index }">
            <el-select
              v-if="!isView"
              v-model="row.productId"
              placeholder="请选择商品"
              filterable
              style="width: 100%"
              @change="handleProductChangeFromSelect($event, $index)"
            >
              <el-option
                v-for="product in productList"
                :key="product.id"
                :label="`${product.name} (${product.sku})`"
                :value="product.id"
              />
            </el-select>
            <span v-else>{{ row.productName || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sku" label="商品编码" width="150" />
        <el-table-column label="账面库存" width="110" align="center">
          <template #default="{ row }">
            <span v-if="isBlindDraftLocked" class="masked-text">盲盘隐藏</span>
            <span v-else>{{ row.systemStock ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实盘库存" width="140" align="center">
          <template #default="{ row, $index }">
            <el-input-number
              v-if="!isView"
              v-model="row.actualStock"
              :min="0"
              :precision="0"
              style="width: 100%"
              @change="calculateDifference($index)"
            />
            <span v-else>{{ row.actualStock ?? '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="差异数量" width="120" align="center">
          <template #default="{ row }">
            <span v-if="isBlindDraftLocked" class="masked-text">待对比</span>
            <span v-else :class="differenceClass(row.difference)">{{ formatDifference(row.difference) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="差异原因" min-width="180">
          <template #default="{ row }">
            <el-select
              v-if="!isView"
              v-model="row.reasonType"
              clearable
              :disabled="!canEditDifferenceDetail(row)"
              :placeholder="getReasonPlaceholder(row)"
              style="width: 100%"
            >
              <el-option v-for="item in reasonOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-tag v-else :type="getReasonTagType(row.reasonType)" effect="plain">
              {{ getReasonText(row.reasonType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="差异说明" min-width="220">
          <template #default="{ row }">
            <el-input
              v-if="!isView"
              v-model="row.remark"
              :disabled="!canEditDifferenceDetail(row)"
              :placeholder="getRemarkPlaceholder(row)"
            />
            <span v-else>{{ row.remark || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!isView" label="操作" width="90" fixed="right">
          <template #default="{ $index }">
            <el-button type="danger" link @click="handleRemoveItem($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="isView" shadow="never" class="timeline-card">
      <template #header>
        <div class="section-title">库存变化时间线</div>
      </template>
      <el-empty
        v-if="!analysisData?.relatedTransactions?.length"
        :description="form.status === 2 ? '审核后没有需要调整的库存差异' : '待审核通过后这里会生成库存变化时间线'"
      />
      <el-timeline v-else class="timeline-list">
        <el-timeline-item
          v-for="item in analysisData?.relatedTransactions || []"
          :key="item.id || item.transactionNo"
          :timestamp="item.createTime"
          :type="getTransactionTimelineType(item.transactionType)"
        >
          <div class="timeline-content">
            <div class="timeline-line-top">
              <span class="timeline-product">{{ item.productName }}</span>
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
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  inventoryCheckApi,
  type InventoryCheck,
  type InventoryCheckAnalysis,
  type InventoryCheckItem,
  type InventoryTransactionRecord
} from '@/api/inventoryCheck'
import { productApi } from '@/api/product'

type ProductOption = {
  id: number
  name: string
  sku: string
  stock?: number
}

type SummarySnapshot = {
  totalItems: number
  differenceItems: number
  profitQuantity: number
  lossQuantity: number
  netChangeQuantity: number
  differenceRate: number
}

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const productList = ref<ProductOption[]>([])
const analysisData = ref<InventoryCheckAnalysis | null>(null)
const blindCompareUnlocked = ref(true)
const compareChartRef = ref<HTMLDivElement | null>(null)
const reasonChartRef = ref<HTMLDivElement | null>(null)

let compareChart: echarts.ECharts | null = null
let reasonChart: echarts.ECharts | null = null

const isView = computed(() => route.name === 'InventoryCheckView')
const isEdit = computed(() => route.name === 'InventoryCheckEdit')

const modeOptions = [
  { label: '静态盘点', value: 1 },
  { label: '动态盘点', value: 2 },
  { label: '循环盘点', value: 3 },
  { label: '抽盘', value: 4 }
]

const reasonOptions = [
  { label: '流程执行问题', value: 1 },
  { label: '记录与操作问题', value: 2 },
  { label: '客观损耗', value: 3 },
  { label: '其他', value: 4 }
]

const rules = {
  checkDate: [{ required: true, message: '请选择盘点日期', trigger: 'change' }],
  checkMode: [{ required: true, message: '请选择盘点方式', trigger: 'change' }],
  scopeName: [{ required: true, message: '请输入盘点范围', trigger: 'blur' }]
}

const form = reactive<InventoryCheck>(createDefaultForm())

const isBlindDraftLocked = computed(() => !isView.value && form.status === 0 && form.blindCheck === 1 && !blindCompareUnlocked.value)

const pageTitle = computed(() => {
  if (isView.value) {
    return '查看盘点单'
  }
  if (isEdit.value) {
    return '编辑盘点单'
  }
  return '新增盘点单'
})

const pageSubtitle = computed(() => {
  if (isView.value) {
    return '查看账实差异、提交快照和审核后的库存变化时间线。'
  }
  if (isBlindDraftLocked.value) {
    return '当前为盲盘草稿，先完成实盘录入，再显示差异并补充原因。'
  }
  return '先记录实盘结果，再补全差异原因和说明后提交审核。'
})

const toolbarTip = computed(() => {
  if (isView.value) {
    return form.status === 2
      ? '时间线展示审核通过后生成的真实库存流水。'
      : '提交后会冻结账面库存快照，审核前会校验是否发生库存漂移。'
  }
  if (isBlindDraftLocked.value) {
    return '盲盘中暂不显示账面库存和差异，先完成实盘录入。'
  }
  return '系统会按当前账面库存自动计算差异，差异项需补全原因和说明。'
})

const logicAlertType = computed(() => {
  if (isBlindDraftLocked.value || form.status === 1) {
    return 'warning'
  }
  if (form.status === 2) {
    return 'success'
  }
  return 'info'
})

const logicAlertTitle = computed(() => {
  if (isBlindDraftLocked.value) {
    return '盲盘草稿已隐藏账面库存与差异，请先完成实盘录入，再解锁对比并补充差异原因。'
  }
  if (isView.value && form.status === 1) {
    return `该盘点单已于 ${form.snapshotTime || '--'} 冻结账面库存快照，审核前会校验库存是否发生漂移。`
  }
  if (isView.value && form.status === 2) {
    return '该盘点单已审核通过，库存调整已落地，并生成可追溯的库存流水时间线。'
  }
  return '盘点的核心不是做单据，而是完成账实核对、差异归因、审批调整和复盘追踪。'
})

const currentSummary = computed<SummarySnapshot>(() => {
  if (isView.value && analysisData.value?.overview) {
    const overview = analysisData.value.overview
    return {
      totalItems: overview.totalItems || 0,
      differenceItems: overview.differenceItems || 0,
      profitQuantity: overview.profitQuantity || 0,
      lossQuantity: overview.lossQuantity || 0,
      netChangeQuantity: overview.netChangeQuantity || 0,
      differenceRate: Number(overview.differenceRate || 0)
    }
  }

  let differenceItems = 0
  let profitQuantity = 0
  let lossQuantity = 0

  form.items.forEach(item => {
    const difference = item.difference ?? 0
    if (difference !== 0) {
      differenceItems += 1
    }
    if (difference > 0) {
      profitQuantity += difference
    }
    if (difference < 0) {
      lossQuantity += Math.abs(difference)
    }
  })

  const totalItems = form.items.length
  const differenceRate = totalItems ? Number(((differenceItems / totalItems) * 100).toFixed(1)) : 0

  return {
    totalItems,
    differenceItems,
    profitQuantity,
    lossQuantity,
    netChangeQuantity: profitQuantity - lossQuantity,
    differenceRate
  }
})

const summaryCards = computed(() => {
  const summary = currentSummary.value
  const masked = isBlindDraftLocked.value

  return [
    {
      key: 'totalItems',
      label: '盘点商品数',
      value: `${summary.totalItems}`,
      tip: form.scopeName || '未填写盘点范围',
      tone: 'tone-neutral'
    },
    {
      key: 'differenceItems',
      label: '差异商品数',
      value: masked ? '盲盘中' : `${summary.differenceItems}`,
      tip: masked ? '完成盲盘后显示差异结果' : `差异率 ${summary.differenceRate}%`,
      tone: masked ? 'tone-neutral' : summary.differenceItems > 0 ? 'tone-warning' : 'tone-success'
    },
    {
      key: 'profitQuantity',
      label: '盘盈数量',
      value: masked ? '盲盘中' : formatSignedNumber(summary.profitQuantity),
      tip: masked ? '解锁对比后显示盘盈结果' : '审核通过后计入库存增加',
      tone: masked ? 'tone-neutral' : 'tone-success'
    },
    {
      key: 'lossQuantity',
      label: '盘亏数量',
      value: masked ? '盲盘中' : `-${summary.lossQuantity}`,
      tip: masked ? '解锁对比后显示盘亏结果' : `净调整 ${formatSignedNumber(summary.netChangeQuantity)}`,
      tone: masked ? 'tone-neutral' : summary.lossQuantity > 0 ? 'tone-danger' : 'tone-neutral'
    }
  ]
})

const analysisOverviewCards = computed(() => {
  if (!analysisData.value?.overview) {
    return []
  }

  const overview = analysisData.value.overview
  return [
    {
      key: 'differenceRate',
      label: '差异率',
      value: `${overview.differenceRate || 0}%`,
      tip: `${overview.differenceItems || 0} / ${overview.totalItems || 0} 商品存在差异`,
      tone: Number(overview.differenceRate || 0) > 10 ? 'tone-danger' : (overview.differenceItems || 0) > 0 ? 'tone-warning' : 'tone-success'
    },
    {
      key: 'netChangeQuantity',
      label: '净调整数量',
      value: formatSignedNumber(overview.netChangeQuantity || 0),
      tip: `盘盈 ${formatSignedNumber(overview.profitQuantity || 0)}，盘亏 -${overview.lossQuantity || 0}`,
      tone: (overview.netChangeQuantity || 0) >= 0 ? 'tone-success' : 'tone-danger'
    },
    {
      key: 'snapshotTime',
      label: '提交快照',
      value: overview.snapshotTime || '--',
      tip: '提交时冻结账面库存，防止审核按错账面数调整',
      tone: 'tone-neutral'
    },
    {
      key: 'reviewTime',
      label: '审核状态',
      value: getStatusText(overview.status),
      tip: overview.reviewTime || '待审核',
      tone: overview.status === 2 ? 'tone-success' : overview.status === 1 ? 'tone-warning' : 'tone-neutral'
    }
  ]
})

function createDefaultForm(): InventoryCheck {
  return {
    id: undefined,
    checkNo: '',
    checkDate: formatDateTime(new Date()),
    checkMode: 1,
    scopeName: '',
    blindCheck: 0,
    status: 0,
    remark: '',
    snapshotTime: '',
    reviewTime: '',
    items: []
  }
}

function formatDateTime(date: Date) {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes()
  )}:${pad(date.getSeconds())}`
}

function createEmptyItem(): InventoryCheckItem {
  return {
    productId: null,
    productName: '',
    sku: '',
    systemStock: 0,
    actualStock: null,
    difference: null,
    reasonType: null,
    remark: ''
  }
}

function resetFormState() {
  Object.assign(form, createDefaultForm())
  form.items = []
  analysisData.value = null
  blindCompareUnlocked.value = true
  formRef.value?.clearValidate()
}

function applyCheckData(data: InventoryCheck) {
  form.id = data.id
  form.checkNo = data.checkNo || ''
  form.checkDate = data.checkDate || formatDateTime(new Date())
  form.checkMode = data.checkMode || 1
  form.scopeName = data.scopeName || ''
  form.blindCheck = data.blindCheck ?? 0
  form.status = data.status ?? 0
  form.remark = data.remark || ''
  form.snapshotTime = data.snapshotTime || ''
  form.reviewTime = data.reviewTime || ''
  form.items = (data.items || []).map(item => ({
    id: item.id,
    checkId: item.checkId,
    productId: item.productId,
    productName: item.productName,
    sku: item.sku,
    systemStock: item.systemStock ?? 0,
    actualStock: item.actualStock ?? null,
    difference: item.difference ?? null,
    reasonType: item.reasonType ?? null,
    remark: item.remark || ''
  }))
  restoreBlindCompareState()
}

function restoreBlindCompareState() {
  if (isView.value || form.status !== 0 || form.blindCheck !== 1) {
    blindCompareUnlocked.value = true
    return
  }

  const hasCompared = form.items.some(item => {
    const hasReason = item.reasonType !== null && item.reasonType !== undefined
    const hasRemark = Boolean(item.remark && item.remark.trim())
    return hasReason || hasRemark
  })
  blindCompareUnlocked.value = hasCompared
}

function disposeCharts() {
  compareChart?.dispose()
  reasonChart?.dispose()
  compareChart = null
  reasonChart = null
}

function getStatusType(status?: number) {
  const map: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return map[status ?? 0] || 'info'
}

function getStatusText(status?: number) {
  const map: Record<number, string> = {
    0: '草稿',
    1: '待审核',
    2: '已审核',
    3: '已作废'
  }
  return map[status ?? -1] || '未知'
}

function getReasonText(reasonType?: number | null) {
  const map: Record<number, string> = {
    1: '流程执行问题',
    2: '记录与操作问题',
    3: '客观损耗',
    4: '其他'
  }
  if (!reasonType) {
    return '待归因'
  }
  return map[reasonType] || '待归因'
}

function getReasonTagType(reasonType?: number | null) {
  const map: Record<number, string> = {
    1: 'warning',
    2: 'primary',
    3: 'danger',
    4: 'info'
  }
  return map[reasonType ?? 0] || 'info'
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

function formatTransactionChange(item: InventoryTransactionRecord) {
  const prefix = item.transactionType === 1 || item.transactionType === 3 ? '+' : '-'
  return `${prefix}${item.quantity ?? 0}`
}

function differenceClass(difference?: number | null) {
  if (difference === null || difference === undefined) {
    return 'difference-neutral'
  }
  if (difference > 0) {
    return 'difference-positive'
  }
  if (difference < 0) {
    return 'difference-negative'
  }
  return 'difference-neutral'
}

function formatDifference(difference?: number | null) {
  if (difference === null || difference === undefined) {
    return '--'
  }
  if (difference > 0) {
    return `+${difference}`
  }
  return `${difference}`
}

function formatSignedNumber(value: number) {
  if (value > 0) {
    return `+${value}`
  }
  return `${value}`
}

function canEditDifferenceDetail(item: InventoryCheckItem) {
  return !isView.value && !isBlindDraftLocked.value && (item.difference ?? 0) !== 0
}

function getReasonPlaceholder(item: InventoryCheckItem) {
  if (isBlindDraftLocked.value) {
    return '完成盲盘后选择差异原因'
  }
  if ((item.difference ?? 0) === 0) {
    return '无差异无需填写'
  }
  return '差异时必填'
}

function getRemarkPlaceholder(item: InventoryCheckItem) {
  if (isBlindDraftLocked.value) {
    return '完成盲盘后补充差异说明'
  }
  if ((item.difference ?? 0) === 0) {
    return '无差异无需填写'
  }
  return '说明差异原因或现场情况'
}

function getRowClassName({ row }: { row: InventoryCheckItem }) {
  if (isBlindDraftLocked.value) {
    return ''
  }
  return (row.difference ?? 0) !== 0 ? 'difference-row' : ''
}

async function fetchProducts() {
  try {
    const res = await productApi.list()
    productList.value = res.data || []
  } catch (error) {
    console.error('获取商品列表失败', error)
  }
}

async function initPage() {
  loading.value = true
  try {
    resetFormState()
    const routeId = Number(route.params.id)

    if (Number.isFinite(routeId) && routeId > 0) {
      if (isView.value) {
        const res = await inventoryCheckApi.analysis(routeId)
        analysisData.value = res.data
        applyCheckData(res.data.check)
        await nextTick()
        renderViewCharts()
      } else {
        const res = await inventoryCheckApi.getById(routeId)
        applyCheckData(res.data)
      }
      return
    }

    form.items = [createEmptyItem()]
    disposeCharts()
  } finally {
    loading.value = false
  }
}

function handleProductChangeFromSelect(value: unknown, index: number) {
  const productId = Number(value)
  if (!Number.isFinite(productId)) {
    return
  }
  handleProductChange(productId, index)
}

function handleProductChange(productId: number, index: number) {
  const currentItem = form.items[index]
  if (!currentItem) {
    return
  }

  const product = productList.value.find(item => item.id === productId)
  if (!product) {
    form.items[index] = createEmptyItem()
    return
  }

  currentItem.productId = product.id
  currentItem.productName = product.name
  currentItem.sku = product.sku
  currentItem.systemStock = Number(product.stock ?? 0)
  calculateDifference(index)
}

function calculateDifference(index: number) {
  const item = form.items[index]
  if (!item) {
    return
  }

  if (item.actualStock === null || item.actualStock === undefined) {
    item.difference = null
    item.reasonType = null
    item.remark = ''
    return
  }

  const systemStock = Number(item.systemStock ?? 0)
  const actualStock = Number(item.actualStock ?? 0)
  item.difference = actualStock - systemStock

  if (item.difference === 0) {
    item.reasonType = null
    item.remark = ''
  }
}

function handleAddItem() {
  form.items.push(createEmptyItem())
}

function handleRemoveItem(index: number) {
  form.items.splice(index, 1)
}

function validateItems() {
  if (!form.items.length) {
    ElMessage.warning('请至少添加一条盘点明细')
    return false
  }

  const productIds = new Set<number>()
  for (let index = 0; index < form.items.length; index += 1) {
    const item = form.items[index]
    if (!item) {
      continue
    }
    if (!item.productId) {
      ElMessage.warning(`第 ${index + 1} 行未选择商品`)
      return false
    }
    if (item.actualStock === null || item.actualStock === undefined || Number(item.actualStock) < 0) {
      ElMessage.warning(`第 ${index + 1} 行请录入有效的实盘库存`)
      return false
    }
    if (productIds.has(Number(item.productId))) {
      ElMessage.warning('同一商品不能重复盘点')
      return false
    }
    productIds.add(Number(item.productId))
  }

  return true
}

function validateDifferenceDetails() {
  for (let index = 0; index < form.items.length; index += 1) {
    const item = form.items[index]
    if (!item) {
      continue
    }
    if ((item.difference ?? 0) === 0) {
      continue
    }
    if (!item.reasonType) {
      ElMessage.warning(`第 ${index + 1} 行存在差异，请先选择差异原因`)
      return false
    }
    if (!item.remark || !item.remark.trim()) {
      ElMessage.warning(`第 ${index + 1} 行存在差异，请补充差异说明`)
      return false
    }
  }
  return true
}

function buildPayload(): InventoryCheck {
  return {
    id: form.id,
    checkNo: form.checkNo,
    checkDate: form.checkDate,
    checkMode: form.checkMode,
    scopeName: form.scopeName.trim(),
    blindCheck: form.blindCheck,
    status: form.status,
    remark: form.remark?.trim() || '',
    items: form.items.map(item => ({
      productId: item.productId,
      actualStock: item.actualStock,
      reasonType: (item.difference ?? 0) === 0 ? null : item.reasonType,
      remark: (item.difference ?? 0) === 0 ? '' : item.remark?.trim() || ''
    }))
  }
}

async function persistCheck() {
  const payload = buildPayload()
  if (isEdit.value) {
    const res = await inventoryCheckApi.update(payload)
    return Number(res.data.id)
  }
  const res = await inventoryCheckApi.create(payload)
  return Number(res.data.id)
}

async function handleSave(action: 'draft' | 'submit') {
  if (!formRef.value) {
    return
  }

  if (action === 'submit') {
    if (isBlindDraftLocked.value) {
      ElMessage.warning('盲盘提交前需要先完成对比，显示差异并补全原因。')
      return
    }
    if (!validateDifferenceDetails()) {
      return
    }
    try {
      await ElMessageBox.confirm('提交后将冻结账面库存快照，草稿数据不再允许直接修改。', '提交盘点单', {
        type: 'warning',
        confirmButtonText: '确认提交',
        cancelButtonText: '取消'
      })
    } catch {
      return
    }
  }

  try {
    await formRef.value.validate()
    if (!validateItems()) {
      return
    }
    if (action === 'submit' && !validateDifferenceDetails()) {
      return
    }

    saving.value = true
    const savedId = await persistCheck()

    if (action === 'submit') {
      await inventoryCheckApi.submit(savedId)
      ElMessage.success('盘点单已提交，账面库存快照已冻结')
      await router.replace(`/inventory-check/view/${savedId}`)
      return
    }

    ElMessage.success(isEdit.value ? '盘点单已更新' : '盘点单已创建为草稿')
    if (!isEdit.value) {
      await router.replace(`/inventory-check/edit/${savedId}`)
      return
    }
    await initPage()
  } catch (error: any) {
    if (error?.checkDate || error?.checkMode || error?.scopeName) {
      ElMessage.warning('请先填写完整的盘点日期、盘点方式和盘点范围')
      return
    }
    console.error('保存盘点单失败', error)
  } finally {
    saving.value = false
  }
}

async function handleAudit() {
  if (!form.id) {
    return
  }

  try {
    await ElMessageBox.confirm(
      '审核时会校验当前库存是否仍等于提交快照，若期间发生出入库或其他调整，将阻止审核并要求重新盘点。',
      '审核盘点单',
      {
        type: 'warning',
        confirmButtonText: '确认审核',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }

  saving.value = true
  try {
    await inventoryCheckApi.audit(form.id)
    ElMessage.success('盘点单已审核，库存调整和时间线已生成')
    await initPage()
  } catch (error) {
    console.error('审核盘点单失败', error)
  } finally {
    saving.value = false
  }
}

async function handleUnlockBlindCheck() {
  try {
    await ElMessageBox.confirm(
      '完成盲盘后将显示账面库存和差异结果，差异项需要补全原因与说明后才能提交。',
      '完成盲盘',
      {
        type: 'warning',
        confirmButtonText: '显示差异',
        cancelButtonText: '继续盲盘'
      }
    )
  } catch {
    return
  }

  blindCompareUnlocked.value = true
  ElMessage.success('已显示账面库存和差异，请补全差异原因后提交')
}

function handleBack() {
  router.push('/inventory-check/list')
}

function renderViewCharts() {
  renderCompareChart()
  renderReasonChart()
}

function renderCompareChart() {
  if (!compareChartRef.value || !analysisData.value) {
    return
  }
  if (!compareChart) {
    compareChart = echarts.init(compareChartRef.value)
  }

  const source = [...(analysisData.value.check.items || [])]
    .sort((a, b) => Math.abs(b.difference ?? 0) - Math.abs(a.difference ?? 0))
    .slice(0, 8)

  compareChart.setOption({
    color: ['#94a3b8', '#2563eb'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      axisLabel: {
        interval: 0,
        rotate: source.length > 5 ? 25 : 0
      },
      data: source.map(item => item.productName || item.sku || '商品')
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '账面库存',
        type: 'bar',
        data: source.map(item => item.systemStock ?? 0)
      },
      {
        name: '实盘库存',
        type: 'bar',
        data: source.map(item => item.actualStock ?? 0)
      }
    ]
  })
}

function renderReasonChart() {
  if (!reasonChartRef.value || !analysisData.value) {
    return
  }
  if (!reasonChart) {
    reasonChart = echarts.init(reasonChartRef.value)
  }

  const reasonData = analysisData.value.reasonDistribution || []
  reasonChart.setOption({
    tooltip: {
      trigger: 'item'
    },
    color: ['#f59e0b', '#3b82f6', '#ef4444', '#94a3b8'],
    legend: {
      bottom: 0
    },
    series: [
      {
        name: '差异原因',
        type: 'pie',
        radius: ['44%', '72%'],
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
  compareChart?.resize()
  reasonChart?.resize()
}

watch(
  () => form.blindCheck,
  (value, oldValue) => {
    if (isView.value || form.status !== 0) {
      blindCompareUnlocked.value = true
      return
    }
    if (value !== 1) {
      blindCompareUnlocked.value = true
      return
    }
    if (oldValue !== undefined && oldValue !== value) {
      blindCompareUnlocked.value = false
    }
  }
)

watch(
  () => `${String(route.name)}-${String(route.params.id ?? 'new')}`,
  () => {
    void initPage()
  },
  { immediate: true }
)

onMounted(() => {
  void fetchProducts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  disposeCharts()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.inventory-check-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
}

.form-card,
.summary-card,
.analysis-card,
.analysis-overview-card,
.timeline-card {
  border-radius: 18px;
  border: 1px solid #e7ebf2;
}

.summary-card,
.analysis-overview-card,
.analysis-card,
.timeline-card {
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.card-header,
.header-actions,
.toolbar-info,
.toolbar-actions,
.timeline-line-top {
  display: flex;
  align-items: center;
}

.card-header {
  justify-content: space-between;
  gap: 16px;
}

.header-actions,
.toolbar-actions {
  gap: 12px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.page-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.summary-grid,
.analysis-overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.summary-grid {
  margin-bottom: 20px;
}

.analysis-wrap {
  margin: 20px 0 8px;
}

.analysis-overview-grid {
  margin-bottom: 20px;
}

.summary-label {
  font-size: 13px;
  color: #6b7280;
}

.summary-value {
  margin-top: 10px;
  font-size: 30px;
  line-height: 1.2;
  font-weight: 700;
  color: #22304a;
  word-break: break-word;
}

.analysis-overview-card .summary-value {
  font-size: 22px;
}

.summary-tip {
  margin-top: 10px;
  font-size: 13px;
  color: #6b7280;
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

.logic-alert {
  margin-bottom: 20px;
}

.check-form {
  margin-bottom: 10px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.chart-box-small {
  height: 260px;
}

.ranking-card {
  margin-top: 20px;
}

.item-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.toolbar-info {
  gap: 12px;
  flex-wrap: wrap;
  color: #4b5563;
  font-size: 13px;
}

.toolbar-tip {
  color: #6b7280;
}

.masked-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  padding: 2px 10px;
  border-radius: 999px;
  background: #eef2f7;
  color: #94a3b8;
  font-size: 12px;
}

:deep(.difference-row) {
  background: rgba(252, 243, 221, 0.45);
}

.difference-positive {
  color: #147a58;
  font-weight: 700;
}

.difference-negative {
  color: #c2410c;
  font-weight: 700;
}

.difference-neutral {
  color: #6b7280;
  font-weight: 700;
}

.timeline-list {
  max-height: 340px;
  overflow: auto;
  padding-right: 10px;
}

.timeline-content {
  padding-bottom: 6px;
}

.timeline-line-top {
  justify-content: space-between;
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
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.timeline-remark {
  margin-top: 6px;
  color: #374151;
  font-size: 13px;
}

@media (max-width: 1200px) {
  .summary-grid,
  .analysis-overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .inventory-check-form {
    padding: 12px;
  }

  .card-header,
  .item-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions,
  .toolbar-actions {
    width: 100%;
  }

  .summary-grid,
  .analysis-overview-grid {
    grid-template-columns: 1fr;
  }

  .summary-value {
    font-size: 24px;
  }

  .analysis-overview-card .summary-value {
    font-size: 20px;
  }

  .chart-box {
    height: 260px;
  }
}
</style>

