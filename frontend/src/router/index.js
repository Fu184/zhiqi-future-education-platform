import { createRouter, createWebHashHistory } from 'vue-router'
import { authStore as store } from '../stores/auth'
import { restoreSession, managementRoles } from '../services/auth'
import { infoPages } from '../config/portal'
import { homeSections, homeTopicPaths } from '../data/homeSections'

// URL、页面名、源码一一对应；权限由路由入口和后端共同校验。
const routes = [
  ...homeSections.flatMap((section) => [
    { path: section.path, name: section.title, component: () => import('../pages/TopicPage.vue') },
    ...section.items.map((item) => ({
      path: item.path,
      name: `${section.title}-${item.title}`,
      component: () => import('../pages/TopicPage.vue'),
    })),
  ]),
  ...Object.keys(infoPages)
    .filter(
      (path) =>
        /^\/(competitions|study-tours|training)(\/|$)/.test(path) && !homeTopicPaths.includes(path),
    )
    .map((path) => ({
      path,
      name: infoPages[path].title,
      component: () => import('../components/InfoPage.vue'),
    })),
  { path: '/', name: '首页', component: () => import('../pages/HomePage.vue') },
  { path: '/news', name: '新闻资讯', component: () => import('../pages/NewsPage.vue') },
  { path: '/courses', name: '课程资源', component: () => import('../pages/CoursesPage.vue') },
  {
    path: '/tasks',
    name: '学习任务',
    component: () => import('../pages/TasksPage.vue'),
    meta: { learning: true },
  },
  {
    path: '/create',
    name: '创作中心',
    component: () => import('../pages/CreatePage.vue'),
    meta: { learning: true },
  },
  { path: '/works', name: '作品展示', component: () => import('../pages/WorksPage.vue') },
  {
    path: '/assignments',
    name: '作业表',
    component: () => import('../pages/AssignmentsPage.vue'),
    meta: { learning: true },
  },
  {
    path: '/my-works',
    name: '我的作品',
    component: () => import('../pages/MyWorksPage.vue'),
    meta: { learning: true },
  },
  { path: '/assistant', name: 'AI 助手', component: () => import('../pages/AssistantPage.vue') },
  { path: '/shop', name: '商城', component: () => import('../pages/ShopPage.vue') },
  {
    path: '/organization',
    name: '学校与机构',
    component: () => import('../pages/OrganizationPage.vue'),
  },
  {
    path: '/admin',
    name: '内容管理',
    component: () => import('../pages/ContentAdminPage.vue'),
    meta: { roles: ['admin'] },
  },
  {
    path: '/users',
    name: '用户管理',
    component: () => import('../pages/UserManagementPage.vue'),
    meta: { roles: managementRoles },
  },
  {
    path: '/government',
    name: '政府数据概览',
    component: () => import('../pages/GovernmentDashboardPage.vue'),
    meta: { roles: ['government', 'admin'] },
  },
  {
    path: '/account',
    name: '账户设置',
    component: () => import('../pages/AccountPage.vue'),
    meta: { authenticated: true },
  },
  { path: '/login', name: '登录注册', component: () => import('../pages/LoginPage.vue') },
  { path: '/faculty', name: '师资介绍', component: () => import('../pages/FacultyPage.vue') },
  {
    path: '/course-intro',
    name: '课程介绍',
    component: () => import('../pages/CourseIntroPage.vue'),
  },
  { path: '/join', name: '加入我们', component: () => import('../pages/JoinPage.vue') },
  { path: '/about', name: '关于我们', component: () => import('../pages/AboutPage.vue') },
  { path: '/contact', name: '联系我们', component: () => import('../pages/ContactPage.vue') },
  {
    path: '/privacy',
    name: '隐私说明',
    component: () => import('../pages/SiteInformationPage.vue'),
  },
  {
    path: '/site-notice',
    name: '网站使用说明',
    component: () => import('../pages/SiteInformationPage.vue'),
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})
router.beforeEach(async (to) => {
  try {
    await restoreSession({
      force: Boolean(to.meta.authenticated || to.meta.roles || to.meta.learning),
    })
  } catch {
    if (to.meta.authenticated || to.meta.roles || to.meta.learning) return false
  }
  const user = store.user
  if (user?.role === 'government' && !['/government', '/account'].includes(to.path))
    return '/government'
  if ((to.meta.authenticated || to.meta.roles || to.meta.learning) && !user)
    return { path: '/login', query: { redirect: to.fullPath } }
  if (to.meta.roles && !to.meta.roles.includes(user.role)) return '/'
  if (to.meta.learning && !user.canLearn)
    return { path: '/shop', query: { notice: 'learning-required' } }
})
export default router
