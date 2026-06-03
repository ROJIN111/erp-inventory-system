import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/Forbidden.vue'),
    meta: { title: '无权限' }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', requiresAuth: true, permission: 'dashboard:view' }
      },
      {
        path: 'product',
        redirect: '/product/list'
      },
      {
        path: 'product/list',
        name: 'ProductList',
        component: () => import('@/views/product/List.vue'),
        meta: { title: '商品列表', requiresAuth: true, permission: 'product:view' }
      },
      {
        path: 'product/add',
        name: 'ProductAdd',
        component: () => import('@/views/product/Form.vue'),
        meta: { title: '新增商品', requiresAuth: true, permission: 'product:create' }
      },
      {
        path: 'product/edit/:id',
        name: 'ProductEdit',
        component: () => import('@/views/product/Form.vue'),
        meta: { title: '编辑商品', requiresAuth: true, permission: 'product:update' }
      },
      {
        path: 'product/view/:id',
        name: 'ProductView',
        component: () => import('@/views/product/Form.vue'),
        meta: { title: '查看商品', requiresAuth: true, permission: 'product:view' }
      },
      {
        path: 'inbound',
        redirect: '/inbound/list'
      },
      {
        path: 'inbound/list',
        name: 'InboundList',
        component: () => import('@/views/inbound/List.vue'),
        meta: { title: '入库单列表', requiresAuth: true, permission: 'inbound:view' }
      },
      {
        path: 'inbound/add',
        name: 'InboundAdd',
        component: () => import('@/views/inbound/Form.vue'),
        meta: { title: '新增入库单', requiresAuth: true, permission: 'inbound:create' }
      },
      {
        path: 'outbound',
        redirect: '/outbound/list'
      },
      {
        path: 'outbound/list',
        name: 'OutboundList',
        component: () => import('@/views/outbound/List.vue'),
        meta: { title: '出库单列表', requiresAuth: true, permission: 'outbound:view' }
      },
      {
        path: 'outbound/add',
        name: 'OutboundAdd',
        component: () => import('@/views/outbound/Form.vue'),
        meta: { title: '新增出库单', requiresAuth: true, permission: 'outbound:create' }
      },
      {
        path: 'inventory-check',
        redirect: '/inventory-check/list'
      },
      {
        path: 'inventory-check/list',
        name: 'InventoryCheckList',
        component: () => import('@/views/inventory-check/List.vue'),
        meta: { title: '盘点单列表', requiresAuth: true, permission: 'inventory_check:view' }
      },
      {
        path: 'inventory-check/add',
        name: 'InventoryCheckAdd',
        component: () => import('@/views/inventory-check/Form.vue'),
        meta: { title: '新增盘点单', requiresAuth: true, permission: 'inventory_check:create' }
      },
      {
        path: 'inventory-check/edit/:id',
        name: 'InventoryCheckEdit',
        component: () => import('@/views/inventory-check/Form.vue'),
        meta: { title: '编辑盘点单', requiresAuth: true, permission: 'inventory_check:update' }
      },
      {
        path: 'inventory-check/view/:id',
        name: 'InventoryCheckView',
        component: () => import('@/views/inventory-check/Form.vue'),
        meta: { title: '查看盘点单', requiresAuth: true, permission: 'inventory_check:view' }
      },
      {
        path: 'inventory-transaction',
        name: 'InventoryTransaction',
        component: () => import('@/views/inventory-transaction/List.vue'),
        meta: { title: '库存流水账', requiresAuth: true, permission: 'inventory_flow:view' }
      },
      {
        path: 'warning',
        redirect: '/warning/config'
      },
      {
        path: 'warning/config',
        name: 'WarningConfig',
        component: () => import('@/views/warning/Config.vue'),
        meta: { title: '预警配置', requiresAuth: true, permission: 'warning:config' }
      },
      {
        path: 'warning/list',
        name: 'WarningList',
        component: () => import('@/views/warning/List.vue'),
        meta: { title: '预警消息', requiresAuth: true, permission: 'warning:view' }
      },
      {
        path: 'user',
        redirect: '/user/list'
      },
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('@/views/user/List.vue'),
        meta: { title: '用户列表', requiresAuth: true, permission: 'user:view' }
      },
      {
        path: 'log',
        name: 'LogList',
        component: () => import('@/views/log/List.vue'),
        meta: { title: '操作日志', requiresAuth: true, permission: 'log:view' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  const title = typeof to.meta.title === 'string' ? to.meta.title : 'ERP 系统'
  document.title = title ? `${title} - ERP 系统` : 'ERP 系统'

  await authStore.ensureSessionInitialized()

  if (to.path === '/login' && authStore.isLoggedIn) {
    next('/dashboard')
    return
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
    return
  }

  const permission = typeof to.meta.permission === 'string' ? to.meta.permission : ''
  if (permission && !authStore.hasPermission(permission)) {
    next('/403')
    return
  }

  next()
})

export default router
