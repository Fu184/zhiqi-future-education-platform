<script setup>
import { ArrowRight } from 'lucide-vue-next'
import { featuredCourses } from '../data'

import { infoPages } from '../config/portal'
import { useUi } from '../composables/useUi'
const { page, go } = useUi()
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">{{ infoPages[page].kicker }}</span>
      <h1>{{ infoPages[page].title }}</h1>
      <p>{{ infoPages[page].description }}</p>
    </div>
    <component :is="infoPages[page].icon" :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body info-page">
    <p>{{ infoPages[page].body }}</p>
    <button class="btn btn-primary" @click="go(infoPages[page].target)">
      {{ infoPages[page].action }}
      <ArrowRight :size="17" />
    </button>
    <div v-if="page === '/course-intro'" class="course-intro-list">
      <article v-for="course in featuredCourses" :key="course.id">
        <span class="tag">{{ course.type }}</span>
        <h3>{{ course.title }}</h3>
        <p>{{ course.summary }}</p>
      </article>
    </div>
  </section>
</template>
