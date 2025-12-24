<template>
  <div id="resourcesPage" :class="{ 'page-entered': pageReady }">
    <!-- 顶部融合区域：标题 + 搜索筛选 -->
    <section
      :class="['hero-search-wrap', 'section-anim', { 'search-loading': showTopBarLoading, 'is-visible': pageReady }]">
      <div v-if="showTopBarLoading" class="search-loading-overlay" aria-hidden="true"></div>
      <div class="hero-search-inner">
        <header class="page-hero hero-inline">
          <div class="hero-text">
            <p class="hero-tag">文化探索</p>
            <h1 class="page-title">非遗资源库</h1>
            <p class="page-subtitle">探索来自全国各地的非物质文化遗产资源</p>
          </div>
        </header>

        <div class="search-bar">
          <div class="search-input-wrapper">
            <div class="search-input-container">
              <input v-model="filters.searchText" type="text"
                     :class="['search-input', { 'loading-glimmer': showTopBarLoading }]"
                     placeholder="搜索资源名称、简介或发布者..." @input="onDebouncedSearch" @keydown.enter="handleSearch"
                     @focus="onSearchFocus" @blur="onSearchBlur" />
              <button v-if="filters.searchText" class="clear-btn" @click="clearSearch" title="清除搜索">
                <span class="clear-icon">×</span>
              </button>
              <!-- 搜索下拉：热搜与历史 -->
              <div v-if="dropdownVisible" class="search-dropdown" @mousedown.prevent>
                <div v-if="hotLoading || historyLoading" class="search-loading-inline">
                  <a-spin size="small" />
                </div>
                <div class="search-section history-section">
                  <div class="section-title">搜索记录</div>
                  <div class="section-body">
                    <template v-if="historySearches.length > 0">
                      <button v-for="t in historySearches" :key="t" class="search-term" @click="applySearchTerm(t)">{{ t }}</button>
                    </template>
                    <template v-else-if="!historyLoading">
                      <div class="empty-term">暂无搜索记录</div>
                    </template>
                  </div>
                </div>
                <div class="search-section hot-section">
                  <div class="section-title">热搜</div>
                  <div class="section-body">
                    <template v-if="hotSearches.length > 0">
                      <button v-for="t in hotSearches" :key="t" class="search-term hot" @click="applySearchTerm(t)">{{ t }}</button>
                    </template>
                    <template v-else-if="!hotLoading">
                      <div class="empty-term">暂无热搜</div>
                    </template>
                  </div>
                </div>
              </div>
            </div>
            <button :class="['search-btn', { searching: loading, 'loading-glimmer': showTopBarLoading }]"
                    :disabled="loading" @click="handleSearch" title="搜索">
              <a-spin v-if="loading" :spinning="true" size="small" />
              <SearchOutlined v-else />
            </button>
            <div class="search-quick-actions">
              <a-button type="default" size="middle" @click="onPublish" class="publish-btn">
                                <span class="btn-icon-wrapper">
                                    <PlusOutlined class="btn-icon" />
                                </span>
                <span class="btn-text">上传资源</span>
              </a-button>
              <button class="ai-chat-btn" @click="toggleAiChat" title="AI问非遗">
                                <span class="btn-icon-wrapper">
                                    <RobotOutlined class="btn-icon" />
                                </span>
                <span class="btn-text">AI问非遗</span>
              </button>
            </div>
          </div>
        </div>

        <div class="filters-bar">
          <div class="filter-group">
            <label class="filter-label">地区筛选</label>
            <select v-model="filters.region"
                    :class="['filter-select', { 'loading-glimmer': showTopBarLoading }]" @change="fetchList(1)">
              <option value="">全部地区</option>
              <option v-for="r in regions" :key="r" :value="r">{{ r }}</option>
            </select>
          </div>

          <div class="filter-group">
            <label class="filter-label">分类筛选</label>
            <select v-model="filters.category"
                    :class="['filter-select', { 'loading-glimmer': showTopBarLoading }]" @change="fetchList(1)">
              <option value="">全部分类</option>
              <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
            </select>
          </div>
          <button :class="['reset-btn', { 'loading-glimmer': showTopBarLoading }]" @click="onReset">重置筛选</button>
        </div>
      </div>
    </section>

    <!-- AI问答框 - 使用 Teleport 传送到 body 以确保固定定位正确 -->
    <Teleport to="body">
      <transition name="ai-chat-slide">
        <div v-if="showAiChat" class="ai-chat-container">
          <div class="ai-chat-box">
            <div class="ai-chat-header">
              <div class="ai-chat-title">
                <RobotOutlined />
                <span>AI问非遗</span>
              </div>
              <button class="ai-chat-close" @click="closeAiChat" title="关闭">
                <CloseOutlined />
              </button>
            </div>
            <div class="ai-chat-messages" ref="messagesContainer" @click="handleLinkClick">
              <div v-if="aiMessages.length === 0" class="ai-chat-empty">
                <p>您好！我是非遗AI助手，可以回答关于非物质文化遗产的问题。</p>
                <p class="ai-chat-hint">例如：苏绣发源地在哪？</p>
              </div>
              <div v-for="(msg, index) in aiMessages" :key="index" :class="['ai-message', msg.role]">
                <div v-if="msg.content || msg.loading" class="ai-message-content">
                  <span v-html="parseMessageContent(msg.content)"></span>
                  <span v-if="msg.loading && !msg.content"
                        class="ai-message-placeholder">思考中...</span>
                </div>
                <div v-if="msg.role === 'assistant' && msg.loading" class="ai-message-loading">
                  <a-spin size="small" />
                </div>
              </div>
            </div>
            <div class="ai-chat-input-wrapper">
              <input v-model="aiInputMessage" type="text" class="ai-chat-input" placeholder="苏绣发源地在哪？"
                     @keydown.enter="sendAiMessage" :disabled="aiLoading" />
              <button class="ai-chat-send" @click="sendAiMessage"
                      :disabled="aiLoading || !aiInputMessage.trim()">
                <SendOutlined v-if="!aiLoading" />
                <a-spin v-else size="small" />
              </button>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>

    <!-- 登录弹窗 -->
    <LoginModal v-model:open="loginModalVisible" />

    <!-- 资源列表展示区域 - 表格形式 -->
    <section :class="['resources-section', 'section-anim', { 'is-visible': pageReady }]">
      <div class="resources-container">
        <div v-if="loading" class="loading-state">
          <a-spin size="large" />
          <p>加载中...</p>
        </div>

        <div v-else-if="list.length === 0" class="empty-state">
          <div class="empty-icon">
            <FileSearchOutlined :style="{ fontSize: '64px', color: '#d4a76a' }" />
          </div>
          <p class="empty-text">暂无资源数据</p>
          <p class="empty-hint">试试调整筛选条件或搜索关键词</p>
          <a-button type="primary" @click="onPublish" class="empty-action-btn">
            <PlusOutlined />
            上传首个资源
          </a-button>
        </div>

        <div v-else class="resources-list-container">
          <transition-group name="resource-item" tag="div" class="resources-list">
            <div v-for="(item, index) in list" :key="item.id"
                 class="resource-item"
                 @click="goToDetail(item.id)">
              <!-- 左侧图片 -->
              <div class="resource-item-image">
                <div class="image-wrapper">
                  <img v-if="item.resourceImgUrl"
                       :src="item.resourceImgUrl"
                       :alt="item.name || '资源封面'"
                       loading="lazy"
                       @error="onImageError($event)" />
                  <div v-else class="image-placeholder">
                    <PictureOutlined />
                  </div>
                </div>
              </div>

              <!-- 中间内容区 -->
              <div class="resource-item-content">
                <div class="resource-header">
                  <h3 class="resource-title">
                    {{ item.name || '未命名资源' }}
                    <span v-if="item.price && item.price > 0" class="commercial-indicator">
                                            <DollarOutlined />
                                        </span>
                  </h3>
                  <div class="resource-meta-row">
                                        <span v-if="item.category" class="meta-item category-badge">
                                            <FolderOutlined />
                                            {{ item.category }}
                                        </span>
                    <span v-if="item.region" class="meta-item region-badge">
                                            <EnvironmentOutlined />
                                            {{ item.region }}
                                        </span>
                    <span class="meta-item price-badge" v-if="item.price && item.price > 0">
                                            {{ formatPrice(item.price) }}
                                        </span>
                  </div>
                </div>

                <p class="resource-intro">{{ truncateText(item.introduction, 50) }}</p>

                <div class="resource-footer">
                  <div class="tags-section" v-if="normalizeTags(item.tags).length > 0">
                    <TagOutlined class="tags-icon" />
                    <div class="tags-list">
                                            <span v-for="tag in normalizeTags(item.tags).slice(0, 5)" :key="tag"
                                                  class="tag-chip">
                                                {{ tag }}
                                            </span>
                      <span v-if="normalizeTags(item.tags).length > 5" class="tag-more">
                                                +{{ normalizeTags(item.tags).length - 5 }}
                                            </span>
                    </div>
                  </div>
                  <div class="resource-author">
                    <UserOutlined />
                    <span>{{ item.userName || '匿名' }}</span>
                  </div>
                </div>
              </div>

              <!-- 右侧操作区 -->
              <div class="resource-item-actions" @click.stop>
                <a-button type="primary" size="middle" @click="goToDetail(item.id)" class="view-btn">
                  <EyeOutlined />
                  查看详情
                </a-button>
              </div>
            </div>
          </transition-group>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-wrapper">
          <a-pagination v-model:current="pagination.current" v-model:page-size="pagination.pageSize"
                        :total="total" :show-size-changer="true" :show-quick-jumper="true"
                        :page-size-options="['10', '20', '50', '100']" show-total @change="onPageChange"
                        @show-size-change="onPageSizeChange" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  PictureOutlined,
  DollarOutlined,
  EnvironmentOutlined,
  FolderOutlined,
  TagOutlined,
  UserOutlined,
  PlusOutlined,
  RobotOutlined,
  CloseOutlined,
  SendOutlined,
  FileSearchOutlined,
  EyeOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue'
import {
  listResourcesVoByPage,
  getResourceCategoryList,
  listHotSearchText,
  getSearchHistory
} from '../api/resourcesController'
import { useLoginUserStore } from '../stores/useLoginUserStore'
import myAxios from '../request'
import LoginModal from '../components/LoginModal.vue'

type ResourceVO = {
  id?: number
  name?: string
  introduction?: string
  category?: string
  tags?: string | string[]
  region?: string
  userName?: string
  price?: number
  resourceImgUrl?: string
  createTime?: string
  userId?: number
}

type ResourcePageData = {
  records?: ResourceVO[]
  total?: number
  size?: number
  current?: number
}

type ResourceCategoryMeta = {
  categoryList?: string[]
  regionList?: string[]
}

type ResourceResponse<T> = {
  code?: number
  data?: T
  message?: string
}

type ResourceQueryPayload = {
  current?: number
  pageSize?: number
  sortField?: string
  sortOrder?: string
  searchText?: string
  category?: string
  region?: string
  userName?: string
  tags?: string[]
}

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

// --- 状态 ---
const loading = ref(false)
const list = ref<ResourceVO[]>([])
const total = ref(0)
const categories = ref<string[]>([])
const regions = ref<string[]>([])
const pageReady = ref(false)

// 获取当前登录用户ID
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

const filters = reactive<{
  searchText: string
  region: string
  category: string
  tagsText: string
}>({
  searchText: '',
  region: '',
  category: '',
  tagsText: ''
})

const pagination = reactive({ current: 1, pageSize: 24 })

const showTopBarLoading = computed(() => loading.value)

// 热搜词与历史搜索
const hotSearches = ref<string[]>([])
const historySearches = ref<string[]>([])
const hotLoading = ref(false)
const historyLoading = ref(false)
const dropdownVisible = computed(() => {
  // 始终在输入框聚焦时展示下拉（即使没有数据）
  return searchFocused.value
})

// AI问答相关状态
const showAiChat = ref(false)
const aiInputMessage = ref('')
const aiMessages = ref<Array<{ role: 'user' | 'assistant'; content: string; loading?: boolean }>>([])
const aiLoading = ref(false)
const currentChatId = ref(`chat_${Date.now()}`)
const messagesContainer = ref<HTMLElement | null>(null)

// --- 工具函数 ---
let searchTimer: number | null = null

function onDebouncedSearch() {
  if (searchTimer) window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => fetchList(1), 400)
}

function handleSearch() {
  if (searchTimer) window.clearTimeout(searchTimer)
  fetchList(1)
}

function clearSearch() {
  filters.searchText = ''
  fetchList(1)
}

const searchFocused = ref(false)

function onSearchFocus() {
  searchFocused.value = true
  // 加载热搜和历史（若登录则加载历史）
  loadHotSearches()
  if (currentUserId.value) {
    loadSearchHistory()
  } else {
    historySearches.value = []
    historyLoading.value = false
  }
}

function onSearchBlur() {
  // 延迟一下，让清除按钮的点击事件能够触发
  setTimeout(() => {
    searchFocused.value = false
  }, 200)
}

async function loadHotSearches() {
  hotLoading.value = true
  try {
    const res = await listHotSearchText()
    const raw = (res as any)
    // 支持多种返回包装形式
    if (raw?.data?.code === 0 && Array.isArray(raw.data.data)) {
      hotSearches.value = raw.data.data || []
    } else if (Array.isArray(raw?.data)) {
      hotSearches.value = raw.data || []
    } else if (Array.isArray(raw?.data?.data)) {
      hotSearches.value = raw.data.data || []
    } else {
      hotSearches.value = []
    }
  } catch (e) {
    console.error('加载热搜词失败', e)
    hotSearches.value = []
  } finally {
    hotLoading.value = false
  }
}

async function loadSearchHistory() {
  if (!currentUserId.value) {
    historySearches.value = []
    return
  }
  historyLoading.value = true
  try {
    const res = await getSearchHistory({ UserId: currentUserId.value } as any)
    const raw = (res as any)
    if (raw?.data?.code === 0 && Array.isArray(raw.data.data)) {
      historySearches.value = raw.data.data || []
    } else if (Array.isArray(raw?.data)) {
      historySearches.value = raw.data || []
    } else if (Array.isArray(raw?.data?.data)) {
      historySearches.value = raw.data.data || []
    } else {
      historySearches.value = []
    }
  } catch (e) {
    console.error('加载搜索历史失败', e)
    historySearches.value = []
  } finally {
    historyLoading.value = false
  }
}

function applySearchTerm(term: string) {
  filters.searchText = term
  // 立即执行搜索
  fetchList(1)
}

function normalizeTags(v: unknown): string[] {
  if (Array.isArray(v)) {
    return v.map(item => (typeof item === 'string' ? item.trim() : '')).filter(Boolean)
  }
  if (typeof v === 'string') {
    return parseTagsText(v)
  }
  return []
}

function parseTagsText(text?: string | null): string[] {
  if (!text) return []
  return text
    .split(/[，,、;；\s\n\r\t]+/)
    .map(s => s.trim())
    .filter(Boolean)
}

function stringifyTags(tags: string[]): string {
  return tags.join('， ')
}

function commitTagsInput() {
  filters.tagsText = stringifyTags(parseTagsText(filters.tagsText))
  fetchList(1)
}

function formatPrice(v?: number) {
  if (typeof v === 'number' && !Number.isNaN(v)) {
    return `¥ ${Math.trunc(v)}`
  }
  return '-'
}

function onImageError(event: Event) {
  const target = event.target as HTMLImageElement
  if (target) {
    target.style.display = 'none'
    const placeholder = target.nextElementSibling as HTMLElement
    if (placeholder && placeholder.classList.contains('resource-image-placeholder')) {
      placeholder.style.display = 'flex'
    }
  }
}

// --- 数据获取 ---
async function fetchList(page?: number) {
  loading.value = true
  try {
    if (page) pagination.current = page

    const parsedTags = parseTagsText(filters.tagsText)
    const payload: ResourceQueryPayload = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'createTime',
      sortOrder: 'descend',
      searchText: filters.searchText || undefined,
      region: filters.region || undefined,
      category: filters.category || undefined,
      tags: parsedTags.length > 0 ? parsedTags : undefined
    }

    const res = await listResourcesVoByPage(payload)
    // 你的接口返回形态：res.data 是 BaseResponsePageResourcesVO
    // 这里安全地取值并兼容后端约定
    const responseData = res as any
    if (responseData?.code === 0 && responseData?.data) {
      const { records = [], total: totalCount = 0, current, size } = responseData.data
      list.value = records
      total.value = totalCount
      pagination.current = current || pagination.current
      pagination.pageSize = size || pagination.pageSize
    } else {
      // 有些后端 wrapper 层会返回 res.data.data 等，兼容尝试：
      const alt = (res && (res as any).data) ? (res as any).data : null
      if (alt && alt.data) {
        const { records = [], total: totalCount = 0, current, size } = alt.data
        list.value = records
        total.value = totalCount
        pagination.current = current || pagination.current
        pagination.pageSize = size || pagination.pageSize
      } else {
        console.warn('加载失败或返回结构未知', res)
        list.value = []
        total.value = 0
      }
    }
  } catch (e) {
    console.error('加载失败', e)
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadMeta() {
  try {
    const res = await getResourceCategoryList()
    // 接口返回 wrapper，安全兼容处理
    const responseData = res as any
    if (responseData?.code === 0 && responseData?.data) {
      categories.value = responseData.data.categoryList || []
      regions.value = responseData.data.regionList || []
    } else if (responseData && responseData.data && responseData.data.data) {
      const inner = responseData.data.data
      categories.value = inner.categoryList || []
      regions.value = inner.regionList || []
    } else {
      // 兜底：尝试直接取 res.data
      categories.value = (res as any)?.data?.categoryList || []
      regions.value = (res as any)?.data?.regionList || []
    }
  } catch (e) {
    console.error('获取分类/地域失败', e)
  }
}

function onReset() {
  filters.searchText = ''
  filters.region = ''
  filters.category = ''
  filters.tagsText = ''
  pagination.current = 1
  pagination.pageSize = 24
  fetchList(1)
}

function onPageChange(page: number) {
  fetchList(page)
}

function onPageSizeChange(current: number, size: number) {
  pagination.current = 1
  pagination.pageSize = size
  fetchList(1)
}

function goToDetail(id?: number | string) {
  if (id !== undefined && id !== null) {
    // 将 ID 转换为字符串，避免大数字精度丢失
    // 这样可以正确处理超过 JavaScript 安全整数范围（2^53）的 ID
    router.push(`/resources/${String(id)}`)
  }
}

function onPublish() {
  // 检查是否登录
  if (!currentUserId.value) {
    message.warning('请先登录后再添加非遗资源')
    // 打开登录弹窗
    loginUserStore.openLoginModal('/addResources')
    return
  }

  // 已登录，跳转到添加资源页面
  router.push('/addResources')
}

// AI问答相关函数
function toggleAiChat() {
  // 如果正在关闭，直接关闭
  if (showAiChat.value) {
    showAiChat.value = false
    return
  }

  // 如果正在打开，需要检查登录状态
  // 检查是否登录
  if (!currentUserId.value) {
    message.warning('请先登录后再使用AI问答功能')
    // 打开登录弹窗
    loginUserStore.openLoginModal()
    return
  }

  // 已登录，打开问答框
  showAiChat.value = true
  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

function closeAiChat() {
  showAiChat.value = false
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 解析消息内容，将 Markdown 链接转换为可点击的 HTML 链接
function parseMessageContent(content: string): string {
  if (!content) return ''

  // 转义 HTML 特殊字符，防止 XSS 攻击
  const escapeHtml = (text: string) => {
    const map: { [key: string]: string } = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      '\'': '&#039;'
    }
    return text.replace(/[&<>"']/g, (m) => map[m])
  }

  // 匹配 Markdown 链接格式：[文本](URL)
  // 使用非贪婪匹配，支持多个链接
  const linkRegex = /\[([^\]]+)\]\(([^)]+)\)/g

  const placeholders: string[] = []
  const links: Array<{ text: string; url: string }> = []
  let placeholderIndex = 0

  // 用占位符替换所有链接，同时收集链接信息
  let result = content.replace(linkRegex, (match, linkText, url) => {
    const placeholder = `__LINK_PLACEHOLDER_${placeholderIndex}__`
    placeholders.push(placeholder)
    links.push({ text: linkText, url: url })
    placeholderIndex++
    return placeholder
  })

  // 转义整个文本（占位符不会被转义影响）
  result = escapeHtml(result)

  // 恢复占位符为 HTML 链接
  links.forEach((link, index) => {
    const escapedText = escapeHtml(link.text)
    const originalUrl = link.url

    // 处理 URL：判断是内部链接还是外部链接
    let finalUrl = originalUrl
    let isExternal = false

    // 检查是否是外部链接（http/https协议开头）
    if (originalUrl.startsWith('http://') || originalUrl.startsWith('https://')) {
      isExternal = true
      finalUrl = escapeHtml(originalUrl)
    }
    // 检查是否是协议相对链接
    else if (originalUrl.startsWith('//')) {
      isExternal = true
      finalUrl = escapeHtml(originalUrl)
    }
    // 处理 localhost URL：如果是 localhost:端口号，提取路径部分作为内部链接
    else if (originalUrl.includes('localhost:') || originalUrl.includes('127.0.0.1:')) {
      // 提取路径部分，例如 localhost:5173/resources/xxx -> /resources/xxx
      // 找到第一个斜杠的位置
      const slashIndex = originalUrl.indexOf('/', originalUrl.indexOf(':'))
      if (slashIndex !== -1) {
        finalUrl = originalUrl.substring(slashIndex)
      } else {
        finalUrl = '/'
      }
      finalUrl = escapeHtml(finalUrl)
    }
    // 相对路径，确保以 / 开头
    else {
      finalUrl = originalUrl.startsWith('/') ? escapeHtml(originalUrl) : '/' + escapeHtml(originalUrl)
    }

    // 创建 HTML 链接
    const htmlLink = `<a href="${finalUrl}" class="ai-message-link" data-href="${finalUrl}" ${isExternal ? 'target="_blank" rel="noopener noreferrer"' : ''}>${escapedText}</a>`

    // 替换占位符
    result = result.replace(placeholders[index], htmlLink)
  })

  // 将换行符转换为 <br>
  result = result.replace(/\n/g, '<br>')

  return result
}

// 处理链接点击事件（事件委托）
function handleLinkClick(event: Event) {
  const target = event.target as HTMLElement
  const link = target.closest('.ai-message-link') as HTMLAnchorElement

  if (link) {
    event.preventDefault()
    event.stopPropagation()

    const href = link.getAttribute('data-href') || link.getAttribute('href') || ''

    if (href) {
      // 如果链接已经有 target="_blank"，让浏览器默认行为处理
      if (link.hasAttribute('target') && link.getAttribute('target') === '_blank') {
        window.open(href, '_blank', 'noopener,noreferrer')
      }
      // 如果是外部链接（以 http/https 开头），在新窗口打开
      else if (href.startsWith('http://') || href.startsWith('https://') || href.startsWith('//')) {
        window.open(href, '_blank', 'noopener,noreferrer')
      }
      // 如果是内部链接，使用 Vue Router 跳转
      else {
        router.push(href)
      }
    }
  }
}

async function sendAiMessage() {
  const messageText = aiInputMessage.value.trim()
  if (!messageText || aiLoading.value) return

  // 添加用户消息
  aiMessages.value.push({
    role: 'user',
    content: messageText
  })
  aiInputMessage.value = ''

  // 添加助手占位消息
  const assistantIndex = aiMessages.value.length
  aiMessages.value.push({
    role: 'assistant',
    content: '',
    loading: true
  })

  aiLoading.value = true
  scrollToBottom()

  try {
    // 使用 fetch 处理 SSE 流
    const baseURL = myAxios.defaults.baseURL || 'http://localhost:8123'
    const params = new URLSearchParams({
      message: messageText,
      chatId: currentChatId.value
    })
    const url = `${baseURL}/ai/chat/sse?${params.toString()}`

    // 获取认证 token（如果需要）
    const cookies = document.cookie.split(';')
    let sessionId = ''
    cookies.forEach(cookie => {
      const [name, value] = cookie.trim().split('=')
      if (name === 'JSESSIONID' || name === 'sessionId') {
        sessionId = value
      }
    })

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Accept': 'text/event-stream',
        ...(sessionId ? { 'Cookie': `${sessionId.includes('=') ? '' : 'JSESSIONID='}${sessionId}` } : {})
      },
      credentials: 'include'
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    if (!reader) {
      throw new Error('无法读取响应流')
    }

    while (true) {
      const { done, value } = await reader.read()

      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        // SSE格式：data: 内容 或 data:内容
        if (line.startsWith('data:')) {
          // 提取 data: 后面的内容（支持 data: 和 data:内容两种格式）
          let data = line.slice(5) // 移除 'data:'
          // 如果第一个字符是空格，则移除
          if (data.length > 0 && data[0] === ' ') {
            data = data.slice(1)
          }

          // 跳过空数据和结束标记
          if (data && data !== '[DONE]') {
            // 确保消息存在，然后更新内容
            if (aiMessages.value[assistantIndex]) {
              // 使用数组索引访问并直接更新，Vue 3 的响应式系统会检测到
              const message = aiMessages.value[assistantIndex]
              // 直接赋值以确保响应式更新
              aiMessages.value[assistantIndex] = {
                ...message,
                content: message.content + data,
                loading: false
              }

              // 触发Vue更新和滚动（使用 requestAnimationFrame 确保渲染后再滚动）
              requestAnimationFrame(() => {
                nextTick(() => {
                  scrollToBottom()
                })
              })
            }
          }
        }
      }
    }

    // 处理剩余的 buffer
    if (buffer.trim()) {
      const lines = buffer.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          let data = line.slice(5)
          if (data.length > 0 && data[0] === ' ') {
            data = data.slice(1)
          }
          if (data && data !== '[DONE]') {
            if (aiMessages.value[assistantIndex]) {
              const message = aiMessages.value[assistantIndex]
              // 使用展开运算符创建新对象以确保响应式更新
              aiMessages.value[assistantIndex] = {
                ...message,
                content: message.content + data,
                loading: false
              }
              requestAnimationFrame(() => {
                nextTick(() => {
                  scrollToBottom()
                })
              })
            }
          }
        }
      }
    }

    // 确保 loading 状态已清除
    if (aiMessages.value[assistantIndex]) {
      aiMessages.value[assistantIndex].loading = false
    }
  } catch (error: any) {
    console.error('AI对话失败', error)
    message.error(error.message || 'AI对话失败，请稍后重试')

    // 移除占位消息，添加错误消息
    if (aiMessages.value[assistantIndex]) {
      aiMessages.value[assistantIndex].content = '抱歉，发生了错误，请稍后重试。'
      aiMessages.value[assistantIndex].loading = false
    }
  } finally {
    aiLoading.value = false
    scrollToBottom()
  }
}

// 新添加的函数：截断文本
function truncateText(text?: string, maxLength: number = 100): string {
  if (!text) return '-'
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

// 新添加的函数：格式化日期
function formatDate(dateString?: string): string {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return '-'
    return date.toLocaleDateString('zh-CN')
  } catch {
    return '-'
  }
}


onMounted(async () => {
  requestAnimationFrame(() => {
    pageReady.value = true
  })
  await loadMeta()
  await fetchList(1)
})
</script>

<style scoped>
/* 非遗文化主题样式 - 表格版本 */

#resourcesPage {
  min-height: calc(100vh - 200px);
  padding: 0;
  max-width: 100%;
  margin: 0 auto;
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.6s ease, transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
  will-change: opacity, transform;
  background: linear-gradient(180deg,
  #fcf9f4 0%,
  #f9f2e9 50%,
  #f5eadf 100%);
  position: relative;
}

/* 添加文化纹理背景 */
#resourcesPage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23d4a76a' fill-opacity='0.03' fill-rule='evenodd'/%3E%3C/svg%3E");
  opacity: 0.5;
  pointer-events: none;
  z-index: 0;
}

#resourcesPage.page-entered {
  opacity: 1;
  transform: translateY(0);
}

.section-anim {
  opacity: 0;
  transform: translateY(32px);
  transition: opacity 0.6s ease, transform 0.6s cubic-bezier(0.22, 1, 0.36, 1),
  filter 0.6s ease;
  will-change: opacity, transform;
}

.section-anim.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* 顶部Hero区域 - 非遗文化风格 */
.hero-search-wrap {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg,
  rgba(244, 237, 232, 0.98) 0%,
  rgba(251, 247, 240, 0.95) 100%),
  url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23d4a76a' fill-opacity='0.04' fill-rule='evenodd'/%3E%3C/svg%3E");
  border-radius: 24px;
  padding: 28px 32px 32px;
  margin: 24px 8% 32px;
  max-width: 1800px;
  box-shadow: 0 12px 40px rgba(139, 69, 19, 0.1),
  0 4px 12px rgba(212, 167, 106, 0.1),
  inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(212, 167, 106, 0.3);
  position: relative;
  z-index: 1;
}

/* 添加顶部装饰线条 */
.hero-search-wrap::before {
  content: '';
  position: absolute;
  top: 0;
  left: 20px;
  right: 20px;
  height: 3px;
  background: linear-gradient(90deg,
  #b8860b 0%,
  #d4a76a 25%,
  #a0522d 50%,
  #8b4513 75%,
  #654321 100%);
  z-index: 1;
}

/* 添加传统边框装饰 */
.hero-search-wrap::after {
  content: '';
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  bottom: 10px;
  border: 1px solid rgba(212, 167, 106, 0.2);
  border-radius: 16px;
  pointer-events: none;
  z-index: 0;
}

.page-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 24px;
  position: relative;
  z-index: 2;
}

.hero-text {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  position: relative;
  padding-left: 16px;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(164, 123, 67, 0.1);
  color: #8b4513;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 1px;
  width: fit-content;
  border: 1px solid rgba(164, 123, 67, 0.2);
  font-family: 'Noto Serif SC', serif;
  position: relative;
}

.hero-tag::before {
  content: '「';
  position: absolute;
  left: -12px;
  color: #d4a76a;
}

.hero-tag::after {
  content: '」';
  position: absolute;
  right: -12px;
  color: #d4a76a;
}

.hero-inline .page-title {
  margin: 0;
  font-size: 42px;
  line-height: 1.2;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #2c1810;
  font-family: 'Noto Serif SC', serif;
  position: relative;
  border-left: 8px solid #d4a76a;
  padding-left: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.05);
}

.page-subtitle {
  margin: 0;
  font-size: 16px;
  color: #5d4037;
  line-height: 1.6;
  font-family: 'Noto Sans SC', sans-serif;
  padding-left: 28px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.search-input-wrapper {
  display: flex;
  gap: 12px;
  flex: 1;
  min-width: 360px;
  max-width: 860px;
  align-items: center;
}

.search-input-container {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  background: #fffaf4;
  border: 2px solid rgba(212, 167, 106, 0.3);
  border-radius: 16px;
  padding: 0 16px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8),
  0 6px 20px rgba(139, 69, 19, 0.08);
  transition: all 0.3s ease;
}

/* 搜索下拉：热搜与历史 */
.search-dropdown {
  position: absolute;
  left: 12px;
  right: 12px;
  top: calc(100% + 8px);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(26, 35, 50, 0.12);
  border: 1px solid rgba(212, 167, 106, 0.12);
  padding: 12px;
  z-index: 50;
  max-height: 240px;
  overflow: auto;
}

.search-loading-inline {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 0;
}

.search-section {
  margin-bottom: 8px;
}
.search-section .section-title {
  font-size: 13px;
  color: #8b4513;
  font-weight: 600;
  margin-bottom: 8px;
}
.search-section .section-body {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.search-term {
  padding: 6px 10px;
  border-radius: 8px;
  background: #fffaf4;
  border: 1px solid rgba(212, 167, 106, 0.18);
  color: #5d4037;
  font-size: 13px;
  cursor: pointer;
}
.search-term.hot {
  background: linear-gradient(135deg, #f6e1c6, #f1d3a8);
  border-color: rgba(184, 134, 11, 0.18);
  color: #8b4513;
  font-weight: 600;
}
.search-term:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(139, 69, 19, 0.12);
}

.empty-term {
  color: #a1887f;
  font-size: 13px;
  padding: 6px 10px;
}

.search-input-container:focus-within {
  border-color: #8b4513;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8),
  0 8px 24px rgba(139, 69, 19, 0.12),
  0 0 0 3px rgba(139, 69, 19, 0.1);
}

.search-input {
  width: 100%;
  height: 52px;
  padding: 0 12px;
  padding-right: 40px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  outline: none;
  transition: all 0.2s ease;
  background: transparent;
  color: #2c1810;
  font-family: 'Noto Sans SC', sans-serif;
}

.search-input::placeholder {
  color: #a1887f;
}

.clear-btn {
  position: absolute;
  right: 16px;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(139, 69, 19, 0.1);
  color: #8b4513;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 1;
}

.clear-btn:hover {
  background: rgba(139, 69, 19, 0.15);
  color: #654321;
  transform: scale(1.1);
}

.clear-icon {
  font-size: 22px;
  line-height: 1;
  font-weight: 300;
}

.search-btn {
  width: 60px;
  height: 52px;
  border: 2px solid #d4a76a;
  border-radius: 16px;
  background: #f5e8d9;
  color: #8b4513;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(212, 167, 106, 0.2);
}

.search-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  background: #eeddc9;
  border-color: #b8860b;
  color: #654321;
  box-shadow: 0 6px 16px rgba(212, 167, 106, 0.3);
}

.search-quick-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* 上传按钮样式优化 */
.search-input-wrapper :deep(.publish-btn) {
  height: 52px;
  padding: 0 24px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #a0522d, #8b4513);
  border: 2px solid #8b4513;
  color: #fff;
  box-shadow: 0 6px 16px rgba(139, 69, 19, 0.25);
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-family: 'Noto Sans SC', sans-serif;
  position: relative;
  overflow: hidden;
}

.search-input-wrapper :deep(.publish-btn)::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.search-input-wrapper :deep(.publish-btn:hover) {
  background: linear-gradient(135deg, #8b4513, #654321);
  border-color: #654321;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.35);
}

.search-input-wrapper :deep(.publish-btn:hover)::before {
  left: 100%;
}

.search-input-wrapper :deep(.publish-btn:active) {
  transform: translateY(0);
  background: linear-gradient(135deg, #654321, #4a2f1a);
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.25);
}

.ai-chat-btn {
  height: 52px;
  padding: 0 24px;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 16px;
  background: linear-gradient(135deg, #d4a76a, #b8860b);
  border: 2px solid #d4a76a;
  color: #fff;
  box-shadow: 0 6px 16px rgba(212, 167, 106, 0.25);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  cursor: pointer;
  font-family: 'Noto Sans SC', sans-serif;
}

.ai-chat-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.ai-chat-btn:hover::before {
  left: 100%;
}

.ai-chat-btn:hover {
  transform: translateY(-2px);
  background: linear-gradient(135deg, #b8860b, #a0522d);
  border-color: #b8860b;
  color: #fff;
  box-shadow: 0 8px 20px rgba(212, 167, 106, 0.35);
}

.ai-chat-btn:active {
  transform: translateY(0);
  background: linear-gradient(135deg, #a0522d, #8b4513);
  box-shadow: 0 4px 12px rgba(212, 167, 106, 0.25);
}

.ai-chat-btn .btn-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  transition: transform 0.3s ease;
  position: relative;
  z-index: 1;
}

.ai-chat-btn:hover .btn-icon-wrapper {
  transform: rotate(15deg) scale(1.1);
}

.ai-chat-btn .btn-text {
  letter-spacing: 0.5px;
  position: relative;
  z-index: 1;
}

/* 筛选区域优化 */
.filters-bar {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
  padding-top: 16px;
  border-top: 1px solid rgba(212, 167, 106, 0.2);
  position: relative;
  z-index: 2;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 0 0 240px;
  min-width: 200px;
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  color: #5d4037;
  font-family: 'Noto Sans SC', sans-serif;
}

.filter-select {
  height: 44px;
  padding: 0 16px;
  border: 2px solid rgba(212, 167, 106, 0.3);
  border-radius: 12px;
  font-size: 14px;
  outline: none;
  background: #fffaf4;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #2c1810;
  font-family: 'Noto Sans SC', sans-serif;
  box-shadow: 0 4px 8px rgba(139, 69, 19, 0.05);
}

.filter-select:focus {
  border-color: #8b4513;
  background: #fffdf9;
  box-shadow: 0 0 0 3px rgba(139, 69, 19, 0.1);
}

.reset-btn {
  height: 44px;
  padding: 0 24px;
  border: 2px solid rgba(164, 123, 67, 0.4);
  border-radius: 12px;
  background: rgba(164, 123, 67, 0.1);
  color: #8b4513;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  align-self: flex-end;
  font-family: 'Noto Sans SC', sans-serif;
  box-shadow: 0 4px 8px rgba(139, 69, 19, 0.05);
}

.reset-btn:hover {
  background: rgba(164, 123, 67, 0.15);
  border-color: #8b4513;
  color: #654321;
  transform: translateY(-1px);
  box-shadow: 0 6px 12px rgba(139, 69, 19, 0.08);
}

.reset-btn:active {
  transform: translateY(0);
  background: rgba(164, 123, 67, 0.2);
}

/* 资源展示区域优化 */
.resources-section {
  min-height: 400px;
  padding: 0 8% 48px;
  position: relative;
  z-index: 1;
}

.resources-container {
  max-width: 1800px;
  margin: 0 auto;
  width: 100%;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120px 20px;
  color: #8b4513;
  background: rgba(255, 253, 249, 0.9);
  border-radius: 20px;
  border: 2px dashed rgba(212, 167, 106, 0.4);
  margin: 20px 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120px 20px;
  color: #8b4513;
  background: rgba(255, 253, 249, 0.9);
  border-radius: 20px;
  border: 2px dashed rgba(212, 167, 106, 0.4);
  margin: 20px 0;
}

.empty-icon {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.empty-text {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #2c1810;
  font-family: 'Noto Serif SC', serif;
}

.empty-hint {
  font-size: 15px;
  color: #5d4037;
  font-family: 'Noto Sans SC', sans-serif;
  margin-bottom: 16px;
}

.empty-action-btn {
  background: linear-gradient(135deg, #a0522d, #8b4513);
  border: none;
  color: #fff;
  font-family: 'Noto Sans SC', sans-serif;
  border-radius: 12px;
  padding: 8px 20px;
  transition: all 0.3s ease;
}

.empty-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.25);
}

/* 列表容器 */
.resources-list-container {
  margin-bottom: 32px;
}

.resources-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 列表项 */
.resource-item {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  padding: 24px;
  background: #fffaf4;
  border-radius: 16px;
  border: 2px solid rgba(212, 167, 106, 0.2);
  box-shadow: 0 4px 16px rgba(139, 69, 19, 0.08),
  0 2px 8px rgba(212, 167, 106, 0.1);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.resource-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, #d4a76a, #b8860b);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.resource-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(139, 69, 19, 0.12),
  0 4px 12px rgba(212, 167, 106, 0.15);
  border-color: rgba(212, 167, 106, 0.4);
  background: #fffdf9;
}

.resource-item:hover::before {
  opacity: 1;
}

/* 图片区域 */
.resource-item-image {
  flex-shrink: 0;
}

.image-wrapper {
  width: 180px;
  height: 180px;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #f8eddc, #f5e6d0);
  border: 2px solid rgba(212, 167, 106, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.1);
}

.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.resource-item:hover .image-wrapper img {
  transform: scale(1.05);
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a1887f;
  font-size: 48px;
  width: 100%;
  height: 100%;
}

/* 内容区域 */
.resource-item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resource-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resource-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 22px;
  font-weight: 700;
  color: #2c1810;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  line-height: 1.4;
}

.commercial-indicator {
  color: #b8860b;
  font-size: 14px;
  background: rgba(184, 134, 11, 0.15);
  padding: 4px 8px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid rgba(184, 134, 11, 0.3);
}

.resource-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-family: 'Noto Sans SC', sans-serif;
}

.category-badge {
  background: rgba(212, 167, 106, 0.15);
  color: #8b4513;
  padding: 6px 14px;
  border-radius: 8px;
  font-weight: 500;
  border: 1px solid rgba(212, 167, 106, 0.3);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.region-badge {
  background: rgba(139, 69, 19, 0.1);
  color: #5d4037;
  padding: 6px 14px;
  border-radius: 8px;
  font-weight: 500;
  border: 1px solid rgba(139, 69, 19, 0.2);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.price-badge {
  background: rgba(184, 134, 11, 0.15);
  color: #b8860b;
  padding: 6px 14px;
  border-radius: 8px;
  font-weight: 600;
  border: 1px solid rgba(184, 134, 11, 0.3);
  font-size: 15px;
}

.resource-intro {
  font-family: 'Noto Sans SC', sans-serif;
  font-size: 15px;
  color: #5d4037;
  line-height: 1.7;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.resource-footer {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: auto;
}

.tags-section {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tags-icon {
  color: #8b4513;
  font-size: 16px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tag-chip {
  padding: 4px 12px;
  background: rgba(212, 167, 106, 0.1);
  color: #8b4513;
  border-radius: 12px;
  font-size: 13px;
  white-space: nowrap;
  border: 1px solid rgba(212, 167, 106, 0.2);
  font-family: 'Noto Sans SC', sans-serif;
}

.tag-more {
  color: #a1887f;
  font-size: 13px;
  font-style: italic;
}

.resource-author {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #5d4037;
  font-family: 'Noto Sans SC', sans-serif;
}

.resource-author .separator {
  color: #a1887f;
  margin: 0 4px;
}

/* 操作区域 */
.resource-item-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;
  align-items: flex-end;
  justify-content: flex-start;
}

.view-btn {
  min-width: 120px;
  height: 44px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 15px;
  font-family: 'Noto Sans SC', sans-serif;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #d4a76a, #b8860b);
  border: 2px solid #d4a76a;
  color: #fff;
  box-shadow: 0 6px 16px rgba(212, 167, 106, 0.25);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.view-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.view-btn:hover {
  transform: translateY(-2px);
  background: linear-gradient(135deg, #b8860b, #a0522d);
  border-color: #b8860b;
  color: #fff;
  box-shadow: 0 8px 20px rgba(212, 167, 106, 0.35);
}

.view-btn:hover::before {
  left: 100%;
}

.view-btn:active {
  transform: translateY(0);
  background: linear-gradient(135deg, #a0522d, #8b4513);
  box-shadow: 0 4px 12px rgba(212, 167, 106, 0.25);
}

/* 列表项动画 */
.resource-item-enter-active,
.resource-item-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.resource-item-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.resource-item-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 40px 0 20px;
}

.pagination-wrapper :deep(.ant-pagination) {
  font-family: 'Noto Sans SC', sans-serif;
}

.pagination-wrapper :deep(.ant-pagination-item) {
  border: 2px solid rgba(212, 167, 106, 0.3);
  border-radius: 12px;
  background: #fffaf4;
  margin: 0 4px;
  transition: all 0.3s ease;
}

.pagination-wrapper :deep(.ant-pagination-item a) {
  color: #8b4513;
  padding: 0 8px;
  font-weight: 500;
}

.pagination-wrapper :deep(.ant-pagination-item:hover) {
  border-color: #d4a76a;
  background: #f5e8d9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(212, 167, 106, 0.2);
}

.pagination-wrapper :deep(.ant-pagination-item-active) {
  background: linear-gradient(135deg, #a0522d, #8b4513);
  border-color: #8b4513;
}

.pagination-wrapper :deep(.ant-pagination-item-active a) {
  color: #fff;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .resources-section {
    padding: 0 6% 48px;
  }

  .hero-search-wrap {
    margin: 24px 6% 32px;
  }

  .resource-item {
    gap: 20px;
    padding: 20px;
  }

  .image-wrapper {
    width: 160px;
    height: 160px;
  }

  .resource-title {
    font-size: 20px;
  }
}

@media (max-width: 992px) {
  .resource-item {
    flex-direction: column;
    align-items: stretch;
  }

  .resource-item-image {
    align-self: center;
  }

  .image-wrapper {
    width: 200px;
    height: 200px;
  }

  .resource-item-actions {
    flex-direction: row;
    justify-content: flex-end;
    align-items: center;
  }

  .view-btn {
    min-width: 100px;
  }
}

@media (max-width: 768px) {
  .hero-search-wrap {
    padding: 24px;
    margin: 16px 4% 24px;
    border-radius: 20px;
  }

  .resources-section {
    padding: 0 4% 32px;
  }

  .resource-item {
    padding: 16px;
    gap: 16px;
  }

  .image-wrapper {
    width: 100%;
    max-width: 300px;
    height: 200px;
  }

  .resource-title {
    font-size: 18px;
  }

  .resource-intro {
    font-size: 14px;
    -webkit-line-clamp: 3;
    line-clamp: 3;
  }

  .resource-meta-row {
    gap: 8px;
  }

  .meta-item {
    font-size: 13px;
  }

  .category-badge,
  .region-badge,
  .price-badge {
    padding: 5px 10px;
    font-size: 13px;
  }

  .resource-item-actions {
    flex-direction: column;
    width: 100%;
  }

  .view-btn {
    width: 100%;
    min-width: auto;
  }
}

@media (max-width: 576px) {
  .resource-item {
    padding: 12px;
    gap: 12px;
  }

  .image-wrapper {
    height: 160px;
  }

  .resource-title {
    font-size: 16px;
  }

  .resource-intro {
    font-size: 13px;
  }

  .tags-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .tags-list {
    width: 100%;
  }
}

/* AI问答框样式保持原样 */
.ai-chat-container {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 1000;
  width: 480px;
  max-width: calc(100vw - 48px);
}

.ai-chat-box {
  background: linear-gradient(180deg, #fdf7f2 0%, #f9f1ea 100%);
  border-radius: 24px;
  box-shadow: 0 16px 40px rgba(139, 69, 19, 0.2),
  0 8px 24px rgba(212, 167, 106, 0.25);
  display: flex;
  flex-direction: column;
  height: 620px;
  max-height: calc(100vh - 100px);
  overflow: hidden;
  animation: ai-chat-pop-in 0.4s cubic-bezier(0.22, 1, 0.36, 1);
  border: 2px solid rgba(212, 167, 106, 0.3);
  position: relative;
}

.ai-chat-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #d4a76a, #b8860b);
  z-index: 2;
}

@keyframes ai-chat-pop-in {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.ai-chat-slide-enter-active,
.ai-chat-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.ai-chat-slide-enter-from,
.ai-chat-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.ai-chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: #f3dfcf;
  color: #3d2b1f;
  border-bottom: 2px solid #e5cfbd;
  font-family: 'Noto Serif SC', serif;
}

.ai-chat-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
}

.ai-chat-close {
  width: 36px;
  height: 36px;
  border: 2px solid #e6d4c4;
  background: #faf1e8;
  border-radius: 12px;
  color: #5a4333;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 16px;
}

.ai-chat-close:hover {
  background: #f1e3d6;
  border-color: #d8c2b1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.ai-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: #fefbf7;
}

.ai-chat-messages::-webkit-scrollbar {
  width: 8px;
}

.ai-chat-messages::-webkit-scrollbar-track {
  background: rgba(212, 167, 106, 0.1);
  border-radius: 4px;
}

.ai-chat-messages::-webkit-scrollbar-thumb {
  background: rgba(139, 69, 19, 0.3);
  border-radius: 4px;
}

.ai-chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(139, 69, 19, 0.4);
}

.ai-chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  text-align: center;
  color: #8b4513;
  padding: 40px 20px;
}

.ai-chat-empty p {
  margin: 12px 0;
  font-size: 15px;
  line-height: 1.7;
  font-family: 'Noto Sans SC', sans-serif;
}

.ai-chat-hint {
  color: #a1887f;
  font-size: 14px;
  font-style: italic;
}

.ai-message {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 85%;
  animation: ai-message-fade-in 0.3s ease;
}

@keyframes ai-message-fade-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-message.user {
  align-self: flex-end;
}

.ai-message.assistant {
  align-self: flex-start;
}

.ai-message-content {
  padding: 16px 20px;
  border-radius: 20px;
  font-size: 15px;
  line-height: 1.7;
  word-wrap: break-word;
  white-space: pre-wrap;
  font-family: 'Noto Sans SC', sans-serif;
  position: relative;
}

.ai-message.user .ai-message-content {
  background: linear-gradient(135deg, #f8c78a, #f3a35c);
  color: #42230f;
  border-bottom-right-radius: 6px;
  border: 2px solid #e5a854;
  box-shadow: 0 6px 16px rgba(243, 163, 92, 0.3);
}

.ai-message.assistant .ai-message-content {
  background: #fffaf4;
  color: #3d2b1f;
  border: 2px solid #efdcc7;
  border-bottom-left-radius: 6px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.ai-message-loading {
  padding: 8px 0;
  display: flex;
  align-items: center;
}

.ai-message-placeholder {
  color: #a1887f;
  font-style: italic;
  font-family: 'Noto Sans SC', sans-serif;
}

.ai-message-link {
  color: #8b4513;
  text-decoration: none;
  border-bottom: 2px solid rgba(139, 69, 19, 0.4);
  transition: all 0.2s ease;
  cursor: pointer;
  font-weight: 500;
  padding: 2px 0;
}

.ai-message-link:hover {
  color: #654321;
  border-bottom-color: #654321;
  background: rgba(139, 69, 19, 0.1);
  padding: 2px 4px;
  margin: 0 -4px;
  border-radius: 4px;
}

.ai-message.user .ai-message-link {
  color: rgba(255, 255, 255, 0.95);
  border-bottom-color: rgba(255, 255, 255, 0.6);
}

.ai-message.user .ai-message-link:hover {
  color: #fff;
  border-bottom-color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.15);
}

.ai-chat-input-wrapper {
  display: flex;
  gap: 16px;
  padding: 20px 24px;
  background: #fbeee2;
  border-top: 2px solid #f0dcc8;
}

.ai-chat-input {
  flex: 1;
  height: 52px;
  padding: 0 18px;
  border: 2px solid #e7d5c5;
  border-radius: 16px;
  font-size: 15px;
  outline: none;
  transition: all 0.3s ease;
  background: #fffaf4;
  color: #3d2b1f;
  font-family: 'Noto Sans SC', sans-serif;
}

.ai-chat-input:focus {
  border-color: #f1b883;
  background: #fffdf9;
  box-shadow: 0 0 0 4px rgba(241, 184, 131, 0.3);
}

.ai-chat-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #f7f0ea;
}

.ai-chat-send {
  width: 52px;
  height: 52px;
  border: 2px solid #e4c7af;
  border-radius: 16px;
  background: linear-gradient(135deg, #f6c48a, #eea868);
  color: #3f2b1d;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
  box-shadow: 0 8px 20px rgba(238, 168, 104, 0.3);
}

.ai-chat-send:hover:not(:disabled) {
  background: linear-gradient(135deg, #f3b774, #e99a56);
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(233, 154, 86, 0.35);
}

.ai-chat-send:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 6px 16px rgba(233, 154, 86, 0.25);
}

.ai-chat-send:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  filter: grayscale(0.2);
}

@media (max-width: 768px) {
  .search-bar {
    gap: 12px;
  }

  .ai-chat-btn {
    width: 100%;
    justify-content: center;
  }

  .ai-chat-container {
    width: calc(100vw - 32px);
    right: 16px;
    bottom: 16px;
  }

  .ai-chat-box {
    height: 500px;
    max-height: calc(100vh - 80px);
  }
}

/* 添加字体引入 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;500;600;700;800&family=Noto+Sans+SC:wght@300;400;500;600;700&display=swap');
</style>
