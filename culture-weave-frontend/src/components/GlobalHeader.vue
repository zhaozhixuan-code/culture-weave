<template>
  <div class="globalHeader">

    <a-row :wrap="false">
      <!--左边图标-->
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">文韵智创</div>
          </div>
        </RouterLink>
      </a-col>
      <!-- 菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>

      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <ASpace>
<!--                <a-avatar :src="loginUserStore.loginUser.userAvatar" />-->
                {{ loginUserStore.loginUser.userName ?? '无名' }}
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
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>

      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { HomeOutlined, LogoutOutlined, UserOutlined ,EditOutlined} from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'

import { useRouter } from 'vue-router'
import { healthCheck } from '@/api/healthController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { userLogout } from '@/api/userController'

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()

// 菜单列表
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },

]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    if (menu.key.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== "admin") {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const items = computed<MenuProps['items']>(() => filterMenus(originItems))


const current = ref<string[]>(['home'])
const router = useRouter()

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  })
}

// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  current.value = [to.path];
});

healthCheck().then((res) => {
  console.log(res);
});

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  console.log(res)
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}

</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
  margin-left: 16px;
}

.title-bar .title {
  color: #000000;
  font-size: 18px;
  margin-left: 16px;
}

.title-bar .logo {
  height: 48px;
}
</style>
