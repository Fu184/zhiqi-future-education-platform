import {
  BookOpen,
  Bot,
  Box,
  BrainCircuit,
  ClipboardList,
  CloudSun,
  Code2,
  GraduationCap,
  Newspaper,
  Rocket,
  ShoppingBag,
  Sparkles,
  Users,
} from 'lucide-vue-next'

export const iconMap = {
  BrainCircuit,
  Code2,
  Bot,
  Box,
  Rocket,
  CloudSun,
  BookOpen,
  Newspaper,
  Users,
  ShoppingBag,
  ClipboardList,
  Sparkles,
}
export const nav = [
  { label: '首页', path: '/' },
  {
    label: '新闻资讯',
    path: '/news',
    children: [
      { label: '相关活动', path: '/news?section=activities' },
      { label: '赛事', path: '/news?section=events' },
      { label: '政策', path: '/news?section=policies' },
    ],
  },
  {
    label: '教学中心',
    path: '/courses',
    children: [
      { label: '教师成长中心', path: '/teacher-growth' },
      { label: '课程资源', path: '/courses' },
      { label: '学习任务', path: '/tasks' },
      { label: '作业表', path: '/assignments' },
      { label: '我的作品', path: '/my-works' },
      { label: '创作中心', path: '/create' },
    ],
  },
  { label: '商城', path: '/shop' },
  {
    label: '关于我们',
    path: '/about',
    children: [
      { label: '公司介绍', path: '/about' },
      { label: '师资介绍', path: '/faculty' },
      { label: '课程介绍', path: '/course-intro' },
    ],
  },
  { label: '加入我们', path: '/join' },
]
export const newsSections = { activities: '相关活动', events: '赛事', policies: '政策' }
export const newsTabs = [
  '全部',
  '相关活动',
  '赛事',
  '政策',
  '平台动态',
  '教学新闻',
  '学校新闻',
  '公告',
]
export const navGroups = {
  '/courses': ['/courses', '/teacher-growth', '/tasks', '/assignments', '/my-works', '/create'],
  '/about': ['/about', '/faculty', '/course-intro'],
}
export const infoPages = {
  '/teacher-growth': {
    title: '教师成长中心',
    kicker: 'TEACHER DEVELOPMENT',
    description: '面向教师的课程与教学支持入口。',
    body: '教师培训和教学资料正在整理。你可以先浏览现有课程资源。',
    action: '查看课程资源',
    target: '/courses',
    icon: GraduationCap,
  },
  '/faculty': {
    title: '师资介绍',
    kicker: 'OUR EDUCATORS',
    description: '了解参与课程建设的教师团队。',
    body: '师资资料正在整理，正式介绍将在核实后发布。',
    action: '了解课程',
    target: '/course-intro',
    icon: Users,
  },
  '/course-intro': {
    title: '课程介绍',
    kicker: 'OUR COURSES',
    description: '查看平台目前展示的科技教育课程。',
    body: '以下课程为当前前端演示内容，课程安排以正式发布的信息为准。',
    action: '查看课程资源',
    target: '/courses',
    icon: BookOpen,
  },
  '/join': {
    title: '加入我们',
    kicker: 'JOIN US',
    description: '关注智启未来的合作与招聘信息。',
    body: '具体岗位与合作方式正在整理，欢迎通过联系页面了解后续消息。',
    action: '联系我们',
    target: '/contact',
    icon: Users,
  },
}
