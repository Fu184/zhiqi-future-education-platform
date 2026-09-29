import { reactive, watch } from 'vue'

export const featuredCourses = [
  { id: 1, title: '人工智能启蒙：让机器学会思考', type: '人工智能', level: '小学高年级', lessons: 12, learners: '2.4k', color: 'purple', icon: 'BrainCircuit', summary: '从生活里的 AI 出发，认识机器学习的基本概念，完成第一个智能小项目。', chapters: ['走近人工智能', '图像识别初体验', '训练我的分类器', '创意项目实践'] },
  { id: 2, title: 'Python 编程：从零到创意作品', type: '编程创造', level: '初中', lessons: 16, learners: '1.8k', color: 'blue', icon: 'Code2', summary: '用有趣的案例掌握变量、条件与循环，编写属于自己的互动程序。', chapters: ['认识 Python', '变量与输入输出', '让程序做选择', '做一款互动小游戏'] },
  { id: 3, title: '机器人探索：感知与行动', type: '机器人', level: '小学', lessons: 10, learners: '1.2k', color: 'orange', icon: 'Bot', summary: '观察传感器与执行器如何配合，设计机器人的第一个挑战任务。', chapters: ['认识机器人', '传感器的秘密', '让机器人动起来', '完成挑战任务'] },
  { id: 4, title: '三维创意设计与打印', type: '创意设计', level: '初中', lessons: 8, learners: '986', color: 'green', icon: 'Box', summary: '从空间想象到数字建模，动手设计一件可以制作的创意作品。', chapters: ['三维世界入门', '基础造型', '组合与修改', '作品发布'] },
]

export const initialNews = [
  { id: 1, category: '平台动态', date: '2026.09.18', title: '让科技教育更有温度：智启未来平台正式上线', excerpt: '以课程、创作和实践为核心，连接学校、教师与每一位热爱探索的学生。' },
  { id: 2, category: '教学新闻', date: '2026.09.12', title: '项目式学习专题课程正式开放', excerpt: '围绕真实问题开展跨学科学习，让创意从课堂走向生活。' },
  { id: 3, category: '学校新闻', date: '2026.09.06', title: '新学期，一起开启科技探索之旅', excerpt: '从编程到人工智能，为新学期准备更多有趣的学习体验。' },
  { id: 4, category: '公告', date: '2026.09.01', title: '关于平台学习空间试运行的通知', excerpt: '学习记录、任务提交和作品展示等功能已开放体验。' },
]

const initialTasks = [
  { id: 1, title: '设计一个会打招呼的机器人', course: '机器人探索：感知与行动', due: '2026.10.08', status: '进行中', description: '结合传感器或交互逻辑，让机器人根据不同情境打招呼。' },
  { id: 2, title: '我的第一个 Python 小程序', course: 'Python 编程：从零到创意作品', due: '2026.10.12', status: '未开始', description: '编写一个简单的互动程序，并解释自己的设计思路。' },
  { id: 3, title: '用 AI 识别身边的植物', course: '人工智能启蒙：让机器学会思考', due: '2026.09.30', status: '已完成', description: '收集身边植物的照片，体验图像分类的完整过程。' },
]

const initialWorks = [
  { id: 1, title: '星际漫游', author: '小智同学', type: 'Scratch', color: 'purple', icon: 'Rocket', description: '驾驶飞船穿过星际障碍，收集能量星星。' },
  { id: 2, title: '天气小助手', author: '探索者', type: 'Python', color: 'blue', icon: 'CloudSun', description: '输入城市与天气，得到贴心的出行建议。' },
  { id: 3, title: '未来教室', author: '创想家', type: '3D 设计', color: 'orange', icon: 'Box', description: '把心目中的未来学习空间变成立体模型。' },
]

function saved(key, fallback) {
  try { return JSON.parse(localStorage.getItem(key)) ?? fallback } catch { return fallback }
}

export const store = reactive({
  news: saved('zhiqi-news', initialNews),
  tasks: saved('zhiqi-tasks', initialTasks),
  works: saved('zhiqi-works', initialWorks),
  favorites: saved('zhiqi-favorites', []),
  enrolled: saved('zhiqi-enrolled', []),
  user: saved('zhiqi-user', null),
})

for (const key of ['news', 'tasks', 'works', 'favorites', 'enrolled', 'user']) {
  watch(() => store[key], value => localStorage.setItem(`zhiqi-${key}`, JSON.stringify(value)), { deep: true })
}
