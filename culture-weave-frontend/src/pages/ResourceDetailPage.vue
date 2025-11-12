<template>
    <div id="resourceDetailPage">
        {{ }}
        <a-card v-if="loading" class="loading-card">
            <a-skeleton active />
        </a-card>

        <a-card v-else-if="!resource" class="empty-card">
            <div class="empty-state">
                <PictureOutlined style="font-size:48px;color:#d3d9e6" />
                <p>资源不存在或已被删除</p>
            </div>
        </a-card>

        <a-card v-else class="detail-card">
            <div class="top">
                <div class="left">
                    <img v-if="resource.resourceImgUrl" :src="resource.resourceImgUrl" alt="封面" class="cover"
                        @error="onImageError" />
                    <div v-else class="cover-placeholder">
                        <PictureOutlined style="font-size:36px" />
                        <span>暂无封面</span>
                    </div>
                </div>

                <div class="right">
                    <h1 class="title">{{ resource.name || '未命名资源' }}</h1>

                    <div class="meta">
                        <div class="meta-line">
                            <UserOutlined />
                            <span class="author">{{ resource.userName || '匿名' }}</span>
                            <span class="dot">•</span>
                            <EnvironmentOutlined v-if="resource.region" />
                            <span v-if="resource.region">{{ resource.region }}</span>
                        </div>

                        <div class="meta-line">
                            <FolderOutlined v-if="resource.category" />
                            <span v-if="resource.category">{{ resource.category }}</span>
                            <span class="spacer"></span>
                            <div v-if="normalizeTags(resource.tags).length" class="tags">
                                <TagOutlined />
                                <span v-for="t in normalizeTags(resource.tags)" :key="t" class="tag-chip">{{ t }}</span>
                            </div>
                        </div>

                        <div class="price-area">
                            <div v-if="resource.price && resource.price > 0" class="price">
                                <DollarOutlined />
                                <span class="price-text">{{ formatPrice(resource.price) }}</span>
                                <span class="price-hint">（含商用授权）</span>
                            </div>
                            <div v-else class="free-hint">免费资源 / 仅限非商用</div>
                        </div>
                    </div>

                    <div class="cta-row">
                        <a-button type="primary" size="large" :disabled="!canDownload"
                            @click="onDownload">立即下载</a-button>
                        <a-button v-if="resource.price && resource.price > 0" size="large"
                            @click="onPurchase">购买授权</a-button>
                      <a-button v-if="isOwner" danger @click="openDelete">删除资源</a-button>
                        <a-button v-if="isOwner" @click="onEdit">编辑</a-button>
                    </div>
                </div>
            </div>

            <div class="content">
                <section class="introduction">
                    <h3>资源简介</h3>
                    <p v-if="resource.introduction">{{ resource.introduction }}</p>
                    <p v-else class="muted">暂无简介</p>
                </section>

                <section class="similar">
                    <div class="similar-head">
                        <h3>相似资源</h3>
                        <p v-if="resource?.category" class="similar-hint">
                            与 <strong>{{ resource.category }}</strong> 分类相关的更多灵感
                        </p>
                    </div>
                    <div v-if="similar.length" class="similar-gallery">
                        <article v-for="s in similar" :key="s.id" class="similar-tile" @click="goToDetail(s.id)">
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
                                    <span v-for="t in normalizeTags(s.tags)" :key="t" class="tile-tag">{{ t }}</span>
                                </div>
                            </div>
                        </article>
                    </div>
                    <div v-else class="muted">暂无相似资源</div>
                </section>
            </div>
        </a-card>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
    PictureOutlined,
    UserOutlined,
    EnvironmentOutlined,
    FolderOutlined,
    TagOutlined,
    DollarOutlined
} from '@ant-design/icons-vue'
import { getResourcesVoById, listResourcesVoByPage, deleteResource } from '../api/resourcesController'
import { useLoginUserStore } from '../stores/useLoginUserStore'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const resource = ref<API.ResourcesVO | null>(null)
const similar = ref<API.ResourcesVO[]>([])

// 获取当前登录用户ID
const currentUserId = computed(() => loginUserStore.loginUser?.id || null)
const isOwner = computed(() => {
    if (!currentUserId.value || !resource.value) return false
    return resource.value.userId === currentUserId.value
})
const canDownload = ref(true) // 简化权限判断，可根据实际业务逻辑调整
const deleteVisible = ref(false)
const deleting = ref(false)

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
}

.detail-card {
    max-width: 1800px;
    margin: 0 auto;
    padding: 0 8%;
}

.top {
    display: flex;
    gap: 24px;
}

.left {
    width: 360px;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.cover {
    width: 100%;
    height: 360px;
    object-fit: cover;
    border-radius: 12px;
}

.cover-placeholder {
    width: 100%;
    height: 360px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: linear-gradient(135deg, #f5f7fb, #e8ecf4);
    border-radius: 12px;
    color: #9aa4b8;
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

.right {
    flex: 1;
}

.title {
    margin: 0 0 12px 0;
    font-size: 26px;
}

.meta {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 16px;
}

.meta-line {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #6a7a99;
}

.price-area {
    margin-top: 6px;
}

.price {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    color: #ff6b95;
    font-weight: 600;
}

.price-hint {
    font-size: 12px;
    color: #9aa4b8;
    margin-left: 8px;
}

.cta-row {
    margin-top: 18px;
    display: flex;
    gap: 12px;
    align-items: center;
}

.content {
    margin-top: 24px;
}

.introduction {
    background: #fff;
    padding: 16px;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(15, 35, 95, 0.04);
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
    color: #1a2332;
}

.similar-hint {
    margin: 0;
    font-size: 13px;
    color: #6a7a99;
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
    color: #1a2332;
    line-height: 1.4;
}

.tile-meta {
    display: flex;
    gap: 8px;
    align-items: center;
    font-size: 13px;
    color: #6a7a99;
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
    padding: 4px 8px;
    background: rgba(61, 107, 255, 0.08);
    color: #3d6bff;
    border-radius: 10px;
    margin-right: 6px
}

@media (max-width: 900px) {
    .top {
        flex-direction: column
    }

    .left {
        width: 100%
    }

    .cover {
        height: 260px
    }
}
</style>
