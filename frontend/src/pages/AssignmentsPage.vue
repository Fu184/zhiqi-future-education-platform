<script setup>
import { ClipboardList } from 'lucide-vue-next'
import { demoStore as store } from '../stores/demo'
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">ASSIGNMENTS</span>
      <h1>作业表</h1>
      <p>查看当前演示任务及完成状态。</p>
    </div>
    <ClipboardList :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body">
    <div class="assignment-list">
      <article v-for="task in store.tasks" :key="task.id" class="assignment-row">
        <div>
          <span class="tag">{{ task.course }}</span>
          <h3>{{ task.title }}</h3>
          <p>{{ task.description }}</p>
        </div>
        <div class="assignment-meta">
          <span>截止：{{ task.due }}</span
          ><span
            :class="[
              'status-badge',
              task.status === '已完成' ? 'done' : task.status === '进行中' ? 'progress' : 'waiting',
            ]"
            >{{ task.status }}</span
          >
        </div>
      </article>
    </div>
    <div v-if="!store.tasks.length" class="empty-state">暂无作业</div>
  </section>
</template>
