<script setup>
import {
  BookOpen,
  ChevronRight,
  ClipboardList,
  Layers3,
  LayoutDashboard,
  Newspaper,
  Plus,
  ShieldCheck,
  Sparkles,
  Trash2,
  Check,
  X,
} from 'lucide-vue-next'
import { featuredCourses } from '../data'
import { demoStore as store } from '../stores/demo'

import { useContentAdmin } from '../composables/useContentAdmin'
import { useUi } from '../composables/useUi'
const { go } = useUi()
const {
  adminTab,
  removeAdminItem,
  adding,
  formTitle,
  formDescription,
  formCategory,
  newsTabs,
  addAdminItem,
} = useContentAdmin()
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">PLATFORM CONSOLE</span>
      <h1>后台管理</h1>
      <p>
        集中查看平台内容与学习数据。<button class="btn btn-outline" @click="go('/users')">
          用户管理
        </button>
      </p>
    </div>
    <LayoutDashboard :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body">
    <div class="notice-bar">
      <ShieldCheck :size="18" /><span
        >当前为前端演示工作台，数据保存在本机浏览器中；登录权限已接入后端；正式内容数据仍需接入服务端。</span
      >
    </div>
    <div class="admin-stats">
      <div>
        <Newspaper :size="22" /><strong>{{ store.news.length }}</strong
        ><span>新闻资讯</span>
      </div>
      <div>
        <BookOpen :size="22" /><strong>{{ featuredCourses.length }}</strong
        ><span>课程资源</span>
      </div>
      <div>
        <ClipboardList :size="22" /><strong>{{ store.tasks.length }}</strong
        ><span>学习任务</span>
      </div>
      <div>
        <Sparkles :size="22" /><strong>{{ store.works.length }}</strong
        ><span>展示作品</span>
      </div>
    </div>
    <div class="admin-layout">
      <aside>
        <span>内容管理</span
        ><button
          v-for="tab in ['新闻管理', '任务管理', '作品管理', '课程管理', '用户与学校', '系统设置']"
          :key="tab"
          :class="{ selected: adminTab === tab }"
          @click="adminTab = tab"
        >
          {{ tab }}
          <ChevronRight :size="16" />
        </button>
      </aside>
      <div class="admin-main">
        <div class="admin-main-head">
          <div>
            <h2>{{ adminTab }}</h2>
            <p>
              {{
                ['新闻管理', '任务管理', '作品管理'].includes(adminTab)
                  ? '管理平台展示内容和学习资料。'
                  : '该模块的完整操作将在接入后端服务后开放。'
              }}
            </p>
          </div>
          <button
            v-if="['新闻管理', '任务管理', '作品管理'].includes(adminTab)"
            class="btn btn-primary"
            @click="adding = true"
          >
            <Plus :size="16" /> 新增内容
          </button>
        </div>
        <template v-if="['新闻管理', '任务管理', '作品管理'].includes(adminTab)">
          <div
            v-for="item in adminTab === '新闻管理'
              ? store.news
              : adminTab === '任务管理'
                ? store.tasks
                : store.works"
            :key="item.id"
            class="admin-row"
          >
            <div>
              <span class="tag">{{ item.category || item.status || item.type }}</span
              ><strong>{{ item.title }}</strong
              ><small>{{ item.date || item.course || item.author }}</small>
            </div>
            <button title="删除" @click="removeAdminItem(item.id)">
              <Trash2 :size="17" />
            </button>
          </div>
        </template>
        <div v-else class="admin-placeholder">
          <Layers3 :size="43" :stroke-width="1.3" />
          <h3>管理入口已预留</h3>
          <p>{{ adminTab }}需要配合服务端数据与权限系统完成。</p>
        </div>
      </div>
    </div>
  </section>
  <div v-if="adding" class="modal-backdrop" @click.self="adding = false">
    <div class="modal" role="dialog" aria-modal="true" aria-label="新增内容">
      <button class="modal-close" @click="adding = false" aria-label="关闭">
        <X :size="19" />
      </button>
      <span class="kicker">CONTENT MANAGEMENT</span>
      <h2>新增{{ adminTab.replace('管理', '') }}</h2>
      <label class="form-field">名称<input v-model="formTitle" placeholder="请输入名称" /></label
      ><label class="form-field"
        >{{ adminTab === '新闻管理' ? '摘要' : '说明'
        }}<textarea v-model="formDescription" placeholder="请输入内容说明"></textarea></label
      ><label v-if="adminTab === '新闻管理'" class="form-field"
        >分类<select v-model="formCategory">
          <option v-for="category in newsTabs.slice(1)" :key="category">{{ category }}</option>
        </select></label
      ><button class="btn btn-primary modal-submit" @click="addAdminItem">
        保存内容
        <Check :size="16" />
      </button>
    </div>
  </div>
</template>
