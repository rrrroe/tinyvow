# Tiny Vow 文案术语方案

本文档用于统一 Tiny Vow 的中英文产品文案。代码中的 `CONTROL` / `ENCOURAGE` 枚举、DAO 字段和历史数据字段不因此重命名；用户可见文案按本文档执行。

## 核心原则

- 两类入口词使用“约定 / 投入”。
- 两类结果词使用“守约 / 收获”。
- `CONTROL` 类型面向少用、限额、及时停下，中文叫“约定”，英文叫 “Vow”。
- `ENCOURAGE` 类型面向多做、目标时长、正向积累，中文叫“投入”，英文叫 “Focus”。
- “守约”只描述约定类型的完成状态和长期表现。
- “收获”只描述投入类型带来的结果，例如积分、达成、连胜和复盘。
- “分组 / group”只在结构性或技术性场景使用；普通 UI 尽量写成“约定 / 投入 / 项目”。
- 不再使用“节制”作为产品术语；它语义正确但不像 Tiny Vow 的主关键词。

## 中英文术语表

| 业务含义 | 中文 | English | 说明 |
| --- | --- | --- | --- |
| `CONTROL` 类型 | 约定 | Vow | 设置上限，防止过量 |
| `ENCOURAGE` 类型 | 投入 | Focus | 设置目标，鼓励投入 |
| 约定完成 | 守约 / 已守住 | Kept | 没有超过有效限额 |
| 约定超额 | 超时 / 超出约定 | Over limit | 阻断和统计中的超额状态 |
| 投入完成 | 有收获 / 已收获 | Gains / Gained | 达到目标或获得积分 |
| 投入未完成 | 暂无收获 / 还差一点 | No gains yet / Still short | 正向提醒，不责备 |
| 约定连胜 | 守约连胜 | Vow streak / Kept-vow streak | 不写“控制连胜” |
| 投入连胜 | 收获连胜 | Gains streak | 不写“鼓励连胜” |
| 积分 | 积分 | Points | 不混用“点数”“奖励点” |
| 统计总称 | 战报 | Report | 页面入口可叫“统计”，页面内偏“战报” |

## 类型说明

中文：

- 约定：给容易过量的应用设一个边界，到点时提醒自己停一下。
- 投入：给值得花时间的应用设一个目标，把有价值的时间变成收获。

English:

- Vow: Set a boundary for apps that are easy to overuse, then pause when the limit is reached.
- Focus: Set a goal for apps and activities worth your time, then turn useful minutes into gains.

## 常用页面文案

中文：

- 今日约定
- 今日投入
- 新建约定
- 新建投入
- 已守约
- 已超时
- 今日收获
- 暂无收获
- 还差 %1$d 分钟
- 剩余 %1$d 分钟

English:

- Today's vows
- Today's focus
- New vow
- New focus
- Kept
- Over limit
- Today's gains
- No gains yet
- %1$d min short
- %1$d min left

## 阻断页语气

中文推荐：

- 标题：这条约定已经超时了
- 正文：你为 %1$s 设置的限额已经用完。先离开一会儿，把时间留给更重要的事。
- 按钮：回到 Tiny Vow / 返回桌面 / 使用应急解锁

English recommended:

- Title: This vow is over limit
- Body: The limit for %1$s is used up. Step away for a moment and leave the time for what matters more.
- Buttons: Back to Tiny Vow / Go home / Use emergency unlock

## 统计与奖励

中文：

- 守约与收获复盘
- 守约成效
- 收获进度
- 约定
- 投入
- 守约节省
- 投入时长
- 收获积分
- 完成投入，获得 %1$d 积分

English:

- Vows and gains review
- Kept-vow results
- Gains progress
- Vow
- Focus
- Vow savings
- Focus time
- Gains points
- Reached focus and earned %1$d points

## 禁用和替换规则

- “控制分组 / control group” -> “约定 / vow”
- “守约分组 / commitment group” -> “约定 / vow”
- “节制 / restraint” -> “约定 / vow”，状态场景使用“守约 / kept”
- “鼓励分组 / encourage group” -> “投入 / focus”
- “鼓励进度 / encourage progress” -> “收获进度 / gains progress”
- “鼓励色 / encourage color” -> “投入色 / focus color”
- “节制色 / restraint color” -> “约定色 / vow color”
- “分组明细 / group details” -> “约定明细 / vow details”
- “目标/约定 / target / commitment” -> “目标/限额 / goal / limit”
