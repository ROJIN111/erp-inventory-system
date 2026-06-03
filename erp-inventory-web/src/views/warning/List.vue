<template>
  <div class="warning-list-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="page-header">
          <div>
            <div class="page-title">预警消息</div>
            <div class="page-subtitle">把预警从“看到”推进到“谁处理、怎么处理、何时跟进”都可追溯。</div>
          </div>
          <div class="page-actions">
            <el-button :loading="checking" @click="handleCheck">检查预警</el-button>
            <el-button type="success" @click="openBatchHandleDialog">批量处理</el-button>
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
        <el-select v-model="queryParams.status" clearable placeholder="处理状态" style="width: 160px">
          <el-option label="未处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>
        <el-select v-model="queryParams.warningType" clearable placeholder="预警类型" style="width: 160px">
          <el-option label="库存过低" :value="1" />
          <el-option label="库存过高" :value="2" />
        </el-select>
        <el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="搜索商品、SKU、处理人或处理说明"
          style="width: 320px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">查询</el-button>
          </template>
        </el-input>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table ref="tableRef" :data="tableData" border stripe v-loading="loading">
        <el-table-column type="expand" width="50">
          <template #default="{ row }">
            <div class="trace-panel">
              <div class="trace-grid">
                <div class="trace-item">
                  <span class="trace-label">处理来源</span>
                  <span class="trace-value">{{ getHandleSourceText(row) }}</span>
                </div>
                <div class="trace-item">
                  <span class="trace-label">处理方式</span>
                  <span class="trace-value">{{ getHandleTypeText(row.handleType) }}</span>
                </div>
                <div class="trace-item">
                  <span class="trace-label">处理人</span>
                  <span class="trace-value">{{ row.handleByName || '--' }}</span>
                </div>
                <div class="trace-item">
                  <span class="trace-label">责任人</span>
                  <span class="trace-value">{{ row.ownerName || '--' }}</span>
                </div>
                <div class="trace-item">
                  <span class="trace-label">跟进时间</span>
                  <span class="trace-value">{{ row.followUpTime || '--' }}</span>
                </div>
                <div class="trace-item">
                  <span class="trace-label">处理时间</span>
                  <span class="trace-value">{{ row.handleTime || '--' }}</span>
                </div>
              </div>
              <div class="trace-note">
                <span class="trace-label">处理说明</span>
                <div class="trace-note-content">{{ row.handleNote || '尚未处理' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="180" />
        <el-table-column prop="sku" label="SKU" width="150" />
        <el-table-column prop="currentStock" label="当前库存" width="100" align="center" />
        <el-table-column prop="minStock" label="最低库存" width="100" align="center" />
        <el-table-column prop="maxStock" label="最高库存" width="100" align="center" />
        <el-table-column label="预警类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.warningType === 1 ? 'danger' : 'warning'">
              {{ row.warningType === 1 ? '库存过低' : '库存过高' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="预警消息" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'" effect="plain">
              {{ row.status === 0 ? '未处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理来源" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getHandleSourceTagType(row)" effect="plain">
              {{ getHandleSourceText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleByName" label="处理人" width="110" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column prop="handleTime" label="处理时间" width="170" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" link @click="openHandleDialog(row)">
              处理
            </el-button>
            <el-button v-else link @click="expandHandledRow(row)">
              查看留痕
            </el-button>
            <el-button type="danger" link :loading="deletingIds.includes(Number(row.id))" @click="handleDelete(row)">
              删除
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

    <el-dialog
      v-model="handleDialogVisible"
      :title="handleDialogMode === 'single' ? '处理预警消息' : '批量处理预警消息'"
      width="560px"
      destroy-on-close
    >
      <div class="dialog-intro">
        <template v-if="handleDialogMode === 'single' && currentHandlingRow">
          正在处理商品“{{ currentHandlingRow.productName }}”的预警，请填写处置动作并留痕。
        </template>
        <template v-else>
          将一次性处理 {{ summary.pendingWarnings || 0 }} 条未处理预警，以下处置表单会写入所有本次关闭记录。
        </template>
      </div>

      <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="96px">
        <el-form-item label="处理方式" prop="handleType">
          <el-select v-model="handleForm.handleType" placeholder="请选择处理方式" style="width: 100%">
            <el-option v-for="item in handleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任人" prop="ownerName">
          <el-input v-model="handleForm.ownerName" maxlength="50" placeholder="默认使用当前处理人，可改为实际责任人" />
        </el-form-item>
        <el-form-item label="跟进时间" prop="followUpTime">
          <el-date-picker
            v-model="handleForm.followUpTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="可选，用于安排后续跟进"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="处理说明" prop="handleNote">
          <el-input
            v-model="handleForm.handleNote"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请说明已采取的动作、原因判断和后续安排"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="closeHandleDialog">取消</el-button>
        <el-button type="primary" :loading="submittingHandle" @click="submitHandleForm">提交处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type TableInstance } from 'element-plus'
import { warningApi, type ProductWarningMessage, type WarningHandleRequest, type WarningSummary } from '@/api/warning'

type HandleDialogMode = 'single' | 'batch'

const tableRef = ref<TableInstance>()
const handleFormRef = ref<FormInstance>()
const loading = ref(false)
const checking = ref(false)
const deletingIds = ref<number[]>([])
const handleDialogVisible = ref(false)
const submittingHandle = ref(false)
const handleDialogMode = ref<HandleDialogMode>('single')
const currentHandlingRow = ref<ProductWarningMessage | null>(null)
const tableData = ref<ProductWarningMessage[]>([])
const total = ref(0)
const summary = ref<WarningSummary>({
  totalWarnings: 0,
  pendingWarnings: 0,
  handledWarnings: 0,
  lowWarnings: 0,
  highWarnings: 0,
  affectedProducts: 0
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null as number | null,
  warningType: null as number | null,
  keyword: ''
})

const handleForm = reactive<WarningHandleRequest>({
  handleType: 1,
  ownerName: '',
  handleNote: '',
  followUpTime: ''
})

const handleTypeOptions = [
  { label: '补货申请', value: 1 },
  { label: '调拨处理', value: 2 },
  { label: '促销去化', value: 3 },
  { label: '盘点复核', value: 4 },
  { label: '人工确认', value: 5 },
  { label: '其他', value: 6 }
]

const handleRules: FormRules<WarningHandleRequest> = {
  handleType: [{ required: true, message: '请选择处理方式', trigger: 'change' }],
  handleNote: [{ required: true, message: '请填写处理说明', trigger: 'blur' }]
}

const summaryCards = computed(() => [
  {
    key: 'pendingWarnings',
    label: '未处理消息',
    value: `${summary.value.pendingWarnings || 0}`,
    tip: `${summary.value.affectedProducts || 0} 个商品仍处于异常区间`,
    tone: (summary.value.pendingWarnings || 0) > 0 ? 'tone-danger' : 'tone-success'
  },
  {
    key: 'lowWarnings',
    label: '低库存消息',
    value: `${summary.value.lowWarnings || 0}`,
    tip: '库存低于安全下限',
    tone: (summary.value.lowWarnings || 0) > 0 ? 'tone-danger' : 'tone-neutral'
  },
  {
    key: 'highWarnings',
    label: '高库存消息',
    value: `${summary.value.highWarnings || 0}`,
    tip: '库存高于安全上限',
    tone: (summary.value.highWarnings || 0) > 0 ? 'tone-warning' : 'tone-neutral'
  },
  {
    key: 'handledWarnings',
    label: '已处理历史',
    value: `${summary.value.handledWarnings || 0}`,
    tip: `累计消息 ${summary.value.totalWarnings || 0}`,
    tone: 'tone-success'
  }
])

async function fetchTableData() {
  loading.value = true
  try {
    const res: any = await warningApi.page({
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      status: queryParams.status,
      warningType: queryParams.warningType,
      keyword: queryParams.keyword || undefined
    })
    tableData.value = res.data.records || []
    total.value = Number(res.data.total || 0)
  } finally {
    loading.value = false
  }
}

async function fetchSummary() {
  const res: any = await warningApi.summary()
  summary.value = res.data || summary.value
}

async function refreshAll() {
  await Promise.all([fetchTableData(), fetchSummary()])
}

async function handleCheck() {
  checking.value = true
  try {
    await warningApi.check()
    ElMessage.success('预警检查完成')
    await refreshAll()
  } finally {
    checking.value = false
  }
}

function resetHandleForm() {
  handleForm.handleType = 1
  handleForm.ownerName = ''
  handleForm.handleNote = ''
  handleForm.followUpTime = ''
}

function openHandleDialog(row: ProductWarningMessage) {
  currentHandlingRow.value = row
  handleDialogMode.value = 'single'
  resetHandleForm()
  handleDialogVisible.value = true
}

function openBatchHandleDialog() {
  if (!summary.value.pendingWarnings) {
    ElMessage.info('当前没有待处理的预警消息')
    return
  }
  currentHandlingRow.value = null
  handleDialogMode.value = 'batch'
  resetHandleForm()
  handleDialogVisible.value = true
}

function closeHandleDialog() {
  handleDialogVisible.value = false
  submittingHandle.value = false
  currentHandlingRow.value = null
  handleFormRef.value?.clearValidate()
}

async function submitHandleForm() {
  if (!handleFormRef.value) {
    return
  }

  const valid = await handleFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  submittingHandle.value = true
  try {
    const payload: WarningHandleRequest = {
      handleType: Number(handleForm.handleType),
      ownerName: handleForm.ownerName?.trim() || undefined,
      handleNote: handleForm.handleNote.trim(),
      followUpTime: handleForm.followUpTime || undefined
    }

    if (handleDialogMode.value === 'single' && currentHandlingRow.value?.id) {
      await warningApi.handle(Number(currentHandlingRow.value.id), payload)
      ElMessage.success('预警消息已处理并留痕')
    } else {
      await warningApi.handleAll(payload)
      ElMessage.success('未处理预警消息已批量处理并留痕')
    }

    closeHandleDialog()
    await refreshAll()
  } finally {
    submittingHandle.value = false
  }
}

function getHandleSourceText(row: ProductWarningMessage) {
  if (row.status === 0) {
    return '待处理'
  }
  const map: Record<number, string> = {
    1: '人工处理',
    2: '系统恢复',
    3: '系统切换'
  }
  return row.handleSource ? map[row.handleSource] || '已处理' : '已处理'
}

function getHandleSourceTagType(row: ProductWarningMessage) {
  if (row.status === 0) {
    return 'warning'
  }
  if (row.handleSource === 1) {
    return 'success'
  }
  if (row.handleSource === 2 || row.handleSource === 3) {
    return 'info'
  }
  return 'success'
}

function getHandleTypeText(handleType?: number) {
  const option = handleTypeOptions.find(item => item.value === handleType)
  return option?.label || '--'
}

async function expandHandledRow(row: ProductWarningMessage) {
  await nextTick()
  tableRef.value?.toggleRowExpansion(row, true)
}

async function handleDelete(row: ProductWarningMessage) {
  const rowId = Number(row.id)
  if (!rowId) {
    return
  }
  try {
    await ElMessageBox.confirm('删除后这条预警历史将不可恢复。', '删除预警消息', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  deletingIds.value = [...deletingIds.value, rowId]
  try {
    await warningApi.delete(rowId)
    ElMessage.success('预警消息已删除')
    await refreshAll()
  } finally {
    deletingIds.value = deletingIds.value.filter(id => id !== rowId)
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  void fetchTableData()
}

function handleReset() {
  queryParams.pageNum = 1
  queryParams.status = null
  queryParams.warningType = null
  queryParams.keyword = ''
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
.warning-list-page {
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

.toolbar {
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-intro {
  margin-bottom: 16px;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.6;
}

.trace-panel {
  padding: 8px 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.trace-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.trace-item,
.trace-note {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.trace-label {
  color: #6b7280;
  font-size: 12px;
}

.trace-value,
.trace-note-content {
  color: #1f2937;
  font-size: 13px;
  line-height: 1.6;
}

.trace-note {
  margin-top: 14px;
}

.trace-note-content {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  white-space: pre-wrap;
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

  .trace-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .warning-list-page {
    padding: 12px;
  }

  .page-header,
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .summary-grid,
  .trace-grid {
    grid-template-columns: 1fr;
  }
}
</style>


