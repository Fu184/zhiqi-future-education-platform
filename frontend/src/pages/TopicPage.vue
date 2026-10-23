<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ArrowRight, ChevronRight } from 'lucide-vue-next'
import { homeSections } from '../data/homeSections'
import { topicContent } from '../data/topicContent'
import { featuredCourses } from '../data'
import { useCourses } from '../composables/useCourses'
const { openCourse } = useCourses()
const route = useRoute()
const section = computed(() =>
  homeSections.find(
    (section) =>
      section.path === route.path || section.items.some((item) => item.path === route.path),
  ),
)
const topic = computed(() => section.value.items.find((item) => item.path === route.path))
const content = computed(() => topicContent[route.path])
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
      <h2>栏目介绍</h2>
      <p>{{ content.overview }}</p>
      <div class="topic-guides">
        <section v-for="block in content.blocks" :key="block.title">
          <h3>{{ block.title }}</h3>
          <ul>
            <li v-for="item in block.items" :key="item">{{ item }}</li>
          </ul>
        </section>
      </div>
      <p v-if="content.notice" class="topic-notice">{{ content.notice }}</p>
      <div class="topic-actions">
        <template v-for="link in content.links" :key="link.label">
          <RouterLink v-if="link.to" class="topic-action" :to="link.to"
            >{{ link.label }}<ArrowRight :size="16"
          /></RouterLink>
          <a
            v-else
            class="topic-action topic-action-external"
            :href="link.href"
            target="_blank"
            rel="noopener noreferrer"
            >{{ link.label }}（外部网站）<ArrowRight :size="16"
          /></a>
        </template>
      </div>
      <section
        v-if="content.courses"
        class="topic-resources"
        aria-labelledby="topic-resource-title"
      >
        <h2 id="topic-resource-title">课程目录</h2>
        <p>以下为当前展示的课程资料，实际开放安排以正式发布的信息为准。</p>
        <div class="topic-course-grid">
          <button
            v-for="course in featuredCourses"
            :key="course.id"
            class="topic-course"
            @click="openCourse(course.id)"
          >
            <small>{{ course.type }} · {{ course.level }} · {{ course.lessons }} 课时</small>
            <h3>{{ course.title }}</h3>
            <p>{{ course.summary }}</p>
            <span>查看课程介绍 <ArrowRight :size="15" /></span>
          </button>
        </div>
      </section>
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
.topic-guides {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 28px;
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid #e5eaf1;
}
.topic-guides h3 {
  margin: 0 0 12px;
  font-size: 17px;
  color: #293b51;
}
.topic-guides ul {
  margin: 0;
  padding-left: 20px;
  color: #64778d;
  font-size: 14px;
  line-height: 1.9;
}
.topic-guides li + li {
  margin-top: 10px;
}
.topic-detail .topic-notice {
  max-width: none;
  padding: 16px 20px;
  margin: 28px 0 8px;
  border-left: 3px solid #809db8;
  background: #f0f4f8;
  color: #52677d;
  font-size: 13px;
}
.topic-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.topic-action-external {
  color: #28567f;
  background: #edf3f8;
}
.topic-action-external:hover {
  color: #fff;
}
.topic-resources {
  margin-top: 36px;
  padding-top: 28px;
  border-top: 1px solid #e5eaf1;
}
.topic-course-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}
.topic-course {
  text-align: left;
  padding: 22px;
  border: 1px solid #e0e7ef;
  background: #fff;
  color: #293b51;
}
.topic-course:hover {
  border-color: #8baaca;
}
.topic-course h3 {
  font-size: 16px;
  line-height: 1.7;
  margin: 12px 0;
}
.topic-course small {
  font-size: 12px;
  color: #687d96;
}
.topic-course span {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #2875bd;
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
  .topic-guides,
  .topic-course-grid {
    grid-template-columns: minmax(0, 1fr);
  }
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
