<script setup>
import { ArrowRight, GraduationCap, ShieldCheck, Users } from 'lucide-vue-next'

import { useUi } from '../composables/useUi'
const { go } = useUi()
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">SCHOOLS & COMMUNITY</span>
      <h1>学校与机构</h1>
      <p>连接学校、教师、学生与教育管理者。</p>
    </div>
    <Users :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body">
    <div class="org-hero">
      <div>
        <span class="kicker light">GROW TOGETHER</span>
        <h2>让优质科技教育<br />走进更多课堂</h2>
        <p>以课程资源、班级协作和作品成长记录，支持学校开展丰富的科技教育实践。</p>
      </div>
      <div class="org-shapes">
        <GraduationCap :size="88" :stroke-width="1" />
      </div>
    </div>
    <div class="org-grid">
      <div
        v-for="item in [
          {
            icon: GraduationCap,
            title: '学校管理',
            text: '组织课程与班级，构建学校专属学习空间。',
          },
          { icon: Users, title: '班级协作', text: '连接教师与学生，关注每个人的学习进度。' },
          { icon: ShieldCheck, title: '角色权限', text: '按不同角色提供清晰的入口与管理边界。' },
        ]"
        :key="item.title"
      >
        <component :is="item.icon" :size="27" />
        <h3>{{ item.title }}</h3>
        <p>{{ item.text }}</p>
      </div>
    </div>
    <div class="info-callout">
      <div>
        <h3>加入智启未来教育社区</h3>
        <p>学校入驻和机构管理功能需要后台服务支持，前端入口与展示页面已经预留。</p>
      </div>
      <button class="btn btn-primary" @click="go('/contact')">
        联系我们
        <ArrowRight :size="16" />
      </button>
    </div>
  </section>
</template>
