# Tiny Vow Agent 规范

本文件是后续 Agent 在 `E:\Project\tinyvow` 工作时的项目说明和约束。修改代码前先读本文件，涉及具体模块时再读对应源码。

## 项目概况

- Tiny Vow 是一个本地优先的 Android 应用，用“约定 + 鼓励”管理手机使用。
- 当前是单模块项目：`:app`，包名和 namespace 都是 `com.rrrrz.tinyvow`。
- 技术栈：Kotlin、Jetpack Compose、Material 3、Room、DataStore Preferences、WorkManager、AccessibilityService、UsageStats、Play Billing、Credential Manager / Google ID。
- 构建配置：`compileSdk 36.1`、`targetSdk 36`、`minSdk 26`、Java 11、Kotlin `2.2.10`、AGP `9.1.0`。
- 入口：`MainActivity` 挂载 Compose UI，`TinyVowApplication` 初始化 `AppText`。
- 应用数据默认保存在本机；隐私导出/清理在 `data/privacy`，不要无意引入自动上传或远端依赖。

## 当前产品基线

把下面能力视为已经验证过的稳定主线，除非修复明确 bug，不要推倒重做：

- `CONTROL` 分组：按日/周/月限额统计使用量，超额后软阻断。
- `ENCOURAGE` 分组：按使用时长累计积分，并支持目标达成奖励。
- 使用情况访问权限：读取应用用量和使用周期统计。
- 无障碍服务：监听前台窗口变化，显示全屏阻断 overlay，并承担一部分积分结算。
- 奖励/兑换/成就：Room 持久化，积分通过 ledger 记录来源。
- 统计页：基于每日归档和当前 UsageStats 展示日报、趋势、热力图、分享图等。
- 外观主题：预设主题 + 自定义三色主题，DataStore 保存。
- 多语言：支持系统语言、简体中文、英文。
- 订阅：Play Billing 本地接入，Google Play 配置不完整时要有可理解的错误文案。

## 业务逻辑速览

### 启动与全局状态

- `TinyVowApplication` 调用 `AppText.attach(...)`，让服务、Worker、通知等非 Compose 代码也能读取应用文案。
- `MainActivity` 开启 edge-to-edge，创建通知渠道，监听主题和语言偏好，并挂载 `HomeRoute`。
- `ManagedAppPreferences` 通过 DataStore Preferences 保存积分、今日积分、主题、语言、权限 disclosure 状态、权限提示 dismissed 状态、旧单 App 限额兼容字段等全局状态。
- 主题通过 `resolveThemeSeed(...)` 选择预设或自定义三色主题；阻断 overlay 和统计分享图也要跟随当前主题。
- 语言通过 `AppText.localizedContext(...)` 注入 `LocalContext`，同时用 `AppText.setLanguage(...)` 更新全局文案上下文。

### 分组模型

- `AppGroupEntity` 是主模型，核心字段包括 `type`、`limitPeriod`、`limitMinutes`、`pointsPerMinute`、`lastBonusAt`、`sortOrder`。
- `GroupAppCrossRef` 表示分组与 App 包名关系。
- `CONTROL` 分组把 `limitMinutes` 视为限额；`ENCOURAGE` 分组把 `limitMinutes` 视为目标时长。
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

### 积分、奖励与加时包

- DataStore 中的 `userPoints` 是当前余额，`todayPoints` 是今日展示值。
- `PointLedgerEntity` 记录积分来源和快照，用于解释积分变化。
- `PointsRepository` 负责常规积分入账，包括使用时长积分、目标奖励、手动调整等。
- `AppLimitAccessibilityService` 在前台 App 切换和定时 ticker 中结算 `ENCOURAGE` 分组积分。
- 鼓励组目标奖励通过 `lastBonusAt` 控制每天最多发放一次。
- 奖励与兑换逻辑在 `AppLimitRepository`。兑换时先检查奖励有效性、库存、积分余额和目标分组。
- `TIME_PACK` 只能兑换给 `CONTROL` 分组，兑换成功后插入 `BonusTimeEntity`，同时写兑换历史和积分 ledger。
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
- 每日归档读取当天 UsageStats、打开次数、session，并构建 `DailyArchiveEntity`、`DailyGroupArchiveEntity`、`DailyAppArchiveEntity`。
- 归档保存的是历史事实。分组名、周期、限额、加时、成员 App、App 标签等都作为当时快照展示。
- 刷新旧日期归档时，如果当天已有分组和 App 快照，应优先复用旧快照，避免后续分组编辑覆盖历史状态。
- 未分组但当天使用超过最小阈值的 App 会作为 ungrouped 快照归档，用于完整设备使用回顾。
- 周/月/年统计基于归档窗口聚合，并对同一 package 的跨分组快照去重。
- 实时阻断和统计达标是两套语义：阻断页在 `CONTROL` 分组超过有效限额时立即弹出；统计归档允许 5 分钟裕度，超过有效限额 5 分钟以内仍按完成处理。
- `blockEventCount` 独立记录阻断发生次数，即使统计上仍处于 5 分钟裕度内，也可以看到当天发生过阻断。

## 目录和模块

- `app/src/main/java/com/rrrrz/tinyvow/MainActivity.kt`：应用入口、主题、语言 context 注入。
- `app/src/main/java/com/rrrrz/tinyvow/TinyVowApplication.kt`：Application 初始化。
- `data/db`：Room entity、dao、migration，当前数据库版本是 `18`，schema 导出到 `app/schemas`。
- `data/repository`：分组、奖励、积分、每日归档等主要业务仓库。
- `domain/limit`：限额评估策略，尤其是 `GroupLimitEnforcer`。
- `service/block`：无障碍软阻断服务和 overlay。
- `data/usage`：UsageStats 权限与用量读取。
- `data/settings/ManagedAppPreferences.kt`：DataStore 偏好，包含积分、主题、权限引导状态、语言等。
- `data/notification`、`data/reminder`：通知渠道和提醒 Worker。
- `ui/home`：主导航、首页、统计、我的、实验室、主题、支持页面。
- `ui/rewards`：成就和兑换。
- `ui/theme`：Compose theme、主题模型、分享图主题。
- `i18n`：`AppLanguage` 与 `AppText`。
- `docs`：隐私、账号删除、Google Play 发布检查。
- `design`：图标、报告视觉参考。

## 开发原则

- 小步改动。优先修明确问题或补完整闭环，不要为了“架构更好看”大范围重构。
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

- 数据库定义在 `AppDatabase`，当前 `version = 18`，`exportSchema = true`。
- 改 entity/dao/schema 时必须：
  - 增加数据库版本号。
  - 添加从上一版本到新版本的 `Migration`。
  - 把 migration 加到 `Room.databaseBuilder(...).addMigrations(...)`。
  - 更新/提交 `app/schemas/com.rrrrz.tinyvow.data.db.AppDatabase/<version>.json`。
  - 尽量保留旧数据，尤其是用户分组、积分、兑换历史、归档、主题相关字段。
- 软删除语义已经用于分组和分组-App 关系，不要改成物理删除，除非明确处理所有历史引用。
- `PointLedgerEntity` 用于解释积分变化，新增积分来源时同步考虑 ledger entry type、message key、参数 JSON 和本地化文案。
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
- `AppLimitAccessibilityService` 必须避免阻断 Tiny Vow 自身包名。
- overlay 使用 `TYPE_ACCESSIBILITY_OVERLAY`，文案、按钮、contentDescription 仍要走资源或 `AppText`。
- 热路径里已有 debounce、conflated channel、短缓存。改动阻断判断、缓存 TTL、overlay 移除时机前，要手动验证快速切换、返回桌面、返回 Tiny Vow、重复打开超额 app。
- 国产厂商后台限制差异大；自启动和电池白名单引导要保持“建议/可跳过”的语气。

## Compose 和 UI 约束

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
- `LocalDataManager.exportPrivacyReport()` 导出本地表级摘要到缓存分享目录。
- 账号删除、隐私说明相关改动要同步检查 `docs/account-delete.html`、`docs/privacy.html` 和应用内支持页文案。
- 导出/清理本地数据时必须覆盖 Room 数据、DataStore 偏好和用户能感知的本地状态；不要误删应用安装外部数据。

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

涉及安装验证时再运行：

```powershell
.\gradlew.bat installDefaultDebug
```

修改后进行编译测试，并安装应用，不需要自动实机测试，修改较大或风险较高时提醒人工真机验证，尤其是：

- 首次权限引导。
- Usage Access / Accessibility 开关后返回刷新。
- 超额阻断 overlay。
- ENCOURAGE 积分累计。
- 奖励兑换和积分扣减。
- 统计页归档/空状态。
- 主题和语言切换后重启。
- 息屏、后台、厂商自启动/电池限制场景。

## 常用命令

```powershell
.\gradlew.bat testChinaDebugUnitTest
.\gradlew.bat assembleDefaultDebug
.\gradlew.bat connectedDebugAndroidTest
```

Windows PowerShell 读取中文文档前先设置 UTF-8 输出，避免终端显示乱码：

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Get-Content -Raw -Encoding UTF8 AGENTS.md
```

如果只是文档改动，可以不跑 Gradle，但最终说明里要明确“未运行测试，因为只改文档”。

## 优先阅读文件

处理相关任务前优先看这些文件：

- `app/src/main/java/com/rrrrz/tinyvow/service/block/AppLimitAccessibilityService.kt`
- `app/src/main/java/com/rrrrz/tinyvow/domain/limit/GroupLimitEnforcer.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/repository/AppLimitRepository.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/repository/DailyArchiveRepository.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/repository/PointsRepository.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/settings/ManagedAppPreferences.kt`
- `app/src/main/java/com/rrrrz/tinyvow/i18n/AppText.kt`
- `app/src/main/java/com/rrrrz/tinyvow/ui/home/HomeScreen.kt`
- `app/src/main/java/com/rrrrz/tinyvow/ui/home/StatsScreen.kt`
- `app/src/main/res/values/app_texts.xml`
- `app/src/main/res/values-zh-rCN/app_texts.xml`
- `app/src/main/res/xml/accessibility_service_config.xml`

## 渠道包规则

- 项目拆分为 `googlePlay` 和 `china` 两个 product flavor。
- Google Play 版使用 `com.rrrrz.tinyvow`，只上传 `:app:bundleGooglePlayRelease` 到 Play Console。
- 国内版使用 `com.rrrrz.tinyvow.cn`，用于国内测试和后续激活码能力，可与 Google Play 版同时安装，但本地数据不互通。
- 日常 debug 默认使用国内版：优先运行 `:app:assembleChinaDebug` 或 `:app:installChinaDebug`。
- 为方便记忆，也可以运行 `:app:assembleDefaultDebug` 或 `:app:installDefaultDebug`，这两个 alias 当前指向国内版。
- 不要把国内激活码、国内支付或外部购买入口显示在 `googlePlay` flavor 中。
- 不要在 `china` flavor 中触发 Google 登录、Play Billing 购买、恢复购买或管理订阅流程。
- `:app:assembleDebug` 会构建多个 debug flavor，速度更慢，不作为日常默认命令。
- 国内版本地 Pro 激活使用 `tools/activation/ActivationCodeTool.java` 生成激活码；私钥文件 `tools/activation/private_key.pkcs8` 只留在本机，不能提交。
- 激活码绑定国内版本地 `userId`，支持自定义天数；无后端时只能防普通伪造和简单时间回拨，不能替代服务器时间。

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
