<script setup>
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { ChevronLeft, ChevronRight, Pause, Play } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import blossom from '../../../img/DSC_2630.jpg'
import spring from '../../../img/DSC_2744.jpg'
import bookshop from '../../../img/DSC_0830.JPG'
const props = defineProps({
  slides: { type: Array, default: undefined },
  variant: { type: String, default: 'banner' },
})
const defaultSlides = [
  { src: blossom, alt: '枝头盛开的粉色花朵', label: '自然之美', position: '50% 48%' },
  { src: spring, alt: '明亮天空下的春日花枝', label: '春日发现', position: '50% 50%' },
  { src: bookshop, alt: '夜色中亮起灯光的书店招牌', label: '阅读时光', position: '50% 52%' },
]
const slides = computed(() => (props.slides?.length ? props.slides : defaultSlides))
const current = ref(0),
  paused = ref(false),
  hovered = ref(false),
  focused = ref(false)
watch(
  () => slides.value.length,
  () => {
    current.value = 0
  },
)
const reducedMotion = ref(false),
  hidden = ref(false)
const autoplay = computed(
  () => !paused.value && !hovered.value && !focused.value && !reducedMotion.value && !hidden.value,
)
let timer, media
function select(index) {
  current.value = (index + slides.value.length) % slides.value.length
}
function changeMotion() {
  reducedMotion.value = media.matches
}
function changeVisibility() {
  hidden.value = document.hidden
}
onMounted(() => {
  media = window.matchMedia('(prefers-reduced-motion: reduce)')
  changeMotion()
  changeVisibility()
  media.addEventListener('change', changeMotion)
  document.addEventListener('visibilitychange', changeVisibility)
  timer = setInterval(() => {
    if (autoplay.value) select(current.value + 1)
  }, 5000)
})
onUnmounted(() => {
  clearInterval(timer)
  media?.removeEventListener('change', changeMotion)
  document.removeEventListener('visibilitychange', changeVisibility)
})
</script>

<template>
  <section
    :class="['home-carousel', { 'news-carousel': variant === 'news' }]"
    :aria-label="variant === 'news' ? '资讯图片轮播' : '首页图片轮播'"
    aria-roledescription="轮播图"
    @mouseenter="hovered = true"
    @mouseleave="hovered = false"
    @focusin="focused = true"
    @focusout="focused = $event.currentTarget.contains($event.relatedTarget)"
    @keydown.left.prevent="select(current - 1)"
    @keydown.right.prevent="select(current + 1)"
  >
    <h1 v-if="variant === 'banner'" class="visually-hidden">智启未来科技教育云平台</h1>
    <div class="carousel-stage">
      <div
        v-for="(slide, index) in slides"
        :key="slide.src"
        :class="['carousel-slide', { 'is-active': current === index }]"
        role="group"
        aria-roledescription="幻灯片"
        :aria-label="`${index + 1} / ${slides.length}：${slide.label}`"
        :aria-hidden="current !== index"
      >
        <img
          :src="slide.src"
          :alt="slide.alt"
          :style="{ objectPosition: slide.position }"
          :fetchpriority="index === 0 ? 'high' : 'auto'"
        />
        <RouterLink
          v-if="slide.to"
          :to="slide.to"
          class="slide-news-link"
          :tabindex="current === index ? 0 : -1"
        >
          <span>{{ slide.label }}</span>
        </RouterLink>
      </div>
      <button class="carousel-arrow previous" aria-label="上一张图片" @click="select(current - 1)">
        <ChevronLeft :size="27" />
      </button>
      <button class="carousel-arrow next" aria-label="下一张图片" @click="select(current + 1)">
        <ChevronRight :size="27" />
      </button>
    </div>
    <div class="carousel-toolbar">
      <div class="carousel-pagination">
        <button
          v-for="(slide, index) in slides"
          :key="slide.src"
          :class="['carousel-dot', { active: current === index }]"
          :aria-label="`切换到第 ${index + 1} 张图片：${slide.label}`"
          :aria-current="current === index ? 'true' : undefined"
          @click="select(index)"
        >
          <span></span>
        </button>
        <button
          class="carousel-pause"
          :aria-label="paused ? '播放轮播' : '暂停轮播'"
          :aria-pressed="paused"
          @click="paused = !paused"
        >
          <component :is="paused ? Play : Pause" :size="15" />
        </button>
      </div>
      <span class="carousel-caption" aria-live="off"
        >{{ variant === 'news' ? '资讯速览' : slides[current].label
        }}<span class="carousel-count">0{{ current + 1 }} / 0{{ slides.length }}</span></span
      >
    </div>
  </section>
</template>

<style scoped>
.home-carousel {
  background: #fff;
  min-width: 0;
}
.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip-path: inset(50%);
  white-space: nowrap;
}
.carousel-stage {
  position: relative;
  height: clamp(280px, 31vw, 480px);
  overflow: hidden;
  background: #eef2f4;
}
.carousel-slide {
  position: absolute;
  inset: 0;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.65s ease;
}
.carousel-slide.is-active {
  opacity: 1;
  pointer-events: auto;
}
.carousel-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.carousel-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  display: grid;
  place-items: center;
  width: 42px;
  height: 64px;
  border: 0;
  border-radius: 0;
  color: #fff;
  background: #15283e55;
  transition: background 0.2s;
}
.carousel-arrow:hover {
  background: #15283ea6;
}
.previous {
  left: max(20px, calc((100% - 1232px) / 2));
}
.next {
  right: max(20px, calc((100% - 1232px) / 2));
}
.carousel-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 52px;
  gap: 16px;
  padding-inline: 18px;
}
.news-carousel .carousel-stage {
  height: 232px;
  border-radius: 0;
}
.news-carousel .carousel-toolbar {
  min-height: 40px;
  padding-inline: 0;
}
.news-carousel .carousel-arrow {
  width: 30px;
  height: 40px;
}
.news-carousel .previous {
  left: 12px;
}
.news-carousel .next {
  right: 12px;
}
.slide-news-link {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-end;
  padding: 45px 18px 16px;
  background: linear-gradient(transparent 45%, #16263cd9);
  color: #fff;
}
.slide-news-link span {
  font-size: 14px;
  line-height: 1.8;
}
.slide-news-link:focus-visible {
  outline-offset: -4px;
}
.carousel-pagination {
  display: flex;
  align-items: center;
  gap: 3px;
}
.carousel-dot {
  display: grid;
  place-items: center;
  background: none;
  border: 0;
  padding: 12px 7px;
}
.carousel-dot span {
  width: 7px;
  height: 7px;
  background: #d3dbe4;
  border-radius: 0;
  transition:
    width 0.2s,
    background 0.2s;
}
.carousel-dot.active span {
  width: 30px;
  background: #214f7d;
}
.carousel-pause {
  display: grid;
  place-items: center;
  background: none;
  border: 0;
  padding: 9px;
  color: #68778a;
  margin-left: 8px;
}
.carousel-caption {
  color: #66788d;
  font-size: 12px;
}
.carousel-count {
  margin-left: 20px;
  font-variant-numeric: tabular-nums;
  color: #a3afbc;
}
@media (max-width: 600px) {
  .carousel-stage {
    height: 250px;
  }
  .carousel-arrow {
    width: 32px;
    height: 48px;
  }
  .previous {
    left: 12px;
  }
  .next {
    right: 12px;
  }
  .carousel-count {
    margin-left: 8px;
  }
}
@media (prefers-reduced-motion: reduce) {
  .carousel-slide,
  .carousel-dot span {
    transition: none;
  }
}
</style>
