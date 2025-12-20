import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { message } from 'ant-design-vue'
import router from '@/router'

// 是否为首次获取登录用户
let firstFetchLoginUser = true;

/**
 * 全局权限校验
 */
router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser
  // 确保页面刷新，首次加载时，能够等后端返回用户信息后再校验权限
  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false;
  }
  const toUrl = to.fullPath
    // 如果访问的是登录或注册页面，直接放行
    if (toUrl.startsWith('/user/login') || toUrl.startsWith('/user/register')) {
        next()
        return
    }
  if (toUrl.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole !== 'admin') {
        message.error('没有权限，请先登录')
        // 打开登录弹窗并保存目标路由
        loginUserStore.openLoginModal(to.fullPath)
        // 阻止导航，保持在当前页面
        next(false)
      return
    }
  }
  next()
})
