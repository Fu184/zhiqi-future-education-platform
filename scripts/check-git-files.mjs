// 在项目任意目录运行；--committed 检查真正会随当前提交推送的文件。
import { execFileSync } from 'node:child_process'
import { existsSync, readdirSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { resolve } from 'node:path'

const root = fileURLToPath(new URL('../', import.meta.url))
const committed = process.argv.includes('--committed')
const refIndex = process.argv.indexOf('--ref')
const revision = refIndex === -1 ? 'HEAD' : process.argv[refIndex + 1]
if (!revision || revision.startsWith('-')) throw new Error('Invalid revision')
function git(args, input) {
  return execFileSync('git', args, { cwd: root, input, encoding: 'utf8' })
}
function files(output) {
  return output.split('\0').filter(Boolean)
}
const eligible = [...new Set(files(git(['ls-files', '--cached', '--others', '--exclude-standard', '-z'])))].filter((p) => existsSync(resolve(root, p)))
const snapshot = committed
  ? files(git(['ls-tree', '-r', '--name-only', '-z', revision]))
  : eligible
const required = new Set([
  '.gitignore', '.gitattributes', '.editorconfig', 'README.md', 'GIT_GUIDE.md',
  'pom.xml', 'backend/pom.xml', 'backend/mvnw', 'backend/mvnw.cmd',
  'backend/.mvn/wrapper/maven-wrapper.properties', 'backend/.env.example',
  'backend/README.md', 'backend/sql/README.md', 'backend/sql/schema-mysql.sql',
  'frontend/index.html', 'frontend/package.json', 'frontend/package-lock.json',
  'frontend/vite.config.js', 'frontend/.prettierrc.json', 'frontend/README.md',
  'frontend/DSC_4070.jpg', '.run/Spring Boot Backend.run.xml',
  'scripts/check-git-files.mjs', '.githooks/pre-push', '.github/workflows/check-project.yml',
  '.github/workflows/deploy-pages.yml',
])
function requireDirectory(directory) {
  const absolute = resolve(root, directory)
  if (!existsSync(absolute)) {
    required.add(directory)
    return
  }
  for (const entry of readdirSync(absolute, { withFileTypes: true })) {
    const path = `${directory}/${entry.name}`
    if (entry.isDirectory()) requireDirectory(path)
    else required.add(path)
  }
}
for (const directory of ['frontend/src', 'frontend/scripts', 'backend/src', 'backend/sql']) requireDirectory(directory)
if (existsSync(resolve(root, 'frontend/public'))) requireDirectory('frontend/public')
const problems = []
const present = new Set(snapshot)
for (const path of required) {
  if (!present.has(path)) problems.push(`缺少${committed ? '已提交' : '可提交'}文件: ${path}`)
}
function forbidden(path) {
  const name = path.split('/').at(-1)
  return /(^|\/)(node_modules|dist|target|build|coverage|\.vite|\.idea|\.vscode|__pycache__)(\/|$)/.test(path)
    || /^(artifacts|data|backend\/data|frontend\/previews)\//.test(path)
    || (name.startsWith('.env') && name !== '.env.example')
    || /\.(log|tmp|bak|swp|sqlite|sqlite3|pem|key|p12|pfx|jks)$/.test(name)
    || /\.(mv|trace|lock)\.db$/.test(name)
    || /\.sqlite-(shm|wal)$/.test(name)
    || /^\.browser-/.test(path)
    || ['.DS_Store', 'Thumbs.db', 'Desktop.ini'].includes(name)
}
for (const path of snapshot) {
  if (forbidden(path)) problems.push(`无关或本地私有文件进入提交范围: ${path}`)
}
// 使用 --no-index，已被跟踪的源码也不能被新的忽略规则误排除。
try {
  const ignored = git(['check-ignore', '--no-index', '--stdin'], [...required].join('\n') + '\n').trim()
  if (ignored) for (const path of ignored.split('\n')) problems.push(`必需文件被忽略: ${path}`)
} catch (error) {
  if (error.status !== 1) throw error
}
if (committed) {
  for (const path of files(git(['diff', revision, '--name-only', '-z']))) {
    problems.push(`修改尚未提交，推送会遗漏: ${path}`)
  }
  for (const path of eligible) {
    if (!present.has(path)) problems.push(`文件尚未提交，推送会遗漏: ${path}`)
  }
} else {
  const untracked = files(git(['ls-files', '--others', '--exclude-standard', '-z']))
  console.log(`可提交文件 ${snapshot.length} 个，其中未加入 Git ${untracked.length} 个。`)
  if (untracked.length) console.log('提交前执行 git add -A，再查看 git diff --cached --stat；提交后运行 --committed 检查。')
}
if (problems.length) {
  console.error(problems.join('\n'))
  process.exitCode = 1
} else {
  console.log(`通过：${committed ? '当前提交' : '工作区提交范围'}包含必需源码、SQL 和构建配置，未包含已知私有文件或生成产物。`)
}
