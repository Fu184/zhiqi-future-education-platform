<script setup>
import { ArrowRight, ArrowUpRight, Compass, Mail, MapPin } from 'lucide-vue-next'

import { useUi } from '../composables/useUi'
const { page, go } = useUi()
</script>

<template>
  <section class="page-banner container">
    <div>
      <span class="kicker">ABOUT ZHIQI FUTURE</span>
      <h1>{{ page === '/contact' ? '联系我们' : '关于我们' }}</h1>
      <p>让科技教育为每一个孩子打开更广阔的未来。</p>
    </div>
    <Compass :size="124" :stroke-width="1" />
  </section>
  <section class="container page-body">
    <div class="about-grid">
      <div>
        <span class="kicker">OUR VISION</span>
        <h2>用科技点亮好奇心</h2>
        <p>
          智启未来科技教育平台面向学校、教师与学生，连接教学资源、创作工具与成长记录。我们希望让学习从问题出发，在实践中发生。
        </p>
        <button class="btn btn-primary" @click="go('/courses')">
          探索课程
          <ArrowRight :size="17" />
        </button>
      </div>
      <div class="about-contact">
        <h3>联系我们</h3>
        <p>联系方式和咨询入口请在正式上线前配置。</p>
        <span> <Mail :size="17" /> 邮箱：待配置 </span
        ><span> <MapPin :size="17" /> 地址：待配置 </span
        ><button @click="go('/assistant')">
          咨询学习助手
          <ArrowUpRight :size="16" />
        </button>
      </div>
    </div>
  </section>
</template>
