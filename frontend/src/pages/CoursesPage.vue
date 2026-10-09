<script setup>
import { ArrowRight, BookOpen, Search, Star } from 'lucide-vue-next'

import { useCourses } from '../composables/useCourses'
import { iconMap } from '../config/portal'
const { keyword, courseCategory, filteredCourses, openCourse } = useCourses()
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">LEARNING CENTER</span>
      <h1>教学中心</h1>
      <p>打开一门课程，开启一段充满好奇的旅程。</p>
    </div>
    <BookOpen :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body">
    <div class="toolbar">
      <div class="tabs">
        <button
          v-for="cat in ['全部课程', '人工智能', '编程创造', '机器人', '创意设计']"
          :key="cat"
          :class="{ selected: courseCategory === cat }"
          @click="courseCategory = cat"
        >
          {{ cat }}
        </button>
      </div>
      <label class="search-field">
        <Search :size="17" /><input v-model="keyword" placeholder="搜索课程" />
      </label>
    </div>
    <div class="course-grid all-courses">
      <article
        v-for="course in filteredCourses"
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
  </section>
</template>
