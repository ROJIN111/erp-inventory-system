<template>
  <div class="product-form-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <div class="card-title">{{ pageTitle }}</div>
            <div class="card-subtitle">{{ pageSubtitle }}</div>
          </div>
          <div class="header-actions">
            <el-button v-if="isView && hasPermission('product:update')" type="primary" @click="handleEditCurrent">编辑商品</el-button>
            <el-button v-if="hasDetail && hasPermission('inventory_flow:view')" @click="handleViewFullTransactions">完整流水账</el-button>
          </div>
        </div>
      </template>

      <el-alert
        title="库存预警说明"
        type="info"
        show-icon
        :closable="false"
        class="tips-banner"
      >
        <template #default>
          最低库存用于识别缺货风险，最高库存用于识别积压风险。当前库存低于最低值或高于最高值时，系统会自动生成预警消息。
        </template>
      </el-alert>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="110px"
        class="form-content"
      >
        <div class="form-grid">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入商品名称" :disabled="isView" />
          </el-form-item>

          <el-form-item label="SKU" prop="sku">
            <el-input v-model="formData.sku" placeholder="请输入 SKU" :disabled="isView" />
          </el-form-item>

          <el-form-item label="分类" prop="category">
            <el-input v-model="formData.category" placeholder="请输入分类" :disabled="isView" />
          </el-form-item>

          <el-form-item label="单位" prop="unit">
            <el-input v-model="formData.unit" placeholder="请输入单位，如 件、箱、台" :disabled="isView" />
          </el-form-item>

          <el-form-item label="单价" prop="price">
            <el-input-number
              v-model="formData.price"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              :disabled="isView"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="当前库存" prop="stock">
            <el-input-number
              v-model="formData.stock"
              :min="0"
              :precision="0"
              :step="1"
              controls-position="right"
              :disabled="isView"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="最低库存" prop="minStock">
            <el-input-number
              v-model="formData.minStock"
              :min="0"
              :precision="0"
              :step="1"
              controls-position="right"
              :disabled="isView"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="最高库存" prop="maxStock">
            <el-input-number
              v-model="formData.maxStock"
              :min="0"
              :precision="0"
              :step="1"
              controls-position="right"
              :disabled="isView"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status" :disabled="isView">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入商品描述"
            :disabled="isView"
          />
        </el-form-item>

        <div class="range-preview">
          <span class="preview-label">安全库存区间</span>
          <el-tag type="info" effect="plain">
            {{ formData.minStock }} - {{ formData.maxStock }}
          </el-tag>
          <el-tag :type="warningTag.type" effect="plain">
            {{ warningTag.text }}
          </el-tag>
        </div>

        <el-form-item class="actions">
          <el-button v-if="!isView" type="primary" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建商品' }}
          </el-button>
          <el-button @click="handleBack">{{ isView ? '返回列表' : '取消' }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="hasDetail && hasPermission('inventory_flow:view')" shadow="never" class="history-card">
      <template #header>
        <div class="card-header">
          <div>
            <div class="card-title history-title">商品库存流水</div>
            <div class="card-subtitle">直接查看这个商品最近的库存变化、经手人和关联单据，快速定位异常来源。</div>
          </div>
          <div class="header-actions">
            <el-button @click="handleViewFullTransactions">查看完整流水账</el-button>
          </div>
        </div>
      </template>

      <div class="history-summary-grid">
        <el-card v-for="card in historyCards" :key="card.key" shadow="hover" class="history-summary-card">
          <div class="summary-label">{{ card.label }}</div>
          <div class="summary-value" :class="card.tone">{{ card.value }}</div>
          <div class="summary-tip">{{ card.tip }}</div>
        </el-card>
      </div>

      <el-table :data="historyData" border stripe v-loading="historyLoading" empty-text="暂无库存流水记录">
        <el-table-column prop="transactionNo" label="流水号" min-width="180" />
        <el-table-column label="交易类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTransactionTypeTag(row.transactionType)" effect="plain">
              {{ getTransactionTypeText(row.transactionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动量" width="100" align="right">
          <template #default="{ row }">
            <span :class="['change-text', isIncrease(row.transactionType) ? 'is-increase' : 'is-decrease']">
              {{ formatQuantity(row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="库存变化" width="150" align="center">
          <template #default="{ row }">
            {{ row.beforeStock ?? 0 }} → {{ row.afterStock ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="经手人" width="120" />
        <el-table-column label="关联单据" min-width="180">
          <template #default="{ row }">
            <el-button v-if="row.relatedNo" link type="primary" @click="handleOpenRelated(row)">
              {{ resolveRelatedNo(row) }}
            </el-button>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发生时间" width="170" />
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { inventoryTransactionApi, type InventoryTransaction } from '@/api/inventoryTransaction'
import { productApi } from '@/api/product'
import { hasPermission } from '@/utils/auth'

interface ProductFormModel {
  id: number | null
  name: string
  sku: string
  category: string
  unit: string
  price: number
  stock: number
  minStock: number
  maxStock: number
  status: number
  description: string
}

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const loading = ref(false)
const historyLoading = ref(false)
const historyData = ref<InventoryTransaction[]>([])
const historyTotal = ref(0)
const isEdit = computed(() => route.name === 'ProductEdit')
const isView = computed(() => route.name === 'ProductView')
const hasDetail = computed(() => isEdit.value || isView.value)

const formData = reactive<ProductFormModel>({
  id: null,
  name: '',
  sku: '',
  category: '',
  unit: '',
  price: 0,
  stock: 0,
  minStock: 10,
  maxStock: 1000,
  status: 1,
  description: ''
})

const pageTitle = computed(() => {
  if (isView.value) {
    return '查看商品'
  }
  if (isEdit.value) {
    return '编辑商品'
  }
  return '新增商品'
})

const pageSubtitle = computed(() => {
  if (isView.value) {
    return '查看商品主数据、预警阈值和最近库存流水，确认库存变动来源。'
  }
  return '维护基础资料与库存预警阈值，后续入库、出库和盘点会自动同步预警状态。'
})

const validateMaxStock = (_rule: unknown, value: number, callback: (error?: Error) => void) => {
  if (value < formData.minStock) {
    callback(new Error('最高库存不能小于最低库存'))
    return
  }
  callback()
}

const formRules: FormRules<ProductFormModel> = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  sku: [{ required: true, message: '请输入 SKU', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'change' }],
  stock: [{ required: true, message: '请输入当前库存', trigger: 'change' }],
  minStock: [{ required: true, message: '请输入最低库存', trigger: 'change' }],
  maxStock: [
    { required: true, message: '请输入最高库存', trigger: 'change' },
    { validator: validateMaxStock, trigger: 'change' }
  ]
}

const warningTag = computed(() => {
  if (formData.stock < formData.minStock) {
    return { type: 'danger' as const, text: '当前库存低于下限' }
  }
  if (formData.stock > formData.maxStock) {
    return { type: 'warning' as const, text: '当前库存高于上限' }
  }
  return { type: 'success' as const, text: '当前库存处于安全区间' }
})

const historyCards = computed(() => {
  const increase = historyData.value
    .filter(item => isIncrease(item.transactionType))
    .reduce((sum, item) => sum + Number(item.quantity || 0), 0)
  const decrease = historyData.value
    .filter(item => !isIncrease(item.transactionType))
    .reduce((sum, item) => sum + Number(item.quantity || 0), 0)
  const operatorCount = new Set(
    historyData.value.map(item => item.operatorName).filter((value): value is string => Boolean(value))
  ).size

  return [
    {
      key: 'historyTotal',
      label: '流水总数',
      value: `${historyTotal.value}`,
      tip: '当前商品累计库存变化记录',
      tone: 'tone-neutral'
    },
    {
      key: 'historyIncrease',
      label: '最近增加量',
      value: `${increase}`,
      tip: '入库与盘盈调整累计增加',
      tone: 'tone-success'
    },
    {
      key: 'historyDecrease',
      label: '最近减少量',
      value: `${decrease}`,
      tip: '出库与盘亏调整累计减少',
      tone: 'tone-danger'
    },
    {
      key: 'historyOperators',
      label: '经手人数',
      value: `${operatorCount}`,
      tip: '最近流水涉及的操作人',
      tone: 'tone-primary'
    }
  ]
})

async function fetchDetail(id: number) {
  try {
    const res: any = await productApi.getById(id)
    if (res.code === 200 && res.data) {
      Object.assign(formData, {
        id: Number(res.data.id),
        name: res.data.name || '',
        sku: res.data.sku || '',
        category: res.data.category || '',
        unit: res.data.unit || '',
        price: Number(res.data.price || 0),
        stock: Number(res.data.stock || 0),
        minStock: Number(res.data.minStock ?? 10),
        maxStock: Number(res.data.maxStock ?? 1000),
        status: Number(res.data.status ?? 1),
        description: res.data.description || ''
      })
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
  }
}

async function fetchHistory(id: number) {
  historyLoading.value = true
  try {
    const res: any = await inventoryTransactionApi.page({
      pageNum: 1,
      pageSize: 8,
      productId: id
    })
    historyData.value = res.data.records || []
    historyTotal.value = Number(res.data.total || 0)
  } catch (error) {
    console.error('获取商品库存流水失败:', error)
  } finally {
    historyLoading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  loading.value = true
  try {
    const payload = {
      ...formData,
      id: formData.id ?? undefined
    }
    const res: any = isEdit.value ? await productApi.update(payload) : await productApi.add(payload)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '商品已更新' : '商品已创建')
      handleBack()
    }
  } catch (error) {
    console.error('提交商品失败:', error)
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push('/product/list')
}

function handleEditCurrent() {
  if (!formData.id) {
    return
  }
  router.push(`/product/edit/${formData.id}`)
}

function handleViewFullTransactions() {
  if (!formData.id) {
    return
  }
  router.push({
    path: '/inventory-transaction',
    query: {
      productId: String(formData.id)
    }
  })
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

onMounted(() => {
  const id = Number(route.params.id)
  if (id) {
    void Promise.all([fetchDetail(id), fetchHistory(id)])
  }
})
</script>

<style scoped>
.product-form-page {
  max-width: 980px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.card-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.history-title {
  font-size: 18px;
}

.card-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tips-banner {
  margin-bottom: 24px;
}

.form-content {
  padding-top: 4px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.range-preview {
  margin: 4px 0 20px 110px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.preview-label {
  color: #6b7280;
  font-size: 13px;
}

.actions :deep(.el-form-item__content) {
  margin-left: 110px !important;
}

.history-card,
.history-summary-card {
  border-radius: 16px;
}

.history-summary-grid {
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

.change-text {
  font-weight: 700;
}

.is-increase {
  color: #147a58;
}

.is-decrease {
  color: #c2410c;
}

.tone-neutral {
  color: #22304a;
}

.tone-success {
  color: #147a58;
}

.tone-danger {
  color: #c2410c;
}

.tone-primary {
  color: #1d4ed8;
}

@media (max-width: 900px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .history-summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .product-form-page {
    max-width: none;
  }

  .form-grid,
  .history-summary-grid {
    grid-template-columns: 1fr;
  }

  .range-preview {
    margin-left: 0;
  }

  .actions :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }
}
</style>
