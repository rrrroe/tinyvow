# Tiny Vow 开发 TODO

## 当前基线

- 当前代码以“分组限额 + 奖励积分 + 无障碍软阻断”为主线，且这套主线已经过你本人验证。
- `CONTROL` 分组负责限制与阻断。
- `ENCOURAGE` 分组负责按使用时长累计积分，并支持目标达成奖励。
- 首页已经不是早期 MVP，而是完整产品壳：
  - `HOME`
  - `REWARDS`
  - `STATS`
  - `ME`
  - `LABORATORY`
- 阻断执行当前采用：
  - `AccessibilityService`
  - `TYPE_ACCESSIBILITY_OVERLAY`
  - `GroupLimitEnforcer`
- 现阶段应把这套已验证实现视为稳定基线，不轻易推倒重做。

## 当前实现要点

- `AppLimitAccessibilityService`
  - 监听前台窗口变化
  - 判断当前包名是否超额
  - 超额时显示 overlay
  - 同时承担积分累计与奖励结算
- `GroupLimitEnforcer`
  - 负责按分组、按周期评估是否超额
  - 支持 `DAILY / WEEKLY / MONTHLY`
  - 支持加时包
- `AppLimitRepository`
  - 负责分组、奖励、成就、兑换等数据操作
- `ManagedAppPreferences`
  - 负责用户积分、今日积分、主题、昨日总结、自启动引导状态等偏好数据
- `HomeScreen`
  - 已承载权限引导、分组管理、奖励入口、实验室入口等产品主流程

## 规划原则

- 不随意重写已验证通过的阻断主链路。
- 后续修改尽量小步快跑，一次只改一个明确目标。
- 先补“可观测性”和“产品完善度”，再做架构性重构。
- 只有在现有实现确实成为开发阻碍时，才拆分 `AppLimitAccessibilityService`。

## 第一阶段：已完成

- 完成 Usage Access 接入
- 完成无障碍服务接入
- 完成按分组限额的超额判定
- 完成 overlay 软阻断
- 完成积分累计与奖励雏形
- 完成首页主流程和多页面壳

说明：
- 这一阶段现在应视为“完成并冻结主链路”。
- 除非发现明确 bug，否则不要再对阻断实现做大改。

## 第二阶段：先补产品闭环

### 2.1 分组管理体验

- 梳理“创建分组 / 编辑分组 / 删除分组”完整流程
- 补充分组类型说明：
  - `CONTROL` 是限制组
  - `ENCOURAGE` 是激励组
- 优化 App 选择后的反馈
- 优化分组卡片的信息密度：
  - 已绑定应用数
  - 当前周期预算
  - 当前周期已使用时长
  - 是否超额

### 2.2 奖励与积分体验

- 明确“积分从哪里来”
- 明确“奖励怎么兑换”
- 增加兑换记录页或最近兑换记录模块
- 补充积分变化反馈：
  - 今日新增积分
  - 达成目标奖励
  - 兑换后扣减

### 2.3 统计页落地

- 将 `STATS` 从占位页做成真实页面
- 先做最小可用统计：
  - 今日总使用时长
  - 本周总使用时长
  - 各分组使用排行
  - 节省时长
  - 今日积分 / 本周积分

## 第三阶段：完善系统适配与运维能力

### 3.1 权限与系统引导

- 完善各厂商说明文案：
  - 小米 / HyperOS
  - 华为
  - OPPO
  - vivo
  - 三星
- 把这些说明收敛成统一的“权限与保活引导”
- 明确这些入口：
  - 使用情况访问权限
  - 无障碍服务
  - 通知权限
  - 电池优化白名单
  - 自启动

### 3.2 实验室与调试能力

- 保留实验室页，用于：
  - 手动加积分
  - 触发昨日总结
  - 快速验证奖励与成就
- 如果后续真机排查确实需要，再补一版轻量调试能力：
  - 不默认重做旧的 block debug 体系
  - 先从最小必要日志开始

## 第四阶段：代码质量与维护性

### 4.1 编码与文案清理

- 清理 Kotlin 文件中的乱码注释
- 清理 `strings.xml` 中的乱码内容
- 统一中文文案语气与术语

### 4.2 测试补齐

- 为 `GroupLimitEnforcer` 补单元测试
- 为奖励与兑换逻辑补测试
- 为积分累计逻辑补测试
- 整理一份手动冒烟测试清单：
  - 权限首次开启
  - 超额阻断
  - 奖励积分累计
  - 奖励兑换
  - 昨日总结

### 4.3 未来再考虑的重构

- 是否拆分 `AppLimitAccessibilityService`
- 是否把 overlay 构建提到独立类
- 是否把积分累计从服务中拆到专门协调器

说明：
- 这些都不是当前第一优先级。
- 只有在后续开发明显受阻时再做。

## 最近最该做的事

1. 完成 `STATS` 页面最小可用版。
2. 把分组管理体验做顺。
3. 补奖励兑换记录和积分反馈。
4. 清理 `strings.xml` 和代码里的乱码。
5. 整理一份真机手动测试清单。

## 暂时不要做的事

- 不要重写当前阻断实现。
- 不要默认先拆 `AppLimitAccessibilityService`。
- 不要在没有明确问题的情况下继续调整 overlay 时序。
- 不要为了“架构更漂亮”打断现有已验证功能。

## 现在最值得先读的文件

- `app/src/main/java/com/rrrrz/tinyvow/service/block/AppLimitAccessibilityService.kt`
- `app/src/main/java/com/rrrrz/tinyvow/domain/limit/GroupLimitEnforcer.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/repository/AppLimitRepository.kt`
- `app/src/main/java/com/rrrrz/tinyvow/data/settings/ManagedAppPreferences.kt`
- `app/src/main/java/com/rrrrz/tinyvow/ui/home/HomeScreen.kt`
- `app/src/main/java/com/rrrrz/tinyvow/ui/home/LaboratoryScreen.kt`
- `app/src/main/res/xml/accessibility_service_config.xml`
- `app/src/main/res/values/strings.xml`
