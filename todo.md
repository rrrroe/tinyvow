# Tiny Vow 开发 TODO

最后整理日期：2026-07-14

## 当前基线

- 当前版本：`1.2.0`，`versionCode = 8`。
- 国内正式产物：`dist/tinyvow-cn-1.2.0-vc8-release.apk`。
- 稳定主线已经覆盖：分组限额、鼓励积分、无障碍软阻断、奖励与库存、成就、统计与分享、离线专注、每日签到、超我模式、特殊应用双源时长、媒体后台播放补充、步数、主题、多语言、本地备份和双渠道权益。
- `CONTROL` / `ENCOURAGE`、UsageStats 前台 session、每日归档、积分 ledger 和 local-first 数据边界继续作为稳定基线，不做无明确收益的大改。

## P0：1.2.0 发布候选版收口

### 自动验证

- [x] `./gradlew.bat testChinaDebugUnitTest`（2026-07-14）
- [x] `./gradlew.bat assembleDefaultDebug`（2026-07-14）
- [x] 检查中英文 `app_texts.xml` key 与占位符一致（2026-07-14）。
- [x] 检查 `git diff --check` 和最终工作区改动范围（2026-07-14）。

### 1.2.0 新增链路回归

- [ ] 当前日报能直接使用今天的实时快照。
- [ ] 当前周/月/年报只合并一次今天的数据，不与已有日归档重复。
- [ ] 今天的 Top Apps、打开次数、会话、时间刻痕和积分轨迹保持同一时间窗口口径。
- [ ] 无限离线专注支持开始、暂停、恢复、提前完成和结算；积分按实际有效专注时长计算。
- [ ] 有限专注的 80% 提前完成阈值和强制完成口径不受无限模式影响。
- [ ] Health Connect 实验室诊断能区分不可用、无权限、无数据和小米来源。
- [ ] 首页与统计页 `min` / `PT` 单位、动画和分享图显示一致。

### 核心真机冒烟

- [ ] Usage Access disclosure、授权跳转和返回刷新。
- [ ] Accessibility disclosure、超额阻断 overlay、离开超额 App 和返回 Tiny Vow 后移除 overlay。
- [ ] CONTROL 超额、加时包、周期通行证和紧急解锁边界。
- [ ] ENCOURAGE 前台使用积分、目标奖励和积分 ledger。
- [ ] 奖励购买、库存使用、积分扣减和历史记录。
- [ ] 无权限、无数据、实时今日、历史归档和分享图场景。
- [ ] 主题、语言和应用重启后的状态保留。
- [ ] 本地备份导出、导入、重启恢复、清理数据和删除账号。

## P1：发布与渠道

### 国内版

- [x] 已归档 1.2.0 国内签名 APK。
- [ ] 在干净真机安装 release APK 并完成 P0 冒烟。
- [ ] 验证本地激活、过期、时间回拨和本地用户 ID 复制。

### Google Play

- [ ] 需要提审时生成 `tinyvow-googleplay-1.2.0-vc8-release.aab`。
- [ ] 在测试轨道验证 `tinyvow_pro` 购买、恢复、pending、取消和重新订阅。
- [ ] 确认 Data safety、Accessibility API 声明、应用内 disclosure 和隐私政策一致。
- [ ] 确认隐私政策与账号删除页面的生产 URL 可访问。
- [ ] 新品牌域名正式部署前，不替换当前生产链接。

## P1：测试缺口

- [ ] 为 `GroupLimitEnforcer` 的多分组短板效应和缓存失效补更高层测试。
- [ ] 为无限专注的暂停区间、跨业务日和每日积分上限补 repository 测试。
- [ ] 为实时今日快照与历史归档重叠场景补聚合回归测试。
- [ ] 为备份导入补跨渠道包名、新版本、超大 zip 和路径异常 zip 测试。
- [ ] 数据库版本再次升级时，必须同步 migration、schema JSON 和 `AppDatabaseMigrationTest`。

## P2：低风险维护

- [x] 已把首页底部导航从 `HomeScreen.kt` 提取到独立纯展示组件（2026-07-14）。
- [ ] 继续从 `HomeScreen.kt`、`StatsScreen.kt` 提取低耦合纯展示组件；每次只迁移一个完整区块。
- [ ] 优先迁移底部导航、图表绘制、对话框等无数据库依赖组件。
- [ ] 不拆分 `AppLimitAccessibilityService`、`GroupLimitEnforcer` 或 overlay 时序，除非存在明确 bug 或测试阻碍。
- [ ] 删除确认无引用的旧兼容 UI 前，先验证历史数据和升级路径。

## P2：体验与发布材料

- [ ] 至少覆盖一台 Android 13+ 和一台国产 ROM 的权限、通知、保活与返回刷新。
- [ ] 回归微信读书首次同步前、`READING_FIRST`、`PHONE_FIRST` 和备份恢复后重新填写 Key。
- [ ] 准备 Accessibility API 中英文说明和短录屏。
- [ ] 按 `docs/market-listing-copy.md` 准备首页、分组、投入、战报、奖励、主题、隐私和 Pro 截图。

## 可延后

- 大范围 UI 自动化测试。
- 自有后端、跨设备同步和远端账号体系。
- 崩溃上报或分析 SDK；如需接入，先确认隐私、Data safety 和 local-first 边界。
- 为了架构美观而重写已经验证的阻断、归档或积分主链路。

## 每次改动后的收口

1. 只修改任务相关文件，不回滚工作区已有改动。
2. 代码、资源、Manifest、Room 或构建配置变化后，至少运行单测和默认 debug 构建。
3. 用户可见文案同步维护英文和简体中文资源。
4. 数据结构变化同步 migration、schema、隐私导出和迁移测试。
5. 发布前以 `docs/prelaunch-optimization.md`、`docs/release.md` 和对应渠道清单为准。
