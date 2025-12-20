<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <div class="header-inner container">
          <GlobalHeader />
        </div>
      </a-layout-header>

      <a-layout>
        <a-layout-content class="content container">
          <router-view />
        </a-layout-content>
      </a-layout>

      <a-layout-footer class="footer">
        <div class="footer-inner container">
          <div class="footer-main">
            <div class="footer-brand">
              <div class="footer-title">文韵智创 · CultureWeave</div>
              <p class="footer-desc">
                以数字技术连接非遗资源、创意设计与智能助手，
                让更多人以更轻松的方式看见、理解并参与非遗传承与创新。
              </p>
            </div>
            <div class="footer-columns">
              <div class="footer-column">
                <div class="column-title">产品功能</div>
                <a class="column-link" @click.prevent="$router.push('/resources')">非遗资源库</a>
                <a class="column-link" @click.prevent="$router.push('/creativeCenter')">创意社区</a>
                <a class="column-link" @click.prevent="$router.push('/resources')">AI 问非遗</a>
              </div>
              <div class="footer-column">
                <div class="column-title">适用人群</div>
                <span class="column-text">高校教师 · 学生创作者</span>
                <span class="column-text">设计师 · 文创从业者</span>
                <span class="column-text">非遗传承人 · 研究者</span>
              </div>
            </div>
          </div>
          <div class="footer-bottom">
            <span class="bottom-text">© {{ new Date().getFullYear() }} 文韵智创 CultureWeave</span>
            <span class="bottom-sep">·</span>
            <span class="bottom-text">致力于面向教育与创作场景的非遗数字化实践平台</span>
          </div>
        </div>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { defineAsyncComponent } from 'vue'

const GlobalHeader = defineAsyncComponent(() => import('../components/GlobalHeader.vue'))
</script>

<style scoped>
:global(:root) {
  /* 全局暖色系背景，与首页色调统一（柔和米杏色） */
  --page-grad-start: #fff4e6; /* 顶部更亮的暖白 */
  --page-grad-end: #ffe0c2;   /* 底部偏杏色的暖色 */
  /* 顶部导航栏背景改为纯白色 */
  --header-bg: #ffffff;
  --header-border: rgba(236, 199, 158, 0.9);
  --content-bg: rgba(255, 249, 241, 0.98);
  --shadow-card: 0 24px 40px rgba(173, 117, 62, 0.12);
  /* 页脚背景改为深色 #2D2926，并提升文字对比度 */
  --footer-bg: #2d2926;
  --footer-text: #f5eee7;
  --footer-border: rgba(245, 238, 231, 0.24);
  /* 大面积主体区域统一暖色卡片背景（稍微加深，避免看起来接近纯白） */
  --surface-card: #ffe9d2;
  --surface-card-soft: #ffe2c4;
  --surface-soft-alt: #fbd2aa;
  --surface-chat: #ffeada;
  /* 统一按钮色系（暖橙主色 + 柔和描边） */
  --btn-primary-start: #f29b4c;
  --btn-primary-end: #e6763a;
  --btn-primary-shadow: 0 8px 22px rgba(226, 118, 58, 0.38);
  --btn-primary-border: rgba(226, 118, 58, 0.85);
  --btn-ghost-border: rgba(226, 118, 58, 0.4);
  --btn-ghost-bg: rgba(226, 118, 58, 0.04);
}

@media (prefers-color-scheme: dark) {
  :global(:root) {
    --page-grad-start: #0b0f16;
    --page-grad-end: #0b0f16;
    --header-bg: rgba(17, 17, 17, 0.7);
    --header-border: rgba(255, 255, 255, 0.08);
    --content-bg: rgba(20, 20, 20, 0.9);
    --shadow-card: 0 24px 50px rgba(0, 0, 0, 0.45);
    --footer-bg: #111317;
    --footer-text: #c8d1e0;
    --footer-border: rgba(255, 255, 255, 0.08);
  }
}

.container {
  max-width: 1800px;
  margin: 0 auto;
  padding: 0 8%;
}

#basicLayout {
  /* 页面中间主背景色：统一为柔和米白色 #F9F6F0 */
  background-color: #f9f6f0;
  background-image:
    /* 传统纹样背景（低透明度叠加在米白底上） */
    url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23c91f37' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E"),
    linear-gradient(135deg,
      rgba(255, 255, 255, 0.12) 0,
      rgba(255, 255, 255, 0.12) 1px,
      transparent 1px,
      transparent 20px
    ),
    linear-gradient(225deg,
      rgba(255, 255, 255, 0.08) 0,
      rgba(255, 255, 255, 0.08) 1px,
      transparent 1px,
      transparent 20px
    );
  background-size: 60px 60px, 26px 26px, 26px 26px;
  background-repeat: repeat, repeat, repeat;
  min-height: 100vh;
}

:global(body) {
  background: linear-gradient(180deg, var(--page-grad-start) 0%, var(--page-grad-end) 40%);
}

/* 覆盖 Ant Design Layout 默认白色背景，让中间整块区域用暖色背景 */
#basicLayout :deep(.ant-layout) {
  background: transparent;
}

:global(body) {
  background: linear-gradient(180deg, var(--page-grad-start) 0%, var(--page-grad-end) 40%);
}

/* 关键：去掉默认行高带来的“整体下沉”视觉问题 */
#basicLayout :deep(.ant-layout-header),
#basicLayout .header {
  line-height: normal !important;
  padding: 0 !important;

}

#basicLayout .header {
  position: sticky;
  top: 0;
  z-index: 10;
  margin-bottom: 16px;
  color: unset;
  background: #ffffff !important;
  border-bottom: 1px solid var(--header-border);
  box-shadow: 0 4px 20px rgba(15, 35, 95, 0.08);
  transition: box-shadow 0.3s ease, background 0.3s ease;
}

#basicLayout .header-inner {
  display: flex;
  align-items: center;
  /* 真正负责垂直居中 */
  min-height: 64px;
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
}

#basicLayout .content {
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  margin: 0 auto 48px;
  padding: 0;
  width: 100%;
}

#basicLayout .footer {
  background-color: var(--footer-bg);
  padding: 72px 0 40px;
  color: var(--footer-text);
  font-size: 14px;
  border-top: 1px solid var(--footer-border);
  letter-spacing: 0.3px;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'PingFang SC',
    'Segoe UI', sans-serif;
}

.footer-inner {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.footer-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 32px;
  padding-bottom: 4px;
  border-bottom: 1px dashed rgba(185, 167, 151, 0.5);
}

.footer-brand {
  max-width: 520px;
}

.footer-title {
  font-size: 18px;
  font-weight: 650;
  letter-spacing: 0.6px;
  margin-bottom: 10px;
}

.footer-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.9;
  opacity: 0.88;
}

.footer-columns {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.footer-column {
  min-width: 140px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.column-title {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.4px;
  margin-bottom: 6px;
}

.column-link {
  font-size: 14px;
  color: var(--footer-text);
  opacity: 0.9;
  cursor: pointer;
  line-height: 1.8;
  transition: color 0.2s ease, opacity 0.2s ease;
}

.column-link:hover {
  opacity: 1;
  color: #ffffff;
}

.column-text {
  font-size: 14px;
  opacity: 0.9;
}

.footer-bottom {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  opacity: 0.8;
}

.bottom-text {
  white-space: nowrap;
}

.bottom-sep {
  margin: 0 4px;
}

@media (max-width: 960px) {
  #basicLayout .header {
    margin-bottom: 12px;
  }

  #basicLayout .header-inner {
    min-height: 56px;
  }

  #basicLayout .content {
    padding: 20px;
    border-radius: 16px;
    margin-bottom: 32px;
  }

  .footer-main {
    flex-direction: column;
    align-items: flex-start;
  }

  .footer-bottom {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .container {
    padding: 0 8%;
  }

  #basicLayout .content {
    margin-bottom: 32px;
  }
}
</style>
