import {
  BookOpen,
  Video,
  Box,
  Code2,
  Trophy,
  Route,
  Star,
  ClipboardList,
  FileText,
  Presentation,
  Users,
  BadgeCheck,
  Building2,
  GraduationCap,
  FlaskConical,
  MessagesSquare,
} from 'lucide-vue-next'

export const homeSections = [
  {
    id: 'courses',
    title: '课程服务中心',
    description: '共享优质课程资源，让学习与探索随时发生。',
    path: '/course-services',
    items: [
      {
        title: '在线精品课程',
        description: '编程、人工智能、机器人与创意设计课程。',
        path: '/course-services/online',
        icon: BookOpen,
        action: '浏览课程资源',
        target: '/courses',
      },
      {
        title: '视频公开课',
        description: '通过主题讲解和案例演示，拓展科技视野。',
        path: '/course-services/open-classes',
        icon: Video,
      },
    ],
  },
  {
    id: 'products',
    title: '产品服务中心',
    description: '连接课程与实践，了解科技教育产品和配套资源。',
    path: '/products',
    items: [
      {
        title: '科技教育产品',
        description: '了解面向科技课堂的教学工具与实践器材。',
        path: '/products/tools',
        icon: Box,
      },
      {
        title: '课程配套资源',
        description: '围绕学习主题，查找课程与实践资源。',
        path: '/products/resources',
        icon: Code2,
        action: '浏览商品与课程',
        target: '/shop',
      },
    ],
  },
  {
    id: 'competitions',
    title: '赛事中心',
    description: '从赛事了解、成长规划到特长培养与等级考试。',
    path: '/competitions',
    items: [
      {
        title: '服务',
        description: '赛事信息与参赛支持',
        path: '/competitions/services',
        icon: Trophy,
      },
      {
        title: '规划',
        description: '了解学习路径与升学规划',
        path: '/competitions/planning',
        icon: Route,
      },
      {
        title: '特长',
        description: '科技兴趣与特长培养',
        path: '/competitions/talents',
        icon: Star,
      },
      {
        title: '考试',
        description: '等级考试与备考信息',
        path: '/competitions/exams',
        icon: ClipboardList,
      },
    ],
  },
  {
    id: 'teachers',
    title: '教师培养中心',
    description: '围绕政策、教学与交流，支持教师专业成长。',
    path: '/teacher-growth',
    items: [
      {
        title: '科技教育政策研读',
        description: '关注政策导向与教育实践',
        path: '/teacher-growth/policies',
        icon: FileText,
        action: '查看政策资讯',
        target: '/news?section=policies',
      },
      {
        title: '课程实施',
        description: '课程设计与课堂教学支持',
        path: '/teacher-growth/teaching',
        icon: Presentation,
        action: '浏览课程资源',
        target: '/courses',
      },
      {
        title: '虚拟教研室',
        description: '教学交流与协同研修',
        path: '/teacher-growth/research',
        icon: Users,
      },
      {
        title: '资格证',
        description: '了解资格与认证相关信息',
        path: '/teacher-growth/certificates',
        icon: BadgeCheck,
      },
    ],
  },
  {
    id: 'study-tours',
    title: '研学中心',
    description: '走进企业、高校与科研院所，在交流中拓展视野。',
    path: '/study-tours',
    items: [
      {
        title: '企业',
        description: '走近产业，了解科技应用',
        path: '/study-tours/enterprises',
        icon: Building2,
      },
      {
        title: '高校',
        description: '感受校园与学科探索',
        path: '/study-tours/universities',
        icon: GraduationCap,
      },
      {
        title: '科研院所',
        description: '认识科研场景与研究方法',
        path: '/study-tours/institutes',
        icon: FlaskConical,
      },
      {
        title: '专家面对面',
        description: '交流提问，拓展科学视野',
        path: '/study-tours/experts',
        icon: MessagesSquare,
      },
    ],
  },
]

export const homeTopicPaths = homeSections.flatMap((section) => [
  section.path,
  ...section.items.map((item) => item.path),
])
