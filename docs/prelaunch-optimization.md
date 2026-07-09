# Tiny Vow 上架前优化清单

最后检查日期：2026-05-31

本清单用于每次准备上架、提审、发国内测试包或整理发布产物前做收口。目标是先保证发布可控、审核材料一致、核心链路可验证，再考虑体验打磨。

## 当前结论

- 当前版本来源是 `gradle.properties`：`TINYVOW_VERSION_NAME=1.1.0`、`TINYVOW_VERSION_CODE=6`。
- Google Play 渠道包名是 `com.rrrrz.tinyvow`，国内渠道包名是 `com.rrrrz.tinyvow.cn`。
- release 产物必须归档到 `dist/`，命名为 `tinyvow-{channel}-{versionName}-vc{versionCode}-release.{apk|aab}`。
- 核心发布风险不在单一代码点，而在权限披露、无障碍审核说明、订阅商品配置、真机 release 验证和商店资料一致性。

## 必须完成

- 跑通基础验证：
  - `.\gradlew.bat testChinaDebugUnitTest`
  - `.\gradlew.bat assembleDefaultDebug`
  - 需要安装验证时运行 `.\gradlew.bat installDefaultDebug`
- 打正式归档产物：
  - 国内版 APK：`.\tools\package-china-release.ps1`
  - 国内 APK + Google Play AAB 双渠道归档：`.\tools\package-release-artifacts.ps1`
- 检查 `dist/` 文件名、包名、版本号和签名校验输出，确认国内版没有 Play Billing / Google 登录相关权限和组件。
- 在干净真机上安装 release 包，至少手动验证：
  - 首次启动、引导、首页和“我的”页版本信息。
  - Usage Access disclosure、跳转设置、授权后返回刷新。
  - Accessibility disclosure、开启服务、超额 App 阻断 overlay、返回 Tiny Vow 后 overlay 移除。
  - 通知权限拒绝后核心功能仍可用。
  - CONTROL 分组超额阻断、ENCOURAGE 积分累计、奖励兑换和积分扣减。
  - 统计页无权限、无数据、已有归档、分享图。
  - 主题切换、语言切换、重启后状态保留。
  - 本地数据导出、可恢复备份导入、清理本地数据、删除账号；备份 zip 不应包含微信读书 Key 明文。
- Google Play 提审前确认：
  - `docs/privacy.html` 和 `docs/account-delete.html` 已发布到 Play Console 填写的真实 URL。
  - Play Console 已配置 `tinyvow_pro` 订阅、base plan、价格、测试账号、取消/宽限期等设置。
  - Data safety 表单、Accessibility API 声明、敏感权限说明与应用内 disclosure 和 `docs/google-play-release-checklist.md` 保持一致。
  - Google Play 版购买、恢复、pending、取消、重新订阅至少在测试轨道验证一次。
- 国内版发布前确认：
  - `release-signing/tinyvow-cn-release.properties` 和 keystore 只保留在本机，不提交仓库，不复制到 `dist/`。
  - 国内版不显示 Google 登录、Play Billing 购买、恢复购买或管理订阅入口。
  - 本地激活码入口、用户 ID 复制、激活成功/过期/时间回拨提示可用。

## 建议优化

- 商店资料：
  - 根据 `docs/market-listing-copy.md` 固定上架标题、短描述、长描述和关键词口径，避免混用 CONTROL / ENCOURAGE 这类内部术语。
  - 准备截图时覆盖首页、约定分组、投入积分、战报、奖励、主题、数据隐私和 Pro 页面。
  - Google Play 资料避免承诺“完全锁机”“强制防沉迷”“后台持续监控”等高风险表达，优先使用“本地统计”“提醒”“用户主动开启权限”。
- 权限和审核材料：
  - 为 Accessibility API 声明准备一段与应用内 disclosure 一致的中文/英文说明，并准备一段短录屏：开启服务、进入超额 App、显示 Tiny Vow overlay、返回应用。
  - 确认隐私政策、账号删除说明、应用内支持页、Play Console Data safety 的数据类型和删除口径一致。
- 体验和稳定性：
  - 真机覆盖至少一台 Android 13+ 设备，因为通知权限是运行时权限。
  - 额外覆盖一台国产 ROM，重点看自启动、电池优化、无障碍服务保活和返回设置刷新。
  - 对微信读书双源口径做一次回归：首次同步前、READING_FIRST、PHONE_FIRST、备份恢复后重新填写 Key。
  - 导入备份时覆盖异常文件：跨渠道包名、新版本备份、超大 zip、路径异常 zip 都应失败并显示可理解的错误。
- 多语言：
  - 检查 `app/src/main/res/values/app_texts.xml` 没有中文默认文案。
  - 检查中英文 `app_texts.xml` key 和占位符一致。
  - 重点查看权限 disclosure、订阅错误、数据隐私、版本页和支持页的英文表达。

## 可延后

- 更大范围的 UI 自动化测试。权限设置、无障碍和 overlay 很多步骤依赖系统设置页，首版以人工真机冒烟为主。
- 自有后端同步、跨设备同步、远端账号删除自动化。当前产品定位是 local-first，首版不要为了上架临时引入远端依赖。
- 崩溃上报或分析 SDK。若后续接入，必须同步更新隐私政策、Data safety、应用内 disclosure 和 local-first 表述。
- 更细的国内分发平台素材适配。首版先保证标准 APK、截图、隐私链接和联系方式可用，再按平台要求补图。

## 每次改动后的收口原则

- 代码、资源、Manifest、Room、构建配置或发布脚本改动后，至少运行 `testChinaDebugUnitTest` 和 `assembleDefaultDebug`。
- 只改文档时可以不跑 Gradle，但最终说明必须明确未运行测试的原因。
- 涉及上架资料时，同步检查 `docs/release.md`、`docs/google-play-release-checklist.md`、`docs/market-listing-copy.md`、`docs/privacy.html`、`docs/account-delete.html` 和 `CHANGELOG.md`。
- 提交或归档 release 前，不要把 keystore、签名 properties、激活私钥、Play Console 截图中的敏感信息写入仓库。
