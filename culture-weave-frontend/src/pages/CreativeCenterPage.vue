<template>
    <div id="creativeCenterPage">
        <section class="hero">
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
                        <a-button size="large" ghost @click="refresh">
                            <ReloadOutlined />
                            刷新动态
                        </a-button>
                    </div>
                </div>
                <div class="hero-visual">
                    <div class="orb orb-1"></div>
                    <div class="orb orb-2"></div>
                    <div class="orb orb-3"></div>
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

                <transition-group v-else name="fade" tag="div" class="post-grid">
                    <article v-for="post in posts" :key="post.id" class="post-card">
                        <div class="post-cover" v-if="post.pictureUrl">
                            <img :src="post.pictureUrl" alt="post cover" loading="lazy" @error="onCoverError($event)" />
                        </div>
                        <div v-else class="post-cover placeholder">
                            <HighlightOutlined />
                            <span>创意灵感</span>
                        </div>

                        <div class="post-body">
                            <div class="post-meta">
                                <div class="author">
                                    <img v-if="post.userVO?.userAvatar" :src="post.userVO.userAvatar" alt="avatar"
                                        class="avatar" />
                                    <div v-else class="avatar placeholder-avatar">
                                        {{ abbreviation(post.userVO?.userName) }}
                                    </div>
                                    <div class="author-info">
                                        <span class="name">{{ post.userVO?.userName || '匿名创作者' }}</span>
                                        <span class="time">
                                            <ClockCircleOutlined />
                                            {{ formatTime(post.createTime) }}
                                        </span>
                                    </div>
                                </div>
                                <div class="meta-tags">
                                    <span v-if="authorizationLabel(post.authorization)"
                                        :class="['badge', post.authorization === 2 ? '' : 'outline']">
                                        {{ authorizationLabel(post.authorization) }}
                                    </span>
                                    <span v-if="statementLabel(post.statement)"
                                        :class="['badge', 'neutral', post.statement === 2 ? 'outline' : '']">
                                        {{ statementLabel(post.statement) }}
                                    </span>
                                </div>
                            </div>

                            <h3 class="post-title">{{ post.title || '未命名创作' }}</h3>
                            <p class="post-content">
                                {{ excerpt(post.content) }}
                            </p>
                        </div>

                        <footer class="post-footer">
                            <div class="footer-left">
                                <a-button type="link" @click="toggleExpand(post)">
                                    {{ expandedPostIds.has(post.id) ? '收起' : '展开全文' }}
                                </a-button>
                            </div>
                            <div class="footer-right">
                                <a-popconfirm v-if="canManage(post)" title="确定删除这篇创作吗？" ok-text="删除" cancel-text="取消"
                                    @confirm="() => removePost(post)">
                                    <a-button type="link" danger>
                                        <DeleteOutlined />
                                        删除
                                    </a-button>
                                </a-popconfirm>
                            </div>
                        </footer>

                        <div v-if="expandedPostIds.has(post.id)" class="post-expanded">
                            <a-typography-paragraph>
                                {{ post.content || '暂无正文内容' }}
                            </a-typography-paragraph>
                        </div>
                    </article>
                </transition-group>
            </a-spin>

            <div v-if="total > pagination.pageSize" class="pagination-wrapper">
                <a-pagination v-model:current="pagination.current" v-model:page-size="pagination.pageSize"
                    :total="total" :show-size-changer="true" :page-size-options="['6', '12', '18']"
                    @change="onPageChange" @show-size-change="onPageSizeChange" show-total />
            </div>
        </div>

    </div>
</template>

<script setup lang="ts">
/// <reference path="../api/typings.d.ts" />
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
    PlusOutlined,
    ReloadOutlined,
    SearchOutlined,
    UndoOutlined,
    CarryOutOutlined,
    HighlightOutlined,
    ClockCircleOutlined,
    DeleteOutlined
} from '@ant-design/icons-vue'
import { listResourcesVoByPage1, deletePost } from '../api/postController'
import { useLoginUserStore } from '../stores/useLoginUserStore'

type PostVO = API.PostVO

const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const posts = ref<PostVO[]>([])
const total = ref(0)

const filters = reactive({
    searchText: ''
})

const pagination = reactive({
    current: 1,
    pageSize: 6
})

const expandedPostIds = ref(new Set<number | undefined>())

const currentUserId = computed(() => loginUserStore.loginUser?.id || null)

let searchTimer: number | null = null

async function handleCreate() {
    if (!currentUserId.value) {
        await loginUserStore.fetchLoginUser()
    }
    if (!currentUserId.value) {
        message.warning('请先登录后再发布创作')
        router.push('/user/login')
        return
    }
    router.push('/creativeCenter/create')
}

function refresh() {
    fetchPosts()
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

function excerpt(content?: string) {
    if (!content) return '这位创作者暂未留下文字描述。'
    return content.length > 120 ? `${content.slice(0, 120)}...` : content
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

function canManage(post: PostVO) {
    if (!post?.userVO?.id || !currentUserId.value) return false
    return post.userVO.id === currentUserId.value
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
        const placeholder = img.nextElementSibling as HTMLElement
        if (placeholder) placeholder.style.display = 'flex'
    }
}

async function removePost(post: PostVO) {
    if (!post.id) return
    try {
        const res = await deletePost({ id: post.id })
        const responseData = (res as any)?.data as API.BaseResponseBoolean
        if (responseData?.code === 0) {
            message.success('已删除该创作')
            fetchPosts(1)
        } else {
            message.error(responseData?.message || '删除失败，请稍后再试')
        }
    } catch (error) {
        console.error('删除创作失败', error)
        message.error('删除失败，请稍后再试')
    }
}

onMounted(async () => {
    if (!currentUserId.value) {
        await loginUserStore.fetchLoginUser()
    }
    fetchPosts(1)
})
</script>

<style scoped>
#creativeCenterPage {
    min-height: calc(100vh - 200px);
    padding: 0;
    background: linear-gradient(180deg, #f8f6ff 0%, #f6fbff 100%);
}

.hero {
    padding: 56px 8% 40px;
}

.hero-inner {
    max-width: 1800px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 40px;
}

.hero-copy {
    max-width: 620px;
}

.overline {
    letter-spacing: 4px;
    text-transform: uppercase;
    font-size: 12px;
    color: #7f8ac5;
    margin-bottom: 8px;
}

.hero-copy h1 {
    font-size: 42px;
    margin: 0 0 12px 0;
    color: #1a2332;
    font-weight: 700;
}

.sub-title {
    font-size: 16px;
    color: #505b7a;
    line-height: 1.7;
    margin-bottom: 24px;
}

.hero-actions {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
}

.hero-visual {
    flex: 1;
    position: relative;
    min-height: 220px;
}

.orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(0);
    opacity: 0.8;
}

.orb-1 {
    width: 220px;
    height: 220px;
    background: radial-gradient(circle at top left, rgba(61, 107, 255, 0.9), rgba(61, 107, 255, 0));
    top: 0;
    right: 20%;
}

.orb-2 {
    width: 260px;
    height: 260px;
    background: radial-gradient(circle at center, rgba(255, 138, 128, 0.8), rgba(255, 138, 128, 0));
    bottom: 0;
    right: 0;
}

.orb-3 {
    width: 180px;
    height: 180px;
    background: radial-gradient(circle at center, rgba(130, 211, 255, 0.8), rgba(130, 211, 255, 0));
    top: 20%;
    left: 10%;
}

.page-container {
    padding: 0 8% 48px;
    max-width: 1800px;
    margin: 0 auto;
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
    border-radius: 999px;
    padding: 12px 18px;
    display: flex;
    align-items: center;
    gap: 12px;
    box-shadow: 0 10px 24px rgba(61, 107, 255, 0.12);
}

.search-box input {
    flex: 1;
    border: none;
    outline: none;
    font-size: 15px;
    background: transparent;
}

.search-icon {
    color: #3d6bff;
    font-size: 18px;
}

.clear-search {
    border: none;
    background: rgba(0, 0, 0, 0.05);
    width: 26px;
    height: 26px;
    border-radius: 50%;
    font-size: 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s ease;
}

.clear-search:hover {
    background: rgba(0, 0, 0, 0.1);
}

.toolbar-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    text-align: center;
    color: #6a7a99;
}

.empty-icon {
    width: 90px;
    height: 90px;
    border-radius: 50%;
    background: rgba(61, 107, 255, 0.12);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36px;
    color: #3d6bff;
    margin-bottom: 16px;
}

.empty-text {
    font-size: 20px;
    font-weight: 600;
    margin: 0 0 8px 0;
}

.empty-hint {
    font-size: 14px;
    margin-bottom: 16px;
}

.post-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 24px;
}

.post-card {
    background: #fff;
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 18px 36px rgba(26, 35, 50, 0.12);
    display: flex;
    flex-direction: column;
    position: relative;
    transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.3s ease;
}

.post-card:hover {
    transform: translateY(-6px);
    box-shadow: 0 24px 40px rgba(26, 35, 50, 0.18);
}

.post-cover {
    position: relative;
    width: 100%;
    height: 200px;
    overflow: hidden;
    background: linear-gradient(135deg, #eff3ff, #ffeef1);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    color: #9aa4b8;
    gap: 6px;
    font-size: 14px;
}

.post-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
}

.post-body {
    padding: 20px 24px 0;
    display: flex;
    flex-direction: column;
    gap: 16px;
    flex: 1;
}

.post-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
}

.author {
    display: flex;
    align-items: center;
    gap: 12px;
}

.avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    object-fit: cover;
    background: rgba(61, 107, 255, 0.2);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #1a2332;
    font-weight: 600;
}

.author-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.author-info .name {
    font-size: 14px;
    font-weight: 600;
    color: #1a2332;
}

.author-info .time {
    font-size: 12px;
    color: #6a7a99;
    display: flex;
    align-items: center;
    gap: 4px;
}

.meta-tags {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
    justify-content: flex-end;
}

.badge {
    padding: 4px 10px;
    border-radius: 999px;
    background: rgba(61, 107, 255, 0.15);
    color: #3d6bff;
    font-size: 12px;
}

.badge.outline {
    background: rgba(255, 138, 128, 0.12);
    color: #ff6b6b;
}

.badge.neutral {
    background: rgba(93, 118, 203, 0.15);
    color: #5d76cb;
}

.badge.neutral.outline {
    background: rgba(255, 171, 64, 0.15);
    color: #ff8f3d;
}

.post-title {
    font-size: 20px;
    font-weight: 700;
    margin: 0;
    color: #1a2332;
}

.post-content {
    font-size: 14px;
    color: #4f5b76;
    line-height: 1.8;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.post-footer {
    padding: 0 24px 18px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
}

.post-expanded {
    padding: 0 24px 24px;
    color: #444f6b;
    font-size: 14px;
    line-height: 1.8;
    background: linear-gradient(180deg, rgba(61, 107, 255, 0.06) 0%, rgba(132, 91, 255, 0.06) 100%);
}

.pagination-wrapper {
    margin-top: 32px;
    display: flex;
    justify-content: center;
}

.fade-enter-active,
.fade-leave-active {
    transition: all 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: translateY(12px);
}

@media (max-width: 1024px) {
    .hero-inner {
        flex-direction: column;
        align-items: flex-start;
    }

    .hero-visual {
        align-self: stretch;
        min-height: 180px;
    }
}

@media (max-width: 768px) {
    .toolbar {
        flex-direction: column;
        align-items: stretch;
    }

    .toolbar-actions {
        justify-content: flex-end;
    }

    .post-grid {
        grid-template-columns: 1fr;
    }
}
</style>