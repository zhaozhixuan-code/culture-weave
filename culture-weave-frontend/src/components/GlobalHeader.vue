<template>
  <div class="globalHeader">
    <div class="header-bar container">
      <RouterLink to="/" class="brand">
        <img class="logo" src="../assets/logo.png" alt="logo" />
        <div class="title">文韵智创</div>
      </RouterLink>

      <div class="nav-wrapper">
        <a-menu 
          v-model:selectedKeys="current" 
          mode="horizontal" 
          :items="items" 
          @click="doMenuClick"
          :triggerSubMenuAction="'hover'"
        />
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
                <a-menu-item @click="goToProfile">
                  <UserOutlined />
                  个人中心
                </a-menu-item>
                <a-menu-item @click="doLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>

        <div v-else>
          <a-button class="login-btn" type="primary" @click="openLoginModal">登录</a-button>
        </div>
      </div>
    </div>

    <LoginModal v-model:open="loginModalVisible"/>
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
  SettingOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useRouter, useRoute } from 'vue-router'
import { healthCheck } from '../api/healthController'
import { useLoginUserStore } from '../stores/useLoginUserStore'
import { userLogout } from '../api/userController'
import LoginModal from './LoginModal.vue'

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()

// 使用 store 中的登录弹窗状态
const loginModalVisible = computed({
  get: () => loginUserStore.loginModalVisible,
  set: (value) => {
    if (!value) {
      loginUserStore.closeLoginModal()
    } else {
      // 如果外部直接设置 visible 为 true，也打开弹窗（但不设置 redirectPath）
      loginUserStore.loginModalVisible = true
    }
  }
})

const openLoginModal = () => {
  loginUserStore.openLoginModal()
}

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
    key: '/about',
    icon: () => h(InfoCircleOutlined),
    label: '关于我们',
    title: '关于我们',
  },
  {
    key: 'admin',
    icon: () => h(SettingOutlined),
    label: '管理',
    title: '管理',
    children: [
      {
        key: '/admin/userManage',
        icon: () => h(UserOutlined),
        label: '用户管理',
        title: '用户管理',
      },
      {
        key: '/admin/resourcesManage',
        icon: () => h(FolderOutlined),
        label: '资源库管理',
        title: '资源库管理',
      },
      {
        key: '/admin/postsManage',
        icon: () => h(EditOutlined),
        label: '社区管理',
        title: '社区管理',
      },
    ],
  },
]

const filterMenus = (menus: MenuProps['items'] = []) => {
  const loginUser = loginUserStore.loginUser
  const isAdmin = loginUser && loginUser.userRole === 'admin'
  
  return (menus ?? []).filter((menu): menu is NonNullable<typeof menu> => {
    if (!menu) return false
    const key = menu.key
    
    // 如果是管理菜单，需要检查是否是管理员
    if (key === 'admin') {
      if (!isAdmin) {
        return false
      }
      // 如果是submenu，也需要过滤其children
      if ('children' in menu && menu.children) {
        menu.children = menu.children.filter((child: any) => {
          return child !== null && child !== undefined
        })
      }
      return true
    }
    
    // 其他菜单项正常显示
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

const goToProfile = () => {
  router.push('/user/profile')
}

const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' })
    message.success('退出登录成功')
    await router.push('/')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
/* 主题变量 */
:root {
  --header-bg: rgba(255, 255, 255, 0.9);
  --header-border: rgba(212, 167, 106, 0.3);
  --card-shadow: 0 4px 20px rgba(212, 167, 106, 0.15);
  --brand-color: #2c1810;
  --muted: #5d4037;
  --primary: #d4a76a;
  --primary-strong: #8b4513;
  --menu-active: #8b4513;
}

@media (prefers-color-scheme: dark) {
  :root {
    --header-bg: rgba(23, 23, 23, 0.7);
    --header-border: rgba(212, 167, 106, 0.3);
    --card-shadow: 0 8px 24px rgba(212, 167, 106, 0.2);
    --brand-color: #f2f4f7;
    --muted: #d4a76a;
    --primary: #d4a76a;
    --primary-strong: #8b4513;
    --menu-active: #d4a76a;
  }

  .nav-wrapper :deep(.ant-menu-item-selected) {
    background: linear-gradient(135deg, rgba(212, 167, 106, 0.2), rgba(139, 69, 19, 0.15)) !important;
  }

  .nav-wrapper :deep(.ant-menu-item-selected:hover) {
    background: linear-gradient(135deg, rgba(212, 167, 106, 0.25), rgba(139, 69, 19, 0.2)) !important;
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
  background: linear-gradient(90deg, #b8860b 0%, #d4a76a 25%, #a0522d 50%, #8b4513 75%, #654321 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transition: all 0.3s ease;
}

.brand:hover .title {
  background: linear-gradient(90deg, #a0522d 0%, #8b4513 25%, #654321 50%, #8b4513 75%, #a0522d 100%);
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
  background: rgba(212, 167, 106, 0.1);
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
  background: linear-gradient(135deg, rgba(212, 167, 106, 0.15), rgba(139, 69, 19, 0.1)) !important;
  border-radius: 8px;
  position: relative;
}

.nav-wrapper :deep(.ant-menu-item-selected::after) {
  border-bottom-color: var(--menu-active) !important;
  border-bottom-width: 2px;
}

.nav-wrapper :deep(.ant-menu-item-selected .anticon) {
  color: var(--menu-active) !important;
}

/* 增强选中状态的视觉效果 */
.nav-wrapper :deep(.ant-menu-item-selected:hover) {
  background: linear-gradient(135deg, rgba(212, 167, 106, 0.2), rgba(139, 69, 19, 0.15)) !important;
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
  background: rgba(212, 167, 106, 0.1);
  box-shadow: 0 4px 12px rgba(212, 167, 106, 0.15);
  border-color: rgba(212, 167, 106, 0.4);
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
  box-shadow: 0 4px 12px rgba(212, 167, 106, 0.3);
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(212, 167, 106, 0.4);
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
