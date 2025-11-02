import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 设置默认值
  const loginUser = ref<any>({
    userName: '未登录',
  })

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

  // 返回
  return { loginUser, setLoginUser, fetchLoginUser }
})
