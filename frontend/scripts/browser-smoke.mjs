// 可选端到端检查：需要 playwright，以及 Chromium / Edge。
// PLAYWRIGHT_MODULE 可指定现有 Playwright 模块路径，无需改动应用依赖。
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import { resolve } from 'node:path'
import { mkdirSync } from 'node:fs'
import { spawn } from 'node:child_process'
import assert from 'node:assert/strict'
import { createServer } from 'vite'
import vue from '@vitejs/plugin-vue'

const require = createRequire(import.meta.url)
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright')
const root = fileURLToPath(new URL('..', import.meta.url))
const previewDir = resolve(root, '../artifacts/browser')
mkdirSync(previewDir, { recursive: true })
const password = 'Browser-test-password-123'
// 使用真实 Spring Boot 可执行 JAR 和独立内存数据库，先在 backend 运行 mvn package。
const java = spawn(
  process.env.JAVA_EXECUTABLE || 'java',
  [
    '-jar',
    resolve(root, '../backend/target/education-platform-1.0.0.jar'),
    '--server.port=0',
    '--server.address=127.0.0.1',
    '--spring.profiles.active=dev',
    '--spring.datasource.url=jdbc:h2:mem:browser-tests;DB_CLOSE_DELAY=-1',
    '--app.production=false',
    '--app.public-origin=',
    '--app.bootstrap.enabled=true',
    '--app.bootstrap.phone=13800000000',
    `--app.bootstrap.password=${password}`,
    '--app.sms.mode=console',
    '--app.sms.secret=browser-tests-only-secret',
  ],
  { cwd: resolve(root, '../backend'), windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] },
)
let javaLog = ''
let javaPort
let springPid
async function stopJava() {
  if (process.platform === 'win32' && (springPid || java.pid)) {
    // 某些 Windows Java 启动器会派生 JVM；终止本次测试 JVM，避免遗留后台进程。
    await new Promise((done) => {
      const killer = spawn('taskkill', ['/PID', String(springPid || java.pid), '/T', '/F'], {
        windowsHide: true,
        stdio: 'ignore',
      })
      killer.once('exit', done)
      killer.once('error', done)
    })
  }
  java.kill()
}
await new Promise((done, reject) => {
  const timeout = setTimeout(() => {
    void stopJava()
    reject(new Error(`Spring Boot startup timeout: ${javaLog}`))
  }, 60000)
  function fail(error) {
    clearTimeout(timeout)
    reject(error)
  }
  function capture(chunk) {
    javaLog = (javaLog + chunk.toString()).slice(-12000)
    const match = javaLog.match(/Tomcat started on port (\d+)/)
    if (match) javaPort = Number(match[1])
    const pid = javaLog.match(/Starting PlatformApplication .*? with PID (\d+)/)
    if (pid) springPid = Number(pid[1])
    if (javaPort && javaLog.includes('Platform API ready')) {
      clearTimeout(timeout)
      done()
    }
  }
  java.stdout.on('data', capture)
  java.stderr.on('data', capture)
  java.once('error', fail)
  java.once('exit', (code) => fail(new Error(`Spring Boot exited (${code}): ${javaLog}`)))
})
const vite = await createServer({
  configFile: false,
  root,
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    port: 0,
    proxy: { '/api': `http://127.0.0.1:${javaPort}` },
  },
})
await vite.listen()
const origin = `http://127.0.0.1:${vite.httpServer.address().port}`
// HTTP Origin 验证允许测试服务实际分配的端口。
// Vite 代理会将 Host 保持为前端 Host，匹配服务端开发模式校验。
let browser, page
const errors = []
try {
  browser = await chromium.launch({
    headless: true,
    ...(process.env.BROWSER_EXECUTABLE ? { executablePath: process.env.BROWSER_EXECUTABLE } : {}),
  })
  const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } })
  page = await context.newPage()
  page.on('pageerror', (error) => errors.push(error.message))
  async function login(phone) {
    await page.goto(origin + '/#/login')
    await page.getByRole('button', { name: '登录', exact: true }).click()
    await page.getByLabel('手机号', { exact: true }).fill(phone)
    await page.getByLabel('密码', { exact: true }).fill(password)
    await page.getByRole('button', { name: '登录平台', exact: true }).click()
  }
  async function api(path, body) {
    const result = await context.request.post(origin + '/api' + path, { data: body })
    assert.ok(result.ok(), await result.text())
    return result.json()
  }
  await login('13800000000')
  await page.getByRole('heading', { name: '用户管理', exact: true }).waitFor()
  for (const path of [
    '/',
    '/news',
    '/courses',
    '/tasks',
    '/create',
    '/works',
    '/assignments',
    '/my-works',
    '/assistant',
    '/shop',
    '/organization',
    '/admin',
    '/teacher-growth',
    '/faculty',
    '/course-intro',
    '/join',
    '/about',
    '/contact',
    '/account',
  ]) {
    await page.goto(origin + '/#' + path)
    await page.locator('main h1').waitFor()
  }
  await page.goto(origin + '/#/users')
  await page.getByRole('heading', { name: '用户管理', exact: true }).waitFor()
  await page.locator('.management-card select').first().selectOption('principal')
  await page.getByLabel('姓名', { exact: true }).fill('浏览器测试校长')
  await page.getByLabel('手机号', { exact: true }).fill('13900000100')
  await page.getByLabel('初始密码').fill(password)
  await page.getByLabel('允许创建的老师数量').fill('1')
  await page.getByLabel('学校学生总名额').fill('2')
  await page.getByRole('button', { name: '创建账号', exact: true }).click()
  await page.getByRole('cell', { name: '浏览器测试校长 13900000100' }).waitFor()
  await page
    .getByRole('row')
    .filter({ hasText: '浏览器测试校长' })
    .getByRole('button', { name: '管理', exact: true })
    .click()
  await page.getByLabel('老师名额', { exact: true }).fill('2')
  await page.getByLabel('学生名额', { exact: true }).fill('3')
  await page.getByRole('button', { name: '保存修改', exact: true }).click()
  await page.getByText('用户信息已更新', { exact: true }).waitFor()
  const ordinary = await api('/users', {
    phone: '13900000150',
    name: '待开通学习用户',
    password,
    role: 'ordinary',
  })
  assert.equal(ordinary.user.role, 'ordinary')
  await page.getByRole('button', { name: '刷新', exact: true }).click()
  await page
    .getByRole('row')
    .filter({ hasText: '待开通学习用户' })
    .getByRole('button', { name: '管理', exact: true })
    .click()
  await page.getByLabel('实际购买凭证编号').fill('BROWSER-ORDER-001')
  await page.getByRole('button', { name: '确认购买并开通学习', exact: true }).click()
  await page.getByText('购买已确认，学习权限已开通', { exact: true }).waitFor()
  assert.match(
    await page.getByRole('row').filter({ hasText: '待开通学习用户' }).innerText(),
    /可学习用户/,
  )
  await page.screenshot({ path: resolve(previewDir, 'users-desktop.png'), fullPage: true })
  await page.setViewportSize({ width: 390, height: 844 })
  await page.screenshot({ path: resolve(previewDir, 'users-mobile.png'), fullPage: true })
  assert.ok(
    await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth),
    '用户管理移动端横向溢出',
  )
  await page.setViewportSize({ width: 1440, height: 1000 })
  const gov = await api('/users', {
    phone: '13900000200',
    name: '教育数据观察员',
    password,
    role: 'government',
  })
  assert.equal(gov.user.role, 'government')
  await api('/auth/logout', {})
  await login('13900000100')
  await page.getByRole('heading', { name: '创建老师账号', exact: true }).waitFor()
  await page.getByLabel('姓名', { exact: true }).fill('浏览器测试老师')
  await page.getByLabel('手机号', { exact: true }).fill('13900000101')
  await page.getByLabel('初始密码').fill(password)
  await page.getByLabel('分配给该老师的学生名额').fill('2')
  await page.getByRole('button', { name: '创建账号', exact: true }).click()
  await page.getByRole('cell', { name: '浏览器测试老师 13900000101' }).waitFor()
  await api('/auth/logout', {})
  await login('13900000101')
  await page.getByRole('heading', { name: '创建学生账号', exact: true }).waitFor()
  await page.getByLabel('姓名', { exact: true }).fill('浏览器测试学生')
  await page.getByLabel('手机号', { exact: true }).fill('13900000102')
  await page.getByLabel('初始密码').fill(password)
  await page.getByRole('button', { name: '创建账号', exact: true }).click()
  await page.getByRole('cell', { name: '浏览器测试学生 13900000102' }).waitFor()
  // 同名账号、任务进度、收藏、选课清单和作品均按用户 ID 隔离。
  const teacherList = await (await context.request.get(origin + '/api/users')).json()
  const student = teacherList.users.find((user) => user.phone === '13900000102')
  const renamed = await context.request.patch(origin + `/api/users/${student.id}`, {
    data: { name: '待开通学习用户' },
  })
  assert.ok(renamed.ok())
  await api('/auth/logout', {})
  await login('13900000102')
  await page.waitForURL('**/#/')
  await page.goto(origin + '/#/tasks')
  await page.locator('.task-card').first().getByRole('button', { name: '标记完成' }).click()
  await page.goto(origin + '/#/courses')
  await page.locator('.course-card').first().click()
  await page.getByRole('button', { name: '收藏课程', exact: true }).click()
  await page.getByRole('button', { name: '开始学习', exact: true }).click()
  await page.waitForURL('**/#/tasks')
  await page.goto(origin + '/#/create')
  await page.getByLabel('作品名称', { exact: true }).fill('按账号保存的作品')
  await page.getByRole('button', { name: '保存作品', exact: true }).click()
  await page.waitForURL('**/#/works')
  await page.goto(origin + '/#/my-works')
  await page.getByRole('heading', { name: '按账号保存的作品' }).waitFor()
  await page.goto(origin + '/#/shop')
  await page.locator('.shop-card').first().getByRole('button').click()
  await page.goto(origin + '/#/assistant')
  await page.getByPlaceholder('输入你的问题，开始探索...').fill('仅当前账号的对话')
  await page.getByRole('button', { name: '发送消息' }).click()
  await page.getByRole('button', { name: '退出', exact: true }).click()
  await page.waitForURL('**/#/login')
  await page.getByLabel('手机号', { exact: true }).fill('13900000150')
  await page.getByLabel('密码', { exact: true }).fill(password)
  await page.getByRole('button', { name: '登录平台', exact: true }).click()
  await page.waitForURL('**/#/')
  await page.goto(origin + '/#/my-works')
  assert.equal(await page.getByRole('heading', { name: '按账号保存的作品' }).count(), 0)
  await page.goto(origin + '/#/tasks')
  assert.match(await page.locator('.task-card').first().innerText(), /进行中/)
  await page.goto(origin + '/#/shop')
  assert.match(await page.locator('.cart-count').innerText(), /0/)
  await page.goto(origin + '/#/courses')
  await page.locator('.course-card').first().click()
  await page.getByRole('button', { name: '收藏课程', exact: true }).waitFor()
  await page.getByRole('button', { name: '开始学习', exact: true }).waitFor()
  await page.getByRole('button', { name: '关闭', exact: true }).click()
  await page.goto(origin + '/#/assistant')
  assert.equal(await page.getByText('仅当前账号的对话', { exact: true }).count(), 0)
  await api('/auth/logout', {})
  await login('13900000101')
  await page.getByRole('heading', { name: '创建学生账号', exact: true }).waitFor()
  const changedName = await context.request.patch(origin + `/api/users/${student.id}`, {
    data: { name: '改名后的学生' },
  })
  assert.ok(changedName.ok())
  await api('/auth/logout', {})
  await login('13900000102')
  await page.waitForURL('**/#/')
  await page.goto(origin + '/#/my-works')
  await page.getByRole('heading', { name: '按账号保存的作品' }).waitFor()
  // 网络故障不清空已验证身份，也不放行新的受保护页面。
  await page.route('**/api/auth/me', (route) => route.abort())
  await page.evaluate(() => {
    location.hash = '/tasks'
  })
  await page.getByText('无法验证登录状态，请检查网络或后端服务后重试。', { exact: true }).waitFor()
  assert.match(await page.locator('.header-actions').innerText(), /改名后的学生/)
  await page.unroute('**/api/auth/me')
  await page.evaluate(() => {
    location.hash = '/tasks'
  })
  await page.getByRole('heading', { name: '学习任务', exact: true }).waitFor()
  assert.match(await page.locator('.task-card').first().innerText(), /已完成/)
  await api('/auth/logout', {})
  await page.goto(origin + '/#/login')
  await page.getByRole('button', { name: '注册', exact: true }).click()
  await page.getByLabel('姓名 / 昵称').fill('自行注册用户')
  await page.getByLabel('手机号', { exact: true }).fill('13900000300')
  await page.getByLabel('密码', { exact: true }).fill(password)
  const smsResponse = page.waitForResponse((response) => response.url().endsWith('/api/auth/sms'))
  await page.getByRole('button', { name: '获取验证码', exact: true }).click()
  const sms = await (await smsResponse).json()
  await page.getByPlaceholder('六位验证码').fill(sms.devCode)
  await page.screenshot({ path: resolve(previewDir, 'register-desktop.png'), fullPage: true })
  await page.getByRole('button', { name: '验证并注册', exact: true }).click()
  await page.waitForURL('**/#/')
  await page.goto(origin + '/#/tasks')
  await page.waitForURL('**/#/shop?notice=learning-required')
  await api('/auth/logout', {})
  await login('13900000200')
  await page.getByRole('heading', { name: '科技教育 · 数据概览' }).waitFor()
  await page.getByText('用户总量', { exact: true }).waitFor()
  assert.equal(await page.locator('.site-header').count(), 0)
  await page.screenshot({ path: resolve(previewDir, 'government-desktop.png'), fullPage: true })
  await page.goto(origin + '/#/users')
  await page.waitForURL('**/#/government')
  await page.setViewportSize({ width: 390, height: 844 })
  await page.screenshot({ path: resolve(previewDir, 'government-mobile.png'), fullPage: true })
  assert.ok(
    await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth),
    '政府移动端页面横向溢出',
  )
  await api('/auth/logout', {})
  await page.goto(origin + '/#/login')
  await page.getByRole('button', { name: '注册', exact: true }).click()
  await page.screenshot({ path: resolve(previewDir, 'register-mobile.png'), fullPage: true })
  assert.ok(
    await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth),
    '注册移动端页面横向溢出',
  )
  // 超过一页的数据验证分页、服务端搜索与独立上级搜索。
  await page.setViewportSize({ width: 1440, height: 1000 })
  await login('13800000000')
  await page.getByRole('heading', { name: '用户管理', exact: true }).waitFor()
  await Promise.all(
    Array.from({ length: 21 }, (_, index) =>
      api('/users', {
        phone: `139400${String(index).padStart(5, '0')}`,
        name: `分页验收${String(index + 1).padStart(2, '0')}`,
        password,
        role: 'ordinary',
      }),
    ),
  )
  await page.getByRole('button', { name: '刷新', exact: true }).click()
  await page.locator('.users-table tbody tr').first().waitFor()
  await page.waitForFunction(() => document.querySelectorAll('.users-table tbody tr').length === 20)
  const firstPage = await page.locator('.users-table tbody tr').allTextContents()
  const nextPage = page.waitForResponse((response) => response.url().includes('/api/users?page=2'))
  await page.getByRole('button', { name: '下一页', exact: true }).click()
  await nextPage
  await page.locator('.pagination').getByText('2 / 2', { exact: true }).waitFor()
  await page.waitForFunction(() => document.querySelectorAll('.users-table tbody tr').length < 20)
  const secondPage = await page.locator('.users-table tbody tr').allTextContents()
  assert.ok(secondPage.length > 0)
  assert.ok(secondPage.every((text) => !firstPage.includes(text)))
  await page.getByPlaceholder('搜索姓名、手机号、角色').fill('分页验收21')
  await page.getByRole('cell', { name: '分页验收21 13940000020', exact: true }).waitFor()
  assert.equal(await page.locator('.users-table tbody tr').count(), 1)
  await page.locator('.management-card select').first().selectOption('teacher')
  await page.getByLabel('搜索上级账号', { exact: true }).fill('浏览器测试校长')
  await page
    .locator('select option')
    .filter({ hasText: '浏览器测试校长' })
    .waitFor({ state: 'attached' })
  assert.deepEqual(errors, [], '浏览器页面异常')
  console.log(
    'PASS: all page routes, admin/principal/teacher provisioning, SMS registration, learning guard, government isolation, per-user data isolation, renamed works, network recovery, mobile overflow checks',
  )
} catch (error) {
  console.error('Browser errors:', errors)
  console.error('Page text:', await page?.locator('body').innerText())
  if (page)
    await page.screenshot({ path: resolve(previewDir, 'browser-failure.png'), fullPage: true })
  throw error
} finally {
  await browser?.close()
  await stopJava()
  await vite.close()
}
