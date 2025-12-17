<template>
    <div id="createPostPage">
        <div class="page-container">
            <a-card class="form-card">
                <template #title>
                    <div class="card-header">
                        <ArrowLeftOutlined class="back-icon" @click="goBack" />
                        <div class="header-text">
                            <h2>发布灵感</h2>
                            <p>记录非遗创作、灵感火花或传承故事，与社区同好分享。</p>
                        </div>
                    </div>
                </template>

                <a-form ref="formRef" :model="formModel" layout="vertical">
                    <a-form-item label="标题" name="title"
                        :rules="[{ required: true, message: '请输入创作标题', trigger: 'blur' }]">
                        <a-input v-model:value="formModel.title" placeholder="给创作取一个吸引人的标题吧" size="large" />
                    </a-form-item>

                    <a-form-item label="封面图片" required>
                        <div class="upload-section">
                            <a-upload v-model:file-list="coverList" :before-upload="beforeCoverUpload" :max-count="1"
                                list-type="picture-card" accept="image/*" @remove="handleCoverRemove"
                                :show-upload-list="{ showPreviewIcon: false }">
                                <div v-if="coverList.length < 1">
                                    <PlusOutlined />
                                    <div style="margin-top: 8px">上传封面</div>
                                </div>
                            </a-upload>
                            <div v-if="coverPreview" class="image-preview">
                                <img :src="coverPreview" alt="封面预览" />
                            </div>
                        </div>
                        <div class="form-hint">支持 JPG / PNG 格式，大小不超过 5MB</div>
                    </a-form-item>

                    <a-form-item label="正文" name="content"
                        :rules="[{ required: true, message: '请输入创作正文', trigger: 'blur' }]">
                        <a-textarea v-model:value="formModel.content" :rows="6" :maxlength="1000" show-count
                            placeholder="记录灵感来源、创作过程或对非遗的独特理解..." />
                    </a-form-item>

                    <div class="form-row">
                        <a-form-item label="是否为商业创作">
                            <a-switch v-model:checked="formModel.isCommercial" checked-children="是"
                                un-checked-children="否" />
                        </a-form-item>

                        <a-form-item label="授权状态" v-if="formModel.isCommercial">
                            <a-radio-group v-model:value="formModel.authorization">
                                <a-radio :value="1">未授权</a-radio>
                                <a-radio :value="2">已授权</a-radio>
                            </a-radio-group>
                        </a-form-item>
                    </div>

                    <a-form-item label="内容声明" required>
                        <a-radio-group v-model:value="formModel.statement">
                            <a-radio :value="1">原创</a-radio>
                            <a-radio :value="2">转载</a-radio>
                        </a-radio-group>
                    </a-form-item>

                    <a-form-item>
                        <div class="form-actions">
                            <a-button size="large" @click="goBack">取消</a-button>
                            <a-button type="primary" size="large" @click="onSubmit" :loading="saving">
                                {{ saving ? '发布中...' : '立即发布' }}
                            </a-button>
                        </div>
                    </a-form-item>
                </a-form>
            </a-card>
        </div>
    </div>
</template>

<script setup lang="ts">
/// <reference path="../api/typings.d.ts" />
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance, UploadFile, UploadProps } from 'ant-design-vue'
import { ArrowLeftOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { addPost } from '../api/postController'
import { useLoginUserStore } from '../stores/useLoginUserStore'

type PostAddRequest = API.PostAddRequest

const router = useRouter()
const loginUserStore = useLoginUserStore()
const currentUserId = computed(() => loginUserStore.loginUser?.id || null)

const formRef = ref<FormInstance>()
const saving = ref(false)

const formModel = reactive<{
    title: string
    content: string
    isCommercial: boolean
    authorization: number
    statement: number
}>({
    title: '',
    content: '',
    isCommercial: false,
    authorization: 0,
    statement: 1
})

const coverList = ref<UploadFile[]>([])
const coverFile = ref<File | null>(null)
const coverPreview = ref<string | null>(null)

const beforeCoverUpload: UploadProps['beforeUpload'] = (file) => {
    const isImage = file.type.startsWith('image/')
    if (!isImage) {
        message.error('只能上传图片文件作为封面')
        return false
    }
    const isLt5M = file.size / 1024 / 1024 <= 5
    if (!isLt5M) {
        message.error('封面图片大小需小于 5MB')
        return false
    }

    const uploadFile = file as UploadFile
    coverList.value = [uploadFile]
    coverFile.value = file

    const reader = new FileReader()
    reader.onload = (e) => {
        coverPreview.value = e.target?.result as string
    }
    reader.readAsDataURL(file)

    return false
}

function handleCoverRemove() {
    coverList.value = []
    coverFile.value = null
    coverPreview.value = null
}

function goBack() {
    router.back()
}

async function ensureLogin() {
    if (!currentUserId.value) {
        await loginUserStore.fetchLoginUser()
    }
    if (!currentUserId.value) {
        message.warning('请先登录后再发布创作')
        router.push('/user/login')
        return false
    }
    return true
}

async function onSubmit() {
    if (!(await ensureLogin())) return

    if (!formRef.value) return
    try {
        await formRef.value.validate()
    } catch {
        return
    }

    if (!coverFile.value) {
        message.warning('请上传封面图片')
        return
    }

    saving.value = true
    try {
        const postAddRequest: PostAddRequest = {
            title: formModel.title,
            content: formModel.content,
            authorization: formModel.isCommercial ? formModel.authorization : 0,
            statement: formModel.statement
        }

        const res = await addPost({ postAddRequest }, coverFile.value)
        const responseData = (res as any)?.data as API.BaseResponsePostVO
        if (responseData?.code === 0) {
            message.success('发布成功')
            router.push('/creativeCenter')
        } else {
            message.error(responseData?.message || '发布失败，请稍后再试')
        }
    } catch (error: any) {
        console.error('发布创作失败', error)
        message.error(error?.message || '发布失败，请稍后再试')
    } finally {
        saving.value = false
    }
}

onMounted(async () => {
    await ensureLogin()
})
</script>

<style scoped>
#createPostPage {
    min-height: calc(100vh - 200px);
    padding: 32px 8% 48px;
    /* 使用全局布局背景，保持色调统一 */
    background: transparent;
}

.page-container {
    max-width: 1200px;
    margin: 0 auto;
}

.form-card {
    border-radius: 20px;
    box-shadow: 0 22px 48px rgba(26, 35, 50, 0.12);
    overflow: hidden;
}

.card-header {
    display: flex;
    align-items: center;
    gap: 16px;
}

.header-text h2 {
    margin: 0;
    font-size: 24px;
    color: #1a2332;
}

.header-text p {
    margin: 4px 0 0 0;
    color: #6a7a99;
    font-size: 13px;
}

.back-icon {
    font-size: 18px;
    cursor: pointer;
    color: #3d6bff;
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

.image-preview img {
    max-width: 220px;
    border-radius: 12px;
    box-shadow: 0 10px 24px rgba(26, 35, 50, 0.16);
    object-fit: cover;
}

.form-hint {
    margin-top: 4px;
    font-size: 12px;
    color: #9aa4b8;
}

.form-row {
    display: flex;
    flex-wrap: wrap;
    gap: 24px;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

@media (max-width: 768px) {
    #createPostPage {
        padding: 24px 5% 32px;
    }

    .form-row {
        flex-direction: column;
    }

    .form-actions {
        flex-direction: column;
        align-items: stretch;
    }
}
</style>
