<script setup>
import { ArrowRight, Compass, Mail, MapPin, Phone, Building2 } from 'lucide-vue-next'

import { useUi } from '../composables/useUi'
import { site, hasPublicContact } from '../config/site'
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
        <span v-if="site.operator"><Building2 :size="17" />运营单位：{{ site.operator }}</span>
        <template v-if="hasPublicContact">
          <span v-if="site.phone"
            ><Phone :size="17" /><a :href="`tel:${site.phone.replace(/[^+0-9-]/g, '')}`">{{
              site.phone
            }}</a></span
          >
          <span v-if="site.email"
            ><Mail :size="17" /><a :href="`mailto:${site.email}`">{{ site.email }}</a></span
          >
          <span v-if="site.address"><MapPin :size="17" />{{ site.address }}</span>
        </template>
        <p v-else>公开联系方式尚未发布。确认后的咨询渠道将在本页统一公布。</p>
        <p>课程与活动安排请关注平台发布的具体说明。</p>
        <button @click="go('/news')">查看平台资讯<ArrowRight :size="16" /></button>
      </div>
    </div>
  </section>
</template>
