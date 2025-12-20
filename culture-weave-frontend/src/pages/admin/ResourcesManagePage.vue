<template>
  <div id="" class="page">
    <section class="toolbar">
      <div class="grid">
        <label class="field">
          <span>关键词</span>
          <input v-model="filters.searchText" type="text" placeholder="名称 / 简介 / 发布者"
                 @input="onDebouncedSearch"/>
        </label>
        <label class="field">
          <span>分类</span>
          <select v-model="filters.category">
            <option value="">全部</option>
            <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
          </select>
        </label>
        <label class="field">
          <span>地域</span>
          <select v-model="filters.region">
            <option value="">全部</option>
            <option v-for="r in regions" :key="r" :value="r">{{ r }}</option>
          </select>
        </label>
        <label class="field">
          <span>发布者</span>
          <input v-model="filters.userName" type="text" placeholder="发布者用户名"/>
        </label>
        <label class="field wide">
          <span>标签（逗号 / 回车分隔）</span>
          <input v-model="filters.tagsText" type="text" placeholder="如：银饰，木雕，陶瓷"
                 @keydown.enter.prevent="commitTagsInput('filter')" @blur="commitTagsInput('filter')"/>
        </label>
      </div>
      <div class="actions">
        <button class="btn primary" @click="fetchList(1)">查询</button>
        <button class="btn" @click="onReset">重置</button>
        <button class="btn success" @click="openCreate">新增资源</button>
      </div>
    </section>

    <section class="card">
      <table class="table">
        <thead>
        <tr>
          <th style="width: 120px">图片</th>
          <th style="width: 220px">名称 / 简介</th>
          <th>分类</th>
          <th>标签</th>
          <th>地域</th>
          <th>价格</th>
          <th>发布者</th>
          <th style="width: 160px">创建时间</th>
          <th style="width: 140px">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="!loading && list.length === 0">
          <td colspan="9" class="empty">暂无数据</td>
        </tr>
        <tr v-for="row in list" :key="row.id">
          <td>
            <div class="thumb">
              <img
                v-if="row.resourceImgUrl"
                :src="row.resourceImgUrl"
                :alt="row.name || '封面图'"
                loading="lazy"
                @error="onThumbError($event)"
              />
              <div v-else class="thumb-placeholder">暂无图</div>
            </div>
          </td>

          <td>
            <div class="name">{{ row.name || '-' }}</div>
            <div class="intro">{{ row.introduction || '-' }}</div>
          </td>
          <td>{{ row.category || '-' }}</td>
          <td>
            <div class="tags">
              <span v-for="t in normalizeTags(row.tags)" :key="t" class="tag">{{ t }}</span>
            </div>
          </td>
          <td>{{ row.region || '-' }}</td>
          <td>
            <!-- 若标记 commercial 则显示整数价格，否则提示不可商用 -->
            <div v-if="row['commercial']">
              {{ formatPrice(row.price) }}
            </div>
            <div v-else class="small-muted">不可商用</div>
          </td>
          <td>{{ row.userName || '-' }}</td>
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

    <!-- 新增 / 编辑 弹窗 -->
    <dialog ref="dialogRef" class="dialog">
      <form method="dialog" class="dialog-body" @submit.prevent>
        <h3>{{ editMode ? '编辑资源' : '新增资源' }}</h3>
        <div class="form-grid">
          <label class="field">
            <span>名称</span>
            <input v-model="formModel.name" type="text" required placeholder="请输入资源名称"/>
          </label>
          <label class="field">
            <span>分类</span>
            <select v-model="formModel.category">
              <option value="">未分类</option>
              <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
            </select>
          </label>
          <label class="field">
            <span>地域</span>
            <select v-model="formModel.region">
              <option value="">未选择</option>
              <option v-for="r in regions" :key="r" :value="r">{{ r }}</option>
            </select>
          </label>

          <!-- 封面图片 与 预览 -->
          <label class="field">
            <span>封面图片</span>
            <input ref="fileInputRef" type="file" accept="image/*" @change="onImageChange"/>
            <small v-if="editMode" class="hint">如无需更换封面可留空</small>
            <div class="preview" v-if="imagePreview">
              <img :src="imagePreview" alt="预览" />
              <button type="button" class="btn" @click="clearImage">移除</button>
            </div>
            <div class="preview" v-else-if="editMode && formModel.resourceImgUrl">
              <img :src="formModel.resourceImgUrl" alt="当前封面" />
            </div>
          </label>

          <!-- 商用授权选择：只有勾选时才显示价格输入 -->
          <label class="field">
            <span>授权</span>
            <div style="display:flex;align-items:center;gap:12px;">
              <label style="display:flex;align-items:center;gap:6px;">
                <input type="checkbox" v-model="formModel.commercial" @change="onCommercialToggle" />
                <span>允许商用授权</span>
              </label>
            </div>
            <small class="hint">勾选后用户可购买商用授权并填写价格</small>
          </label>

          <!-- 价格输入：仅在 commercial 为 true 时显示；只允许整数 -->
          <label class="field" v-if="formModel.commercial">
            <span>价格（整数，单位：元）</span>
            <input
              :value="formModel.price === undefined || formModel.price === null ? '' : String(formModel.price)"
              @input="onPriceInput"
              type="number"
              inputmode="numeric"
              pattern="[0-9]*"
              step="1"
              min="0"
              placeholder="填写商用授权价格（整数）"
            />
            <small class="hint">价格必须为非负整数（不允许小数）</small>
          </label>

          <label class="field wide">
            <span>简介</span>
            <textarea v-model="formModel.introduction" rows="3" placeholder="一句话介绍"></textarea>
          </label>
          <label class="field wide">
            <span>标签（逗号 / 回车分隔）</span>
            <input v-model="tagsText" type="text" placeholder="如：银饰，木雕，陶瓷"
                   @keydown.enter.prevent="commitTagsInput('form')" @blur="commitTagsInput('form')"/>
          </label>
          <label class="field">
            <span>发布者</span>
            <input v-model="formModel.userName" type="text" placeholder="可选"/>
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
  getResourceCategoryList,
  listResourcesByPage,
  addResource,
  updateResource,
  deleteResource
} from '../../api/resourcesController'

type ResourceRow = {
  id?: number
  name?: string
  introduction?: string
  category?: string
  tags?: string | string[]
  region?: string
  userName?: string
  price?: number
  commercial?: boolean
  createTime?: string
  resourceImgUrl?: string
}

type ResourcePageData = {
  records?: ResourceRow[]
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

type ResourceFormModel = {
  id?: number
  name: string
  introduction: string
  category: string
  tags: string[]
  region: string
  userName: string
  price?: number
  commercial?: boolean
  resourceImgUrl?: string
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

// --- 路由 ---
const router = useRouter()

// --- 状态 ---
const loading = ref(false)
const list = ref<ResourceRow[]>([])
const total = ref(0)
const categories = ref<string[]>([])
const regions = ref<string[]>([])
const imageFile = ref<File | null>(null)

// 用于展示选中文件或现有图片的预览 URL
const imagePreview = ref<string | null>(null)

const filters = reactive<{
  searchText: string
  category: string
  region: string
  userName: string
  tagsText: string
}>({searchText: '', category: '', region: '', userName: '', tagsText: ''})

const pagination = reactive({current: 1, pageSize: 10})
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pagination.pageSize)))
const paginationTotalText = (t: number) => `共 ${t} 条`

// --- 工具 ---
let searchTimer: number | null = null

function onDebouncedSearch() {
  if (searchTimer) window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => fetchList(1), 400)
}

function normalizeTags(v: unknown): string[] {
  if (Array.isArray(v)) return v.map(item => (typeof item === 'string' ? item.trim() : '')).filter(Boolean)
  if (typeof v === 'string') return parseTagsText(v)
  return []
}

// 显示整数价格（不带小数）
function formatPrice(v?: number) {
  if (typeof v === 'number' && !Number.isNaN(v)) {
    return `¥ ${Math.trunc(v)}`
  }
  return '-'
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

function commitTagsInput(target: 'filter' | 'form') {
  if (target === 'filter') {
    filters.tagsText = stringifyTags(parseTagsText(filters.tagsText))
  } else {
    tagsText.value = stringifyTags(parseTagsText(tagsText.value))
  }
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
    const parsedTags = parseTagsText(filters.tagsText)
    const tags = parsedTags.length > 0 ? parsedTags : undefined

    const payload: ResourceQueryPayload = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'createTime',
      sortOrder: 'descend',
      searchText: filters.searchText || undefined,
      category: filters.category || undefined,
      region: filters.region || undefined,
      userName: filters.userName || undefined,
      tags
    }

    const res = await listResourcesByPage(payload)
    const responseData = res.data as ResourceResponse<ResourcePageData>
    if (responseData?.code === 0 && responseData.data) {
      const {records = [], total: totalCount = 0, current, size} = responseData.data
      // 确保后端可能返回的小数价格被处理为整数以保证一致性（向下取整）
      records.forEach(r => {
        if (typeof r.price === 'number' && !Number.isNaN(r.price)) {
          r.price = Math.trunc(r.price)
        }
      })
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
  filters.category = ''
  filters.region = ''
  filters.userName = ''
  filters.tagsText = ''
  pagination.current = 1
  pagination.pageSize = 10
  fetchList(1)
}

// 分页变更：保持与用户管理页一致的交互体验
function onPageChange(page: number) {
  pagination.current = page
  fetchList()
}

function onPageSizeChange(_page: number, pageSize: number) {
  pagination.pageSize = pageSize
  pagination.current = 1
  fetchList(1)
}

// --- 分类/地域 ---
async function loadMeta() {
  try {
    const res = await getResourceCategoryList()
    const responseData = res.data as ResourceResponse<ResourceCategoryMeta>
    if (responseData?.code === 0 && responseData.data) {
      categories.value = responseData.data.categoryList || []
      regions.value = responseData.data.regionList || []
    }
  } catch (e) {
    console.error('获取分类/地域失败', e)
  }
}

// --- 新增 / 编辑 ---
const dialogRef = ref<HTMLDialogElement | null>(null)
const saving = ref(false)
const editMode = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const formModel = reactive<ResourceFormModel>({
  id: undefined,
  name: '',
  introduction: '',
  category: '',
  tags: [],
  region: '',
  userName: '',
  price: undefined,
  commercial: false,
  resourceImgUrl: undefined
})
const tagsText = ref('')

function openCreate() {
  editMode.value = false
  Object.assign(formModel, {
    id: undefined,
    name: '',
    introduction: '',
    category: '',
    tags: [],
    region: '',
    userName: '',
    price: undefined,
    commercial: false,
    resourceImgUrl: undefined
  })
  tagsText.value = ''
  imageFile.value = null
  imagePreview.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
  dialogRef.value?.showModal()
}

function openDetail(row: ResourceRow) {
  if (row.id) {
    router.push(`/resources/${row.id}`)
  }
}

function openEdit(row: ResourceRow) {
  editMode.value = true
  Object.assign(formModel, {
    id: row.id,
    name: row.name || '',
    introduction: row.introduction || '',
    category: row.category || '',
    tags: normalizeTags(row.tags),
    region: row.region || '',
    userName: row.userName || '',
    price: typeof row.price === 'number' ? Math.trunc(row.price) : undefined,
    commercial: !!row.commercial,
    resourceImgUrl: row.resourceImgUrl
  })
  tagsText.value = stringifyTags(normalizeTags(row.tags))
  imageFile.value = null
  imagePreview.value = row.resourceImgUrl || null
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
  if (imagePreview.value) {
    try { URL.revokeObjectURL(imagePreview.value) } catch (e) { /* ignore */ }
  }
  if (file) {
    imagePreview.value = URL.createObjectURL(file)
  } else {
    imagePreview.value = null
  }
}

function clearImage() {
  imageFile.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
  if (imagePreview.value) {
    try { URL.revokeObjectURL(imagePreview.value) } catch (e) { /* ignore */ }
  }
  imagePreview.value = null
}

// 当用户切换商用授权时：若取消商用则清空 price
function onCommercialToggle() {
  if (!formModel.commercial) {
    formModel.price = undefined
  }
}

// 价格输入：强制为非负整数（向下取整），并同步回输入框
function onPriceInput(event: Event) {
  const target = event.target as HTMLInputElement
  const raw = target.value
  // 若为空则清空 price
  if (raw === '' || raw === null) {
    formModel.price = undefined
    return
  }
  // 尝试解析为数字
  const n = Number(raw)
  if (Number.isNaN(n)) {
    formModel.price = undefined
    target.value = ''
    return
  }
  // 向下取整并保证非负
  const intVal = Math.max(0, Math.floor(n))
  formModel.price = intVal
  // 将处理后的整数写回输入框（保证显示无小数）
  target.value = String(intVal)
}

async function onSubmit() {
  try {
    saving.value = true

    // 校验：若允许商用则价格为必须且必须为整数且 >= 0
    if (formModel.commercial) {
      if (formModel.price === undefined || formModel.price === null || Number.isNaN(formModel.price)) {
        alert('请选择商用授权后请填写价格（非空，整数）')
        saving.value = false
        return
      }
      if (!Number.isInteger(formModel.price) || formModel.price < 0) {
        alert('价格必须为非负整数')
        saving.value = false
        return
      }
    }

    const payload = {
      ...formModel,
      tags: parseTagsText(tagsText.value)
    }

    if (editMode.value) {
      // 更新：如果选择了新图片，就把 imageFile 作为第二参数传给 updateResource（后端若需 multipart）
      const res = await (imageFile.value ? updateResource(payload, imageFile.value) : updateResource(payload))
      const responseData = res.data as ResourceResponse<boolean>
      if (responseData?.code === 0) {
        closeDialog()
        fetchList()
      } else {
        alert(responseData?.message || '更新失败')
      }
    } else {
      if (!imageFile.value) {
        alert('请上传资源封面图片')
        saving.value = false
        return
      }
      const {id: _omitId, ...resourcesAddRequest} = payload
      const addPayload = {
        resourcesAddRequest
      }
      const res = await addResource(addPayload as unknown as Parameters<typeof addResource>[0], imageFile.value)
      const responseData = res.data as ResourceResponse<ResourceRow>
      if (responseData?.code === 0) {
        closeDialog()
        fetchList(1)
      } else {
        alert(responseData?.message || '新增失败')
      }
    }
  } catch (e) {
    console.error('保存失败', e)
    alert('保存失败')
  } finally {
    saving.value = false
  }
}

async function onDelete(row: ResourceRow) {
  if (!row.id) return
  const ok = confirm(`确认删除「${row.name || row.id}」吗？`)
  if (!ok) return
  try {
    const res = await deleteResource({id: row.id})
    const responseData = res.data as ResourceResponse<boolean>
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
  await loadMeta()
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
  min-height: 80px;
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
  max-height: 2.8em; /* 2行的高度，约等于 line-height * 2 */
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  font-size: 12px;
  background: #f5f5f5;
  border: 1px solid #eee;
  padding: 2px 6px;
  border-radius: 999px;
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
