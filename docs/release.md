# Tiny Vow 发布流程

本项目的 China、Global Beta、Google Play 三个渠道共用基础 `versionName`，但使用独立 `versionCode` 序列和签名配置。

上架前剩余优化、人工验证和审核材料收口见 `docs/prelaunch-optimization.md`。本文件只记录固定发布流程和产物规则。

## 版本来源

- 在根目录 `gradle.properties` 修改 `TINYVOW_VERSION_NAME`、`TINYVOW_VERSION_CODE`、`TINYVOW_GLOBAL_VERSION_CODE` 和 `TINYVOW_PLAY_VERSION_CODE`。
- `TINYVOW_VERSION_NAME` 必须使用 SemVer 三段式：`MAJOR.MINOR.PATCH`，例如 `1.0.0`。
- `TINYVOW_VERSION_CODE` 只属于 China 包；已有国内用户时只能递增，不能换签或回退。
- `TINYVOW_GLOBAL_VERSION_CODE` 属于官网 Global APK；`TINYVOW_PLAY_VERSION_CODE` 必须严格高于已分发的 Global versionCode。
- 不要把渠道后缀写进 `TINYVOW_VERSION_NAME`。`china` flavor 自动追加 `-cn`；`global` 和 `googlePlay` 使用基础版本名。

## 渠道产物

- Global Beta 和 Google Play 包名：`com.rorolo.tinyvow`。
- China 包名：`com.rrrrz.tinyvow.cn`。
- Global Beta 发布包：`:app:assembleGlobalRelease`。
- Google Play 发布包：`:app:bundleGooglePlayRelease`。
- 日常本地调试包：`:app:assembleDefaultDebug`，当前指向 `chinaDebug`。
- 标准归档目录：根目录 `dist/`。
- 标准产物命名：`tinyvow-{channel}-{versionName}-vc{versionCode}-release.{apk|aab}`。
- 当前渠道名固定使用 `cn`、`global` 和 `googleplay`。
- 国内版可直接运行 `.\tools\package-china-release.ps1`。
- Global Beta 可运行 `.\tools\package-global-release.ps1`；加 `-PrepareWebsite` 只同步官网本地工作区并测试，不发布生产。
- Google Play AAB 可运行 `.\tools\package-play-release.ps1`。
- 需要同时整理国内 APK 和 Google Play AAB 时运行 `.\tools\package-release-artifacts.ps1`。
- `.\tools\package-china-release.ps1` 和 `.\tools\package-release-artifacts.ps1` 默认只在本地生成、签名、校验并归档发布产物，不改变官网、Sites 或其他生产环境。
- 只有用户明确要求“发布国内版”或“发布官网”后，才可传入 `-PublishWebsite`。该参数会把 APK、官网版本信息、`design/appstore/exports/cn-stores/` 中当前定稿的 S01–S07 宣传图和 `design/tinyvow-website-qr.png` 同步到官网仓库，构建验证后原子发布到 `tinyvow.rorolo.com`。
- 官网仓库同时配置了 Sites。由 Agent 执行官网发布时，官方域名成功后还必须把完全相同的已验证源码发布到 `.openai/hosting.json` 对应的 Sites 生产项目；不需要再次向用户确认。两个目标都成功后才能报告官网发布完成。
- `-PublishWebsite` 不授权也不触发后端部署。后端只有在用户明确要求“发布后端”时才单独部署。
- 双渠道命令带 `-PublishWebsite` 时，必须先完成国内 APK 和 Google Play AAB 两个本地产物，再开始官网发布，避免 AAB 构建失败但官网已经提前切换。

## 签名注意事项

- 国内版 release 签名默认从 `release-signing/tinyvow-cn-release.properties` 读取，字段必须包含 `storeFile`、`storePassword`、`keyAlias`、`keyPassword`。
- Global APK 使用 `release-signing/tinyvow-global-app-signing.properties` 对应的 App Signing key。
- Google Play AAB 使用 `release-signing/tinyvow-play-upload.properties` 对应的 Upload key；Play 分发 APK 必须由导入 Play App Signing 的 Global key重签。
- `release-signing/` 下的 keystore、properties 和密码材料只保留在本机，不提交仓库，不放进 `dist/`，也不要贴进文档或更新日志。
- `.\tools\package-china-release.ps1` 会检查签名配置、keystore 是否存在，并在最终 APK 生成后执行 `apksigner verify`；国内版正式包优先走这个脚本，不要手动跳过校验。
- `.\tools\package-global-release.ps1` 会校验 Global APK 包名、版本、权限和 App Signing 证书。
- `.\tools\package-play-release.ps1` 会校验 AAB 包名、版本、JAR 签名和 Upload 证书。
- 需要同时整理 China 与 Google Play 时，`.\tools\package-release-artifacts.ps1` 会依次调用两个签名流程。
- 如果某个渠道已经对外分发或上传过，不要随意更换 release keystore；换签名前先确认升级链路和外部平台要求。
- Play Console 建档和密钥导入严格按 `docs/google-play-signing-setup.md` 执行。Play App signing certificate 不等于 Upload certificate。

## 发布检查

1. 先按 `docs/prelaunch-optimization.md` 判断本次属于调试包、国内发布、Google Play 提审还是双渠道归档。
2. 确定本次发布版本，并更新 `gradle.properties`。
3. 在 `CHANGELOG.md` 增加对应版本记录。
4. 运行国内 debug 单测：

   ```powershell
   .\gradlew.bat testChinaDebugUnitTest
   ```

5. 运行默认 debug 构建：

   ```powershell
   .\gradlew.bat assembleDefaultDebug
   ```

6. 需要本机安装验证时运行：

   ```powershell
   .\gradlew.bat installDefaultDebug
   ```

7. 生成 Global Beta APK 并准备官网本地副本：

   ```powershell
   .\tools\package-global-release.ps1 -PrepareWebsite
   ```

8. 生成 Google Play AAB：

   ```powershell
   .\tools\package-play-release.ps1
   ```

9. 需要同时归档国内 APK 和 Google Play AAB 时运行：

   ```powershell
   .\tools\package-release-artifacts.ps1
   ```

10. 只生成国内版 release APK 并留在本地：

   ```powershell
   .\tools\package-china-release.ps1
   ```

   用户已经明确要求发布国内版时运行：

   ```powershell
   .\tools\package-china-release.ps1 -PublishWebsite
   ```

11. 检查 `dist/` 里的最终归档名：
   - `tinyvow-cn-{versionName}-vc{versionCode}-release.apk`
   - `tinyvow-global-{versionName}-vc{versionCode}-release.apk`
   - `tinyvow-googleplay-{versionName}-vc{versionCode}-release.aab`

12. 打开应用“我的”页，确认版本信息行：
   - 国内版：`{TINYVOW_VERSION_NAME}-cn`、构建 `{TINYVOW_VERSION_CODE}`、国内版。
   - Global Beta：`{TINYVOW_VERSION_NAME}`、构建 `{TINYVOW_GLOBAL_VERSION_CODE}`、Global。
   - Google Play 版：`{TINYVOW_VERSION_NAME}`、构建 `{TINYVOW_PLAY_VERSION_CODE}`、Google Play。

13. 明确发布国内版并完成官网双目标发布后，确认：
    - 首页下载链接指向新的 `tinyvow-cn-{versionName}-vc{versionCode}-release.apk`。
    - `/downloads/tinyvow-cn-{versionName}-vc{versionCode}-release.apk` 返回 `200`。
    - 输出的 SHA-256 与 `dist/` 中 APK 的 SHA-256 一致。
    - 官方域名 `tinyvow.rorolo.com` 与 Sites 生产版本都来自同一份已验证源码并部署成功。

## Git 标签

- 国内版标签：`china-v{versionName}+{versionCode}`。
- Global Beta 标签：`global-v{versionName}+{versionCode}`。
- Google Play 标签：`googleplay-v{versionName}+{versionCode}`。
- 如果两个渠道从同一个 commit 发布，可以在同一个 commit 上打两个标签。
