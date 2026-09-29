<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowDownRight, ArrowLeft, ArrowRight, ArrowUpRight, Bell, BookOpen, Bot,
  Box, BrainCircuit, CalendarDays, Check, CheckCircle2, ChevronDown,
  ChevronRight, CircleHelp, ClipboardList, CloudSun, Code2, Compass,
  FileText, Filter, GraduationCap, Heart, Layers3, LayoutDashboard,
  Lightbulb, Mail, MapPin, Menu, MessageCircle, MoreHorizontal, Newspaper,
  Play, Plus, Rocket, Search, Send, Settings2, ShieldCheck, ShoppingBag,
  Sparkles, Star, Trash2, UploadCloud, UserRound, Users, WandSparkles, X,
} from 'lucide-vue-next'
import { featuredCourses, store } from './data'

const route = useRoute()
const router = useRouter()
const iconMap = { BrainCircuit, Code2, Bot, Box, Rocket, CloudSun, BookOpen, Newspaper, Users, ShoppingBag, ClipboardList, Sparkles }
const nav = [
  { label: '首页', path: '/' },
  { label: '新闻资讯', path: '/news', children: [
    { label: '相关活动', path: '/news?section=activities' },
    { label: '赛事', path: '/news?section=events' },
    { label: '政策', path: '/news?section=policies' },
  ] },
  { label: '教学中心', path: '/courses', children: [
    { label: '教师成长中心', path: '/teacher-growth' },
    { label: '课程资源', path: '/courses' },
    { label: '学习任务', path: '/tasks' },
    { label: '作业表', path: '/assignments' },
    { label: '我的作品', path: '/my-works' },
    { label: '创作中心', path: '/create' },
  ] },
  { label: '商城', path: '/shop' },
  { label: '关于我们', path: '/about', children: [
    { label: '公司介绍', path: '/about' },
    { label: '师资介绍', path: '/faculty' },
    { label: '课程介绍', path: '/course-intro' },
  ] },
  { label: '加入我们', path: '/join' },
]
const page = computed(() => route.path)
const mobileOpen = ref(false)
const dialog = ref(null)
const toast = ref('')
const keyword = ref('')
const newsCategory = ref('全部')
const courseCategory = ref('全部课程')
const taskFilter = ref('全部任务')
const createMode = ref('Scratch')
const selectedBlocks = ref(['当绿旗被点击', '说「你好，未来！」', '等待 1 秒'])
const pythonCode = ref('print("你好，未来！")\n\nfor step in range(3):\n    print("探索第", step + 1, "步")')
const previewText = ref('')
const workTitle = ref('我的创意作品')
const aiInput = ref('')
const aiMessages = ref([{ role: 'assistant', text: '你好！我是智启 AI 学习助手。你可以问我课程知识，也可以请我帮你梳理学习思路。' }])
const loginName = ref('')
const adminTab = ref('新闻管理')
const formTitle = ref('')
const formDescription = ref('')
const formCategory = ref('平台动态')
const cart = ref([])

const filteredNews = computed(() => store.news.filter(item =>
  (newsCategory.value === '全部' || item.category === newsCategory.value) &&
  `${item.title}${item.excerpt}`.includes(keyword.value.trim())
))
const filteredCourses = computed(() => featuredCourses.filter(item =>
  (courseCategory.value === '全部课程' || item.type === courseCategory.value) &&
  `${item.title}${item.summary}`.includes(keyword.value.trim())
))
const filteredTasks = computed(() => store.tasks.filter(item => taskFilter.value === '全部任务' || item.status === taskFilter.value))
const currentCourse = computed(() => dialog.value?.kind === 'course' ? featuredCourses.find(c => c.id === dialog.value.id) : null)
const myWorks = computed(() => store.user ? store.works.filter(work => work.author === store.user.name) : [])
const newsSections = { activities: '相关活动', events: '赛事', policies: '政策' }
const newsTabs = ['全部', '相关活动', '赛事', '政策', '平台动态', '教学新闻', '学校新闻', '公告']
const navGroups = {
  '/courses': ['/courses', '/teacher-growth', '/tasks', '/assignments', '/my-works', '/create'],
  '/about': ['/about', '/faculty', '/course-intro'],
}
const isNavActive = item => (navGroups[item.path] || [item.path]).includes(page.value)
const infoPages = {
  '/teacher-growth': { title: '教师成长中心', kicker: 'TEACHER DEVELOPMENT', description: '面向教师的课程与教学支持入口。', body: '教师培训和教学资料正在整理。你可以先浏览现有课程资源。', action: '查看课程资源', target: '/courses', icon: GraduationCap },
  '/faculty': { title: '师资介绍', kicker: 'OUR EDUCATORS', description: '了解参与课程建设的教师团队。', body: '师资资料正在整理，正式介绍将在核实后发布。', action: '了解课程', target: '/course-intro', icon: Users },
  '/course-intro': { title: '课程介绍', kicker: 'OUR COURSES', description: '查看平台目前展示的科技教育课程。', body: '以下课程为当前前端演示内容，课程安排以正式发布的信息为准。', action: '查看课程资源', target: '/courses', icon: BookOpen },
  '/join': { title: '加入我们', kicker: 'JOIN US', description: '关注智启未来的合作与招聘信息。', body: '具体岗位与合作方式正在整理，欢迎通过联系页面了解后续消息。', action: '联系我们', target: '/contact', icon: Users },
}

watch(() => route.fullPath, () => { keyword.value = ''; mobileOpen.value = false; window.scrollTo({ top: 0, behavior: 'smooth' }) })
watch(() => route.query.section, section => { newsCategory.value = newsSections[section] || '全部' }, { immediate: true })

function go(path) { mobileOpen.value = false; router.push(path) }
function notify(message) { toast.value = message; setTimeout(() => { if (toast.value === message) toast.value = '' }, 3000) }
function openCourse(id) { dialog.value = { kind: 'course', id } }
function toggleFavorite(id) {
  const index = store.favorites.indexOf(id)
  if (index >= 0) store.favorites.splice(index, 1)
  else store.favorites.push(id)
  notify(index >= 0 ? '已取消收藏' : '已加入收藏')
}
function enroll(id) {
  if (!store.enrolled.includes(id)) store.enrolled.push(id)
  dialog.value = null
  notify('已加入我的学习，开始探索吧')
  go('/tasks')
}
function markTask(task) {
  task.status = task.status === '已完成' ? '进行中' : '已完成'
  notify(task.status === '已完成' ? '任务已标记为完成' : '任务已重新开始')
}
function addBlock(text) { selectedBlocks.value.push(text); notify('积木已加入工作区') }
function runPreview() {
  if (createMode.value === 'Scratch') previewText.value = `已连接 ${selectedBlocks.value.length} 块积木。点击保存可发布你的创作。`
  else if (createMode.value === 'Python') previewText.value = '代码已保存在编辑区。在线 Python 运行环境将在接入执行服务后开放。'
  else previewText.value = 'CAD / SOLIDWORKS 在线编辑器将在集成设计服务后开放。'
}
function saveWork() {
  const title = workTitle.value.trim()
  if (!title) return notify('请先填写作品名称')
  store.works.unshift({ id: Date.now(), title, author: store.user?.name || '创作者', type: createMode.value, color: createMode.value === 'Python' ? 'blue' : createMode.value === 'Scratch' ? 'purple' : 'orange', icon: createMode.value === 'Python' ? 'Code2' : 'Rocket', description: createMode.value === 'Python' ? pythonCode.value.slice(0, 70) : `${selectedBlocks.value.length} 块创意积木组成的作品` })
  notify('作品已保存到本地作品展示')
  go('/works')
}
function sendAi(text = aiInput.value) {
  const prompt = text.trim()
  if (!prompt) return
  aiMessages.value.push({ role: 'user', text: prompt })
  const answer = /Python|编程|代码/i.test(prompt)
    ? '可以从一个小目标开始：先明确输入和输出，再用变量保存数据，最后用条件或循环完成逻辑。你想做什么小程序？'
    : /机器人|传感器/.test(prompt)
      ? '设计机器人任务时，可以按“感知 → 判断 → 行动”拆解。先列出要用的传感器，再写下每种情况对应的动作。'
      : /课程|学什么|推荐/.test(prompt)
        ? '如果你刚开始学习，推荐先体验人工智能启蒙和 Python 编程课程；喜欢动手制作的话，可以看看机器人探索与三维设计。'
        : '我们可以先把问题拆成几个小步骤：明确目标、列出已知条件、尝试一个最简单的方案，再观察结果并改进。你可以补充更多细节。'
  aiMessages.value.push({ role: 'assistant', text: answer })
  aiInput.value = ''
}
function login() {
  if (!loginName.value.trim()) return notify('请输入昵称')
  store.user = { name: loginName.value.trim(), role: '普通用户' }
  notify('欢迎回来，' + store.user.name)
  go('/')
}
function addAdminItem() {
  const title = formTitle.value.trim()
  if (!title) return notify('请填写名称')
  if (adminTab.value === '新闻管理') store.news.unshift({ id: Date.now(), title, category: formCategory.value, date: new Date().toISOString().slice(0, 10).replaceAll('-', '.'), excerpt: formDescription.value.trim() || '点击查看详细内容。' })
  if (adminTab.value === '任务管理') store.tasks.unshift({ id: Date.now(), title, course: '平台任务', due: '待安排', status: '未开始', description: formDescription.value.trim() || '请查看任务要求。' })
  if (adminTab.value === '作品管理') store.works.unshift({ id: Date.now(), title, author: store.user?.name || '管理员', type: '展示作品', color: 'blue', icon: 'Sparkles', description: formDescription.value.trim() || '创意作品' })
  dialog.value = null; formTitle.value = ''; formDescription.value = ''; notify('新增成功')
}
function removeAdminItem(id) {
  const list = adminTab.value === '新闻管理' ? store.news : adminTab.value === '任务管理' ? store.tasks : store.works
  const index = list.findIndex(item => item.id === id)
  if (index >= 0) { list.splice(index, 1); notify('已删除') }
}
function addCart(id) { if (!cart.value.includes(id)) cart.value.push(id); notify('已加入选课清单') }
</script>

<template>
  <div class="site-shell">
    <header class="site-header">
      <div class="header-inner container">
        <button class="brand" @click="go('/')" aria-label="返回首页">
          <span class="brand-mark"><span></span><span></span><span></span><span></span></span>
          <span class="brand-copy"><strong>智启未来</strong><small>科技教育云平台</small></span>
        </button>
        <nav class="desktop-nav" aria-label="主导航">
          <div v-for="item in nav" :key="item.path" :class="['nav-item', { 'has-children': item.children }]">
            <button :class="['nav-link', { active: isNavActive(item) }]" @click="go(item.path)">{{ item.label }}<ChevronDown v-if="item.children" :size="14" /></button>
            <div v-if="item.children" class="nav-dropdown">
              <button v-for="child in item.children" :key="child.path" @click="go(child.path)">{{ child.label }}<ChevronRight :size="14" /></button>
            </div>
          </div>
        </nav>
        <div class="header-actions">
          <button class="icon-button header-search" title="搜索课程" @click="go('/courses')"><Search :size="19" /></button>
          <button class="login-action" @click="go(store.user ? '/organization' : '/login')"><UserRound :size="17" /><span>{{ store.user?.name || '登录 / 注册' }}</span></button>
          <button class="icon-button menu-button" @click="mobileOpen = !mobileOpen" aria-label="菜单"><Menu :size="23" /></button>
        </div>
      </div>
      <div v-if="mobileOpen" class="mobile-menu">
        <div v-for="item in nav" :key="item.path" class="mobile-nav-group">
          <button class="mobile-nav-main" @click="go(item.path)">{{ item.label }}<ChevronRight :size="16" /></button>
          <button v-for="child in item.children" :key="child.path" class="mobile-nav-child" @click="go(child.path)">{{ child.label }}</button>
        </div>
      </div>
    </header>

    <main>
      <template v-if="page === '/'">
        <section class="hero">
          <div class="hero-image"></div><div class="hero-shade"></div>
          <div class="container hero-content">
            <div class="hero-eyebrow"><span class="eyebrow-line"></span> 面向未来的科技教育平台 <span class="eyebrow-dot"></span> AI · 创造 · 成长</div>
            <h1>让每一个奇思妙想<br><em>都有发生的可能</em></h1>
            <p>从探索知识到动手创造，连接课程、任务与作品。<br class="desktop-only">在这里，开启属于你的未来学习之旅。</p>
            <div class="hero-actions"><button class="btn btn-primary" @click="go('/courses')">探索课程 <ArrowUpRight :size="18" /></button><button class="btn btn-glass" @click="go('/create')"><Play :size="16" fill="currentColor" />开始创作</button></div>
            <div class="hero-bottom"><span><span class="pulse"></span> 让想象力成为创造力</span><span>01 <i></i> 03</span></div>
          </div>
          <div class="hero-float"><Sparkles :size="18" /><span>智启你的<br><strong>无限可能</strong></span></div>
        </section>

        <section class="quick-section container">
          <div class="quick-heading"><span>QUICK ACCESS</span><p>探索你的学习空间</p></div>
          <div class="quick-grid">
            <button v-for="item in [
              { title:'精品课程',sub:'发现适合你的学习内容',path:'/courses',icon:BookOpen,color:'blue' },
              { title:'学习任务',sub:'每一步成长都有记录',path:'/tasks',icon:ClipboardList,color:'orange' },
              { title:'创作中心',sub:'把想法变成精彩作品',path:'/create',icon:WandSparkles,color:'purple' },
              { title:'AI 学习助手',sub:'随时陪你探索与思考',path:'/assistant',icon:Bot,color:'green' }
            ]" :key="item.path" :class="['quick-card', item.color]" @click="go(item.path)">
              <span class="quick-icon"><component :is="item.icon" :size="25" :stroke-width="1.8" /></span><span class="quick-copy"><strong>{{ item.title }}</strong><small>{{ item.sub }}</small></span><ArrowUpRight class="quick-arrow" :size="19" />
            </button>
          </div>
        </section>

        <section class="section courses-section"><div class="container">
          <div class="section-top"><div><span class="kicker">CURATED LEARNING</span><h2>发现热爱，<span>从这里开始</span></h2><p>多元课程与真实项目，让每一次学习都充满发现。</p></div><button class="text-link" @click="go('/courses')">查看全部课程 <ArrowUpRight :size="18" /></button></div>
          <div class="course-grid"><article v-for="course in featuredCourses.slice(0, 3)" :key="course.id" class="course-card" @click="openCourse(course.id)">
            <div :class="['course-art', course.color]"><span class="course-art-grid"></span><component :is="iconMap[course.icon]" :size="63" :stroke-width="1.15" /><span class="course-type">{{ course.type }}</span></div>
            <div class="course-body"><div class="course-meta"><span>{{ course.level }}</span><span><Star :size="13" fill="currentColor" /> 精品课程</span></div><h3>{{ course.title }}</h3><p>{{ course.summary }}</p><div class="course-foot"><span><BookOpen :size="15" /> {{ course.lessons }} 课时</span><span>{{ course.learners }} 人在学 <ArrowRight :size="16" /></span></div></div>
          </article></div>
        </div></section>

        <section class="feature-section"><div class="container feature-inner"><div class="feature-text"><span class="kicker light">CREATE SOMETHING NEW</span><h2>从灵感出发，<br>创造属于你的未来</h2><p>图形化编程、Python 与三维设计，一个空间承载不同的创意表达。动手试一试，让作品替你说话。</p><button class="btn btn-white" @click="go('/create')">进入创作中心 <ArrowUpRight :size="18" /></button></div><div class="feature-panel"><div class="panel-top"><span class="dot red"></span><span class="dot yellow"></span><span class="dot green-dot"></span><span>my_future.py</span></div><div class="panel-code"><span class="line-no">01</span><code><b>def</b> imagine_future():</code><span class="line-no">02</span><code>&nbsp;&nbsp;idea = <i>"无限可能"</i></code><span class="line-no">03</span><code>&nbsp;&nbsp;creativity = <i>True</i></code><span class="line-no">04</span><code>&nbsp;&nbsp;<b>return</b> idea + creativity</code><span class="line-no">05</span><code></code><span class="line-no">06</span><code>imagine_future()<span class="cursor">|</span></code></div><div class="panel-status"><span class="status-dot"></span> Ready to create <span>Python 3.11</span></div></div></div></section>

        <section class="section news-home"><div class="container"><div class="section-top"><div><span class="kicker">LATEST STORIES</span><h2>发生在这里的<span>新鲜事</span></h2><p>关注平台动态，发现更多精彩的科技教育故事。</p></div><button class="text-link" @click="go('/news')">更多资讯 <ArrowUpRight :size="18" /></button></div><div class="news-home-grid"><article v-for="(item, index) in store.news.slice(0,3)" :key="item.id" class="news-home-card" @click="dialog = { kind:'news', item }"><span class="news-index">0{{ index + 1 }}</span><span class="news-date">{{ item.date }} <i>·</i> {{ item.category }}</span><h3>{{ item.title }}</h3><p>{{ item.excerpt }}</p><span class="circle-arrow"><ArrowUpRight :size="19" /></span></article></div></div></section>
      </template>

      <template v-else-if="page === '/news'">
        <section class="page-banner container"><div><span class="kicker">NEWS & INSIGHTS</span><h1>{{ newsCategory === '全部' ? '新闻资讯' : newsCategory }}</h1><p>了解平台动态，记录每一次探索与成长。</p></div><Newspaper :size="124" :stroke-width="1" /></section>
        <section class="container page-body"><div class="toolbar"><div class="tabs"><button v-for="cat in newsTabs" :key="cat" :class="{ selected:newsCategory===cat }" @click="newsCategory=cat">{{ cat }}</button></div><label class="search-field"><Search :size="17" /><input v-model="keyword" placeholder="搜索新闻" /></label></div><div class="news-list"><article v-for="item in filteredNews" :key="item.id" class="news-row" @click="dialog={kind:'news',item}"><span class="news-row-date">{{ item.date }}</span><div><span class="tag">{{ item.category }}</span><h3>{{ item.title }}</h3><p>{{ item.excerpt }}</p></div><span class="row-arrow"><ArrowUpRight :size="20" /></span></article></div><div v-if="!filteredNews.length" class="empty-state">该栏目暂无资讯</div></section>
      </template>

      <template v-else-if="page === '/courses'"><section class="page-banner container"><div><span class="kicker">LEARNING CENTER</span><h1>教学中心</h1><p>打开一门课程，开启一段充满好奇的旅程。</p></div><BookOpen :size="124" :stroke-width="1" /></section><section class="container page-body"><div class="toolbar"><div class="tabs"><button v-for="cat in ['全部课程','人工智能','编程创造','机器人','创意设计']" :key="cat" :class="{ selected:courseCategory===cat }" @click="courseCategory=cat">{{ cat }}</button></div><label class="search-field"><Search :size="17" /><input v-model="keyword" placeholder="搜索课程" /></label></div><div class="course-grid all-courses"><article v-for="course in filteredCourses" :key="course.id" class="course-card" @click="openCourse(course.id)"><div :class="['course-art',course.color]"><span class="course-art-grid"></span><component :is="iconMap[course.icon]" :size="63" :stroke-width="1.15" /><span class="course-type">{{ course.type }}</span></div><div class="course-body"><div class="course-meta"><span>{{ course.level }}</span><span><Star :size="13" fill="currentColor" /> 精品课程</span></div><h3>{{ course.title }}</h3><p>{{ course.summary }}</p><div class="course-foot"><span><BookOpen :size="15" /> {{ course.lessons }} 课时</span><span>{{ course.learners }} 人在学 <ArrowRight :size="16" /></span></div></div></article></div></section></template>

      <template v-else-if="page === '/tasks'"><section class="page-banner container"><div><span class="kicker">MY LEARNING JOURNEY</span><h1>学习任务</h1><p>从一个小目标开始，积累看得见的成长。</p></div><ClipboardList :size="124" :stroke-width="1" /></section><section class="container page-body"><div class="stats-row"><div><strong>{{ store.tasks.length }}</strong><span>全部任务</span></div><div><strong>{{ store.tasks.filter(t=>t.status==='进行中').length }}</strong><span>进行中</span></div><div><strong>{{ store.tasks.filter(t=>t.status==='已完成').length }}</strong><span>已完成</span></div></div><div class="toolbar"><div class="tabs"><button v-for="cat in ['全部任务','未开始','进行中','已完成']" :key="cat" :class="{selected:taskFilter===cat}" @click="taskFilter=cat">{{ cat }}</button></div><button class="btn btn-outline" @click="go('/courses')">发现更多课程 <ArrowRight :size="16" /></button></div><div class="task-list"><article v-for="task in filteredTasks" :key="task.id" class="task-card"><div class="task-icon"><ClipboardList :size="24" /></div><div class="task-main"><div class="task-meta"><span>{{ task.course }}</span><span class="task-separator">·</span><span><CalendarDays :size="14" /> {{ task.due }}</span></div><h3>{{ task.title }}</h3><p>{{ task.description }}</p></div><div class="task-side"><span :class="['status-badge',task.status==='已完成'?'done':task.status==='进行中'?'progress':'waiting']">{{ task.status }}</span><button class="task-action" @click="markTask(task)">{{ task.status==='已完成'?'重新开始':'标记完成' }} <ArrowRight :size="16" /></button></div></article></div></section></template>

      <template v-else-if="page === '/create'"><section class="page-banner container"><div><span class="kicker">MAKE IT REAL</span><h1>创作中心</h1><p>每一个想法，都值得被动手实现。</p></div><WandSparkles :size="124" :stroke-width="1" /></section><section class="container page-body"><div class="creator-tabs"><button v-for="mode in ['Scratch','Python','CAD / SOLIDWORKS']" :key="mode" :class="{selected:createMode===mode}" @click="createMode=mode;previewText=''">{{ mode==='Scratch'?'图形化编程':mode==='Python'?'Python 编程':'三维设计' }}<span>{{ mode }}</span></button></div><div class="creator-shell"><div class="creator-top"><div><span class="tiny-label">PROJECT / NEW</span><input v-model="workTitle" aria-label="作品名称" /></div><div><button class="btn btn-outline" @click="runPreview"><Play :size="16" /> 运行预览</button><button class="btn btn-primary" @click="saveWork"><Check :size="16" /> 保存作品</button></div></div><div v-if="createMode==='Scratch'" class="creator-workspace"><aside class="blocks-panel"><h4>积木工具箱</h4><p>点击积木加入工作区</p><button v-for="block in ['移动 10 步','旋转 15 度','说「你好！」','等待 1 秒','重复执行','如果碰到边缘']" :key="block" @click="addBlock(block)">{{ block }} <Plus :size="15" /></button></aside><div class="canvas-panel"><div class="canvas-head"><span>脚本工作区</span><span>{{ selectedBlocks.length }} 块积木</span></div><div class="block-stack"><div v-for="(block,i) in selectedBlocks" :key="i" class="script-block">{{ block }}<button @click="selectedBlocks.splice(i,1)" aria-label="移除积木"><X :size="13" /></button></div><p v-if="!selectedBlocks.length">从左侧添加积木，开始你的创作</p></div></div><div class="stage-panel"><div class="stage-head">舞台预览 <span>480 × 360</span></div><div class="stage"><Rocket :size="57" :stroke-width="1.2" /><span>你的创意，从这里启航</span></div></div></div><div v-else-if="createMode==='Python'" class="python-workspace"><div class="editor-panel"><div class="editor-tab"><Code2 :size="16" /> main.py <span>PYTHON</span></div><textarea v-model="pythonCode" spellcheck="false" aria-label="Python 代码编辑器"></textarea></div><div class="console-panel"><div class="editor-tab">控制台 <span>OUTPUT</span></div><p>编写代码后保存作品。在线执行功能需接入 Python 运行服务。</p><pre>{{ previewText || '> 等待运行预览...' }}</pre></div></div><div v-else class="cad-workspace"><Box :size="78" :stroke-width="1.1" /><h3>为你的三维创意预留空间</h3><p>课程和作品资料可以先在平台展示；在线 CAD 编辑将在集成相关服务后开放。</p><button class="btn btn-outline" @click="go('/courses')">查看三维设计课程 <ArrowRight :size="16" /></button></div><div v-if="previewText && createMode!=='Python'" class="preview-message"><CheckCircle2 :size="18" /> {{ previewText }}</div></div></section></template>

      <template v-else-if="page === '/works'"><section class="page-banner container"><div><span class="kicker">STUDENT SHOWCASE</span><h1>作品展示</h1><p>每一个作品，都是勇敢探索的证明。</p></div><Sparkles :size="124" :stroke-width="1" /></section><section class="container page-body"><div class="section-top compact"><div><h2>创意作品集</h2><p>看看大家如何把灵感变成现实。</p></div><button class="btn btn-primary" @click="go('/create')"><Plus :size="17" /> 创作新作品</button></div><div class="works-grid"><article v-for="work in store.works" :key="work.id" class="work-card" @click="dialog={kind:'work',item:work}"><div :class="['work-art',work.color]"><component :is="iconMap[work.icon] || Sparkles" :size="68" :stroke-width="1.1" /><span>{{ work.type }}</span></div><div class="work-info"><span class="tag">{{ work.type }}</span><h3>{{ work.title }}</h3><p>{{ work.description }}</p><div><span><UserRound :size="14" /> {{ work.author }}</span><ArrowUpRight :size="18" /></div></div></article></div></section></template>

      <template v-else-if="page === '/assignments'">
        <section class="page-banner container"><div><span class="kicker">ASSIGNMENTS</span><h1>作业表</h1><p>查看当前演示任务及完成状态。</p></div><ClipboardList :size="124" :stroke-width="1" /></section>
        <section class="container page-body"><div class="assignment-list"><article v-for="task in store.tasks" :key="task.id" class="assignment-row"><div><span class="tag">{{ task.course }}</span><h3>{{ task.title }}</h3><p>{{ task.description }}</p></div><div class="assignment-meta"><span>截止：{{ task.due }}</span><span :class="['status-badge',task.status==='已完成'?'done':task.status==='进行中'?'progress':'waiting']">{{ task.status }}</span></div></article></div><div v-if="!store.tasks.length" class="empty-state">暂无作业</div></section>
      </template>

      <template v-else-if="page === '/my-works'">
        <section class="page-banner container"><div><span class="kicker">MY WORKS</span><h1>我的作品</h1><p>查看你在此浏览器中保存的创作。</p></div><Sparkles :size="124" :stroke-width="1" /></section>
        <section class="container page-body"><div class="section-top compact"><div><h2>我的创作记录</h2><p>作品保存在当前浏览器中。</p></div><button class="btn btn-primary" @click="go('/create')"><Plus :size="17" /> 创作新作品</button></div><div v-if="myWorks.length" class="works-grid"><article v-for="work in myWorks" :key="work.id" class="work-card" @click="dialog={kind:'work',item:work}"><div :class="['work-art',work.color]"><component :is="iconMap[work.icon] || Sparkles" :size="68" :stroke-width="1.1" /><span>{{ work.type }}</span></div><div class="work-info"><span class="tag">{{ work.type }}</span><h3>{{ work.title }}</h3><p>{{ work.description }}</p></div></article></div><div v-else class="empty-state">{{ store.user ? '你还没有保存作品，去创作中心试试吧。' : '登录演示账号后，可查看以该昵称保存的作品。' }}</div></section>
      </template>

      <template v-else-if="page === '/assistant'"><section class="page-banner container"><div><span class="kicker">AI LEARNING COMPANION</span><h1>AI 学习助手</h1><p>灵感卡住时，换一种方式继续探索。</p></div><Bot :size="124" :stroke-width="1" /></section><section class="container page-body assistant-layout"><aside class="assistant-intro"><div class="assistant-avatar"><Bot :size="32" /></div><h2>你好，探索者！</h2><p>把问题讲给我听，我们一起梳理思路、寻找下一步。</p><span class="demo-note"><CircleHelp :size="16" /> 当前为本地演示问答，正式 AI 服务需接入模型接口。</span><div class="suggestions"><strong>试试问我</strong><button v-for="prompt in ['推荐一门适合初学者的课程','Python 编程该怎么入门？','如何设计机器人任务？']" :key="prompt" @click="sendAi(prompt)">{{ prompt }}<ArrowUpRight :size="16" /></button></div></aside><div class="chat-panel"><div class="chat-head"><span class="online-dot"></span><strong>智启 AI 学习助手</strong><small>在线演示</small></div><div class="chat-messages"><div v-for="(message,i) in aiMessages" :key="i" :class="['chat-message',message.role]"><div class="chat-bubble">{{ message.text }}</div></div></div><form class="chat-input" @submit.prevent="sendAi()"><input v-model="aiInput" placeholder="输入你的问题，开始探索..." /><button type="submit" aria-label="发送消息"><Send :size="19" /></button></form></div></section></template>

      <template v-else-if="page === '/shop'"><section class="page-banner container"><div><span class="kicker">COURSE MARKETPLACE</span><h1>商品与课程</h1><p>为学校、老师和学生提供丰富的课程资源。</p></div><ShoppingBag :size="124" :stroke-width="1" /></section><section class="container page-body"><div class="notice-bar"><ShieldCheck :size="18" /><span>课程价格、订单和支付接口将在后端接入后启用；这里展示选课清单交互。</span></div><div class="section-top compact"><div><h2>精选课程资源</h2><p>探索适合不同学习阶段的主题课程。</p></div><span class="cart-count"><ShoppingBag :size="17" /> 选课清单 {{ cart.length }}</span></div><div class="shop-grid"><article v-for="course in featuredCourses" :key="course.id" class="shop-card"><div :class="['shop-icon',course.color]"><component :is="iconMap[course.icon]" :size="27" /></div><span class="tag">{{ course.type }}</span><h3>{{ course.title }}</h3><p>{{ course.summary }}</p><div><span>{{ course.lessons }} 课时 · {{ course.level }}</span><button @click="addCart(course.id)">{{ cart.includes(course.id)?'已加入':'加入清单' }} <ArrowRight :size="15" /></button></div></article></div></section></template>

      <template v-else-if="page === '/organization'"><section class="page-banner container"><div><span class="kicker">SCHOOLS & COMMUNITY</span><h1>学校与机构</h1><p>连接学校、教师、学生与教育管理者。</p></div><Users :size="124" :stroke-width="1" /></section><section class="container page-body"><div class="org-hero"><div><span class="kicker light">GROW TOGETHER</span><h2>让优质科技教育<br>走进更多课堂</h2><p>以课程资源、班级协作和作品成长记录，支持学校开展丰富的科技教育实践。</p></div><div class="org-shapes"><GraduationCap :size="88" :stroke-width="1" /></div></div><div class="org-grid"><div v-for="item in [{icon:GraduationCap,title:'学校管理',text:'组织课程与班级，构建学校专属学习空间。'},{icon:Users,title:'班级协作',text:'连接教师与学生，关注每个人的学习进度。'},{icon:ShieldCheck,title:'角色权限',text:'按不同角色提供清晰的入口与管理边界。'}]" :key="item.title"><component :is="item.icon" :size="27" /><h3>{{ item.title }}</h3><p>{{ item.text }}</p></div></div><div class="info-callout"><div><h3>加入智启未来教育社区</h3><p>学校入驻和机构管理功能需要后台服务支持，前端入口与展示页面已经预留。</p></div><button class="btn btn-primary" @click="go('/contact')">联系我们 <ArrowRight :size="16" /></button></div></section></template>

      <template v-else-if="page === '/admin'"><section class="page-banner container"><div><span class="kicker">PLATFORM CONSOLE</span><h1>后台管理</h1><p>集中查看平台内容与学习数据。</p></div><LayoutDashboard :size="124" :stroke-width="1" /></section><section class="container page-body"><div class="notice-bar"><ShieldCheck :size="18" /><span>当前为前端演示工作台，数据保存在本机浏览器中；正式上线需接入身份验证与服务端权限。</span></div><div class="admin-stats"><div><Newspaper :size="22" /><strong>{{ store.news.length }}</strong><span>新闻资讯</span></div><div><BookOpen :size="22" /><strong>{{ featuredCourses.length }}</strong><span>课程资源</span></div><div><ClipboardList :size="22" /><strong>{{ store.tasks.length }}</strong><span>学习任务</span></div><div><Sparkles :size="22" /><strong>{{ store.works.length }}</strong><span>展示作品</span></div></div><div class="admin-layout"><aside><span>内容管理</span><button v-for="tab in ['新闻管理','任务管理','作品管理','课程管理','用户与学校','系统设置']" :key="tab" :class="{selected:adminTab===tab}" @click="adminTab=tab">{{ tab }}<ChevronRight :size="16" /></button></aside><div class="admin-main"><div class="admin-main-head"><div><h2>{{ adminTab }}</h2><p>{{ ['新闻管理','任务管理','作品管理'].includes(adminTab)?'管理平台展示内容和学习资料。':'该模块的完整操作将在接入后端服务后开放。' }}</p></div><button v-if="['新闻管理','任务管理','作品管理'].includes(adminTab)" class="btn btn-primary" @click="dialog={kind:'admin-add'}"><Plus :size="16" /> 新增内容</button></div><template v-if="['新闻管理','任务管理','作品管理'].includes(adminTab)"><div v-for="item in adminTab==='新闻管理'?store.news:adminTab==='任务管理'?store.tasks:store.works" :key="item.id" class="admin-row"><div><span class="tag">{{ item.category || item.status || item.type }}</span><strong>{{ item.title }}</strong><small>{{ item.date || item.course || item.author }}</small></div><button title="删除" @click="removeAdminItem(item.id)"><Trash2 :size="17" /></button></div></template><div v-else class="admin-placeholder"><Layers3 :size="43" :stroke-width="1.3" /><h3>管理入口已预留</h3><p>{{ adminTab }}需要配合服务端数据与权限系统完成。</p></div></div></div></section></template>

      <template v-else-if="infoPages[page]">
        <section class="page-banner container"><div><span class="kicker">{{ infoPages[page].kicker }}</span><h1>{{ infoPages[page].title }}</h1><p>{{ infoPages[page].description }}</p></div><component :is="infoPages[page].icon" :size="124" :stroke-width="1" /></section>
        <section class="container page-body info-page"><p>{{ infoPages[page].body }}</p><button class="btn btn-primary" @click="go(infoPages[page].target)">{{ infoPages[page].action }} <ArrowRight :size="17" /></button><div v-if="page === '/course-intro'" class="course-intro-list"><article v-for="course in featuredCourses" :key="course.id"><span class="tag">{{ course.type }}</span><h3>{{ course.title }}</h3><p>{{ course.summary }}</p></article></div></section>
      </template>

      <template v-else-if="page === '/login'"><section class="login-page"><div class="login-visual"><div><span class="kicker light">WELCOME TO ZHIQI FUTURE</span><h1>每一次好奇<br>都是未来的起点</h1><p>探索 · 创造 · 成长</p></div></div><div class="login-form-wrap"><div class="login-form"><div class="brand-mini"><span class="brand-mark"><span></span><span></span><span></span><span></span></span> 智启未来</div><h2>欢迎来到智启未来</h2><p>输入昵称，开始体验你的学习空间。</p><label>昵称<input v-model="loginName" placeholder="请输入昵称" @keyup.enter="login" /></label><button class="btn btn-primary" @click="login">进入平台 <ArrowRight :size="17" /></button><small>演示模式：此处不使用参考网站账号，也不会发送你的信息。</small></div></div></section></template>

      <template v-else><section class="page-banner container"><div><span class="kicker">ABOUT ZHIQI FUTURE</span><h1>{{ page==='/contact'?'联系我们':'关于我们' }}</h1><p>让科技教育为每一个孩子打开更广阔的未来。</p></div><Compass :size="124" :stroke-width="1" /></section><section class="container page-body"><div class="about-grid"><div><span class="kicker">OUR VISION</span><h2>用科技点亮好奇心</h2><p>智启未来科技教育平台面向学校、教师与学生，连接教学资源、创作工具与成长记录。我们希望让学习从问题出发，在实践中发生。</p><button class="btn btn-primary" @click="go('/courses')">探索课程 <ArrowRight :size="17" /></button></div><div class="about-contact"><h3>联系我们</h3><p>联系方式和咨询入口请在正式上线前配置。</p><span><Mail :size="17" /> 邮箱：待配置</span><span><MapPin :size="17" /> 地址：待配置</span><button @click="go('/assistant')">咨询学习助手 <ArrowUpRight :size="16" /></button></div></div></section></template>
    </main>

    <footer v-if="page !== '/login'" class="footer"><div class="container footer-main"><div><div class="footer-brand"><span class="brand-mark"><span></span><span></span><span></span><span></span></span><strong>智启未来</strong></div><p>科技赋能教育，创意点亮未来。<br>和每一位探索者一起，拥抱无限可能。</p></div><div class="footer-links"><div><strong>探索平台</strong><button @click="go('/courses')">教学中心</button><button @click="go('/create')">创作中心</button><button @click="go('/works')">作品展示</button></div><div><strong>关于我们</strong><button @click="go('/about')">平台介绍</button><button @click="go('/news')">新闻资讯</button><button @click="go('/contact')">联系我们</button></div><div><strong>更多服务</strong><button @click="go('/organization')">学校与机构</button><button @click="go('/shop')">商品与课程</button><button @click="go('/admin')">管理后台</button></div></div></div><div class="container footer-bottom"><span>© 2026 智启未来科技教育平台. 前端演示版</span><span>让想象力成为创造力 <Sparkles :size="15" /></span></div></footer>

    <div v-if="toast" class="toast"><CheckCircle2 :size="18" />{{ toast }}</div>
    <div v-if="dialog" class="modal-backdrop" @click.self="dialog=null"><div class="modal"><button class="modal-close" @click="dialog=null" aria-label="关闭"><X :size="19" /></button><template v-if="dialog.kind==='course' && currentCourse"><span class="kicker">COURSE DETAILS</span><div :class="['modal-icon',currentCourse.color]"><component :is="iconMap[currentCourse.icon]" :size="31" /></div><h2>{{ currentCourse.title }}</h2><p>{{ currentCourse.summary }}</p><div class="modal-meta"><span>{{ currentCourse.type }}</span><span>{{ currentCourse.level }}</span><span>{{ currentCourse.lessons }} 课时</span></div><h3>课程章节</h3><div class="chapter-list"><div v-for="(chapter,i) in currentCourse.chapters" :key="chapter"><span>0{{ i+1 }}</span>{{ chapter }}<Play :size="15" /></div></div><div class="modal-actions"><button class="btn btn-outline" @click="toggleFavorite(currentCourse.id)"><Heart :size="16" :fill="store.favorites.includes(currentCourse.id)?'currentColor':'none'" />{{ store.favorites.includes(currentCourse.id)?'已收藏':'收藏课程' }}</button><button class="btn btn-primary" @click="enroll(currentCourse.id)">{{ store.enrolled.includes(currentCourse.id)?'继续学习':'开始学习' }} <ArrowRight :size="16" /></button></div></template><template v-else-if="dialog.kind==='news'"><span class="kicker">{{ dialog.item.category }} / {{ dialog.item.date }}</span><h2>{{ dialog.item.title }}</h2><p>{{ dialog.item.excerpt }}</p><div class="article-body"><p>{{ dialog.item.excerpt }}</p><p>更多详细内容可在新闻管理中补充发布。关注平台动态，一起发现科技教育的更多可能。</p></div></template><template v-else-if="dialog.kind==='work'"><span class="kicker">STUDENT SHOWCASE</span><div :class="['modal-icon',dialog.item.color]"><component :is="iconMap[dialog.item.icon] || Sparkles" :size="31" /></div><h2>{{ dialog.item.title }}</h2><p>{{ dialog.item.description }}</p><div class="modal-meta"><span>{{ dialog.item.type }}</span><span>创作者：{{ dialog.item.author }}</span></div><div class="notice-bar"><CircleHelp :size="18" />作品文件与在线运行需要接入文件服务后展示。</div></template><template v-else-if="dialog.kind==='admin-add'"><span class="kicker">CONTENT MANAGEMENT</span><h2>新增{{ adminTab.replace('管理','') }}</h2><label class="form-field">名称<input v-model="formTitle" placeholder="请输入名称" /></label><label class="form-field">{{ adminTab==='新闻管理'?'摘要':'说明' }}<textarea v-model="formDescription" placeholder="请输入内容说明"></textarea></label><label v-if="adminTab==='新闻管理'" class="form-field">分类<select v-model="formCategory"><option v-for="category in newsTabs.slice(1)" :key="category">{{ category }}</option></select></label><button class="btn btn-primary modal-submit" @click="addAdminItem">保存内容 <Check :size="16" /></button></template></div></div>
  </div>
</template>
