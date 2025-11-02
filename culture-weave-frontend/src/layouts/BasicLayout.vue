<template>
  <div id="basicLayout">
    <!--使用组件-->
    <a-layout style="min-height: 100vh">
      <!--头部-->
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>

      <!-- 主体 -->
      <a-layout>
        <!--内容-->
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>

      <!-- 页脚 -->
      <a-layout-footer class="footer">文韵智创-CultureWeave项目</a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
</script>

<style scoped>
#basicLayout .header {
  padding-inline: 20px;
  margin-bottom: 1px;
  color: unset;
  background: white;
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
}

#basicLayout .content {
  background: linear-gradient(to right, #fefefe, #fff);
  margin-bottom: 28px;
  padding: 28px;
}

#basicLayout .footer {
  background-color: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
</style>
