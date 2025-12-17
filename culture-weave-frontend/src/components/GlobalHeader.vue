<template>
  <div class="globalHeader">
    <div class="header-bar container">
      <RouterLink to="/" class="brand">
        <img class="logo" src="../assets/logo.png" alt="logo" />
        <div class="title">文韵智创</div>
      </RouterLink>

      <div class="nav-wrapper">
        <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" @click="doMenuClick" />
      </div>

      <div class="user-login-status">
        <div v-if="loginUserStore.loginUser.id" class="login-user">
          <a-dropdown trigger="click">
            <ASpace size="small" class="user-toggle">
              <UserOutlined />
              <span class="user-name">{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
            </ASpace>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="doLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>

        <div v-else>
          <a-button class="login-btn" type="primary" href="/user/login">登录</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, h, ref, watchEffect } from 'vue'
import {
  HomeOutlined,
  LogoutOutlined,
  UserOutlined,
  FolderOutlined,
  EditOutlined,
  SettingOutlined
} from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useRouter, useRoute } from 'vue-router'
import { healthCheck } from '../api/healthController'
import { useLoginUserStore } from '../stores/useLoginUserStore'
import { userLogout } from '../api/userController'

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()

const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/resources',
    icon: () => h(FolderOutlined),
    label: '资源库',
    title: '资源库',
  },
  {
    key: '/creativeCenter',
    icon: () => h(EditOutlined),
    label: '社区',
    title: '社区',
  },

  {
    key: '/admin/userManage',
    icon: () => h(SettingOutlined),
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/resourcesManage',
    icon: () => h(SettingOutlined),
    label: '资源库管理',
    title: '资源库管理',
  },
]

const filterMenus = (menus: MenuProps['items'] = []) => {
  return (menus ?? []).filter((menu): menu is NonNullable<typeof menu> => {
    if (!menu) return false
    const key = menu.key
    if (typeof key === 'string' && key.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

const items = computed<MenuProps['items']>(() => filterMenus(originItems))

const router = useRouter()
const route = useRoute()

const current = ref<string[]>([route.path])
watchEffect(() => {
  current.value = [route.path]
})

const doMenuClick = ({ key }: { key: string }) => {
  if (key && key !== route.path) {
    router.push({ path: key })
  }
}

healthCheck()

const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
/* 主题变量 */
:root {
  --header-bg: rgba(255, 255, 255, 0.9);
  --header-border: rgba(228, 233, 242, 0.9);
  --card-shadow: 0 4px 20px rgba(15, 35, 95, 0.08);
  --brand-color: #101828;
  --muted: #667085;
  --primary: #1677ff;
  --primary-strong: #145bcb;
  --menu-active: #0b59ff;
}

@media (prefers-color-scheme: dark) {
  :root {
    --header-bg: rgba(23, 23, 23, 0.7);
    --header-border: rgba(255, 255, 255, 0.08);
    --card-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
    --brand-color: #f2f4f7;
    --muted: #98a2b3;
    --primary: #3b82f6;
    --primary-strong: #2563eb;
    --menu-active: #60a5fa;
  }
}

.globalHeader {
  width: 100%;
  background: transparent;
}

/* 与 BasicLayout 宽度对齐 */
.container {
  max-width: 1800px;
  margin: 0 auto;
  padding: 0 8%;
}

.header-bar {
  display: flex;
  align-items: center;
  /* 垂直居中关键 */
  gap: 32px;
  min-height: 72px;
  width: 100%;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  text-decoration: none;
  color: var(--brand-color);
  flex: 0 0 auto;
  transition: transform 0.2s ease, opacity 0.2s ease;
  padding: 4px 0;
}

.brand:hover {
  transform: translateY(-1px);
  opacity: 0.92;
}

.logo {
  height: 44px;
  width: auto;
  display: block;
  transition: transform 0.3s ease;
}

.brand:hover .logo {
  transform: scale(1.05);
}

/* 移除基线空隙 */
.title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.8px;
  background: linear-gradient(135deg, #3d6bff, #845bff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transition: all 0.3s ease;
}

.brand:hover .title {
  background: linear-gradient(135deg, #2d5ae6, #7445e6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-wrapper {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
}

.nav-wrapper :deep(.ant-menu) {
  justify-content: center;
  background: transparent;
  border-bottom: none;
  white-space: nowrap;
}

.nav-wrapper :deep(.ant-menu-horizontal) {
  border: none;
}

.nav-wrapper :deep(.ant-menu-horizontal > .ant-menu-item),
.nav-wrapper :deep(.ant-menu-horizontal > .ant-menu-submenu) {
  padding-inline: 18px;
  height: 56px;
  display: flex;
  /* 保证图标与文字垂直居中 */
  align-items: center;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.nav-wrapper :deep(.ant-menu-horizontal > .ant-menu-item:hover),
.nav-wrapper :deep(.ant-menu-horizontal > .ant-menu-submenu:hover) {
  color: var(--menu-active);
  background: rgba(61, 107, 255, 0.06);
  border-radius: 8px;
}

.nav-wrapper :deep(.ant-menu-horizontal > .ant-menu-item::after),
.nav-wrapper :deep(.ant-menu-horizontal > .ant-menu-submenu::after) {
  bottom: 0;
  border-bottom: 2px solid transparent;
  transition: all .2s ease;
}

.nav-wrapper :deep(.ant-menu-item-selected) {
  font-weight: 600;
  color: var(--menu-active) !important;
}

.nav-wrapper :deep(.ant-menu-item-selected::after) {
  border-bottom-color: var(--menu-active) !important;
}

.user-login-status {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 140px;
}

.user-toggle {
  display: inline-flex;
  /* 避免基线不齐 */
  align-items: center;
  gap: 8px;
  line-height: 1;
  cursor: pointer;
  font-weight: 500;
  padding: 8px 14px;
  border-radius: 12px;
  border: 1px solid var(--header-border);
  background: rgba(255, 255, 255, 0.6);
  transition: all .2s ease;
  user-select: none;
}

.user-toggle:hover {
  background: rgba(61, 107, 255, 0.08);
  box-shadow: 0 4px 12px rgba(61, 107, 255, 0.15);
  border-color: rgba(61, 107, 255, 0.3);
  transform: translateY(-1px);
}

.user-name {
  max-width: 10em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* —— 登录按钮：文本100%垂直/水平居中 —— */
/* 直接作用于渲染出来的 button.login-btn 元素，避免 :deep 选择器歧义 */
.login-btn {
  display: inline-flex;
  /* 真正的居中 */
  align-items: center;
  justify-content: center;
  height: 40px;
  padding: 0 20px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 15px;
  line-height: 1;
  /* 防止行高把文本挤偏 */
  box-shadow: 0 4px 12px rgba(61, 107, 255, 0.3);
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(61, 107, 255, 0.4);
}

/* 按钮内部的 span 在 Ant 里会影响行高，这里统一为 flex 居中 */
.login-btn :deep(span) {
  display: inline-flex;
  align-items: center;
  line-height: 1;
}

/* 渐变主色（使用全局按钮主色变量） */
.login-btn:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, var(--btn-primary-start), var(--btn-primary-end));
  border-color: var(--btn-primary-border);
  box-shadow: var(--btn-primary-shadow);
}

/* —— 响应式 —— */
@media (max-width: 1200px) {
  .nav-wrapper :deep(.ant-menu) {
    justify-content: flex-start;
  }
}

@media (max-width: 1024px) {
  .header-bar {
    gap: 16px;
    min-height: 64px;
  }

  .logo {
    height: 40px;
  }

  .title {
    font-size: 20px;
  }
}

@media (max-width: 768px) {
  .header-bar {
    gap: 12px;
    min-height: 56px;
  }

  .nav-wrapper {
    flex: 1 1 auto;
    order: 3;
    overflow-x: auto;
    padding-bottom: 6px;
    -webkit-overflow-scrolling: touch;
    scroll-snap-type: x mandatory;
  }

  .nav-wrapper::-webkit-scrollbar {
    display: none;
  }

  .nav-wrapper {
    -ms-overflow-style: none;
    scrollbar-width: none;
  }

  .nav-wrapper :deep(.ant-menu-horizontal > .ant-menu-item),
  .nav-wrapper :deep(.ant-menu-horizontal > .ant-menu-submenu) {
    height: 44px;
  }

  .user-login-status {
    margin-left: auto;
  }
}
</style>
