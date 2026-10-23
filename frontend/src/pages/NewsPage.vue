<script setup>
import { watch } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowUpRight, Newspaper, Search } from 'lucide-vue-next'

import { useNews } from '../composables/useNews'
import { useUi } from '../composables/useUi'
import { demoStore as store } from '../stores/demo'
const { dialog } = useUi()
const route = useRoute()
watch(
  () => route.query.article,
  (id) => {
    const item = store.news.find((item) => String(item.id) === String(id))
    if (item) dialog.value = { kind: 'news', item }
  },
  { immediate: true, flush: 'post' },
)
const { keyword, newsCategory, filteredNews, newsTabs } = useNews()
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">NEWS & INSIGHTS</span>
      <h1>{{ newsCategory === '全部' ? '新闻资讯' : newsCategory }}</h1>
      <p>了解平台动态，记录每一次探索与成长。</p>
    </div>
    <Newspaper :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body">
    <div class="toolbar">
      <div class="tabs">
        <button
          v-for="cat in newsTabs"
          :key="cat"
          :class="{ selected: newsCategory === cat }"
          @click="newsCategory = cat"
        >
          {{ cat }}
        </button>
      </div>
      <label class="search-field">
        <Search :size="17" /><input v-model="keyword" placeholder="搜索新闻" />
      </label>
    </div>
    <div class="news-list">
      <article
        v-for="item in filteredNews"
        :key="item.id"
        class="news-row"
        @click="dialog = { kind: 'news', item }"
      >
        <span class="news-row-date">{{ item.date }}</span>
        <div>
          <span class="tag">{{ item.category }}</span>
          <h3>{{ item.title }}</h3>
          <p>{{ item.excerpt }}</p>
        </div>
        <span class="row-arrow">
          <ArrowUpRight :size="20" />
        </span>
      </article>
    </div>
    <div v-if="!filteredNews.length" class="empty-state">该栏目暂无资讯</div>
  </section>
</template>
