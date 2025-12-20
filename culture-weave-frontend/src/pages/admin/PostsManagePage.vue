<template>
  <div id="" class="page">
    <section class="toolbar">
      <div class="grid">
        <label class="field">
          <span>关键词</span>
          <input v-model="filters.searchText" type="text" placeholder="标题 / 内容"
                 @input="onDebouncedSearch"/>
        </label>
      </div>
      <div class="actions">
        <button class="btn primary" @click="fetchList(1)">查询</button>
        <button class="btn" @click="onReset">重置</button>
      </div>
    </section>

    <section class="card">
      <table class="table">
        <thead>
        <tr>
          <th style="width: 120px">图片</th>
          <th style="width: 300px">标题 / 内容</th>
          <th>发布者</th>
          <th style="width: 100px">授权</th>
          <th style="width: 100px">状态</th>
          <th style="width: 160px">创建时间</th>
          <th style="width: 180px">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="!loading && list.length === 0">
          <td colspan="7" class="empty">暂无数据</td>
        </tr>
        <tr v-for="row in list" :key="row.id">
          <td>
            <div class="thumb">
              <img
                v-if="row.pictureUrl"
                :src="row.pictureUrl"
                :alt="row.title || '封面图'"
                loading="lazy"
                @error="onThumbError($event)"
              />
              <div v-else class="thumb-placeholder">暂无图</div>
            </div>
          </td>

          <td>
            <div class="name">{{ row.title || '-' }}</div>
            <div class="intro">{{ row.content || '-' }}</div>
          </td>
          <td>{{ row.userVO?.userName || '-' }}</td>
          <td>
            <div v-if="row.authorization === 0" class="small-muted">公开</div>
            <div v-else-if="row.authorization === 1" class="small-muted">私有</div>
            <div v-else class="small-muted">-</div>
          </td>
          <td>
            <div v-if="row.statement === 0" class="small-muted">正常</div>
            <div v-else-if="row.statement === 1" class="small-muted danger">已删除</div>
            <div v-else class="small-muted">-</div>
          </td>
          <td>{{ formatDateTime(row.createTime) }}</td>
          <td>
            <div class="row-actions">
              <button class="link" @click="openDetail(row)">详情</button>
              <button class="link" @click="openEdit(row)">编辑</button>
              <button class="link danger" @click="onDelete(row)">删除</button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>

      <div class="pagination" v-if="total > 0">
        <a-pagination
          :current="pagination.current"
          :page-size="pagination.pageSize"
          :total="total"
          :show-size-changer="true"
          :show-quick-jumper="true"
          :page-size-options="['10','20','50']"
          :show-total="paginationTotalText"
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </div>
    </section>

    <!-- 帖子详情弹窗 -->
    <PostDetailModal v-model:open="detailModalOpen" :post-id="selectedPostId" @close="handleDetailModalClose" />

    <!-- 编辑 弹窗 -->
    <dialog ref="dialogRef" class="dialog">
      <form method="dialog" class="dialog-body" @submit.prevent>
        <h3>编辑帖子</h3>
        <div class="form-grid">
          <label class="field wide">
            <span>标题</span>
            <input v-model="formModel.title" type="text" required placeholder="请输入帖子标题"/>
          </label>

          <label class="field wide">
            <span>内容</span>
            <textarea v-model="formModel.content" rows="6" placeholder="请输入帖子内容"></textarea>
          </label>

          <!-- 封面图片 与 预览 -->
          <label class="field">
            <span>封面图片</span>
            <input ref="fileInputRef" type="file" accept="image/*" @change="onImageChange"/>
            <small class="hint">如无需更换封面可留空</small>
            <div class="preview" v-if="imagePreview">
              <img :src="imagePreview" alt="预览" />
              <button type="button" class="btn" @click="clearImage">移除</button>
            </div>
            <div class="preview" v-else-if="formModel.imageUrl">
              <img :src="formModel.imageUrl" alt="当前封面" />
            </div>
          </label>

          <label class="field">
            <span>授权</span>
            <select v-model="formModel.authorization">
              <option :value="0">公开</option>
              <option :value="1">私有</option>
            </select>
          </label>

          <label class="field">
            <span>状态</span>
            <select v-model="formModel.statement">
              <option :value="0">正常</option>
              <option :value="1">已删除</option>
            </select>
          </label>
        </div>
        <div class="dialog-actions">
          <button class="btn" @click="closeDialog">取消</button>
          <button class="btn primary" :disabled="saving" @click="onSubmit">{{ saving ? '保存中…' : '保存' }}</button>
        </div>
      </form>
    </dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref, computed} from 'vue'
import {useRouter} from 'vue-router'
import {
  listResourcesVoByPage1,
  updateResource1,
  deletePost
} from '../../api/postController'
import PostDetailModal from '../../components/PostDetailModal.vue'

type PostRow = {
  id?: number
  pictureUrl?: string
  title?: string
  content?: string
  userId?: number
  authorization?: number
  statement?: number
  createTime?: string
  userVO?: {
    userName?: string
    userAvatar?: string
  }
}

type PostPageData = {
  records?: PostRow[]
  total?: number
  size?: number
  current?: number
}

type PostResponse<T> = {
  code?: number
  data?: T
  message?: string
}

type PostFormModel = {
  id?: number
  title: string
  content: string
  imageUrl?: string
  authorization?: number
  statement?: number
}

type PostQueryPayload = {
  current?: number
  pageSize?: number
  sortField?: string
  sortOrder?: string
  searchText?: string
  title?: string
  content?: string
}

// --- 路由 ---
const router = useRouter()

// --- 状态 ---
const loading = ref(false)
const list = ref<PostRow[]>([])
const total = ref(0)
const imageFile = ref<File | null>(null)
const detailModalOpen = ref(false)
const selectedPostId = ref<number | undefined>(undefined)

// 用于展示选中文件或现有图片的预览 URL
const imagePreview = ref<string | null>(null)

const filters = reactive<{
  searchText: string
}>({searchText: ''})

const pagination = reactive({current: 1, pageSize: 10})
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pagination.pageSize)))
const paginationTotalText = (t: number) => `共 ${t} 条`

// --- 工具 ---
let searchTimer: number | null = null

function onDebouncedSearch() {
  if (searchTimer) window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => fetchList(1), 400)
}

/** 按北京时间 (Asia/Shanghai) 格式化为 YYYY-MM-DD HH:mm:ss */
function formatDateTime(v?: string | number | Date | null): string {
  if (v === undefined || v === null || v === '') return '-'

  const d = v instanceof Date ? v : new Date(v as string | number)
  if (Number.isNaN(d.getTime())) return '-'

  const tz = 'Asia/Shanghai'
  const fmt = new Intl.DateTimeFormat('en-GB', {
    timeZone: tz,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
  const parts = fmt.formatToParts(d)
  const map: Record<string, string> = {}
  for (const p of parts) {
    if (p.type !== 'literal') map[p.type] = p.value
  }
  if (!map.year || !map.month || !map.day) return '-'
  return `${map.year}-${map.month}-${map.day} ${map.hour || '00'}:${map.minute || '00'}:${map.second || '00'}`
}

// --- 列表查询 ---
async function fetchList(page?: number) {
  loading.value = true
  try {
    if (page) pagination.current = page

    const payload: PostQueryPayload = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'createTime',
      sortOrder: 'descend',
      searchText: filters.searchText || undefined,
    }

    const res = await listResourcesVoByPage1(payload)
    const responseData = res.data as PostResponse<PostPageData>
    if (responseData?.code === 0 && responseData.data) {
      const {records = [], total: totalCount = 0, current, size} = responseData.data
      list.value = records
      total.value = totalCount
      pagination.current = current || pagination.current
      pagination.pageSize = size || pagination.pageSize
    } else {
      console.warn('加载失败', responseData?.message)
    }
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

function onReset() {
  filters.searchText = ''
  pagination.current = 1
  pagination.pageSize = 10
  fetchList(1)
}

// 分页变更
function onPageChange(page: number) {
  pagination.current = page
  fetchList()
}

function onPageSizeChange(_page: number, pageSize: number) {
  pagination.pageSize = pageSize
  pagination.current = 1
  fetchList(1)
}

// --- 编辑 ---
const dialogRef = ref<HTMLDialogElement | null>(null)
const saving = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const formModel = reactive<PostFormModel>({
  id: undefined,
  title: '',
  content: '',
  imageUrl: undefined,
  authorization: 0,
  statement: 0
})

function openDetail(row: PostRow) {
  if (row.id) {
    selectedPostId.value = row.id
    detailModalOpen.value = true
  }
}

function handleDetailModalClose() {
  detailModalOpen.value = false
  selectedPostId.value = undefined
}

function openEdit(row: PostRow) {
  Object.assign(formModel, {
    id: row.id,
    title: row.title || '',
    content: row.content || '',
    imageUrl: row.pictureUrl,
    authorization: row.authorization ?? 0,
    statement: row.statement ?? 0
  })
  imageFile.value = null
  imagePreview.value = row.pictureUrl || null
  if (fileInputRef.value) fileInputRef.value.value = ''
  dialogRef.value?.showModal()
}

function closeDialog() {
  dialogRef.value?.close()
}

// 选择图片时创建临时预览 URL
function onImageChange(event: Event) {
  const target = event.target as HTMLInputElement | null
  const files = target?.files
  const file = files && files.length > 0 ? files[0] : null
  imageFile.value = file
  if (imagePreview.value && imagePreview.value.startsWith('blob:')) {
    try { URL.revokeObjectURL(imagePreview.value) } catch (e) { /* ignore */ }
  }
  if (file) {
    imagePreview.value = URL.createObjectURL(file)
  } else {
    imagePreview.value = formModel.imageUrl || null
  }
}

function clearImage() {
  imageFile.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
  if (imagePreview.value && imagePreview.value.startsWith('blob:')) {
    try { URL.revokeObjectURL(imagePreview.value) } catch (e) { /* ignore */ }
  }
  imagePreview.value = formModel.imageUrl || null
}

async function onSubmit() {
  try {
    saving.value = true

    const payload = {
      postUpdateRequest: {
        id: formModel.id,
        title: formModel.title,
        content: formModel.content,
        imageUrl: formModel.imageUrl,
        authorization: formModel.authorization,
        statement: formModel.statement
      }
    }

    const res = await updateResource1(payload)
    const responseData = res.data as PostResponse<boolean>
    if (responseData?.code === 0) {
      closeDialog()
      fetchList()
    } else {
      alert(responseData?.message || '更新失败')
    }
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败')
  } finally {
    saving.value = false
  }
}

async function onDelete(row: PostRow) {
  if (!row.id) return
  const ok = confirm(`确认删除「${row.title || row.id}」吗？`)
  if (!ok) return
  try {
    const res = await deletePost({id: row.id})
    const responseData = res.data as PostResponse<boolean>
    if (responseData?.code === 0) {
      fetchList()
    } else {
      alert(responseData?.message || '删除失败')
    }
  } catch (e) {
    console.error('删除失败', e)
    alert('删除失败')
  }
}

// 当缩略图加载失败时显示占位
function onThumbError(e: Event) {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
  const placeholder = img.parentElement?.querySelector('.thumb-placeholder') as HTMLElement | null
  if (placeholder) placeholder.style.display = 'flex'
}

onMounted(async () => {
  await fetchList(1)
})
</script>

<style scoped>
.page {
  padding: 16px 8%;
  max-width: 1800px;
  margin: 0 auto;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .05);
}

.toolbar {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .05);
  margin-bottom: 12px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field.wide {
  grid-column: 1 / -1;
}

.field > span {
  font-size: 12px;
  color: #666;
}

.field > input,
.field > select,
.field > textarea {
  height: 36px;
  padding: 6px 10px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  outline: none;
  font-size: 14px;
}

.field small.hint {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.field > textarea {
  height: auto;
  resize: vertical;
  min-height: 120px;
}

.actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  width: 100%;
  justify-content: flex-end;
}

.btn {
  height: 34px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  background: #fff;
  cursor: pointer;
}

.btn.primary {
  background: #1677ff;
  color: #fff;
  border-color: #1677ff;
}

.btn.success {
  background: #00b578;
  color: #fff;
  border-color: #00b578;
}

.btn:disabled {
  opacity: .6;
  cursor: not-allowed;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  text-align: left;
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: top;
}

.name {
  font-weight: 600;
}

.intro {
  font-size: 12px;
  color: #888;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  max-height: 2.8em;
}

.row-actions {
  display: flex;
  gap: 8px;
}

.link {
  background: transparent;
  border: none;
  color: #1677ff;
  cursor: pointer;
  padding: 0;
}

.link.danger {
  color: #ff4d4f;
}

.empty {
  text-align: center;
  color: #999;
  padding: 24px 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 12px;
  gap: 12px;
}

/* 缩略图样式 */
.thumb {
  width: 88px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border-radius: 8px;
  overflow: hidden;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.thumb-placeholder {
  display: none;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
}

/* 弹窗内的预览样式 */
.preview {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  align-items: center;
}
.preview img {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}

/* dialog */
.dialog {
  border: none;
  border-radius: 12px;
  padding: 0;
  width: 720px;
  max-width: 92vw;
}

.dialog::backdrop {
  background: rgba(0, 0, 0, .35);
}

.dialog-body {
  padding: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

.small-muted {
  color: #999;
  font-size: 12px;
}

.small-muted.danger {
  color: #ff4d4f;
}

@media (max-width: 960px) {

  .grid,
  .form-grid {
    grid-template-columns: 1fr;
  }
  /* 缩略图在窄屏时调整大小 */
  .thumb {
    width: 64px;
    height: 48px;
  }
  .preview img {
    width: 96px;
    height: 64px;
  }
}
</style>

