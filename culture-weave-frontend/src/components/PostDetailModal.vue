<template>
    <a-modal v-model:open="visible" :footer="null" :width="1200" :styles="{ body: { padding: 0 } }"
        :mask-closable="true" @cancel="handleClose" class="post-detail-modal">
        <div class="post-detail-container" v-if="postDetail">
            <!-- 左侧图片区域 -->
            <div class="post-image-section">
                <div v-if="postDetail.pictureUrl" class="post-image-wrapper">
                    <img :src="postDetail.pictureUrl" alt="post image" class="post-image" />
                </div>
                <div v-else class="post-image-placeholder">
                    <HighlightOutlined />
                    <span>创意灵感</span>
                </div>
            </div>

            <!-- 右侧内容区域 -->
            <div class="post-content-section">
                <!-- 用户信息头部 -->
                <div class="post-header">
                    <div class="user-info">
                        <img v-if="postDetail.userVO?.userAvatar" :src="postDetail.userVO.userAvatar" alt="avatar"
                            class="user-avatar" />
                        <div v-else class="user-avatar placeholder-avatar">
                            {{ abbreviation(postDetail.userVO?.userName) }}
                        </div>
                        <div class="user-details">
                            <div class="user-name">{{ postDetail.userVO?.userName || '匿名创作者' }}</div>
                            <div class="post-time">
                                <ClockCircleOutlined />
                                {{ formatTime(postDetail.createTime) }}
                            </div>
                        </div>
                    </div>
                    <div class="post-tags">
                        <span v-if="authorizationLabel(postDetail.authorization)"
                            :class="['badge', postDetail.authorization === 2 ? '' : 'outline']">
                            {{ authorizationLabel(postDetail.authorization) }}
                        </span>
                        <span v-if="statementLabel(postDetail.statement)"
                            :class="['badge', 'neutral', postDetail.statement === 2 ? 'outline' : '']">
                            {{ statementLabel(postDetail.statement) }}
                        </span>
                    </div>
                </div>

                <!-- 帖子标题和内容 -->
                <div class="post-body">
                    <h2 class="post-title">{{ postDetail.title || '未命名创作' }}</h2>
                    <div class="post-text">
                        <a-typography-paragraph>
                            {{ postDetail.content || '暂无正文内容' }}
                        </a-typography-paragraph>
                    </div>
                </div>

                <!-- 评论区域 -->
                <div class="comments-section">
                    <div class="comments-header">
                        <h3 class="comments-title">
                            <MessageOutlined />
                            评论 ({{ commentTotal }})
                        </h3>
                    </div>

                    <!-- 评论列表 -->
                    <div class="comments-list" ref="commentsListRef">
                        <a-spin :spinning="commentsLoading" size="small">
                            <div v-if="!commentsLoading && comments.length === 0" class="empty-comments">
                                <CommentOutlined />
                                <p>还没有评论，快来抢沙发吧~</p>
                            </div>
                            <div v-else class="comment-items">
                                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                                    <img v-if="comment.creator?.userAvatar" :src="comment.creator.userAvatar" alt="avatar"
                                        class="comment-avatar" />
                                    <div v-else class="comment-avatar placeholder-avatar">
                                        {{ abbreviation(comment.creator?.userName) }}
                                    </div>
                                    <div class="comment-content">
                                        <div class="comment-header">
                                            <span class="comment-author">{{ comment.creator?.userName || '匿名用户' }}</span>
                                            <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                                        </div>
                                        <div class="comment-text">{{ comment.content }}</div>
                                    </div>
                                </div>
                            </div>
                        </a-spin>

                        <!-- 评论分页 -->
                        <div v-if="commentTotal > commentPagination.pageSize" class="comment-pagination">
                            <a-pagination v-model:current="commentPagination.current" v-model:page-size="commentPagination.pageSize"
                                :total="commentTotal" :show-size-changer="false" size="small"
                                @change="onCommentPageChange" />
                        </div>
                    </div>

                    <!-- 评论输入框 -->
                    <div class="comment-input-section">
                        <div class="comment-input-wrapper">
                            <img v-if="currentUser?.userAvatar" :src="currentUser.userAvatar" alt="avatar"
                                class="input-avatar" />
                            <div v-else class="input-avatar placeholder-avatar">
                                {{ abbreviation(currentUser?.userName) }}
                            </div>
                            <a-textarea v-model:value="newComment" :placeholder="currentUser ? '写下你的评论...' : '请先登录后评论'"
                                :rows="3" :disabled="!currentUser" class="comment-input" />
                        </div>
                        <div class="comment-actions">
                            <a-button type="primary" :loading="submitting" :disabled="!currentUser || !newComment.trim()"
                                @click="handleSubmitComment">
                                发布评论
                            </a-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </a-modal>
</template>

<script setup lang="ts">
/// <reference path="../api/typings.d.ts" />
import { ref, computed, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
    HighlightOutlined,
    ClockCircleOutlined,
    MessageOutlined,
    CommentOutlined
} from '@ant-design/icons-vue'
import { getResourcesVoById1 } from '../api/postController'
import { getCommentList, addComment } from '../api/commentController'
import { useLoginUserStore } from '../stores/useLoginUserStore'

type PostVO = API.PostVO
type CommentVO = API.CommentVO

const props = defineProps<{
    postId?: number | null
    open: boolean
}>()

const emit = defineEmits<{
    (e: 'update:open', value: boolean): void
    (e: 'close'): void
}>()

const loginUserStore = useLoginUserStore()
const visible = computed({
    get: () => props.open,
    set: (val) => emit('update:open', val)
})

const postDetail = ref<PostVO | null>(null)
const comments = ref<CommentVO[]>([])
const commentTotal = ref(0)
const loading = ref(false)
const commentsLoading = ref(false)
const submitting = ref(false)
const newComment = ref('')
const commentsListRef = ref<HTMLElement | null>(null)

const currentUser = computed(() => loginUserStore.loginUser)

const commentPagination = ref({
    current: 1,
    pageSize: 10
})

function abbreviation(name?: string | null) {
    if (!name) return '匿'
    return name.slice(0, 1).toUpperCase()
}

function formatTime(value?: string) {
    if (!value) return '刚刚'
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return value
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)

    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`
    return date.toLocaleDateString()
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

async function fetchPostDetail() {
    if (!props.postId) return
    loading.value = true
    try {
        const res = await getResourcesVoById1({ id: props.postId })
        const responseData = (res as any)?.data as API.BaseResponsePostVO
        if (responseData?.code === 0 && responseData?.data) {
            postDetail.value = responseData.data
        } else {
            message.error('加载帖子详情失败')
            handleClose()
        }
    } catch (error) {
        console.error('加载帖子详情失败', error)
        message.error('加载帖子详情失败，请稍后再试')
        handleClose()
    } finally {
        loading.value = false
    }
}

async function fetchComments() {
    if (!props.postId) return
    commentsLoading.value = true
    try {
        const payload: API.CommentQueryRequest = {
            current: commentPagination.value.current,
            pageSize: commentPagination.value.pageSize,
            postId: props.postId,
            sortField: 'createTime',
            sortOrder: 'descend'
        }
        const res = await getCommentList(payload)
        const responseData = (res as any)?.data as API.BaseResponsePageCommentVO
        if (responseData?.code === 0 && responseData?.data) {
            comments.value = responseData.data.records || []
            commentTotal.value = responseData.data.total || 0
        } else {
            comments.value = []
            commentTotal.value = 0
        }
    } catch (error) {
        console.error('加载评论失败', error)
        message.error('加载评论失败，请稍后再试')
        comments.value = []
        commentTotal.value = 0
    } finally {
        commentsLoading.value = false
    }
}

function onCommentPageChange(page: number) {
    commentPagination.value.current = page
    fetchComments()
    // 滚动到评论区域顶部
    nextTick(() => {
        if (commentsListRef.value) {
            commentsListRef.value.scrollTop = 0
        }
    })
}

async function handleSubmitComment() {
    if (!props.postId || !newComment.value.trim() || !currentUser.value) return

    submitting.value = true
    try {
        const payload: API.CommentAddRequest = {
            postId: props.postId,
            content: newComment.value.trim()
        }
        const res = await addComment(payload)
        const responseData = (res as any)?.data as API.BaseResponseCommentVO
        if (responseData?.code === 0) {
            message.success('评论发布成功')
            newComment.value = ''
            // 重置到第一页并刷新评论
            commentPagination.value.current = 1
            await fetchComments()
            // 滚动到评论列表顶部
            nextTick(() => {
                if (commentsListRef.value) {
                    commentsListRef.value.scrollTop = 0
                }
            })
        } else {
            message.error(responseData?.message || '评论发布失败，请稍后再试')
        }
    } catch (error) {
        console.error('发布评论失败', error)
        message.error('评论发布失败，请稍后再试')
    } finally {
        submitting.value = false
    }
}

function handleClose() {
    visible.value = false
    emit('close')
    // 清理数据
    postDetail.value = null
    comments.value = []
    commentTotal.value = 0
    newComment.value = ''
    commentPagination.value.current = 1
}

// 监听弹窗打开，加载数据
watch(() => props.open, async (newVal) => {
    if (newVal && props.postId) {
        await Promise.all([fetchPostDetail(), fetchComments()])
        // 确保用户信息已加载
        if (!currentUser.value) {
            await loginUserStore.fetchLoginUser()
        }
    }
})
</script>

<style scoped>
.post-detail-modal :deep(.ant-modal-content) {
    border-radius: 12px;
    overflow: hidden;
}

.post-detail-modal :deep(.ant-modal-body) {
    padding: 0;
    max-height: 90vh;
    overflow: hidden;
}

.post-detail-container {
    display: flex;
    height: 90vh;
    max-height: 800px;
    background: #fff;
}

/* 左侧图片区域 */
.post-image-section {
    flex: 0 0 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #fcf9f4, #f5efe8);
    border-right: 1px solid rgba(212, 167, 106, 0.2);
}

.post-image-wrapper {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}

.post-image {
    width: 100%;
    height: 100%;
    object-fit: contain;
    background: #fff;
}

.post-image-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    color: #8b4513;
    font-size: 16px;
}

.post-image-placeholder :deep(.anticon) {
    font-size: 48px;
    opacity: 0.6;
}

/* 右侧内容区域 */
.post-content-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.post-header {
    padding: 24px;
    border-bottom: 1px solid rgba(212, 167, 106, 0.15);
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.user-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    object-fit: cover;
    background: rgba(212, 167, 106, 0.2);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #2c1810;
    font-weight: 600;
    font-family: 'Noto Sans SC', sans-serif;
}

.placeholder-avatar {
    background: rgba(212, 167, 106, 0.2);
}

.user-details {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.user-name {
    font-size: 16px;
    font-weight: 600;
    color: #2c1810;
    font-family: 'Noto Sans SC', sans-serif;
}

.post-time {
    font-size: 12px;
    color: #8b4513;
    display: flex;
    align-items: center;
    gap: 4px;
    font-family: 'Noto Sans SC', sans-serif;
}

.post-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.badge {
    padding: 4px 12px;
    border-radius: 4px;
    background: rgba(164, 123, 67, 0.15);
    color: #8b4513;
    font-size: 12px;
    font-family: 'Noto Sans SC', sans-serif;
}

.badge.outline {
    background: rgba(139, 69, 19, 0.12);
    color: #a0522d;
}

.badge.neutral {
    background: rgba(184, 134, 11, 0.15);
    color: #b8860b;
}

.badge.neutral.outline {
    background: rgba(101, 67, 33, 0.15);
    color: #654321;
}

/* 帖子内容 */
.post-body {
    padding: 24px;
    border-bottom: 1px solid rgba(212, 167, 106, 0.15);
    flex-shrink: 0;
    overflow-y: auto;
    max-height: 300px;
}

.post-title {
    font-size: 24px;
    font-weight: 700;
    margin: 0 0 16px 0;
    color: #2c1810;
    font-family: 'Noto Serif SC', serif;
}

.post-text {
    font-size: 15px;
    color: #5d4037;
    line-height: 1.8;
    font-family: 'Noto Sans SC', sans-serif;
}

.post-text :deep(.ant-typography) {
    margin-bottom: 0;
    color: #5d4037;
}

/* 评论区域 */
.comments-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.comments-header {
    padding: 20px 24px;
    border-bottom: 1px solid rgba(212, 167, 106, 0.15);
    flex-shrink: 0;
}

.comments-title {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
    color: #2c1810;
    display: flex;
    align-items: center;
    gap: 8px;
    font-family: 'Noto Sans SC', sans-serif;
}

.comments-title :deep(.anticon) {
    color: #8b4513;
}

/* 评论列表 */
.comments-list {
    flex: 1;
    overflow-y: auto;
    padding: 16px 24px;
}

.empty-comments {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    color: #8b4513;
    gap: 12px;
}

.empty-comments :deep(.anticon) {
    font-size: 48px;
    opacity: 0.4;
}

.empty-comments p {
    margin: 0;
    font-size: 14px;
    color: #5d4037;
    font-family: 'Noto Sans SC', sans-serif;
}

.comment-items {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.comment-item {
    display: flex;
    gap: 12px;
}

.comment-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
    background: rgba(212, 167, 106, 0.2);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #2c1810;
    font-weight: 600;
    font-size: 14px;
    flex-shrink: 0;
    font-family: 'Noto Sans SC', sans-serif;
}

.comment-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.comment-header {
    display: flex;
    align-items: center;
    gap: 12px;
}

.comment-author {
    font-size: 14px;
    font-weight: 600;
    color: #2c1810;
    font-family: 'Noto Sans SC', sans-serif;
}

.comment-time {
    font-size: 12px;
    color: #8b4513;
    font-family: 'Noto Sans SC', sans-serif;
}

.comment-text {
    font-size: 14px;
    color: #5d4037;
    line-height: 1.6;
    font-family: 'Noto Sans SC', sans-serif;
}

.comment-pagination {
    padding: 16px 0;
    display: flex;
    justify-content: center;
    border-top: 1px solid rgba(212, 167, 106, 0.1);
    margin-top: 16px;
}

.comment-pagination :deep(.ant-pagination-item) {
    border-color: #d4a76a;
}

.comment-pagination :deep(.ant-pagination-item a) {
    color: #8b4513;
}

.comment-pagination :deep(.ant-pagination-item-active) {
    background: #8b4513;
    border-color: #8b4513;
}

.comment-pagination :deep(.ant-pagination-item-active a) {
    color: #fff;
}

/* 评论输入区域 */
.comment-input-section {
    padding: 20px 24px;
    border-top: 1px solid rgba(212, 167, 106, 0.15);
    background: rgba(252, 249, 244, 0.5);
    flex-shrink: 0;
}

.comment-input-wrapper {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
}

.input-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
    background: rgba(212, 167, 106, 0.2);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #2c1810;
    font-weight: 600;
    font-size: 14px;
    flex-shrink: 0;
    font-family: 'Noto Sans SC', sans-serif;
}

.comment-input {
    flex: 1;
}

.comment-input :deep(.ant-input) {
    border-color: #d4a76a;
    font-family: 'Noto Sans SC', sans-serif;
}

.comment-input :deep(.ant-input:focus) {
    border-color: #8b4513;
    box-shadow: 0 0 0 2px rgba(139, 69, 19, 0.1);
}

.comment-actions {
    display: flex;
    justify-content: flex-end;
}

.comment-actions :deep(.ant-btn-primary) {
    background: linear-gradient(135deg, #a0522d, #8b4513);
    border: none;
    font-family: 'Noto Sans SC', sans-serif;
}

.comment-actions :deep(.ant-btn-primary:hover) {
    background: linear-gradient(135deg, #8b4513, #654321);
}

/* 响应式设计 */
@media (max-width: 1024px) {
    .post-detail-container {
        flex-direction: column;
        height: auto;
        max-height: 90vh;
    }

    .post-image-section {
        flex: 0 0 40%;
        max-height: 300px;
    }

    .post-content-section {
        flex: 1;
        max-height: 60vh;
    }
}

@media (max-width: 768px) {
    .post-detail-modal :deep(.ant-modal) {
        width: 95% !important;
        max-width: 95% !important;
    }

    .post-detail-container {
        flex-direction: column;
        max-height: 85vh;
    }

    .post-image-section {
        flex: 0 0 200px;
    }

    .post-header {
        padding: 16px;
        flex-direction: column;
        align-items: flex-start;
    }

    .post-body {
        padding: 16px;
        max-height: 200px;
    }

    .comments-header {
        padding: 16px;
    }

    .comments-list {
        padding: 12px 16px;
    }

    .comment-input-section {
        padding: 16px;
    }
}
</style>

