<template>
  <div id="homePage" :class="{ 'page-entered': pageReady }">
    <!-- 顶部大图轮播 Banner -->
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
        <p>按非遗分类从资源库中动态选取代表性项目，点击下方选项即可切换不同种类。</p>
      </div>

      <div v-if="meta.categoryList && meta.categoryList.length" class="projects-category-tabs">
        <button
          class="projects-tab"
          :class="{ active: activeCategory === '全部' }"
          @click="handleCategoryClick('全部')"
        >
          全部
        </button>
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
            <p>{{ activeCategory === '全部' ? '正在加载所有分类的非遗项目...' : '正在加载该分类的非遗项目...' }}</p>
          </div>

          <div
            v-else-if="displayProjects && displayProjects.length > 0"
            class="projects-grid"
          >
            <article
              v-for="item in displayProjects"
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

    <!-- 模块分割线 -->
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
        <!-- 左侧：大图 + 引导文案 -->
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

        <!-- 右侧：多位传承人故事列表 -->
        <div class="inheritor-list">
          <!-- 传承人故事卡片 1 -->
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
                1983年，高中毕业的陈进德参与了中卫高庙山的修缮项目，这是他第一次接触建筑彩绘。"斋房架上那些精美绝伦的彩绘图案，就像一道闪电，直直地击中了我。"陈进德说。自此，他就像着了魔一样，白天跟着师傅学习，夜晚自己揣摩绘画技法，同时他走出宁夏，到北京、山西、甘肃等地观摩，不断精进技艺。"干不好就没饭吃"是他的信条。凭借这份执着，他逐渐打磨出独具特色的河西彩绘风格。
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

// 根据当前选中的分类返回要显示的项目
const displayProjects = computed(() => {
  if (!activeCategory.value) return []
  if (activeCategory.value === '全部') {
    // 返回所有项目，限制最多4条
    return (categoryProjectsMap['全部'] || []).slice(0, 4)
  }
  return (categoryProjectsMap[activeCategory.value] || []).slice(0, 4)
})

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

  if (category === '全部') {
    // 加载所有项目（不传category参数）
    if (!categoryProjectsMap['全部'] || categoryProjectsMap['全部'].length === 0) {
      await loadAllProjects()
    }
  } else {
    if (!categoryProjectsMap[category] || categoryProjectsMap[category].length === 0) {
      await loadCategoryProjects(category)
    }
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

async function loadAllProjects() {
  projectsLoading.value = true
  try {
    // 参考资源库的搜索逻辑：不传category参数，则获取全部资源
    const body = {
      current: 1,
      pageSize: 4,
      sortField: 'createTime',
      sortOrder: 'descend',
      // 不传category参数，表示获取全部
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
    categoryProjectsMap['全部'] = records || []
  } catch (e) {
    console.error('加载全部项目失败', e)
    categoryProjectsMap['全部'] = []
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
  // 默认选择"全部"，如果没有选择任何分类则展示全部
  activeCategory.value = '全部'
  await loadAllProjects()
})
</script>

<style scoped>
/* 非遗文化主题样式 - 首页优化版 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;500;600;700;800&family=Noto+Sans+SC:wght@300;400;500;600;700&display=swap');

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
  gap: 48px;
  background: linear-gradient(180deg,
  #fcf9f4 0%,
  #f9f2e9 50%,
  #f5eadf 100%);
  position: relative;
}

/* 添加文化纹理背景 */
#homePage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23d4a76a' fill-opacity='0.03' fill-rule='evenodd'/%3E%3C/svg%3E");
  opacity: 0.5;
  pointer-events: none;
  z-index: 0;
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

/* Hero 轮播区域 - 非遗文化风格 */
.hero-banner {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(139, 69, 19, 0.25),
  0 8px 24px rgba(212, 167, 106, 0.2);
  background: linear-gradient(135deg, #f8eddc, #f5e6d0);
  border: 2px solid rgba(212, 167, 106, 0.3);
  min-height: 520px;
}

.hero-carousel {
  width: 100%;
  border-radius: 24px;
  overflow: hidden;
}

.hero-carousel-slide {
  position: relative;
  min-height: 520px;
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
  background: linear-gradient(135deg, #d4a76a, #b8860b);
  font-family: 'Noto Serif SC', serif;
}

.hero-slide-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg,
  rgba(0, 0, 0, 0.7) 0%,
  rgba(0, 0, 0, 0.4) 42%,
  rgba(0, 0, 0, 0.2) 70%,
  rgba(0, 0, 0, 0) 100%);
}

.hero-slide-content {
  position: relative;
  z-index: 1;
  height: 100%;
  padding: 48px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  max-width: 680px;
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
  color: #8b4513;
  font-family: 'Noto Sans SC', sans-serif;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  border-radius: 6px;
  font-size: 14px;
  color: #fff;
  background: rgba(164, 123, 67, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
  font-family: 'Noto Serif SC', serif;
  position: relative;
}

.hero-tag::before {
  content: '「';
  position: absolute;
  left: -12px;
  color: #d4a76a;
}

.hero-tag::after {
  content: '」';
  position: absolute;
  right: -12px;
  color: #d4a76a;
}

.hero-title {
  font-size: 44px;
  line-height: 1.3;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.96);
  margin: 0 0 20px 0;
  letter-spacing: 0.5px;
  text-shadow: 0 12px 36px rgba(0, 0, 0, 0.6);
  font-family: 'Noto Serif SC', serif;
}

.hero-subtitle {
  font-size: 16px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 32px 0;
  text-shadow: 0 8px 22px rgba(0, 0, 0, 0.5);
  font-family: 'Noto Sans SC', sans-serif;
}

.hero-actions {
  display: flex;
  gap: 20px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.hero-btn {
  border-radius: 12px;
  padding-inline: 32px;
  font-weight: 600;
  letter-spacing: 0.3px;
  height: 56px;
  font-size: 16px;
  font-family: 'Noto Sans SC', sans-serif;
}

.hero-btn.primary {
  background: linear-gradient(135deg, #a0522d, #8b4513) !important;
  border: 2px solid #8b4513 !important;
  box-shadow: 0 10px 30px rgba(139, 69, 19, 0.4);
}

.hero-btn.primary:hover {
  background: linear-gradient(135deg, #8b4513, #654321) !important;
  border-color: #654321 !important;
  transform: translateY(-2px);
  box-shadow: 0 12px 36px rgba(139, 69, 19, 0.5);
}

.hero-btn.ghost {
  border: 2px solid rgba(255, 255, 255, 0.4);
  color: #fff !important;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.hero-btn.ghost:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-2px);
}

.hero-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
  margin-top: 8px;
}

.stat-item {
  min-width: 120px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 6px;
  font-family: 'Noto Sans SC', sans-serif;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
  display: flex;
  align-items: baseline;
  gap: 2px;
  font-family: 'Noto Serif SC', serif;
}

.stat-unit {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
}

.stat-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.stat-tag-chip {
  padding: 6px 14px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 13px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  font-family: 'Noto Sans SC', sans-serif;
}

/* 自定义轮播指示点 */
.dots-custom li {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.dots-custom li.slick-active {
  width: 24px;
  background: #d4a76a;
  border-color: #fff;
}

.hero-dots {
  bottom: 24px !important;
}

/* 区块分割线 */
.section-divider {
  margin: 20px 0;
  height: 2px;
  background: linear-gradient(
      90deg,
      rgba(212, 167, 106, 0),
      rgba(212, 167, 106, 0.6),
      rgba(212, 167, 106, 0)
  );
}

/* 通用标题样式 */
.section-header {
  margin: 0 auto 32px;
  max-width: 720px;
  text-align: center;
}

.section-header h2 {
  margin: 0 0 16px 0;
  font-size: 36px;
  font-weight: 700;
  color: #2c1810;
  font-family: 'Noto Serif SC', serif;
  position: relative;
  padding-bottom: 16px;
}

.section-header h2::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #d4a76a, #b8860b);
  transform: translateX(-50%);
  border-radius: 2px;
}

.section-header p {
  margin: 0;
  font-size: 16px;
  color: #5d4037;
  line-height: 1.8;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 非遗项目展示区域 */
.projects-section {
  margin-top: 8px;
  padding: 32px;
  background: #fffaf4;
  border-radius: 20px;
  box-shadow: 0 12px 32px rgba(139, 69, 19, 0.12),
  0 4px 16px rgba(212, 167, 106, 0.1);
  border: 2px solid rgba(212, 167, 106, 0.2);
}

.projects-header {
  margin-bottom: 24px;
}

.projects-category-tabs {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 28px;
  justify-content: center;
}

.projects-tab {
  padding: 10px 24px;
  border-radius: 12px;
  border: 2px solid rgba(212, 167, 106, 0.3);
  background: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-weight: 600;
  color: #8b4513;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Noto Sans SC', sans-serif;
}

.projects-tab:hover {
  background: rgba(212, 167, 106, 0.1);
  color: #654321;
  border-color: #d4a76a;
  transform: translateY(-2px);
}

.projects-tab.active {
  background: linear-gradient(135deg, #d4a76a, #b8860b);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 6px 18px rgba(212, 167, 106, 0.4);
}

.projects-content {
  min-height: 280px;
}

.projects-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 40px 0;
  color: #8b4513;
  font-family: 'Noto Sans SC', sans-serif;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
}

.project-card {
  background: #fffdf9;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 28px rgba(139, 69, 19, 0.15),
  0 4px 12px rgba(212, 167, 106, 0.1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
  border: 2px solid rgba(212, 167, 106, 0.2);
}

.project-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(139, 69, 19, 0.2),
  0 8px 20px rgba(212, 167, 106, 0.15);
  border-color: rgba(212, 167, 106, 0.4);
}

.project-cover {
  position: relative;
  height: 200px;
  background: linear-gradient(135deg, #f8eddc, #f5e6d0);
  overflow: hidden;
}

.project-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.project-card:hover .project-cover img {
  transform: scale(1.08);
}

.project-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a1887f;
  font-size: 16px;
  letter-spacing: 1px;
  font-family: 'Noto Sans SC', sans-serif;
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
  top: 16px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #b8860b, #a0522d);
  box-shadow: 0 6px 16px rgba(184, 134, 11, 0.45);
  z-index: 2;
  font-family: 'Noto Sans SC', sans-serif;
}

.project-info {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.project-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #2c1810;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-family: 'Noto Serif SC', serif;
}

.project-intro {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #5d4037;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-word;
  overflow-wrap: break-word;
  font-family: 'Noto Sans SC', sans-serif;
}

.project-meta {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.project-tag {
  padding: 6px 14px;
  border-radius: 20px;
  background: rgba(212, 167, 106, 0.15);
  color: #8b4513;
  font-size: 13px;
  font-family: 'Noto Sans SC', sans-serif;
}

.project-tag.ghost {
  background: rgba(44, 24, 16, 0.08);
  color: #5d4037;
}

.projects-category-empty {
  padding: 40px 20px;
  text-align: center;
  font-size: 15px;
  color: #a1887f;
  font-family: 'Noto Sans SC', sans-serif;
}

.projects-more {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.projects-more :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #a0522d, #8b4513) !important;
  border: 2px solid #8b4513 !important;
  box-shadow: 0 8px 24px rgba(139, 69, 19, 0.35) !important;
  border-radius: 12px;
  padding-inline: 32px;
  height: 48px;
  font-size: 15px;
  font-family: 'Noto Sans SC', sans-serif;
}

.projects-empty {
  padding: 40px 20px;
  text-align: center;
  color: #8b4513;
}

.projects-empty-main {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  font-family: 'Noto Serif SC', serif;
}

.projects-empty-sub {
  margin: 0 0 20px 0;
  font-size: 15px;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 功能介绍区域 */
.feature-section {
  margin-top: 8px;
  padding: 32px;
  background: #fffaf4;
  border-radius: 20px;
  box-shadow: 0 12px 32px rgba(139, 69, 19, 0.12),
  0 4px 16px rgba(212, 167, 106, 0.1);
  border: 2px solid rgba(212, 167, 106, 0.2);
}

.feature-inner {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.feature-intro {
  text-align: center;
  max-width: 720px;
  margin: 0 auto;
}

.feature-intro h2 {
  margin: 0 0 24px 0;
  font-size: 36px;
  font-weight: 700;
  color: #2c1810;
  position: relative;
  padding-bottom: 16px;
  font-family: 'Noto Serif SC', serif;
}

.feature-intro h2::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #d4a76a, #b8860b);
  transform: translateX(-50%);
  border-radius: 2px;
}

.feature-intro p {
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: #5d4037;
  font-family: 'Noto Sans SC', sans-serif;
}

.feature-intro p + p {
  margin-top: 16px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.feature-card {
  background: #fffdf9;
  border-radius: 20px;
  padding: 28px 24px;
  box-shadow: 0 8px 24px rgba(139, 69, 19, 0.1),
  0 4px 12px rgba(212, 167, 106, 0.1);
  border: 2px solid rgba(212, 167, 106, 0.2);
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: all 0.3s ease;
  position: relative;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #d4a76a, #b8860b);
  border-radius: 2px 2px 0 0;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 32px rgba(139, 69, 19, 0.15),
  0 8px 20px rgba(212, 167, 106, 0.12);
  border-color: rgba(212, 167, 106, 0.4);
}

.feature-card h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #2c1810;
  font-family: 'Noto Serif SC', serif;
}

.feature-card p {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: #5d4037;
  font-family: 'Noto Sans SC', sans-serif;
}

.feature-link {
  margin-top: 8px;
  font-size: 15px;
  color: #8b4513;
  cursor: pointer;
  align-self: flex-start;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: 'Noto Sans SC', sans-serif;
  border-bottom: 2px solid transparent;
  padding-bottom: 2px;
  transition: all 0.2s ease;
}

.feature-link:hover {
  color: #654321;
  border-bottom-color: #d4a76a;
}

/* 使用流程区域 */
.flow-section {
  margin-top: 8px;
  padding: 32px;
  background: #fffaf4;
  border-radius: 20px;
  box-shadow: 0 12px 32px rgba(139, 69, 19, 0.12),
  0 4px 16px rgba(212, 167, 106, 0.1);
  border: 2px solid rgba(212, 167, 106, 0.2);
}

.flow-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
  max-width: 1000px;
  margin: 0 auto;
}

.flow-item {
  background: #fffdf9;
  border-radius: 20px;
  padding: 32px 28px;
  box-shadow: 0 10px 28px rgba(139, 69, 19, 0.1),
  0 4px 12px rgba(212, 167, 106, 0.1);
  border: 2px solid rgba(212, 167, 106, 0.2);
  text-align: center;
  transition: all 0.3s ease;
}

.flow-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 32px rgba(139, 69, 19, 0.15),
  0 8px 20px rgba(212, 167, 106, 0.12);
}

.flow-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #d4a76a, #b8860b);
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  margin-bottom: 20px;
  font-family: 'Noto Serif SC', serif;
}

.flow-item h3 {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 700;
  color: #2c1810;
  font-family: 'Noto Serif SC', serif;
}

.flow-item p {
  margin: 0;
  font-size: 15px;
  color: #5d4037;
  line-height: 1.7;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 传承人故事区域 */
.inheritor-section {
  margin-top: 8px;
  padding: 32px;
  background: #fffaf4;
  border-radius: 20px;
  box-shadow: 0 12px 32px rgba(139, 69, 19, 0.12),
  0 4px 16px rgba(212, 167, 106, 0.1);
  border: 2px solid rgba(212, 167, 106, 0.2);
}

.inheritor-header {
  max-width: 720px;
  text-align: left;
  margin-bottom: 32px;
}

.inheritor-header h2 {
  margin: 0 0 20px 0;
  font-size: 36px;
  font-weight: 700;
  color: #2c1810;
  font-family: 'Noto Serif SC', serif;
  position: relative;
  padding-bottom: 16px;
}

.inheritor-header h2::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #d4a76a, #b8860b);
  border-radius: 2px;
}

.inheritor-header p {
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: #5d4037;
  font-family: 'Noto Sans SC', sans-serif;
}

.inheritor-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  align-items: stretch;
}

.inheritor-hero-card {
  background: #fffdf9;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 10px 28px rgba(139, 69, 19, 0.1),
  0 4px 12px rgba(212, 167, 106, 0.1);
  border: 2px solid rgba(212, 167, 106, 0.2);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.inheritor-hero-image {
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #f8eddc, #f5e6d0);
  height: 240px;
}

.inheritor-hero-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.inheritor-hero-text h3 {
  margin: 0 0 12px 0;
  font-size: 22px;
  font-weight: 700;
  color: #2c1810;
  font-family: 'Noto Serif SC', serif;
}

.inheritor-hero-text p {
  margin: 0 0 12px 0;
  font-size: 15px;
  line-height: 1.7;
  color: #5d4037;
  font-family: 'Noto Sans SC', sans-serif;
}

.inheritor-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.inheritor-card {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 20px;
  padding: 20px;
  border-radius: 20px;
  background: #fffdf9;
  border: 2px solid rgba(212, 167, 106, 0.2);
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.1);
  transition: all 0.3s ease;
}

.inheritor-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 32px rgba(139, 69, 19, 0.15);
  border-color: rgba(212, 167, 106, 0.4);
}

.inheritor-card-media {
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #f8eddc, #f5e6d0);
  height: 160px;
}

.inheritor-card-image-placeholder {
  width: 100%;
  height: 100%;
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
  gap: 8px;
}

.inheritor-name {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #2c1810;
  font-family: 'Noto Serif SC', serif;
}

.inheritor-meta {
  margin: 0;
  font-size: 14px;
  color: #a1887f;
  font-family: 'Noto Sans SC', sans-serif;
}

.inheritor-story {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: #5d4037;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 最新创意动态区域 */
.latest-section {
  margin-top: 8px;
  padding: 32px;
  background: #fffaf4;
  border-radius: 20px;
  box-shadow: 0 12px 32px rgba(139, 69, 19, 0.12),
  0 4px 16px rgba(212, 167, 106, 0.1);
  border: 2px solid rgba(212, 167, 106, 0.2);
}

.latest-body {
  margin-top: 8px;
}

.latest-empty {
  padding: 60px 20px;
  text-align: center;
  color: #8b4513;
}

.latest-empty-main {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  font-family: 'Noto Serif SC', serif;
}

.latest-empty-sub {
  margin: 0 0 20px 0;
  font-size: 15px;
  font-family: 'Noto Sans SC', sans-serif;
}

.latest-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.latest-card {
  background: #fffdf9;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 28px rgba(139, 69, 19, 0.1),
  0 4px 12px rgba(212, 167, 106, 0.1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
  border: 2px solid rgba(212, 167, 106, 0.2);
}

.latest-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(139, 69, 19, 0.15),
  0 8px 20px rgba(212, 167, 106, 0.12);
  border-color: rgba(212, 167, 106, 0.4);
}

.latest-cover {
  height: 180px;
  background: linear-gradient(135deg, #f8eddc, #f5e6d0);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.latest-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.latest-cover.placeholder {
  color: #a1887f;
  font-size: 15px;
  font-family: 'Noto Sans SC', sans-serif;
}

.latest-content {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  /* 保证卡片内容区域为列布局，便于将 meta 推到底部 */ 
  min-height: 120px;
}

.latest-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #2c1810;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-family: 'Noto Serif SC', serif;
}

.latest-intro {
  margin: 0;
  font-size: 14px;
  color: #5d4037;
  line-height: 1.7;
  /* 多行省略：兼容 -webkit-box 并使用标准属性作为补充 */
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  /* 标准属性（兼容性） */
  display: box;
  line-clamp: 3;
  box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: 'Noto Sans SC', sans-serif;
  /* 保证简介占用一定高度，防止 meta 随简介高度上移 */
  min-height: 3.6em; /* 大约 3 行 */
  max-height: 4.8em;
}

.latest-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #a1887f;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(212, 167, 106, 0.2);
  /* 将 meta 固定在卡片底部 */
  margin-top: auto;
}

.meta-author {
  font-weight: 600;
  color: #8b4513;
  font-family: 'Noto Sans SC', sans-serif;
}

.meta-time {
  white-space: nowrap;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .hero-banner {
    min-height: 480px;
  }

  .hero-carousel-slide {
    min-height: 480px;
  }

  .hero-title {
    font-size: 36px;
  }

  .feature-grid,
  .flow-grid,
  .latest-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  #homePage {
    padding: 24px 6% 48px;
    gap: 32px;
  }

  .hero-banner {
    min-height: 440px;
  }

  .hero-carousel-slide {
    min-height: 440px;
  }

  .hero-slide-content {
    padding: 32px;
  }

  .hero-title {
    font-size: 32px;
  }

  .projects-grid {
    grid-template-columns: 1fr;
  }

  .inheritor-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  #homePage {
    padding: 20px 4% 40px;
    gap: 24px;
  }

  .hero-banner {
    min-height: 400px;
    border-radius: 20px;
  }

  .hero-carousel-slide {
    min-height: 400px;
  }

  .hero-slide-content {
    padding: 24px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 15px;
  }

  .hero-actions {
    flex-direction: column;
    gap: 12px;
  }

  .hero-btn {
    width: 100%;
    justify-content: center;
  }

  .hero-stats {
    flex-direction: column;
    gap: 20px;
  }

  .projects-section,
  .feature-section,
  .flow-section,
  .inheritor-section,
  .latest-section {
    padding: 24px;
  }

  .feature-grid,
  .flow-grid,
  .latest-grid {
    grid-template-columns: 1fr;
  }

  .inheritor-card {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .inheritor-card-media {
    height: 200px;
  }

  .section-header h2 {
    font-size: 28px;
  }

  .section-header p {
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .hero-banner {
    min-height: 360px;
  }

  .hero-carousel-slide {
    min-height: 360px;
  }

  .hero-title {
    font-size: 24px;
  }

  .hero-subtitle {
    font-size: 14px;
  }

  .projects-category-tabs {
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 8px;
  }

  .projects-tab {
    white-space: nowrap;
  }
}
</style>
