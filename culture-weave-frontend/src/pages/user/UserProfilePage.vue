<template>
  <div class="user-profile-page">
    <div class="profile-container">
      <!-- 个人信息卡片 -->
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <img v-if="avatarPreview || userInfo.userAvatar" :src="avatarPreview || userInfo.userAvatar" alt="avatar" class="avatar-img" />
              <div v-else class="avatar-placeholder">
                {{ abbreviation(userInfo.userName) }}
              </div>
              <div v-if="isEditing" class="avatar-upload-overlay">
                <a-upload
                  v-model:file-list="avatarFileList"
                  :before-upload="beforeAvatarUpload"
                  :show-upload-list="false"
                  accept="image/*"
                >
                  <a-button type="primary" shape="circle" size="small" class="avatar-upload-btn">
                    <EditOutlined />
                  </a-button>
                </a-upload>
              </div>
            </div>
            <div v-if="isEditing && avatarFile" class="avatar-upload-hint">
              <a-button type="link" size="small" class="remove-avatar-btn" @click="removeAvatar">移除头像</a-button>
            </div>
          </div>
          <div class="profile-info">
            <h2 class="user-name">{{ userInfo.userName || '未设置用户名' }}</h2>
            <p class="user-account">{{ userInfo.userAccount || '' }}</p>
            <p class="user-role">
              <a-tag :color="userInfo.userRole === 'admin' ? 'red' : 'blue'">
                {{ userInfo.userRole === 'admin' ? '管理员' : '普通用户' }}
              </a-tag>
            </p>
            <div class="profile-actions">
              <a-button v-if="!isEditing" type="primary" class="edit-btn" @click="startEdit">
                <EditOutlined />
                编辑资料
              </a-button>
              <template v-else>
                <a-button type="primary" class="save-btn" @click="saveProfile" :loading="saving">
                  <CheckOutlined />
                  保存
                </a-button>
                <a-button class="cancel-btn" @click="cancelEdit">
                  取消
                </a-button>
              </template>
            </div>
          </div>
        </div>

        <a-divider />

        <div class="profile-form" v-if="isEditing">
          <a-form :model="editForm" layout="vertical">
            <a-form-item label="用户名">
              <a-input v-model:value="editForm.userName" placeholder="请输入用户名" />
            </a-form-item>
            <a-form-item label="个人简介">
              <a-textarea
                v-model:value="editForm.userProfile"
                placeholder="请输入个人简介"
                :rows="4"
                :maxlength="200"
                show-count
              />
            </a-form-item>
          </a-form>
        </div>
        <div v-else class="profile-content">
          <div class="info-item">
            <span class="label">用户名：</span>
            <span class="value">{{ userInfo.userName || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">账号：</span>
            <span class="value">{{ userInfo.userAccount || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">个人简介：</span>
            <span class="value">{{ userInfo.userProfile || '暂无简介' }}</span>
          </div>
          <div class="info-item">
            <span class="label">注册时间：</span>
            <span class="value">{{ formatTime(userInfo.createTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 标签页：我的资源和我的帖子 -->
      <div class="content-tabs">
        <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
          <a-tab-pane key="resources" tab="我的资源">
            <div class="tab-content">
              <a-spin :spinning="resourcesLoading">
                <div v-if="!resourcesLoading && myResources.length === 0" class="empty-state">
                  <FolderOutlined class="empty-icon" />
                  <p class="empty-text">还没有发布任何资源</p>
                  <a-button type="primary" @click="goToAddResource">去发布资源</a-button>
                </div>
                <div v-else class="resources-grid">
                  <div
                    v-for="resource in myResources"
                    :key="resource.id"
                    class="resource-card"
                    @click="goToResourceDetail(resource.id)"
                  >
                    <div class="resource-cover">
                      <img
                        v-if="resource.resourceImgUrl"
                        :src="resource.resourceImgUrl"
                        alt="resource cover"
                        loading="lazy"
                      />
                      <div v-else class="resource-placeholder">
                        <FolderOutlined />
                      </div>
                    </div>
                    <div class="resource-info">
                      <h3 class="resource-title">{{ resource.name || '未命名资源' }}</h3>
                      <p class="resource-intro">{{ excerpt(resource.introduction) }}</p>
                      <div class="resource-meta">
                        <a-tag v-if="resource.category" color="blue">{{ resource.category }}</a-tag>
                        <a-tag v-if="resource.region" color="green">{{ resource.region }}</a-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </a-spin>
              <div v-if="resourcesTotal > pagination.pageSize" class="pagination-wrapper">
                <a-pagination
                  v-model:current="resourcesPagination.current"
                  v-model:page-size="resourcesPagination.pageSize"
                  :total="resourcesTotal"
                  :show-size-changer="true"
                  :page-size-options="['12', '24', '36']"
                  @change="onResourcesPageChange"
                  @show-size-change="onResourcesPageSizeChange"
                  show-total
                />
              </div>
            </div>
          </a-tab-pane>

          <a-tab-pane key="posts" tab="我的帖子">
            <div class="tab-content">
              <a-spin :spinning="postsLoading">
                <div v-if="!postsLoading && myPosts.length === 0" class="empty-state">
                  <EditOutlined class="empty-icon" />
                  <p class="empty-text">还没有发布任何帖子</p>
                  <a-button type="primary" @click="goToCreatePost">去发布帖子</a-button>
                </div>
                <div v-else class="posts-grid">
                  <div
                    v-for="post in myPosts"
                    :key="post.id"
                    class="post-card"
                    @click="handlePostClick(post)"
                  >
                    <div class="post-cover">
                      <img
                        v-if="post.pictureUrl"
                        :src="post.pictureUrl"
                        alt="post cover"
                        loading="lazy"
                      />
                      <div v-else class="post-placeholder">
                        <HighlightOutlined />
                      </div>
                    </div>
                    <div class="post-info">
                      <h3 class="post-title">{{ post.title || '未命名帖子' }}</h3>
                      <p class="post-content">{{ excerpt(post.content) }}</p>
                      <div class="post-meta">
                        <span class="post-time">{{ formatTime(post.createTime) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </a-spin>
              <div v-if="postsTotal > pagination.pageSize" class="pagination-wrapper">
                <a-pagination
                  v-model:current="postsPagination.current"
                  v-model:page-size="postsPagination.pageSize"
                  :total="postsTotal"
                  :show-size-changer="true"
                  :page-size-options="['12', '24', '36']"
                  @change="onPostsPageChange"
                  @show-size-change="onPostsPageSizeChange"
                  show-total
                />
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/// <reference path="../../api/typings.d.ts" />
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { UploadFile, UploadProps } from 'ant-design-vue'
import {
  EditOutlined,
  CheckOutlined,
  FolderOutlined,
  HighlightOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import { getUserVoById, editUser, updateUser } from '../../api/userController'
import { listResourcesVoByPage } from '../../api/resourcesController'
import { listResourcesVoByPage1 } from '../../api/postController'
import { useLoginUserStore } from '../../stores/useLoginUserStore'
import PostDetailModal from '../../components/PostDetailModal.vue'

type UserVO = API.UserVO
type ResourcesVO = API.ResourcesVO
type PostVO = API.PostVO

const router = useRouter()
const loginUserStore = useLoginUserStore()

const userInfo = ref<UserVO>({
  userName: '',
  userAccount: '',
  userAvatar: '',
  userProfile: '',
  userRole: ''
})

const isEditing = ref(false)
const saving = ref(false)
const activeTab = ref('resources')

const editForm = reactive({
  userName: '',
  userProfile: ''
})

// 头像上传相关
const avatarFile = ref<File | null>(null)
const avatarPreview = ref<string | null>(null)
const avatarFileList = ref<UploadFile[]>([])

const resourcesLoading = ref(false)
const myResources = ref<ResourcesVO[]>([])
const resourcesTotal = ref(0)
const resourcesPagination = reactive({
  current: 1,
  pageSize: 12
})

const postsLoading = ref(false)
const myPosts = ref<PostVO[]>([])
const postsTotal = ref(0)
const postsPagination = reactive({
  current: 1,
  pageSize: 12
})

const pagination = computed(() => {
  return activeTab.value === 'resources' ? resourcesPagination : postsPagination
})

const detailModalOpen = ref(false)
const selectedPostId = ref<number | null>(null)

const currentUserId = computed(() => loginUserStore.loginUser?.id || null)

function abbreviation(name?: string | null) {
  if (!name) return '匿'
  return name.slice(0, 1).toUpperCase()
}

function formatTime(value?: string) {
  if (!value) return '未知'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN')
}

function excerpt(content?: string, maxLength: number = 100) {
  if (!content) return '暂无内容'
  return content.length > maxLength ? `${content.slice(0, maxLength)}...` : content
}

async function fetchUserInfo() {
  if (!currentUserId.value) {
    message.warning('请先登录')
    router.push('/')
    return
  }

  try {
    const res = await getUserVoById({ id: currentUserId.value })
    if (res.data.code === 0 && res.data.data) {
      userInfo.value = res.data.data
      editForm.userName = userInfo.value.userName || ''
      editForm.userProfile = userInfo.value.userProfile || ''
    } else {
      message.error('获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
    message.error('获取用户信息失败')
  }
}

function startEdit() {
  isEditing.value = true
  editForm.userName = userInfo.value.userName || ''
  editForm.userProfile = userInfo.value.userProfile || ''
  // 重置头像上传状态
  avatarFile.value = null
  avatarPreview.value = null
  avatarFileList.value = []
}

function cancelEdit() {
  isEditing.value = false
  editForm.userName = userInfo.value.userName || ''
  editForm.userProfile = userInfo.value.userProfile || ''
  // 重置头像上传状态
  avatarFile.value = null
  avatarPreview.value = null
  avatarFileList.value = []
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('头像图片大小不能超过 5MB！')
    return false
  }

  avatarFile.value = file
  const uploadFile = file as UploadFile
  avatarFileList.value = [uploadFile]

  // 创建预览
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target?.result as string
  }
  reader.readAsDataURL(file)

  return false
}

function removeAvatar() {
  avatarFile.value = null
  avatarPreview.value = null
  avatarFileList.value = []
}

async function saveProfile() {
  if (!currentUserId.value) return

  saving.value = true
  try {
    const userEditRequest: API.UserEditRequest = {
      id: currentUserId.value,
      userName: editForm.userName,
      userProfile: editForm.userProfile
    }

    // 如果有新上传的头像文件，则上传文件；否则只更新其他信息
    const res = await editUser(
      { userEditRequest },
      avatarFile.value || undefined
    )
    const responseData = (res as any)?.data as API.BaseResponseBoolean

    if (responseData?.code === 0) {
      message.success('保存成功')
      await fetchUserInfo()
      await loginUserStore.fetchLoginUser()
      isEditing.value = false
      // 清空上传状态
      avatarFile.value = null
      avatarPreview.value = null
      avatarFileList.value = []
    } else {
      message.error('保存失败：' + (responseData?.message || '未知错误'))
    }
  } catch (error) {
    console.error('保存失败', error)
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}


async function fetchMyResources() {
  if (!currentUserId.value || !userInfo.value.userName) return

  resourcesLoading.value = true
  try {
    const res = await listResourcesVoByPage({
      current: resourcesPagination.current,
      pageSize: resourcesPagination.pageSize,
      sortField: 'createTime',
      sortOrder: 'descend',
      userName: userInfo.value.userName
    })

    if (res.data.code === 0 && res.data.data) {
      myResources.value = res.data.data.records || []
      resourcesTotal.value = res.data.data.total || 0
    } else {
      myResources.value = []
      resourcesTotal.value = 0
    }
  } catch (error) {
    console.error('获取我的资源失败', error)
    message.error('获取我的资源失败')
    myResources.value = []
    resourcesTotal.value = 0
  } finally {
    resourcesLoading.value = false
  }
}

async function fetchMyPosts() {
  if (!currentUserId.value) return

  postsLoading.value = true
  try {
    // 由于API不支持按userId查询，需要获取更多数据然后过滤
    // 使用较大的pageSize来获取数据，然后在前端过滤和分页
    const res = await listResourcesVoByPage1({
      current: 1,
      pageSize: 1000, // 获取足够多的数据
      sortField: 'createTime',
      sortOrder: 'descend'
    })

    if (res.data.code === 0 && res.data.data) {
      // 过滤出当前用户的帖子
      const allPosts = res.data.data.records || []
      const filteredPosts = allPosts.filter(post => post.userId === currentUserId.value)
      
      // 前端分页
      postsTotal.value = filteredPosts.length
      const start = (postsPagination.current - 1) * postsPagination.pageSize
      const end = start + postsPagination.pageSize
      myPosts.value = filteredPosts.slice(start, end)
    } else {
      myPosts.value = []
      postsTotal.value = 0
    }
  } catch (error) {
    console.error('获取我的帖子失败', error)
    message.error('获取我的帖子失败')
    myPosts.value = []
    postsTotal.value = 0
  } finally {
    postsLoading.value = false
  }
}

function handleTabChange(key: string) {
  activeTab.value = key
  if (key === 'resources' && userInfo.value.userName) {
    fetchMyResources()
  } else if (key === 'posts') {
    fetchMyPosts()
  }
}

function onResourcesPageChange(page: number) {
  resourcesPagination.current = page
  fetchMyResources()
}

function onResourcesPageSizeChange(_current: number, size: number) {
  resourcesPagination.current = 1
  resourcesPagination.pageSize = size
  fetchMyResources()
}

function onPostsPageChange(page: number) {
  postsPagination.current = page
  fetchMyPosts()
}

function onPostsPageSizeChange(_current: number, size: number) {
  postsPagination.current = 1
  postsPagination.pageSize = size
  fetchMyPosts()
}

function goToAddResource() {
  router.push('/addResources')
}

function goToCreatePost() {
  router.push('/creativeCenter/create')
}

function goToResourceDetail(id?: number) {
  if (id) {
    router.push(`/resources/${id}`)
  }
}

function handlePostClick(post: PostVO) {
  if (!post.id) return
  selectedPostId.value = post.id
  detailModalOpen.value = true
}

function handleDetailModalClose() {
  selectedPostId.value = null
  detailModalOpen.value = false
}

onMounted(async () => {
  if (!currentUserId.value) {
    await loginUserStore.fetchLoginUser()
  }
  if (!currentUserId.value) {
    message.warning('请先登录')
    router.push('/')
    return
  }
  await fetchUserInfo()
  // 等待userInfo加载完成后再获取资源
  if (userInfo.value.userName) {
    await fetchMyResources()
  }
})
</script>

<style scoped>
.user-profile-page {
  min-height: calc(100vh - 200px);
  padding: 32px 8%;
  background: linear-gradient(180deg, #fcf9f4 0%, #fff 100%);
}

.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(212, 167, 106, 0.15);
  border: 1px solid rgba(212, 167, 106, 0.2);
}

.profile-header {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.avatar-upload-overlay {
  position: absolute;
  bottom: 0;
  right: 0;
  background: rgba(139, 69, 19, 0.8);
  border-radius: 50%;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.3);
}

.avatar-upload-overlay:hover {
  background: rgba(139, 69, 19, 0.95);
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(139, 69, 19, 0.4);
}

.avatar-upload-overlay :deep(.avatar-upload-btn) {
  border: none;
  box-shadow: none;
  background: transparent;
  color: #fff;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-upload-overlay :deep(.avatar-upload-btn:hover) {
  background: rgba(255, 255, 255, 0.1);
  transform: rotate(15deg);
}

.avatar-upload-hint {
  text-align: center;
  margin-top: 8px;
}

.avatar-upload-hint :deep(.remove-avatar-btn) {
  color: #d32f2f;
  font-size: 13px;
  padding: 0;
  height: auto;
  transition: all 0.3s ease;
}

.avatar-upload-hint :deep(.remove-avatar-btn:hover) {
  color: #b71c1c;
  text-decoration: underline;
}

.avatar-img {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(212, 167, 106, 0.3);
}

.avatar-placeholder {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #d4a76a, #8b4513);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 48px;
  font-weight: 600;
  border: 3px solid rgba(212, 167, 106, 0.3);
}

.profile-info {
  flex: 1;
}

.user-name {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #2c1810;
}

.user-account {
  font-size: 16px;
  color: #5d4037;
  margin: 0 0 12px 0;
}

.user-role {
  margin: 0 0 16px 0;
}

.profile-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 编辑按钮样式 */
.profile-actions :deep(.edit-btn),
.profile-actions :deep(.save-btn) {
  height: 36px;
  padding: 0 20px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
  font-family: 'Noto Sans SC', sans-serif;
  letter-spacing: 0.3px;
  background: linear-gradient(135deg, #a0522d, #8b4513);
  border: 2px solid #8b4513;
  color: #fff;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.25);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.profile-actions :deep(.edit-btn::before),
.profile-actions :deep(.save-btn::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.profile-actions :deep(.edit-btn:hover),
.profile-actions :deep(.save-btn:hover) {
  background: linear-gradient(135deg, #8b4513, #654321);
  border-color: #654321;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(139, 69, 19, 0.35);
}

.profile-actions :deep(.edit-btn:hover::before),
.profile-actions :deep(.save-btn:hover::before) {
  left: 100%;
}

.profile-actions :deep(.edit-btn:active),
.profile-actions :deep(.save-btn:active) {
  transform: translateY(0);
  background: linear-gradient(135deg, #654321, #4a2f1a);
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.25);
}

/* 取消按钮样式 */
.profile-actions :deep(.cancel-btn) {
  height: 36px;
  padding: 0 20px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
  font-family: 'Noto Sans SC', sans-serif;
  letter-spacing: 0.3px;
  background: #fff;
  border: 2px solid rgba(212, 167, 106, 0.4);
  color: #8b4513;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(212, 167, 106, 0.15);
}

.profile-actions :deep(.cancel-btn:hover) {
  background: rgba(212, 167, 106, 0.1);
  border-color: rgba(212, 167, 106, 0.6);
  color: #654321;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(212, 167, 106, 0.25);
}

.profile-actions :deep(.cancel-btn:active) {
  transform: translateY(0);
  background: rgba(212, 167, 106, 0.15);
  box-shadow: 0 2px 6px rgba(212, 167, 106, 0.2);
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(212, 167, 106, 0.1);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  font-weight: 600;
  color: #5d4037;
  min-width: 100px;
}

.info-item .value {
  color: #2c1810;
  flex: 1;
}

.profile-form {
  margin-top: 16px;
}

.content-tabs {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(212, 167, 106, 0.15);
  border: 1px solid rgba(212, 167, 106, 0.2);
}

.content-tabs :deep(.ant-tabs-tab) {
  font-size: 16px;
  font-weight: 500;
}

.content-tabs :deep(.ant-tabs-tab-active) {
  color: #8b4513;
}

.content-tabs :deep(.ant-tabs-ink-bar) {
  background: #8b4513;
}

.tab-content {
  min-height: 400px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  color: #8b4513;
}

.empty-icon {
  font-size: 64px;
  color: rgba(212, 167, 106, 0.5);
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #5d4037;
  margin-bottom: 24px;
}

.resources-grid,
.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.resource-card,
.post-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(212, 167, 106, 0.15);
  box-shadow: 0 2px 8px rgba(139, 69, 19, 0.08);
}

.resource-card:hover,
.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(139, 69, 19, 0.15);
  border-color: rgba(212, 167, 106, 0.3);
}

.resource-cover,
.post-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #fcf9f4, #f5efe8);
}

.resource-cover img,
.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.resource-placeholder,
.post-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8b4513;
  font-size: 48px;
  opacity: 0.5;
}

.resource-info,
.post-info {
  padding: 16px;
}

.resource-title,
.post-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #2c1810;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.resource-intro,
.post-content {
  font-size: 14px;
  color: #5d4037;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
}

.resource-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #8b4513;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.pagination-wrapper :deep(.ant-pagination-item) {
  border-color: #d4a76a;
}

.pagination-wrapper :deep(.ant-pagination-item a) {
  color: #8b4513;
}

.pagination-wrapper :deep(.ant-pagination-item-active) {
  background: #8b4513;
  border-color: #8b4513;
}

.pagination-wrapper :deep(.ant-pagination-item-active a) {
  color: #fff;
}

@media (max-width: 768px) {
  .user-profile-page {
    padding: 16px 4%;
  }

  .profile-card {
    padding: 24px;
  }

  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .resources-grid,
  .posts-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>

