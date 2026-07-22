# Tiny Vow 商店宣传图统一生成规范

> 当前确认基线：S01–S07，2026-07-19  
> 适用渠道：Google Play、国内 Android 应用商店、`tinyvow.rorolo.com`  
> 中文产品名：小约定；英文产品名：Tiny Vow；品牌：揉揉喽 / Rorolo

## 1. 当前唯一推荐版本

| 图号 | 功能主题 | 中文构图 | 英文构图 | 中文版本 | 英文版本 |
| --- | --- | --- | --- | --- | --- |
| S01 | 首页总览 | `compositions/S01_home_zh-CN_v7.html` | `compositions/S01_home_en_v7.html` | v7 | v7 |
| S02 | 约定与投入 | `compositions/S02_control_encourage_zh-CN_v2.html` | `compositions/S02_control_encourage_en_v2.html` | v2 | v2 |
| S03 | 温和阻断 | `compositions/S03_block_overlay_zh-CN_v1.html` | `compositions/S03_block_overlay_en_v1.html` | v1 | v1 |
| S04 | 离线专注 | `compositions/S04_offline_focus_zh-CN_v4.html` | `compositions/S04_offline_focus_en_v4.html` | v4 | v4 |
| S05 | 手机使用追踪 | `compositions/S05_phone_usage_tracking_zh-CN_v3.html` | `compositions/S05_phone_usage_tracking_en_v3.html` | v3 | v3 |
| S06 | 奖励与成就 | `compositions/S06_rewards_achievements_zh-CN_v3.html` | `compositions/S06_rewards_achievements_en_v3.html` | v3 | v3 |
| S07 | 产品特性 | `compositions/S07_product_characteristics_zh-CN_v2.html` | `compositions/S07_product_characteristics_en_v2.html` | v2 | v2 |

旧版本只用于设计过程回溯，不再标记为“当前推荐”。

## 2. 所有图片共同遵守的硬规则

1. 母版固定为 `1080 × 1920 px`、竖版 `9:16`；同时导出 `360 × 640 px` 手机缩略预览。
2. 第一优先级是手机商店缩略图可读：主标题使用粗体大字；说明只保留一行或两行；禁止页顶小眉题、页底免责声明和密集小字。
3. 背景固定使用 `generated/bg-paper-olive-portrait-v1.png`，允许叠加低对比光晕、细线或纸张纹理，不更换为花哨场景。
4. App 界面必须来自真机截图。不得生成、重绘或拼造不存在的页面、按钮、报表和功能。
5. 中文图使用中文真机素材；英文图使用英文真机素材。英文界面中仍为中文的用户分组名、专注分类名和演示 App 标签，可在宣传素材副本中做确定性覆盖翻译，但不修改应用数据，也不暗示产品会自动翻译用户数据。
6. 所有营销用局部修图放在 `screenshots/edited/<language>/`；原始截图保留在 `screenshots/raw/<language>/`，不得覆盖原图。
7. 使用应用正式图标，不创造第二套 Logo 或品牌符号；正式产品名保持“小约定 / Tiny Vow”。
8. 状态栏不进入构图。底部导航若需要表达“页面延伸到画布外”，让它自然超出宣传图边界，不把它误删成页面残缺。
9. 同一张图中的同级卡片必须有明确网格：等高、等间距、统一内边距、统一标题层级。错位仅用于展示大面积真实页面，不能破坏基础对齐。
10. 不宣传“步数投入、步数积分、手动录入步数”等能力；当前宣传主线只展示 App 使用时长、约定、积分、离线专注、统计、兑换、成就和本地数据能力。
11. 当前数据不足，不展示月报和年报；报表宣传只使用日报和周报。
12. Google Play 图不出现国内激活码、支付宝、国内价格或限时促销；国内商店图不出现 Google Play 购买流程。

## 3. 七张图的固定构图规则

### S01 首页总览

- 标题和说明居中，首页真机图居中并放大。
- 真机图底部导航自然延伸到画布外。
- 三张悬浮标签横向覆盖首页第二行分组，总宽大于真机截图宽度；分别解释 Vow、Effort、Focus。
- 中文标签：约定 / 温和限额，投入 / 正向积累，专注 / 离线专注。
- 英文标签：Vow / Gentle limits，Effort / Positive progress，Focus / Offline focus。

### S02 约定与投入

- 上半区并列展示首页中的约定与投入分组，保持同一视觉比例。
- 中部两张等高机制卡：约定解释多 App 共享限额与日/周/月周期；投入解释阅读/学习时长持续获得积分与目标奖励。
- 底部用“少一点无意识消耗 / 多一点有意义投入”完成逻辑闭环。
- 不使用分组编辑页或 App 选择页作为主体。

### S03 温和阻断

- 使用完整真实阻断页，去除状态栏。
- 页面必须同时显示投入卡片、临时缓冲和返回桌面两个选择。
- 三张说明标签横向覆盖在真机图上：温和提醒、不丢记录、保留选择。
- 文案不能写成惩罚、锁死或强制戒断。

### S04 离线专注

- 两张大面积真机页面采用斜向错位叠放：进行中页面在后、日报专注收藏在前；保持页面足够大。
- 下方独立放周报“聚沙成塔”，表达一次一颗、一天一瓶、一周积累。
- 同类大页面放不下时沿用此方法：前后层级、轻微旋转、局部遮挡，但标题与核心数据必须完整可读。

### S05 手机使用追踪

- 固定为五张自带标题的真实报表卡片，不再添加重复小标题或总结卡。
- 卡片组成：关键数据、周报行为图谱散点、周报积分轨迹、周五日报时光刻痕、周报时光刻痕。
- 行为图谱只显示散点主体，不露出下方九宫格分类卡。
- 日报时光刻痕使用周五数据；周报时光刻痕使用周报页面。
- 卡片允许轻微叠放，但边缘、圆角和标题必须干净，不能留下大块无效上边缘。

### S06 奖励与成就

- 不使用实机截图，直接使用应用正式奖励图标和成就徽章。
- 固定六条等高横向轨道：上三行奖励、下三行成就；行间距相等，图标尺寸相等。
- 每行横向错位，左右边缘淡出，形成仍可横向滚动的丰富陈列感。
- 图标不放进额外方块容器；允许轨道背景使用低对比渐变。

### S07 产品特性

- 固定严格 `2 × 2` 等规格卡片网格，每张 `470 × 650 px`，横纵间距均为 `30 px`。
- 每张卡结构一致：编号分类、图标区、标题、单句说明、底部状态。
- 四个维度固定为：数据边界、规则保护、外观选择、数据管理。
- 不展示“四档文字大小”和“中英双语”；保留本地保存、超我模式、自定义三色主题、备份恢复清理。

## 4. 英文真机素材处理规则

- 英文营销副本由 `scripts/localize_en_screenshots.ps1` 生成。
- 当前处理的用户数据映射包括：视频→Video、购物→Shopping、乱刷→Scroll、复盘→Review、健身→Fitness、阅读→Reading、考证→Exam prep、散步→Walk、手帐→Journal。
- 映射只服务于宣传图，不进入 App 字符串资源、不写回数据库、不覆盖 `raw/en`。
- 系统或第三方 App 的品牌图标保持原样；若中文标签会在最终手机缩略图中形成明显视觉噪声，可在 `edited/en` 副本中改成常见英文名。

## 5. 背景提示词与负面提示词

背景只在需要重新生成纸张底图时使用以下提示词；App UI、文字、Logo、按钮和图标仍由真实素材与 HTML 确定性排版完成。

**背景提示词**

```text
Premium editorial paper background for a mobile app-store screenshot, portrait 9:16, warm ivory paper texture, soft olive green and muted coral translucent shapes, subtle botanical geometry, airy central reading area, refined wellness-tech mood, low contrast, gentle natural light, sophisticated and calm, no focal object, no text, no logo, no device mockup, no interface.
```

**负面提示词**

```text
no words, no letters, no typography, no logo, no app icon, no phone, no UI, no buttons, no people, no photorealistic room, no dark background, no neon, no heavy 3D, no busy pattern, no hard shadows, no high-contrast decoration behind text
```

**构图提示模板**

```text
Use the approved Tiny Vow 1080x1920 app-store system. Keep the warm paper background, centered large mobile-readable headline, one concise subtitle, and only real app screenshots or official in-app icon assets. Preserve equal card geometry and generous spacing. For large screenshots, use controlled diagonal overlap without shrinking the UI. Do not invent features, reports, UI, logos, or small disclaimer text. Verify the result at 360x640.
```

## 6. 导出与验收

Chrome 导出基准：

```powershell
& 'C:\Program Files\Google\Chrome\Application\chrome.exe' `
  --headless=new --disable-gpu --hide-scrollbars `
  --force-device-scale-factor=1 --window-size=1080,1920 `
  --screenshot='<absolute-output.png>' 'file:///<absolute-composition.html>'
```

每次变更后必须检查：

1. `1080 × 1920` 原图无裁切错误、无缺图、无字体溢出。
2. `360 × 640` 缩略图中主标题、核心标签和报表标题仍能辨认。
3. 英文 HTML 不含可见中文；英文真机图中的演示分组名已使用宣传副本本地化。
4. S05 的每张报表自有标题完整露出；行为图谱不出现九宫格。
5. S06 六行等高、等距、等图标尺寸；S07 四卡等尺寸、等间距。

