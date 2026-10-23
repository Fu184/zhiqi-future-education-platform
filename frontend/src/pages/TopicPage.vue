<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ArrowRight, ChevronRight } from 'lucide-vue-next'
import { homeSections } from '../data/homeSections'
const route = useRoute()
const section = computed(() =>
  homeSections.find(
    (section) =>
      section.path === route.path || section.items.some((item) => item.path === route.path),
  ),
)
const topic = computed(() => section.value.items.find((item) => item.path === route.path))
</script>

<template>
  <div class="container topic-page">
    <nav class="topic-breadcrumb" aria-label="当前位置">
      <RouterLink to="/">首页</RouterLink><ChevronRight :size="14" />
      <RouterLink v-if="topic" :to="section.path">{{ section.title }}</RouterLink>
      <span v-else>{{ section.title }}</span>
      <template v-if="topic"
        ><ChevronRight :size="14" /><span>{{ topic.title }}</span></template
      >
    </nav>
    <header class="topic-heading">
      <h1>{{ topic?.title || section.title }}</h1>
      <p>{{ topic?.description || section.description }}</p>
    </header>
    <section v-if="topic" class="topic-detail">
      <component :is="topic.icon" :size="48" :stroke-width="1.25" />
      <h2>{{ topic.title }}</h2>
      <p v-if="topic.target">{{ topic.description }} 点击下方入口查看现有资源。</p>
      <p v-else>
        本栏目将集中发布{{
          topic.title
        }}的相关内容，具体资源与安排将在确认后更新。欢迎联系我们了解相关信息。
      </p>
      <RouterLink class="topic-action" :to="topic.target || '/contact'"
        >{{ topic.action || '联系我们' }}<ArrowRight :size="16"
      /></RouterLink>
    </section>
    <section v-else class="topic-list" :aria-label="section.title + '栏目'">
      <RouterLink
        v-for="item in section.items"
        :key="item.path"
        :to="item.path"
        class="topic-list-item"
      >
        <component :is="item.icon" :size="32" :stroke-width="1.5" />
        <div>
          <h2>{{ item.title }}</h2>
          <p>{{ item.description }}</p>
        </div>
        <ArrowRight :size="20" />
      </RouterLink>
    </section>
    <RouterLink v-if="topic" class="topic-back" :to="section.path"
      >查看{{ section.title }}全部栏目 <ArrowRight :size="15"
    /></RouterLink>
  </div>
</template>

<style scoped>
.topic-page {
  padding-top: 24px;
  padding-bottom: 80px;
}
.topic-breadcrumb {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
  color: #8190a3;
}
.topic-breadcrumb a:hover,
.topic-back:hover {
  color: #2875bd;
}
.topic-heading {
  padding: 36px 0 28px;
  border-bottom: 1px solid #e5eaf1;
}
.topic-heading h1 {
  margin-bottom: 14px;
  font-size: 32px;
  color: #293b51;
}
.topic-heading p,
.topic-detail p {
  color: #7a899b;
  line-height: 1.9;
  font-size: 14px;
}
.topic-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  margin-top: 30px;
}
.topic-list-item {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #fff;
  border: 1px solid #e5eaf1;
  border-radius: 0;
  padding: 28px;
  color: #2875bd;
}
.topic-list-item h2 {
  font-size: 18px;
  font-weight: 600;
  color: #293b51;
  margin-bottom: 8px;
}
.topic-list-item p {
  color: #8190a3;
  font-size: 13px;
  line-height: 1.8;
  margin: 0;
}
.topic-list-item > svg {
  flex: none;
}
.topic-list-item > svg:last-child {
  margin-left: auto;
}
.topic-list-item:hover {
  border-color: #a7c9e9;
}
.topic-detail {
  margin-top: 30px;
  padding: 38px;
  background: #fff;
  border: 1px solid #e5eaf1;
  border-radius: 0;
}
.topic-detail > svg {
  color: #2875bd;
  margin-bottom: 20px;
}
.topic-detail h2 {
  font-size: 22px;
}
.topic-detail p {
  max-width: 660px;
}
.topic-action,
.topic-back {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}
.topic-action {
  margin-top: 15px;
  padding: 12px 20px;
  border-radius: 2px;
  color: #fff;
  background: #2875bd;
  font-size: 14px;
}
.topic-action:hover {
  background: #1e629f;
}
.topic-back {
  margin-top: 26px;
  color: #687d96;
  font-size: 13px;
}
@media (max-width: 700px) {
  .topic-list {
    grid-template-columns: minmax(0, 1fr);
  }
  .topic-detail,
  .topic-list-item {
    padding: 24px;
  }
  .topic-heading h1 {
    font-size: 26px;
  }
}
</style>
