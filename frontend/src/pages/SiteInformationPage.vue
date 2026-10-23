<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { site, hasPublicContact } from '../config/site'
const route = useRoute()
const isPrivacy = computed(() => route.path === '/privacy')
const title = computed(() => (isPrivacy.value ? '隐私说明' : '网站使用说明'))
const privacySections = [
  {
    title: '账户与登录',
    text: '注册和登录涉及手机号、姓名、密码及短信验证码。账户信息用于身份验证、账户维护和权限管理；密码由服务端进行哈希处理。短信验证码的实际发送方式以当前服务配置和页面提示为准。',
  },
  {
    title: '会话与访问记录',
    text: '登录状态通过会话 Cookie 维护。服务端会在登录、短信发送等请求中使用网络地址进行访问频率控制。请勿在公共设备上保存密码，使用完毕后及时退出登录。',
  },
  {
    title: '浏览器中的学习记录',
    text: '当前版本的作品、任务进度、课程收藏、学习记录与选课清单等数据保存在浏览器本地，并按账户区分。退出登录不会自动清除本地记录；清除网站数据或更换浏览器可能导致这些记录无法恢复。请勿在作品或记录中填写证件号码等敏感信息。',
  },
  {
    title: '第三方网站',
    text: '部分栏目链接至教育主管部门、考试机构等外部网站。访问后将适用对方的数据处理规则，请留意浏览器地址与对方的隐私说明。本网站展示的场景图片随站点加载。',
  },
  {
    title: '账户管理与咨询',
    text:
      '已登录用户可在账户设置中查看资料和修改密码。当前未提供在线自助注销入口。' +
      (hasPublicContact
        ? '个人信息相关咨询可通过本页或联系页面公布的渠道提出。'
        : '个人信息相关咨询渠道将在运营信息确认后公开。'),
  },
]
const usageSections = [
  {
    title: '信息使用',
    text: '本站栏目介绍、学习方法和准备指引用于帮助了解相关主题。考试、赛事、招生、认证和研学活动的具体要求，应以主管部门、组织机构或正式活动通知为准。',
  },
  {
    title: '资源与功能',
    text: '课程目录可查看主题、章节和适用阶段。课程视频、在线运行、订单支付与活动预约等功能以页面实际开放情况为准；学习助手当前提供预设的学习反馈。部分课程与新闻资料为展示内容，不能作为已开课或活动已开展的证明。',
  },
  {
    title: '作品与资料',
    text: '使用课程资料、上传或整理作品时，应尊重作者权益，注明引用来源，不得擅自公开他人的个人信息。请保留重要作品的独立备份。',
  },
  {
    title: '图片与合作关系',
    text: '首页部分图片为教育、工程与研学场景示意，不代表平台自有场地、已开展活动或已签约的合作机构。实际合作单位与活动信息以经核实的发布内容为准。',
  },
  {
    title: '外部链接',
    text: '带有外部网站标识的链接用于提供信息查询入口。网站内容与服务由相应机构负责，访问时请核对网址、通知来源及相关条件。',
  },
]
const sections = computed(() => (isPrivacy.value ? privacySections : usageSections))
</script>

<template>
  <article class="container site-information-page">
    <nav aria-label="当前位置">
      <RouterLink to="/">首页</RouterLink><span> / {{ title }}</span>
    </nav>
    <header>
      <h1>{{ title }}</h1>
      <p>{{ site.name }}</p>
    </header>
    <p class="information-intro">
      {{
        isPrivacy
          ? site.operator && hasPublicContact
            ? '以下说明对应网站当前实现的功能，联系信息见本页。功能或数据处理方式调整时，应同步更新说明。'
            : '以下说明对应网站当前实现的功能。运营主体和个人信息联系渠道信息尚未完整公布，正式对外服务前需补充相应信息并复核本说明。'
          : '请在使用资源与服务前了解以下内容。具体课程、活动与服务条件以相应页面发布的信息为准。'
      }}
    </p>
    <section v-for="(section, index) in sections" :key="section.title">
      <h2>{{ index + 1 }}. {{ section.title }}</h2>
      <p>{{ section.text }}</p>
    </section>
    <section v-if="site.operator || hasPublicContact">
      <h2>联系信息</h2>
      <p v-if="site.operator">运营单位：{{ site.operator }}</p>
      <p v-if="site.email">
        邮箱：<a :href="`mailto:${site.email}`">{{ site.email }}</a>
      </p>
      <p v-if="site.phone">电话：{{ site.phone }}</p>
      <p v-if="site.address">地址：{{ site.address }}</p>
    </section>
    <RouterLink class="information-back" to="/contact">查看联系信息</RouterLink>
  </article>
</template>

<style scoped>
.site-information-page {
  padding-top: 28px;
  padding-bottom: 80px;
  color: #293b51;
}
.site-information-page > nav {
  font-size: 13px;
  color: #6d7c8d;
}
.site-information-page header {
  margin-top: 26px;
  padding-bottom: 20px;
  border-bottom: 2px solid #dfe5eb;
}
.site-information-page h1 {
  font-size: 30px;
  margin-bottom: 12px;
}
.site-information-page header p {
  color: #6d7c8d;
  font-size: 14px;
}
.information-intro {
  margin-top: 26px;
  padding: 18px 22px;
  background: #eaf0f6;
  border-left: 3px solid #547b9f;
}
.site-information-page section {
  max-width: 880px;
  margin-top: 30px;
}
.site-information-page h2 {
  font-size: 18px;
  font-weight: 600;
}
.site-information-page p {
  line-height: 2;
  font-size: 14px;
}
.information-back {
  display: inline-block;
  margin-top: 24px;
  color: #28649c;
}
.site-information-page a:hover {
  text-decoration: underline;
  text-underline-offset: 4px;
}
@media (max-width: 700px) {
  .site-information-page h1 {
    font-size: 26px;
  }
}
</style>
