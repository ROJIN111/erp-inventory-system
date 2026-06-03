<template>
  <div class="inbound-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑入库单' : '新增入库单' }}</span>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" style="width: 100%">
            <el-option
              v-for="product in productList"
              :key="product.id"
              :label="product.name"
              :value="product.id"
              @click="handleProductChange(product)"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%" />
        </el-form-item>

        <el-form-item label="单价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>

        <el-form-item label="总金额">
          <span>¥{{ totalAmount }}</span>
        </el-form-item>

        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="form.supplier" placeholder="请输入供应商" />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { inboundApi } from '@/api/inbound'
import { productApi } from '@/api/product'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const productList = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  productId: null,
  quantity: 1,
  price: 0,
  supplier: '',
  remark: ''
})

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

const totalAmount = computed(() => {
  if (form.price && form.quantity) {
    return (form.price * form.quantity).toFixed(2)
  }
  return '0.00'
})

const fetchProductList = async () => {
  try {
    const res = await productApi.page({ pageNum: 1, pageSize: 1000 })
    if (res.code === 200) {
      productList.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
  }
}

const handleProductChange = (product) => {
  if (product.price) {
    form.price = product.price
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    const res = await inboundApi.create(form)
    if (res.code === 200) {
      ElMessage.success('创建成功')
      router.push('/inbound/list')
    }
  } catch (error) {
    console.error('提交失败:', error)
  }
}

const handleCancel = () => {
  router.push('/inbound/list')
}

onMounted(() => {
  fetchProductList()
})
</script>

<style scoped>
.inbound-form {
  max-width: 600px;
  margin: 0 auto;
}
</style>
