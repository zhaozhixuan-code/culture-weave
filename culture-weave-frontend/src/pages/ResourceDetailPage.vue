<template>
    <div id="resourceDetailPage">
        <a-card v-if="loading" class="loading-card">
            <a-skeleton active />
        </a-card>

        <a-card v-else-if="!resource" class="empty-card">
            <div class="empty-state">
                <PictureOutlined style="font-size:48px;color:#d3d9e6" />
                <p>资源不存在或已被删除</p>
            </div>
        </a-card>

        <div v-else class="detail-card">

            <div class="page-header">
                <h1 class="page-title">{{ resource.name || '未命名资源' }}</h1>
                <div class="summary-bar">

                </div>
            </div>
            <div class="hero-tags" v-if="normalizedTags.length">
                <div class="hero-tags-label">
                    <TagOutlined />
                    <span>标签</span>
                </div>
                <div class="hero-tags-list">
                    <span v-for="t in normalizedTags" :key="t" class="tag-chip">{{ t }}</span>
                </div>
            </div>
            <div class="detail-hero">
                <div class="hero-media">
                    <img v-if="resource.resourceImgUrl" :src="resource.resourceImgUrl" alt="封面" class="cover"
                        @error="onImageError" />
                    <div v-else class="cover-placeholder">
                        <PictureOutlined style="font-size:36px" />
                        <span>暂无封面</span>
                    </div>
                </div>

                <div class="hero-content">
                    <div class="hero-panels">
                        <a-card class="info-card hero-info-card" size="small" :bordered="true" title="基本信息">
                            <div class="basic-info-list">
                                <div class="basic-info-item">
                                    <span class="basic-info-label">非遗传承人</span>
                                    <span class="basic-info-value">{{ creatorName }}</span>
                                </div>
                                <div v-if="resource.category" class="basic-info-item">
                                    <span class="basic-info-label">分类</span>
                                    <span class="basic-info-value">{{ resource.category }}</span>
                                </div>
                                <div v-if="resource.region" class="basic-info-item">
                                    <span class="basic-info-label">地点</span>
                                    <span class="basic-info-value">{{ resource.region }}</span>
                                </div>
                                <div v-if="normalizedTags.length" class="basic-info-item basic-info-tags">
                                    <span class="basic-info-label">标签</span>
                                    <div class="basic-info-value basic-info-tag-value">
                                        <span v-for="t in normalizedTags" :key="t" class="tag-chip">{{ t }}</span>
                                    </div>
                                </div>
                                <div class="basic-info-item">
                                    <span class="basic-info-label">价格</span>
                                    <div class="basic-info-value">
                                        <div v-if="resource.price && resource.price > 0" class="price">
                                            <DollarOutlined />
                                            <span class="price-text">{{ formatPrice(resource.price) }}</span>
                                            <span class="price-hint">（含商用授权）</span>
                                        </div>
                                        <div v-else class="free-hint">免费资源 / 仅限非商用</div>
                                    </div>
                                </div>
                            </div>
                        </a-card>

                        <div class="hero-cta-card">
                            <div>

                                <h3 class="hero-cta-title">获取资源</h3>
                                <p class="hero-cta-desc">
                                    下载原始素材或联系创作者获取商业授权，保护非遗与版权。
                                </p>
                            </div>
                            <div class="cta-row hero-cta">
                                <a-button type="primary" size="large" :disabled="!canDownload"
                                    @click="onDownload">下载资源</a-button>
                                <a-button v-if="resource.price && resource.price > 0" size="large"
                                    @click="onPurchase">购买授权</a-button>
                                <a-button v-if="isOwner" danger @click="openDelete">删除资源</a-button>
                                <a-button v-if="isOwner" @click="onEdit">编辑</a-button>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

            <div class="content">
                <div class="detail-layout">
                    <div class="main-column">
                        <section class="introduction">
                            <div class="introduction-header">
                                <h3>详细介绍</h3>
                                <a-button v-if="resource.id" type="primary" :loading="aiExplaining"
                                    :disabled="aiExplaining" @click="handleAiExplain" class="ai-explain-btn">
                                    <RobotOutlined />
                                    <span>AI一键解释</span>
                                </a-button>
                            </div>
                            <p v-if="resource.introduction">{{ resource.introduction }}</p>
                            <p v-else class="muted">暂无简介</p>

                            <!-- AI解释区域 -->
                            <div v-if="showAiExplanation" class="ai-explanation-section"
                                @click="handleAiExplanationLinkClick">
                                <div class="ai-explanation-header">
                                    <div class="ai-explanation-title">
                                        <RobotOutlined />
                                        <span>AI智能解释</span>
                                    </div>
                                    <button class="ai-explanation-close" @click="closeAiExplanation" title="关闭">
                                        <CloseOutlined />
                                    </button>
                                </div>
                                <div class="ai-explanation-content" ref="aiExplanationContainer">
                                    <div v-if="!aiExplanation && !aiExplaining" class="ai-explanation-empty">
                                        <p>点击"AI一键解释"按钮，获取资源的智能解释</p>
                                    </div>
                                    <div v-else class="ai-explanation-text">
                                        <span v-if="aiExplanation" v-html="parseAiContent(aiExplanation)"></span>
                                        <span v-if="!aiExplanation && aiExplaining" class="ai-explanation-loading">
                                            <a-spin size="small" />
                                            <span>AI正在分析中...</span>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </section>

                        <section class="inheritor">
                            <div class="inheritor-head">
                                <div class="inheritor-title">
                                    <span class="inheritor-icon">
                                        <UserOutlined />
                                    </span>
                                    <div class="inheritor-text">
                                        <p class="inheritor-label">相关传承人</p>
                                        <h3 class="inheritor-name">{{ creatorName }}</h3>
                                    </div>
                                </div>
                                <span class="inheritor-badge">守护非遗</span>
                            </div>
                            <div class="inheritor-body">
                                <div class="inheritor-info">
                                    <p class="inheritor-copy" v-if="resource?.userDescription">{{
                                        resource.userDescription }}</p>
                                    <p class="inheritor-copy muted" v-else>这位传承人暂未提供更多介绍。</p>
                                </div>
                            </div>
                        </section>

                        <section class="similar">
                            <div class="similar-head">
                                <h3>相似资源</h3>
                                <p v-if="resource?.category" class="similar-hint">
                                    与 <strong>{{ resource.category }}</strong> 分类相关的更多灵感
                                </p>
                            </div>
                            <div v-if="similar.length" class="similar-gallery">
                                <article v-for="s in similar" :key="s.id" class="similar-tile"
                                    @click="goToDetail(s.id)">
                                    <div class="tile-media">
                                        <img v-if="s.resourceImgUrl" :src="s.resourceImgUrl" alt="resource cover"
                                            class="tile-cover" loading="lazy" />
                                        <div v-else class="tile-placeholder">
                                            <PictureOutlined />
                                        </div>
                                    </div>
                                    <div class="tile-content">
                                        <h4 class="tile-title">{{ s.name || '未命名资源' }}</h4>
                                        <div class="tile-meta">
                                            <span>
                                                <FolderOutlined v-if="s.category" />
                                                {{ s.category || '未分类' }}
                                            </span>
                                            <span v-if="s.userName">• {{ s.userName }}</span>
                                        </div>
                                        <div v-if="normalizeTags(s.tags).length" class="tile-tags">
                                            <span v-for="t in normalizeTags(s.tags)" :key="t" class="tile-tag">{{ t
                                            }}</span>
                                        </div>
                                    </div>
                                </article>
                            </div>
                            <div v-else class="muted">暂无相似资源</div>
                        </section>
                    </div>

                    <aside class="side-column">
                        <a-card v-if="resource?.userVO" class="creator-card" size="small" :bordered="true" title="创作者">
                            <div class="creator-profile">
                                <div class="creator-avatar">
                                    <img v-if="!creatorAvatarBroken && creatorAvatar" :src="creatorAvatar" alt="创作者头像"
                                        @error="onAvatarError" />
                                    <UserOutlined v-else />
                                </div>
                                <div class="creator-meta">
                                    <p class="creator-name">{{ creatorName }}</p>
                                    <p class="creator-desc">{{ creatorProfile }}</p>
                                </div>
                            </div>
                        </a-card>
                    </aside>
                </div>
            </div>
        </div>
        <a-modal v-model:open="deleteVisible" :confirm-loading="deleting" :footer="null" centered class="danger-modal"
            :mask-closable="!deleting">
            <div class="danger-modal-body">
                <div class="danger-icon">!</div>
                <h3 class="danger-title">确认删除该资源？</h3>
                <p class="danger-desc">删除后不可恢复，请谨慎操作。</p>
                <div class="danger-actions">
                    <a-button size="large" @click="closeDelete" :disabled="deleting">取消</a-button>
                    <a-button type="primary" size="large" danger @click="confirmDelete" :loading="deleting">
                        确认删除
                    </a-button>
                </div>
            </div>
        </a-modal>
    </div>
</template>

<script setup lang="ts">
/// <reference path="../api/typings.d.ts" />
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
    PictureOutlined,
    UserOutlined,
    EnvironmentOutlined,
    FolderOutlined,
    TagOutlined,
    DollarOutlined,
    RobotOutlined,
    CloseOutlined
} from '@ant-design/icons-vue'
import { getResourcesVoById, listResourcesVoByPage, deleteResource } from '../api/resourcesController'
import { useLoginUserStore } from '../stores/useLoginUserStore'
import myAxios from '../request'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const resource = ref<API.ResourcesVO | null>(null)
const similar = ref<API.ResourcesVO[]>([])
const creatorAvatarBroken = ref(false)

const normalizedTags = computed(() => normalizeTags(resource.value?.tags ?? []))
const creatorName = computed(
    () => resource.value?.userVO?.userName || resource.value?.userName || '匿名'
)
const creatorProfile = computed(
    () => resource.value?.userVO?.userProfile || '这位创作者暂未添加简介。'
)
const creatorAvatar = computed(() => resource.value?.userVO?.userAvatar || '')
// 获取当前登录用户ID
const currentUserId = computed(() => loginUserStore.loginUser?.id || null)
const isOwner = computed(() => {
    if (!currentUserId.value || !resource.value) return false
    return resource.value.userId === currentUserId.value
})
const canDownload = ref(true) // 简化权限判断，可根据实际业务逻辑调整
const deleteVisible = ref(false)
const deleting = ref(false)

// AI解释相关状态
const showAiExplanation = ref(false)
const aiExplanation = ref('')
const aiExplaining = ref(false)
const aiExplanationContainer = ref<HTMLElement | null>(null)

function normalizeTags(v: unknown): string[] {
    if (Array.isArray(v)) return v.map((i: any) => String(i).trim()).filter(Boolean)
    if (typeof v === 'string') return String(v).split(/[，,、;；\s\n\r\t]+/).map(s => s.trim()).filter(Boolean)
    return []
}

function formatPrice(v?: number) {
    if (typeof v === 'number' && !Number.isNaN(v)) return `¥ ${Math.trunc(v)}`
    return '-'
}

function formatTime(ts?: string) {
    if (!ts) return '-'
    try {
        const d = new Date(ts)
        return d.toLocaleString()
    } catch (e) {
        return ts
    }
}

async function load(id: string | number) {
    // 处理大数字 ID：如果 ID 是字符串，直接使用；如果是数字，转换为字符串
    // 这样可以避免 JavaScript Number 精度丢失问题（超过 2^53 的数字会丢失精度）
    const idStr = typeof id === 'string' ? id : String(id)

    if (!idStr || idStr.trim() === '' || idStr === '0' || idStr === 'NaN') {
        resource.value = null
        return
    }

    // 尝试转换为数字，但如果转换后精度丢失，则使用字符串
    // 检查是否是安全整数范围（-2^53 到 2^53）
    const idNum = Number(idStr)
    const isSafeInteger = Number.isSafeInteger(idNum) && idNum > 0

    // 如果转换后的数字不在安全范围内，或者转换后与原字符串不匹配，说明精度丢失
    const useStringId = !isSafeInteger || String(idNum) !== idStr

    loading.value = true
    try {
        // 根据后端是否支持字符串 ID，选择传递数字或字符串
        // 如果后端只接受数字，且 ID 在安全范围内，传递数字；否则传递字符串
        const idParam = useStringId ? (idStr as any) : idNum
        const res = await getResourcesVoById({ id: idParam })
        // 根据 OpenAPI 生成的接口，res 是 axios response，res.data 是 BaseResponseResourcesVO
        const responseData = (res as any)?.data as API.BaseResponseResourcesVO

        if (responseData?.code === 0 && responseData?.data) {
            resource.value = responseData.data
            // 加载相似资源（按分类或标签去匹配）
            loadSimilar()
        } else {
            // code 不是 0 或 data 为空，资源不存在
            resource.value = null
            if (responseData?.message) {
                message.warning(responseData.message)
            }
        }
    } catch (e: any) {
        console.error('加载资源详情失败', e)
        resource.value = null
        message.error('加载资源详情失败，请稍后重试')
    } finally {
        loading.value = false
    }
}

async function loadSimilar() {
    if (!resource.value || !resource.value.category) {
        similar.value = []
        return
    }

    const payload: API.ResourcesQueryRequest = {
        current: 1,
        pageSize: 9,
        sortField: 'createTime',
        sortOrder: 'descend',
        category: resource.value.category
    }

    try {
        const res = await listResourcesVoByPage(payload)
        // res 是 axios response，res.data 是 BaseResponsePageResourcesVO
        const responseData = (res as any)?.data as API.BaseResponsePageResourcesVO

        if (responseData?.code === 0 && responseData?.data) {
            // 过滤掉当前资源，只显示相似资源
            similar.value = (responseData.data.records || [])
                .filter(item => item.id !== resource.value?.id)
                .slice(0, 6)
        } else {
            similar.value = []
        }
    } catch (e) {
        console.error('加载相似资源失败', e)
        similar.value = []
    }
}

function onImageError(e: Event) {
    const t = e.target as HTMLImageElement
    if (t) t.style.display = 'none'
}

function onAvatarError(e: Event) {
    creatorAvatarBroken.value = true
    const t = e.target as HTMLImageElement
    if (t) t.style.display = 'none'
}

function goToDetail(i?: number) {
    if (!i) return
    router.push(`/resources/${i}`)
}

function onDownload() {
    // 根据后端实现，这里可以直接跳转到资源文件链接或调用下载接口
    // 作为示例，简单提示
    if (!resource.value) return
    // ResourcesVO 中没有 resourceFileUrl，暂时使用 resourceImgUrl
    window.open(resource.value.resourceImgUrl || '#', '_blank')
}

function onPurchase() {
    // 购买流程：跳转到支付页或弹窗
    alert('购买流程尚未实现，请接入支付/授权流程')
}

function openDelete() {
    if (!resource.value?.id) return
    deleteVisible.value = true
}

function closeDelete() {
    if (deleting.value) return
    deleteVisible.value = false
}

async function confirmDelete() {
    if (!resource.value?.id) return
    deleting.value = true
    try {
        const res = await deleteResource({ id: resource.value.id })
        const responseData = (res as any)?.data as API.BaseResponseBoolean
        if (responseData?.code === 0) {
            message.success('删除成功')
            deleteVisible.value = false
            router.push('/resources')
        } else {
            message.error(responseData?.message || '删除失败')
        }
    } catch (e: any) {
        console.error('删除资源失败', e)
        message.error('删除失败，请稍后重试')
    } finally {
        deleting.value = false
    }
}

function onEdit() {
    if (!resource.value) return
    router.push(`/resources/edit/${resource.value.id}`)
}

// AI解释相关函数
function handleAiExplain() {
    // 检查是否登录
    if (!currentUserId.value) {
        message.warning('请先登录后再使用AI解释功能')
        router.push({
            path: '/user/login',
            query: {
                redirect: route.fullPath
            }
        })
        return
    }

    // 检查是否有资源ID
    if (!resource.value?.id) {
        message.warning('资源信息不完整，无法进行AI解释')
        return
    }

    // 显示解释区域
    showAiExplanation.value = true
    aiExplanation.value = ''
    aiExplaining.value = true

    nextTick(() => {
        scrollAiExplanationToBottom()
    })

    // 调用AI解释接口
    fetchAiExplanation()
}

function closeAiExplanation() {
    showAiExplanation.value = false
    aiExplanation.value = ''
    aiExplaining.value = false
}

function scrollAiExplanationToBottom() {
    if (aiExplanationContainer.value) {
        aiExplanationContainer.value.scrollTop = aiExplanationContainer.value.scrollHeight
    }
}

// 解析AI内容，支持Markdown链接
function parseAiContent(content: string): string {
    if (!content) return ''

    // 转义HTML特殊字符
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

    // 匹配Markdown链接格式：[文本](URL)
    const linkRegex = /\[([^\]]+)\]\(([^)]+)\)/g

    const placeholders: string[] = []
    const links: Array<{ text: string; url: string }> = []
    let placeholderIndex = 0

    // 用占位符替换所有链接
    let result = content.replace(linkRegex, (match, linkText, url) => {
        const placeholder = `__LINK_PLACEHOLDER_${placeholderIndex}__`
        placeholders.push(placeholder)
        links.push({ text: linkText, url: url })
        placeholderIndex++
        return placeholder
    })

    // 转义整个文本
    result = escapeHtml(result)

    // 恢复占位符为HTML链接
    links.forEach((link, index) => {
        const escapedText = escapeHtml(link.text)
        const originalUrl = link.url

        let finalUrl = originalUrl
        let isExternal = false

        if (originalUrl.startsWith('http://') || originalUrl.startsWith('https://')) {
            isExternal = true
            finalUrl = escapeHtml(originalUrl)
        } else if (originalUrl.startsWith('//')) {
            isExternal = true
            finalUrl = escapeHtml(originalUrl)
        } else if (originalUrl.includes('localhost:') || originalUrl.includes('127.0.0.1:')) {
            const slashIndex = originalUrl.indexOf('/', originalUrl.indexOf(':'))
            if (slashIndex !== -1) {
                finalUrl = originalUrl.substring(slashIndex)
            } else {
                finalUrl = '/'
            }
            finalUrl = escapeHtml(finalUrl)
        } else {
            finalUrl = originalUrl.startsWith('/') ? escapeHtml(originalUrl) : '/' + escapeHtml(originalUrl)
        }

        const htmlLink = `<a href="${finalUrl}" class="ai-explanation-link" data-href="${finalUrl}" ${isExternal ? 'target="_blank" rel="noopener noreferrer"' : ''}>${escapedText}</a>`
        result = result.replace(placeholders[index], htmlLink)
    })

    // 将换行符转换为<br>
    result = result.replace(/\n/g, '<br>')

    return result
}

// 处理链接点击事件
function handleAiExplanationLinkClick(event: Event) {
    const target = event.target as HTMLElement
    const link = target.closest('.ai-explanation-link') as HTMLAnchorElement

    if (link) {
        event.preventDefault()
        event.stopPropagation()

        const href = link.getAttribute('data-href') || link.getAttribute('href') || ''

        if (href) {
            if (link.hasAttribute('target') && link.getAttribute('target') === '_blank') {
                window.open(href, '_blank', 'noopener,noreferrer')
            } else if (href.startsWith('http://') || href.startsWith('https://') || href.startsWith('//')) {
                window.open(href, '_blank', 'noopener,noreferrer')
            } else {
                router.push(href)
            }
        }
    }
}

async function fetchAiExplanation() {
    if (!resource.value?.id) return

    try {
        // 使用fetch处理SSE流
        const baseURL = myAxios.defaults.baseURL || 'http://localhost:8123'
        const params = new URLSearchParams({
            resourcesId: String(resource.value.id)
        })
        const url = `${baseURL}/ai/explain/resources?${params.toString()}`

        // 获取sessionId
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
                if (line.startsWith('data:')) {
                    let data = line.slice(5)
                    if (data.length > 0 && data[0] === ' ') {
                        data = data.slice(1)
                    }

                    if (data && data !== '[DONE]') {
                        aiExplanation.value = aiExplanation.value + data

                        requestAnimationFrame(() => {
                            nextTick(() => {
                                scrollAiExplanationToBottom()
                            })
                        })
                    }
                }
            }
        }

        // 处理剩余的buffer
        if (buffer.trim()) {
            const lines = buffer.split('\n')
            for (const line of lines) {
                if (line.startsWith('data:')) {
                    let data = line.slice(5)
                    if (data.length > 0 && data[0] === ' ') {
                        data = data.slice(1)
                    }
                    if (data && data !== '[DONE]') {
                        aiExplanation.value = aiExplanation.value + data
                        requestAnimationFrame(() => {
                            nextTick(() => {
                                scrollAiExplanationToBottom()
                            })
                        })
                    }
                }
            }
        }
    } catch (error: any) {
        console.error('AI解释失败', error)
        message.error(error.message || 'AI解释失败，请稍后重试')
        aiExplanation.value = '抱歉，发生了错误，请稍后重试。'
    } finally {
        aiExplaining.value = false
        scrollAiExplanationToBottom()
    }
}

watch(
    () => resource.value?.id,
    () => {
        creatorAvatarBroken.value = false
    }
)

// 监听路由参数变化，当 ID 改变时重新加载
// 注意：保持 ID 为字符串，避免大数字精度丢失
watch(
    () => route.params.id,
    (newId) => {
        if (newId) {
            // 直接使用字符串 ID，不转换为数字，避免精度丢失
            load(String(newId))
        }
    },
    { immediate: true }
)

onMounted(() => {
    if (route.params.id) {
        // 直接使用字符串 ID，不转换为数字，避免精度丢失
        load(String(route.params.id))
    }
})
</script>

<style scoped>
#resourceDetailPage {
    padding: 24px 8%;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', 'PingFang SC', 'Microsoft YaHei', sans-serif;
    color: #1a2332;
    line-height: 1.6;
    --cw-primary: #3d6bff;
    --cw-secondary: #ff6b95;
    --cw-text: #1a2332;
    --cw-muted: #6a7a99;
    --cw-bg-soft: #f7f9fc;
    --cw-chip-bg: rgba(61, 107, 255, 0.12);
    --cw-chip-border: rgba(61, 107, 255, 0.18);
}

.detail-card {
    max-width: 1800px;
    margin: 0 auto;
    padding: 0 8%;
}

.page-header {
    padding-top: 8px;
    padding-bottom: 8px;
    margin-bottom: 8px;
    position: relative;
}

.page-title {
    margin: 0;
    font-size: 30px;
    font-weight: 700;
    letter-spacing: 0.3px;
    color: #0f1b2e;
}

.page-title::after {
    content: '';
    display: block;
    margin-top: 8px;
    width: 56px;
    height: 3px;
    border-radius: 3px;
    background: linear-gradient(90deg, var(--cw-primary), var(--cw-secondary));
}

.detail-hero {
    display: grid;
    grid-template-columns: minmax(320px, 460px) 1fr;
    column-gap: 32px;
    row-gap: 16px;
    margin-top: 12px;
    margin-bottom: 28px;
    align-items: stretch;
}

.hero-media {
    position: relative;
    display: flex;
    flex-direction: column;
    height: 100%;
}

.hero-overlay {
    position: absolute;
    left: 16px;
    right: 16px;
    bottom: 16px;
    border-radius: 12px;
    padding: 14px 16px;
    background: rgba(11, 16, 40, 0.75);
    backdrop-filter: blur(6px);
    color: #fff;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.hero-owner {
    display: flex;
    align-items: center;
    gap: 12px;
}

.hero-owner-icon {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.18);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
}

.hero-owner-label {
    margin: 0;
    font-size: 12px;
    letter-spacing: 0.4px;
    opacity: 0.75;
}

.hero-owner-name {
    margin: 0;
    font-size: 16px;
}

.hero-overlay-info {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.hero-overlay-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}

.meta-pill {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 8px 14px;
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.18);
    min-width: 0;
}

.meta-pill-icon {
    width: 32px;
    height: 32px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #0b1028;
    font-size: 16px;
}

.meta-pill-content {
    display: flex;
    flex-direction: column;
    line-height: 1.2;
}

.meta-pill-label {
    font-size: 11px;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: rgba(255, 255, 255, 0.7);
}

.meta-pill-value {
    font-size: 14px;
    font-weight: 600;
}

.hero-content {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.hero-panels {
    display: flex;
    flex-direction: column;
    gap: 16px;
    flex: 1;
}

.hero-cta-card {
    border-radius: 20px;
    padding: 24px;
    background: #fff;
    color: #22263e;
    display: flex;
    flex-direction: column;
    gap: 20px;
    box-shadow: 0 12px 28px rgba(15, 25, 68, 0.18);
    margin-top: auto;
}

.hero-cta-label {
    margin: 0;
    font-size: 12px;
    letter-spacing: 0.2em;
    text-transform: uppercase;
    color: rgba(34, 38, 62, 0.6);
}

.hero-cta-title {
    margin: 4px 0 6px 0;
    font-size: 22px;
    font-weight: 700;
}

.hero-cta-desc {
    margin: 0;
    font-size: 14px;
    color: rgba(34, 38, 62, 0.8);
}

.hero-cta-card .cta-row {
    flex-direction: row;
    align-items: stretch;
    flex-wrap: nowrap;
    width: 100%;
}

.hero-cta-card .cta-row :deep(.ant-btn) {
    flex: 1;
    justify-content: center;
}

.hero-cta-card :deep(.ant-btn) {
    height: 46px;
    font-size: 16px;
}

.detail-hero .hero-info-card {
    border-radius: 20px;
    box-shadow: 0 18px 36px rgba(21, 36, 76, 0.12);
    border: 1px solid rgba(82, 110, 255, 0.16);
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(242, 246, 255, 0.9));
}

.detail-hero .hero-info-card :deep(.ant-card-head) {
    border-bottom: none;
    padding: 18px 20px 6px;
}

.detail-hero .hero-info-card :deep(.ant-card-body) {
    padding: 0 20px 20px;
}

.detail-hero .hero-info-card :deep(.ant-card-head-title) {
    font-size: 16px;
    font-weight: 700;
    letter-spacing: 0.02em;
}

.detail-hero .hero-info-card :deep(.ant-descriptions-view) {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.detail-hero .hero-info-card :deep(.ant-descriptions-item) {
    padding: 0;
}

.detail-hero .hero-info-card :deep(.ant-descriptions-item-label) {
    font-size: 12px;
    color: #7e89a8;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    font-weight: 600;
    margin-bottom: 4px;
    display: inline-block;
}

.detail-hero .hero-info-card :deep(.ant-descriptions-item-content) {
    font-size: 17px;
    font-weight: 600;
    color: var(--cw-text);
}

.basic-info-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.basic-info-item {
    display: flex;
    align-items: center;
    gap: 16px;
}

.basic-info-item.basic-info-tags {
    align-items: flex-start;
}

.basic-info-label {
    flex: 0 0 88px;
    font-size: 13px;
    font-weight: 600;
    color: #6f7a95;
    letter-spacing: 0.04em;
}

.basic-info-value {
    flex: 1;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
    font-size: 15px;
    color: var(--cw-text);
}

.basic-info-tag-value {
    gap: 8px;
}

.detail-hero .hero-tags {
    grid-column: 1 / -1;
}

.hero-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    margin-top: 4px;
}

.hero-tags-label {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-weight: 600;
    color: var(--cw-text);
}

.hero-tags-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}

.hero-cta {
    flex-wrap: wrap;
    margin-top: 6px;
}

.cover {
    width: 100%;
    flex: 1;
    height: 100%;
    min-height: 360px;
    object-fit: cover;
    border-radius: 12px;
    border: 1px solid rgba(0, 0, 0, 0.06);
    box-shadow: 0 12px 28px rgba(27, 57, 138, 0.15);
}

.cover-placeholder {
    width: 100%;
    flex: 1;
    height: 100%;
    min-height: 360px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: linear-gradient(135deg, #f5f7fb, #e8ecf4);
    border-radius: 12px;
    color: #9aa4b8;
    border: 1px dashed rgba(0, 0, 0, 0.08);
}

.actions {
    display: flex;
    gap: 12px;
}

.danger-modal :deep(.ant-modal-content) {
    border-radius: 18px;
    padding: 0;
    overflow: hidden;
}

.danger-modal-body {
    padding: 24px 24px 20px;
    text-align: center;
}

.danger-icon {
    width: 52px;
    height: 52px;
    line-height: 52px;
    border-radius: 50%;
    margin: 0 auto 10px;
    background: linear-gradient(135deg, #ffeded, #ffe5e5);
    color: #ff4d4f;
    font-weight: 800;
    font-size: 22px;
    box-shadow: 0 8px 20px rgba(255, 107, 107, 0.25) inset;
}

.danger-title {
    margin: 0;
    font-size: 18px;
    color: #1a2332;
}

.danger-desc {
    margin: 6px 0 0 0;
    font-size: 13px;
    color: #6a7a99;
}

.danger-actions {
    margin-top: 16px;
    display: flex;
    gap: 12px;
    justify-content: center;
}

.info-card {
    width: 100%;
    border-radius: 12px;
    box-shadow: 0 6px 18px rgba(27, 57, 138, 0.08);
}

.info-card :deep(.ant-card-head) {
    border-bottom: 0;
    padding: 12px 16px;
}

.info-card :deep(.ant-card-head-title) {
    font-size: 16px;
    font-weight: 600;
    color: var(--cw-text);
}

.info-card :deep(.ant-card-body) {
    padding: 12px 16px;
}

.info-card :deep(.ant-descriptions-item) {
    padding: 6px 0;
}

.info-card :deep(.ant-descriptions-item-label) {
    color: var(--cw-muted);
    font-weight: 600;
    display: inline-block;
    min-width: 64px;
}

.info-card :deep(.ant-descriptions-item-content) {
    color: var(--cw-text);
}

.title {
    margin: 0 0 12px 0;
    font-size: 26px;
}

.price-area {
    margin-top: 6px;
}

.price {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    color: var(--cw-secondary);
    font-weight: 600;
}

.price-hint {
    font-size: 12px;
    color: #9aa4b8;
    margin-left: 8px;
}

.cta-row {
    display: flex;
    gap: 12px;
    align-items: center;
}

.cta-row :deep(.ant-btn-primary) {
    background: linear-gradient(90deg, var(--cw-primary), #6f8bff);
    box-shadow: 0 8px 20px rgba(61, 107, 255, 0.25);
}

.cta-row :deep(.ant-btn-primary:hover) {
    filter: brightness(1.05);
}

.content {
    margin-top: 8px;
}

.detail-layout {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(320px, 460px);
    gap: 32px;
    align-items: start;
}

.main-column {
    display: contents;
}

.side-column {
    display: flex;
    flex-direction: column;
    gap: 16px;
    position: sticky;
    top: 32px;
    grid-column: 2;
}

.creator-card {
    border-radius: 12px;
    box-shadow: 0 8px 22px rgba(27, 57, 138, 0.08);
}

.creator-profile {
    display: flex;
    gap: 14px;
    align-items: flex-start;
}

.creator-avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: var(--cw-bg-soft);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--cw-primary);
    font-size: 20px;
    flex-shrink: 0;
    overflow: hidden;
}

.creator-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.creator-name {
    margin: 0;
    font-weight: 600;
    color: var(--cw-text);
}

.creator-desc {
    margin: 4px 0 0 0;
    font-size: 13px;
    color: var(--cw-muted);
    line-height: 1.6;
}

.introduction {
    background: #fff;
    padding: 16px;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(15, 35, 95, 0.04);
    border: 1px solid rgba(61, 107, 255, 0.1);
    width: 100%;
    max-width: none;
    margin-left: 0;
    margin-right: 0;
    grid-column: 1 / -1;
}

.introduction-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.introduction-header h3 {
    margin: 0;
    font-weight: 700;
    color: var(--cw-text);
    font-size: 20px;
    letter-spacing: 0.04em;
}

.introduction p {
    margin: 0 0 14px 0;
    font-size: 15px;
    line-height: 1.8;
    color: #6a7a99;
}

.introduction p.muted {
    color: var(--cw-muted);
    font-style: italic;
}

.ai-explain-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 600;
    letter-spacing: 0.02em;
    border-radius: 999px;
    padding: 0 18px;
    height: 36px;
    background: linear-gradient(135deg, #6a7bff, #8f7bff);
    border: none;
    box-shadow: 0 8px 20px rgba(80, 102, 255, 0.25);
    transition: all 0.2s ease;
}

.ai-explain-btn.ant-btn[disabled] {
    background: linear-gradient(135deg, #9aa7ff, #b1a0ff);
    box-shadow: none;
    opacity: 0.9;
}

.ai-explain-btn:hover,
.ai-explain-btn:focus {
    background: linear-gradient(135deg, #596bff, #7c69ff);
    transform: translateY(-1px);
    box-shadow: 0 12px 26px rgba(58, 88, 255, 0.35);
}

.ai-explain-btn .anticon {
    font-size: 16px;
}

.ai-explanation-section {
    margin-top: 20px;
    border: 1px solid rgba(61, 107, 255, 0.2);
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(132, 91, 255, 0.05), rgba(61, 107, 255, 0.05));
    overflow: hidden;
    animation: ai-explanation-fade-in 0.3s ease;
}

@keyframes ai-explanation-fade-in {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.ai-explanation-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background: linear-gradient(135deg, rgba(132, 91, 255, 0.1), rgba(61, 107, 255, 0.1));
    border-bottom: 1px solid rgba(61, 107, 255, 0.1);
}

.ai-explanation-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 700;
    color: #3d6bff;
    letter-spacing: 0.03em;
    text-transform: uppercase;
}

.ai-explanation-close {
    width: 24px;
    height: 24px;
    border: none;
    background: rgba(61, 107, 255, 0.1);
    border-radius: 50%;
    color: #3d6bff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
}

.ai-explanation-close:hover {
    background: rgba(61, 107, 255, 0.2);
    transform: rotate(90deg);
}

.ai-explanation-content {
    max-height: 400px;
    overflow-y: auto;
    padding: 16px;
}

.ai-explanation-content::-webkit-scrollbar {
    width: 6px;
}

.ai-explanation-content::-webkit-scrollbar-track {
    background: transparent;
}

.ai-explanation-content::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.2);
    border-radius: 3px;
}

.ai-explanation-content::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.3);
}

.ai-explanation-empty {
    text-align: center;
    color: #9aa4b8;
    padding: 20px;
}

.ai-explanation-text {
    line-height: 1.8;
    color: #1a2332;
    font-size: 15px;
    letter-spacing: 0.01em;
}

.ai-explanation-loading {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #6a7a99;
}

.ai-explanation-link {
    color: #3d6bff;
    text-decoration: none;
    border-bottom: 1px solid rgba(61, 107, 255, 0.3);
    transition: all 0.2s ease;
    cursor: pointer;
}

.ai-explanation-link:hover {
    color: #2d5ae6;
    border-bottom-color: #2d5ae6;
    background: rgba(61, 107, 255, 0.05);
    padding: 0 2px;
    margin: 0 -2px;
    border-radius: 3px;
}

.inheritor {
    margin-top: 24px;
    padding: 20px;
    border-radius: 16px;
    border: 1px solid rgba(61, 107, 255, 0.12);
    background: #fff;
    box-shadow: 0 8px 20px rgba(22, 45, 110, 0.08);
    grid-column: 1 / -1;
}

.inheritor-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
}

.inheritor-title {
    display: flex;
    align-items: center;
    gap: 12px;
}

.inheritor-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    background: rgba(61, 107, 255, 0.12);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: var(--cw-primary);
    flex-shrink: 0;
}

.inheritor-text {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.inheritor-name {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: var(--cw-text);
    letter-spacing: 0.01em;
}

.inheritor-label {
    margin: 0;
    font-size: 12px;
    letter-spacing: 0.18em;
    text-transform: uppercase;
    color: var(--cw-muted);
    font-weight: 600;
}

.inheritor-badge {
    padding: 6px 14px;
    border-radius: 999px;
    background: rgba(255, 138, 138, 0.15);
    color: #ff6b6b;
    font-weight: 600;
    font-size: 13px;
}

.inheritor-body {
    display: flex;
    gap: 16px;
    align-items: flex-start;
}

.inheritor-avatar {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: #fff;
    border: 2px solid rgba(61, 107, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: var(--cw-primary);
    flex-shrink: 0;
    overflow: hidden;
}

.inheritor-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.inheritor-copy {
    margin: 0;
    font-size: 15px;
    line-height: 1.8;
    color: #3a4460;
    font-weight: 500;
}

.inheritor-meta {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    font-size: 13px;
    color: #6a7a99;
}

.inheritor-meta span {
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.more-info {
    display: flex;
    gap: 24px;
    margin-top: 16px;
}

.info-item .label {
    font-weight: 600;
    color: #4f5b76
}

.info-item .value {
    color: #6a7a99
}

.similar {
    margin-top: 24px;
    background: linear-gradient(145deg, rgba(61, 107, 255, 0.08), rgba(255, 138, 138, 0.08));
    border-radius: 18px;
    padding: 20px;
    border: 1px solid rgba(61, 107, 255, 0.12);
    width: 100%;
    max-width: none;
    margin-left: 0;
    margin-right: 0;
    grid-column: 1 / -1;
}

.similar-head {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-bottom: 16px;
}

.similar-head h3 {
    margin: 0;
    font-size: 20px;
    color: var(--cw-text);
}

.similar-hint {
    margin: 0;
    font-size: 13px;
    color: var(--cw-muted);
}

.similar-hint strong {
    color: #3d6bff;
}

.similar-gallery {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
}

.similar-tile {
    border-radius: 16px;
    overflow: hidden;
    background: #fff;
    box-shadow: 0 10px 24px rgba(27, 57, 138, 0.12);
    transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.35s ease;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    min-height: 220px;
}

.similar-tile:hover {
    transform: translateY(-6px);
    box-shadow: 0 18px 30px rgba(27, 57, 138, 0.18);
}

.tile-media {
    height: 140px;
    position: relative;
    overflow: hidden;
    background: linear-gradient(135deg, #f5f7ff, #fdf1f1);
}

.tile-cover {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.3s ease;
}

.similar-tile:hover .tile-cover {
    transform: scale(1.05);
}

.tile-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #d0d8f5;
    font-size: 32px;
}

.tile-content {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.tile-title {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: var(--cw-text);
    line-height: 1.4;
}

.tile-meta {
    display: flex;
    gap: 8px;
    align-items: center;
    font-size: 13px;
    color: var(--cw-muted);
    flex-wrap: wrap;
}

.tile-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}

.tile-tag {
    padding: 4px 10px;
    border-radius: 999px;
    background: rgba(61, 107, 255, 0.12);
    color: #3d6bff;
    font-size: 12px;
}

.muted {
    color: #9aa4b8
}

.tag-chip {
    padding: 4px 10px;
    background: var(--cw-chip-bg);
    color: var(--cw-primary);
    border: 1px solid var(--cw-chip-border);
    border-radius: 999px;
    margin-right: 8px;
    font-size: 12px;
}

@media (max-width: 900px) {
    .detail-hero {
        grid-template-columns: 1fr;
    }

    .hero-cta-card {
        padding: 20px;
    }

    .detail-layout {
        grid-template-columns: 1fr;
    }

    .side-column {
        position: static;
    }

    .main-column,
    .side-column {
        grid-column: auto;
    }

    .cover {
        height: 260px;
    }
}
</style>
.summary-bar {
display: flex;
gap: 12px;
margin-top: 12px;
flex-wrap: wrap;
}

.summary-chip {
display: inline-flex;
align-items: center;
gap: 10px;
padding: 8px 16px 8px 10px;
border-radius: 16px;
background: linear-gradient(120deg, rgba(17, 78, 215, 0.1), rgba(255, 255, 255, 0.05));
border: 1px solid rgba(17, 78, 215, 0.25);
color: var(--cw-primary);
font-size: 12px;
box-shadow: 0 4px 12px rgba(12, 27, 66, 0.08);
}

.summary-chip-icon {
width: 34px;
height: 34px;
border-radius: 12px;
background: rgba(17, 78, 215, 0.1);
color: var(--cw-primary);
display: flex;
align-items: center;
justify-content: center;
font-size: 16px;
}

.summary-chip-text {
display: flex;
flex-direction: column;
line-height: 1.2;
}

.summary-chip-label {
text-transform: uppercase;
letter-spacing: 0.08em;
font-size: 10px;
color: rgba(13, 27, 66, 0.6);
}

.summary-chip-value {
font-size: 14px;
font-weight: 600;
color: #0b1129;
}
