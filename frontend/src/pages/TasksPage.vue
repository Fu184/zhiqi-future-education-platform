<script setup>
import { ArrowRight, CalendarDays, ClipboardList } from 'lucide-vue-next'
import { demoStore as store } from '../stores/demo'

import { useTasks } from '../composables/useTasks'
import { useUi } from '../composables/useUi'
const { go } = useUi()
const { taskFilter, filteredTasks, markTask } = useTasks()
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">MY LEARNING JOURNEY</span>
      <h1>学习任务</h1>
      <p>从一个小目标开始，积累看得见的成长。</p>
    </div>
    <ClipboardList :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body">
    <div class="stats-row">
      <div>
        <strong>{{ store.tasks.length }}</strong
        ><span>全部任务</span>
      </div>
      <div>
        <strong>{{ store.tasks.filter((t) => t.status === '进行中').length }}</strong
        ><span>进行中</span>
      </div>
      <div>
        <strong>{{ store.tasks.filter((t) => t.status === '已完成').length }}</strong
        ><span>已完成</span>
      </div>
    </div>
    <div class="toolbar">
      <div class="tabs">
        <button
          v-for="cat in ['全部任务', '未开始', '进行中', '已完成']"
          :key="cat"
          :class="{ selected: taskFilter === cat }"
          @click="taskFilter = cat"
        >
          {{ cat }}
        </button>
      </div>
      <button class="btn btn-outline" @click="go('/courses')">
        发现更多课程
        <ArrowRight :size="16" />
      </button>
    </div>
    <div class="task-list">
      <article v-for="task in filteredTasks" :key="task.id" class="task-card">
        <div class="task-icon">
          <ClipboardList :size="24" />
        </div>
        <div class="task-main">
          <div class="task-meta">
            <span>{{ task.course }}</span
            ><span class="task-separator">·</span
            ><span> <CalendarDays :size="14" /> {{ task.due }} </span>
          </div>
          <h3>{{ task.title }}</h3>
          <p>{{ task.description }}</p>
        </div>
        <div class="task-side">
          <span
            :class="[
              'status-badge',
              task.status === '已完成' ? 'done' : task.status === '进行中' ? 'progress' : 'waiting',
            ]"
            >{{ task.status }}</span
          ><button class="task-action" @click="markTask(task)">
            {{ task.status === '已完成' ? '重新开始' : '标记完成' }}
            <ArrowRight :size="16" />
          </button>
        </div>
      </article>
    </div>
  </section>
</template>
