# Tiny Vow 海外 Public Beta 发布整改审计

审计日期：2026-07-23  
审计范围：`E:\Project\tinyvow`、`E:\Project\tinyvow-site`、`E:\Project\tinyvow-backend`、`https://tinyvow.rorolo.com/en/`  
审计状态：只读审计已完成；2026-07-23 已按负责人确认进入收敛后的本地实施阶段

## 已确认的实施范围（覆盖下文原始建议）

负责人已确认本轮不执行完整整改，而采用以下临时发布边界：

- 保留现有 `china` flavor、`com.rrrrz.tinyvow.cn` 包名和国内已安装用户的升级链路。
- 新增 `global` flavor，使用 `com.rrrrz.tinyvow`，去掉 APK 文件名和 `versionName` 中的 `cn` 字样。
- Global Beta 暂时复用 China 版功能、国内后端、账号、支付宝依赖和激活码能力；这与下文最初建议的渠道隔离不同，属于负责人明确接受的临时风险。
- China 与 Global 先行版均通过激活码激活；Global 只展示计划价格 `US$1/月`、`US$10/年`、`US$39/终身`，暂不开放海外付款。
- 英文公开联系方式改为 X：`https://x.com/roroloxxx`；现有邮箱暂不迁移到域名邮箱。
- Growth 继续展示真实接口数据，不增加低样本隐藏逻辑。
- 本轮不补 Terms、Support、下载详情、完整隐私重写和法律主体集中配置；这些事项保留为后续整改。
- 本轮仅本地构建、复制和测试，不发布官网、Sites、后端或应用商店。

因此，下文“Global 不含中国后端/账号/激活/支付宝”等内容保留为原始审计建议，不再作为本轮验收条件。

## 签名链实施结果

2026-07-23 已完成本地构建与验证，未触碰生产环境：

- China 保持 `com.rrrrz.tinyvow.cn`、vc10 和原证书 `82501E80068CECE9DF2380CC0998F02803EE02A7BBA2CD97A7F90ABAC86E0326`。
- Global Beta 使用 `com.rrrrz.tinyvow`、vc11 和新 App Signing 证书 `D02A49C08D6AACCB8561A3FAB5D57B60B05B5D32FF6E69FE9678728007DD503B`。
- Google Play AAB 使用 `com.rrrrz.tinyvow`、vc12 和独立 Upload 证书 `C7E7AA5719160CFD60635DB4032C54A97E2F6301378FEE275C5BAE6F59ECFAC0`。
- 官网本地工作区已准备 `tinyvow-global-1.3.1-vc11-release.apk`，尚未部署。
- Play Console 建档时必须导入 Global App Signing key，不能让 Google 自动生成另一把 App signing key；完整步骤见 `docs/google-play-signing-setup.md`。

## 结论先行

当前方案的方向是对的，但实施顺序需要调整为：

1. 先锁定 Global APK 的包名、功能边界、签名证书和 Google Play App Signing 证书。
2. 再固定下载接口和发布元数据合同。
3. 再切换英文站下载入口和公开文案。
4. 最后同步隐私、主体、账号删除、服务条款和支持页面。

现在不能把英文站标为 Global Beta。英文站实际下载的是中国版 APK，且下载接口把统计写入和文件跳转绑定在一起。若先改页面文案，用户仍会下载错误渠道，升级和隐私承诺都会失真。

以下“只新增审计文件、未执行构建”的表述是原始审计阶段记录；当前已完成上方列出的本地构建和签名链准备，仍未执行网站、后端或应用商店生产发布。

## 现状证据

### 英文首页和更新日志

英文首页 `https://tinyvow.rorolo.com/en/` 当前保持既有视觉结构，包含 Hero、产品三段、Growth、Voices、Feedback、下载区和页脚。桌面端和 390 x 844 移动端抽样均能正常渲染，暂不建议重做视觉体系。

但两个英文下载入口都指向：

```text
/api/downloads/tinyvow-cn-1.3.1-vc10-release.apk
```

对应源码：

- `tinyvow-site/app/release.ts:1-10`
- `tinyvow-site/app/home-page.tsx:25-36,71-74`
- `tinyvow-site/app/release-history.ts:11-17`
- `tinyvow-site/app/release-history-page.tsx:14-35`

英文更新日志还明确写着“download a previous China-build APK”，历史版本也全部是 `tinyvow-cn-*`。因此英文站不是“Global Beta 页面缺少几段文案”，而是渠道源文件和发布模型仍然只有中国版。

Growth 当前从 `/api/stats` 读取真实数字，Voices 使用两个故事占位卡。它没有 Public Beta 编辑状态，也没有“样本不足时不显示数字”的产品规则。

### 公开页面状态

| URL | 当前状态 | 备注 |
| --- | ---: | --- |
| `/` | 200 | 中文首页 |
| `/en/` | 200 | 英文首页 |
| `/privacy/` | 200 | 中文隐私政策 |
| `/en/privacy/` | 200 | 英文隐私政策 |
| `/account-delete/` | 200 | 中文账号删除 |
| `/en/account-delete/` | 200 | 英文账号删除 |
| `/changelog/` | 200 | 中文更新日志 |
| `/en/changelog/` | 200 | 英文更新日志，内容仍以 China build 为主 |
| `/en/terms/` | 404 | 尚未创建 |
| `/en/support/` | 404 | 尚未创建 |
| 任意未知路径 | 404 | 当前为 nginx 默认 `404 Not Found`，不是品牌化 404 |

`tinyvow-site/scripts/export-static.mjs:7-17` 也只导出当前八个静态页面，没有 Terms、Support 或自定义 404 条目。

### Growth 数字的审计污染

第一次读取英文首页时，公开接口显示 `totalDownloads=3`、`totalMembers=1`，与需求中“1、0”的假设不一致。为确认合法下载请求的 HTTP 行为，本次审计对有效下载 API 发出了 `GET`、`HEAD`、`Range` 探测；当前接口会把三种请求都计入下载统计。之后公开接口至少显示为 `totalDownloads=8`、`totalMembers=1`。

这几个数字不能作为真实增长基线，也不应在生产数据库中自动回写或“修正”。后续实施前应由运营负责人决定是否标记该日期为测试污染、重新开始新的 Public Beta 统计窗口，或仅在页面进入 Public Beta 模式时隐藏旧数字。本审计没有重置或删除任何统计数据。

## APK 和渠道审计

### 当前英文站 APK

本地归档和官网 `public/downloads/` 中的文件一致：

```text
tinyvow-cn-1.3.1-vc10-release.apk
```

实测元数据：

| 项目 | 结果 |
| --- | --- |
| 文件大小 | 25,825,604 bytes |
| SHA-256 | `8F70F743578F7B5FA3212018BB569F3ECFB4794B5D51B81F8F4929A0C291793B` |
| application ID | `com.rrrrz.tinyvow.cn` |
| versionName | `1.3.1-cn` |
| versionCode | `10` |
| minSdk | 26（Android 8.0） |
| targetSdk | 36 |
| debuggable | `false` |
| 签名证书 SHA-256 | `82501e80068cece9df2380cc0998f02803ee02a7bba2cd97a7f90abac86e0326` |
| 签名主题 | `CN=Tiny Vow China Release, OU=Internal Testing, O=Tiny Vow, ...` |
| 签名方案 | 仅 v2；v1/v3/v3.1/v4 未启用 |

本地 `dist/tinyvow-cn-1.3.1-vc10-release.apk` 与官网副本 SHA-256、文件大小一致。这个事实只证明“网站当前文件和国内归档一致”，不能证明它适合海外分发。

### 中国版专属能力已确认存在

APK 的 manifest、DEX 和资源中包含：

- `com.rrrrz.tinyvow.cn` 和 `STORE_CHANNEL=china`
- `https://api.tinyvow.rorolo.com`
- 匿名账号、订单、账号删除和会话接口
- `TVA1`、`TVB1`、`local_activation` 等激活标识
- 支付宝 SDK、支付入口和相关资源
- 国内账号、邮箱、激活码和支付宝文案

源码证据：

- `tinyvow/app/build.gradle.kts:110-124`：China application ID、后端地址和激活开关
- `tinyvow/app/build.gradle.kts:210`：支付宝仅加入 `chinaImplementation`
- `tinyvow/app/src/china/java/com/rrrrz/tinyvow/data/payment/PlatformAlipayPaymentLauncher.kt:8-13`
- `tinyvow/app/src/main/java/com/rrrrz/tinyvow/data/activation/ActivationModels.kt:6-30`
- `tinyvow/app/src/main/java/com/rrrrz/tinyvow/data/activation/ChinaSubscriptionRepository.kt:29-45`
- `tinyvow/app/src/main/java/com/rrrrz/tinyvow/data/server/TinyVowBackendApi.kt:104-120`

### 渠道矩阵

| 目标渠道 | 当前实现 | 目标定义 | 当前结论 |
| --- | --- | --- | --- |
| `china` | `china` flavor，`.cn` 包名，国内后端、激活码、支付宝 | 保留现状 | 可继续作为中国版 |
| `global` | 不存在 | 同 Play 包名的官网侧载 Beta；不含中国账号、激活、支付宝和中国后端 | P0 阻塞 |
| `play` | 没有名为 `play` 的 flavor | Google Play AAB，Play Billing 仅在商店交付 | 当前内部名为 `googlePlay` |
| `googlePlay` | `com.rrrrz.tinyvow`，开启 Google 登录/Play Billing | 作为 Play 交付 flavor 保留，是否改名需评估兼容成本 | 不能直接当 Global Beta |

现有 `googlePlay` 配置在 `app/build.gradle.kts:98-108` 中启用了：

- `ENABLE_GOOGLE_LOGIN=true`
- `ENABLE_PLAY_BILLING=true`
- `ENABLE_LOCAL_ACTIVATION=false`
- `TINYVOW_BACKEND_BASE_URL=""`

由于当前 `TINYVOW_GOOGLE_WEB_CLIENT_ID` 为空，Google 登录会降级；侧载环境中的 Play Billing 也可能只显示不可用。建议对外把它称为 `play` 渠道，但暂不为改名而重构既有 Gradle 任务；可新增 `global` flavor，并保留内部 `googlePlay` 标识直到迁移完成。

### 包名和升级结论

中国版的 `com.rrrrz.tinyvow.cn` 不能升级为 `com.rrrrz.tinyvow`，也不能保证跨包名导入本地数据。英文站误发中国版后，不能用“覆盖安装”承诺迁移。

Global Beta 与 Play 版本必须同时满足：

1. application ID 都是 `com.rrrrz.tinyvow`。
2. Global APK 的签名证书等于 Play Console 的 **App signing certificate**，不是仅仅等于本地 upload key。
3. Play 发布的 versionCode 高于任何已分发的 Global Beta versionCode。
4. Room 数据库名、DataStore 名和历史兼容标识保持不变。

本机现有 JKS 的证书摘要与历史 `dist/tinyvow-googleplay-1.0.2-vc3-release.aab` 一致，但这不能证明 Play Console 重新签发的 APK 使用同一证书。发 Global Beta 前必须在 Play Console > App integrity 取得并记录：

- App signing certificate SHA-256
- Upload certificate SHA-256
- 已上传或已发布的最高 versionCode
- `com.rrrrz.tinyvow` 是否已经注册

若 App signing certificate 不是 `82501e...e0326`，不能用当前 JKS 签出的同包名 APK 对外承诺可平滑升级。不要通过卸载重装解决签名冲突。

## 下载接口审计

### 当前请求链

当前链路是：

```text
英文页面
  -> /api/downloads/{fileName}
  -> 官网 Worker 或 nginx 代理
  -> backend /v1/web/downloads/{fileName}
  -> 写入 website_download_days
  -> 302 Location: /downloads/{fileName}
  -> nginx 静态文件
```

源码证据：

- `tinyvow-site/worker/index.ts:38-58`：Sites 侧代理所有 `/api/` 请求，并强制 `Cache-Control: no-store`
- `tinyvow-site/server/rorolo.conf:86-97`：官方域名把 `/api/` 直接代理到后端
- `tinyvow-backend/src/main/kotlin/com/rrrrz/tinyvow/backend/controller/WebController.kt:122-127`：下载方法固定返回 302
- `tinyvow-backend/src/main/kotlin/com/rrrrz/tinyvow/backend/service/WebStatisticsService.kt:24-45`：先写计数，再生成静态 URL

### 实测响应

对不会写入计数的静态文件和无效文件进行的请求结果：

| 请求 | 状态 | 关键响应 |
| --- | ---: | --- |
| `HEAD /downloads/tinyvow-cn-1.3.1-vc10-release.apk` | 200 | `Content-Type: application/octet-stream`；`Content-Length: 25825604`；`Accept-Ranges: bytes` |
| `GET Range: bytes=0-0 /downloads/...apk` | 206 | `Content-Type: application/octet-stream`；`Content-Length: 1`；`Content-Range: bytes 0-0/25825604` |
| `GET /api/downloads/not-a-release.apk` | 404 | JSON `{"error":"download_not_found","message":"Download not found"}` |
| `HEAD /api/downloads/not-a-release.apk` | 404 | JSON content type，无文件实体 |
| 路径穿越编码请求 | 404 | nginx 不返回目标文件 |

静态 APK 响应目前没有明确的：

- `Content-Type: application/vnd.android.package-archive`
- `Content-Disposition: attachment; filename="..."`
- `Cache-Control`（当前主要依赖 nginx 默认行为）

合法 API 文件请求的 `GET`、`HEAD`、`Range` 都观察到 302；由于后端在 302 前写计数，`HEAD` 和断点探测也会被算作下载。302 本身不是文件实体响应，不能向客户端提供最终 APK 的长度、类型和 SHA-256。

### 当前接口风险

| 优先级 | 风险 |
| --- | --- |
| P0 | HEAD、Range、自动重试和机器人探测会污染下载量 |
| P0 | 统计数据库写失败会阻止 302，导致“计数失败即无法下载” |
| P1 | 后端只用 `tinyvow-cn-...` 正则，不支持 Global 文件名，也没有以发布清单为准的 allowlist |
| P1 | 没有下载接口级别的明确限流；反馈服务的旧 Python 接收器限流不能替代下载限流 |
| P1 | Worker 和 nginx 的缓存头行为不一致，官方域名与 Sites 可能出现不同合同 |
| P1 | 静态目录没有明确 APK MIME、Content-Disposition、Cache-Control 策略 |
| P2 | 失败响应需统一 JSON schema、真实状态码和 `Retry-After`（限流时） |

### 建议的最小接口合同

不要让动态 API 直接承担文件读取和计数两个职责：

1. 发布流程生成只读的 release manifest，并把文件复制到固定的 `public/downloads/`。
2. 页面最终下载链接指向 allowlist 中的静态 APK；静态响应负责 `200/206`、MIME、长度、断点和缓存。
3. 若需要统计，使用单独的 `POST /api/download-events`，只接受已发布的 release ID；统计写入采用 best-effort，失败返回不影响静态下载。
4. `GET`、`HEAD`、`Range` 不得写下载统计。
5. API 不接受任意文件路径；文件名必须从 manifest allowlist 解析，路径规范化后仍需位于下载根目录。
6. 对事件端点增加温和限流（例如按短时匿名速率键限制，429 + `Retry-After`），并明确说明临时限流键和日志保留边界。
7. 官方 nginx 和 Sites Worker 复用同一份响应合同和测试矩阵。

如果运营确实要求“下载请求数”而不是按钮点击数，应把指标名称改为“download starts / requests”，并明确它不是去重安装数，也不是实际安装数。

## 法律主体、联系和隐私事实

### 主体和邮箱

当前英文页脚、隐私页、账号删除页只有 `Rorolo` / `by Rorolo`，英文隐私和账号删除还公开：

```text
rrrr.zhao@qq.com
WeChat: rourourenren222
```

对应源码：

- `tinyvow-site/app/en/privacy/page.tsx:12-16`
- `tinyvow-site/app/en/account-delete/page.tsx:12-16`
- `tinyvow-site/app/legal-shell.tsx:24-50`
- `tinyvow/docs/privacy.html:42,71`
- `tinyvow/docs/account-delete.html:48-50`
- `tinyvow/docs/google-play-release-checklist.md:12-13`

英文页面尚未声明：

```text
Tiny Vow is developed and operated by 青柠软件开发工作室.
```

在营业执照名称最终核对前，不应擅自翻译或创造英文法定名称。建议集中配置：

- `operatorNameZh = "青柠软件开发工作室"`（待营业执照逐字确认）
- `brandNameEn = "Rorolo"`
- `productNameEn = "Tiny Vow"`
- `supportEmail = "support@rorolo.com"`

该配置应有一个应用仓库内容源，并由官网发布脚本同步/校验；网站、应用文档和官网副本不能各自维护不同主体。建立邮箱、DNS、SPF/DKIM/DMARC 和收发测试属于发布前外部准备事项，本审计未代为创建账号或修改 DNS。

英文页面只保留 `support@rorolo.com`。QQ 和微信可以保留在中文 Support 页面或作为中文备用渠道，但不能继续作为英文页的主联系方式。

### 当前隐私叙事的问题

英文隐私政策把 China edition、激活码、支付宝和中国后端放在主要数据段落中，缺少清晰的 Global edition 边界。它还声称下载统计不保存 IP，但当前实际部署的 nginx/上游日志保留策略未在本地仓库得到验证；这项声明不能在确认基础设施日志边界前继续扩大。

网站反馈实际路径是 `/api/feedback` -> backend `WebController` -> `website_feedback` 表。旧的 `server/feedback_server.py` 仍存在并有单独的 `/feedback` nginx 配置和内存限流，但当前 React 表单并不调用该路径，不能把旧接收器当作当前事实源。

当前后端反馈表保存：

- 分类、内容、提交时间、语言
- 用户主动填写的联系方式
- 登录提交时的 user ID、邮箱、显示名、会员状态快照

当前迁移没有看到自动过期任务；“处理完或不再需要时删除”的公开文案需要对应人工流程或保留期实现，不能当成已经自动执行。

### `PRIVACY_DATA_MAP.md` 应覆盖的事实

建议把 `E:\Project\tinyvow\docs\PRIVACY_DATA_MAP.md` 作为跨仓库事实源，官网发布副本只从它同步。至少按下表分类，并为每一行记录“数据项、来源、用途、存储位置、保留期、删除方式、第三方处理方、证据文件/版本”：

| 分类 | 当前事实 |
| --- | --- |
| All editions | Room 分组、关联 App、UsageStats 前台 session、归档、积分 ledger、奖励、成就、阻断事件、主题、专注、签到、媒体/特殊应用本地数据；默认设备本地 |
| All editions | DataStore 偏好、语言、权限 disclosure 状态、备份/导出文件、导入图标；由用户导出或清理 |
| Optional account features | 国内账号资料、邮箱验证码哈希、密码哈希、头像、设备会话、登录历史、权益、订单、激活记录；仅用户选择账号/会员能力时 |
| Global edition | 目前没有可核对的 Global flavor；在 Global APK 完成二进制扫描和网络边界验证前，不得声称“无账号/无后端”或“支持某项 Global 专属服务” |
| China edition | 国内匿名会话、设备凭据哈希、账号、服务端激活码、支付宝订单和 Pro 权益；后端不应接收 local-first 使用数据 |
| Website feedback | `/api/feedback` 的表单字段和登录账号快照；当前数据库无自动过期证据，需补保留/删除流程 |
| Payment providers | Google Play Billing 由 Google 处理订阅/支付；中国版支付宝由支付宝和国内后端处理订单确认；Tiny Vow 仅保留必要订单/权益审计字段 |
| Website download telemetry | 当前 `website_download_days` 按 release 和日期累计，但 HEAD/Range 也计数；基础设施是否记录 IP、日志保留多久仍需核实 |
| WeRead / media providers | 用户主动启用时与对应第三方接口或 MediaSession/通知状态交互；Key 使用 Android Keystore，本地数据不应上传 Tiny Vow 后端 |

## 风险登记

| 优先级 | 风险 | 影响 | 发布闸门 |
| --- | --- | --- | --- |
| P0 | 英文站分发 China APK | 错误后端、账号、支付和隐私边界；无法升级到 Play | Global APK 通过全部扫描后才能切链接 |
| P0 | Global flavor 不存在 | 无法证明渠道隔离 | 新 flavor + 独立构建任务 + 二进制证据 |
| P0 | Play App Signing 指纹未知 | 可能无法从官网 APK 升级到 Play | Play Console App integrity 截图/记录 |
| P0 | 下载统计与下载实体耦合 | 统计故障会阻断下载，HEAD/Range 污染数字 | API/静态文件合同和异常测试通过 |
| P1 | 英文法律页无完整运营主体 | 主体识别和商店合规风险 | 营业执照逐字确认 + 集中配置 |
| P1 | 英文页公开 QQ/微信 | 联系渠道和隐私叙事不符合海外目标 | support@rorolo.com 收发验证 |
| P1 | 隐私政策 China-first 且反馈保留期未落地 | 公开承诺与代码不一致 | `PRIVACY_DATA_MAP.md` 与代码/后端核对 |
| P1 | 英文更新日志仍下载 China 历史包 | 用户可能从历史页继续装错渠道 | 按渠道拆分 release history |
| P1 | 静态 APK 响应头不完整 | 下载器、浏览器和断点续传兼容性差 | 200/206/HEAD/失败矩阵 |
| P1 | Terms、Support、品牌化 404 缺失 | 法律链接和安装排障闭环不完整 | 两语言路由和静态导出检查 |
| P2 | Growth/Voices 样本不足仍展示指标/占位故事 | 空洞数字或类似虚构社会证明 | Public Beta editorial mode |

## 优化后的实施顺序

### 阶段 0：事实冻结（先于任何页面修改）

需要负责人确认并写入发布记录：

1. 营业执照上的运营主体逐字名称；在确认前只允许使用待确认的中文值，不得造英文法定名。
2. `support@rorolo.com` 的邮箱服务商、DNS、SPF/DKIM/DMARC 和实际收发测试。
3. Play Console 的 app ID、App signing certificate、Upload certificate 和最高 versionCode。
4. Global Beta 是否完全无账号/无会员购买，还是先沿用 Play 代码但把购买入口关闭。建议 Global Beta 使用无中国后端、无激活码、无支付宝、无 Play Billing 的 `Noop` 权益状态。
5. 下载统计的可信起点。当前 2026-07-23 数字已被接口探测污染，不应直接用于 Public Beta 社会证明。

### 阶段 1：建立渠道合同

应用仓库保留现有兼容标识，不修改 Room 数据库名、DataStore 名和包名规则：

- `china`：`com.rrrrz.tinyvow.cn`，国内后端、激活码、支付宝。
- `global`：`com.rrrrz.tinyvow`，官网侧载 APK，无中国后端、账号、激活码、支付宝和 Play Billing。
- `googlePlay`（对外称 `play`）：`com.rrrrz.tinyvow`，AAB 和 Play Billing，仅 Play 交付。

中国账号、激活、支付宝实现和资源应从 `main` 源集移入 flavor/provider 边界，或确保 R8 后完全不进入 Global 二进制。Global 构建必须做 DEX、资源、manifest 和网络常量扫描，不能只依赖运行时 `BuildConfig` 条件。

### 阶段 2：发布产物和机器元数据

在应用仓库新增或扩展本地打包脚本，生成：

```text
dist/tinyvow-global-{versionName}-vc{versionCode}-release.apk
```

发布脚本从 APK 自动提取并写入不可手填的 manifest：

- channel：`global-beta`
- versionName、versionCode、application ID
- minSdk / Android 最低版本
- 精确字节数和展示大小
- 发布时间（由发布命令参数或 CI 时间生成）
- APK SHA-256
- 签名证书 SHA-256
- 签名方案
- 对应英文更新日志
- 权限说明链接、隐私链接、Support/Terms 链接

脚本必须在复制到官网前失败于以下任一条件：包名不对、文件名不对、versionCode 不单调、证书不匹配、存在 China URL/激活标识/支付宝 SDK、SHA 或文件大小无法生成。

### 阶段 3：重做下载合同，不重做下载视觉

推荐把下载分成“静态文件”和“可选统计事件”：

- 静态 `/downloads/{allowlisted-file}`：负责实际 `200`、`206`、`HEAD`、MIME、Content-Disposition、Content-Length、Accept-Ranges、Cache-Control。
- `POST /api/download-events`：只记录已发布 release ID 的按日聚合；写失败不影响静态文件。
- 不再让 `GET`、`HEAD` 或 `Range` 写数据库。
- 失败返回真实 4xx/5xx JSON；不要用 200/OK 文案包装失败。
- 对事件端点做温和限流，普通用户不受影响，429 携带 `Retry-After`。
- 官方 nginx 和 Sites Worker 必须共享 allowlist/manifest 语义，并各自通过同一 HTTP 矩阵。

下载详情弹窗或专用 `/en/download/` 页面只读取 manifest，展示渠道、版本、大小、SHA、证书指纹、更新日志、未知来源安装说明、权限链接、隐私链接和 Support/Terms。详情内容不能再手工复制 `release.ts` 中的数字。

### 阶段 4：最小页面增量

在现有组件和 token 上做增量修改：

1. 下载区增加 “Global Beta” 标签和详情入口。
2. 下载附近加入简短 How it works 三步：
   - Choose apps
   - Set boundaries or goals
   - Earn rewards and review progress
3. 下载附近加入权限透明说明：Usage Access、Accessibility Service、Notifications、Battery optimization，并链接 Support 的对应锚点。
4. 增加 `publicBetaMode` 编辑配置。当统计样本不足或没有明确同意的真实反馈时，Growth 显示 Public Beta 招募，不显示 1/0、8/1 等空洞数字；Voices 显示招募和反馈入口，不放虚构评论或 Story slot。
5. 数据量和反馈达到负责人设定的门槛、且每条反馈有来源/日期/版本和明确授权后，才关闭 Public Beta mode。
6. 中文首页保持中文文案和 China 下载；英文历史页只列 Global/Play 可用产物，不再让英文用户下载 China archive。

### 阶段 5：法律、隐私和支持闭环

先更新应用仓库内容源，再同步官网副本：

- 集中配置运营主体、品牌、产品名和支持邮箱。
- 英文隐私政策按 `All editions`、`Optional account features`、`Global edition`、`China edition`、`Website feedback`、`Payment providers` 分段。
- 生成并审查 `tinyvow/docs/PRIVACY_DATA_MAP.md`。
- 更新账号删除页，区分 Global 无账号状态、China 账号删除和 Google Play 自有支付记录。
- 新增 `/en/terms/`、`/en/support/`，并为语言切换补齐 `/terms/`、`/support/`。
- Support 至少覆盖 APK 安装、未知来源、Usage Access、Accessibility、电池优化、限制失效排查、诊断导出、账号/密码、删除账号和 support@rorolo.com。
- 只在中文 Support 页面保留 QQ/微信备用渠道。

### 阶段 6：本地验收和 Play 预演

不触碰真实用户设备数据，优先使用模拟器/专用测试设备：

- `npm test`
- `npm run lint`
- Android Global release 构建、静态扫描、`apksigner verify`、SHA 校验
- 英文/中文首页桌面和移动端
- 200、206、HEAD、Range 中断续传、非法路径、缺失文件、统计失败、限流
- APK 完整下载后 SHA-256 和证书指纹
- 全部法律链接、语言切换、Terms、Support、404
- Global Beta 覆盖安装到同包名下一版本（不卸载、不清数据）
- Play internal track 安装/升级预演，确认 Play App signing 证书和 versionCode

真实设备安装仍需遵守应用仓库的“只允许同包名同签名原地升级、禁止卸载和清数据”规则。官网和后端生产发布不在本阶段授权范围内。

## 拟修改文件

### Android 仓库：`E:\Project\tinyvow`

实施确认后预计涉及：

- `app/build.gradle.kts`
- `app/src/main`、`app/src/global`、`app/src/googlePlay`、`app/src/china` 的渠道 provider/资源边界
- `tools/package-release-artifacts.ps1`
- 新增 Global 打包、manifest 生成和 APK 扫描脚本
- `docs/release.md`
- `docs/prelaunch-optimization.md`
- `docs/google-play-release-checklist.md`
- `docs/privacy.html`
- `docs/account-delete.html`
- 新增 `docs/PRIVACY_DATA_MAP.md`
- `CHANGELOG.md`

不应修改：`com.rrrrz.tinyvow`、`com.rrrrz.tinyvow.cn` 的既有兼容性意图、Room 数据库名、DataStore 名、正式签名材料和历史文件名。

### 官网仓库：`E:\Project\tinyvow-site`

实施确认后预计涉及：

- `app/release.ts`（改为生成 manifest 的消费层）
- 新增 `app/legal-config.ts` 或生成配置
- `app/home-page.tsx`
- `app/growth-stats.tsx`
- `app/site-copy.ts`
- `app/release-history.ts`
- `app/release-history-page.tsx`
- `app/legal-shell.tsx`
- `app/en/privacy/page.tsx`
- `app/en/account-delete/page.tsx`
- 新增 `app/en/terms/page.tsx`、`app/en/support/page.tsx` 及中文对应路由
- `scripts/export-static.mjs`
- `scripts/publish-release.ps1`
- `worker/index.ts`
- `server/rorolo.conf`
- `tests/rendered-html.test.mjs` 及下载/路由测试
- `public/downloads/` 中由应用仓库提供的 Global APK 和 manifest

不应做：重建首页、替换现有宣传图、另起一套视觉系统、在官网自行重签或修改 APK。

### 后端仓库：`E:\Project\tinyvow-backend`

若采用独立统计事件端点，预计涉及：

- `WebController.kt`
- `WebStatisticsService.kt` 或新下载事件 service
- 统一错误响应和限流组件
- 对应 controller/service 测试
- 只有确有新数据字段时才新增 Flyway migration，并同步隐私数据图

本阶段不部署后端、不运行生产 migration、不重置下载统计。

## 验收矩阵

### 渠道和升级

- 英文首页只下载 `tinyvow-global-{versionName}-vc{versionCode}-release.apk`。
- 中文首页只下载 `tinyvow-cn-{versionName}-vc{versionCode}-release.apk`。
- Global application ID 为 `com.rrrrz.tinyvow`。
- Global APK 不含中国后端 URL、激活码标识、支付宝 SDK/组件或 China channel 常量。
- Global APK 与 Play App signing certificate 相同。
- Play versionCode 高于已分发 Global Beta。
- 不需要卸载即可从 Global Beta 更新到同包名兼容版本。

### HTTP

| 场景 | 期望 |
| --- | --- |
| `GET` 正常下载 | 200，正确 MIME、attachment、长度、缓存策略 |
| `HEAD` | 200，同一元数据，不计数 |
| `GET Range` | 206，Content-Range、长度正确，可续传 |
| 中断后续传 | SHA-256 与发布 manifest 一致 |
| 未知 release | 404 JSON，不暴露文件系统 |
| 路径穿越/编码路径 | 400 或 404，不读取任意文件 |
| 统计写失败 | 文件仍可下载 |
| 超过限流 | 429 + Retry-After，正常用户不受影响 |
| 统计成功/失败 | 不改变下载实体的状态码或文案 |

### 页面、法律和隐私

- 桌面、移动英文首页保留现有结构和截图风格。
- How it works、权限说明和 Download details 可见且不遮挡主下载操作。
- Growth/Voices 在样本不足时只显示 Public Beta 招募，不显示虚构数字或评论。
- `/en/terms/`、`/en/support/`、中文对应页、Privacy、Delete account、Changelog 全部 200。
- 英文页展示 `青柠软件开发工作室` 和 `support@rorolo.com`，不伪造英文法定名称。
- 中文页可展示 QQ/微信备用渠道，英文页不以它们作为主支持入口。
- Privacy、Data map、应用内 disclosure、Play Data safety 和后端实际字段一致。
- 404 页面有可理解的返回官网和语言切换入口。

## 当前不执行的事项

- 不重写首页或替换现有视觉设计。
- 不构建、签名、上传或发布 Global APK。
- 不把 China APK 改名后当作 Global APK。
- 不修改生产数据库中的下载计数，也不删除本次审计产生的记录。
- 不部署官网、Sites、后端或 DNS/邮箱。
- 不创建服务条款、隐私承诺或英文法定名称的未经核验版本。

## 进入实施阶段前的确认项

请先确认以下五项，再开始代码修改：

1. 营业执照上的运营主体完整中文名称是否逐字为“青柠软件开发工作室”。
2. Play Console App signing certificate SHA-256、Upload certificate SHA-256 和最高 versionCode。
3. Global Beta 是否采用“无中国账号/激活/支付、Noop 会员状态”的范围。
4. `support@rorolo.com` 的邮箱服务和实际收发是否已经准备好。
5. 是否接受将当前被探测污染的 Growth 数据从 Public Beta 统计窗口中排除或隐藏。

确认后，按“阶段 1 -> 阶段 2 -> 阶段 3 -> 阶段 4/5 -> 阶段 6”的顺序实施；在阶段 1-3 通过前，不切换英文首页下载链接。
