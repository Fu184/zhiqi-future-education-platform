<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { ArrowRight } from 'lucide-vue-next'
import HomeHero from '../components/home/HomeHero.vue'
import { demoStore as store } from '../stores/demo'
import { homeSections } from '../data/homeSections'
import blossom from '../../img/DSC_2630.jpg'
import spring from '../../img/DSC_2744.jpg'
import bookshop from '../../img/DSC_0830.JPG'
const newsImages = [blossom, spring, bookshop]
const newsItems = computed(() => store.news.slice(0, 6))
const newsSlides = computed(() =>
  newsItems.value.slice(0, 3).map((item, index) => ({
    src: newsImages[index],
    alt: ['枝头盛开的粉色花朵', '明亮天空下的春日花枝', '夜色中的书店'][index],
    label: item.title,
    position: '50% 50%',
    to: { path: '/news', query: { article: item.id } },
  })),
)
</script>

<template>
  <div class="education-home">
    <div class="container home-banner"><HomeHero /></div>
    <div class="container home-content">
      <section class="home-news" aria-labelledby="home-news-title">
        <div class="home-section-heading">
          <div>
            <h2 id="home-news-title">资讯中心</h2>
            <p>关注科技教育，了解平台最新动态</p>
          </div>
          <RouterLink class="home-more" to="/news" aria-label="更多资讯"
            >更多 <ArrowRight :size="16"
          /></RouterLink>
        </div>
        <div v-if="newsItems.length" class="home-news-grid">
          <HomeHero :slides="newsSlides" variant="news" />
          <div class="home-news-links">
            <RouterLink
              v-for="item in newsItems"
              :key="item.id"
              :to="{ path: '/news', query: { article: item.id } }"
              class="home-news-link"
            >
              <span class="news-link-category">{{ item.category }}</span>
              <strong>{{ item.title }}</strong>
              <time>{{ item.date }}</time>
              <ArrowRight :size="15" />
            </RouterLink>
          </div>
        </div>
        <p v-else class="home-empty">暂无资讯，后续内容将在这里发布。</p>
      </section>

      <section
        v-for="section in homeSections"
        :key="section.id"
        :class="['home-center', `home-center-${section.id}`]"
        :aria-labelledby="`${section.id}-title`"
      >
        <div class="home-section-heading">
          <div>
            <h2 :id="`${section.id}-title`">{{ section.title }}</h2>
            <p>{{ section.description }}</p>
          </div>
          <RouterLink class="home-more" :to="section.path" :aria-label="`更多${section.title}内容`"
            >更多 <ArrowRight :size="16"
          /></RouterLink>
        </div>
        <div :class="['home-topic-grid', { 'home-topic-grid-wide': section.items.length === 2 }]">
          <RouterLink
            v-for="item in section.items"
            :key="item.path"
            class="home-topic-card"
            :to="item.path"
          >
            <div class="home-topic-art" aria-hidden="true">
              <component :is="item.icon" :size="54" :stroke-width="1.25" />
            </div>
            <div class="home-topic-copy">
              <div>
                <h3>{{ item.title }}</h3>
                <p>{{ item.description }}</p>
              </div>
              <ArrowRight :size="18" />
            </div>
          </RouterLink>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.education-home {
  background: #f3f5f7;
  color: #293b51;
  padding: 24px 0 72px;
}
.home-banner {
  overflow: hidden;
  border-radius: 0;
  background: #fff;
}
.home-content {
  display: grid;
  gap: 28px;
  margin-top: 26px;
}
.home-news,
.home-center {
  min-width: 0;
  background: #fff;
  padding: 26px;
  border: 1px solid #dfe5eb;
  border-radius: 0;
}
.home-section-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 17px;
  border-bottom: 2px solid #dce3eb;
}
.home-section-heading h2 {
  margin: 0 0 8px;
  padding-left: 12px;
  border-left: 4px solid #214f7d;
  color: #203e5d;
  font-size: 23px;
  font-weight: 700;
  line-height: 1.3;
}
.home-section-heading p {
  margin: 0;
  color: #687888;
  font-size: 12px;
  line-height: 1.8;
}
.home-more {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex: none;
  color: #52677d;
  font-size: 13px;
  padding: 8px 0 8px 12px;
}
.home-more:hover {
  color: #2875bd;
}
.home-news-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  align-items: stretch;
}
.home-news-links {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.home-news-link {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 12px;
  padding: 21px 0;
  border-bottom: 1px solid #e3e8ee;
}
.home-news-link:last-child {
  border-bottom: 0;
}
.news-link-category {
  color: #2875bd;
  font-size: 11px;
}
.home-news-link strong {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.home-news-link time {
  color: #718093;
  font-size: 11px;
  font-variant-numeric: tabular-nums;
}
.home-news-link > svg {
  color: #a9b6c4;
}
.home-news-link:hover strong {
  color: #2875bd;
  text-decoration: underline;
  text-underline-offset: 4px;
}
.home-empty {
  padding: 32px 0;
  color: #687888;
  font-size: 14px;
}
.home-topic-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}
.home-topic-grid-wide {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}
.home-topic-card {
  min-width: 0;
  overflow: hidden;
  border: 1px solid #dfe5eb;
  border-radius: 0;
  transition:
    border-color 0.2s,
    background 0.2s;
}
.home-topic-card:hover {
  border-color: #718fae;
  background: #f8fafc;
}
.home-topic-art {
  position: relative;
  display: grid;
  place-items: center;
  height: 120px;
  overflow: hidden;
  color: #365f88;
  background: #eef2f6;
  border-bottom: 1px solid #dfe5eb;
}
.home-topic-copy {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 20px 17px;
}
.home-topic-copy h3 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
}
.home-topic-copy p {
  margin: 0;
  color: #6d7c8d;
  font-size: 12px;
  line-height: 1.8;
}
.home-topic-copy > svg {
  color: #98abc0;
  flex: none;
}
@media (max-width: 1000px) {
  .home-topic-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .home-news-grid {
    gap: 20px;
  }
  .home-news-link {
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 8px;
    padding: 16px 0;
  }
  .news-link-category,
  .home-news-link > svg {
    display: none;
  }
}
@media (max-width: 700px) {
  .education-home {
    padding-top: 16px;
  }
  .home-content {
    gap: 22px;
    margin-top: 20px;
  }
  .home-news,
  .home-center {
    padding: 18px;
  }
  .home-section-heading {
    gap: 12px;
    margin-bottom: 18px;
  }
  .home-section-heading h2 {
    font-size: 20px;
  }
  .home-news-grid {
    grid-template-columns: minmax(0, 1fr);
    gap: 12px;
  }
  .home-topic-grid {
    gap: 12px;
  }
  .home-topic-art {
    height: 115px;
  }
  .home-topic-copy {
    padding: 16px 12px;
    gap: 5px;
  }
  .home-topic-copy h3 {
    font-size: 14px;
    line-height: 1.6;
  }
  .home-topic-copy p {
    font-size: 12px;
  }
  .home-topic-copy > svg {
    display: none;
  }
  .home-news-link {
    padding: 15px 0;
  }
}
@media (max-width: 360px) {
  .home-topic-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
@media (prefers-reduced-motion: reduce) {
  .home-topic-card {
    transition: none;
  }
}
</style>
