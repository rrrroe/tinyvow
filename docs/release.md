# Tiny Vow 发布流程

本项目的两个商店渠道默认共用同一套基础版本号。

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
- 建议产物命名：`tinyvow-{channel}-{versionName}-vc{versionCode}-{buildType}.{apk|aab}`。

## 发布检查

1. 确定本次发布版本，并更新 `gradle.properties`。
2. 在 `CHANGELOG.md` 增加对应版本记录。
3. 运行国内 debug 单测：

   ```powershell
   .\gradlew.bat testChinaDebugUnitTest
   ```

4. 运行默认 debug 构建：

   ```powershell
   .\gradlew.bat assembleDefaultDebug
   ```

5. 需要本机安装验证时运行：

   ```powershell
   .\gradlew.bat installDefaultDebug
   ```

6. Google Play 发布前构建 release bundle：

   ```powershell
   .\gradlew.bat :app:bundleGooglePlayRelease
   ```

7. 国内版发布前构建 release APK：

   ```powershell
   .\gradlew.bat :app:assembleChinaRelease
   ```

8. 打开应用“我的”页，确认版本信息行：
   - 国内版：`{TINYVOW_VERSION_NAME}-cn`、构建 `{TINYVOW_VERSION_CODE}`、国内版。
   - Google Play 版：`{TINYVOW_VERSION_NAME}`、构建 `{TINYVOW_VERSION_CODE}`、Google Play。

## Git 标签

- 国内版标签：`china-v{versionName}+{versionCode}`。
- Google Play 标签：`googleplay-v{versionName}+{versionCode}`。
- 如果两个渠道从同一个 commit 发布，可以在同一个 commit 上打两个标签。
