<script setup>
import {
  ArrowRight,
  ArrowUpRight,
  BookOpen,
  Bot,
  ClipboardList,
  Star,
  WandSparkles,
} from 'lucide-vue-next'
import { ref } from 'vue'
import HomeHero from '../components/home/HomeHero.vue'
import { featuredCourses } from '../data'
import { demoStore as store } from '../stores/demo'

import { useCourses } from '../composables/useCourses'
import { iconMap } from '../config/portal'
import { useUi } from '../composables/useUi'
const { dialog, go } = useUi()
const { openCourse } = useCourses()
const learningSpace = ref(null)
function explore() {
  learningSpace.value?.scrollIntoView({
    behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth',
    block: 'start',
  })
}
</script>

<template>
  <HomeHero @explore="explore" />

  <section ref="learningSpace" class="quick-section container home-learning-space">
    <div class="quick-heading">
      <span>QUICK ACCESS</span>
      <p>探索你的学习空间</p>
    </div>
    <div class="quick-grid">
      <button
        v-for="item in [
          {
            title: '精品课程',
            sub: '发现适合你的学习内容',
            path: '/courses',
            icon: BookOpen,
            color: 'blue',
          },
          {
            title: '学习任务',
            sub: '每一步成长都有记录',
            path: '/tasks',
            icon: ClipboardList,
            color: 'orange',
          },
          {
            title: '创作中心',
            sub: '把想法变成精彩作品',
            path: '/create',
            icon: WandSparkles,
            color: 'purple',
          },
          {
            title: 'AI 学习助手',
            sub: '随时陪你探索与思考',
            path: '/assistant',
            icon: Bot,
            color: 'green',
          },
        ]"
        :key="item.path"
        :class="['quick-card', item.color]"
        @click="go(item.path)"
      >
        <span class="quick-icon">
          <component :is="item.icon" :size="25" :stroke-width="1.8" /> </span
        ><span class="quick-copy"
          ><strong>{{ item.title }}</strong
          ><small>{{ item.sub }}</small></span
        >
        <ArrowUpRight class="quick-arrow" :size="19" />
      </button>
    </div>
  </section>

  <section class="section courses-section">
    <div class="container">
      <div class="section-top">
        <div>
          <span class="kicker">CURATED LEARNING</span>
          <h2>发现热爱，<span>从这里开始</span></h2>
          <p>多元课程与真实项目，让每一次学习都充满发现。</p>
        </div>
        <button class="text-link" @click="go('/courses')">
          查看全部课程
          <ArrowUpRight :size="18" />
        </button>
      </div>
      <div class="course-grid">
        <article
          v-for="course in featuredCourses.slice(0, 3)"
          :key="course.id"
          class="course-card"
          @click="openCourse(course.id)"
        >
          <div :class="['course-art', course.color]">
            <span class="course-art-grid"></span>
            <component :is="iconMap[course.icon]" :size="63" :stroke-width="1.15" /><span
              class="course-type"
              >{{ course.type }}</span
            >
          </div>
          <div class="course-body">
            <div class="course-meta">
              <span>{{ course.level }}</span
              ><span> <Star :size="13" fill="currentColor" /> 精品课程 </span>
            </div>
            <h3>{{ course.title }}</h3>
            <p>{{ course.summary }}</p>
            <div class="course-foot">
              <span> <BookOpen :size="15" /> {{ course.lessons }} 课时 </span
              ><span
                >{{ course.learners }} 人在学
                <ArrowRight :size="16" />
              </span>
            </div>
          </div>
        </article>
      </div>
    </div>
  </section>

  <section class="feature-section">
    <div class="container feature-inner">
      <div class="feature-text">
        <span class="kicker light">CREATE SOMETHING NEW</span>
        <h2>从灵感出发，<br />创造属于你的未来</h2>
        <p>
          图形化编程、Python 与三维设计，一个空间承载不同的创意表达。动手试一试，让作品替你说话。
        </p>
        <button class="btn btn-white" @click="go('/create')">
          进入创作中心
          <ArrowUpRight :size="18" />
        </button>
      </div>
      <div class="feature-panel">
        <div class="panel-top">
          <span class="dot red"></span><span class="dot yellow"></span
          ><span class="dot green-dot"></span><span>my_future.py</span>
        </div>
        <div class="panel-code">
          <span class="line-no">01</span><code><b>def</b> imagine_future():</code
          ><span class="line-no">02</span><code>&nbsp;&nbsp;idea = <i>"无限可能"</i></code
          ><span class="line-no">03</span><code>&nbsp;&nbsp;creativity = <i>True</i></code
          ><span class="line-no">04</span><code>&nbsp;&nbsp;<b>return</b> idea + creativity</code
          ><span class="line-no">05</span><code></code><span class="line-no">06</span
          ><code>imagine_future()<span class="cursor">|</span></code>
        </div>
        <div class="panel-status">
          <span class="status-dot"></span> Ready to create <span>Python 3.11</span>
        </div>
      </div>
    </div>
  </section>

  <section class="section news-home">
    <div class="container">
      <div class="section-top">
        <div>
          <span class="kicker">LATEST STORIES</span>
          <h2>发生在这里的<span>新鲜事</span></h2>
          <p>关注平台动态，发现更多精彩的科技教育故事。</p>
        </div>
        <button class="text-link" @click="go('/news')">
          更多资讯
          <ArrowUpRight :size="18" />
        </button>
      </div>
      <div class="news-home-grid">
        <article
          v-for="(item, index) in store.news.slice(0, 3)"
          :key="item.id"
          class="news-home-card"
          @click="dialog = { kind: 'news', item }"
        >
          <span class="news-index">0{{ index + 1 }}</span
          ><span class="news-date">{{ item.date }} <i>·</i> {{ item.category }}</span>
          <h3>{{ item.title }}</h3>
          <p>{{ item.excerpt }}</p>
          <span class="circle-arrow">
            <ArrowUpRight :size="19" />
          </span>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.home-learning-space {
  margin-top: 0;
  scroll-margin-top: calc(var(--portal-header-height) + 16px);
}
</style>
