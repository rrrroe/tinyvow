# TinyVow 全面代码审查与整体优化方案

**审查日期**:2026-07-26
**审查基线**:master 最新提交 `16226b5`(2026-07-23 "feat: add promotional video assets")+ 工作区最新的配置/资源文件
**审查范围**:全部 Kotlin 源码(221 个文件,约 3.5MB,覆盖率 ~97%)、Room schema v35、AndroidManifest、Gradle 配置、资源目录、备份规则
**审查方式**:四路并行深度审查(UI 显示与交互逻辑 / 数据库与数据层 / 性能·动画·负载 / 架构·业务逻辑·安全),共产出 **110+ 条经代码逐条核实的发现**,以下为汇总与整体方案。

> 说明:有 21 个小文件(合计约 85KB,如 AccountCenterScreen.kt、WelcomeIntroScreen.kt、DailyCheckInDao.kt 等)因 git 打包位置原因未能读取,已在附录列出;结论不受实质影响。

---

## 一、总体结论

TinyVow 的**产品完成度和细节投入远超一般独立应用**:自研主题令牌系统、双语文案 3108 个 key 几乎完全同步、权限引导有披露弹窗/诊断页/骨架屏、加密选型全部正确(PBKDF2 口令、Keystore 存 API Key、RSA-2048 离线签名激活码、强制 HTTPS)、离线专注的防双记设计(`settled_ledger_id` 唯一索引)是全库最稳的一段代码。

但存在 **四个系统性问题**,它们是绝大多数具体缺陷的共同根源:

| # | 系统性问题 | 典型表现 | 后果 |
|---|---|---|---|
| 1 | **无 ViewModel、无导航框架、无 DI**,`HomeRoute` 是约 3000 行的巨型 Composable,内联 new 出 ~20 个仓库、收集 ~70 个 Flow | HomeScreen.kt 单文件 11,130 行 / 503KB | 状态丢失、回退错乱、重组风暴、无法测试 |
| 2 | **积分余额"双真值"**:余额存 DataStore、台账存 Room,二者更新不在同一事务;互斥锁是"每实例一把" | 购买在事务提交后才扣余额;13 处 `new AppLimitRepository()` | 崩溃白嫖、并发双花、账实永久漂移 |
| 3 | **热路径无节制的全量扫描与轮询** | 每记一笔积分全表扫成就;首页 5 秒轮询 UsageStats;专注服务每秒查 DB+重发通知;无障碍服务对全系统每个窗口事件查一次 Room | 耗电、卡顿、启动变慢,随使用天数线性恶化 |
| 4 | **~19MB 未优化 PNG + 关闭资源收缩** | 30 张成就徽章各 300–540KB、二维码 PNG 1.35MB、`isShrinkResources=false` | APK 25MB 中约 14–16MB 可直接省掉 |

另有一颗**必须立即排除的雷**:代码里 `AppDatabase version = 34`,但 schema 目录已导出 `35.json`(多出 `blocked_hours_mask` 列)且无 34→35 迁移——如果任何设备的数据库已升到 35,当前包会触发 Room 降级异常**启动即崩溃**。

---

## 二、Top 10 关键问题(按"用户伤害 × 修复性价比"排序)

### P0-1 数据库版本 34/35 分叉(数据层 A2)💣
`AppDatabase.kt:45` 是 `version = 34`,但 `app/schemas/.../35.json` 已存在且比 34 多 `app_groups.blocked_hours_mask` 列,迁移链止于 33→34。
**行动**:立即确认是否有任何构建/内测包跑过 v35。有 → 恢复 version=35 并补 `MIGRATION_34_35`(一条 ALTER TABLE);没有 → 删除 35.json。这是"下次发版就爆"级别。

### P0-2 积分经济可被双花/白嫖(数据层 A1/A3/A4/A5 + 架构 A-1/A-4)
- `AppLimitRepository.kt:212` 的 `rewardActionMutex` 是**实例字段**,而该仓库被 13 处 `new`(UI、无障碍服务、其他仓库内部各一套)——不同实例的锁互不相干,余额检查(:486)与扣减(:522)可并发通过 → 双花/负库存。
- `purchaseReward:503-531`:Room 事务先提交,`preferences.addUserPoints(-cost)` 在**事务外** → 事务后被杀 = 得道具不扣分。
- 归档补分 `DailyArchiveRepository.kt:719` 的 `sourceRefId` 掺 `UUID.randomUUID()`,唯一索引防重完全失效,并发归档双倍计分。
- 微信读书计分:记账在前、水位更新在后且不同事务(`SpecialAppUsageRepository.kt:780-799`),崩溃后重复发分。

**行动**(一揽子修,约 2–3 天):
1. `AppLimitRepository` 改单例(或锁提为 `companion object` 级);
2. 库存扣减改 SQL 原子操作 `UPDATE ... SET quantity = quantity - 1 WHERE id=? AND quantity>0` 并检查受影响行数;
3. 余额只留一处事实来源——推荐余额并入 Room(单行余额表,与台账同事务),DataStore 只做展示缓存;
4. `sourceRefId` 去掉 UUID 后缀,使 `archive-earn:$date:$groupId` 天然幂等;水位更新与记账放同一 `withTransaction`。

### P0-3 系统时钟操纵可绕过一切限额(架构 A-2)
限额窗口、业务日、每日购买上限、目标奖励、超我模式时间窗全部基于 `System.currentTimeMillis()`,无回拨检测——用户把系统时间往前调一天,当日用量即归零。而激活码模块**已经实现了**回拨检测(`ActivationEntitlementResolver` 的 `lastSeenWallClockMillis` + 10 分钟容忍),把同样机制推广到限额/购买窗口即可。对一款反拖延应用,这是核心价值主张的完整性问题。

### P0-4 备份是坏的:导出的 zip 可能已损坏,换机即清零(数据层 A7/B13)
- `LocalDataManager` 在数据库**开着、后台还在写**的时候直接拷贝 db/-wal/-shm 三个文件,无 checkpoint → 备份文件可能 `SQLITE_CORRUPT`。修复:导出前 `PRAGMA wal_checkpoint(TRUNCATE)`,或用 `VACUUM INTO` 生成一致快照(半天工作量)。
- `backup_rules.xml`/`data_extraction_rules.xml` 把 database/datastore 全部排除,连**设备间迁移(D2D)**也排除 → 用户换手机,积分、归档、成就全部清零,唯一出路是手动 zip(而它还可能是坏的)。建议至少对 device-transfer 放开 database。

### P0-5 进程重建后界面状态全丢(UI A1 + 架构 B-2)
`currentScreen`、`rewardsSection`、全部对话框状态用 `remember`(全文件 38 处,仅 5 处 `rememberSaveable`)。而这款应用的核心流程恰恰是"跳系统设置开权限再回来"——期间进程被回收是常态,回来后 31 个页面全部归零、填写内容消失。**一小时的改动**(导航 enum 改 `rememberSaveable`)即可消除最大挫败点。

### P0-6 常驻服务的耗电三连(性能 A4/A5/B4/B5)
- 无障碍服务对**全系统每个窗口/焦点事件**都启协程查一次 Room(离线专注前台检查),即使用户从未用过离线专注;
- 专注计时服务每秒查一次 DB + 每秒重发一次前台通知(2 小时专注 = 7200 次查询 + 7200 次通知),Widget 明明已经用了正确的 `setChronometer` 系统自走秒方案;
- 两个媒体监听服务各自 60 秒轮询 `getActiveNotifications/getActiveSessions`,即使目标 App 没在播放;心跳每 60 秒整文件重写一次 63KB 的 DataStore。

**行动**:内存标志位短路 + 通知改 Chronometer + 空闲停轮询,合计约 1.5 天,是全项目最大的电量收益。

### P0-7 AppText.t() 是全局性能税(性能 A3 + UI A4)
每次调用都 `createConfigurationContext`(新建重量级 Context)+ `getIdentifier` 反射查找 + `Locale.setDefault` 全局副作用;全库 **3017 处调用点**,HomeScreen 单文件 523 处,全在重组热路径上。它还是 `isShrinkResources=false` 的根因。修复(缓存 localizedContext + key→resId ConcurrentHashMap)半天即可,一处改动全局收益。

### P0-8 首页 5 秒轮询 + 70 个 Flow 同一作用域(性能 A1/A2)
每 5 秒做多次 UsageStats 跨进程大查询,然后创建新的 RuntimeState 实例写回 HomeRoute → 整树重组 + 主线程重算含 3650 条归档的聚合。四个 Tab 的全部数据流无论在哪个页都保持活跃;另有同一 Room Flow 被重复收集 4 组、两个重复的分钟级 ticker。

### P0-9 APK 体积:约 14–16MB 白给(性能 A8)
成就徽章 12MB → 转 WebP q85 + 降到 512px ≈ 1.5–2MB;网站二维码 1.35MB → 运行时 ZXing 生成或 300px ≈ 30KB;石子瓶背景 1.4MB → 200KB;加上修好 P0-7 后开启 `shrinkResources`。一天工作量,包体积从 ~25MB 降到 ~10MB,还顺带消除成就页 100MB 级的解码内存风险。

### P0-10 激活码可清数据重放叠加续期(架构 A-3)
一次性核销依赖本地 DataStore 的 `usedCodeIds`,清应用数据+保留 userId 即可在有效期内反复激活同一码,且 `extendFrom` 叠加续期。国内已有后端 `/v1/activation-codes/redeem`,建议离线码也强制走一次服务端核销,本地去重仅作离线兜底。

---

## 三、分维度发现摘要

### 3.1 UI 显示与交互逻辑(29 项)

**高severity**:
- **深色模式完全未实现**:`Theme.kt:14` 的 `darkTheme` 参数从未被读取,全工程无 `darkColorScheme`;叠加 `enableEdgeToEdge()`,深色系统下状态栏浅色图标压在浅色背景上几乎不可读;夜间(自律类应用的高频时段)全屏白光,拦截 overlay 同样纯亮色。
- **系统字体缩放被钉死 `fontScale=1`**(`Theme.kt:20-24`),视障用户调大系统字号完全不生效;且 `MainActivity` 传入的 `appFontScale` 参数与快照中 `TinyVowTheme` 签名对不上——应用内"文字大小"设置疑似实际无效,需核对。
- **商店奖励一键扣积分,无确认、无撤销**(`RedeemScreen.kt:824-832`):滚动误触即扣掉用户自律换来的积分;而删除自定义奖励反而有确认弹窗,标准不一致。
- **新手引导蒙层不拦截触摸**(`HomeScreen.kt:5391`):用户可点穿遮罩操作底层界面,引导错位。
- **拦截页"紧急解锁"失败无任何反馈**(`AppLimitAccessibilityService.kt:846-874`):解锁券用完时按钮只是闪一下,用户在最激动的时刻以为应用坏了;且 fast-path 分支不校验库存,0 库存也显示解锁按钮。

**中severity 精选**:回退表错位(首页进签到总览,返回却落在"我的");奖励配置页没有 BackHandler,系统返回直接跳回首页;全屏约定编辑器按返回**静默丢弃所有编辑**;设计系统的 `TinyVowAlertDialog` 定义了却 **0 处调用**,44 处裸 `AlertDialog` 各自为政;成就页三套等级配色 40 处硬编码 `Color(0xFF...)` 绕过主题;`res/layout/block_overlay.xml` 是死代码且与真实 overlay 严重脱节;拦截 overlay 用物理屏高定位,分屏/横屏错位;英文文案 `maxLines=2` 截断;底部导航纯图标无文字无 Tab 语义;长按隐藏功能无提示;分享失败把英文异常消息直接 Toast 给用户;`remember` 缓存了 `AppText.t()` 结果导致切语言后残留旧文案;MIUI 硬编码的自启动跳转在其他厂商跳到不相干页面。

**i18n 整体质量很好**:中英 key 集合完全一致,仅 `%1$dmin` 未汉化、调试页 "Palette" 未翻译两处瑕疵。

### 3.2 数据库结构与数据层(31 项)

**表清单**:33 张表,**0 个外键**;`redemption_history` 和 `bonus_times` **零索引**(购买限购检查每次全表扫);`media/lock_screen` 段表**没有任何删除路径**(只增不减,数年后拖慢区间查询与备份);`point_ledger`、`protection_events`、`block_events`、`reward_use_history` 同样无保留策略;`daily_app_archives` 的 `hour_00..hour_23` 24 列反规范化,且与 5 分钟切片表重复表达同一事实。

**热路径问题**:
- 每记一笔积分 → `checkAchievements()` → 2 个全表 SUM + 3 个全表加载(每几分钟由无障碍服务触发一次,O(全历史));
- 每次进首页 → `repairArchivesMissingAppSnapshots()` 对**全部历史日期**做 N+1 扫描,每个日期还查一次 UsageStats 全量事件——启动卡顿随使用天数线性恶化;
- `observeAllEntries()` 等全表观察 Flow,任一行写入(高频计分)都重跑全表查询,HomeRoute 把 5 个全表 Flow combine 在一起。

**正确性问题**:成就解锁 UPDATE 无 `AND is_unlocked=0` 守卫 → 并发双通知;`updateGroupApps` 先删后插无事务,中间被杀 = 组成员全没;打卡库存增量在事务外读取(read-modify-write 竞态);`AppDatabase.getDatabase` 快路径不校验库名与 isOpen;`BusinessDay.cachedStartHour()` 全局缓存在部分服务进程从不初始化,自定义日界用户的媒体播放会记错业务日;`observeTodayState` 捕获的"今天"跨日不刷新。

**代码树内部漂移**(需要在你本地核实):`AppLimitRepository.kt:581` 调用了 `ActiveRewardEffectDao` 中不存在的 `getBlockingForGroupAndPeriod`;`ArchiveDateUtils.localDateAt` 被以 3 参调用但定义只有 2 参——提示部分文件版本不同步,建议 `git status` + 编译确认。

**亮点**:`offline_focus_sessions.settled_ledger_id` 唯一索引 + 事务内查重是全库最稳的防双记设计;步数/微信读书的水位表思路正确(只差事务边界);迁移 11→12 流程规范。

### 3.3 性能、动画与页面负载(32 项)

除 Top10 中的以外,值得注意的还有:
- **统计页首帧**:图表颜色在组合期同步做 图标IPC + 128×128 位图 + Palette 量化 × 全部应用(≈150–450ms 主线程);每次进入统计 Tab 全量拉 3650 天归档(缓存分支放在了查询之后);`LazyColumn` 里只有**一个 item**,懒加载完全失效,整页所有 Canvas 图表一次性组合。
- **动画**:骨架屏 shimmer 在组合期读动画值 → 加载期整页逐帧重组(应移到 draw 阶段);`AnimatedMetricText` 动画期间**每帧重新编译 1–4 个 Regex**;成就徽章每个实例 3 通道 InfiniteTransition + blur,首页那颗滚出屏幕仍在跑。
- **内存**:自定义专注图标主线程 `BitmapFactory.decodeFile` 全尺寸解码、导入不压缩(12MP 照片 = 48MB Bitmap/组合点,OOM 风险);分享海报 2880px 宽 × 不限高 ARGB 可达 50–80MB;奖励图标选择器一次性组合 44 张 PNG 非懒加载。
- **启动**:`MainActivity.onCreate` 在 `setContent` 前主线程初始化 WorkManager 两次调度;首帧后 `LaunchedEffect(Unit)` 串行跑同步内建奖励 + 成就定义 + 全表成就检查;30 秒一次的过期奖励 DB 轮询挂在 UI 层;`requestHighestRefreshRate` 强制 120Hz 渲染大量静态内容。
- **工程**:Compose BOM 停在 2024.09.00(约两年前),无 Baseline Profile(对 500KB 巨型文件冷启动收益通常 15–30%);`material-icons-extended` 全量依赖;gradle 未开 parallel/caching,堆仅 2GB;`activity:1.8.0` 被强制降级锁定。

### 3.4 架构、业务逻辑与安全(24 项)

- **装配方式**:全库只有 `AppDatabase` 一个真单例,仓库全部在使用点 `new`,UI 直接调 DAO(`HomeScreen.kt:871,879`)、直接持有 database。无 Hilt/Koin、无 ViewModel。
- **安全做对了的**:强制 HTTPS(`require(baseUrl.startsWith("https://"))`)、微信读书 Key 走 Keystore AES/GCM、超我口令 PBKDF2 120k 迭代 + 常量时间比较、激活码 RSA-2048 验签。
- **需要注意的**:后端权益/token/`entitlementStatus` 明文存 DataStore 无完整性保护(root 可改 ACTIVE);`tools/activation/` 目录下**私钥文件 `private_key.pkcs8` 在项目仓库里**——只要它进过 git,任何拿到仓库的人可离线签发任意 Pro 激活码,强烈建议移出仓库并评估换钥;release 构建缺失公钥配置时会静默兜底到脚本内联的默认公钥,应 fail-fast。
- **拦截健壮性**:`showBlockOverlay` 的 `if (blockView != null) return` 会漏挡"限额应用 A → 限额应用 B"的切换(overlay 语义不刷新,存在短暂可用窗口);`onDestroy` 里 `runBlocking(IO)` 阻塞主线程有 ANR 风险;`checkAchievements` 的 `catch (_: Exception) {}` 静默吞掉一切。
- **测试**:纯函数策略类(`*Policy`/`*Calculator`/`BusinessDay`)覆盖良好,但 `GroupLimitEnforcer`、奖励并发、`SuperModeController`、整个 supermode 包 **0 单测**——恰好是本报告 P0 级问题所在的路径。

---

## 四、整体优化方案(三阶段路线图)

### 第一阶段:止血(约 1–2 周,发一个修复版)

| 序 | 事项 | 工作量 | 对应问题 |
|---|---|---|---|
| 1 | 排除 34/35 版本分叉;决定补迁移或删 schema | 0.5 天 | P0-1 |
| 2 | 积分经济一揽子:仓库单例化 + SQL 原子扣减 + 余额入事务 + sourceRefId 幂等 + 水位入事务 | 2–3 天 | P0-2 |
| 3 | 备份修复:checkpoint/`VACUUM INTO` 后再导出;放开 device-transfer | 1 天 | P0-4 |
| 4 | 导航/关键状态改 `rememberSaveable` | 0.5 天 | P0-5 |
| 5 | 服务耗电三连:无障碍标志位短路、专注通知改 Chronometer、媒体轮询空闲即停、心跳独立小文件+放宽到 5 分钟 | 1.5 天 | P0-6 |
| 6 | `AppText` 缓存化(Context 复用 + resId 缓存 + 移除 `Locale.setDefault` 副作用) | 0.5 天 | P0-7 |
| 7 | 资源瘦身:PNG→WebP、二维码运行时生成、开 shrinkResources | 1 天 | P0-9 |
| 8 | 情绪敏感点三小修:购买加确认弹窗、引导蒙层吞触摸、紧急解锁失败给文案(顺带 fast-path 校验库存) | 1 天 | P0-5/UI |
| 9 | 首页轮询 5s→30-60s + 聚合移到 Default 线程 + RuntimeState 内容相等判断 | 1 天 | P0-8 |
| 10 | 私钥移出仓库 + release 缺公钥 fail-fast | 0.5 天 | 安全 |

**预期收益**:消除全部"资损级"缺陷;冷启动约 -20–40%;前台耗电显著下降;APK 25MB→~10MB;换机/备份可用。

### 第二阶段:结构改造(约 1–2 个月,渐进式,不重写)

1. **依赖装配抽取**:先把 `HomeRoute` 的 20 个仓库构造抽成 `AppContainer`(手写容器即可,不必先上 Hilt),服务与 UI 共享同一套实例——这同时是 P0-2 锁问题的根治。
2. **按 Tab 拆 ViewModel**:HomeViewModel / StatsViewModel / RewardsViewModel / MeViewModel,数据收集下沉,`SavedStateHandle` 承接状态;规则:**新功能不再进 HomeScreen.kt**。
3. **Navigation Compose 迁移**:替换 31 个 Screen 枚举 + 手写回退表,根治回退错乱类缺陷(B1/B2 等一整类)。
4. **数据层收敛**:成就检查节流(按日/解锁边界);归档修复加水位游标;全表 Flow 改窗口查询/分页;补 `redemption_history`、`bonus_times` 索引;段表/事件表加 90 天保留策略;成就解锁 UPDATE 加守卫;多步写补事务。
5. **时钟防护**:把激活码模块的回拨检测推广到限额/购买窗口/超我时间窗。
6. **统计页性能**:LazyColumn 按模块拆 item;图表颜色去掉同步路径;归档内存缓存前置;shimmer 移到 draw 阶段;Regex 提为常量。
7. **补测试**:优先 `GroupLimitEnforcer.evaluate`、奖励并发(双花回归)、`SuperModeController` 时间窗、迁移链 MigrationTestHelper 9→35。

### 第三阶段:体验升级(排期视产品节奏)

1. **深色模式**:先锁定状态栏图标可读(一行),再做暗色 tokens(拦截 overlay 一并);
2. **尊重系统字体缩放**(clamp 到 1.3 而非钉死 1.0),理顺应用内字号设置;
3. **无障碍**:底部导航 Tab 语义 + 文字标签、语言选择 `selectable` 语义、信息性图标补 contentDescription;
4. **激活码服务端核销**;后端权益字段加 HMAC 完整性校验;
5. **工程升级**:Compose BOM 升级 + Baseline Profile + gradle parallel/caching + 解除 activity 1.8.0 锁定;
6. **数据模型清理**:24 小时列 vs 切片表二选一、删除冗余前缀索引、备份清单改 `sqlite_master` 动态枚举;
7. 平板/折叠屏适配(放开竖屏锁定,随 ViewModel 化一并做)。

---

## 五、量化收益预期

| 维度 | 现状 | 第一阶段后 | 第二阶段后 |
|---|---|---|---|
| APK 体积 | ~25MB | **~10MB** | ~9MB(shrink 后) |
| 冷启动 | 主线程 WorkManager+全表扫描+反射文案 | **-20~40%** | 再 -15~30%(Baseline Profile) |
| 前台耗电 | 5s UsageStats 轮询+每秒通知+全事件 DB 查询 | 服务侧降 1 个数量级 | 重组范围再收敛一个数量级 |
| 积分账实 | 崩溃/并发可漂移、可双花 | **强一致** | 有并发回归测试保护 |
| 状态留存 | 授权往返即丢 | 关键导航可恢复 | 全量(ViewModel+SavedState) |
| 备份 | 可能损坏、换机清零 | 一致快照、D2D 可迁 | — |

---

## 附录 A:未能读取的文件(21 个,约 85KB)

`ui/home/AccountCenterScreen.kt`、`ui/home/WelcomeIntroScreen.kt`、`ui/home/BehaviorScoreCalculator.kt`、`ui/home/BehaviorScoreMetricDetail.kt`、`ui/home/RuntimeDiagnostics.kt`、`ui/home/SpecialAppReplacementStatus.kt`、`ui/home(rewards)/TinyVowPackageButtons.kt`、`ui/home(rewards)/TinyVowPackageDialogs.kt`、`data/account/BackendAccountModels.kt`、`data/db/DailyCheckInDao.kt`、`data/db/DailyCheckInEntity.kt`、`data/db/ProtectionEventDao.kt`、`data/db/ProtectionEventEntity.kt`、`data/db/RewardEffectBenefitDao.kt`、`data/db/RewardEffectBenefitEntity.kt`、`data/reminder/GroupReminderEvaluator.kt`、`data/reminder/ReminderPolicy.kt`、`data/reminder/ReminderScheduler.kt`、`data/repository/ProtectionEventRepository.kt` 及 4 个测试文件。另 `ManagedAppPreferences.kt` 与 `BusinessDay.kt` 仅有部分历史版本,涉及其内部行为的结论已在正文标注。

## 附录 B:需要你本地确认的两件事

1. `git status`:工作区有 7 月 24–26 日的未提交修改(SuperMode、HomeScreen、AppLimit 拦截链路、DB v35 相关),本报告基于 7 月 23 日提交 + 最新资源文件——**34/35 分叉(P0-1)很可能正是这批未提交代码的一半**,请优先核实。
2. 在本机跑一次 `./gradlew :app:compileDebugKotlin`,确认 3.2 节提到的"仓库调用了 DAO 不存在的方法"是否为快照时间差(若你本地能编译通过,则说明相关文件已在未提交修改中同步更新)。
