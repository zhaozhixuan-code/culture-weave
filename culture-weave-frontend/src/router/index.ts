import { createRouter, createWebHistory } from 'vue-router'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: () => import('../pages/HomePage.vue')
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: () => import('../pages/user/UserLoginPage.vue')
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: () => import('../pages/user/UserRegisterPage.vue')
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
        component: () => import('../pages/admin/UserManagePage.vue')
    },
    {
      path: '/admin/resourcesManage',
      name: '资源库管理',
      component: () => import('../pages/admin/ResourcesManagePage.vue')
    },
    {
      path: '/resources',
      name: '资源库',
      component: () => import('../pages/ResourcesPage.vue')
    },
    {
      path: '/addResources',
      name: '添加资源',
      component: () => import('../pages/AddResourcesPage.vue')
    },
    
    {
      path: '/resources/:id',
      name: '资源详情',
      component: () => import('../pages/ResourceDetailPage.vue')
    },
    {
      path: '/creativeCenter',
      name: '创作中心',
      component: () => import('../pages/CreativeCenterPage.vue')
    },
  ]
})

export default router
