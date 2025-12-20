<template>
  <a-modal
      v-model:open="visible"
      :title="null"
      :footer="null"
      :width="600"
      :mask-closable="false"
      centered
      class="login-modal"
      @cancel="handleCancel"
  >
    <div class="modal-content">
      <div class="modal-header">
        <h2 class="modal-title">文韵智创</h2>
        <p class="modal-subtitle">CultureWeave</p>
      </div>

      <a-tabs v-model:activeKey="activeTab" class="auth-tabs" @change="handleTabChange">
        <a-tab-pane key="login" tab="登录">
          <a-form
              :model="loginForm"
              name="login"
              autocomplete="off"
              class="login-form"
              @finish="handleLogin"
          >
            <a-form-item
                name="userAccount"
                :rules="[{ required: true, message: '请输入账号' }]"
            >
              <a-input
                  v-model:value="loginForm.userAccount"
                  placeholder="请输入账号"
                  size="large"
              />
            </a-form-item>

            <a-form-item
                name="userPassword"
                :rules="[
                { required: true, message: '请输入密码' },
                { min: 8, message: '密码长度不能小于8位' },
                { max: 16, message: '密码长度不能大于16位' },
              ]"
            >
              <a-input-password
                  v-model:value="loginForm.userPassword"
                  placeholder="请输入密码"
                  size="large"
              />
            </a-form-item>

            <a-form-item>
              <div class="submit-btn-wrapper">
                <a-button
                    type="primary"
                    html-type="submit"
                    size="large"
                    :loading="loginLoading"
                    class="submit-btn"
                >
                  登录
                </a-button>
              </div>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="register" tab="注册">
          <a-form
              :model="registerForm"
              name="register"
              autocomplete="off"
              @finish="handleRegister"
          >
            <a-form-item
                name="userAccount"
                :rules="[{ required: true, message: '请输入账号' }]"
            >
              <a-input
                  v-model:value="registerForm.userAccount"
                  placeholder="请输入账号"
                  size="large"
              />
            </a-form-item>

            <a-form-item
                name="userPassword"
                :rules="[
                { required: true, message: '请输入密码' },
                { min: 8, message: '密码不能小于 8 位' },
              ]"
            >
              <a-input-password
                  v-model:value="registerForm.userPassword"
                  placeholder="请输入密码"
                  size="large"
              />
            </a-form-item>

            <a-form-item
                name="checkPassword"
                :rules="[
                { required: true, message: '请输入确认密码' },
                { min: 8, message: '确认密码不能小于 8 位' },
              ]"
            >
              <a-input-password
                  v-model:value="registerForm.checkPassword"
                  placeholder="请输入确认密码"
                  size="large"
              />
            </a-form-item>

            <a-form-item>
              <div class="submit-btn-wrapper">
                <a-button
                    type="primary"
                    html-type="submit"
                    size="large"
                    :loading="registerLoading"
                    class="submit-btn"
                >
                  注册
                </a-button>
              </div>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import {ref, reactive, watch} from 'vue'
import {message} from 'ant-design-vue'
import {useLoginUserStore} from '@/stores/useLoginUserStore'
import {userLogin, userRegister} from '@/api/userController'
import router from '@/router'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const visible = ref(false)
const activeTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginUserStore = useLoginUserStore()

const loginForm = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const registerForm = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

watch(
    () => props.open,
    (newVal) => {
      visible.value = newVal
      if (newVal) {
        // 重置表单
        activeTab.value = 'login'
        resetForms()
      }
    }
)

watch(visible, (newVal) => {
  emit('update:open', newVal)
})

const resetForms = () => {
  loginForm.userAccount = ''
  loginForm.userPassword = ''
  registerForm.userAccount = ''
  registerForm.userPassword = ''
  registerForm.checkPassword = ''
}

const handleTabChange = () => {
  // 切换标签时重置表单
  resetForms()
}

const handleCancel = () => {
  visible.value = false
  resetForms()
  loginUserStore.closeLoginModal()
}

const handleLogin = async () => {
  loginLoading.value = true
  try {
    const res = await userLogin(loginForm)
    if (res.data.code === 0 && res.data.data) {
      await loginUserStore.fetchLoginUser()
      message.success('登录成功')
      visible.value = false
      resetForms()

      // 如果有关联的路由，登录成功后跳转
      const redirectPath = loginUserStore.redirectPath
      if (redirectPath) {
        router.push(redirectPath)
      }
      loginUserStore.closeLoginModal()
    } else {
      message.error('登录失败：' + res.data.message)
    }
  } catch (error) {
    message.error('登录失败，请稍后重试')
  } finally {
    loginLoading.value = false
  }
}

const handleRegister = async () => {
  // 判断两次输入的密码是否一致
  if (registerForm.userPassword !== registerForm.checkPassword) {
    message.error('二次输入的密码不一致')
    return
  }

  registerLoading.value = true
  try {
    const res = await userRegister(registerForm)
    if (res.data.code === 0 && res.data.data) {
      message.success('注册成功，请登录')
      // 注册成功后切换到登录标签页
      activeTab.value = 'login'
      // 将注册的账号填入登录表单
      loginForm.userAccount = registerForm.userAccount
      loginForm.userPassword = ''
      registerForm.userAccount = ''
      registerForm.userPassword = ''
      registerForm.checkPassword = ''
    } else {
      message.error('注册失败：' + res.data.message)
    }
  } catch (error) {
    message.error('注册失败，请稍后重试')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-modal :deep(.ant-modal-content) {
  border-radius: 16px;
  overflow: hidden;
}

.login-modal :deep(.ant-modal-body) {
  padding: 0;
}

.modal-content {
  padding: 32px 40px 40px;
}

.modal-header {
  text-align: center;
  margin-bottom: 8px;
}

.modal-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 4px 0;
  background: linear-gradient(135deg, #3d6bff, #845bff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.modal-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
  letter-spacing: 1px;
}

.auth-tabs {
  text-align: center;
}

.auth-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}

.auth-tabs :deep(.ant-tabs-nav-wrap) {
  display: flex;
  justify-content: center;
}

.auth-tabs :deep(.ant-tabs-nav-list) {
  display: flex;
  justify-content: center;
  margin: 0 auto;
}

.auth-tabs :deep(.ant-tabs-tab) {
  font-size: 16px;
  font-weight: 500;
  padding: 12px 24px;
}

.auth-tabs :deep(.ant-tabs-tab-active) {
  font-weight: 600;
}

.auth-tabs :deep(.ant-tabs-ink-bar) {
  background: linear-gradient(135deg, #3d6bff, #845bff);
}

.auth-tabs :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.auth-tabs :deep(.login-form .ant-form-item:not(:has(.submit-btn-wrapper))) {
  margin-bottom: 50px;
}

.auth-tabs :deep(.ant-form-item:has(.submit-btn-wrapper)) {
  margin-bottom: 0;
  text-align: center;
}

.auth-tabs :deep(.ant-form-item:has(.submit-btn-wrapper) .ant-form-item-control) {
  max-width: 100%;
}

.auth-tabs :deep(.ant-form-item:has(.submit-btn-wrapper) .ant-form-item-control-input) {
  width: 100%;
}

.auth-tabs :deep(.ant-form-item:has(.submit-btn-wrapper) .ant-form-item-control-input-content) {
  display: flex;
  justify-content: center;
}

.auth-tabs :deep(.ant-input),
.auth-tabs :deep(.ant-input-password) {
  border-radius: 8px;
}

.submit-btn-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 8px;
  width: 100%;
}

.submit-btn {
  height: 44px;
  min-width: 200px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #3d6bff, #845bff);
  border: none;
  box-shadow: 0 4px 12px rgba(61, 107, 255, 0.3);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(61, 107, 255, 0.4);
}

.submit-btn:active {
  transform: translateY(0);
}
</style>

