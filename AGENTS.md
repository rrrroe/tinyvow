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

## 目录和模块

- `app/src/main/java/com/rrrrz/tinyvow/MainActivity.kt`：应用入口、主题、语言 context 注入。
- `app/src/main/java/com/rrrrz/tinyvow/TinyVowApplication.kt`：Application 初始化。
- `data/db`：Room entity、dao、migration，当前数据库版本是 `15`，schema 导出到 `app/schemas`。
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

- 数据库定义在 `AppDatabase`，当前 `version = 15`，`exportSchema = true`。
- 改 entity/dao/schema 时必须：
  - 增加数据库版本号。
  - 添加从上一版本到新版本的 `Migration`。
  - 把 migration 加到 `Room.databaseBuilder(...).addMigrations(...)`。
  - 更新/提交 `app/schemas/com.rrrrz.tinyvow.data.db.AppDatabase/<version>.json`。
  - 尽量保留旧数据，尤其是用户分组、积分、兑换历史、归档、主题相关字段。
- 软删除语义已经用于分组和分组-App 关系，不要改成物理删除，除非明确处理所有历史引用。
- `PointLedgerEntity` 用于解释积分变化，新增积分来源时同步考虑 ledger entry type、message key、参数 JSON 和本地化文案。
- 每日归档是统计页稳定数据源；改归档字段时同步更新 DAO、聚合逻辑、统计 UI 和测试。

## 权限、服务和阻断约束

- 使用情况访问、无障碍、通知、电池白名单、自启动都是敏感权限/系统设置入口。打开系统设置前要保留清晰 disclosure 和用户确认。
- 核心权限是 Usage Access + Accessibility；通知、电池白名单、自启动是可靠性增强，不要把它们写成硬性前置条件。
- 无障碍服务只用于检测前台应用和显示超额阻断页，不要扩展到读取用户输入、采集屏幕内容或无关自动化。
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
- 本地调试构建不一定能完成真实购买，不要把它当成代码必然错误。
- 账号删除、隐私说明相关改动要同步检查 `docs/account-delete.html`、`docs/privacy.html` 和应用内支持页文案。
- 导出/清理本地数据时必须覆盖 Room 数据、DataStore 偏好和用户能感知的本地状态；不要误删应用安装外部数据。

## 测试和检查

常规代码改动后优先运行：

```powershell
.\gradlew.bat testDebugUnitTest
```

涉及资源、Manifest、Room schema、混淆、构建配置、权限、通知、服务或发布相关改动后运行：

```powershell
.\gradlew.bat assembleDebug
```

多语言相关改动必须覆盖：

- 中英文 key 一致。
- 占位符一致。
- 英文默认资源无中文。
- 无生成型 `auto_[hash]` key。
- 主文案在 `app_texts.xml`。

高风险功能还要按 `SMOKE_TEST_CHECKLIST.md` 在真机手动验证，尤其是：

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
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest
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
