<template>
    <div id="addResourcePage">
        <a-card class="form-card">
            <template #title>
                <div class="card-header">
                    <ArrowLeftOutlined @click="goBack" class="back-icon" />
                    <span>{{ isEditMode ? '编辑非遗资源' : '添加非遗资源' }}</span>
                </div>
            </template>

            <a-form :model="formModel" :rules="rules" ref="formRef" layout="vertical" @finish="onSubmit">

                <a-col :xs="24">
                    <a-form-item label="封面图片" name="imageFile" required>
                        <div class="upload-section">
                            <a-upload v-model:file-list="fileList" :before-upload="beforeUpload" :max-count="1"
                                list-type="picture-card" accept="image/*" @remove="handleRemove"
                                :show-upload-list="{ showPreviewIcon: false }">
                                <div v-if="fileList.length < 1">
                                    <PlusOutlined />
                                    <div style="margin-top: 8px">上传封面</div>
                                </div>
                            </a-upload>
                            <div v-if="imagePreview" class="image-preview">
                                <img :src="imagePreview" alt="预览" />
                            </div>
                        </div>
                        <div class="form-hint">请上传资源封面图片（支持 JPG、PNG 等格式）</div>
                    </a-form-item>
                </a-col>
                <a-row :gutter="24">
                    <a-col :xs="24" :md="12">
                        <a-form-item label="非遗名称" name="name" required>
                            <a-input v-model:value="formModel.name" placeholder="请输入非遗名称" size="large" />
                        </a-form-item>
                    </a-col>

                    <a-col :xs="24" :md="12">
                        <a-form-item label="分类" name="category">
                            <a-select v-model:value="formModel.category" placeholder="请选择分类" size="large" allow-clear>
                                <a-select-option value="">未分类</a-select-option>
                                <a-select-option v-for="c in categories" :key="c" :value="c">{{ c }}</a-select-option>
                            </a-select>
                        </a-form-item>
                    </a-col>

                    <a-col :xs="24" :md="12">
                        <a-form-item label="地域" name="region">
                            <a-select v-model:value="formModel.region" placeholder="请选择地域" size="large" allow-clear>
                                <a-select-option value="">未选择</a-select-option>
                                <a-select-option v-for="r in regions" :key="r" :value="r">{{ r }}</a-select-option>
                            </a-select>
                        </a-form-item>
                    </a-col>

                    <a-col :xs="24" :md="12">
                        <a-form-item label="非遗传承人" name="userName">
                            <a-input v-model:value="formModel.userName" placeholder="可选，留空则使用当前登录用户" size="large" />
                        </a-form-item>
                    </a-col>


                    <a-col :xs="24">
                        <a-form-item label="非遗传承人简介" name="userDescription">
                            <a-textarea v-model:value="formModel.userDescription" placeholder="可选，介绍传承人的经历、荣誉、传承故事等"
                                :rows="3" :maxlength="300" show-count />
                        </a-form-item>
                    </a-col>

                    <a-col :xs="24">
                        <a-form-item label="非遗简介" name="introduction">
                            <a-textarea v-model:value="formModel.introduction" placeholder="请介绍一下这个非遗的来源、特点、传承方式等"
                                :rows="4" :maxlength="500" show-count />
                        </a-form-item>
                    </a-col>

                    <a-col :xs="24">
                        <a-form-item label="标签" name="tags">
                            <a-input v-model:value="tagsText" placeholder="输入标签，用逗号、回车或空格分隔，如：银饰，木雕，陶瓷" size="large"
                                @keydown.enter.prevent="commitTagsInput" @blur="commitTagsInput" />
                            <div class="form-hint">多个标签用逗号、回车或空格分隔</div>
                            <div v-if="formModel.tags.length > 0" class="tags-display">
                                <a-tag v-for="tag in formModel.tags" :key="tag" closable @close="removeTag(tag)"
                                    color="blue">
                                    {{ tag }}
                                </a-tag>
                            </div>
                        </a-form-item>
                    </a-col>

                    <a-col :xs="24">
                        <a-form-item label="商用授权">
                            <a-checkbox v-model:checked="formModel.commercial" @change="onCommercialToggle">
                                允许商用授权
                            </a-checkbox>
                            <div class="form-hint">勾选后用户可购买商用授权</div>
                        </a-form-item>
                    </a-col>

                    <a-col :xs="24" v-if="formModel.commercial">
                        <a-form-item label="价格（元）" name="price" :rules="priceRules">
                            <a-input-number v-model:value="formModel.price" placeholder="填写商用授权价格" :min="0"
                                :precision="0" :step="1" style="width: 100%" size="large" />
                            <div class="form-hint">价格必须为非负整数（不允许小数）</div>
                        </a-form-item>
                    </a-col>
                </a-row>

                <a-form-item class="form-actions">
                    <a-space size="large">
                        <a-button size="large" @click="goBack">取消</a-button>
                        <a-button type="primary" size="large" html-type="submit" :loading="saving">
                            {{ saving ? '提交中...' : (isEditMode ? '保存修改' : '提交') }}
                        </a-button>
                    </a-space>
                </a-form-item>
            </a-form>
        </a-card>
    </div>
</template>

<script setup lang="ts">
/// <reference path="../api/typings.d.ts" />
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { ArrowLeftOutlined, PlusOutlined } from '@ant-design/icons-vue'
import type { UploadProps, UploadFile } from 'ant-design-vue'
import { addResource, getResourceCategoryList, getResourcesVoById, updateResource } from '../api/resourcesController'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const saving = ref(false)
const fileList = ref<UploadFile[]>([])
const imageFile = ref<File | null>(null)
const imagePreview = ref<string | null>(null)
const categories = ref<string[]>([])
const regions = ref<string[]>([])
const tagsText = ref('')
const hasExistingCover = ref(false)

const editId = computed(() => {
    const p = route.params?.id as string | undefined
    const q = (route.query?.id as string | undefined) || undefined
    return p || q || ''
})

const isEditMode = computed(() => !!editId.value)

const formModel = reactive<{
    name: string
    introduction: string
    category: string
    tags: string[]
    region: string
    userName: string
    userDescription: string
    price?: number
    commercial: boolean
    imageFile?: File | null
}>({
    name: '',
    introduction: '',
    category: '',
    tags: [],
    region: '',
    userName: '',
    userDescription: '',
    price: undefined,
    commercial: false,
    imageFile: undefined
})

// 表单验证规则
const rules = computed(() => {
    const base = {
        name: [{ required: true, message: '请输入资源名称', trigger: 'blur' }]
    } as any
    if (!isEditMode.value && !hasExistingCover.value) {
        base.imageFile = [{ required: true, message: '请上传封面图片', trigger: 'change' }]
    }
    return base
})

const priceRules = computed(() => {
    if (!formModel.commercial) return []
    return [
        { required: true, message: '请填写商用授权价格', trigger: 'blur' },
        { type: 'number', min: 0, message: '价格必须为非负整数', trigger: 'blur' },
        {
            validator: (_rule: any, value: number) => {
                if (value !== undefined && value !== null && !Number.isInteger(value)) {
                    return Promise.reject('价格必须为整数（不允许小数）')
                }
                return Promise.resolve()
            },
            trigger: 'blur'
        }
    ]
})

// 标签输入处理
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
    const parsed = parseTagsText(tagsText.value)
    formModel.tags = parsed
    tagsText.value = stringifyTags(parsed)
}

function removeTag(tag: string) {
    formModel.tags = formModel.tags.filter(t => t !== tag)
    tagsText.value = stringifyTags(formModel.tags)
}

function onCommercialToggle() {
    if (!formModel.commercial) {
        formModel.price = undefined
    }
}

// 图片上传逻辑
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    const isImage = file.type.startsWith('image/')
    if (!isImage) {
        message.error('只能上传图片文件！')
        return false
    }
    const isLt10M = file.size / 1024 / 1024 < 10
    if (!isLt10M) {
        message.error('图片大小不能超过 10MB！')
        return false
    }

    // 同步更新
    const uploadFile = file as UploadFile
    fileList.value = [uploadFile]
    imageFile.value = file
    formModel.imageFile = file

    // 创建预览
    const reader = new FileReader()
    reader.onload = (e) => {
        imagePreview.value = e.target?.result as string
    }
    reader.readAsDataURL(file)

    // 阻止自动上传
    return false
}

function handleRemove() {
    imageFile.value = null
    imagePreview.value = null
    fileList.value = []
    formModel.imageFile = undefined
}

// 加载分类与地域
async function loadMeta() {
    try {
        const res = await getResourceCategoryList()
        const responseData = (res as any)?.data
        if (responseData?.code === 0 && responseData?.data) {
            categories.value = responseData.data.categoryList || []
            regions.value = responseData.data.regionList || []
        } else if (responseData?.data?.data) {
            categories.value = responseData.data.data.categoryList || []
            regions.value = responseData.data.data.regionList || []
        } else {
            categories.value = (res as any)?.data?.categoryList || []
            regions.value = (res as any)?.data?.regionList || []
        }
    } catch (e) {
        console.error('获取分类/地域失败', e)
        message.error('获取分类/地域失败')
    }
}

// 表单提交
async function onSubmit() {
    if (!formRef.value) return

    try {
        await formRef.value.validate()
    } catch (e) {
        console.error('表单验证失败', e)
        return
    }

    // 编辑模式下允许不更换封面；新增模式必须有封面
    if (!isEditMode.value) {
        if (!imageFile.value) {
            message.error('请上传资源封面图片')
            return
        }
    }

    if (formModel.commercial) {
        if (formModel.price === undefined || formModel.price === null || Number.isNaN(formModel.price)) {
            message.error('请填写商用授权价格')
            return
        }
        if (!Number.isInteger(formModel.price) || formModel.price < 0) {
            message.error('价格必须为非负整数')
            return
        }
    }

    saving.value = true
    try {
        const payload = {
            name: formModel.name,
            introduction: formModel.introduction || '',
            category: formModel.category || undefined,
            region: formModel.region || undefined,
            userName: formModel.userName || undefined,
            userDescription: formModel.userDescription || undefined,
            tags: formModel.tags.length > 0 ? formModel.tags : undefined,
            price: formModel.commercial && formModel.price !== undefined ? Math.trunc(formModel.price) : undefined
        }

        if (isEditMode.value) {
            // 更新资源（不处理封面更换；如需更换，需后端提供带文件的更新接口）
            const updateBody: any = {
                id: editId.value,
                ...payload
            }
            const res = await updateResource(updateBody)
            const responseData = (res as any)?.data
            if (responseData?.code === 0 && responseData?.data) {
                message.success('资源更新成功！')
                router.push(`/resources/${String(editId.value)}`)
            } else {
                message.error(responseData?.message || '更新资源失败')
            }
        } else {
            const resourcesAddRequest = payload
            const addPayload = { resourcesAddRequest }

            const res = await addResource(addPayload as any, imageFile.value as File)
            const responseData = (res as any)?.data

            if (responseData?.code === 0) {
                message.success('资源添加成功！')
                router.push('/resources')
            } else {
                message.error(responseData?.message || '添加资源失败')
            }
        }
    } catch (e: any) {
        console.error('提交失败', e)
        message.error(e?.message || '提交失败，请稍后重试')
    } finally {
        saving.value = false
    }
}

function goBack() {
    router.back()
}

async function loadForEdit() {
    if (!isEditMode.value) return
    try {
        const res = await getResourcesVoById({ id: editId.value as any })
        const responseData = (res as any)?.data as API.BaseResponseResourcesVO
        if (responseData?.code === 0 && responseData?.data) {
            const r = responseData.data
            formModel.name = r.name || ''
            formModel.introduction = r.introduction || ''
            formModel.category = r.category || ''
            formModel.region = r.region || ''
            formModel.userName = r.userName || ''
            formModel.userDescription = ((r as any)?.userDescription as string | undefined) || ''

            const rawTags = (r as any)?.tags
            formModel.tags = Array.isArray(rawTags)
                ? (rawTags as any[]).map(v => String(v))
                : (typeof rawTags === 'string' ? rawTags.split(/[，,、;；\s\n\r\t]+/).filter(Boolean) : [])
            tagsText.value = formModel.tags.join('， ')
            if (typeof r.price === 'number' && r.price > 0) {
                formModel.commercial = true
                formModel.price = Math.trunc(r.price)
            } else {
                formModel.commercial = false
                formModel.price = undefined
            }
            if (r.resourceImgUrl) {
                imagePreview.value = r.resourceImgUrl
                hasExistingCover.value = true
            } else {
                imagePreview.value = null
                hasExistingCover.value = false
            }
        }
    } catch (e) {
        console.error('加载资源失败', e)
    }
}

onMounted(async () => {
    await loadMeta()
    await loadForEdit()
})
</script>

<style scoped>
#addResourcePage {
    min-height: calc(100vh - 200px);
    padding: 24px 8%;
    background: #f5f7fb;
}

.form-card {
    max-width: 1800px;
    margin: 0 auto;
}

.card-header {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 20px;
    font-weight: 600;
}

.back-icon {
    cursor: pointer;
    color: #3d6bff;
    font-size: 18px;
    transition: all 0.2s ease;
}

.back-icon:hover {
    color: #2d5ae6;
    transform: translateX(-2px);
}

.upload-section {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.image-preview {
    margin-top: 12px;
}

.image-preview img {
    max-width: 200px;
    max-height: 200px;
    border-radius: 8px;
    object-fit: cover;
    border: 1px solid #e8e8e8;
}

.form-hint {
    margin-top: 4px;
    font-size: 12px;
    color: #9aa4b8;
}

.tags-display {
    margin-top: 12px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.form-actions {
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid #f0f0f0;
    text-align: center;
}

:deep(.ant-upload-select-picture-card) {
    width: 120px;
    height: 120px;
}

:deep(.ant-form-item-label > label) {
    font-weight: 500;
    color: #4f5b76;
}

@media (max-width: 768px) {
    #addResourcePage {
        padding: 16px 8%;
    }

    .form-actions {
        margin-top: 24px;
    }

    .form-actions :deep(.ant-space) {
        width: 100%;
        justify-content: center;
    }

    .form-actions :deep(.ant-btn) {
        flex: 1;
        max-width: 200px;
    }
}
</style>
