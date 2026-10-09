<script setup>
import { ArrowRight, ShieldCheck, ShoppingBag } from 'lucide-vue-next'
import { featuredCourses } from '../data'

import { useShop } from '../composables/useShop'
import { iconMap } from '../config/portal'
import { useUi } from '../composables/useUi'
const { route } = useUi()
const { cart, addCart } = useShop()
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">COURSE MARKETPLACE</span>
      <h1>商品与课程</h1>
      <p>为学校、老师和学生提供丰富的课程资源。</p>
    </div>
    <ShoppingBag :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body">
    <div v-if="route.query.notice === 'learning-required'" class="notice-bar">
      当前为普通账号，购买学习资料或由老师创建学习账号后可使用学习功能。
    </div>
    <div class="notice-bar">
      <ShieldCheck :size="18" /><span
        >课程价格、订单和支付接口将在后端接入后启用；这里展示选课清单交互。</span
      >
    </div>
    <div class="section-top compact">
      <div>
        <h2>精选课程资源</h2>
        <p>探索适合不同学习阶段的主题课程。</p>
      </div>
      <span class="cart-count"> <ShoppingBag :size="17" /> 选课清单 {{ cart.length }} </span>
    </div>
    <div class="shop-grid">
      <article v-for="course in featuredCourses" :key="course.id" class="shop-card">
        <div :class="['shop-icon', course.color]">
          <component :is="iconMap[course.icon]" :size="27" />
        </div>
        <span class="tag">{{ course.type }}</span>
        <h3>{{ course.title }}</h3>
        <p>{{ course.summary }}</p>
        <div>
          <span>{{ course.lessons }} 课时 · {{ course.level }}</span
          ><button @click="addCart(course.id)">
            {{ cart.includes(course.id) ? '已加入' : '加入清单' }}
            <ArrowRight :size="15" />
          </button>
        </div>
      </article>
    </div>
  </section>
</template>
