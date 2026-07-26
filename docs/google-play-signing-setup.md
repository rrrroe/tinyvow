# Google Play 签名与官网升级链

最后检查日期：2026-07-23

## 目标与固定关系

官网 Global APK 和 Google Play 分发 APK 必须同时满足：

- application ID 都是 `com.rorolo.tinyvow`。
- Play 版本的 `versionCode` 高于官网已安装版本。
- 两者最终安装到设备上的 APK 使用同一张 App Signing 证书。

当前本地基线：

| 渠道 | 产物 | versionCode | 证书 SHA-256 |
| --- | --- | ---: | --- |
| China | `tinyvow-cn-1.3.2-vc11-release.apk` | 11 | `82501E80068CECE9DF2380CC0998F02803EE02A7BBA2CD97A7F90ABAC86E0326` |
| Global Beta | `tinyvow-global-1.3.2-vc12-release.apk` | 12 | `D02A49C08D6AACCB8561A3FAB5D57B60B05B5D32FF6E69FE9678728007DD503B` |
| Google Play upload | `tinyvow-googleplay-1.3.2-vc13-release.aab` | 13 | `C7E7AA5719160CFD60635DB4032C54A97E2F6301378FEE275C5BAE6F59ECFAC0` |

China 是不同包名、不同签名、独立升级链，不参与 Global/Play 迁移。

## 密钥职责

- Global App Signing key：签官网 APK，并作为 Play App Signing key 导入 Play Console。
- Play Upload key：只签上传到 Play Console 的 AAB，不能拿来签官网 APK。
- Play 下载后分发的 APK 必须显示 Global App Signing 证书，而不是 Upload 证书。

本机材料位于被 Git 忽略的 `release-signing/`。创建 Play 应用前，至少做两份加密离线备份，分别保存 JKS、properties、PEM/DER 证书，并验证备份可以读取。不要把密码、JKS 或 PEPK 输出提交到仓库、网盘公开链接或聊天记录。

## Play Console 建档

1. 创建新应用，应用名使用 `Tiny Vow`，默认语言按实际商店资料选择。
2. 首个 AAB 的 application ID 必须是 `com.rorolo.tinyvow`。
3. 进入 App integrity / Play App Signing 设置，选择使用现有 App signing key 或等价的高级选项。
4. 按 Play Console 当页提供的 PEPK 工具、加密公钥和命令模板，导入 `tinyvow-global-app-signing.jks`。
5. 不得选择让 Google 自动生成新的 App signing key。若页面没有导入现有 key 的入口，先停止，不要上传首个 release。
6. 将 `tinyvow-play-upload-cert.pem` 注册为独立 Upload certificate；首个 AAB 使用 `tinyvow-play-upload.jks` 签名。
7. 完成后在 App integrity 页面逐字核对：
   - App signing certificate SHA-256：`D02A49C08D6AACCB8561A3FAB5D57B60B05B5D32FF6E69FE9678728007DD503B`
   - Upload certificate SHA-256：`C7E7AA5719160CFD60635DB4032C54A97E2F6301378FEE275C5BAE6F59ECFAC0`
8. 任一指纹不一致都不要继续发布，应在尚无 Play/Global 用户时删除错误草稿或重新建档。

Play Console 的按钮名称可能调整，但最终证书指纹是唯一验收依据。PEPK 命令使用 Console 当次生成的加密参数，不要复用网上示例，也不要在命令历史中直接写密码。

## 首次内部测试

1. 本地运行：

   ```powershell
   .\tools\package-global-release.ps1
   .\tools\package-play-release.ps1
   ```

2. 向 Internal testing 上传 `dist/tinyvow-googleplay-1.3.2-vc13-release.aab`。
3. 在 Play Console 的 App bundle explorer / App integrity 中再次确认分发证书是 Global App Signing 证书。
4. 使用专用测试设备安装官网 Global vc12，创建一条本地约定或其他可识别测试数据。
5. 加入 Internal testing 后从 Google Play 更新到 vc13。不要卸载，不要清数据。
6. 验证包仍是 `com.rorolo.tinyvow`、版本变为 vc13、本地数据库和偏好仍在、应用可正常启动。
7. 如 Play 只显示“卸载/安装”而非“更新”，停止发布并重新核对 App signing 指纹、versionCode、测试账号和测试轨道资格。

## 红线与恢复策略

- Global APK 一旦向真实用户分发，不能再换 App Signing key。
- Play App Signing key 若被错误设置成 Google 新生成的另一把 key，Play 版本无法原地覆盖官网 APK。
- Upload key 丢失通常可以通过 Play Console 支持流程重置；App Signing key 的影响更大，必须优先保护。
- Global key 丢失但尚未创建 Play 应用、且 Global 无用户时，可以重新生成整条 Global/Play 签名链；有用户后不能这样处理。
- 不使用 China key 签 Global，不修改 `com.rrrrz.tinyvow.cn`，不尝试把 China 用户迁移到 Global 包。
- 后续每个 Play `versionCode` 必须高于所有已分发的同包名 Global/Play 版本。

## 发布前记录

每次发布保留以下非敏感记录：

- Git commit、versionName、versionCode、构建时间。
- APK/AAB 文件 SHA-256。
- App Signing 与 Upload certificate SHA-256。
- Play Console 轨道和 release 状态。
- “官网版本 -> Play 版本”不卸载升级测试结果。

不得把 keystore、properties、PEPK 加密包或密码当作发布附件。
