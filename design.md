# design.md

> 项目：Tiny Vow  
> 用途：作为 Android Compose UI 开发的统一设计系统约束。  
> 范围：本文件只定义视觉设计系统、组件使用和页面呈现规则，不定义数据库、业务路线、发布流程和 Agent 行为。

---

## 1. 设计方向

Tiny Vow 的 UI 方向为：

> local-first calm self-control companion  
> 本地优先、克制可信的自我约定工具

核心目标：

1. 视觉统一，减少每个页面单独调 UI；
2. 有 Material 3 的清晰结构，但不显得像后台管理工具；
3. 有“约定 + 鼓励”的温和陪伴感，但不幼稚、不鸡血；
4. 数据是判断依据，行动按钮是下一步，不堆装饰；
5. 权限、阻断、超我模式等敏感功能必须显得可信、透明、低压；
6. 首页服务今天，统计页服务复盘，奖励页服务选择，我的页服务管理；
7. 长期使用时不造成视觉疲劳，也不制造惩罚感。

---

## 2. 视觉关键词

所有页面都应符合以下关键词：

```text
克制
清楚
可信
安静
低打扰
数据可读
行动明确
本地安全感
温和但不幼稚
Material 3 但少工具感
```

禁止方向：

```text
不要游戏化过重
不要健身打卡 App 风
不要金融仪表盘风
不要后台管理系统风
不要惩罚感太强
不要会员营销页风
不要大面积霓虹渐变
不要复杂拟物装饰
不要密集堆卡片
不要每个功能页面单独发明样式
```

---

## 3. 信息气质

Tiny Vow 不是“惩罚你别玩手机”的 App，而是“帮你把约定看清楚并守住”的工具。

文案和视觉都要遵守：

1. 把状态说清楚，不吓人；
2. 把原因说清楚，不命令；
3. 把下一步说清楚，不只报错；
4. 对权限保持透明，不暗示必须授权全部增强权限；
5. 对失败和空状态保持温和，不使用羞辱、鸡血或强刺激话术。

推荐语气：

```text
今天的约定
本周期仍会记录真实用量
需要使用情况访问权限后才能生成战报
可以先添加一个约定
这个权限用于识别前台应用和显示超额提醒
```

避免语气：

```text
你又失败了
马上开启所有权限
不授权就无法使用
神级自律神器
立即变强
彻底戒掉手机
```

---

## 4. 颜色系统

项目已经支持预设主题和自定义三色主题，颜色必须从 `ThemeSeed`、`ThemeTokens`、`MaterialTheme.colorScheme` 或 `LocalThemeColors.current` 读取。

业务页面禁止直接写新的裸颜色，除非属于：

1. Canvas 图表内部的固定语义色；
2. 成就徽章、分享图等独立视觉资产；
3. 兼容旧资源或 Android 系统 API 所需颜色。

### 4.1 语义颜色

| 语义 | 来源 | 用途 |
|---|---|---|
| 页面背景 | `MaterialTheme.colorScheme.background` / `LocalThemeColors.pageGradient` | 主页面背景 |
| 卡片背景 | `MaterialTheme.colorScheme.surface` | 普通信息卡 |
| 柔和背景 | `LocalThemeColors.current.surfaceSoft` | 次级区域、空状态、表单底 |
| 玻璃背景 | `LocalThemeColors.current.surfaceGlass` | 底部导航、轻浮层 |
| 主行动 | `LocalThemeColors.current.base` | 主按钮、选中态、关键入口 |
| 约定色 | `LocalThemeColors.current.control` | CONTROL 分组、限额、节制、阻断相关 |
| 鼓励色 | `LocalThemeColors.current.encourage` | ENCOURAGE 分组、积分、目标达成 |
| 保存/成长色 | `LocalThemeColors.current.save` | 节省、专注完成、正向成果 |
| 约束/危险色 | `LocalThemeColors.current.restraint` / `danger` | 删除、超额、关键风险 |
| 强文字 | `LocalThemeColors.current.inkStrong` | 标题、关键数字 |
| 正文 | `LocalThemeColors.current.ink` | 普通文本 |
| 弱文字 | `LocalThemeColors.current.inkMuted` / `inkFaint` | 辅助说明、空状态 |
| 边框 | `LocalThemeColors.current.borderSoft` | 卡片边框 |
| 分割线 | `LocalThemeColors.current.dividerSoft` | 列表分割 |

### 4.2 颜色使用规则

1. 首页和设置页以背景、白色卡片、少量主题色为主。
2. `control` 只用于限额、阻断、约定、危险接近等语义。
3. `encourage` 只用于积分、奖励、鼓励目标、正向进度等语义。
4. `danger` 只用于删除、清空、不可逆或明确失败。
5. 图表可以使用 `chartPalette`，但不要把图表色扩散到普通 UI。
6. 权限页不要大面积使用红色，未开启状态用状态点和说明表达。
7. PRO/订阅入口避免金色营销感，优先作为权益状态卡处理。

---

## 5. 字体系统

所有字体样式必须使用 `MaterialTheme.typography`。业务页面禁止新增裸 `fontSize`，除非在 Canvas 分享图或徽章绘制中。

### 5.1 字体层级

| 场景 | 样式 |
|---|---|
| 极少数大数字 | `displaySmall` / `headlineLarge` |
| 页面主标题 | `headlineMedium` / `headlineSmall` |
| 区块标题 | `titleLarge` |
| 卡片标题 | `titleMedium` |
| 设置项标题 | `titleSmall` / `bodyLarge` |
| 正文说明 | `bodyMedium` |
| 辅助说明 | `bodySmall` |
| 标签、状态、按钮 | `labelLarge` / `labelMedium` |

### 5.2 文本约束

1. 一个页面内不要超过 5 种文字层级。
2. 一张普通卡片内不要超过 3 种文字层级。
3. 首页大数字可以突出，但不能形成金融仪表盘风。
4. 统计页允许数据密度更高，但标题、解释、图表之间必须有呼吸感。
5. 按钮文字最多一行，必要时缩短文案，不压缩字号。
6. 不使用负字距；全项目字体字距保持 0。

---

## 6. 间距系统

所有常规间距必须从 `TinyVowSpacing` 读取。

| 语义 | 当前 token | 用途 |
|---|---|---|
| 页面左右边距 | `PageHorizontal` | 主页面内容边距 |
| 页面顶部间距 | `PageTop` | 顶部内容起点 |
| 区块间距 | `SectionGap` | 页面内大模块之间 |
| 卡片间距 | `CardGap` | 卡片、列表项之间 |
| 卡片横向内边距 | `CardHorizontal` | 普通卡片内容 |
| 卡片纵向内边距 | `CardVertical` | 普通卡片内容 |
| 紧凑卡片横向内边距 | `CompactCardHorizontal` | 列表型小卡 |
| 紧凑卡片纵向内边距 | `CompactCardVertical` | 列表型小卡 |

页面密度：

```text
首页：中低密度，突出今天状态和下一步行动
统计页：中密度，允许图表密集但区块分明
奖励页：中密度，强调库存、代价、可用性
我的页：中密度，像设置中心但不冷
权限页：中低密度，解释优先于按钮
阻断页：低密度，强焦点、少干扰
```

---

## 7. 圆角、边框和阴影

所有圆角必须从 `TinyVowRadius` 读取。所有常规卡片必须使用 `TinyVowCard`。

| 场景 | 圆角 |
|---|---|
| 首屏主卡 / 重点卡 | `FeaturedCard` |
| 普通卡片 | `Card` |
| 列表项卡片 | `ItemCard` |
| 输入框 / 筛选器 / 小按钮 | `Control` |
| 标签 / 状态胶囊 | `Pill` |

阴影规则：

1. 普通卡片默认轻阴影或无阴影；
2. 首页主卡、底部导航、弹窗可以使用 `FeaturedCard` 级别阴影；
3. 不使用重阴影、彩色阴影；
4. 层级优先靠背景、留白、边框和标题组织，而不是靠阴影堆叠。

---

## 8. 核心组件

所有业务页面优先使用以下组件。需要新视觉结构时，先抽象到 `ui/theme` 或对应模块组件文件，再在页面中使用。

### 8.1 `TinyVowPageBackground`

统一页面背景。适用于首页、统计、奖励、我的、二级设置页。

职责：

```text
统一背景色/渐变
承载 full-size 页面
避免每页单独写 background
```

### 8.2 `TinyVowCard`

所有普通卡片默认使用它。

默认规则：

```text
背景：surface
圆角：TinyVowRadius.Card
边框：borderSoft
阴影：轻
内边距：由卡片内容自行用 TinyVowSpacing 控制
```

卡片语义：

```text
featured  首页重点状态、用户状态、关键权限
normal    普通信息区块
soft      空状态、提示、说明
item      列表项、奖励项、设置项
danger    删除、清理、本地数据风险提示
```

### 8.3 `TinyVowSection`

用于页面区块。

结构：

```text
图标 / 标题
可选副标题
可选右侧操作
内容
```

区块标题不要做成大卡片；它是页面导航标记。

### 8.4 `TinyVowButton`

按钮类型：

```text
Neutral   次级行动
Primary   页面主行动
Danger    删除、清空、退出、危险确认
```

规则：

1. 一个视图内最多一个强主按钮；
2. 主按钮使用 `Primary`；
3. 危险按钮必须只用于危险操作；
4. 不使用渐变按钮；
5. 小图标操作优先使用图标按钮或胶囊按钮，不写成长文字按钮。

### 8.5 `TinyVowStatusPill`

用于权限状态、PRO 状态、库存状态、归档状态。

规则：

1. 只显示短状态，不承载长解释；
2. 状态颜色必须来自语义色；
3. 不要用红色填满整张卡片表示未开启。

### 8.6 `TinyVowEmptyState`

空状态结构：

```text
轻图标
一句主文案
一句说明
一个可选行动
```

不要只写“暂无数据”。空状态必须告诉用户为什么为空，下一步是什么。

### 8.7 表单字段

表单输入优先用模块内已有紧凑字段组件；新增表单组件应统一：

```text
圆角：TinyVowRadius.Control
高度：约 50-56dp
背景：surfaceSoft / surfaceVariant 低透明
边框：borderSoft
错误：danger + 简短说明
```

---

## 9. 页面级规则

### 9.1 首页

首页目标：

> 让用户一眼看到今天是否还在约定内，以及下一步该做什么。

推荐结构：

```text
今天概览 / 圆环状态
约定与鼓励分组
离线专注
权限可靠性提示
今日奖励/生效道具
```

禁止：

1. 首页变成复杂统计仪表盘；
2. 把所有功能入口堆成宫格；
3. 用强烈惩罚色压迫用户；
4. 未授权时只给一个按钮，不解释用途。

### 9.2 统计页

统计页目标：

> 复盘真实行为，而不是制造焦虑。

规则：

1. 报告顶部先给时间窗口和分享入口；
2. 今日、周、月、年数据使用一致的卡片语义；
3. 趋势、热力图、Top Apps 必须有清晰标题和说明；
4. 无权限、无归档、样本不足都必须有专门空状态；
5. 免费版锁定高级战报时，要解释权益，不遮挡基础日报。

### 9.3 奖励页

奖励页目标：

> 让积分、库存、兑换代价和使用后果清楚可判断。

规则：

1. 顶部固定展示当前积分；
2. 内置奖励按使用目的分组，不混成一张长清单；
3. 购买按钮必须表达“花费”和“是否可购买”；
4. 需要超我保护的奖励要有明确保护感；
5. 自定义奖励是用户数据，不自动翻译。

### 9.4 我的页

我的页目标：

> 管理身份、权益、权限、外观、隐私和版本。

规则：

1. 顶部是账户/本地身份，不做营销 hero；
2. 总节省、总积分、使用天数可以作为可信摘要；
3. 设置分组要像系统设置，图标线性、文字清楚；
4. 隐私和本地数据管理必须保持显眼且可信；
5. PRO 状态作为权益状态，不做夸张促销。

### 9.5 权限页

权限页目标：

> 让用户理解每个敏感权限为什么需要、是否必要、如何开启。

规则：

1. Usage Access + Accessibility 是核心权限；
2. 通知、电池白名单、自启动是可靠性增强；
3. 每张权限卡必须包含用途、当前状态、行动；
4. 未开启状态可以提醒，但不要写成硬性前置条件；
5. 打开系统设置前必须已有 disclosure 或清楚说明。

### 9.6 阻断 overlay

阻断页目标：

> 在超额时打断行为，但不羞辱用户。

规则：

1. 层级简单：原因、用量、可选行动；
2. 默认行动应是离开或回到 Tiny Vow；
3. 紧急解锁必须显示代价和历史保留语义；
4. 不出现“失败”“自控力差”等词；
5. 主题颜色跟随当前主题，但阻断语义使用 `control/restraint`。

---

## 10. 图标、动效和图表

图标：

```text
线性为主
少量填充用于选中态
线宽统一
状态语义明确
不使用卡通或 3D 图标
```

动效：

```text
页面切换自然
状态变化轻微淡入/位移
进度变化平滑但不炫
阻断页不使用娱乐化动效
```

图表：

1. 图表颜色来自 `chartPalette`；
2. 图表必须有标题或上下文；
3. 不用过多图例占据首屏；
4. 空图表要显示原因，不显示空白坐标系；
5. 分享图可以更有表现力，但仍要清晰可信。

---

## 11. 多语言和文案

用户可见文案必须走 `app_texts.xml` 或基础 `strings.xml` 中已有系统文案。新增文案必须同时添加英文和简体中文。

规则：

1. 不在 Compose 中硬编码用户可见文案；
2. Canvas 分享图文字也要本地化；
3. 用户数据不翻译；
4. 内置奖励、成就、主题名通过 key 本地化；
5. 中英文占位符数量和顺序必须一致。

---

## 12. Codex UI 开发规则

Codex 修改 UI 时必须遵守：

1. 先查 `design.md`、`ui/theme`、相关页面已有组件；
2. 能复用 `TinyVowCard`、`TinyVowButton`、`TinyVowSection`、`TinyVowEmptyState` 就不要新造样式；
3. 不在业务页面直接新增裸颜色；
4. 不在业务页面直接新增裸字号；
5. 不在业务页面随意新增圆角、阴影、边框；
6. 页面布局优先使用设计系统组件，业务页面只负责数据绑定和局部排列；
7. 新状态必须覆盖加载、空、无权限、错误、受保护、PRO 限制；
8. 改权限、阻断、奖励、统计等敏感 UI 时同步检查文案是否准确；
9. 新增文案同步英文/中文资源；
10. 大改 UI 后至少运行 `.\gradlew.bat assembleDefaultDebug`，并安装应用；无法安装时说明原因。
11. 二级设置页、帮助页、特殊应用页、媒体应用页和离线专注设置页必须使用 `TinyVowPageBackground` 承载内容区。
12. 设置分组优先使用 `TinyVowSettingsGroup` / `TinyVowSettingsItem`，不要在页面里重新组合 `Surface + Row + Icon + Arrow`。

---

## 13. 第一阶段必须统一的组件

当前 Compose 实现入口：

```text
app/src/main/java/com/rrrrz/tinyvow/ui/theme/UiTokens.kt
app/src/main/java/com/rrrrz/tinyvow/ui/theme/TinyVowDesignSystem.kt
```

P0：

```text
TinyVowPageBackground
TinyVowDetailScaffold
TinyVowCard
TinyVowSection
TinyVowButton
TinyVowStatusPill
TinyVowEmptyState
TinyVowMetricTile
TinyVowSettingsGroup
TinyVowSettingsItem
```

P1：

```text
TinyVowSegmentedControl
TinyVowField
TinyVowPermissionCard
TinyVowStatCard
TinyVowChartCard
TinyVowBottomSheet
TinyVowTopBar
TinyVowIconSurface
```

---

## 14. 最终 UI 原则

Tiny Vow 的 UI 不是靠单页惊艳，而是靠长期一致、可信、低打扰。

最终原则：

```text
少刺激，重判断。
少装饰，重状态。
少惩罚，重约定。
少临时样式，重组件复用。
少营销感，重本地安全感。
```
