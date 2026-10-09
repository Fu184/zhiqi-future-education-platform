<script setup>
import { Plus, Sparkles } from 'lucide-vue-next'
import { demoStore as store } from '../stores/demo'

import { useWorks } from '../composables/useWorks'
import { iconMap } from '../config/portal'
import { useUi } from '../composables/useUi'
const { dialog, go } = useUi()
const { myWorks } = useWorks()
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">MY WORKS</span>
      <h1>我的作品</h1>
      <p>查看你在此浏览器中保存的创作。</p>
    </div>
    <Sparkles :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body">
    <div class="section-top compact">
      <div>
        <h2>我的创作记录</h2>
        <p>作品保存在当前浏览器中。</p>
      </div>
      <button class="btn btn-primary" @click="go('/create')"><Plus :size="17" /> 创作新作品</button>
    </div>
    <div v-if="myWorks.length" class="works-grid">
      <article
        v-for="work in myWorks"
        :key="work.id"
        class="work-card"
        @click="dialog = { kind: 'work', item: work }"
      >
        <div :class="['work-art', work.color]">
          <component :is="iconMap[work.icon] || Sparkles" :size="68" :stroke-width="1.1" /><span>{{
            work.type
          }}</span>
        </div>
        <div class="work-info">
          <span class="tag">{{ work.type }}</span>
          <h3>{{ work.title }}</h3>
          <p>{{ work.description }}</p>
        </div>
      </article>
    </div>
    <div v-else class="empty-state">
      {{ store.user ? '你还没有保存作品，去创作中心试试吧。' : '登录后查看自己的作品。' }}
    </div>
  </section>
</template>
