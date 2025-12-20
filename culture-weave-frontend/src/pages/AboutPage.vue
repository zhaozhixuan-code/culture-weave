<template>
  <div id="aboutPage" :class="{ 'page-entered': pageReady }">
    <!-- 顶部标题区域 -->
    <section class="hero-section section-anim" :class="{ 'is-visible': pageReady }">
      <div class="hero-content">
        <p class="hero-tag">关于我们</p>
        <h1 class="hero-title">文韵智创 · CultureWeave</h1>
        <p class="hero-subtitle">
          以数字技术连接非遗资源、创意设计与智能助手，让更多人以更轻松的方式看见、理解并参与非遗传承与创新。
        </p>
      </div>
    </section>

    <!-- 主要内容区域 -->
    <section class="content-section section-anim" :class="{ 'is-visible': pageReady }">
      <div class="content-container">
        <!-- 项目介绍 -->
        <div class="content-block">
          <h2 class="block-title">项目介绍</h2>
          <div class="block-content">
            <p>
              文韵智创（CultureWeave）是一个致力于非物质文化遗产数字化传承与创新的综合性平台。
              我们通过整合全国各地的非遗资源，为高校教师、学生创作者、设计师、文创从业者以及非遗传承人和研究者
              提供一个便捷的数字场景，让大家能够浏览、学习和创作。
            </p>
            <p>
              平台集成了AI智能助手功能，可以帮助用户快速了解非遗知识，激发创作灵感。
              我们相信，通过数字技术的赋能，可以让传统文化焕发新的生命力。
            </p>
          </div>
        </div>

        <!-- 核心功能 -->
        <div class="content-block">
          <h2 class="block-title">核心功能</h2>
          <div class="features-grid">
            <div class="feature-item">
              <div class="feature-icon">
                <FolderOutlined />
              </div>
              <h3 class="feature-title">非遗资源库</h3>
              <p class="feature-desc">
                汇聚全国各地的非物质文化遗产资源，包括传统工艺、民间艺术、传统医药等多个类别。
                每个资源都配有详细的介绍、图片和相关信息。
              </p>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <EditOutlined />
              </div>
              <h3 class="feature-title">创意社区</h3>
              <p class="feature-desc">
                创作者可以在这里分享灵感、展示作品、交流心得。社区为非遗文化的创新应用提供了
                一个开放的交流平台。
              </p>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <RobotOutlined />
              </div>
              <h3 class="feature-title">AI 问非遗</h3>
              <p class="feature-desc">
                智能AI助手可以回答关于非物质文化遗产的各种问题，帮助用户快速了解非遗知识，
                激发创作灵感。
              </p>
            </div>
          </div>
        </div>

        <!-- 适用人群 -->
        <div class="content-block">
          <h2 class="block-title">适用人群</h2>
          <div class="audience-grid">
            <div class="audience-item">
              <h3 class="audience-title">高校教师 · 学生创作者</h3>
              <p>提供丰富的教学资源和创作素材，支持教学和学术研究</p>
            </div>
            <div class="audience-item">
              <h3 class="audience-title">设计师 · 文创从业者</h3>
              <p>获取非遗元素和灵感，创作具有文化内涵的现代作品</p>
            </div>
            <div class="audience-item">
              <h3 class="audience-title">非遗传承人 · 研究者</h3>
              <p>展示和传播非遗文化，记录和分享传承经验</p>
            </div>
          </div>
        </div>

        <!-- 联系我们 -->
        <div class="content-block">
          <h2 class="block-title">联系我们</h2>
          <div class="contact-content">
            <p>如果您有任何问题、建议或合作意向，欢迎通过以下方式联系我们：</p>
            <div class="contact-info">
              <p><strong>邮箱：</strong>53525430x@gmail.com</p>
              <p><strong>项目地址：</strong><a href="https://github.com/zhaozhixuan-code/culture-weave" target="_blank" rel="noopener">GitHub</a></p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  FolderOutlined,
  EditOutlined,
  RobotOutlined
} from '@ant-design/icons-vue'

const pageReady = ref(false)

onMounted(() => {
  requestAnimationFrame(() => {
    pageReady.value = true
  })
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;500;600;700;800&family=Noto+Sans+SC:wght@300;400;500;600;700&display=swap');

#aboutPage {
  min-height: calc(100vh - 200px);
  padding: 32px 8% 56px;
  max-width: 1800px;
  margin: 0 auto;
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

#aboutPage.page-entered {
  opacity: 1;
  transform: translateY(0);
}

.section-anim {
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.6s ease 0.2s, transform 0.6s ease 0.2s;
}

.section-anim.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* 顶部标题区域 */
.hero-section {
  text-align: center;
  padding: 80px 0 60px;
  background: linear-gradient(135deg, rgba(244, 237, 232, 0.98), rgba(251, 247, 240, 0.95));
  border-radius: 24px;
  margin-bottom: 48px;
  border: 1px solid rgba(212, 167, 106, 0.3);
  box-shadow: 0 8px 32px rgba(212, 167, 106, 0.15);
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
}

.hero-tag {
  font-size: 14px;
  color: #8b4513;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 16px;
  font-family: 'Noto Sans SC', sans-serif;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  color: #2c1810;
  margin-bottom: 24px;
  background: linear-gradient(90deg, #b8860b 0%, #d4a76a 25%, #a0522d 50%, #8b4513 75%, #654321 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-family: 'Noto Serif SC', serif;
}

.hero-subtitle {
  font-size: 18px;
  line-height: 1.8;
  color: #5d4037;
  max-width: 700px;
  margin: 0 auto;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 内容区域 */
.content-section {
  margin-top: 48px;
}

.content-container {
  max-width: 1000px;
  margin: 0 auto;
}

.content-block {
  margin-bottom: 64px;
}

.block-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c1810;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(212, 167, 106, 0.3);
  font-family: 'Noto Serif SC', serif;
}

.block-content {
  font-size: 16px;
  line-height: 1.8;
  color: #5d4037;
  font-family: 'Noto Sans SC', sans-serif;
}

.block-content p {
  margin-bottom: 16px;
}

.block-content p:last-child {
  margin-bottom: 0;
}

/* 功能网格 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 32px;
  margin-top: 32px;
}

.feature-item {
  padding: 32px;
  background: rgba(255, 253, 249, 0.9);
  border-radius: 16px;
  border: 1px solid rgba(212, 167, 106, 0.3);
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(212, 167, 106, 0.1);
}

.feature-item:hover {
  box-shadow: 0 8px 24px rgba(139, 69, 19, 0.15);
  transform: translateY(-4px);
  border-color: rgba(212, 167, 106, 0.5);
}

.feature-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(212, 167, 106, 0.15), rgba(139, 69, 19, 0.1));
  border-radius: 16px;
  margin-bottom: 20px;
  font-size: 28px;
  color: #d4a76a;
  border: 1px solid rgba(212, 167, 106, 0.3);
}

.feature-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c1810;
  margin-bottom: 12px;
  font-family: 'Noto Serif SC', serif;
}

.feature-desc {
  font-size: 14px;
  line-height: 1.7;
  color: #5d4037;
  margin: 0;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 适用人群网格 */
.audience-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-top: 32px;
}

.audience-item {
  padding: 24px;
  background: linear-gradient(135deg, rgba(244, 237, 232, 0.5), rgba(251, 247, 240, 0.5));
  border-radius: 12px;
  border: 1px solid rgba(212, 167, 106, 0.3);
  box-shadow: 0 2px 8px rgba(212, 167, 106, 0.08);
}

.audience-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c1810;
  margin-bottom: 8px;
  font-family: 'Noto Serif SC', serif;
}

.audience-item p {
  font-size: 14px;
  line-height: 1.7;
  color: #5d4037;
  margin: 0;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 联系我们 */
.contact-content {
  font-size: 16px;
  line-height: 1.8;
  color: #5d4037;
  font-family: 'Noto Sans SC', sans-serif;
}

.contact-info {
  margin-top: 24px;
  padding: 24px;
  background: rgba(255, 253, 249, 0.9);
  border-radius: 12px;
  border: 1px solid rgba(212, 167, 106, 0.3);
  box-shadow: 0 4px 12px rgba(212, 167, 106, 0.1);
}

.contact-info p {
  margin-bottom: 12px;
  color: #5d4037;
}

.contact-info p:last-child {
  margin-bottom: 0;
}

.contact-info a {
  color: #d4a76a;
  text-decoration: none;
  transition: color 0.2s ease;
  font-weight: 500;
}

.contact-info a:hover {
  color: #8b4513;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #aboutPage {
    padding: 24px 4% 40px;
  }

  .hero-section {
    padding: 60px 0 40px;
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .block-title {
    font-size: 24px;
  }

  .features-grid,
  .audience-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .feature-item,
  .audience-item {
    padding: 24px;
  }
}
</style>

