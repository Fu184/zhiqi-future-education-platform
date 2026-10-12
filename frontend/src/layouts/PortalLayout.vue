<script setup>
import { Search, Sparkles, UserRound } from 'lucide-vue-next'
import { authStore as store } from '../stores/auth'
import { nav } from '../config/portal'
import { useUi } from '../composables/useUi'
import { landingPage, logout } from '../services/auth'
const { route, page, go, notify } = useUi()
const navigationItems = nav.flatMap(({ children, ...item }) => [item, ...(children || [])])
function navigate(path) {
  return go(path)
}
function isLinkActive(path) {
  const [pathname, query] = path.split('?')
  if (route.path !== pathname) return false
  return query
    ? route.query.section === new URLSearchParams(query).get('section')
    : !route.query.section
}
async function signOut() {
  try {
    await logout()
    go('/login')
  } catch (error) {
    notify(error.message)
  }
}
</script>
<template>
  <div :class="['site-shell', { 'home-layout': page === '/' }]">
    <a class="skip-link" href="#portal-main">跳转到主要内容</a>
    <header v-if="store.user?.role !== 'government'" class="site-header portal-header">
      <div class="header-inner container">
        <button class="brand" @click="navigate('/')" aria-label="返回首页">
          <span class="brand-mark"><span></span><span></span><span></span><span></span></span>
          <span class="brand-copy"><strong>智启未来</strong><small>科技教育云平台</small></span>
        </button>
        <p class="header-motto">共享优质资源 · 点亮科技梦想</p>
        <div class="header-actions">
          <button class="icon-button header-search" title="搜索课程" @click="navigate('/courses')">
            <Search :size="19" />
          </button>
          <button
            class="login-action"
            @click="navigate(store.user ? landingPage(store.user) : '/login')"
          >
            <UserRound :size="17" /><span>{{ store.user?.name || '登录 / 注册' }}</span>
          </button>
          <button v-if="store.user" class="login-action" @click="navigate('/account')">账户</button>
          <button v-if="store.user" class="login-action" @click="signOut">退出</button>
        </div>
      </div>
      <nav class="portal-navigation" aria-label="主导航">
        <div class="container portal-nav-list">
          <button
            v-for="item in navigationItems"
            :key="item.label"
            :class="['portal-nav-link', { active: isLinkActive(item.path) }]"
            :aria-current="isLinkActive(item.path) ? 'page' : undefined"
            @click="navigate(item.path)"
          >
            {{ item.label }}
          </button>
        </div>
      </nav>
    </header>

    <main id="portal-main" tabindex="-1">
      <p v-if="store.sessionError" class="container form-error" role="alert">
        {{ store.sessionError }}
      </p>
      <slot />
    </main>

    <footer v-if="page !== '/login' && store.user?.role !== 'government'" class="footer">
      <div class="container footer-main">
        <div>
          <div class="footer-brand">
            <span class="brand-mark"><span></span><span></span><span></span><span></span></span
            ><strong>智启未来</strong>
          </div>
          <p>科技赋能教育，创意点亮未来。<br />和每一位探索者一起，拥抱无限可能。</p>
        </div>
        <div class="footer-links">
          <div>
            <strong>探索平台</strong><button @click="navigate('/courses')">教学中心</button
            ><button @click="navigate('/create')">创作中心</button
            ><button @click="navigate('/works')">作品展示</button>
          </div>
          <div>
            <strong>关于我们</strong><button @click="navigate('/about')">平台介绍</button
            ><button @click="navigate('/news')">新闻资讯</button
            ><button @click="navigate('/contact')">联系我们</button>
          </div>
          <div>
            <strong>更多服务</strong><button @click="navigate('/organization')">学校与机构</button
            ><button @click="navigate('/shop')">商品与课程</button
            ><button @click="navigate('/admin')">管理后台</button>
          </div>
        </div>
      </div>
      <div class="container footer-bottom">
        <span>© 2026 智启未来科技教育平台. 前端演示版</span
        ><span
          >让想象力成为创造力
          <Sparkles :size="15" />
        </span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.footer-brand {
  color: #243c5c;
}
.portal-header {
  position: relative;
  background: #fff;
  box-shadow: none;
}
.header-inner {
  height: auto;
  min-height: 92px;
  padding-block: 20px;
  gap: 24px;
}
.header-motto {
  margin: 0 auto 0 24px;
  color: #778397;
  font-size: 14px;
  letter-spacing: 2px;
}
.header-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}
.login-action {
  color: #2367b5;
  border-color: #dce7f4;
  border-radius: 5px;
}
.login-action:hover {
  background: #edf5ff;
}
.portal-navigation {
  background: #edf5ff;
  border-top: 1px solid #e4edf8;
  border-bottom: 1px solid #dce8f5;
}
.portal-nav-list {
  width: calc(100% - 32px);
  display: flex;
  flex-wrap: nowrap;
  overflow-x: auto;
  scrollbar-width: thin;
  scrollbar-color: #b7cce3 transparent;
  align-items: center;
  gap: 2px;
  padding-block: 10px;
}
.portal-nav-link {
  flex: 1 0 auto;
  border: 0;
  background: transparent;
  color: #243c5c;
  font-size: clamp(13px, 0.98vw, 15px);
  font-weight: 500;
  line-height: 1.5;
  white-space: nowrap;
  padding: 10px clamp(4px, 0.4vw, 8px);
  border-radius: 4px;
  transition:
    color 0.2s,
    background 0.2s;
}
.portal-nav-link.active,
.portal-nav-link:hover {
  color: #176cc3;
  background: #dfeeff;
}
.skip-link {
  position: absolute;
  top: -60px;
  left: 20px;
  z-index: 40;
  padding: 10px;
  background: #fff;
  color: #176cc3;
}
.skip-link:focus {
  top: 8px;
}
@media (max-width: 1000px) {
  .header-motto {
    display: none;
  }
}
@media (max-width: 600px) {
  .header-inner {
    min-height: 80px;
    padding-block: 16px;
    gap: 12px;
    flex-wrap: wrap;
  }
  .brand-copy strong {
    font-size: 19px;
  }
  .header-actions {
    gap: 6px;
    margin-left: auto;
  }
  .login-action span {
    display: inline;
  }
  .header-search {
    display: none;
  }
  .login-action {
    padding: 8px 10px;
    font-size: 12px;
  }
  .portal-nav-list {
    gap: 4px;
    padding-block: 10px;
  }
  .portal-nav-link {
    font-size: 13px;
    padding: 8px 10px;
  }
}
@media (prefers-reduced-motion: reduce) {
  .portal-nav-link {
    transition: none;
  }
}
</style>
