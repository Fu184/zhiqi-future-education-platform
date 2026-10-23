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
import { homeSections } from '../data/homeSections'

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
      { label: '相关政策', path: '/news?section=policies' },
      { label: '动态', path: '/news?section=updates' },
    ],
  },
  {
    label: '教学中心',
    path: '/courses',
    children: [{ label: '课程资源', path: '/courses' }],
  },
  { label: '教师成长中心', path: '/teacher-growth' },
  {
    label: '赛事',
    path: '/competitions',
    children: [
      { label: '赛事服务', path: '/competitions/services' },
      { label: '升学规划', path: '/competitions/planning' },
      { label: '科技特长生', path: '/competitions/talents' },
      { label: '赛事辅导', path: '/competitions/coaching' },
      { label: '等级考试', path: '/competitions/exams' },
    ],
  },
  {
    label: '研学',
    path: '/study-tours',
    children: [
      { label: '教师研学', path: '/study-tours/teachers' },
      { label: '青少年研学', path: '/study-tours/teens' },
      { label: '中小学研学', path: '/study-tours/schools' },
    ],
  },
  { label: '培训', path: '/training' },
  {
    label: '创作中心',
    path: '/create',
    children: [{ label: '我的作品', path: '/my-works' }],
  },
  { label: '商城', path: '/shop' },
  {
    label: '关于我们',
    path: '/about',
  },
]
export const newsSections = {
  activities: '相关活动',
  events: '赛事',
  policies: '政策',
  updates: '平台动态',
}
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
  '/courses': [
    '/courses',
    '/tasks',
    '/assignments',
    ...homeSections[0].items.map((item) => item.path),
    homeSections[0].path,
  ],
  '/teacher-growth': [
    '/teacher-growth',
    ...homeSections.find((section) => section.id === 'teachers').items.map((item) => item.path),
  ],
  '/shop': [
    '/shop',
    '/products',
    ...homeSections.find((section) => section.id === 'products').items.map((item) => item.path),
  ],
  '/create': ['/create', '/my-works'],
  '/competitions': [
    '/competitions',
    ...nav.find((item) => item.path === '/competitions').children.map((item) => item.path),
  ],
  '/study-tours': [
    '/study-tours',
    ...nav.find((item) => item.path === '/study-tours').children.map((item) => item.path),
    ...homeSections.find((section) => section.id === 'study-tours').items.map((item) => item.path),
  ],
  '/about': ['/about', '/faculty', '/course-intro'],
}
export const infoPages = {
  ...Object.fromEntries(
    nav
      .filter((item) => ['/competitions', '/study-tours', '/training'].includes(item.path))
      .flatMap((item) => [item, ...(item.children || [])])
      .map((item) => [
        item.path,
        {
          title: item.label,
          kicker: item.path.startsWith('/competitions')
            ? 'COMPETITIONS'
            : item.path.startsWith('/study-tours')
              ? 'STUDY TOURS'
              : 'TRAINING',
          description: `了解${item.label}的相关信息与服务。`,
          body: `${item.label}内容正在整理，具体安排将在发布后展示。`,
          action: '联系我们',
          target: '/contact',
          icon: GraduationCap,
        },
      ]),
  ),
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
