# Tiny Vow App 逻辑文档

本文档用于梳理 Tiny Vow 当前实现逻辑，面向后续开发者和 Agent。修改代码前先确认本文档、`AGENTS.md` 和相关源码是否一致；如果行为发生变化，需要同步更新本文档。

## 1. 产品目标与本地优先原则

Tiny Vow 是本地优先的手机使用管理应用，用“约定 + 鼓励”管理应用使用：

- `CONTROL` 分组负责限制。用户为一组 App 设置日/周/月限额，超过有效限额后由无障碍服务显示全屏软阻断 overlay。
- `ENCOURAGE` 分组负责鼓励。用户为一组 App 设置目标时长和积分倍率，使用越多获得越多积分，并可在达成目标时获得奖励。
- 用户分组、App 绑定、奖励、兑换记录、积分 ledger、主题、语言、统计归档、阻断记录默认保存在本机。
- 第一版没有 Tiny Vow 后端自动同步。Google 登录、Play Billing 等第三方 SDK 只用于对应能力，不改变本地优先的数据原则。

## 2. 启动与全局状态

应用入口是 `MainActivity`，Application 入口是 `TinyVowApplication`。

- `TinyVowApplication` 初始化 `AppText`，让非 Compose 代码也能拿到应用文案。
- `MainActivity` 开启 edge-to-edge，创建通知渠道，并挂载 `HomeRoute`。
- `ManagedAppPreferences` 通过 DataStore Preferences 保存全局偏好，包括积分、今日积分、主题、语言、权限 disclosure 状态、权限提示 dismissed 状态、旧单 App 限额兼容字段等。
- `MainActivity` 监听主题和语言偏好：
  - 主题通过 `resolveThemeSeed(...)` 选择预设或自定义三色主题。
  - 语言通过 `AppText.localizedContext(...)` 注入 `LocalContext`，同时调用 `AppText.setLanguage(...)` 更新全局文案上下文。
- `TinyVowTheme` 提供 Material 3 主题和 Tiny Vow 自定义色彩 token，统计分享图也复用报告主题色。

## 3. 权限链路

核心权限是 Usage Access 和 Accessibility。通知、电池白名单、自启动是可靠性增强，不是硬性前置条件。

- Usage Access 用于读取系统应用使用时长、打开次数、使用 session 和当前周期统计。
- Accessibility 只用于监听前台窗口变化、识别当前前台 App、显示 Tiny Vow 自己的阻断 overlay，并承担一部分积分结算。
- Accessibility disclosure 必须先由用户确认。未确认时服务会移除 overlay，不执行阻断。
- 通知权限用于本地限额提醒和阻断提示。拒绝通知不影响分组、统计和阻断主链路。
- 电池白名单和自启动用于降低系统清理后台任务、提醒或服务的概率，文案应保持“建议/可跳过”的语气。

敏感权限入口必须先说明用途、当前状态和开启方式，再引导用户进入系统设置。

## 4. 分组模型

分组是当前产品主模型，Room 表包括 `app_groups` 和 `group_app_cross_ref`。

- `AppGroupEntity` 表示分组，核心字段包括：
  - `type`: `CONTROL` 或 `ENCOURAGE`
  - `limitPeriod`: `DAILY`、`WEEKLY`、`MONTHLY`
  - `limitMinutes`: 限制组的限额，鼓励组的目标时长
  - `pointsPerMinute`: 鼓励组每分钟积分倍率
  - `lastBonusAt`: 鼓励组目标奖励的最近发放时间
  - `sortOrder`: 同类型分组排序
- `GroupAppCrossRef` 表示分组与 App 包名的关系。
- 分组和关联关系使用软删除，历史和归档可能仍引用旧分组、旧名称、旧 App 关系，不应物理删除后强行抹掉历史。
- 同一个 App 可以属于多个分组：
  - 分组明细中按每个分组分别展示该 App 对分组的贡献。
  - 设备总用量、周/月/年总览和 Top Apps 聚合应按 package 去重，避免同一 App 被多个分组重复计算。

## 5. 实时阻断链路

实时阻断链路由 `UsageStatsUsageRepository`、`GroupLimitEnforcer` 和 `AppLimitAccessibilityService` 组成。

1. `AppLimitAccessibilityService` 监听 `TYPE_WINDOW_STATE_CHANGED` 和 `TYPE_WINDOWS_CHANGED`。
2. 如果当前包名是 Tiny Vow 自身，只结算上一个 App 的积分，不进行限额检查。
3. 服务对高频窗口事件做短防抖，并把最新事件送入 conflated channel。
4. `GroupLimitEnforcer.evaluate(packageName)` 查询该包所属的 `CONTROL` 分组，按分组周期统计组内所有 App 的总使用时长。
5. 有效限额 = 分组基础限额 + 当前生效加时包。
6. 实时阻断语义是“超过有效限额即阻断”，不使用统计裕度。只要 `exceededMillis > 0`，即可返回超额结果。
7. 服务记录 `BlockEventEntity`，再在主线程显示 `TYPE_ACCESSIBILITY_OVERLAY`。
8. 切换到非超额 App 或回到 Tiny Vow 时移除 overlay。

服务内有两类缓存：

- 分组配置缓存，减少前台切换时同步 DAO 查询。
- 使用量缓存，减少短时间内重复读取 UsageStats。

修改阻断判断、缓存 TTL、overlay 移除时机前，必须真机验证快速切换、回桌面、回 Tiny Vow、重复打开超额 App。

## 6. 积分、奖励与加时包

积分变化由 DataStore 余额和 Room ledger 共同表达。

- DataStore 中的 `userPoints` 是当前余额，`todayPoints` 是今日展示值。
- `PointLedgerEntity` 记录积分来源和快照，用于解释积分变化。
- `PointsRepository` 负责常规积分入账，包括使用时长积分、目标奖励、手动调整等。
- `AppLimitAccessibilityService` 在前台 App 切换和定时 ticker 中结算 ENCOURAGE 分组积分。
- 鼓励组目标奖励通过 `lastBonusAt` 控制每天最多发放一次。

奖励与兑换逻辑在 `AppLimitRepository`：

- 内置奖励通过 `builtinKey` 做本地化，数据库旧标题只作兜底。
- 自定义奖励标题和描述是用户数据，不自动翻译。
- 兑换时先检查奖励是否有效、库存、积分余额和目标分组。
- `TIME_PACK` 只能兑换给 `CONTROL` 分组，兑换成功后插入 `BonusTimeEntity`，同时写兑换历史和积分 ledger。
- 加时包按目标分组周期决定到期时间：
  - `DAILY`: 兑换当天 23:59:59.999 失效。
  - `WEEKLY`: 兑换日起第 7 天 23:59:59.999 失效。
  - `MONTHLY`: 兑换所在自然月最后一天 23:59:59.999 失效。
- 当前生效加时包会被实时阻断和统计归档同时计入有效限额。

## 7. 统计、归档与历史快照

统计页主要依赖每日归档。当天实时数据和历史窗口数据的口径不同，维护时要明确区分。

- `DailyArchiveRepository.ensureArchivesUpToYesterday()` 从归档起始日补齐到昨天。
- `archiveDate(date)` 只归档已完成日期，不归档今天。
- 每日归档读取当天 UsageStats、打开次数、session，并构建：
  - `DailyArchiveEntity`: 当天总览
  - `DailyGroupArchiveEntity`: 分组快照
  - `DailyAppArchiveEntity`: App 快照
- 归档保存的是历史事实。分组名、周期、限额、加时、成员 App、App 标签等都作为当时快照展示。
- 刷新旧日期归档时，如果当天已有分组和 App 快照，应优先复用旧快照，避免后续分组编辑覆盖历史状态。
- 未分组但当天使用超过最小阈值的 App 会作为 ungrouped 快照归档，用于完整设备使用回顾。
- 周/月/年统计基于归档窗口聚合，并对同一 package 的跨分组快照去重。

实时阻断和统计达标是两套语义：

- 实时阻断：`CONTROL` 分组超过有效限额即弹出阻断页。
- 统计归档：允许 5 分钟裕度；超过有效限额 5 分钟以内仍按完成处理，超过 5 分钟才计为超额/未完成。
- `blockEventCount` 独立记录阻断发生次数，即使统计上仍处于 5 分钟裕度内，也可以看到当天发生过阻断。

## 8. 多语言、主题与分享图

多语言由资源文件和 `AppText` 共同支持。

- 英文默认文案在 `app/src/main/res/values/app_texts.xml`。
- 简体中文文案在 `app/src/main/res/values-zh-rCN/app_texts.xml`。
- 普通 UI 文案不应写进 `strings.xml`，`strings.xml` 只放 `app_name` 等基础字符串。
- Compose 中可用 `stringResource(...)` 或 `AppText.t(...)`。
- 服务、Worker、通知等非 Compose 代码使用 `AppText.t(...)` 前要确保语言已设置。
- 用户数据不翻译，包括用户分组名、自定义奖励名、自定义主题名、设备返回 App 名称、历史快照字段。

主题逻辑：

- 预设主题和自定义主题都用 `ThemeSeed` 表达。
- 主题包含控制色、鼓励色和基础色。
- `ManagedAppPreferences` 保存已选主题和自定义主题 JSON。
- 阻断 overlay 监听主题偏好，使用当前主题生成原生 View 调色板。
- 统计分享图使用报告主题色，Canvas 文案同样必须本地化。

## 9. 订阅、账号与隐私

订阅通过 Google Play Billing 本地接入，产品 ID 是 `tinyvow_pro`。

- `PlayBillingSubscriptionRepository` 查询订阅商品、发起购买、查询购买状态并 acknowledge 已购买订单。
- `SubscriptionEntitlementResolver` 将购买快照解析为 `FREE`、`ACTIVE`、`PENDING` 或 `UNAVAILABLE`。
- Play Console 产品或基础计划未配置、设备不支持、连接断开、pending 等状态要给出可理解错误。
- 本地调试构建不一定能完成真实购买，不能直接视为代码错误。

隐私和账号：

- `LocalDataManager.exportPrivacyReport()` 导出本地表级摘要到缓存分享目录。
- `clearLocalData()` 清理 Room 表和 DataStore 偏好。
- 账号删除、隐私说明改动需要同步检查 `docs/account-delete.html`、`docs/privacy.html` 和应用内支持页文案。
- 不应误删应用安装目录之外的数据。

## 10. 当前边界与维护规则

当前稳定主线是：分组管理、Usage Access 统计、Accessibility 软阻断、ENCOURAGE 积分、奖励兑换、每日归档、统计页、主题语言、订阅和隐私本地处理。

维护规则：

- 不重写 `AppLimitAccessibilityService`、`GroupLimitEnforcer` 或 overlay 时序，除非有明确 bug。
- 业务逻辑优先放 repository/domain 层，Compose 中避免堆数据库细节和复杂计算。
- 数据库实体或 schema 变化必须升级 Room version、补 migration、更新 schema JSON，并保留用户数据。
- 热路径中同步 DAO 和 UsageStats 读取要谨慎，避免拖慢前台切换和阻断响应。
- 新增积分来源必须同步考虑 ledger entry type、message key、参数 JSON 和本地化展示。
- 新增用户可见文案必须同时加英文和简体中文资源。
- 高风险改动后必须运行 `testDebugUnitTest` 和 `assembleDebug`，并按冒烟清单做真机验证。
