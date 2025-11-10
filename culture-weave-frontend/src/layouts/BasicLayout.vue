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
        文韵智创-CultureWeave项目
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { defineAsyncComponent } from 'vue'

const GlobalHeader = defineAsyncComponent(() => import('../components/GlobalHeader.vue'))
</script>

<style scoped>
:root {
  --page-grad-start: #f7f9fc;
  --page-grad-end: #ffffff;
  --header-bg: rgba(255, 255, 255, 0.9);
  --header-border: rgba(228, 233, 242, 0.9);
  --content-bg: rgba(255, 255, 255, 0.96);
  --shadow-card: 0 24px 40px rgba(23, 44, 94, 0.05);
  --footer-bg: #f5f7fb;
  --footer-text: #4f5b76;
  --footer-border: #e6e9f2;
}

@media (prefers-color-scheme: dark) {
  :root {
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
  background: linear-gradient(180deg, var(--page-grad-start) 0%, var(--page-grad-end) 40%);
  min-height: 100vh;
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
  background: var(--header-bg);
  -webkit-backdrop-filter: blur(8px);
  backdrop-filter: blur(8px);
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
  padding: 18px;
  text-align: center;
  color: var(--footer-text);
  font-size: 13px;
  border-top: 1px solid var(--footer-border);
  letter-spacing: .2px;
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

  .container {
    padding: 0 8%;
  }

  #basicLayout .content {
    margin-bottom: 32px;
  }
}
</style>
