import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 设置默认值
  const loginUser = ref<any>({
    userName: '未登录',
  })

    // 控制登录弹窗显示
    const loginModalVisible = ref(false)
    // 登录成功后需要跳转的路由
    const redirectPath = ref<string | null>(null)

    // 更改状态
  async function fetchLoginUser() {
    // 测试用户登录，3 秒后登录
    // setTimeout(() => {
    //   loginUser.value = { userName: '测试用户', id: 1 }
    // }, 3000)
    const res = await getLoginUser();
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data;
    }
  }

  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

    // 打开登录弹窗
    function openLoginModal(path?: string) {
        if (path) {
            redirectPath.value = path
        }
        loginModalVisible.value = true
    }

    // 关闭登录弹窗
    function closeLoginModal() {
        loginModalVisible.value = false
        redirectPath.value = null
    }

    // 返回
    return {
        loginUser,
        setLoginUser,
        fetchLoginUser,
        loginModalVisible,
        redirectPath,
        openLoginModal,
        closeLoginModal
    }
})
