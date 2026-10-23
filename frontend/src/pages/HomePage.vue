<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { ArrowRight } from 'lucide-vue-next'
import HomeHero from '../components/home/HomeHero.vue'
import { demoStore as store } from '../stores/demo'
import { homeSections } from '../data/homeSections'
import classroom from '../../img/classroom.jpg'
import digitalLearning from '../../img/digital-learning.jpg'
import engineering from '../../img/engineering.jpg'
import laboratory from '../../img/laboratory.jpg'
import university from '../../img/university.jpg'
import seminar from '../../img/seminar.jpg'
const newsImages = [classroom, digitalLearning, seminar]
const newsImageAlts = ['课堂教学场景示意', '数字化学习场景示意', '教学交流场景示意']
const covers = {
  '/course-services/online': {
    src: digitalLearning,
    alt: '通过电脑学习数字课程的场景示意',
    label: '在线学习',
  },
  '/course-services/open-classes': {
    src: classroom,
    alt: '教师开展课堂教学的场景示意',
    label: '公开课堂',
  },
  '/products/tools': {
    src: engineering,
    alt: '工程实践设备与操作场景示意',
    label: '教学工具与实践器材',
  },
  '/products/resources': {
    src: digitalLearning,
    alt: '使用电脑查阅学习资料的场景示意',
    label: '课程与实践资源',
  },
  '/study-tours/enterprises': { src: engineering, alt: '企业工程实践场景示意' },
  '/study-tours/universities': { src: university, alt: '大学校园建筑场景示意' },
  '/study-tours/institutes': { src: laboratory, alt: '科研实验器具场景示意' },
  '/study-tours/experts': { src: seminar, alt: '专业人员交流场景示意' },
}
const centerIntroductions = {
  competitions: {
    title: '赛事与成长支持',
    text: '按需查找赛事服务、成长规划、科技特长及考试信息。',
  },
  teachers: { title: '教师专业发展', text: '从政策研读到课堂实施，连接教学资源与教研交流。' },
}
const newsItems = computed(() => store.news.slice(0, 6))
const newsSlides = computed(() =>
  newsItems.value.slice(0, 3).map((item, index) => ({
    src: newsImages[index],
    alt: newsImageAlts[index],
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
              v-for="(item, index) in newsItems"
              :key="item.id"
              :to="{ path: '/news', query: { article: item.id } }"
              :class="['home-news-link', { 'home-news-lead': index === 0 }]"
            >
              <span class="news-link-category">{{ item.category }}</span>
              <strong>{{ item.title }}</strong>
              <p v-if="index === 0" class="news-link-excerpt">{{ item.excerpt }}</p>
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
        <div
          :class="[
            'home-center-body',
            { 'home-center-body-services': centerIntroductions[section.id] },
          ]"
        >
          <aside v-if="centerIntroductions[section.id]" class="center-introduction">
            <span>{{ centerIntroductions[section.id].title }}</span>
            <p>{{ centerIntroductions[section.id].text }}</p>
            <component
              :is="section.items[0].icon"
              :size="54"
              :stroke-width="1"
              aria-hidden="true"
            />
          </aside>
          <div :class="['home-topic-grid', { 'home-topic-grid-wide': section.items.length === 2 }]">
            <RouterLink
              v-for="item in section.items"
              :key="item.path"
              class="home-topic-card"
              :to="item.path"
            >
              <div v-if="covers[item.path]" class="home-topic-cover">
                <img
                  :src="covers[item.path].src"
                  :alt="covers[item.path].alt"
                  loading="lazy"
                  width="1000"
                  height="667"
                />
                <span v-if="covers[item.path].label" class="topic-cover-label">{{
                  covers[item.path].label
                }}</span>
              </div>
              <span v-else class="home-topic-art" aria-hidden="true"
                ><component :is="item.icon" :size="28" :stroke-width="1.5"
              /></span>
              <div class="home-topic-copy">
                <div>
                  <h3>{{ item.title }}</h3>
                  <p>{{ item.description }}</p>
                </div>
                <ArrowRight :size="17" />
              </div>
            </RouterLink>
          </div>
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
  color: #2167a2;
  text-decoration: underline;
  text-underline-offset: 4px;
}
.home-news-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 30px;
  align-items: stretch;
}
.home-news-grid :deep(.news-carousel .carousel-stage) {
  height: 280px;
}
.home-news-links {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.home-news-link {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 12px;
  padding: 19px 0;
  border-bottom: 1px solid #e3e8ee;
}
.home-news-link:last-child {
  border-bottom: 0;
}
.news-link-category {
  color: #28649c;
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
  color: #7c91a7;
}
.home-news-link:hover strong {
  color: #2167a2;
  text-decoration: underline;
  text-underline-offset: 4px;
}
.home-news-lead {
  grid-template-columns: 1fr auto;
  gap: 10px 16px;
  padding: 2px 0 20px;
}
.home-news-lead .news-link-category {
  grid-column: 1;
}
.home-news-lead strong {
  grid-column: 1 / -1;
  grid-row: 2;
  white-space: normal;
  font-size: 19px;
  font-weight: 600;
  color: #203e5d;
  line-height: 1.6;
}
.news-link-excerpt {
  grid-column: 1 / -1;
  margin: 0;
  color: #6d7c8d;
  font-size: 12px;
  line-height: 1.9;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.home-news-lead time {
  grid-column: 2;
  grid-row: 1;
}
.home-news-lead > svg {
  display: none;
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
  transition:
    border-color 0.2s,
    background 0.2s;
}
.home-topic-card:hover {
  border-color: #718fae;
  background: #f8fafc;
}
.home-topic-cover {
  position: relative;
  height: 160px;
  background: #e6edf3;
  overflow: hidden;
}
.home-topic-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.topic-cover-label {
  position: absolute;
  left: 0;
  bottom: 0;
  padding: 7px 12px;
  color: #fff;
  background: #173d64e8;
  font-size: 12px;
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
  line-height: 1.6;
}
.home-topic-copy p {
  margin: 0;
  color: #6d7c8d;
  font-size: 12px;
  line-height: 1.9;
}
.home-topic-copy > svg {
  color: #728ba5;
  flex: none;
}
.home-center-courses .home-topic-card {
  display: grid;
  grid-template-columns: 48% minmax(0, 1fr);
}
.home-center-courses .home-topic-cover {
  height: 210px;
}
.home-center-courses .home-topic-copy {
  align-items: flex-start;
  flex-direction: column;
  justify-content: center;
  padding: 24px;
}
.home-center-courses .home-topic-copy h3 {
  color: #203e5d;
  font-size: 19px;
}
.home-center-courses .home-topic-copy > svg {
  margin-top: 12px;
}
.home-center-products .home-topic-card {
  display: grid;
  grid-template-columns: 40% minmax(0, 1fr);
}
.home-center-products .home-topic-cover {
  height: 170px;
}
.home-center-products .topic-cover-label {
  display: none;
}
.home-center-body-services {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 24px;
}
.center-introduction {
  position: relative;
  min-width: 0;
  overflow: hidden;
  padding: 24px;
  border-top: 3px solid #315d86;
  background: #edf2f7;
}
.center-introduction > span {
  color: #203e5d;
  font-size: 18px;
  font-weight: 600;
}
.center-introduction p {
  position: relative;
  z-index: 1;
  max-width: 180px;
  margin: 12px 0 48px;
  color: #64788d;
  font-size: 12px;
  line-height: 1.9;
}
.center-introduction > svg {
  position: absolute;
  right: 22px;
  bottom: 18px;
  color: #a8bdd1;
}
.home-center-body-services .home-topic-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 24px;
}
.home-center-body-services .home-topic-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px 0;
  border: 0;
  border-bottom: 1px solid #e0e6ed;
}
.home-center-body-services .home-topic-card:nth-last-child(-n + 2) {
  border-bottom: 0;
}
.home-topic-art {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  background: #edf2f7;
  color: #365f88;
  flex: none;
}
.home-center-body-services .home-topic-copy {
  flex: 1;
  min-width: 0;
  padding: 0;
}
.home-center-body-services .home-topic-copy h3 {
  color: #203e5d;
}
.home-center-study-tours .home-topic-card {
  border: 0;
}
.home-center-study-tours .home-topic-copy {
  padding: 16px 0 0;
}
.home-center-study-tours .home-topic-cover {
  height: 155px;
}
@media (max-width: 1100px) {
  .home-center-courses .home-topic-card {
    grid-template-columns: minmax(0, 1fr);
  }
  .home-center-courses .home-topic-cover {
    height: 180px;
  }
  .home-center-courses .home-topic-copy {
    padding: 20px;
  }
  .home-center-body-services {
    grid-template-columns: 195px minmax(0, 1fr);
    gap: 20px;
  }
  .home-center-body-services .home-topic-grid {
    gap: 0 16px;
  }
  .home-center-body-services .home-topic-card {
    gap: 12px;
  }
}
@media (max-width: 1000px) {
  .home-topic-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .home-news-grid {
    gap: 20px;
  }
  .home-news-link:not(.home-news-lead) {
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 8px;
    padding: 16px 0;
  }
  .home-news-link:not(.home-news-lead) .news-link-category,
  .home-news-link > svg {
    display: none;
  }
  .home-center-body-services {
    grid-template-columns: minmax(0, 1fr);
  }
  .center-introduction {
    display: none;
  }
  .home-center-products .home-topic-card {
    grid-template-columns: minmax(0, 1fr);
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
    gap: 16px;
  }
  .home-news-grid :deep(.news-carousel .carousel-stage) {
    height: 210px;
  }
  .home-news-lead strong {
    font-size: 17px;
  }
  .home-topic-grid {
    gap: 14px;
  }
  .home-center-courses .home-topic-cover,
  .home-center-products .home-topic-cover,
  .home-center-study-tours .home-topic-cover {
    height: 125px;
  }
  .home-center-courses .home-topic-copy,
  .home-topic-copy {
    padding: 16px 12px;
    gap: 5px;
  }
  .home-center-courses .home-topic-copy h3,
  .home-topic-copy h3 {
    font-size: 15px;
  }
  .home-topic-copy > svg {
    display: none;
  }
  .home-center-courses .topic-cover-label {
    display: none;
  }
  .home-center-body-services .home-topic-card {
    align-items: flex-start;
    flex-direction: column;
    padding: 18px 0;
    gap: 12px;
  }
  .home-topic-art {
    width: 40px;
    height: 40px;
  }
}
@media (max-width: 360px) {
  .home-center-courses .home-topic-grid,
  .home-center-products .home-topic-grid {
    grid-template-columns: minmax(0, 1fr);
  }
  .home-center-body-services .home-topic-grid {
    grid-template-columns: minmax(0, 1fr);
  }
  .home-center-body-services .home-topic-card {
    flex-direction: row;
  }
  .home-center-body-services .home-topic-card:nth-last-child(2) {
    border-bottom: 1px solid #e0e6ed;
  }
}
@media (prefers-reduced-motion: reduce) {
  .home-topic-card {
    transition: none;
  }
}
</style>
