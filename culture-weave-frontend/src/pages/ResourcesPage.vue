<template>
    <div id="resourcesPage" :class="{ 'page-entered': pageReady }">
        <!-- 页面主标题区块 -->
        <section class="page-hero-wrap section-anim" :class="{ 'is-visible': pageReady }">
            <header class="page-hero">
                <h1 class="page-title">非遗资源库</h1>
                <p class="page-subtitle">探索来自全国各地的非物质文化遗产资源</p>
            </header>
        </section>

        <!-- 搜索和筛选区域 -->
        <section
            :class="['search-section', 'section-anim', { 'search-loading': showTopBarLoading, 'is-visible': pageReady }]">
            <div v-if="showTopBarLoading" class="search-loading-overlay" aria-hidden="true"></div>
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
                    </div>
                    <button :class="['search-btn', { searching: loading, 'loading-glimmer': showTopBarLoading }]"
                        :disabled="loading" @click="handleSearch" title="搜索">
                        <a-spin v-if="loading" :spinning="true" size="small" />
                        <SearchOutlined v-else />
                    </button>
                </div>
                <!-- AI问非遗按钮 -->
                <div class="ai-chat-btn-wrapper">
                    <button class="ai-chat-btn" @click="toggleAiChat" title="AI问非遗">
                        <span class="btn-icon-wrapper">
                            <RobotOutlined class="btn-icon" />
                        </span>
                        <span class="btn-text">AI问非遗</span>
                    </button>
                </div>
                <!-- 添加非遗资源按钮 -->
                <div class="publish-btn-wrapper">
                    <a-button type="primary" size="large" @click="onPublish" class="publish-btn">
                        <span class="btn-icon-wrapper">
                            <PlusOutlined class="btn-icon" />
                        </span>
                        <span class="btn-text">添加非遗资源</span>
                    </a-button>
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

                <!-- 分类筛选 -->
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

        <!-- 资源卡片展示区域 -->
        <section :class="['resources-section', 'section-anim', { 'is-visible': pageReady }]">
            <div class="resources-container">
                <div v-if="loading" class="loading-state">
                    <a-spin size="large" />
                    <p>加载中...</p>
                </div>

                <div v-else-if="list.length === 0" class="empty-state">
                    <div class="empty-icon">
                        <PictureOutlined :style="{ fontSize: '64px', color: '#d3d9e6' }" />
                    </div>
                    <p class="empty-text">暂无资源数据</p>
                    <p class="empty-hint">试试调整筛选条件或搜索关键词</p>
                </div>

                <div v-else :class="['resources-grid', { 'cards-ready': cardsReady }]">
                    <div v-for="(item, index) in list" :key="item.id" class="resource-card"
                        :style="cardAnimationStyle(index)" @click="goToDetail(item.id)">
                        <div class="card-image-wrapper">
                            <!-- {{ item.id }} -->
                            <img v-if="item.resourceImgUrl" :src="item.resourceImgUrl" :alt="item.name || '资源封面'"
                                class="card-image" loading="lazy" @error="onImageError($event)" />
                            <div v-else class="card-image-placeholder">
                                <PictureOutlined />
                                <span>暂无图片</span>
                            </div>
                            <div v-if="item.price && item.price > 0" class="commercial-badge">
                                <DollarOutlined />
                                <span>可商用</span>
                            </div>
                        </div>

                        <div class="card-content">
                            <h3 class="card-title">{{ item.name || '未命名资源' }}</h3>
                            <p class="card-intro">{{ item.introduction || '暂无简介' }}</p>

                            <div class="card-meta">
                                <div v-if="item.region" class="meta-item">
                                    <EnvironmentOutlined />
                                    <span>{{ item.region }}</span>
                                </div>
                                <div v-if="item.category" class="meta-item">
                                    <FolderOutlined />
                                    <span>{{ item.category }}</span>
                                </div>
                            </div>

                            <div v-if="normalizeTags(item.tags).length > 0" class="card-tags">
                                <TagOutlined />
                                <div class="tags-list">
                                    <span v-for="tag in normalizeTags(item.tags)" :key="tag" class="tag-chip">
                                        {{ tag }}
                                    </span>
                                </div>
                            </div>

                            <div class="card-footer">
                                <div class="card-author">
                                    <UserOutlined />
                                    <span>{{ item.userName || '匿名' }}</span>
                                </div>
                                <div v-if="item.price && item.price > 0" class="card-price">
                                    {{ formatPrice(item.price) }}
                                </div>
                            </div>
                        </div>
                    </div>
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
    SendOutlined
} from '@ant-design/icons-vue'
import {
    listResourcesVoByPage,
    getResourceCategoryList
} from '../api/resourcesController'
import { useLoginUserStore } from '../stores/useLoginUserStore'
import myAxios from '../request'

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
const cardsReady = ref(false)

// 获取当前登录用户ID
const currentUserId = computed(() => loginUserStore.loginUser?.id || null)

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

// AI问答相关状态
const showAiChat = ref(false)
const aiInputMessage = ref('')
const aiMessages = ref<Array<{ role: 'user' | 'assistant'; content: string; loading?: boolean }>>([])
const aiLoading = ref(false)
const currentChatId = ref(`chat_${Date.now()}`)
const messagesContainer = ref<HTMLElement | null>(null)

watch(loading, async newVal => {
    if (newVal) {
        cardsReady.value = false
        return
    }

    await nextTick()
    requestAnimationFrame(() => {
        cardsReady.value = true
    })
})

function cardAnimationStyle(index: number) {
    return {
        '--card-stagger': String(index)
    } as Record<string, string>
}

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
}

function onSearchBlur() {
    // 延迟一下，让清除按钮的点击事件能够触发
    setTimeout(() => {
        searchFocused.value = false
    }, 200)
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
        if (placeholder && placeholder.classList.contains('card-image-placeholder')) {
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
        // 跳转到登录页，并保存当前页面路径，登录后可以返回
        router.push({
            path: '/user/login',
            query: {
                redirect: route.fullPath
            }
        })
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
        // 跳转到登录页，并保存当前页面路径，登录后可以返回
        router.push({
            path: '/user/login',
            query: {
                redirect: route.fullPath
            }
        })
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
            "'": '&#039;'
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

onMounted(async () => {
    requestAnimationFrame(() => {
        pageReady.value = true
    })
    await loadMeta()
    await fetchList(1)
})
</script>

<style scoped>
/* （样式与原来一致 — 省略复述，保持不变） */
#resourcesPage {
    min-height: calc(100vh - 200px);
    padding: 0;
    max-width: 100%;
    margin: 0 auto;
    opacity: 0;
    transform: translateY(18px);
    transition: opacity 0.6s ease, transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
    will-change: opacity, transform;
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

.section-anim.is-visible.search-section {
    filter: drop-shadow(0 18px 38px rgba(37, 72, 177, 0.12));
}

/* 搜索区域 */
.search-section {
    position: relative;
    overflow: hidden;
    background: linear-gradient(135deg, rgba(61, 107, 255, 0.05), rgba(132, 91, 255, 0.05));
    border-radius: 20px;
    padding: 32px;
    margin-bottom: 32px;
    margin-left: 8%;
    margin-right: 8%;
    max-width: 1800px;
    box-shadow: 0 8px 24px rgba(15, 35, 95, 0.06);
}

.page-hero-wrap {
    margin: 0 8% 12px;
    max-width: 1800px;
}

.page-hero {
    text-align: center;
    margin-bottom: 8px;
}

.page-title {
    margin: 0;
    font-size: 36px;
    line-height: 1.3;
    font-weight: 700;
    letter-spacing: 0.5px;
    color: #1a2332;
}

.page-subtitle {
    margin: 10px 0 0 0;
    font-size: 16px;
    color: #4f5b76;
}

.search-bar {
    margin-bottom: 24px;
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
    min-width: 300px;
    max-width: 800px;
}

.ai-chat-btn-wrapper {
    display: flex;
    align-items: center;
    margin-left: 12px;
}

.ai-chat-btn {
    height: 52px;
    padding: 0 24px;
    font-size: 16px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 10px;
    border-radius: 26px;
    background: linear-gradient(135deg, #845bff, #3d6bff);
    border: none;
    box-shadow: 0 4px 16px rgba(132, 91, 255, 0.35);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;
    color: #fff;
    cursor: pointer;
}

.ai-chat-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s ease;
}

.ai-chat-btn:hover::before {
    left: 100%;
}

.ai-chat-btn:hover {
    transform: translateY(-2px) scale(1.02);
    box-shadow: 0 8px 24px rgba(132, 91, 255, 0.45);
    background: linear-gradient(135deg, #7445e6, #2d5ae6);
}

.ai-chat-btn:active {
    transform: translateY(0) scale(0.98);
    box-shadow: 0 2px 8px rgba(132, 91, 255, 0.3);
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

.ai-chat-btn .btn-icon {
    font-size: 18px;
    transition: transform 0.3s ease;
}

.ai-chat-btn .btn-text {
    letter-spacing: 0.5px;
    position: relative;
    z-index: 1;
}

.search-input-container {
    position: relative;
    flex: 1;
    display: flex;
    align-items: center;
}

.publish-btn-wrapper {
    display: flex;
    align-items: center;
}

.publish-btn {
    height: 52px;
    padding: 0 28px;
    font-size: 16px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 10px;
    border-radius: 26px;
    background: linear-gradient(135deg, #ff6b6b, #ff8e53);
    border: none;
    box-shadow: 0 4px 16px rgba(255, 107, 107, 0.35);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;
}

.publish-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s ease;
}

.publish-btn:hover::before {
    left: 100%;
}

.publish-btn:hover {
    transform: translateY(-2px) scale(1.02);
    box-shadow: 0 8px 24px rgba(255, 107, 107, 0.45);
    background: linear-gradient(135deg, #ff5252, #ff7a3d);
}

.publish-btn:active {
    transform: translateY(0) scale(0.98);
    box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}

.publish-btn .btn-icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    transition: transform 0.3s ease;
}

.publish-btn:hover .btn-icon-wrapper {
    transform: rotate(90deg) scale(1.1);
}

.publish-btn .btn-icon {
    font-size: 18px;
    transition: transform 0.3s ease;
}

.publish-btn .btn-text {
    letter-spacing: 0.5px;
    position: relative;
    z-index: 1;
}

/* 确保按钮文字颜色为白色，覆盖 Ant Design 默认样式 */
:deep(.publish-btn),
:deep(.publish-btn:hover),
:deep(.publish-btn:focus),
:deep(.publish-btn:active) {
    color: #fff !important;
    background: linear-gradient(135deg, #ff6b6b, #ff8e53) !important;
    border: none !important;
}

:deep(.publish-btn:hover) {
    background: linear-gradient(135deg, #ff5252, #ff7a3d) !important;
}

:deep(.publish-btn:focus) {
    outline: none;
    box-shadow: 0 4px 16px rgba(255, 107, 107, 0.35), 0 0 0 3px rgba(255, 107, 107, 0.2) !important;
}

.search-input {
    width: 100%;
    height: 52px;
    padding: 0 20px;
    padding-right: 40px;
    border: 2px solid rgba(61, 107, 255, 0.2);
    border-radius: 26px;
    font-size: 16px;
    outline: none;
    transition: all 0.3s ease;
    background: #fff;
}

.search-input:focus {
    border-color: #3d6bff;
    box-shadow: 0 0 0 4px rgba(61, 107, 255, 0.1);
}

.clear-btn {
    position: absolute;
    right: 12px;
    width: 28px;
    height: 28px;
    border: none;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.06);
    color: #6a7a99;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    z-index: 1;
}

.clear-btn:hover {
    background: rgba(0, 0, 0, 0.1);
    color: #1a2332;
    transform: scale(1.1);
}

.clear-icon {
    font-size: 20px;
    line-height: 1;
    font-weight: 300;
}

.search-btn {
    width: 52px;
    height: 52px;
    border: none;
    border-radius: 26px;
    background: linear-gradient(120deg, #3d6bff, #845bff);
    color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(61, 107, 255, 0.3);
    flex-shrink: 0;
}

.search-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(61, 107, 255, 0.4);
    background: linear-gradient(120deg, #2d5ae6, #7445e6);
}

.search-btn:active:not(:disabled) {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(61, 107, 255, 0.3);
}

.search-btn:disabled {
    cursor: not-allowed;
    opacity: 0.7;
}

.search-btn.searching {
    animation: pulse 1.5s ease-in-out infinite;
}

.search-section .search-loading-overlay {
    position: absolute;
    inset: 0;
    pointer-events: none;
    background: linear-gradient(120deg, rgba(61, 107, 255, 0), rgba(61, 107, 255, 0.12), rgba(132, 91, 255, 0));
    background-size: 200% 100%;
    opacity: 0;
    transition: opacity 0.3s ease;
    z-index: 0;
}

.search-section.search-loading .search-loading-overlay {
    opacity: 1;
    animation: shimmer-shift 2.4s linear infinite;
}

.loading-glimmer {
    position: relative;
    overflow: hidden;
}

.loading-glimmer::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(120deg, rgba(255, 255, 255, 0), rgba(255, 255, 255, 0.55), rgba(255, 255, 255, 0));
    background-size: 200% 100%;
    animation: shimmer-shift 1.8s linear infinite;
    opacity: 0;
    pointer-events: none;
    z-index: 2;
}

.search-section.search-loading .loading-glimmer::after {
    opacity: 1;
}

.loading-glimmer>* {
    position: relative;
    z-index: 1;
}

.search-section.search-loading .clear-btn {
    z-index: 3;
}

@keyframes shimmer-shift {
    0% {
        background-position: -200% 0;
    }

    100% {
        background-position: 200% 0;
    }
}

@keyframes pulse {

    0%,
    100% {
        box-shadow: 0 4px 12px rgba(61, 107, 255, 0.3);
    }

    50% {
        box-shadow: 0 4px 20px rgba(61, 107, 255, 0.5);
    }
}

/* 筛选区域 */
.filters-bar {
    display: flex;
    gap: 20px;
    align-items: flex-end;
    flex-wrap: wrap;
}

.filter-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex: 1;
    min-width: 200px;
}

.filter-label {
    font-size: 14px;
    font-weight: 500;
    color: #4f5b76;
}

.filter-select {
    height: 40px;
    padding: 0 16px;
    border: 1px solid rgba(61, 107, 255, 0.2);
    border-radius: 12px;
    font-size: 14px;
    outline: none;
    background: #fff;
    cursor: pointer;
    transition: all 0.2s ease;
}

.filter-select:focus {
    border-color: #3d6bff;
    box-shadow: 0 0 0 3px rgba(61, 107, 255, 0.1);
}

.tags-input-wrapper {
    position: relative;
}

.tags-input {
    width: 100%;
    height: 40px;
    padding: 0 16px;
    border: 1px solid rgba(61, 107, 255, 0.2);
    border-radius: 12px;
    font-size: 14px;
    outline: none;
    background: #fff;
    transition: all 0.2s ease;
}

.tags-input:focus {
    border-color: #3d6bff;
    box-shadow: 0 0 0 3px rgba(61, 107, 255, 0.1);
}

.reset-btn {
    height: 40px;
    padding: 0 24px;
    border: 1px solid rgba(61, 107, 255, 0.3);
    border-radius: 12px;
    background: #fff;
    color: #3d6bff;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.reset-btn:hover {
    background: rgba(61, 107, 255, 0.05);
    border-color: #3d6bff;
}

/* 资源展示区域 */
.resources-section {
    min-height: 400px;
    padding: 0 8% 32px;
}

.resources-container {
    max-width: 1800px;
    margin: 0 auto;
    width: 100%;
}

.loading-state,
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    color: #6a7a99;
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
    font-size: 18px;
    font-weight: 500;
    margin-bottom: 8px;
}

.empty-hint {
    font-size: 14px;
    color: #9aa4b8;
}

.resources-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
    margin-bottom: 40px;
    contain: layout paint;
}

.resource-card {
    background: #fff;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 16px rgba(15, 35, 95, 0.08);
    transition: transform 0.5s cubic-bezier(0.22, 1, 0.36, 1),
        opacity 0.5s ease, box-shadow 0.3s ease;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    opacity: 0;
    transform: translate3d(0, 28px, 0) scale(0.96);
    transition-delay: calc(var(--card-stagger, 0) * 80ms);
    will-change: transform, opacity;
}

.resources-grid.cards-ready .resource-card {
    opacity: 1;
    transform: translate3d(0, 0, 0) scale(1);
}

.resource-card:hover {
    transform: translate3d(0, -4px, 0);
    box-shadow: 0 12px 32px rgba(15, 35, 95, 0.12);
}

.card-image-wrapper {
    position: relative;
    width: 100%;
    height: 220px;
    overflow: hidden;
    background: linear-gradient(135deg, #f5f7fb, #e8ecf4);
}

.card-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
}

.resource-card:hover .card-image {
    transform: scale(1.05);
}

.card-image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #9aa4b8;
    font-size: 14px;
    gap: 8px;
}

.commercial-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    background: linear-gradient(120deg, #ff9a5e, #ff6b95);
    color: #fff;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(255, 107, 149, 0.4);
}

.card-content {
    flex: 1;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.card-title {
    font-size: 18px;
    font-weight: 600;
    color: #1a2332;
    margin: 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.card-intro {
    font-size: 14px;
    color: #6a7a99;
    line-height: 1.6;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    flex: 1;
}

.card-meta {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
}

.meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #6a7a99;
}

.card-tags {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    font-size: 13px;
    color: #6a7a99;
}

.tags-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}

.tag-chip {
    padding: 4px 10px;
    background: rgba(61, 107, 255, 0.1);
    color: #3d6bff;
    border-radius: 12px;
    font-size: 12px;
    white-space: nowrap;
}

.card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid #f0f2f5;
    margin-top: auto;
}

.card-author {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #6a7a99;
}

.card-price {
    font-size: 16px;
    font-weight: 600;
    color: #ff6b95;
}

/* 分页 */
.pagination-wrapper {
    display: flex;
    justify-content: center;
    padding: 32px 0;
}

/* 响应式 */
@media (max-width: 1400px) {
    .resources-grid {
        grid-template-columns: repeat(3, 1fr);
        gap: 20px;
    }
}

@media (max-width: 1024px) {
    .resources-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 20px;
    }
}

@media (max-width: 768px) {
    .resources-section {
        padding: 0 8% 24px;
    }

    .search-section {
        padding: 24px 8%;
    }

    .search-bar {
        flex-direction: column;
        align-items: stretch;
    }

    .search-input-wrapper {
        flex-direction: column;
    }

    .search-input-container {
        width: 100%;
    }

    .search-input {
        width: 100%;
    }

    .search-btn {
        width: 100%;
    }

    .publish-btn-wrapper {
        width: 100%;
    }

    .publish-btn {
        width: 100%;
        justify-content: center;
        padding: 0 24px;
    }

    .publish-btn .btn-icon-wrapper {
        width: 22px;
        height: 22px;
    }

    .publish-btn .btn-icon {
        font-size: 16px;
    }

    .filters-bar {
        flex-direction: column;
        align-items: stretch;
    }

    .filter-group {
        min-width: 100%;
    }

    .resources-grid {
        grid-template-columns: 1fr;
        gap: 16px;
    }

    .card-image-wrapper {
        height: 180px;
    }
}

@media (max-width: 480px) {
    .search-section {
        padding: 20px 8%;
        margin-left: 8%;
        margin-right: 8%;
        border-radius: 16px;
    }

    .resources-grid {
        gap: 12px;
    }

    .card-content {
        padding: 16px;
    }
}

/* AI问答框样式 */
.ai-chat-container {
    position: fixed;
    bottom: 24px;
    right: 24px;
    z-index: 1000;
    width: 420px;
    max-width: calc(100vw - 48px);
}

.ai-chat-box {
    background: #fff;
    border-radius: 20px;
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
    height: 600px;
    max-height: calc(100vh - 100px);
    overflow: hidden;
    animation: ai-chat-pop-in 0.3s cubic-bezier(0.22, 1, 0.36, 1);
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
    transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
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
    padding: 16px 20px;
    background: linear-gradient(135deg, #845bff, #3d6bff);
    color: #fff;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.ai-chat-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
}

.ai-chat-close {
    width: 32px;
    height: 32px;
    border: none;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
}

.ai-chat-close:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: rotate(90deg);
}

.ai-chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    background: #f5f7fb;
}

.ai-chat-messages::-webkit-scrollbar {
    width: 6px;
}

.ai-chat-messages::-webkit-scrollbar-track {
    background: transparent;
}

.ai-chat-messages::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.2);
    border-radius: 3px;
}

.ai-chat-messages::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.3);
}

.ai-chat-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex: 1;
    text-align: center;
    color: #6a7a99;
}

.ai-chat-empty p {
    margin: 8px 0;
    font-size: 14px;
    line-height: 1.6;
}

.ai-chat-hint {
    color: #9aa4b8;
    font-size: 13px;
}

.ai-message {
    display: flex;
    flex-direction: column;
    gap: 8px;
    max-width: 80%;
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
    padding: 12px 16px;
    border-radius: 16px;
    font-size: 14px;
    line-height: 1.6;
    word-wrap: break-word;
    white-space: pre-wrap;
}

.ai-message.user .ai-message-content {
    background: linear-gradient(135deg, #845bff, #3d6bff);
    color: #fff;
    border-bottom-right-radius: 4px;
}

.ai-message.assistant .ai-message-content {
    background: #fff;
    color: #1a2332;
    border: 1px solid rgba(61, 107, 255, 0.1);
    border-bottom-left-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.ai-message-loading {
    padding: 4px 0;
    display: flex;
    align-items: center;
}

.ai-message-placeholder {
    color: #9aa4b8;
    font-style: italic;
}

.ai-message-link {
    color: #3d6bff;
    text-decoration: none;
    border-bottom: 1px solid rgba(61, 107, 255, 0.3);
    transition: all 0.2s ease;
    cursor: pointer;
}

.ai-message-link:hover {
    color: #2d5ae6;
    border-bottom-color: #2d5ae6;
    background: rgba(61, 107, 255, 0.05);
    padding: 0 2px;
    margin: 0 -2px;
    border-radius: 3px;
}

.ai-message.user .ai-message-link {
    color: rgba(255, 255, 255, 0.9);
    border-bottom-color: rgba(255, 255, 255, 0.5);
}

.ai-message.user .ai-message-link:hover {
    color: #fff;
    border-bottom-color: rgba(255, 255, 255, 0.8);
    background: rgba(255, 255, 255, 0.1);
}

.ai-chat-input-wrapper {
    display: flex;
    gap: 12px;
    padding: 16px 20px;
    background: #fff;
    border-top: 1px solid #e8ecf4;
}

.ai-chat-input {
    flex: 1;
    height: 44px;
    padding: 0 16px;
    border: 2px solid rgba(61, 107, 255, 0.2);
    border-radius: 22px;
    font-size: 14px;
    outline: none;
    transition: all 0.2s ease;
    background: #f5f7fb;
}

.ai-chat-input:focus {
    border-color: #3d6bff;
    background: #fff;
    box-shadow: 0 0 0 4px rgba(61, 107, 255, 0.1);
}

.ai-chat-input:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.ai-chat-send {
    width: 44px;
    height: 44px;
    border: none;
    border-radius: 22px;
    background: linear-gradient(135deg, #845bff, #3d6bff);
    color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    flex-shrink: 0;
}

.ai-chat-send:hover:not(:disabled) {
    background: linear-gradient(135deg, #7445e6, #2d5ae6);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(132, 91, 255, 0.4);
}

.ai-chat-send:active:not(:disabled) {
    transform: translateY(0);
}

.ai-chat-send:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

@media (max-width: 768px) {
    .search-bar {
        gap: 12px;
    }

    .ai-chat-btn-wrapper {
        margin-left: 0;
        width: 100%;
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
</style>
