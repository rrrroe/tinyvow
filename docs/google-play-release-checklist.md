# Google Play 发布清单

最后检查日期：2026-05-23

## 商店资料与政策链接

- 隐私政策 URL：`https://rrrroe.github.io/tinyvow/privacy.html`
- 账号删除 URL：`https://rrrroe.github.io/tinyvow/account-delete.html`
- 订阅取消入口：应用内打开 Google Play 订阅管理页，商品 ID 为 `tinyvow_pro`。
- 支持/联系邮箱：`rrrr.zhao@qq.com`
- 微信：`rourourenren222`

## 数据安全表单草稿

- 用户提供的账号数据：如果用户选择 Google 登录，应用会处理 Google 账号基础资料用于登录状态和本地会话。第一版不会发送到 Tiny Vow 自有服务端。
- 应用活动与应用信息：应用会在设备本地处理使用情况统计、已选择应用、限额、应用分组、阻断记录、积分、成就、兑换奖励、奖励库存、主动奖励效果、自定义奖励图标和主题设置，用于限额、战报和进度功能。
- 购买数据：Google Play Billing 会处理 `tinyvow_pro` 的订阅购买、续费和状态。
- 数据共享：第一版没有 Tiny Vow 后端数据共享。Google 登录和 Play Billing SDK 自身的数据处理仍需按 Google Play Data safety 指引如实声明。
- 安全实践：第一版不启用 Tiny Vow 自有服务端传输；用户可以主动导出本地数据摘要，也可以在应用内清除本地数据。导出文件只在用户通过系统分享面板主动发送时离开设备。
- 数据删除：应用内已有删除账号入口；清除本地数据覆盖数据库、偏好、本地账号/激活状态、导入奖励图标、分享缓存和微信读书 Key 材料。由于应用支持账号登录，Play Console 仍要求提供可用的网页账号删除入口。

## 敏感权限与声明

- `PACKAGE_USAGE_STATS`：用于计算前台使用时长、限额、战报、积分和提醒。打开系统设置前，应用会先展示单独的显著披露并要求用户明确同意。
- 无障碍服务：仅用于检测前台应用切换，并在已设置限额的应用超额时显示 Tiny Vow 的阻断页面。不读取屏幕文字，不代替用户点击，不修改系统设置。需要在 Play Console 完成 Accessibility API 声明。
- `POST_NOTIFICATIONS`：用于限额提醒和应用通知。仅在 Android 13 及以上版本请求运行时通知权限。
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`：用于提升限额执行和提醒的可靠性。用户可以拒绝，核心功能仍可使用。
- `QUERY_ALL_PACKAGES`：未声明。应用发现范围限制为 launcher apps，以及已通过 UsageStats 可见的应用。

## 订阅说明

- 商品 ID：`tinyvow_pro`
- Billing 库版本：Play Billing 8.x
- 未订阅时，免费核心功能保持可用。
- 只有 Google Play 返回已购买状态时才解锁 Pro；pending 付款不会提前解锁。
- 应用内已提供购买、恢复购买、管理/取消订阅入口。
- 正式测试前，需要在 Play Console 配置 `tinyvow_pro` 订阅、base plan、offer、测试账号、价格、宽限期和取消设置。

## 发布前冒烟检查

- 在干净设备上安装 release 构建。
- 确认首次启动在未授权使用情况访问、未开启无障碍服务时仍可正常使用。
- 确认打开使用情况访问设置前会出现显著披露。
- 确认打开无障碍设置前会出现显著披露。
- 确认通知、电池优化和自启动入口都是可选增强，不授权也不阻断核心功能。
- 确认 `TINYVOW_GOOGLE_WEB_CLIENT_ID` 为空时，Google 登录按钮能优雅降级。
- 确认退出登录会保留本地数据。
- 确认删除账号可以同时清除本地数据。
- 确认导出本地数据会打开系统分享面板。
- 使用 Google Play 测试账号确认 `tinyvow_pro` 的购买、恢复、pending、取消和重新订阅流程。
- 提交 Play Console 前，确认隐私政策页面和账号删除页面已经真实上线。

## 官方参考链接

- Google Play Data safety 表单：https://support.google.com/googleplay/android-developer/answer/10787469
- Google Play 账号删除要求：https://support.google.com/googleplay/android-developer/answer/13327111
- Google Play 订阅取消要求：https://support.google.com/googleplay/android-developer/answer/12154973
- Google Play AccessibilityService API 政策：https://support.google.com/googleplay/android-developer/answer/10964491
