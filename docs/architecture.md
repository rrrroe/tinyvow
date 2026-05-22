# Tiny Vow 架构说明

本文记录当前实现边界，供后续人工维护和 Agent 修改代码前参考。核心流程变更后要同步更新本文。

## 核心边界

- Tiny Vow 是本地优先应用。用户分组、奖励、自定义主题、使用归档、积分、阻断记录和激活状态默认保存在本机，除非某个功能明确说明会联网或上传。
- `AppLimitAccessibilityService` 负责前台应用检测、软阻断 overlay 展示，并承担一部分 `ENCOURAGE` 积分结算。不要把它扩展成读取输入、截屏、自动点击或无关自动化能力。
- `GroupLimitEnforcer` 负责实时 `CONTROL` 限额判断。实时阻断语义是只要超过有效限额就立即阻断；5 分钟裕度只用于归档和统计达标。
- `DailyArchiveRepository` 负责历史事实。刷新旧日期归档时应复用当天已有的分组和 App 快照，避免后续分组编辑改写历史。
- `AppLimitRepository` 是当前分组、奖励、库存、奖励效果和成就的主要门面。新增业务规则应优先在 Compose 外可测试。

## 数据和迁移规则

- 当前 Room 数据库版本是 `19`，导出的 schema 位于 `app/schemas/com.rrrrz.tinyvow.data.db.AppDatabase`。
- 修改 entity、DAO 或 schema 时必须提升数据库版本，添加上一版本到新版本的 `Migration`，注册到 `Room.databaseBuilder(...).addMigrations(...)`，更新导出的 schema JSON，并补迁移测试。
- 默认保留用户数据：分组、关联关系、积分 ledger、兑换历史、归档、自定义主题和激活状态都不应无说明地丢弃或重写。
- 分组和分组-App 关系使用软删除。不要改成物理删除，除非已经处理所有历史引用。

## 性能热路径

- 前台应用切换是低延迟路径。数据库读取要尽量批量化，UsageStats 读取要缓存或聚合，overlay 操作只在真正显示或移除时回到主线程。
- UsageStats 查询开销较高。优先一次查询一个周期，再在内存里按 package 汇总，避免对每个 package 循环调用 `queryAndAggregateUsageStats`。
- Compose 页面不要堆复杂业务计算。可复用的战报、奖励和权益计算应放到 repository/domain helper，并保持可单测。

## UI 和本地化

- Compose、Canvas、通知、服务、Dialog、Snackbar、contentDescription 中的用户可见文案必须走 `app_texts.xml` 或 `AppText`。
- 用户数据不翻译：自定义分组名、自定义奖励名/描述、自定义主题名、安装应用名称和历史快照都按用户输入或系统返回展示。
- 内置内容使用稳定 key，例如奖励 `builtinKey`、成就 requirement key、主题 ID，通过本地化资源展示，不改写数据库历史文本。

## 渠道和权益规则

- `googlePlay` 使用包名 `com.rrrrz.tinyvow`，包含 Google 登录、Play Billing 和 Play 订阅管理。
- `china` 使用包名 `com.rrrrz.tinyvow.cn`，走本地激活，不应触发 Google 登录或 Play Billing 流程。
- UI 应通过 `ProEntitlementState.isProActive` 和 `ProFeatureGate` 判断权益，不要直接散落判断 Play Billing 或本地激活细节。
- 免费用户已有超额数据不删除、不迁移、不裁剪；但超出当前权益上限的分组、兑换、主题只能展示，不能继续编辑或保存。

## 手动验证清单

- 首次权限披露：Usage Access、Accessibility、通知、电池白名单、自启动。
- 快速切换前台应用、返回桌面、返回 Tiny Vow、打开超额 App、兑换加时后重新打开 App。
- `ENCOURAGE` 积分从前台切换和定时 ticker 两条路径结算。
- 奖励购买、库存使用、积分 ledger、目标分组限制。
- 每日归档刷新、统计空状态、PRO 锁定战报区、分享图生成。
- 主题和语言切换，包括重启后表现，以及阻断 overlay / 分享图主题一致性。
