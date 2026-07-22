# Tiny Vow 发布流程

本项目的两个商店渠道默认共用同一套基础版本号。

上架前剩余优化、人工验证和审核材料收口见 `docs/prelaunch-optimization.md`。本文件只记录固定发布流程和产物规则。

## 版本来源

- 在根目录 `gradle.properties` 修改 `TINYVOW_VERSION_NAME` 和 `TINYVOW_VERSION_CODE`。
- `TINYVOW_VERSION_NAME` 必须使用 SemVer 三段式：`MAJOR.MINOR.PATCH`，例如 `1.0.0`。
- `TINYVOW_VERSION_CODE` 必须是正整数。每次对外发布 APK/AAB 前必须递增。
- 不要把渠道后缀写进 `TINYVOW_VERSION_NAME`。`china` flavor 会自动追加 `-cn`；`googlePlay` flavor 使用基础版本名。

## 渠道产物

- Google Play 包名：`com.rrrrz.tinyvow`。
- 国内版包名：`com.rrrrz.tinyvow.cn`。
- Google Play 发布包：`:app:bundleGooglePlayRelease`。
- 日常本地调试包：`:app:assembleDefaultDebug`，当前指向 `chinaDebug`。
- 标准归档目录：根目录 `dist/`。
- 标准产物命名：`tinyvow-{channel}-{versionName}-vc{versionCode}-release.{apk|aab}`。
- 当前渠道名固定使用 `cn` 和 `googleplay`。
- 国内版可直接运行 `.\tools\package-china-release.ps1`。
- 需要同时整理国内 APK 和 Google Play AAB 时运行 `.\tools\package-release-artifacts.ps1`。
- `.\tools\package-china-release.ps1` 在 APK 签名、包名和版本校验通过后，会自动发布 `tinyvow.rorolo.com`：同步 APK、官网版本信息和仓库内宣传素材，构建验证后上传到独立版本目录并原子切换。
- 官网发布默认不可跳过，避免“已打包但官网仍下载旧 APK”。只有明确的故障排查或离线归档场景才可传入 `-SkipWebsitePublish`；该参数不会自动补发官网。

## 签名注意事项

- 国内版 release 签名默认从 `release-signing/tinyvow-cn-release.properties` 读取，字段必须包含 `storeFile`、`storePassword`、`keyAlias`、`keyPassword`。
- `release-signing/` 下的 keystore、properties 和密码材料只保留在本机，不提交仓库，不放进 `dist/`，也不要贴进文档或更新日志。
- `.\tools\package-china-release.ps1` 会检查签名配置、keystore 是否存在，并在最终 APK 生成后执行 `apksigner verify`；国内版正式包优先走这个脚本，不要手动跳过校验。
- 需要同时整理两个渠道时，`.\tools\package-release-artifacts.ps1` 会先调用国内版签名流程，再复制 Google Play AAB 到 `dist/`。
- 如果某个渠道已经对外分发或上传过，不要随意更换 release keystore；换签名前先确认升级链路和外部平台要求。
- Google Play AAB 在本地按 release 流程归档，最终上架签名与交付规则仍以 Play Console / Play App Signing 配置为准。

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

7. 需要同时归档国内 APK 和 Google Play AAB 时运行：

   ```powershell
   .\tools\package-release-artifacts.ps1
   ```

8. 只发布国内版时构建 release APK：

   ```powershell
   .\tools\package-china-release.ps1
   ```

9. 检查 `dist/` 里的最终归档名：
   - `tinyvow-cn-{versionName}-vc{versionCode}-release.apk`
   - `tinyvow-googleplay-{versionName}-vc{versionCode}-release.aab`

10. 打开应用“我的”页，确认版本信息行：
   - 国内版：`{TINYVOW_VERSION_NAME}-cn`、构建 `{TINYVOW_VERSION_CODE}`、国内版。
   - Google Play 版：`{TINYVOW_VERSION_NAME}`、构建 `{TINYVOW_VERSION_CODE}`、Google Play。

11. 国内 APK 自动发布完成后，确认命令输出中的官网健康检查通过：
    - 首页下载链接指向新的 `tinyvow-cn-{versionName}-vc{versionCode}-release.apk`。
    - `/downloads/tinyvow-cn-{versionName}-vc{versionCode}-release.apk` 返回 `200`。
    - 输出的 SHA-256 与 `dist/` 中 APK 的 SHA-256 一致。

## Git 标签

- 国内版标签：`china-v{versionName}+{versionCode}`。
- Google Play 标签：`googleplay-v{versionName}+{versionCode}`。
- 如果两个渠道从同一个 commit 发布，可以在同一个 commit 上打两个标签。
