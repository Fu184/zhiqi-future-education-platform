<script setup>
import { ArrowDown, ArrowUpRight, Play } from 'lucide-vue-next'
import { useUi } from '../../composables/useUi'

const { go } = useUi()
defineEmits(['explore'])
</script>

<template>
  <section class="home-hero" aria-labelledby="home-title">
    <div class="home-hero-content">
      <p class="home-hero-eyebrow">智启未来 · 科技教育云平台</p>
      <h1 id="home-title">让每一个奇思妙想<br /><span>都有发生的可能</span></h1>
      <p class="home-hero-description">
        从探索知识到动手创造，连接课程、任务与作品。<br />
        在这里，开启属于你的未来学习之旅。
      </p>
      <div class="home-hero-code" aria-label="让想象力成为创造力">
        <span>print</span>(<b>"让想象力成为创造力"</b>)<i aria-hidden="true">_</i>
      </div>
      <div class="home-hero-actions">
        <button class="btn btn-primary" @click="go('/courses')">
          探索课程 <ArrowUpRight :size="18" />
        </button>
        <button class="btn home-create-button" @click="go('/create')">
          <Play :size="16" />开始创作
        </button>
      </div>
    </div>
    <button class="home-scroll-cue" @click="$emit('explore')">
      <span>向下探索你的学习空间</span><ArrowDown :size="20" />
    </button>
  </section>
</template>

<style scoped>
.home-hero {
  min-height: 100vh;
  min-height: 100dvh;
  box-sizing: border-box;
  position: relative;
  isolation: isolate;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: calc(var(--portal-header-height) + 56px) 24px 112px;
  background: #fffafb;
  overflow: hidden;
}
.home-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: -1;
  background:
    radial-gradient(ellipse at 30% 38%, #fbe4e7 0%, transparent 52%),
    radial-gradient(ellipse at 76% 60%, #ede9fb 0%, transparent 48%),
    linear-gradient(175deg, transparent 75%, #fff 100%);
}
.home-hero-content {
  text-align: center;
  max-width: 900px;
}
.home-hero-eyebrow {
  color: #8b6976;
  font-size: 14px;
  letter-spacing: 3px;
  margin: 0 0 24px;
}
h1 {
  margin: 0;
  color: #35333a;
  font-size: clamp(38px, 4.6vw, 68px);
  line-height: 1.3;
  letter-spacing: -2px;
  font-weight: 800;
  text-wrap: balance;
}
h1 span {
  color: #cf627b;
}
.home-hero-description {
  color: #75717b;
  font-size: 17px;
  line-height: 1.9;
  margin: 30px 0 24px;
  text-wrap: balance;
}
.home-hero-code {
  font-family: Consolas, monospace;
  font-size: 17px;
  color: #5a5462;
}
.home-hero-code > span {
  color: #8465aa;
}
.home-hero-code b {
  font-weight: 500;
  color: #b25f78;
}
.home-hero-code i {
  font-style: normal;
  animation: home-cursor 1.2s step-end infinite;
}
.home-hero-actions {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 36px;
}
.home-create-button {
  background: #fff9;
  color: #61596c;
  border: 1px solid #ddd4e0;
}
.home-create-button:hover {
  background: #fff;
}
.home-scroll-cue {
  position: absolute;
  bottom: 28px;
  border: 0;
  background: transparent;
  color: #8c7e8d;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  padding: 12px 18px;
}
button:focus-visible {
  outline: 2px solid #b25f78;
  outline-offset: 5px;
}
@keyframes home-cursor {
  50% {
    opacity: 0;
  }
}
@media (max-width: 700px) {
  .home-hero {
    padding-top: calc(var(--portal-header-height) + 40px);
  }
  .home-hero-eyebrow {
    font-size: 12px;
    letter-spacing: 2px;
  }
  h1 {
    font-size: clamp(30px, 8.5vw, 46px);
    letter-spacing: -1px;
  }
  .home-hero-description {
    font-size: 14px;
    margin-top: 24px;
  }
  .home-hero-code {
    font-size: 13px;
  }
  .home-hero-actions {
    margin-top: 28px;
  }
}
@media (max-height: 650px) and (min-width: 701px) {
  .home-hero {
    padding-top: calc(var(--portal-header-height) + 24px);
    padding-bottom: 90px;
  }
  h1 {
    font-size: 42px;
  }
  .home-hero-description {
    margin: 18px 0;
  }
  .home-hero-actions {
    margin-top: 22px;
  }
}
@media (prefers-reduced-motion: reduce) {
  .home-hero-code i {
    animation: none;
  }
}
</style>
