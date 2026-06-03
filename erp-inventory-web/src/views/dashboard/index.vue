<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.productStats.totalProducts || 0 }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #E6A23C">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.productStats.lowStockCount || 0 }}</div>
              <div class="stat-label">库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon><Download /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.inboundStats.totalQuantity || 0 }}</div>
              <div class="stat-label">入库总量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" style="background: #F56C6C">
              <el-icon><Upload /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.outboundStats.totalQuantity || 0 }}</div>
              <div class="stat-label">出库总量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>商品分类分布</span>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>库存预警商品</span>
          </template>
          <div ref="stockChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>入库统计</span>
          </template>
          <div class="stats-summary">
            <div class="summary-item">
              <span class="label">入库单数：</span>
              <span class="value">{{ stats.inboundStats.totalInbound || 0 }}</span>
            </div>
            <div class="summary-item">
              <span class="label">总金额：</span>
              <span class="value">¥{{ stats.inboundStats.totalAmount || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>出库统计</span>
          </template>
          <div class="stats-summary">
            <div class="summary-item">
              <span class="label">出库单数：</span>
              <span class="value">{{ stats.outboundStats.totalOutbound || 0 }}</span>
            </div>
            <div class="summary-item">
              <span class="label">总金额：</span>
              <span class="value">¥{{ stats.outboundStats.totalAmount || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { Box, Warning, Download, Upload } from '@element-plus/icons-vue'
import { statisticsApi } from '@/api/statistics'

const categoryChartRef = ref()
const stockChartRef = ref()
let categoryChart = null
let stockChart = null

const stats = reactive({
  productStats: {},
  inventoryStats: {},
  inboundStats: {},
  outboundStats: {}
})

const fetchStats = async () => {
  try {
    const [productRes, inventoryRes, inboundRes, outboundRes] = await Promise.all([
      statisticsApi.getProductStats(),
      statisticsApi.getInventoryStats(),
      statisticsApi.getInboundStats(),
      statisticsApi.getOutboundStats()
    ])

    if (productRes.code === 200) stats.productStats = productRes.data
    if (inventoryRes.code === 200) stats.inventoryStats = inventoryRes.data
    if (inboundRes.code === 200) stats.inboundStats = inboundRes.data
    if (outboundRes.code === 200) stats.outboundStats = outboundRes.data

    initCharts()
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const initCharts = () => {
  initCategoryChart()
  initStockChart()
}

const initCategoryChart = () => {
  if (!categoryChartRef.value) return

  categoryChart = echarts.init(categoryChartRef.value)

  const categoryData = stats.productStats.categoryDistribution || {}
  const data = Object.entries(categoryData).map(([name, value]) => ({ name, value }))

  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '商品分类',
        type: 'pie',
        radius: '50%',
        data: data.length > 0 ? data : [{ name: '暂无数据', value: 1 }],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  categoryChart.setOption(option)
}

const initStockChart = () => {
  if (!stockChartRef.value) return

  stockChart = echarts.init(stockChartRef.value)

  const lowStockProducts = stats.inventoryStats.lowStockProducts || []
  const names = lowStockProducts.map(item => item.name)
  const stocks = lowStockProducts.map(item => item.stock)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: names.length > 0 ? names : ['暂无预警'],
      axisLabel: {
        rotate: names.length > 0 ? 45 : 0,
        color: names.length === 0 ? '#909399' : '#303133'
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: names.length > 0 ? undefined : 10,
      axisLabel: {
        color: names.length === 0 ? '#C0C4CC' : '#606266'
      }
    },
    series: [
      {
        name: '库存数量',
        type: 'bar',
        data: stocks.length > 0 ? stocks : [0],
        itemStyle: {
          color: stocks.length > 0 ? '#E6A23C' : '#DCDFE6'
        },
        barWidth: stocks.length > 0 ? '60%' : '30%'
      }
    ],
    graphic: stocks.length === 0 ? [
      {
        type: 'text',
        left: 'center',
        top: 'middle',
        style: {
          text: '当前无库存预警商品\n所有商品库存充足',
          fill: '#909399',
          fontSize: 14,
          textAlign: 'center'
        }
      }
    ] : []
  }

  stockChart.setOption(option)
}

const handleResize = () => {
  categoryChart?.resize()
  stockChart?.resize()
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  categoryChart?.dispose()
  stockChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  min-width: 320px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  min-width: 150px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 350px;
  min-width: 300px;
}

.chart-container {
  width: 100%;
  height: 280px;
}

.stats-summary {
  padding: 20px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-item .label {
  color: #606266;
  font-size: 14px;
}

.summary-item .value {
  color: #303133;
  font-size: 18px;
  font-weight: bold;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 10px;
  }
  
  .stat-card {
    height: 100px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .chart-card {
    height: 300px;
  }
  
  .chart-container {
    height: 240px;
  }
}
</style>
