# S01 宣传图生成规范

> 当前确认基线：S01 v7，2026-07-18  
> 适用输出：Google Play、国内 Android 应用商店中文首图

## 1. 固定成图规则

- 画布：`1080 × 1920 px`，PNG，非透明背景。
- 目标阅读场景：手机商店截图缩略图；必须额外缩放到 `360 × 640 px` 复核。
- 中文字体：`Noto Sans SC`。
- 主标题：88 px；副标题：36 px。
- 标签分类：36 px；标签标题：34 px；标签说明：30 px。
- 关键宣传信息不得低于 29 px。只允许真机 UI 内部文字随截图等比缩小。
- 顶部不重复放产品小字、应用图标或品牌图标；商店详情页已经展示产品名和 App 图标。
- 底部不放品牌署名、隐私说明等小字。需要表达的卖点必须进入大标题或标签。
- App 界面必须使用真实真机截图，不生成、不重绘、不修改 UI 内容。

## 2. S01 v7 布局

- 主标题：`和手机好好相处`
- 副标题：`约定少一点，投入多一点，专注也被看见。`
- 标题区：左右各 `54 px`，`y=50 px`，主标题与副标题居中。
- 真机区：`x=116, y=318, w=848, h=1794`，在画布中水平居中；底部延伸到 `y=2112`，超出宣传画布。
- 原始截图：`screenshots/raw/zh-CN/S01_home_overview_zh-CN.png`，尺寸 `1080 × 2400`。
- 原始截图在真机区内等比缩放至 `848 px` 宽，并向上偏移 `92 px`；裁掉手机状态栏。
- 完整首页截图结构保留，底部导航仍存在于原图中；通过让真机区延伸到宣传画布之外，使导航栏自然落到 `1920 px` 画布下方，而不是在真机框内部裁掉。
- 成图底边不得出现真机框的底部圆角或人为截断线，视觉上应表现为首页仍在宣传画布下方继续。
- 标签行：`x=20, y=1442, w=1040, h=252`，三列等宽，间距 `18 px`；相较上一稿下移约 `30 px`，确保真机图里的“约定”和“投入”分组标题完整露出。
- 标签总宽比真机图宽 `192 px`，从真机图左右两侧各向外延伸 `96 px`。
- 标签覆盖首页分组第二行开始的位置，使用半透明白色卡面、左侧语义彩条、双层投影和底部环境阴影形成悬浮层次。

## 3. 三个标签的固定文案

### 约定

- 分类：`约定`
- 标题：`温和限额`
- 说明：`日 · 周 · 月`

### 投入

- 分类：`投入`
- 标题：`正向积累`
- 说明：`时长 · 积分`

### 专注

- 分类：`专注`
- 标题：`离线专注`
- 说明：`固定 · 自由`

## 4. 产品事实约束

- 当前宣传材料不得出现“步数投入”“步数积分”“运动步数获得积分”等能力描述。
- 后续 S01–S08、网站图和商店文案都按这一边界执行，除非产品负责人明确重新开放。
- 不生成虚假 App UI，不把未确认能力写进标签、标题或示意图。

## 5. 背景资产

- 当前背景：`generated/bg-paper-olive-portrait-v1.png`。
- 背景只提供纸感、色块和留白，不承载文字、Logo、手机或功能示意。
- 背景生成模式：`Generate`，用途分类：`ads-marketing`。

### 可复现提示词

```text
Create a premium 9:16 portrait background plate for a calm digital wellbeing mobile app store screenshot. Warm ivory handmade paper base with very subtle tactile grain. Add a large low-saturation warm coral translucent arc entering from the lower-left and a large muted sage-green translucent arc entering from the lower-right, with a few extremely fine curved line details that suggest balance and gentle progress. Preserve broad clean negative space across the top and center for Chinese typography, a real mobile UI screenshot, and feature labels. Quiet, trustworthy, editorial, mature, soft natural light, Japanese and Scandinavian restraint. No text, no letters, no logo, no app icon, no phone, no user interface, no device mockup, no people, no neon, no glossy 3D, no confetti, no busy pattern, no hard geometric grid, no high-contrast gradient.
```

## 6. 导出与检查

1. 用 `compositions/S01_home_zh-CN_v7.html` 以 `1080 × 1920` 视口导出 PNG。
2. 输出到：
   - `exports/google-play/google-play_01_home_zh-CN_1080x1920_v7.png`
   - `exports/cn-stores/cn-store_01_home_zh-CN_1080x1920_v7.png`
3. 检查两张图尺寸、哈希和视觉内容一致。
4. 额外生成 `360 × 640` 缩略预览，确认主标题以及“温和限额 / 正向积累 / 离线专注”仍可辨认。
5. 检查状态栏已裁掉；底部导航保留在原截图中但位于宣传画布外，成图底边不出现真机框底部圆角或导航栏。
6. 检查标签覆盖分组第二行开始的位置，且标签总宽大于真机图宽度。
