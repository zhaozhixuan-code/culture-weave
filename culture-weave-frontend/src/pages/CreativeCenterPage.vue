<template>
    <div id="creativeCenterPage">
        <section class="hero">
          <div class="seal"></div>
            <div class="hero-inner">
                <div class="hero-copy">
                    <p class="overline">Community / Creative Hub</p>
                    <h1>创作中心</h1>
                    <p class="sub-title">
                        分享非遗灵感、创作故事与传承心得，结识同路人，发现更多文化与设计的火花。
                    </p>
                    <div class="hero-actions">
                        <a-button type="primary" size="large" @click="handleCreate">
                            <PlusOutlined />
                            发布灵感
                        </a-button>
                    </div>
                </div>
                <div class="hero-visual">
                  <div class="cultural-pattern pattern-1"></div>
                  <div class="cultural-pattern pattern-2"></div>
                  <div class="cultural-pattern pattern-3"></div>
                </div>
            </div>
        </section>

        <div class="page-container">
            <section class="toolbar">
                <div class="search-box">
                    <SearchOutlined class="search-icon" />
                    <input v-model="filters.searchText" type="text" placeholder="搜索创作标题或正文关键词"
                        @input="onDebouncedSearch" />
                    <button v-if="filters.searchText" class="clear-search" @click="clearSearch">×</button>
                </div>
                <div class="toolbar-actions">
                    <a-button size="large" @click="onResetFilters">
                        <UndoOutlined />
                        重置
                    </a-button>
                </div>
            </section>

            <a-spin :spinning="loading" size="large">
                <div v-if="!loading && posts.length === 0" class="empty-state">
                    <div class="empty-icon">
                        <CarryOutOutlined />
                    </div>
                    <p class="empty-text">还没有任何创作</p>
                    <p class="empty-hint">抢先分享你的灵感，成为第一位创作者吧！</p>
                    <a-button type="primary" size="large" @click="handleCreate">立即发布</a-button>
                </div>

                <div v-else class="post-masonry">
                    <article v-for="post in posts" :key="post.id" class="post-card" @click="handlePostClick(post)">
                        <div class="post-cover">
                            <img v-if="post.pictureUrl" :src="post.pictureUrl" alt="post cover" loading="lazy" 
                                @error="onCoverError($event)" />
                            <div v-else class="placeholder">
                                <HighlightOutlined />
                                <span>创意灵感</span>
                            </div>
                        </div>

                        <div class="post-body">
                            <h3 class="post-title">{{ post.title || '未命名创作' }}</h3>
                            <div class="post-header-info">
                                <img v-if="post.userVO?.userAvatar" :src="post.userVO.userAvatar" alt="avatar"
                                    class="avatar" />
                                <div v-else class="avatar placeholder-avatar">
                                    {{ abbreviation(post.userVO?.userName) }}
                                </div>
                                <span class="author-name">{{ post.userVO?.userName || '匿名创作者' }}</span>
                            </div>
                        </div>
                    </article>
                </div>
            </a-spin>

            <div v-if="total > pagination.pageSize" class="pagination-wrapper">
                <a-pagination v-model:current="pagination.current" v-model:page-size="pagination.pageSize"
                    :total="total" :show-size-changer="true" :page-size-options="['20', '40', '60']"
                    @change="onPageChange" @show-size-change="onPageSizeChange" show-total />
            </div>
        </div>

        <!-- 帖子详情弹窗 -->
        <PostDetailModal v-model:open="detailModalOpen" :post-id="selectedPostId" @close="handleDetailModalClose" />

        <!-- 登录弹窗 -->
        <LoginModal v-model:open="loginModalVisible" />
    </div>
</template>

<script setup lang="ts">
/// <reference path="../api/typings.d.ts" />
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
    PlusOutlined,
    SearchOutlined,
    UndoOutlined,
    CarryOutOutlined,
    HighlightOutlined,
    ClockCircleOutlined
} from '@ant-design/icons-vue'
import { listResourcesVoByPage1 } from '../api/postController'
import { useLoginUserStore } from '../stores/useLoginUserStore'
import PostDetailModal from '../components/PostDetailModal.vue'
import LoginModal from '../components/LoginModal.vue'

type PostVO = API.PostVO

const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const posts = ref<PostVO[]>([])
const total = ref(0)

const detailModalOpen = ref(false)
const selectedPostId = ref<number | null>(null)

const filters = reactive({
    searchText: ''
})

const pagination = reactive({
    current: 1,
    pageSize: 20
})

const expandedPostIds = ref(new Set<number | undefined>())

const currentUserId = computed(() => loginUserStore.loginUser?.id || null)

// 登录弹窗状态
const loginModalVisible = computed({
  get: () => loginUserStore.loginModalVisible,
  set: (value) => {
    if (!value) {
      loginUserStore.closeLoginModal()
    } else {
      loginUserStore.loginModalVisible = true
    }
  }
})

let searchTimer: number | null = null

async function handleCreate() {
    if (!currentUserId.value) {
        await loginUserStore.fetchLoginUser()
    }
    if (!currentUserId.value) {
        message.warning('请先登录后再发布创作')
        // 打开登录弹窗，登录成功后跳转到创建页面
        loginUserStore.openLoginModal('/creativeCenter/create')
        return
    }
    router.push('/creativeCenter/create')
}

function abbreviation(name?: string | null) {
    if (!name) return '匿'
    return name.slice(0, 1).toUpperCase()
}

function formatTime(value?: string) {
    if (!value) return '刚刚'
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return value
    return date.toLocaleString()
}

function excerpt(content?: string, maxLength: number = 120) {
    if (!content) return '这位创作者暂未留下文字描述。'
    return content.length > maxLength ? `${content.slice(0, maxLength)}...` : content
}

function authorizationLabel(value?: number | null) {
    if (value === 2) return '已授权'
    if (value === 1) return '未授权'
    return ''
}

function statementLabel(value?: number | null) {
    if (value === 2) return '转载'
    if (value === 1) return '原创'
    return ''
}

function toggleExpand(post: PostVO) {
    if (!post.id) return
    const set = expandedPostIds.value
    if (set.has(post.id)) {
        set.delete(post.id)
    } else {
        set.add(post.id)
    }
    expandedPostIds.value = new Set(set)
}

async function fetchPosts(page?: number) {
    if (page) pagination.current = page
    loading.value = true
    try {
        const payload: API.PostQueryRequest = {
            current: pagination.current,
            pageSize: pagination.pageSize,
            sortField: 'createTime',
            sortOrder: 'descend',
            searchText: filters.searchText || undefined
        }
        const res = await listResourcesVoByPage1(payload)
        const responseData = (res as any)?.data as API.BaseResponsePagePostVO
        if (responseData?.code === 0 && responseData?.data) {
            posts.value = responseData.data.records || []
            total.value = responseData.data.total || 0
            pagination.current = responseData.data.current || pagination.current
            pagination.pageSize = responseData.data.size || pagination.pageSize
        } else {
            posts.value = []
            total.value = 0
        }
    } catch (error) {
        console.error('加载社区内容失败', error)
        message.error('加载社区内容失败，请稍后再试')
        posts.value = []
        total.value = 0
    } finally {
        loading.value = false
    }
}

function onDebouncedSearch() {
    if (searchTimer) window.clearTimeout(searchTimer)
    searchTimer = window.setTimeout(() => fetchPosts(1), 400)
}

function onResetFilters() {
    filters.searchText = ''
    fetchPosts(1)
}

function clearSearch() {
    filters.searchText = ''
    fetchPosts(1)
}

function onPageChange(page: number) {
    fetchPosts(page)
}

function onPageSizeChange(_current: number, size: number) {
    pagination.current = 1
    pagination.pageSize = size
    fetchPosts(1)
}

function onCoverError(event: Event) {
    const img = event.target as HTMLImageElement
    if (img) {
        img.style.display = 'none'
        const parent = img.parentElement
        if (parent) {
            let placeholder = parent.querySelector('.placeholder') as HTMLElement
            if (!placeholder) {
                placeholder = document.createElement('div')
                placeholder.className = 'placeholder'
                const icon = document.createElement('div')
                icon.innerHTML = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>'
                const text = document.createElement('span')
                text.textContent = '创意灵感'
                placeholder.appendChild(icon)
                placeholder.appendChild(text)
                parent.appendChild(placeholder)
            }
            placeholder.style.display = 'flex'
        }
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
    fetchPosts(1)
})
</script>

<style scoped>
/* 非遗文化主题样式 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;500;600;700&family=Noto+Sans+SC:wght@300;400;500;600&display=swap');

#creativeCenterPage {
    min-height: calc(100vh - 200px);
    padding: 0;
  background: linear-gradient(180deg,
  #fcf9f4 0%,
  #fff 100%);
}

/* Hero部分 - 非遗文化风格 */
.hero {
  padding: 100px 8% 80px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg,
  rgba(244, 237, 232, 0.98) 0%,
  rgba(251, 247, 240, 0.95) 100%),
  url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23d4a76a' fill-opacity='0.06' fill-rule='evenodd'/%3E%3C/svg%3E");
  border-bottom: 1px solid rgba(212, 167, 106, 0.2);
  margin-bottom: 40px;
  position: relative;
}

.hero::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg,
  #b8860b 0%,
  #d4a76a 25%,
  #a0522d 50%,
  #8b4513 75%,
  #654321 100%);
  z-index: 1;
}

.hero::after {
  content: '';
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  bottom: 20px;
  border: 1px solid rgba(212, 167, 106, 0.15);
  pointer-events: none;
  border-radius: 4px;
  z-index: 0;
}

.hero-inner {
  max-width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
  gap: 60px;
  position: relative;
  z-index: 2;
}

.hero-copy {
  max-width: 600px;
  animation: fadeInUp 1s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-copy .overline {
  display: inline-block;
  padding: 8px 20px;
  background: rgba(164, 123, 67, 0.1);
  backdrop-filter: blur(8px);
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 2px;
  color: #8b4513;
  margin-bottom: 24px;
  border: 1px solid rgba(164, 123, 67, 0.2);
  position: relative;
  font-family: 'Noto Serif SC', serif;
}

.hero-copy .overline::before {
  content: '「';
  position: absolute;
  left: -10px;
  color: #d4a76a;
}

.hero-copy .overline::after {
  content: '」';
  position: absolute;
  right: -10px;
  color: #d4a76a;
}

.hero-copy h1 {
  font-size: 3.2rem;
  margin: 0 0 24px 0;
  color: #2c1810;
    font-weight: 700;
  line-height: 1.2;
  font-family: 'Noto Serif SC', serif;
  position: relative;
  padding-left: 24px;
  border-left: 6px solid #d4a76a;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.05);
}

.hero-copy h1::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 24px;
  width: 100px;
  height: 2px;
  background: linear-gradient(90deg, #d4a76a, transparent);
}

.sub-title {
  font-size: 18px;
  color: #5d4037;
  line-height: 1.8;
  margin-bottom: 36px;
  font-weight: 400;
  padding: 24px;
  background: rgba(255, 253, 249, 0.9);
  border-radius: 8px;
  border: 1px solid rgba(212, 167, 106, 0.3);
  box-shadow: 0 4px 20px rgba(212, 167, 106, 0.1);
  position: relative;
  font-family: 'Noto Sans SC', sans-serif;
}

.sub-title::before {
  content: '✦';
  position: absolute;
  left: -12px;
  top: 50%;
  transform: translateY(-50%);
  color: #d4a76a;
  font-size: 20px;
}

.hero-actions :deep(.ant-btn-primary) {
  height: 52px;
  padding: 0 32px;
    font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #a0522d, #8b4513);
  border: none;
  box-shadow: 0 8px 24px rgba(139, 69, 19, 0.25);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  position: relative;
  overflow: hidden;
  font-family: 'Noto Sans SC', sans-serif;
}

.hero-actions :deep(.ant-btn-primary)::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.hero-actions :deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(139, 69, 19, 0.35);
  background: linear-gradient(135deg, #8b4513, #654321);
}

.hero-actions :deep(.ant-btn-primary:hover)::before {
  left: 100%;
}

.hero-visual {
    flex: 1;
    position: relative;
  min-height: 320px;
  animation: fadeInRight 1.2s ease-out 0.3s both;
  display: flex;
  align-items: center;
  justify-content: center;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(60px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 非遗文化装饰元素 */
.cultural-pattern {
    position: absolute;
    opacity: 0.8;
  z-index: 1;
}

.pattern-1 {
  width: 120px;
  height: 120px;
  top: 10%;
  right: 15%;
  background-image: url("data:image/svg+xml,%3Csvg width='120' height='120' viewBox='0 0 120 120' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M60 10c27.614 0 50 22.386 50 50s-22.386 50-50 50S10 87.614 10 60 32.386 10 60 10zm0 10c-22.091 0-40 17.909-40 40s17.909 40 40 40 40-17.909 40-40-17.909-40-40-40z' fill='none' stroke='%23d4a76a' stroke-width='2' stroke-opacity='0.3'/%3E%3Cpath d='M60 30c16.569 0 30 13.431 30 30S76.569 90 60 90 30 76.569 30 60s13.431-30 30-30z' fill='none' stroke='%23a0522d' stroke-width='2' stroke-opacity='0.2'/%3E%3C/svg%3E");
  animation: rotate 30s linear infinite;
}

.pattern-2 {
  width: 80px;
  height: 80px;
  bottom: 20%;
    left: 10%;
  background-image: url("data:image/svg+xml,%3Csvg width='80' height='80' viewBox='0 0 80 80' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M40 10c16.542 0 30 13.458 30 30S56.542 70 40 70 10 56.542 10 40s13.458-30 30-30zm0 5c13.807 0 25 11.193 25 25S53.807 65 40 65 15 53.807 15 40 26.193 15 40 15z' fill='none' stroke='%238b4513' stroke-width='2' stroke-opacity='0.2'/%3E%3Ccircle cx='40' cy='40' r='15' fill='none' stroke='%23d4a76a' stroke-width='2' stroke-opacity='0.2'/%3E%3C/svg%3E");
  animation: rotateReverse 40s linear infinite;
}

.pattern-3 {
  width: 100px;
  height: 100px;
  top: 40%;
  left: 20%;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M50 5c24.853 0 45 20.147 45 45S74.853 95 50 95 5 74.853 5 50 25.147 5 50 5zm0 10c-19.33 0-35 15.67-35 35s15.67 35 35 35 35-15.67 35-35-15.67-35-35-35z' fill='none' stroke='%23654321' stroke-width='2' stroke-opacity='0.2'/%3E%3Cpath d='M50 20c16.542 0 30 13.458 30 30S66.542 80 50 80 20 66.542 20 50s13.458-30 30-30z' fill='none' stroke='%23b8860b' stroke-width='2' stroke-opacity='0.2'/%3E%3C/svg%3E");
  animation: float 6s ease-in-out infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes rotateReverse {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(-360deg);
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(20px, -20px) scale(1.05);
  }
  66% {
    transform: translate(-15px, 15px) scale(0.95);
  }
}

/* 印章效果 */
.seal {
  position: absolute;
  top: 40px;
  right: 60px;
  width: 80px;
  height: 80px;
  background: rgba(164, 0, 0, 0.9);
  border-radius: 4px;
  transform: rotate(15deg);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 3;
}

.seal::before {
  content: '非遗';
  color: rgba(255, 255, 255, 0.9);
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 4px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

.seal::after {
  content: '';
  position: absolute;
  top: 5px;
  left: 5px;
  right: 5px;
  bottom: 5px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

/* 页面容器 */
.page-container {
  padding: 32px;
    max-width: 1800px;
  margin: -20px auto 48px;
  background: rgba(255, 253, 249, 0.95);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(212, 167, 106, 0.15);
  position: relative;
  z-index: 2;
}

.toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    flex-wrap: wrap;
}

.search-box {
    flex: 1;
    min-width: 260px;
    position: relative;
    background: #fff;
  border-radius: 8px;
    padding: 12px 18px;
    display: flex;
    align-items: center;
    gap: 12px;
  box-shadow: 0 4px 16px rgba(139, 69, 19, 0.12);
  border: 1px solid rgba(212, 167, 106, 0.3);
}

.search-box input {
    flex: 1;
    border: none;
    outline: none;
    font-size: 15px;
    background: transparent;
  font-family: 'Noto Sans SC', sans-serif;
}

.search-icon {
  color: #8b4513;
    font-size: 18px;
}

.clear-search {
    border: none;
  background: rgba(139, 69, 19, 0.08);
    width: 26px;
    height: 26px;
    border-radius: 50%;
    font-size: 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s ease;
  color: #8b4513;
}

.clear-search:hover {
  background: rgba(139, 69, 19, 0.15);
}

.toolbar-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.toolbar-actions :deep(.ant-btn) {
  border-color: #d4a76a;
  color: #8b4513;
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
    width: 90px;
    height: 90px;
    border-radius: 50%;
  background: rgba(212, 167, 106, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36px;
  color: #8b4513;
    margin-bottom: 16px;
}

.empty-text {
    font-size: 20px;
    font-weight: 600;
    margin: 0 0 8px 0;
  color: #2c1810;
  font-family: 'Noto Serif SC', serif;
}

.empty-hint {
    font-size: 14px;
    margin-bottom: 16px;
  color: #5d4037;
  font-family: 'Noto Sans SC', sans-serif;
}

.empty-state :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #a0522d, #8b4513);
  border: none;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 瀑布流布局 */
.post-masonry {
    column-count: 6;
    column-gap: 16px;
    padding: 0;
    column-fill: balance;
}

.post-card {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(139, 69, 19, 0.08);
    display: inline-block;
    width: 100%;
    margin-bottom: 16px;
    break-inside: avoid;
    position: relative;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    cursor: pointer;
    border: 1px solid rgba(212, 167, 106, 0.15);
}

.post-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(139, 69, 19, 0.15);
    border-color: rgba(212, 167, 106, 0.3);
}

.post-cover {
    position: relative;
    width: 100%;
    overflow: hidden;
    background: linear-gradient(135deg, #fcf9f4, #f5efe8);
    border-radius: 12px 12px 0 0;
    margin-bottom: 0;
}

.post-cover img {
    width: 100%;
    height: auto;
    display: block;
    object-fit: cover;
}

.post-cover .placeholder {
    width: 100%;
    min-height: 200px;
    padding: 40px 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    color: #8b4513;
    gap: 8px;
    font-size: 14px;
}

.post-cover .placeholder :deep(.anticon) {
    font-size: 48px;
    opacity: 0.6;
}

.post-body {
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    position: relative;
}

.post-header-info {
    display: flex;
    align-items: center;
    gap: 6px;
}

.avatar {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    object-fit: cover;
    background: rgba(212, 167, 106, 0.2);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #2c1810;
    font-weight: 600;
    font-size: 11px;
    flex-shrink: 0;
    font-family: 'Noto Sans SC', sans-serif;
}

.placeholder-avatar {
    background: rgba(212, 167, 106, 0.2);
}

.author-name {
    font-size: 12px;
    font-weight: 400;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-family: 'Noto Sans SC', sans-serif;
    line-height: 1.2;
}

.post-title {
    font-size: 14px;
    font-weight: 400;
    margin: 0;
    color: #333;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    font-family: 'Noto Sans SC', sans-serif;
    word-break: break-word;
}


.pagination-wrapper {
    margin-top: 32px;
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

.pagination-wrapper :deep(.ant-select-selector) {
  border-color: #d4a76a !important;
}


/* 响应式设计 */
@media (max-width: 1600px) {
    .post-masonry {
        column-count: 5;
        column-gap: 16px;
    }
}

@media (max-width: 1400px) {
    .post-masonry {
        column-count: 4;
        column-gap: 16px;
    }
}

@media (max-width: 1024px) {
  .hero {
    padding: 80px 5% 60px;
  }

  .hero-copy h1 {
    font-size: 2.6rem;
  }

  .seal {
    width: 60px;
    height: 60px;
    top: 30px;
    right: 40px;
  }

  .seal::before {
    font-size: 18px;
  }

  .hero-visual {
    min-height: 280px;
  }

  .pattern-1 {
    width: 100px;
    height: 100px;
  }

  .pattern-2 {
    width: 60px;
    height: 60px;
  }

  .pattern-3 {
    width: 80px;
    height: 80px;
  }

  .post-masonry {
        column-count: 3;
        column-gap: 12px;
    }
    
    .post-card {
        margin-bottom: 12px;
    }
}

@media (max-width: 768px) {
  .hero {
    padding: 60px 4% 40px;
  }

  .hero-inner {
        flex-direction: column;
    gap: 40px;
  }

  .hero-copy {
    text-align: center;
    max-width: 100%;
  }

  .hero-copy h1 {
    font-size: 2.2rem;
    padding-left: 0;
    border-left: none;
    text-align: center;
    border-bottom: 3px solid #d4a76a;
    padding-bottom: 12px;
  }

  .hero-copy h1::after {
    display: none;
  }

  .sub-title::before {
    left: 50%;
    top: -12px;
    transform: translateX(-50%);
  }

  .seal {
    position: relative;
    top: 0;
    right: 0;
    margin: 0 auto 20px;
    transform: rotate(0);
  }

  .cultural-pattern {
    opacity: 0.3;
    }

  .hero-visual {
    min-height: 200px;
  }

  .toolbar {
        flex-direction: column;
        align-items: stretch;
    }

  .toolbar-actions {
        justify-content: flex-end;
    }

  .post-masonry {
        column-count: 2;
        column-gap: 12px;
    }

    .post-card {
        margin-bottom: 12px;
    }
}

@media (max-width: 480px) {
    .post-masonry {
        column-count: 1;
        column-gap: 0;
    }
    
    .post-card {
        margin-bottom: 16px;
    }
}
</style>
