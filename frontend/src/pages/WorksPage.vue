<script setup>
import { ArrowUpRight, Plus, Sparkles, UserRound } from 'lucide-vue-next'
import { demoStore as store } from '../stores/demo'

import { iconMap } from '../config/portal'
import { useUi } from '../composables/useUi'
const { dialog, go } = useUi()
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">STUDENT SHOWCASE</span>
      <h1>作品展示</h1>
      <p>每一个作品，都是勇敢探索的证明。</p>
    </div>
    <Sparkles :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body">
    <div class="section-top compact">
      <div>
        <h2>创意作品集</h2>
        <p>看看大家如何把灵感变成现实。</p>
      </div>
      <button class="btn btn-primary" @click="go('/create')"><Plus :size="17" /> 创作新作品</button>
    </div>
    <div class="works-grid">
      <article
        v-for="work in store.works"
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
          <div>
            <span> <UserRound :size="14" /> {{ work.author }} </span>
            <ArrowUpRight :size="18" />
          </div>
        </div>
      </article>
    </div>
  </section>
</template>
