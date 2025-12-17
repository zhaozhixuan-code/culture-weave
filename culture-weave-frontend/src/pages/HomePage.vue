<template>
  <div id="homePage" :class="{ 'page-entered': pageReady }">
    <!-- 顶部大图轮播 Banner，将原来的标题和按钮叠加到图片上 -->
    <section class="hero hero-banner section-anim" :class="{ 'is-visible': pageReady }">
      <div v-if="carouselLoading" class="hero-loading">
        <a-spin size="large" />
        <p>精选非遗正在加载中...</p>
      </div>

      <a-carousel
        v-else
        autoplay
        dots-class="dots-custom hero-dots"
        class="hero-carousel"
      >
        <div
          v-for="item in (featuredResources.length > 0 ? featuredResources : [null])"
          :key="item?.id ?? 'placeholder'"
          class="hero-carousel-slide"
          @click="item && goToResourceDetail(item.id)"
        >
          <div class="hero-slide-bg">
            <img
              v-if="item?.resourceImgUrl"
              :src="item.resourceImgUrl"
              :alt="item.name || '资源封面'"
              class="hero-slide-image"
              loading="lazy"
              @error="onImageError"
            />
            <div v-else class="hero-slide-placeholder">
              <span>精选非遗 · 数字故事</span>
            </div>
            <div class="hero-slide-mask" />
          </div>
        </div>
      </a-carousel>

      <div class="hero-slide-content hero-slide-content-fixed">
        <p class="hero-tag">文韵智创 · CultureWeave</p>
        <h1 class="hero-title">
          汇聚非遗之美，<br />
          点亮数字创意灵感
        </h1>
        <p class="hero-subtitle">
          这里是连接非遗资源、创意设计与智能助手的数字场景。浏览全国非遗资源、沉浸式了解背后故事，
          并用 AI 激发你的创意灵感。
        </p>

        <div class="hero-actions">
          <a-button
            type="primary"
            size="large"
            class="hero-btn primary"
            @click.stop="goToResources"
          >
            进入非遗资源库
          </a-button>
          <a-button
            size="large"
            class="hero-btn ghost"
            @click.stop="goToCreativeCenter"
          >
            前往创意社区
          </a-button>
        </div>

        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-label">在库非遗资源</div>
            <div class="stat-value">
              <span>{{ totalResourcesDisplay }}</span>
              <span class="stat-unit">+</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-label">覆盖地区</div>
            <div class="stat-value">
              <span>{{ regionCountDisplay }}</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-label">热门分类</div>
            <div class="stat-tags">
              <span
                v-for="tag in topCategories"
                :key="tag"
                class="stat-tag-chip"
              >
                {{ tag }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 轮播图下方：按分类切换的非遗项目简要展示 -->
    <section class="projects-section section-anim" :class="{ 'is-visible': pageReady }">
      <div class="section-header projects-header">
        <h2>非遗项目一览</h2>
        <p>按非遗分类从资源库中动态选取代表性项目，点击上方选项即可切换不同门类。</p>
      </div>

      <div v-if="meta.categoryList && meta.categoryList.length" class="projects-category-tabs">
        <button
          v-for="category in meta.categoryList"
          :key="category"
          class="projects-tab"
          :class="{ active: category === activeCategory }"
          @click="handleCategoryClick(category)"
        >
          {{ category }}
        </button>
      </div>

      <template v-if="activeCategory">
        <div class="projects-content">
          <div v-if="projectsLoading" class="projects-loading">
            <a-spin size="large" />
            <p>正在加载该分类的非遗项目...</p>
          </div>

          <div
            v-else-if="categoryProjectsMap[activeCategory] && categoryProjectsMap[activeCategory].length > 0"
            class="projects-grid"
          >
            <article
              v-for="item in categoryProjectsMap[activeCategory]"
              :key="item.id"
              class="project-card"
              @click="goToResourceDetail(item.id)"
            >
              <div class="project-cover">
                <img
                  v-if="item.resourceImgUrl"
                  :src="item.resourceImgUrl"
                  :alt="item.name || '非遗项目封面'"
                  loading="lazy"
                  @error="onImageError"
                />
                <div v-else class="project-cover-placeholder">
                  <span>非遗项目</span>
                </div>
                <div class="project-cover-mask" />
                <div class="project-chip">
                  精选项目
                </div>
              </div>
              <div class="project-info">
                <h3 class="project-title">
                  {{ item.name || '未命名非遗项目' }}
                </h3>
                <p class="project-intro">
                  {{ item.introduction || '这是一条来自非遗资源库的项目介绍内容。' }}
                </p>
                <div class="project-meta">
                  <span v-if="item.region" class="project-tag">
                    {{ item.region }}
                  </span>
                  <span v-if="item.category" class="project-tag ghost">
                    {{ item.category }}
                  </span>
                </div>
              </div>
            </article>
          </div>

          <div
            v-else
            class="projects-category-empty"
          >
            <span>该分类下暂时没有可展示的项目。</span>
          </div>
        </div>

        <div class="projects-more">
          <a-button type="primary" size="large" @click="goToResources">
            查看更多非遗项目
          </a-button>
        </div>
      </template>

      <div v-else class="projects-empty">
        <p class="projects-empty-main">暂时没有可展示的非遗项目分类</p>
        <p class="projects-empty-sub">非遗资源分类加载完成后，这里将按分类展示代表性项目。</p>
        <a-button type="primary" size="large" @click="goToResources">
          前往非遗资源库
        </a-button>
      </div>
    </section>

    <!-- 模块分割线：非遗项目一览 与 非遗传承人故事之间 -->
    <div class="section-divider"></div>

   
    <section class="feature-section section-anim" :class="{ 'is-visible': pageReady }">
      <div class="feature-inner">
        <div class="feature-intro">
          <h2>在这里开启你的非遗数字旅程</h2>
          <p>
            CultureWeave 想做的是一个「既懂非遗，又懂创作」的数字场景：你可以先在资源库里认识一门技艺，
            再到创意社区看看别人如何把它融入插画、产品或课程设计，最后再用 AI 助手延展自己的灵感。
          </p>
          <p>
            对于老师，这里可以是备课与展示非遗案例的素材仓库；对于设计师，这里是一座源源不断的视觉与故事宝库；
            对每一位热爱传统文化的人来说，这里都是一条从「看见」到「参与保护与创新」的路径。
          </p>
        </div>

        <div class="feature-grid">
          <div class="feature-card">
            <h3>非遗资源库</h3>
            <p>
              浏览全国各地的非物质文化遗产资源，支持按地区、类别、标签多维筛选，为创作者提供高质量素材基础。
            </p>
            <a class="feature-link" @click.prevent="goToResources">进入资源库 →</a>
          </div>
          <div class="feature-card">
            <h3>创意社区</h3>
            <p>
              将非遗元素与现代设计结合，在创意中心发布灵感、作品与教程，让传统文化在年轻人中重新被看见。
            </p>
            <a class="feature-link" @click.prevent="goToCreativeCenter">前往创意中心 →</a>
          </div>
          <div class="feature-card">
            <h3>AI 问非遗</h3>
            <p>
              基于大模型的非遗知识问答助手，支持对资源详情的智能解读、创意提示及延展学习建议。
            </p>
            <a class="feature-link" @click.prevent="goToResources">去体验 AI 问非遗 →</a>
          </div>
        </div>
      </div>
    </section>

    <div class="section-divider"></div>
    
    <section class="inheritor-section section-anim" :class="{ 'is-visible': pageReady }">
      <div class="inheritor-header">
        <h2>非遗传承人故事</h2>
        <p>
          一项非物质文化遗产的背后，往往是一生只做一件事的守护者。他们用时间和心血，将技艺一代代地传下去。
          通过这些传承人的真实故事，更立体地走近非遗本身。
        </p>
      </div>

      <div class="inheritor-layout">
        <!-- 左侧：大图 + 引导文案（可根据设计稿替换为具体图片与文字） -->
        <div class="inheritor-hero-card">
          <div class="inheritor-hero-image">
            <img
              src="https://zzx-culture-weave-1349365238.cos.ap-beijing.myqcloud.com/resource/1690268987415365.png"
              alt="传承人 · 手艺与人生"
              loading="lazy"
            />
          </div>
          <div class="inheritor-hero-text">
            <h3>在时间长河中，与手艺为伴的人</h3>
            <p>
              他们可能来自山村、集市或城市一隅，却共同肩负着守护技艺的使命。
              在这里，你可以看到他们如何与时间对话、与材料对话，也与每一位观者对话。
            </p>
            <p>
              你可以将这里的故事，作为创作灵感的起点—不仅仅是技艺的展示，更是情感和精神的延续。
            </p>
          </div>
        </div>

        <!-- 右侧：多位传承人故事列表（内容可完全替换为目标网站中的排版与文字） -->
        <div class="inheritor-list">
          <!-- 传承人故事卡片 1：请将标题、摘要、图片等替换为目标网站中的第一位传承人故事 -->
          <article class="inheritor-card">
            <div class="inheritor-card-media">
              <div class="inheritor-card-image-placeholder">
                <img
                  src="https://zzx-culture-weave-1349365238.cos.ap-beijing.myqcloud.com/resource/ScreenShot_2025-12-17_194122_322.png"
                  alt="传承人 · 手艺与人生"
                  loading="lazy"
                />
              </div>
            </div>
            <div class="inheritor-card-content">
              <h3 class="inheritor-name">喻老师 · 手捏泥人</h3>
              <p class="inheritor-meta">国家级 / 代表性传承人 · 无锡</p>
              <p class="inheritor-story">
                喻湘涟和惠山泥人，她对于泥土的粘性、湿度，搓揉和捏形时的力度、巧劲比其他人都更为熟知。这种熟知，通过语言是说不清楚的，这是一种身体里的记忆，一种长期地重复而变得心手合一、自然而然的技艺。
              </p>
            </div>
          </article>

          <!-- 传承人故事卡片 2 -->
          <article class="inheritor-card">
            <div class="inheritor-card-media">
              <div class="inheritor-card-image-placeholder">
                <img
                  src="https://zzx-culture-weave-1349365238.cos.ap-beijing.myqcloud.com/resource/c6e1e9d2461f4cf7897a93a925c22153_th.jpeg"
                  alt="传承人 · 手艺与人生"
                  loading="lazy"
                />
              </div>
            </div>
            <div class="inheritor-card-content">
              <h3 class="inheritor-name">王安江 · 苗族古歌</h3>
              <p class="inheritor-meta">国家级 / 代表性传承人 · 贵州</p>
              <p class="inheritor-story">
                王安江开始学习苗族古歌，通过刻苦学习，佐以嗓音与记忆力的先天优势，形成了深情婉转、优美流畅的唱腔特点,具有明显的古风遗韵。他演唱的古歌语言生动，概括性强，富于哲理，耐人寻味。其内容多为酒宴上的对唱、劳动生活中的说唱以及茶余饭后的交流传唱。
              </p>
            </div>
          </article>

          <!-- 传承人故事卡片 3 -->
          <article class="inheritor-card">
            <div class="inheritor-card-media">
              <div class="inheritor-card-image-placeholder">
                <img
                  src="https://zzx-culture-weave-1349365238.cos.ap-beijing.myqcloud.com/resource/53416354135613.jpg"
                  alt="传承人 · 手艺与人生"
                  loading="lazy"
                />
              </div>
            </div>
            <div class="inheritor-card-content">
              <h3 class="inheritor-name">陈进德 · 非遗项目名称</h3>
              <p class="inheritor-meta">国家级 / 代表性传承人 · 宁夏</p>
              <p class="inheritor-story">
                1983年，高中毕业的陈进德参与了中卫高庙山的修缮项目，这是他第一次接触建筑彩绘。“斋房架上那些精美绝伦的彩绘图案，就像一道闪电，直直地击中了我。”陈进德说。自此，他就像着了魔一样，白天跟着师傅学习，夜晚自己揣摩绘画技法，同时他走出宁夏，到北京、山西、甘肃等地观摩，不断精进技艺。“干不好就没饭吃”是他的信条。凭借这份执着，他逐渐打磨出独具特色的河西彩绘风格。
              </p>
            </div>
          </article>
        </div>
      </div>
    </section>

    <div class="section-divider"></div>


    <!-- 使用流程 -->
    <section class="flow-section section-anim" :class="{ 'is-visible': pageReady }">
      <div class="section-header">
        <h2>三步开启你的非遗数字旅程</h2>
        <p>从发现、理解到再创作，为创作者、教师与策展人提供一条清晰的使用路径。</p>
      </div>
      <div class="flow-grid">
        <div class="flow-item">
          <div class="flow-index">01</div>
          <h3>发现与收藏</h3>
          <p>
            在「非遗资源库」中按地区、类别或关键词检索，收藏心仪的手工艺、戏曲、节庆等资源，构建你的灵感素材盒。
          </p>
        </div>
        <div class="flow-item">
          <div class="flow-index">02</div>
          <h3>对话与学习</h3>
          <p>
            结合资源详情使用「AI 问非遗」，快速了解背后历史、技艺特点与延展阅读建议，形成结构化认知。
          </p>
        </div>
        <div class="flow-item">
          <div class="flow-index">03</div>
          <h3>创作与分享</h3>
          <p>
            在「创意社区」中发布作品、教程或课程设计案例，让更多人看见你的二次创作实践，一起丰富非遗数字生态。
          </p>
        </div>
      </div>
    </section>


    <div class="section-divider"></div>

    <!-- 最新创意动态（社区） -->
    <section class="latest-section section-anim" :class="{ 'is-visible': pageReady }">
      <div class="section-header">
        <h2>社区最新创意灵感</h2>
        <p>来自创作中心的最新 3 条动态，让你快速感受真实的非遗创作氛围。</p>
      </div>

      <div class="latest-body">
        <a-spin :spinning="latestPostsLoading">
          <div v-if="!latestPostsLoading && latestPosts.length === 0" class="latest-empty">
            <p class="latest-empty-main">还没有创作内容展示</p>
            <p class="latest-empty-sub">前往创作中心，成为第一位分享灵感的创作者。</p>
            <a-button type="primary" size="large" @click="goToCreativeCenter">去发布</a-button>
          </div>

          <div v-else class="latest-grid">
            <article
              v-for="post in latestPosts"
              :key="post.id"
              class="latest-card"
              @click="goToCreativeCenter"
            >
              <div class="latest-cover" v-if="post.pictureUrl">
                <img
                  :src="post.pictureUrl"
                  alt="post cover"
                  loading="lazy"
                  @error="onPostCoverError"
                />
              </div>
              <div v-else class="latest-cover placeholder">
                <span>创意灵感</span>
              </div>

              <div class="latest-content">
                <h3 class="latest-title">
                  {{ post.title || '未命名创作' }}
                </h3>
                <p class="latest-intro">
                  {{ postExcerpt(post.content) }}
                </p>
                <div class="latest-meta">
                  <span class="meta-author">{{ post.userVO?.userName || '匿名创作者' }}</span>
                  <span class="meta-time">{{ formatPostTime(post.createTime) }}</span>
                </div>
              </div>
            </article>
          </div>
        </a-spin>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listResourcesVoByPage, getResourceCategoryList } from '../api/resourcesController'
import { listResourcesVoByPage1 } from '../api/postController'

type ResourceVO = {
  id?: number
  name?: string
  introduction?: string
  category?: string
  region?: string
  resourceImgUrl?: string
}

type ResourceCategoryMeta = {
  categoryList?: string[]
  regionList?: string[]
}

type PostVO = {
  id?: number
  title?: string
  content?: string
  pictureUrl?: string
  createTime?: string
  userVO?: {
    userName?: string
  }
}

const router = useRouter()

const pageReady = ref(false)
const carouselLoading = ref(false)
const featuredResources = ref<ResourceVO[]>([])

const meta = reactive<ResourceCategoryMeta>({
  categoryList: [],
  regionList: [],
})

const categoryProjectsMap = reactive<Record<string, ResourceVO[]>>({})
const activeCategory = ref<string | null>(null)
const projectsLoading = ref(false)

const latestPostsLoading = ref(false)
const latestPosts = ref<PostVO[]>([])

// 派生统计信息
const totalResources = ref<number | null>(null)

const totalResourcesDisplay = computed(() => {
  if (totalResources.value === null) return '—'
  if (totalResources.value > 999) return '999'
  return totalResources.value
})

const regionCountDisplay = computed(() =>
  meta.regionList && meta.regionList.length ? meta.regionList.length : '—',
)

const topCategories = computed(() =>
  (meta.categoryList || []).slice(0, 3),
)

function goToResources() {
  router.push('/resources')
}

function goToCreativeCenter() {
  router.push('/creativeCenter')
}

function goToResourcesByCategory(category: string) {
  router.push({
    path: '/resources',
    query: {
      category,
    },
  })
}

async function handleCategoryClick(category: string) {
  if (activeCategory.value === category) return
  activeCategory.value = category

  if (!categoryProjectsMap[category] || categoryProjectsMap[category].length === 0) {
    await loadCategoryProjects(category)
  }
}

function goToResourceDetail(id?: number) {
  if (id == null) return
  router.push(`/resources/${String(id)}`)
}

function onImageError(event: Event) {
  const target = event.target as HTMLImageElement
  if (target) {
    target.style.display = 'none'
  }
}

function onPostCoverError(event: Event) {
  const img = event.target as HTMLImageElement
  if (img) {
    img.style.display = 'none'
  }
}

function postExcerpt(content?: string) {
  if (!content) return '这位创作者暂未留下文字描述。'
  return content.length > 80 ? `${content.slice(0, 80)}...` : content
}

function formatPostTime(value?: string) {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString()
}

async function loadFeaturedResources() {
  carouselLoading.value = true
  try {
    const body = {
      current: 1,
      pageSize: 3,
      sortField: 'createTime',
      sortOrder: 'descend',
    }
    const res = await listResourcesVoByPage(body as any)
    const responseData = res as any

    // 主流返回结构：{ code, data: { records, total, ... } }
    if (responseData?.data?.data) {
      const { records = [], total = 0 } = responseData.data.data
      featuredResources.value = records
      totalResources.value = total
    } else if (responseData?.data?.records) {
      const { records = [], total = 0 } = responseData.data
      featuredResources.value = records
      totalResources.value = total
    } else if (responseData?.records) {
      const { records = [], total = 0 } = responseData
      featuredResources.value = records
      totalResources.value = total
    } else {
      featuredResources.value = []
    }
  } catch (e) {
    console.error('加载首页轮播资源失败', e)
    featuredResources.value = []
  } finally {
    carouselLoading.value = false
  }
}

async function loadMeta() {
  try {
    const res = await getResourceCategoryList()
    const responseData = res as any
    if (responseData?.data?.data) {
      const inner = responseData.data.data as ResourceCategoryMeta
      meta.categoryList = inner.categoryList || []
      meta.regionList = inner.regionList || []
    } else if (responseData?.data) {
      const inner = responseData.data as ResourceCategoryMeta
      meta.categoryList = inner.categoryList || []
      meta.regionList = inner.regionList || []
    }
  } catch (e) {
    console.error('获取首页分类/地域统计失败', e)
  }
}

async function loadCategoryProjects(category: string) {
  if (!category) return

  projectsLoading.value = true
  try {
    const body = {
      current: 1,
      pageSize: 4,
      sortField: 'createTime',
      sortOrder: 'descend',
      category,
    }
    const res = await listResourcesVoByPage(body as any)
    const responseData = res as any

    let records: ResourceVO[] = []
    if (responseData?.data?.data) {
      records = responseData.data.data.records || []
    } else if (responseData?.data?.records) {
      records = responseData.data.records || []
    } else if (responseData?.records) {
      records = responseData.records || []
    }
    categoryProjectsMap[category] = records || []
  } catch (e) {
    console.error(`加载分类「${category}」项目失败`, e)
    categoryProjectsMap[category] = []
  } finally {
    projectsLoading.value = false
  }
}

async function loadLatestPosts() {
  latestPostsLoading.value = true
  try {
    const payload = {
      current: 1,
      pageSize: 3,
      sortField: 'createTime',
      sortOrder: 'descend',
    }
    const res = await listResourcesVoByPage1(payload as any)
    const responseData = (res as any)?.data
    if (responseData?.code === 0 && responseData?.data) {
      latestPosts.value = responseData.data.records || []
    } else {
      latestPosts.value = []
    }
  } catch (e) {
    console.error('获取首页社区最新动态失败', e)
    latestPosts.value = []
  } finally {
    latestPostsLoading.value = false
  }
}

onMounted(async () => {
  requestAnimationFrame(() => {
    pageReady.value = true
  })
  await Promise.all([loadFeaturedResources(), loadMeta(), loadLatestPosts()])
  if (meta.categoryList && meta.categoryList.length > 0) {
    activeCategory.value = meta.categoryList[0]
    await loadCategoryProjects(activeCategory.value)
  }
})
</script>

<style scoped>
#homePage {
  min-height: calc(100vh - 200px);
  padding: 32px 8% 56px;
  max-width: 1800px;
  margin: 0 auto;
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.6s ease, transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
  display: flex;
  flex-direction: column;
  gap: 40px;
}

#homePage.page-entered {
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

.hero {
  margin-bottom: 40px;
}

.hero-banner {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 18px 46px rgba(153, 94, 46, 0.22);
  background: radial-gradient(circle at top left, #fff9f2, #f6ddc2);
}

.hero-carousel {
  width: 100%;
}

.hero-carousel-slide {
  position: relative;
  min-height: 500px;
  cursor: pointer;
}

.hero-slide-bg {
  position: absolute;
  inset: 0;
}

.hero-slide-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scale(1.02);
  transition: transform 0.8s ease;
}

.hero-carousel-slide:hover .hero-slide-image {
  transform: scale(1.06);
}

.hero-slide-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  letter-spacing: 2px;
  color: rgba(255, 255, 255, 0.9);
  background: linear-gradient(135deg, #f29b4c, #c25d32);
}

.hero-slide-mask {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(120deg, rgba(0, 0, 0, 0.6) 0%, rgba(0, 0, 0, 0.35) 42%, rgba(0, 0, 0, 0.1) 70%, rgba(0, 0, 0, 0) 100%);
}

.hero-slide-content {
  position: relative;
  z-index: 1;
  height: 100%;
  padding: 40px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  max-width: 620px;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'PingFang SC',
    'Segoe UI', sans-serif;
}

.hero-slide-content-fixed {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
}

.hero-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px 0;
  color: #6a7a99;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(0, 0, 0, 0.22);
  margin-bottom: 16px;
}

.hero-title {
  font-size: 38px;
  line-height: 1.3;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.94);
  margin: 0 0 16px 0;
  letter-spacing: 0.5px;
  text-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.hero-subtitle {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.82);
  margin: 0 0 24px 0;
  text-shadow: 0 8px 22px rgba(0, 0, 0, 0.45);
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.hero-btn {
  border-radius: 999px;
  padding-inline: 26px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.hero-btn.primary {
  background: #c91f37 !important;
  border: none !important;
  box-shadow: 0 10px 26px rgba(201, 31, 55, 0.4);
}

.hero-btn.ghost {
  border-color: rgba(201, 31, 55, 0.6);
  color: #ffe9ee !important;
  background: rgba(201, 31, 55, 0.08);
}

.hero-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.stat-item {
  min-width: 120px;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.stat-unit {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
}

.stat-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.stat-tag-chip {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  font-size: 12px;
}

.hero-resource-meta {
  margin-top: 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.92);
}

.hero-resource-label {
  opacity: 0.85;
}

.hero-resource-name {
  font-weight: 600;
}

.hero-resource-tag {
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

.hero-resource-tag.ghost {
  border-color: rgba(255, 255, 255, 0.32);
}

/* 自定义轮播指示点 */
.dots-custom li {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.32);
}

.dots-custom li.slick-active {
  width: 18px;
  background: #ffffff;
}

.hero-dots {
  bottom: 18px !important;
}

.feature-section {
  margin-top: 32px;
  padding: 4px 0 0;
  background: transparent;
  border-radius: 0;
}

.feature-inner {
  max-width: 1150px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 28px;
  align-items: stretch;
}

.feature-intro {
  align-self: center;
  text-align: center;
  max-width: 720px;
  margin-bottom: 4px;
}

.feature-intro h2 {
  margin: 0 0 32px 0;
  font-size: 34px;
  font-weight: 650;
  color: #2e241d;
  position: relative;
  padding-bottom: 10px;
}

.feature-intro h2::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 64px;
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(90deg, #c91f37, #f29b4c);
  transform: translateX(-50%);
}

.feature-intro p {
  margin: 0;
  font-size: 15px;
  line-height: 1.8;
  color: #6a4e3a;
}

.feature-intro p + p {
  margin-top: 10px;
}

/* 功能入口区：改为竖向单列展示，让内容更饱满 */
.feature-grid {
  display: flex;
  flex-direction: row;
  gap: 18px;
  max-width: 900px;
  margin: 0 auto;
}

.feature-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 18px 22px 16px;
  box-shadow: 0 8px 22px rgba(153, 94, 46, 0.12);
  border: 1px solid rgba(236, 218, 198, 0.9);
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.feature-card h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 620;
  color: #2e241d;
}

.feature-card p {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: #5b4a3a;
}

.feature-card:hover {
  box-shadow: 0 12px 26px rgba(153, 94, 46, 0.16);
  transform: translateY(-2px);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.feature-link {
  margin-top: 6px;
  font-size: 14px;
  color: #c25d32;
  cursor: pointer;
  align-self: flex-start;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
}

.feature-link:hover {
  text-decoration: underline;
}

@media (max-width: 960px) {
  .feature-section {
    margin-top: 24px;
    padding: 16px 0 4px;
    border-radius: 16px;
  }

  .feature-inner {
    gap: 18px;
  }

  .feature-grid {
    flex-direction: column;
  }
}

.flow-section {
  margin-top: 40px;
}

.section-header {
  margin: 0 auto 24px;
  max-width: 640px;
  text-align: center;
}

.section-header h2 {
  margin: 0 0 28px 0;
  font-size: 40px;
  font-weight: 650;
  color: #1a2332;
}

.section-header p {
  margin: 0 0 50px 0;
  font-size: 18px;
  color: #6a7a99;
}

.flow-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 18px;
}

.flow-item {
  background: rgba(255, 252, 247, 0.98);
  border-radius: 18px;
  padding: 18px 18px 16px;
  box-shadow: 0 10px 26px rgba(153, 94, 46, 0.16);
  border: 1px solid rgba(238, 214, 187, 0.9);
}

.flow-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 999px;
  background: linear-gradient(135deg, #f09d4a, #c25d32);
  color: #fff;
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 10px;
}

.flow-item h3 {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a2332;
}

.flow-item p {
  margin: 0;
  font-size: 14px;
  color: #5b4a3a;
  line-height: 1.7;
}

.latest-section {
  margin-top: 40px;
}

.latest-body {
  margin-top: 8px;
}

.latest-empty {
  padding: 40px 20px;
  text-align: center;
  color: #6a7a99;
}

.latest-empty-main {
  margin: 0 0 6px 0;
  font-size: 18px;
  font-weight: 600;
}

.latest-empty-sub {
  margin: 0 0 16px 0;
  font-size: 14px;
}

.latest-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 18px;
}

.latest-card {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 12px 30px rgba(153, 94, 46, 0.16);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.25s ease;
}

.latest-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 38px rgba(18, 36, 90, 0.2);
}

.latest-cover {
  height: 160px;
  background: linear-gradient(135deg, #fff7eb, #f4ddc1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.latest-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.latest-cover.placeholder span {
  font-size: 14px;
  color: #b9a797;
}

.latest-content {
  padding: 16px 18px 14px;
}

.latest-title {
  margin: 0 0 6px 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a2332;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.latest-intro {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #5b4a3a;
  line-height: 1.6;
}

.latest-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #b9a797;
}

.meta-author {
  font-weight: 500;
  color: #5b4a3a;
}

.meta-time {
  white-space: nowrap;
}

/* 轮播图下方非遗项目展示 */
.projects-section {
  margin-top: 32px;
}

/* 区块分割线：用于分隔项目一览与下方模块 */
.section-divider {
  margin: 20px 0 10px;
  height: 1px;
  background: linear-gradient(
    90deg,
    rgba(194, 93, 50, 0),
    rgba(194, 93, 50, 0.55),
    rgba(194, 93, 50, 0)
  );
}

.projects-header {
  margin-bottom: 20px;
}

.projects-category-tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 18px;
  justify-content: center;
}

.projects-tab {
  padding: 8px 18px;
  border-radius: 999px;
  border: 1px solid rgba(194, 93, 50, 0.3);
  background: rgba(255, 252, 247, 0.9);
  font-size: 15px;
  font-weight: 600;
  color: #7c6858;
  cursor: pointer;
  transition: all 0.2s ease;
}

.projects-tab:hover {
  background: rgba(242, 155, 76, 0.12);
  color: #b05b27;
}

.projects-tab.active {
  background: linear-gradient(135deg, #f29b4c, #e6763a);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 14px rgba(226, 118, 58, 0.4);
}

.projects-content {
  min-height: 220px;
}

.projects-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 32px 0;
  color: #6a7a99;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.project-card {
  background: rgba(255, 252, 247, 0.98);
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 10px 28px rgba(153, 94, 46, 0.18);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.25s ease;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 36px rgba(18, 36, 90, 0.18);
}

.project-cover {
  position: relative;
  height: 180px;
  background: linear-gradient(135deg, #fff8ec, #f5e1c8);
  overflow: hidden;
}

.project-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.project-card:hover .project-cover img {
  transform: scale(1.05);
}

.project-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #b9a797;
  font-size: 15px;
  letter-spacing: 1px;
}

.project-cover-mask {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at top left, rgba(0, 0, 0, 0.15), transparent 60%);
  pointer-events: none;
}

.project-chip {
  position: absolute;
  left: 16px;
  top: 14px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #f29b4c, #e6763a);
  box-shadow: 0 6px 16px rgba(226, 118, 58, 0.45);
}

.project-info {
  padding: 16px 18px 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.project-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a2332;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-intro {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: #5b4a3a;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-meta {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
}

.project-tag {
  padding: 3px 9px;
  border-radius: 999px;
  background: rgba(240, 157, 74, 0.1);
  color: #b05b27;
}

.project-tag.ghost {
  background: rgba(46, 36, 29, 0.05);
  color: #7c6858;
}

.projects-category-empty {
  padding: 12px 4px 4px;
  font-size: 13px;
  color: #9aa4b8;
}

.projects-more {
  margin-top: 18px;
  display: flex;
  justify-content: center;
}

.projects-more :deep(.ant-btn-primary) {
  background: #c91f37 !important;
  border: none !important;
  box-shadow: 0 8px 22px rgba(201, 31, 55, 0.35) !important;
}

.projects-more :deep(.ant-btn-primary:hover) {
  background: #b81b32 !important;
}

.projects-more :deep(.ant-btn-primary:active) {
  background: #a3172b !important;
}

.projects-empty {
  padding: 40px 20px;
  text-align: center;
  color: #6a7a99;
}

.projects-empty-main {
  margin: 0 0 6px 0;
  font-size: 18px;
  font-weight: 600;
}

.projects-empty-sub {
  margin: 0 0 16px 0;
  font-size: 14px;
}

/* 非遗传承人模块样式 */
.inheritor-section {
  margin-top: 32px;
}

.inheritor-header {
  max-width: 720px;
  text-align: left;
  margin-bottom: 24px;
}

.inheritor-header h2 {
  margin: 0 0 16px 0;
  font-size: 32px;
  font-weight: 650;
  color: #2e241d;
}

.inheritor-header p {
  margin: 0;
  font-size: 15px;
  line-height: 1.8;
  color: #6a4e3a;
}

.inheritor-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 1.4fr);
  gap: 20px;
  align-items: stretch;
}

.inheritor-hero-card {
  background: rgba(255, 252, 247, 0.98);
  border-radius: 18px;
  padding: 16px 16px 14px;
  box-shadow: 0 10px 24px rgba(153, 94, 46, 0.18);
  border: 1px solid rgba(238, 214, 187, 0.9);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.inheritor-hero-image {
  border-radius: 14px;
  overflow: hidden;
  background: linear-gradient(135deg, #f29b4c, #c25d32);
  min-height: 75px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.inheritor-hero-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.inheritor-hero-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.95);
  font-size: 16px;
  letter-spacing: 2px;
}

.inheritor-hero-text h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #2e241d;
}

.inheritor-hero-text p {
  margin: 0 0 6px 0;
  font-size: 15px;
  line-height: 1.8;
  color: #6a4e3a;
}

.inheritor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.inheritor-card {
  display: grid;
  grid-template-columns: 200px minmax(0, 1fr);
  gap: 16px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 252, 247, 0.96);
  border: 1px solid rgba(238, 214, 187, 0.96);
  box-shadow: 0 8px 20px rgba(153, 94, 46, 0.14);
}

.inheritor-card-media {
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #fff3e1, #f4cfaa);
  display: flex;
  align-items: center;
  justify-content: center;
}

.inheritor-card-image-placeholder {
  width: 100%;
  height: 160px;
  overflow: hidden;
}

.inheritor-card-image-placeholder img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.inheritor-card-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.inheritor-name {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #2e241d;
}

.inheritor-meta {
  margin: 0;
  font-size: 14px;
  color: #a07b58;
}

.inheritor-story {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: #6a4e3a;
}

@media (max-width: 960px) {
  .inheritor-layout {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 720px) {
  .inheritor-section {
    padding-inline: 16px;
  }

  .inheritor-card {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 1200px) {
  .projects-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .projects-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .project-cover {
    height: 170px;
  }
}

@media (max-width: 1200px) {
  .hero-carousel-slide {
    min-height: 360px;
  }
}

@media (max-width: 960px) {
  #homePage {
    padding-inline: 7%;
  }

  .hero-slide-content {
    padding: 32px 32px 40px;
    max-width: 100%;
  }

  /* 自适应网格在中屏下保持自动布局，这里无需强行指定列数 */
}

@media (max-width: 720px) {
  #homePage {
    padding-inline: 6%;
    padding-top: 16px;
  }

  .hero-title {
    font-size: 26px;
  }

  .hero-carousel-slide {
    min-height: 300px;
  }

  .hero-title {
    font-size: 26px;
  }

  .hero-slide-content {
    padding: 24px 24px 32px;
  }

  /* 移动端同样使用 auto-fit + minmax 自动适配 */
}
</style>
