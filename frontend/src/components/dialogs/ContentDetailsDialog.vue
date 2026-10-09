<script setup>
import { ArrowRight, CircleHelp, Heart, Play, Sparkles, X } from 'lucide-vue-next'
import { demoStore as store } from '../../stores/demo'
import { iconMap } from '../../config/portal'
import { useUi } from '../../composables/useUi'
import { useCourses } from '../../composables/useCourses'
const { dialog } = useUi()
const { currentCourse, toggleFavorite, enroll } = useCourses()
</script>
<template>
  <div v-if="dialog" class="modal-backdrop" @click.self="dialog = null">
    <div class="modal">
      <button class="modal-close" @click="dialog = null" aria-label="关闭">
        <X :size="19" /></button
      ><template v-if="dialog.kind === 'course' && currentCourse"
        ><span class="kicker">COURSE DETAILS</span>
        <div :class="['modal-icon', currentCourse.color]">
          <component :is="iconMap[currentCourse.icon]" :size="31" />
        </div>
        <h2>{{ currentCourse.title }}</h2>
        <p>{{ currentCourse.summary }}</p>
        <div class="modal-meta">
          <span>{{ currentCourse.type }}</span
          ><span>{{ currentCourse.level }}</span
          ><span>{{ currentCourse.lessons }} 课时</span>
        </div>
        <h3>课程章节</h3>
        <div class="chapter-list">
          <div v-for="(chapter, i) in currentCourse.chapters" :key="chapter">
            <span>0{{ i + 1 }}</span
            >{{ chapter }}
            <Play :size="15" />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="toggleFavorite(currentCourse.id)">
            <Heart
              :size="16"
              :fill="store.favorites.includes(currentCourse.id) ? 'currentColor' : 'none'"
            />{{ store.favorites.includes(currentCourse.id) ? '已收藏' : '收藏课程' }}</button
          ><button class="btn btn-primary" @click="enroll(currentCourse.id)">
            {{ store.enrolled.includes(currentCourse.id) ? '继续学习' : '开始学习' }}
            <ArrowRight :size="16" />
          </button>
        </div> </template
      ><template v-else-if="dialog.kind === 'news'"
        ><span class="kicker">{{ dialog.item.category }} / {{ dialog.item.date }}</span>
        <h2>{{ dialog.item.title }}</h2>
        <p>{{ dialog.item.excerpt }}</p>
        <div class="article-body">
          <p>{{ dialog.item.excerpt }}</p>
          <p>更多详细内容可在新闻管理中补充发布。关注平台动态，一起发现科技教育的更多可能。</p>
        </div> </template
      ><template v-else-if="dialog.kind === 'work'"
        ><span class="kicker">STUDENT SHOWCASE</span>
        <div :class="['modal-icon', dialog.item.color]">
          <component :is="iconMap[dialog.item.icon] || Sparkles" :size="31" />
        </div>
        <h2>{{ dialog.item.title }}</h2>
        <p>{{ dialog.item.description }}</p>
        <div class="modal-meta">
          <span>{{ dialog.item.type }}</span
          ><span>创作者：{{ dialog.item.author }}</span>
        </div>
        <div class="notice-bar">
          <CircleHelp :size="18" />作品文件与在线运行需要接入文件服务后展示。
        </div>
      </template>
    </div>
  </div>
</template>
