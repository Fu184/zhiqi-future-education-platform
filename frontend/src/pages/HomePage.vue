<script setup>
import { computed, ref } from 'vue'
import {
  ArrowRight,
  BookOpen,
  Bot,
  ClipboardList,
  WandSparkles,
  GraduationCap,
  LayoutGrid,
} from 'lucide-vue-next'
import HomeHero from '../components/home/HomeHero.vue'
import { featuredCourses } from '../data'
import { demoStore as store } from '../stores/demo'
import { useCourses } from '../composables/useCourses'
import { iconMap } from '../config/portal'
import { useUi } from '../composables/useUi'
const { dialog, go } = useUi()
const { openCourse } = useCourses()
const category = ref('全部')
const categories = ['全部', ...new Set(featuredCourses.map((course) => course.type))]
const courses = computed(() =>
  featuredCourses.filter((course) => category.value === '全部' || course.type === category.value),
)
const entries = [
  { title: '课程资源', sub: '学习科技知识', path: '/courses', icon: BookOpen },
  { title: '学习任务', sub: '开展实践探索', path: '/tasks', icon: ClipboardList },
  { title: '创作中心', sub: '实现创意想法', path: '/create', icon: WandSparkles },
  { title: '作品展示', sub: '分享学习成果', path: '/works', icon: LayoutGrid },
  { title: '教师成长', sub: '支持教学实践', path: '/teacher-growth', icon: GraduationCap },
  { title: 'AI 学习助手', sub: '一起提问思考', path: '/assistant', icon: Bot },
]
</script>

<template>
  <div class="education-home">
    <HomeHero />
    <section class="home-resources container" aria-labelledby="resources-title">
      <div class="home-section-heading">
        <h2 id="resources-title">学习与实践</h2>
        <p>从课程学习到动手创作，找到你的探索方向</p>
      </div>
      <div class="resource-panel">
        <div class="resource-banner">
          <div>
            <span>科技教育</span>
            <p>学科技 · 用科技 · 创科技</p>
          </div>
          <button @click="go('/courses')">浏览资源 <ArrowRight :size="16" /></button>
        </div>
        <div class="resource-grid">
          <button
            v-for="item in entries"
            :key="item.path"
            class="resource-entry"
            @click="go(item.path)"
          >
            <span class="resource-icon"
              ><component :is="item.icon" :size="30" :stroke-width="1.6" /></span
            ><strong>{{ item.title }}</strong
            ><small>{{ item.sub }}</small>
          </button>
        </div>
      </div>
    </section>
    <section class="home-courses container" aria-labelledby="courses-title">
      <div class="home-section-heading">
        <h2 id="courses-title">精品课程</h2>
        <p>编程、人工智能、机器人与创意设计</p>
      </div>
      <div class="home-course-toolbar">
        <div class="course-filters" aria-label="课程分类">
          <button
            v-for="item in categories"
            :key="item"
            :class="{ selected: category === item }"
            :aria-pressed="category === item"
            @click="category = item"
          >
            {{ item }}
          </button>
        </div>
        <button class="home-more" @click="go('/courses')">
          全部课程 <ArrowRight :size="15" />
        </button>
      </div>
      <div class="home-course-grid">
        <button
          v-for="course in courses"
          :key="course.id"
          class="home-course-card"
          @click="openCourse(course.id)"
        >
          <div :class="['course-cover', course.color]">
            <component :is="iconMap[course.icon]" :size="64" :stroke-width="1.1" /><span>{{
              course.type
            }}</span>
          </div>
          <div class="home-course-body">
            <small>{{ course.level }} · {{ course.lessons }} 课时</small>
            <h3>{{ course.title }}</h3>
            <p>{{ course.summary }}</p>
            <span class="course-detail">查看课程 <ArrowRight :size="14" /></span>
          </div>
        </button>
      </div>
    </section>
    <section class="home-updates container" aria-labelledby="updates-title">
      <div class="home-section-heading">
        <h2 id="updates-title">资讯与服务</h2>
        <p>关注平台动态，了解更多教学与合作信息</p>
      </div>
      <div class="home-updates-grid">
        <div class="updates-list">
          <div class="updates-heading">
            <h3>新闻资讯</h3>
            <button class="home-more" @click="go('/news')">
              更多资讯 <ArrowRight :size="15" />
            </button>
          </div>
          <button
            v-for="item in store.news.slice(0, 4)"
            :key="item.id"
            class="update-row"
            @click="dialog = { kind: 'news', item }"
          >
            <span class="update-category">{{ item.category }}</span
            ><strong>{{ item.title }}</strong
            ><time>{{ item.date }}</time
            ><ArrowRight :size="16" />
          </button>
          <p v-if="!store.news.length" class="updates-empty">
            暂无新闻资讯，后续内容将在这里发布。
          </p>
        </div>
        <aside class="home-service-panel">
          <h3>教学与合作服务</h3>
          <p>了解平台、教师团队与学校合作</p>
          <button @click="go('/organization')">学校与机构 <ArrowRight :size="16" /></button
          ><button @click="go('/faculty')">师资介绍 <ArrowRight :size="16" /></button
          ><button @click="go('/contact')">联系我们 <ArrowRight :size="16" /></button>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
.education-home {
  background: #f4f7fb;
  padding-bottom: 64px;
  color: #24374f;
}
.home-section-heading {
  text-align: center;
  padding: 38px 0 24px;
}
.home-section-heading h2 {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin: 0 0 9px;
  font-size: 27px;
  font-weight: 700;
}
.home-section-heading h2::before,
.home-section-heading h2::after {
  content: '';
  width: 28px;
  height: 3px;
  background: #4a94df;
}
.home-section-heading p {
  margin: 0;
  color: #8491a3;
  font-size: 13px;
}
.resource-panel {
  border-radius: 8px;
  overflow: hidden;
  background: #eaf3fc;
}
.resource-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 30px;
  color: #fff;
  background: #2875bd;
  background-image: radial-gradient(ellipse at 90% 20%, #ffffff20, transparent 60%);
}
.resource-banner span {
  font-size: 26px;
  font-weight: 700;
}
.resource-banner p {
  margin: 8px 0 0;
  font-size: 13px;
  color: #dbeaff;
  letter-spacing: 2px;
}
.resource-banner button {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  color: #2965a1;
  border: 0;
  padding: 9px 18px;
  border-radius: 4px;
  font-size: 13px;
}
.resource-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  padding: 24px;
}
.resource-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 0;
  border-radius: 5px;
  padding: 24px 10px 20px;
  background: #fff;
  color: #304660;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}
.resource-entry:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 18px #245c9912;
}
.resource-icon {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  border-radius: 18px;
  background: #edf5ff;
  color: #2776be;
  margin-bottom: 17px;
}
.resource-entry strong {
  font-size: 15px;
  font-weight: 500;
}
.resource-entry small {
  font-size: 11px;
  color: #92a0af;
  margin-top: 8px;
}
.home-course-toolbar,
.updates-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.course-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.course-filters button {
  border: 0;
  background: transparent;
  padding: 8px 18px;
  color: #62738a;
  border-radius: 4px;
  font-size: 14px;
}
.course-filters button.selected {
  background: #2875bd;
  color: #fff;
}
.course-filters button:hover:not(.selected) {
  background: #e4edf7;
}
.home-more {
  display: flex;
  align-items: center;
  gap: 9px;
  background: none;
  border: 0;
  color: #59748e;
  padding: 6px 0;
  font-size: 13px;
  white-space: nowrap;
}
.home-more:hover {
  color: #176cc3;
}
.home-course-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.home-course-card {
  min-width: 0;
  padding: 0;
  text-align: left;
  background: #fff;
  border: 1px solid #e7edf4;
  border-radius: 6px;
  overflow: hidden;
  color: inherit;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}
.home-course-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px #254c7412;
}
.course-cover {
  position: relative;
  display: grid;
  place-items: center;
  height: 150px;
  color: #426c97;
  background: #e4effa;
}
.course-cover.purple {
  background: #eeebf6;
  color: #8076a7;
}
.course-cover.orange {
  background: #f8efdf;
  color: #b28d50;
}
.course-cover.green {
  background: #e5f2eb;
  color: #649b80;
}
.course-cover span {
  position: absolute;
  bottom: 12px;
  left: 16px;
  font-size: 12px;
}
.home-course-body {
  padding: 18px;
}
.home-course-body small {
  color: #8a98a7;
  font-size: 11px;
}
.home-course-body h3 {
  margin: 11px 0 10px;
  font-size: 16px;
  line-height: 1.6;
  min-height: 52px;
}
.home-course-body p {
  font-size: 12px;
  line-height: 1.9;
  color: #8290a0;
  margin-bottom: 16px;
  min-height: 69px;
}
.course-detail {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #2875bd;
  border-top: 1px solid #eff2f6;
  padding-top: 12px;
}
.home-updates-grid {
  display: grid;
  grid-template-columns: 2.3fr 1fr;
  gap: 24px;
}
.updates-list {
  background: #fff;
  padding: 24px;
  border-radius: 6px;
}
.updates-heading {
  margin-bottom: 8px;
}
.updates-heading h3,
.home-service-panel h3 {
  font-size: 18px;
  margin: 0;
}
.update-row {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 100%;
  background: none;
  border: 0;
  border-bottom: 1px solid #edf1f6;
  padding: 18px 0;
  text-align: left;
  color: #3b4e65;
}
.update-row:last-child {
  border: 0;
}
.update-row:hover {
  color: #176cc3;
}
.update-category {
  color: #2875bd;
  font-size: 11px;
  background: #f0f6fd;
  padding: 5px 8px;
  white-space: nowrap;
}
.update-row strong {
  font-size: 13px;
  font-weight: 500;
  flex: 1;
  line-height: 1.7;
}
.update-row time {
  color: #98a4b2;
  font-size: 11px;
  white-space: nowrap;
}
.update-row svg {
  flex: none;
  color: #9fb0c1;
}
.updates-empty {
  margin: 30px 0;
  color: #8491a3;
  font-size: 13px;
}
.home-service-panel {
  padding: 26px;
  background: #eaf2fb;
  border-radius: 6px;
}
.home-service-panel p {
  color: #7b90a7;
  font-size: 12px;
  margin: 10px 0 20px;
}
.home-service-panel button {
  display: flex;
  justify-content: space-between;
  width: 100%;
  border: 0;
  background: #ffffffb0;
  padding: 13px 16px;
  margin-top: 10px;
  color: #42658a;
  font-size: 13px;
  border-radius: 4px;
}
.home-service-panel button:hover {
  background: #fff;
  color: #176cc3;
}
@media (max-width: 1000px) {
  .resource-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .home-course-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .home-updates-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 600px) {
  .home-section-heading {
    padding: 30px 0 20px;
  }
  .home-section-heading h2 {
    font-size: 23px;
    gap: 14px;
  }
  .home-section-heading p {
    font-size: 11px;
  }
  .resource-banner {
    padding: 20px;
  }
  .resource-banner span {
    font-size: 22px;
  }
  .resource-banner p {
    font-size: 11px;
    letter-spacing: 0;
  }
  .resource-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 16px;
  }
  .resource-entry {
    padding: 18px 8px;
  }
  .home-course-toolbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }
  .course-filters {
    gap: 5px;
  }
  .course-filters button {
    font-size: 12px;
    padding: 7px 10px;
  }
  .home-course-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .home-course-body h3,
  .home-course-body p {
    min-height: 0;
  }
  .updates-list {
    padding: 18px;
  }
  .update-row {
    flex-wrap: wrap;
    gap: 8px;
  }
  .update-row strong {
    flex-basis: calc(100% - 95px);
  }
  .update-row time {
    margin-left: auto;
  }
}
@media (prefers-reduced-motion: reduce) {
  .resource-entry,
  .home-course-card {
    transition: none;
  }
}
</style>
