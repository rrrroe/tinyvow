# Tiny Vow Agent 规范

本文件是后续 Agent 在 `E:\Project\tinyvow` 工作时的项目说明和约束。修改代码前先读本文件，涉及具体模块时再读对应源码。

## 项目概况

- Tiny Vow 是一个本地优先的 Android 应用，用“约定 + 鼓励”管理手机使用。
- 当前是单模块项目：`:app`，包名和 namespace 都是 `com.rrrrz.tinyvow`。
- 技术栈：Kotlin、Jetpack Compose、Material 3、Room、DataStore Preferences、WorkManager、AccessibilityService、UsageStats、NotificationListenerService / MediaSession、步数传感器、Foreground Service、Play Billing、Credential Manager / Google ID。
- 构建配置：`compileSdk 36.1`、`targetSdk 36`、`minSdk 26`、Java 11、Kotlin `2.2.10`、AGP `9.1.0`。
- 入口：`MainActivity` 挂载 Compose UI，`TinyVowApplication` 初始化 `AppText`。
- 应用数据默认保存在本机；隐私导出/清理在 `data/privacy`，不要无意引入自动上传或远端依赖。

## 品牌与产品命名

- 正式中文品牌是「揉揉喽」，正式英文品牌是 `Rorolo`，品牌主域名是 `rorolo.com`。
- 品牌简称可使用「揉揉」或 `Roro`；正式署名使用「揉揉喽出品」或 `by Rorolo`，不要自行创造其他品牌变体。
- `Tiny Vow` 是英文产品名，「小约定」是简体中文产品名；产品名与品牌名是两层身份，不得用「揉揉喽」或 `Rorolo` 替换应用名称。
- Tiny Vow 对应的品牌子域名是 `tinyvow.rorolo.com`；品牌主页使用 `rorolo.com` 或 `www.rorolo.com`。
- 品牌调整不等于代码标识符迁移。未经明确要求，不修改 `com.rrrrz.tinyvow`、Room 数据库名、DataStore 名、Gradle 属性、产品 ID、历史文件名和其他兼容性标识。
- 域名确定不代表可以自动引入服务端或上传数据。Tiny Vow 继续遵守 local-first 基线；新增联网、账号、同步或后端能力时，必须单独确认数据边界并同步隐私说明。
- 修改公开产品名、品牌署名、官网或联系地址时，要同步检查中英文字符串资源、应用市场文案、分享图、隐私政策、账号删除页、发布检查清单和应用内链接。
- 新域名页面正式部署并验证可访问前，不得提前替换当前生产中的隐私政策、账号删除或 API 地址。

## 三项目协作与发布授权

Tiny Vow 由三个相邻但独立的 Git 仓库协作：

- `E:\Project\tinyvow`：Android 应用主仓库，负责客户端功能、应用内链接、版本与签名产物、`design/` 原始素材，以及 `docs/privacy.html`、`docs/account-delete.html` 等合规内容源文件。
- `E:\Project\tinyvow-site`：官网仓库，负责 `tinyvow.rorolo.com` 的页面、下载入口、公开合规页面副本和网站发布资源；同一源码还发布到 `.openai/hosting.json` 对应的 Sites 项目。
- `E:\Project\tinyvow-backend`：国内版后端，负责账号、设备会话、头像、商品、订单、支付、激活码、会员权益、邮件和 Flyway 数据库迁移。

三个仓库的配合关系如下：

- 客户端和后端 API 合同必须成对核对。修改请求/响应、认证、账号、头像、商品、订单、支付、权益或删除账号行为时，同时检查应用与后端的兼容性和测试。
- APK 必须先在本仓库构建、签名、校验并归档到 `dist/`，再由官网仓库复制到 `public/downloads/`；网站不得自行构造或修改 APK。
- 网站宣传图以本仓库 `design/appstore/exports/` 中的当前定稿为源，官网仓库中的 `public/marketing/` 只是发布副本。
- 官网和应用分享界面使用的二维码以本仓库 `design/tinyvow-website-qr.png` 为源；应用资源与官网仓库 `public/tinyvow-website-qr.png` 必须从该文件同步，不能各自维护不同版本。
- 隐私政策和账号删除说明以本仓库 `docs/` 为内容源。后端数据边界发生变化时，先核对后端事实，再同步本仓库文档、应用内链接/文案和官网公开副本。
- 网站纯视觉改动不要求修改应用或后端；应用纯本地功能不要求修改网站或后端。完成用户目标确实需要跨仓库联动时，可以直接进行必要的本地修改，但三个仓库必须分别验证、分别提交，并在最终说明中列出影响范围。

生产环境授权遵守以下规则：

- 用户要求“修改、修复、优化、调整、更新、同步、打包、生成 release、构建、测试、验收、准备发布”时，默认只进行本地修改、构建和验收。
- 上述普通指令不授权上传服务器、部署后端、发布 Sites、替换线上 APK/图片/页面、提交应用商店、推送会触发部署的分支、打发布标签或改变其他生产状态。
- 只有用户明确使用“发布、部署、上线、上传到服务器/应用商店”等表述时，才获得对应生产目标的授权。目标明确后直接执行，不再重复询问。
- “发布官网”同时发布官方域名 `tinyvow.rorolo.com` 和官网仓库配置的 Sites 生产版本，但不部署应用或后端。
- “发布后端”只部署后端及该版本必要的、已经审查和验证的非破坏性 Flyway migration，不发布应用或官网。删除表/字段、批量改写或删除数据等破坏性操作仍需单独确认。
- “发布国内版”包括构建并校验签名 APK，以及将同一 APK、版本信息、当前定稿宣传图和合规页面同步发布到官方域名与 Sites；不自动部署后端，除非用户同时明确要求。
- “发布 Google Play 版”只授权生成并上传对应 AAB/商店版本，不自动发布国内 APK、官网或后端。
- 在跨项目任务中只说“发布”但目标不明确时，必须先确认具体目标，不得默认三个项目全部发布。
- 允许创建本地 Git commit；默认不 push、不打 tag、不创建远端版本。明确发布时，授权包含该发布流程必需的上传和精确源码推送，但不得夹带无关改动。
- 生产发布失败时可以自动回滚本次刚替换的应用包、网站版本或后端 jar/config；数据库回滚、数据恢复、删除和手工修复仍需单独确认。

本仓库的发布脚本也必须遵守上述授权边界：`tools/package-china-release.ps1` 和 `tools/package-release-artifacts.ps1` 默认只生成本地产物；只有获得明确发布授权后才可传入 `-PublishWebsite`。不要仅凭脚本历史默认行为推断用户已经允许上线。

## 当前产品基线

把下面能力视为已经验证过的稳定主线，除非修复明确 bug，不要推倒重做：

- `CONTROL` 分组：按日/周/月限额统计使用量，超额后软阻断。
- `ENCOURAGE` 分组：按使用时长累计积分，并支持目标达成奖励。
- 鼓励指标：`ENCOURAGE` 分组当前支持 App 使用时长和本机步数两种指标。
- 特殊应用设置：当前支持微信读书双源时长，分别缓存微信读书阅读时长和本机前台时长，并按用户配置决定替换口径。
- 媒体应用设置：通过通知监听 + MediaSession 记录用户启用的播客/音乐/有声书后台播放时长，并与前台 session 合并去重。
- 离线专注：本地专注计时、分类、普通/严格模式、白名单和专注积分。
- 每日签到：本地签到记录，并发放内置临时缓冲卡到库存。
- 超我模式：用本机密码保护修改约定、删除约定、修改超我设置和购买绕过限额类道具等关键操作。
- 使用情况访问权限：读取应用用量和使用周期统计。
- 无障碍服务：监听前台窗口变化，显示全屏阻断 overlay，并承担一部分积分结算。
- 奖励/库存/使用/成就：Room 持久化，积分通过 ledger 记录来源。
- 统计页：基于每日归档和当前 UsageStats 前台 session 口径展示日报、趋势、热力图、分享图等。
- 外观设置：预设主题 + 自定义三色主题，以及最小/较小/标准/较大四档应用页面文字大小，DataStore 保存；原始字号对应第三档“标准”。
- 多语言：支持系统语言、简体中文、英文。
- 订阅/权益：Google Play 版走 Play Billing；国内版走本地激活码。Google Play 配置不完整时要有可理解的错误文案。

## 业务逻辑速览

### 启动与全局状态

- `TinyVowApplication` 调用 `AppText.attach(...)`，让服务、Worker、通知等非 Compose 代码也能读取应用文案。
- `MainActivity` 开启 edge-to-edge，创建通知渠道，监听主题和语言偏好，并挂载 `HomeRoute`。
- `ManagedAppPreferences` 通过 DataStore Preferences 保存积分、今日积分、主题、应用页面文字大小、语言、业务日分割点、用户资料、权限 disclosure 状态、权限提示 dismissed 状态、提醒设置、App 颜色偏好、首页圆环偏好、离线专注默认项、超我模式状态、旧单 App 限额兼容字段等全局状态。
- 主题通过 `resolveThemeSeed(...)` 选择预设或自定义三色主题；阻断 overlay 和统计分享图也要跟随当前主题。
- 语言通过 `AppText.localizedContext(...)` 注入 `LocalContext`，同时用 `AppText.setLanguage(...)` 更新全局文案上下文。

### 分组模型

- `AppGroupEntity` 是主模型，核心字段包括 `type`、`limitPeriod`、`limitMinutes`、`pointsPerMinute`、`encourageMetric`、`stepTarget`、`pointsPerStep`、`lastBonusAt`、`sortOrder`。
- `GroupAppCrossRef` 表示分组与 App 包名关系。
- `CONTROL` 分组把 `limitMinutes` 视为限额；`ENCOURAGE` 分组在 `APP_USAGE` 指标下把 `limitMinutes` 视为目标时长，在 `STEPS` 指标下使用 `stepTarget` 和 `pointsPerStep`。
- 分组和关联关系使用软删除；历史记录和归档可能引用旧分组、旧名称、旧 App 关系，不要物理删除后强行抹掉历史。
- 同一个 App 可以属于多个分组。分组明细按分组视角分别展示贡献；设备总用量、周/月/年总览和 Top Apps 聚合要按 package 去重。

### 实时阻断链路

实时阻断由 `UsageStatsUsageRepository`、`GroupLimitEnforcer` 和 `AppLimitAccessibilityService` 协作：

1. `AppLimitAccessibilityService` 监听 `TYPE_WINDOW_STATE_CHANGED` 和 `TYPE_WINDOWS_CHANGED`。
2. 如果当前包名是 Tiny Vow 自身，只结算上一个 App 的积分，不做限额检查。
3. 服务对高频窗口事件做短防抖，并把最新事件送入 conflated channel。
4. `GroupLimitEnforcer.evaluate(packageName)` 查询该包所属的 `CONTROL` 分组，按分组周期统计组内所有 App 总使用时长。
5. 有效限额 = 分组基础限额 + 当前生效加时包。
6. 实时阻断语义是“超过有效限额即阻断”，不使用统计裕度。
7. 服务记录 `BlockEventEntity`，再在主线程显示 `TYPE_ACCESSIBILITY_OVERLAY`。
8. 切换到非超额 App 或回到 Tiny Vow 时移除 overlay。

服务内有分组配置缓存和使用量缓存，用于降低前台切换热路径里的 DAO 与 UsageStats 读取成本。

### UsageStats 用量口径

- Tiny Vow 的“应用使用时长”主口径是 `UsageStatsManager.queryEvents(...)` 中的前台 Activity session：用 `ACTIVITY_RESUMED` 与 `ACTIVITY_PAUSED` / `ACTIVITY_STOPPED` 合成 session，再按统计窗口裁剪后聚合。
- 不要把 `queryAndAggregateUsageStats(...).totalTimeInForeground` 作为首页、限额、积分、归档的主使用时长来源。该累计 bucket 在不同厂商系统上可能和系统界面不一致；已在 MIUI / Android 16 上出现同一窗口系统界面 33 分钟、`totalTimeInForeground` 返回 93 分钟的偏差。
- `InstalledAppRepository` 里允许用 `queryAndAggregateUsageStats` 做应用选择列表的最近使用排序和补充包名发现；它不能反向进入首页、限额、积分、归档、统计主口径。
- `queryAndAggregateUsageStats` 看似官方，但受厂商 bucket、后台播放、跨日裁剪、多 Activity、悬浮窗/画中画策略影响，不适合作为 Tiny Vow 的统一前台用量口径。
- 前台 session 口径更符合 Tiny Vow 的业务语义：用户实际看到 App 在前台多久、能按自定义日分割点精确裁剪、首页/阻断/归档/统计内部一致。
- 事件 session 仍有厂商边界：部分系统可能延迟 `PAUSED` / `STOPPED`，画中画、悬浮窗、后台播放是否计入也可能和系统设置页不同。当前约定是只统计前台 Activity session，不统计纯后台播放。
- `openCount`、`sessionCount`、`longestSessionMillis`、`nightUsageMillis`、小时热力图本来就依赖事件流；修改时要保持它们和主使用时长的 session 裁剪逻辑一致。
- 如果将来为了兼容某厂商新增 fallback 或双口径诊断，必须先用真机对比系统设置页、`totalTimeInForeground`、session 聚合三组数据，不要凭直觉切换主口径。

### 积分、奖励与加时包

- DataStore 中的 `userPoints` 是当前余额，`todayPoints` 是今日展示值。
- `PointLedgerEntity` 记录积分来源和快照，用于解释积分变化。
- `PointsRepository` 负责常规积分入账，包括使用时长积分、目标奖励、手动调整等。
- `AppLimitAccessibilityService` 在前台 App 切换和定时 ticker 中结算 `ENCOURAGE` 分组积分。
- 鼓励组目标奖励通过 `lastBonusAt` 控制每天最多发放一次。
- 奖励与兑换逻辑在 `AppLimitRepository`。购买时先检查奖励有效性、每日限制、库存、积分余额；购买成功写兑换历史、积分 ledger，并进入 `RewardInventoryEntity`。
- 当前内置奖励类型是 `TIME_ADD`、`PERIOD_PASS`、`EMERGENCY_UNLOCK`、`STREAK_SHIELD`、`DOUBLE_POINTS_DAY`；`CUSTOM` 只用于用户自定义兑换。
- `TIME_ADD`、`PERIOD_PASS`、`DOUBLE_POINTS_DAY` 从库存页使用并写 `ActiveRewardEffectEntity`；`TIME_ADD` / `PERIOD_PASS` 只能用于 `CONTROL` 分组，双倍积分用于 `ENCOURAGE` 分组。
- `PERIOD_PASS` 的产品语义是“本周期免超额提醒”：跳过阻断/提醒，但用量和统计仍照常记录；文案不要写成“删除记录”“不计入统计”。
- `EMERGENCY_UNLOCK` 只能先购买进库存，再在阻断 overlay 消耗；消耗后创建短时 `ActiveRewardEffectEntity`，避免直接绕开积分/历史记录。
- `STREAK_SHIELD` 用于归档后待处理的断连胜项，待处理记录在 `StreakShieldPendingEntity`，处理结果和奖励使用记录要保留历史。
- 每日签到由 `DailyCheckInRepository` 写 `DailyCheckInEntity`，并把内置临时缓冲卡发到库存；不要绕过库存直接发一次性效果。
- 旧 `BonusTimeEntity` / `bonus_times` 仍用于兼容加时包和历史数据；新增效果优先走 `active_reward_effects`。
- 自定义奖励支持预设图标、导入文件、单 emoji；导入文件由 `RewardIconStorage` 管理，替换或删除时要清理不再引用的文件。
- 内置奖励通过 `builtinKey` 做本地化，数据库旧标题只作兜底；自定义奖励标题和描述是用户数据，不自动翻译。

### 成就体系

- 成就是 6 个 `requirement.type` × 5 个等级的内置体系：`points`、`redeem_points`、`control_days`、`control_streak`、`encourage_days`、`encourage_streak`。
- 内置成就名称和条件由字符串资源 + `AppLimitRepository.syncAchievementDefinitions()` 同步；数据库里的旧标题/描述只作兜底。
- `points` 表示 `point_ledger` 中所有正向 `delta_points` 的累计值，不是当前积分余额。
- `redeem_points` 表示 `PointLedgerEntryType.REWARD_SPEND` 的累计消费积分。
- `control_days` / `encourage_days` 按“当天至少有 1 个对应类型分组完成”计 1 天，不按完成分组数量累加。
- `control_streak` / `encourage_streak` 从最新已归档日期向前连续统计；今天未归档，不参与 streak。
- 成就达标天数和连续天数基于每日归档，不用实时 UsageStats 临时计算。
- 修改成就定义时必须同步英文/中文资源、种子定义、进度计算、UI 进度传参和测试。

### 统计、归档与历史快照

- `DailyArchiveRepository.ensureArchivesUpToYesterday()` 从归档起始日补齐到昨天；`archiveDate(date)` 只归档已完成日期，不归档今天。
- 每日归档读取当天 UsageStats 前台 session 用量、打开次数、session，并构建 `DailyArchiveEntity`、`DailyGroupArchiveEntity`、`DailyAppArchiveEntity`。
- 归档保存的是历史事实。分组名、周期、限额、加时、成员 App、App 标签等都作为当时快照展示。
- 刷新旧日期归档时，如果当天已有分组和 App 快照，应优先复用旧快照，避免后续分组编辑覆盖历史状态。
- 未分组但当天使用超过最小阈值的 App 会作为 ungrouped 快照归档，用于完整设备使用回顾。
- 周/月/年统计基于归档窗口聚合，并对同一 package 的跨分组快照去重。
- 实时阻断和统计达标是两套语义：阻断页在 `CONTROL` 分组超过有效限额时立即弹出；统计归档允许 5 分钟裕度，超过有效限额 5 分钟以内仍按完成处理。
- `blockEventCount` 独立记录阻断发生次数，即使统计上仍处于 5 分钟裕度内，也可以看到当天发生过阻断。

### 特殊应用双源口径

- 当前只支持 `WEREAD`，包名固定为 `com.tencent.weread`。
- `special_app_usage_snapshots` 同时保存两类日粒度历史：
  - `usageMillis` + `readingBucketAvailable`：微信读书 API 返回的阅读时长桶。
  - `phoneUsageMillis` + `phoneCollectedAt`：本机 UsageStats 前台时长。
- `SpecialAppConfigEntity.usagePreference` 决定有效时长：
  - `READING_FIRST`：优先用阅读时长；当天无阅读桶时回退到本机前台时长。
  - `PHONE_FIRST`：优先用本机前台时长；如果当天/历史缺手机快照，再回退到阅读时长。
- 当天口径和历史口径不要拆成两套规则：
  - 当天如果有效来源是手机时长，应直接读取最新 UsageStats，不能只依赖较早同步时写入的手机快照。
  - 历史已完成日期使用 `special_app_usage_snapshots` 中已保存的日快照。
- `MergedUsageRepository` 是唯一替换入口：
  - `GroupType.CONTROL` 只在 `enabledForControl` 时替换。
  - `GroupType.ENCOURAGE` 只在 `enabledForEncourage` 时替换。
  - `groupType = null` 的设备总量也要走统一有效口径，不能简单把 `CONTROL` 和 `ENCOURAGE` 两张 map 直接覆盖合并。
  - 时长替换要求该 provider 至少有一次成功同步；首次成功同步前仍以本机 UsageStats 为主，避免把空快照当成真实历史。
- 微信读书详细行为数据不完整：
  - 替换的只有日粒度使用/阅读时长。
  - `openCount`、`sessionCount`、`longestSessionMillis`、`nightUsageMillis`、小时热力图仍保留本机 UsageStats 结果。
- 鼓励积分要跟有效来源保持一致：
  - `READING_FIRST` 时，微信读书鼓励积分改为同步后按有效时长增量入账，避免与无障碍实时积分重复。
  - `READING_FIRST` 在首次成功同步前也不要先发无障碍实时积分；否则同一天切到远端口径后会重复。
  - `PHONE_FIRST` 时，继续走无障碍实时手机前台积分，不要额外发同步积分。
- 归档、统计、成就、兑换的影响边界：
  - `DailyArchiveEntity.controlUsageMillis` / `encourageUsageMillis` 分别按对应组类型的有效口径计算。
  - `DailyArchiveEntity.totalUsageMillis`、周/月/年总览、Top Apps、趋势等设备总量要按 `groupType = null` 的统一有效口径去重。
  - 成就里的 `control_days` / `encourage_days` / streak 仍只依赖归档结果，不直接看实时快照。
  - 兑换和双倍积分判断依赖当前分组有效时长；如果改双源口径，需同步检查 `AppLimitRepository` 和 `AppLimitAccessibilityService`。

### 媒体播放补充口径

- 媒体应用补充口径由 `MediaAppPlaybackRepository` 和 `MediaAppPlaybackListenerService` 维护，适用于用户手动启用的播客、音乐、有声书等 App。
- 通知监听权限只用于读取启用 App 的通知可见性和 MediaSession 播放状态，不读取通知正文做业务数据，不扩展到无关 App。
- `media_app_playback_days` 保存当天累计可信后台播放时长、非可信断连空档和当前播放状态；`media_app_playback_segments` 保存可信播放区间，用于和 UsageStats 前台 session 合并去重。
- 可信补记窗口由 `MediaAppPlaybackAccountant.TRUSTED_RECONNECT_WINDOW_MILLIS` 控制，当前是 30 分钟；监听断开过久不能直接把整段后台时间算成可信播放。
- `MergedUsageRepository` 会按 override 顺序把微信读书特殊口径和媒体播放补充口径合入同一张使用量 map。新增补充口径时必须考虑不同 override 之间的覆盖顺序和设备总量去重。
- 媒体播放只补充“可被 MediaSession/通知状态确认的后台播放时间”；不要把所有后台进程存活时间、通知常驻时间或缓存时间算作使用。

### 步数、离线专注与签到

- 步数由 `StepTrackingRepository` 通过 `TYPE_STEP_COUNTER` 记录，Android 10+ 需要 `ACTIVITY_RECOGNITION` 权限；没有传感器或权限时 UI 必须显示可理解的不可用状态。
- 步数鼓励积分通过 `step_point_credits` 防重复入账，按业务日记录；不要按每次传感器事件直接累计积分。
- 离线专注由 `OfflineFocusRepository`、`OfflineFocusTimerService`、`offline_focus_categories` 和 `offline_focus_sessions` 维护，分类支持内置图标和用户导入图标。
- 离线专注普通模式遇到非白名单 App 会暂停，严格模式遇到非白名单 App 会放弃；这条链路依赖无障碍服务回调当前前台 App。
- 离线专注前台服务通知必须保持本地化，并使用低打扰通知渠道；修改服务时要验证开始、暂停、恢复、提前完成、放弃和锁屏策略。
- 专注分类使用软归档/删除语义，历史 session 的分类名、图标、颜色、积分倍率快照是历史事实，不要迁移覆盖。
- 每日签到是本地行为记录；签到发放的缓冲卡应进入库存并保留 `daily_checkins` 历史。

### 超我模式

- 超我模式不是新的限额或阻断规则，只是关键操作前的本机密码保护层；不要让它替代 Usage Access / Accessibility 主链路。
- 密码和恢复答案只保存 salted hash，不上传、不写入 Room 明文字段、不出现在隐私报告明细中。
- 默认允许窗口是 06:00-10:00；PRO 用户可自定义窗口，但不支持跨午夜窗口。
- 进入超我模式后短会话默认 5 分钟有效；切后台、超时、离开允许窗口都应退出。
- 受保护操作集中通过 `GuardedAction` 和统一 guard 入口处理，新增“修改规则/绕过限额”类能力时先判断是否需要接入超我模式。
- `ProtectionEventEntity` 记录关键设置变更和被拦截行为，属于本地历史；文案使用 key + args，本地化显示时不要把历史 target label 当内置文案翻译。

## 目录和模块

- `app/src/main/java/com/rrrrz/tinyvow/MainActivity.kt`：应用入口、主题、语言 context 注入。
- `app/src/main/java/com/rrrrz/tinyvow/TinyVowApplication.kt`：Application 初始化。
- `data/db`：Room entity、dao、migration，当前数据库版本是 `29`，schema 导出到 `app/schemas`。
- `data/repository`：分组、奖励、积分、每日归档等主要业务仓库。
- `data/activation`：国内版本地激活码、到期解析、时间回拨检测和激活 DataStore。
- `data/billing`：Google Play Billing、Noop 仓库和统一 `ProEntitlementState`。
- `data/media`：媒体应用后台播放监听、可信播放区间合并和 Usage override。
- `data/steps`：本机步数传感器读取和步数积分。
- `data/supermode`：超我模式密码、窗口、短会话和受保护操作模型。
- `domain/limit`：限额评估策略，尤其是 `GroupLimitEnforcer`。
- `service/block`：无障碍软阻断服务和 overlay。
- `service/media`：通知监听服务和 MediaSession 播放状态监听。
- `service/offline`：离线专注前台计时服务。
- `data/usage`：UsageStats 权限与用量读取。
- `data/settings/ManagedAppPreferences.kt`：DataStore 偏好，包含积分、主题、应用页面文字大小、权限引导状态、语言等。
- `data/notification`、`data/reminder`：通知渠道和提醒 Worker。
- `ui/home`：主导航、首页、统计、我的、实验室、主题、支持页面。
- `ui/rewards`：成就和兑换。
- `ui/theme`：Compose theme、主题模型、分享图主题。
- `i18n`：`AppLanguage` 与 `AppText`。
- `docs`：上架前优化、发布流程、市场文案、隐私、账号删除、Google Play 发布检查。
- `design`：图标、报告视觉参考。

## 开发原则

- 小步改动。优先修明确问题或补完整闭环，不要为了“架构更好看”大范围重构。
- 面向项目维护者的文档默认使用中文，包括 `AGENTS.md`、`CHANGELOG.md` 和 `docs/*.md`。只有外部平台要求、API 原文、代码标识符或用户明确要求时才使用英文。
- 用户要求“准备上架”“发布前优化”“提审检查”“整理发布文档”时，先读 `docs/prelaunch-optimization.md`、`docs/release.md`、`docs/google-play-release-checklist.md` 和 `docs/market-listing-copy.md`，再按必须完成/建议优化/可延后分类处理。
- 不随意拆分或重写 `AppLimitAccessibilityService`、`GroupLimitEnforcer`、阻断 overlay 时序；这些是核心链路。
- UI 改动跟随现有 Compose + Material 3 风格，避免引入新的设计体系。
- 业务逻辑优先写在 repository/domain 层；Compose 里避免堆积复杂计算和数据库细节。
- 数据库和 UsageStats 等耗时操作放到 IO/后台协程；不要在 UI 主线程新增阻塞调用。
- 现有同步 DAO/缓存主要服务于无障碍服务热路径，改动时先确认不会拖慢前台切换和阻断响应。
- 保持 local-first：用户分组、奖励、自定义主题、使用历史、积分、阻断记录默认都是用户本地数据。
- 工作区可能有未提交改动。只改本次任务相关文件，不回滚用户已有修改。

## 多语言

- 用户可见文案不要写死在 Kotlin/Compose/Canvas/通知/Dialog/Snackbar/contentDescription 里，统一走 Android 字符串资源。
- 英文默认文案改 `app/src/main/res/values/app_texts.xml`。
- 简体中文文案改 `app/src/main/res/values-zh-rCN/app_texts.xml`。
- `strings.xml` 只放 `app_name` 这类基础字符串，不要放普通 UI 文案。
- 新增文案必须同时加英文和简体中文，两边 key 要一致。
- key 用语义化模块前缀，例如 `home_...`、`stats_...`、`group_...`、`redeem_...`、`achievement_...`、`theme_...`、`billing_...`、`support_...`。
- 不要使用 `auto_*`、hash key 或临时 key。
- 中英文格式占位符必须一致，例如 `%1$s`、`%2$d` 的数量和顺序要匹配。
- Compose 中可用 `stringResource(R.string.xxx)` 或 `AppText.t("xxx")`；通知、Worker、无障碍服务、非 Compose 代码使用 `AppText.t(...)` 前要确保语言已设置。
- 语言偏好使用 `AppLanguage.SYSTEM / ZH_CN / EN`，由 `ManagedAppPreferences.selectedAppLanguage` 保存。
- `MainActivity` 通过 `AppText.localizedContext(...)` 注入 `LocalContext`；后台/服务读取文案前使用当前应用语言的 context 或调用 `AppText.setLanguage(...)`。
- 新增语言时同步更新：资源目录、设置页语言选项、`app/src/main/res/xml/locales_config.xml`、语言测试。

## 翻译边界

- 必须翻译应用自带文案：标题、按钮、说明、提示、错误、空状态、图表标签、统计解读、通知、权限说明、帮助、隐私导出、内置成就、内置奖励、主题预设、分享图文字。
- 不翻译用户数据：用户创建的分组名、奖励名/描述、自定义主题名、设备返回的 App 名称、历史快照里的用户或外部来源名称。
- 内置内容用稳定 key 本地化显示；数据库里的旧中文或旧英文字段只作兜底。
- 内置奖励使用 `builtinKey` 映射 `reward_..._title` / `reward_..._description`。
- ledger、历史记录、归档中的 snapshot 字段是历史事实，不要在迁移或显示时强行覆盖为当前语言。

## Room 和数据迁移

- 数据库定义在 `AppDatabase`，当前 `version = 29`，`exportSchema = true`。
- 改 entity/dao/schema 时必须：
  - 增加数据库版本号。
  - 添加从上一版本到新版本的 `Migration`。
  - 把 migration 加到 `Room.databaseBuilder(...).addMigrations(...)`。
  - 更新/提交 `app/schemas/com.rrrrz.tinyvow.data.db.AppDatabase/<version>.json`。
  - 尽量保留旧数据，尤其是用户分组、积分、兑换历史、归档、主题相关字段。
- 软删除语义已经用于分组和分组-App 关系，不要改成物理删除，除非明确处理所有历史引用。
- `PointLedgerEntity` 用于解释积分变化，新增积分来源时同步考虑 ledger entry type、message key、参数 JSON 和本地化文案。
- 奖励库存、主动使用效果、连胜保护待处理、奖励使用历史分别在 `reward_inventory`、`active_reward_effects`、`streak_shield_pending`、`reward_use_history`。步数、媒体播放、离线专注、超我模式和每日签到分别涉及 `step_days` / `step_point_credits`、`media_app_*`、`offline_focus_*`、`protection_events`、`daily_checkins`。新增表或字段时同步隐私导出清单和迁移测试。
- 每日归档是统计页稳定数据源；改归档字段时同步更新 DAO、聚合逻辑、统计 UI 和测试。
- 实时阻断和统计达标是两套语义：阻断页应在 `CONTROL` 分组一超过有效限额就弹出；统计/归档里允许 5 分钟裕度，超过 5 分钟才记为超额或未完成。
- 加时包要并入有效限额；按日分组到当天结束，按周分组覆盖兑换日起 7 天窗口，按月分组到当月结束。
- 统计和归档展示设备总用量时要按 package 去重；分组明细可以保留“同一个 App 属于多个分组”的分组视角。历史归档中的分组名、周期、限额、加时和成员 App 是当时快照，刷新旧归档时优先保留已有快照，避免被后续分组修改覆盖。

## 权限、服务和阻断约束

- 使用情况访问、无障碍、通知、电池白名单、自启动都是敏感权限/系统设置入口。打开系统设置前要保留清晰 disclosure 和用户确认。
- 核心权限是 Usage Access + Accessibility；通知、电池白名单、自启动是可靠性增强，不要把它们写成硬性前置条件。
- 无障碍服务只用于检测前台应用和显示超额阻断页，不要扩展到读取用户输入、采集屏幕内容或无关自动化。
- Accessibility disclosure 必须先由用户确认。未确认时服务会移除 overlay，不执行阻断。
- 通知权限用于本地限额提醒和阻断提示。拒绝通知不影响分组、统计和阻断主链路。
- 通知监听权限只服务于用户启用的媒体应用后台播放补充统计；没有开启时，媒体补充口径应显示不可用或仅保留已有可信历史，不影响 UsageStats 主链路。
- `ACTIVITY_RECOGNITION` 只用于本机步数；拒绝后不应影响 App 使用统计、阻断、奖励库存等主功能。
- 离线专注计时使用前台服务和本地通知；通知被拒绝时要注意 Android 版本差异，不要把离线专注状态写成已完成。
- `AppLimitAccessibilityService` 必须避免阻断 Tiny Vow 自身包名。
- overlay 使用 `TYPE_ACCESSIBILITY_OVERLAY`，文案、按钮、contentDescription 仍要走资源或 `AppText`。
- 热路径里已有 debounce、conflated channel、短缓存。改动阻断判断、缓存 TTL、overlay 移除时机前，要手动验证快速切换、返回桌面、返回 Tiny Vow、重复打开超额 app。
- 国产厂商后台限制差异大；自启动和电池白名单引导要保持“建议/可跳过”的语气。

## Compose 和 UI 约束

- UI 改动前先读根目录 `design.md`，再读 `ui/theme` 和目标页面源码；新增页面或大改页面必须遵守其中的设计方向、token、组件和页面级规则。
- 现有主导航页：`HOME`、`STATS`、`REWARDS`、`ME`，二级页包含 `LABORATORY`、`HISTORY`、`THEME`、`HELP_FEEDBACK`、`CONTACT_US`。
- 不新增落地页式 marketing 页面；直接完善实际功能页面。
- 权限页和敏感权限 disclosure 要表达用途、当前状态、如何开启，不要只放按钮。
- 统计页展示必须能处理无权限、无数据、未归档、归档样本不足等空状态。
- 分享图和 Canvas 文案也算 UI 文案，必须本地化。
- 用户可编辑内容不要自动翻译；只对内置 key 做本地化。
- 自定义主题名是用户数据；预设主题名是内置文案。

## 订阅、账号和隐私

- Play Billing 产品当前围绕 `tinyvow_pro`。错误提示要解释 Play Console 产品/基础计划未配置、设备不支持、连接断开、支付 pending 等常见状态。
- `PlayBillingSubscriptionRepository` 查询订阅商品、发起购买、查询购买状态并 acknowledge 已购买订单。
- Google Play 版购买前要重新查询最新 `ProductDetails`；App 启动、回到前台、手动恢复购买都要刷新订阅状态。
- Google Play 购买可传入哈希后的本地 `userId` 作为 `obfuscatedAccountId`，但 Google 登录不作为购买前置条件。
- `SubscriptionEntitlementResolver` 将购买快照解析为 `FREE`、`ACTIVE`、`PENDING` 或 `UNAVAILABLE`。
- `PURCHASED tinyvow_pro` 才解锁 PRO；`PENDING` 只显示待完成，不解锁高级权益；非 `tinyvow_pro` 购买不能影响权益。
- Play Console 必须创建并激活 `tinyvow_pro` 订阅和至少一个 base plan；本地代码不能替代商品、价格、国家地区、测试轨道和 license tester 配置。
- 本地调试构建不一定能完成真实购买，不要把它当成代码必然错误。
- `LocalDataManager` 当前同时维护两条本地数据导出链路：
  - `exportPrivacyReport()` 生成 `tinyvow-local-data-<timestamp>.json`，内容是表级摘要和本地存储摘要，不含完整表内容，输出到 `cache/share`。
  - `exportLocalBackup()` 生成 `tinyvow-local-backup-<timestamp>.zip`，这是当前“我的 > 本地数据管理”实际暴露给用户的可恢复备份入口。
- 可恢复备份 zip 当前包含：
  - Room 数据库主文件和 `-wal` / `-shm`。
  - DataStore 文件：`managed_app_preferences`、`auth_preferences`、`activation_preferences`。
  - `files/reward_icons` 下的导入奖励图标文件。
  - `files/focus_icons` 下的导入离线专注分类图标文件。
- 可恢复备份不导出微信读书 Key 明文；`managed_app_preferences` 里可能带上旧的加密 Key 字段，但导入恢复不会恢复 Android Keystore 里的 `tinyvow_weread_api_key` 密钥材料。`backup_manifest.json` 会用 `requiresWeReadKeyReentry` 标记恢复后是否需要提示用户去特殊应用里重新填写 WeRead Key。
- 导入备份会校验 `backup_manifest.json` 中的 `format`、`schemaVersion`、`packageName` 和 `appVersionCode`，恢复数据库 / DataStore / 奖励图标后立即重启应用；涉及这条链路的改动要手动验证“导入成功提示 + 重启 + 重启后状态恢复”。
- 隐私导出表清单要覆盖当前 Room 本地数据，包括奖励库存、主动效果、连胜保护待处理、奖励使用历史和保护事件；新增本地表时同步 `LocalDataManager.localDataTables`。
- 国内版账号以 `LocalAuthRepository.ensureLocalSession()` 生成的本地用户 ID 为匿名安装标识；后端会话还使用 `BackendSubscriptionStore` 生成并保存的随机设备凭据，服务端只保存其加 pepper 哈希，不能退回到仅凭安装标识即可认领账号。
- 国内版后端固定使用 `https://api.tinyvow.rorolo.com`。`TVB1` 激活码通过 `ChinaSubscriptionRepository` 创建匿名服务端会话并兑换；已有 `TVA1` 本地签名激活码继续由 `LocalActivationSubscriptionRepository` 离线验证，两类权益取有效期更长者，不能破坏旧激活记录。
- 匿名后端只接收本地用户 ID 作为安装标识、随机设备凭据，以及 Android 平台、设备厂商/型号、应用版本和渠道；支付时另处理商品、订单、金额、渠道交易号和必要的去标识化回调审计字段。不得上传使用记录、分组、限额、积分、步数、专注、微信读书或媒体播放等 local-first 数据。会话、设备凭据与缓存保存在 `backend_subscription_preferences`，需要纳入手动备份、本地清理和隐私报告摘要。
- 国内版支付宝购买必须由后端创建签名订单；App 的同步支付结果只触发服务端订单轮询，不能直接解锁 PRO。后端收到支付宝异步通知后必须完成验签、app ID、seller ID、商户订单号和金额核对，再以订单 ID 幂等发放权益。应用私钥只保存在服务器环境变量中。
- 国内版创建订单后要把待确认订单 ID 保存到 `backend_subscription_preferences`；App 启动、回前台和“恢复购买”都应继续向后端查单。支付成功或订单进入 `CLOSED / FAILED / REFUNDED` 后清除待确认记录，网络中断或确认超时不能提前清除。
- 删除已创建的匿名服务端账号必须先调用 `DELETE /v1/me`，成功后再清本机会话；失败时保留本地状态并提示用户重试，不能只清本地却显示服务端账号已删除。
- 账号删除、隐私说明相关改动要同步检查 `docs/account-delete.html`、`docs/privacy.html` 和应用内支持页文案。
- 导出/清理本地数据时必须覆盖 Room 数据、`managed_app_preferences`、国内 `auth_preferences` / `activation_preferences`、奖励导入图标、专注分类导入图标、分享缓存和 WeRead Key 的 Keystore 材料等用户能感知的本地状态；不要误删应用安装外部数据。
- 不要把 Android Auto Backup / device transfer 当成本地备份恢复方案：Manifest 当前虽然开启了 `allowBackup`，但 `backup_rules.xml` / `data_extraction_rules.xml` 已排除数据库、DataStore、`share` 缓存、导入奖励图标、导入专注图标和待恢复微信读书 Key 文件，核心恢复仍应以 `LocalDataManager` 的手动备份导入导出为准。

## 测试和检查

常规代码改动后优先运行日常国内 debug 单测：

```powershell
.\gradlew.bat testChinaDebugUnitTest
```

涉及资源、Manifest、Room schema、混淆、构建配置、权限、通知、服务或发布相关改动后运行日常国内 debug 构建：

```powershell
.\gradlew.bat assembleDefaultDebug
```

多语言相关改动必须覆盖：

- 中英文 key 一致。
- 占位符一致。
- 英文默认资源无中文。
- 无生成型 `auto_[hash]` key。
- 主文案在 `app_texts.xml`。

成就逻辑改动至少运行：

```powershell
.\gradlew.bat testChinaDebugUnitTest
.\gradlew.bat assembleDefaultDebug
```

## 真实设备、安装与验收边界

### 绝对禁止卸载和清除真机数据

- 这是最高优先级的设备数据红线：Tiny Vow 已发生过两次开发/验收过程中应用被卸载、真实本地数据丢失的事故。后续 Agent 不得再次以任何理由自动卸载应用或清除应用数据。
- 用户说“安装”“重新安装”“更新”“覆盖安装”“修复安装”“继续”“完成验收”，都只授权保留应用数据的原地升级，**绝不等于授权卸载、清数据或先卸载再安装**。只有用户明确说出“允许卸载并接受本地数据永久丢失”时，才可以另行评估；不得通过模糊措辞推断授权。
- 严禁直接或通过脚本、Gradle task、IDE/测试工具间接执行 `adb uninstall`、`pm uninstall`、`pm clear`、`uninstallAll`、`uninstall*` task、清理应用数据、删除 `/data/data/com.rrrrz.tinyvow*`，以及厂商安装器的“卸载原应用后安装”。运行安装相关脚本前必须先检查其中没有这些操作。
- 安装只能优先使用经过包名和签名核对的 `adb install -r -t <apk>` 原地升级。若出现签名不一致、版本降级、安装冲突、安装器要求卸载、`INSTALL_FAILED_UPDATE_INCOMPATIBLE` 或其他无法原地升级的错误，立即停止并向用户报告；**不得为了让安装成功而尝试卸载、清数据或换用会覆盖真实包数据的流程**。
- 安装前后都要只读记录目标包是否存在、版本、签名和数据目录状态。若安装前目标包意外不存在，应先明确告诉用户应用已经被卸载、数据可能已丢失，再根据用户明确指示进行全新安装；不得把全新安装描述成“保留数据的覆盖安装”。

- 用户正在实际使用、保存真实数据的手机默认视为生产设备。实际使用验收由用户本人完成；Agent 默认只做到代码检查、单元测试、构建验证和必要的只读诊断，不代替用户操作真实业务流程。
- 用户明确要求安装，或已经同意当前任务包含安装时，可以把 APK 安装到真实手机；但安装必须是保留原应用和全部数据的安全升级。“继续”“做到彻底完成”“部署生产环境”等任务描述不构成卸载、清除数据、真机 UI 自动化、修改输入法或系统设置的授权。
- 用户明确表示“验收我自己来”或同等意思后，严禁再执行真机自动化、界面点击或实际使用验收；只报告构建结果和需要用户手动检查的项目。
- 在任何真实设备上安装 APK 前，必须先完成并确认以下事项，缺一项就停止安装：
  - 用户已明确同意本次安装；设备中有真实数据时，优先确认已有可验证的 Tiny Vow 手动备份。
  - 记录设备上原应用的包名、版本、签名证书摘要和首次安装时间。
  - 对比待安装 APK 的 application ID 和签名证书，确认它能安全升级现有安装。
  - 如果签名不同、应用无法原地升级，或安装器要求先卸载原应用，立即停止安装；改用相同签名的升级包或独立测试包名，不得继续确认卸载重装。
- 严禁为了安装新版执行 `adb uninstall`、`pm uninstall`、`pm clear`、先卸载后安装，或确认厂商安装器提供的“卸载原应用后安装”选项。不得把 `adb install -r` 当成数据一定安全的保证；签名不兼容、厂商安装器确认流程或渠道包差异都可能造成卸载重装和应用私有数据永久丢失。
- 自动化测试必须优先使用模拟器、专用测试设备或独立测试包名，并使用一次性测试账号。不得在用户真实使用的包名和真实数据上执行注册、删除账号、清数据、数据库迁移破坏性验证等流程。
- Android Auto Backup 不能替代安装前手动备份。Tiny Vow 的核心数据恢复以应用内 `tinyvow-local-backup-*.zip` 导出并验证可读为准。
- 安装完成后不代替用户执行实际使用验收。向用户提供构建结果和人工验收清单，由用户本人检查真实业务流程；只有用户另行明确要求时，才做非破坏性的启动或只读诊断。

仅当用户明确要求安装验证、已经确认备份与签名兼容，并授权操作当前设备时，才运行：

```powershell
.\gradlew.bat installDefaultDebug
```

仅当用户同时明确授权启动应用时，才使用确定性启动命令：

```powershell
adb shell am start -n com.rrrrz.tinyvow.cn/com.rrrrz.tinyvow.MainActivity
```

不要用 `adb shell monkey -p com.rrrrz.tinyvow.cn 1` 作为日常启动命令；部分设备上 Monkey 事件可能影响系统方向锁定/自动旋转状态。

修改后默认进行编译测试，不默认安装应用，也不执行自动实机测试。修改较大或风险较高时提醒用户自行进行人工真机验证，尤其是：

- 首次权限引导。
- Usage Access / Accessibility 开关后返回刷新。
- 超额阻断 overlay。
- ENCOURAGE 积分累计。
- 奖励兑换和积分扣减。
- 统计页归档/空状态。
- 媒体播放补充统计和通知监听权限开关后返回刷新。
- 步数权限、无传感器设备和步数积分去重。
- 离线专注开始/暂停/恢复/完成/放弃，普通/严格模式和锁屏策略。
- 超我模式允许时间、短会话超时、切后台退出和受保护操作拦截。
- 主题和语言切换后重启。
- 息屏、后台、厂商自启动/电池限制场景。

## 常用命令

```powershell
.\gradlew.bat testChinaDebugUnitTest
.\gradlew.bat assembleDefaultDebug
.\gradlew.bat connectedDebugAndroidTest
.\tools\package-china-release.ps1
.\tools\package-release-artifacts.ps1
```

如果终端读取中文文档时出现乱码，可显式按 UTF-8 读取：

```powershell
Get-Content -Raw -Encoding UTF8 AGENTS.md
```

如果只是文档改动，可以不跑 Gradle，但最终说明里要明确“未运行测试，因为只改文档”。

## 版本管理和发布维护

- 应用版本统一由根目录 `gradle.properties` 维护：
  - `TINYVOW_VERSION_NAME`：基础版本名，必须是 SemVer 三段式，例如 `1.0.0`。
  - `TINYVOW_VERSION_CODE`：Android 构建号，必须是正整数。
- 正式版本以`#1.0.0`类似的格式保存在标签里，打包正式版本前，汇总上个版本截止现在的修改内容，填写在changlog里。
- 不要在 `TINYVOW_VERSION_NAME` 里写渠道后缀。`china` flavor 通过 `versionNameSuffix = "-cn"` 自动显示为 `1.0.0-cn`；`googlePlay` flavor 使用基础版本名，例如 `1.0.0`。
- Google Play 和国内版默认共享同一个基础 `versionName` 和 `versionCode`。除非用户明确要求渠道独立发版，不要拆成两套版本号。
- 每次对外发布 APK/AAB 前必须手动递增 `TINYVOW_VERSION_CODE`。仅本机调试构建可以不递增。
- 每次调整对外版本时同步更新 `CHANGELOG.md`，记录用户可见变化；版本发布流程和 tag 规则同步维护 `docs/release.md`。
- 上架前优化和人工验证清单维护在 `docs/prelaunch-optimization.md`；发现新的发布风险、审核材料要求或人工验证步骤时，优先补到该文档，而不是散落在对话记录里。
- 应用内版本展示应继续从 `BuildConfig.VERSION_NAME` / `BuildConfig.VERSION_CODE` 读取，不要把版本号写入 DataStore、Room 或普通字符串资源。
- 当用户要求“打包 release 包”“生成发布包”或“给我可归档产物”时，不要只停留在 `app/build/outputs`：
  - 需要把最终发布产物复制到根目录 `dist/`。
  - 文件名统一为 `tinyvow-{channel}-{versionName}-vc{versionCode}-release.{apk|aab}`。
  - 当前渠道名固定使用 `cn` 和 `googleplay`。
  - 国内版 APK 默认使用 `tools/package-china-release.ps1`；同时整理两个渠道时使用 `tools/package-release-artifacts.ps1`。
- 如果本次只是直接执行了 Gradle 任务，也要在结束前把产物补复制到 `dist/` 并按上述规则改名，方便归档，不要只给 `app/build/outputs/...` 原始路径。
- release 签名相关注意事项：
  - 国内版签名配置默认读取 `release-signing/tinyvow-cn-release.properties`，字段必须包含 `storeFile`、`storePassword`、`keyAlias`、`keyPassword`。
  - `release-signing/` 下的 keystore、properties 和其他密码材料只保留在本机，不提交到仓库，不复制到 `dist/`，也不要写进文档示例。
  - `tools/package-china-release.ps1` 会校验签名配置、keystore 存在性，并在出包后执行 `apksigner verify`；改动发布流程时不要绕过这些检查。
  - 如果某个渠道已经对外发布过，不要随意更换 release keystore；需要换签名时先确认已有安装包升级链路和外部平台要求。
  - Google Play AAB 归档也按 release 流程产出，但上传到 Play 后仍以 Play Console / Play App Signing 的最终要求为准。
- 涉及版本、Gradle、渠道或发布文档变更时，至少运行：

```powershell
.\gradlew.bat testChinaDebugUnitTest
.\gradlew.bat assembleDefaultDebug
```

- 需要安装验证时再运行 `.\gradlew.bat installDefaultDebug`。如果没有连接设备导致安装失败，最终说明里要明确失败原因。
- 发布前建议确认生成值：
  - `chinaDebug/chinaRelease` 应显示 `基础版本-cn`，例如 `1.0.0-cn`。
  - `googlePlayDebug/googlePlayRelease` 应显示基础版本，例如 `1.0.0`。
  - 两个渠道的 `VERSION_CODE` 应一致，除非用户明确要求独立版本线。

## 应用商店宣传图定稿基线

- 宣传图当前唯一有效总规范是 `design/appstore/CURRENT_GENERATION_SPEC.md`；开始商店图、网站宣传图或截图本地化任务前必须先读该文件。
- 当前已经定稿并应继续使用的是 S01–S07。旧版本只保留作设计过程记录，不得因为文件名较短、修改时间较新或 README 旧段落而回退使用。
- 当前中文定稿构图与成图：
  - S01 首页总览 v7：`design/appstore/compositions/S01_home_zh-CN_v7.html`；`design/appstore/exports/cn-stores/cn-store_01_home_zh-CN_1080x1920_v7.png`。
  - S02 约定与投入 v2：`design/appstore/compositions/S02_control_encourage_zh-CN_v2.html`；`design/appstore/exports/cn-stores/cn-store_02_control_encourage_zh-CN_1080x1920_v2.png`。
  - S03 温和阻断 v1：`design/appstore/compositions/S03_block_overlay_zh-CN_v1.html`；`design/appstore/exports/cn-stores/cn-store_03_block_overlay_zh-CN_1080x1920_v1.png`。
  - S04 离线专注 v4：`design/appstore/compositions/S04_offline_focus_zh-CN_v4.html`；`design/appstore/exports/cn-stores/cn-store_04_offline_focus_zh-CN_1080x1920_v4.png`。
  - S05 手机使用追踪 v3：`design/appstore/compositions/S05_phone_usage_tracking_zh-CN_v3.html`；`design/appstore/exports/cn-stores/cn-store_05_phone_usage_tracking_zh-CN_1080x1920_v3.png`。
  - S06 奖励与成就 v3：`design/appstore/compositions/S06_rewards_achievements_zh-CN_v3.html`；`design/appstore/exports/cn-stores/cn-store_06_rewards_achievements_zh-CN_1080x1920_v3.png`。
  - S07 产品特性 v2：`design/appstore/compositions/S07_product_characteristics_zh-CN_v2.html`；`design/appstore/exports/cn-stores/cn-store_07_product_characteristics_zh-CN_1080x1920_v2.png`。
- 当前英文定稿构图与 Google Play 成图：
  - S01 v7：`design/appstore/compositions/S01_home_en_v7.html`；`design/appstore/exports/google-play/google-play_01_home_en_1080x1920_v7.png`。
  - S02 v2：`design/appstore/compositions/S02_control_encourage_en_v2.html`；`design/appstore/exports/google-play/google-play_02_control_encourage_en_1080x1920_v2.png`。
  - S03 v1：`design/appstore/compositions/S03_block_overlay_en_v1.html`；`design/appstore/exports/google-play/google-play_03_block_overlay_en_1080x1920_v1.png`。
  - S04 v4：`design/appstore/compositions/S04_offline_focus_en_v4.html`；`design/appstore/exports/google-play/google-play_04_offline_focus_en_1080x1920_v4.png`。
  - S05 v3：`design/appstore/compositions/S05_phone_usage_tracking_en_v3.html`；`design/appstore/exports/google-play/google-play_05_phone_usage_tracking_en_1080x1920_v3.png`。
  - S06 v3：`design/appstore/compositions/S06_rewards_achievements_en_v3.html`；`design/appstore/exports/google-play/google-play_06_rewards_achievements_en_1080x1920_v3.png`。
  - S07 v2：`design/appstore/compositions/S07_product_characteristics_en_v2.html`；`design/appstore/exports/google-play/google-play_07_product_characteristics_en_1080x1920_v2.png`。
- 英文真机截图优先使用 `design/appstore/screenshots/raw/en/`；宣传图中需要把中文演示分组名、专注分类名改成英文时，只生成 `design/appstore/screenshots/edited/en/` 营销副本，并通过 `design/appstore/scripts/localize_en_screenshots.ps1` 复现。不要修改应用真实数据，也不要覆盖 `raw/en` 原图。
- 所有定稿都必须保留 `1080 × 1920` 成图和 `360 × 640` 手机预览。验收以手机预览可读为准：大标题、核心标签和报表自有标题必须能辨认，禁止重新加入顶部眉题、底部小字或密集说明。
- S05 固定使用关键数据、周报行为散点、周报积分轨迹、周五日报时光刻痕、周报时光刻痕五张卡；行为图谱不显示九宫格。S06 固定上三行兑换、下三行成就，六行等高等距。S07 固定四张等规格卡片。
- 不宣传步数投入或步数积分；当前不使用月报、年报。不要生成虚假 App UI，不要用旧稿替换上述定稿。

## 优先阅读文件

处理相关任务前优先看这些文件：

- `app/src/main/java/com/rrrrz/tinyvow/service/block/AppLimitAccessibilityService.kt`
- `app/src/main/java/com/rrrrz/tinyvow/domain/limit/GroupLimitEnforcer.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/repository/AppLimitRepository.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/repository/DailyArchiveRepository.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/repository/PointsRepository.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/special/SpecialAppUsageRepository.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/media/MediaAppPlaybackRepository.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/media/MediaAppPlaybackAccountant.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/repository/OfflineFocusRepository.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/steps/StepTrackingRepository.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/supermode/SuperModeController.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/privacy/LocalDataManager.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/settings/ManagedAppPreferences.kt`
- `app/src/main/java/com/rrrrz/tinyvow/i18n/AppText.kt`
- `design.md`
- `app/src/main/java/com/rrrrz/tinyvow/ui/home/HomeScreen.kt`
- `app/src/main/java/com/rrrrz/tinyvow/ui/home/StatsScreen.kt`
- `app/src/main/res/values/app_texts.xml`
- `app/src/main/res/values-zh-rCN/app_texts.xml`
- `app/src/main/res/xml/accessibility_service_config.xml`
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`

## 渠道包规则

- 项目拆分为 `googlePlay` 和 `china` 两个 product flavor。
- 渠道版本规则见“版本管理和发布维护”；不要在渠道逻辑里另起一套版本号。
- Google Play 版使用 `com.rrrrz.tinyvow`，只上传 `:app:bundleGooglePlayRelease` 到 Play Console。
- 国内版使用 `com.rrrrz.tinyvow.cn`，用于国内测试和后续激活码能力，可与 Google Play 版同时安装，但本地数据不互通。
- 支付宝开放平台的国内版 Android 应用包名填写 `com.rrrrz.tinyvow.cn`；当前国内发布证书的应用签名 MD5 是 `43af31f58bb1e26592a255c749fd4821`（32 位小写、无冒号）。这是 APK 证书摘要，不是支付宝 RSA2 应用公钥。
- 国内版支付宝应用 `AppID` 是 `2021006172687041`，收款账号 ID / seller ID 是 `2088212325078165`。两者属于公开配置；应用私钥和支付宝公钥正文仍只保存在服务器环境文件，不写入仓库。
- 用户于 2026-07-15 明确要求开启支付宝生产真实下单，后端当前已启用生产支付；App 内购买会进入真实支付宝收银台，只有明确的调试入口才可绕过，不能把生产购买流程改成客户端直接发放权益。
- release 归档产物默认放到根目录 `dist/`，不要散落在别的目录；产物名必须保留渠道、`versionName` 和 `versionCode` 方便后续归档。
- 国内版启动时会确保存在本地账号，并在“我的 > Tiny Vow Pro”显示用户 ID 复制与激活码输入入口。
- 日常 debug 默认使用国内版：优先运行 `:app:assembleChinaDebug` 或 `:app:installChinaDebug`。
- 为方便记忆，也可以运行 `:app:assembleDefaultDebug` 或 `:app:installDefaultDebug`，这两个 alias 当前指向国内版。
- 安装后如需启动国内 debug 包，使用 `adb shell am start -n com.rrrrz.tinyvow.cn/com.rrrrz.tinyvow.MainActivity`，不要用 Monkey 启动。
- 不要把国内激活码、国内支付或外部购买入口显示在 `googlePlay` flavor 中。
- 不要在 `china` flavor 中触发 Google 登录、Play Billing 购买、恢复购买或管理订阅流程。
- 国内版 `SubscriptionRepository` 使用 `ChinaSubscriptionRepository`，合并服务端商品/支付宝支付/`TVB1` 服务端激活码和旧 `TVA1` 本地激活权益；Google Play 版使用 `PlayBillingSubscriptionRepository`；其他禁用渠道才用 `NoopSubscriptionRepository`。
- `:app:assembleDebug` 会构建多个 debug flavor，速度更慢，不作为日常默认命令。
- 国内版本地 Pro 激活使用 `tools/activation/ActivationCodeTool.java` 生成激活码；私钥文件 `tools/activation/private_key.pkcs8` 只留在本机，不能提交。
- 激活码绑定国内版本地 `userId`，支持自定义天数；无后端时只能防普通伪造和简单时间回拨，不能替代服务器时间。
- 国内版服务端商品价以分为单位：月付 `90`、年付 `990`、永久 `2990`，App 展示必须读取 `/v1/products`，不要在购买流程另写一套价格。

## PRO 权益规则

- 所有渠道统一通过 `ProEntitlementState.isProActive` 判断会员状态，不要在 UI 里直接分散判断 Play Billing 或国内激活码细节。
- PRO 额度统一写在 `data/pro/ProFeatureGate.kt` 和 `ProLimits`，不要把数字散落在 Compose 页面里。
- 免费额度：`CONTROL` 分组最多 2 个，`ENCOURAGE` 分组最多 2 个；每组 App 最多 3 个；自定义兑换最多 3 个；自定义主题最多 1 个。
- PRO 额度：分组数量不限，自定义兑换不限；每组 App 最多 10 个；自定义主题最多 10 个。
- “不限”只用于 UI 和判断，可用 `Int.MAX_VALUE` 表达，不写入数据库。
- 免费用户已有超额数据不删除、不迁移、不裁剪；但超出免费额度的分组、兑换、主题只可展示，不允许继续编辑。保存时也要校验 App 数量不能超过当前权益上限。
- 触达限制时显示统一 `ProUpsellDialog`，不要直接跳转购买页或激活页。Google Play 版按钮文案指向订阅，国内版按钮文案指向激活码解锁。
- 自定义兑换额度只统计 `builtinKey == null` 的用户自定义兑换；内置兑换不占额度。
- 会员主题 ID 使用 `member_` 前缀。非 PRO 用户可以预览会员主题，但不能选择；如果当前选择的会员主题在权益失效后仍被保存，应自动回退到默认主题。
- 战报免费用户只保留今日和基础日报；趋势、热力图、周/月/年统计、行为分析、对比分析和完整历史窗口属于 PRO 高级战报，免费状态显示锁定遮罩和权益说明入口。
- 实验室里的调试 Pro 入口只允许在 `BuildConfig.DEBUG` 下显示和生效，用于所有 debug 渠道快速增加本地 Pro 时长；不要让该入口进入 release 包或替代真实 Play Billing/国内激活记录。
- 后续新增高级能力时，优先扩展 `ProFeatureGate` 和单元测试，再接入具体 UI 或仓库逻辑。
