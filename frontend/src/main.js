import { createApp } from 'vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import App from './App.vue'
import './style.css'

const routes = [
  '/', '/news', '/courses', '/tasks', '/create', '/works', '/assistant',
  '/shop', '/organization', '/admin', '/about', '/contact', '/login',
  '/teacher-growth', '/assignments', '/my-works', '/faculty', '/course-intro', '/join',
].map(path => ({ path, component: App }))

const router = createRouter({ history: createWebHashHistory(), routes })
createApp(App).use(router).mount('#app')
