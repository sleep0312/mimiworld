# 米米世界 (MimiWorld)

> 以「米」为主题的 Minecraft Forge 模组：完整的稻米产业链、9 级装备科技树、2 个独立维度与 8 个 Boss。

A rice-themed Minecraft Forge mod featuring a rice industry chain, a 9-tier gear progression, 2 dimensions and 8 bosses.

> **项目状态：测试版（Beta）**
>
> 本模组仍处于开发测试阶段：部分功能尚未完成或仍在调整，数值平衡、配方与掉落可能随时变动；纹理与模型大多为初版，尚未进行精细化打磨。
>
> 当前版本不建议用于长期存档或正式服务器。遇到问题或有改进建议，欢迎通过 GitHub Issues 反馈。

## 基本信息

| 项目 | 内容 |
| --- | --- |
| 模组 ID | `mimiworld` |
| 版本 | 1.0.2 |
| Minecraft | 1.20.1 |
| Forge | 47.4.10+ |
| 必需前置 | [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib) 4.7+、[Curios API](https://www.curseforge.com/minecraft/mc-mods/curios) 5.14+ |
| 侧别 | 客户端与服务端均需安装 |

## 内容概览

- **农业与材料**：米树、米草、米矿与米米平原；从「米 → 熟米 → 米团」的加工链，到饭晶 → 超结晶 → 饭晶合金 → 超重结晶 → 复合米饭 → 清洁复合 → 超懒合金 → 最懒天堕 → 残堕的 9 级装备材料科技树。
- **机器与烹饪**：造米机、锤米台、电离台、沉淀台、离解台、碾磨台、杵界台、凝锭台、淬剑台、汲血台、搅碎器、搅拌器、大煮锅、巨无霸大大大煮锅、铁锅、烤箱、炉灶、堕落台等。
- **生物与 Boss**：47 种实体、38 枚生物蛋。8 个 Boss：米米猴王、饭晶虎王、超晶牛王、重晶兔王、复晶天青蟒、清洁长命夔、最懒终末龙、第八宗罪龙。
- **维度与结构**：米米维度（米米平原）与残堕界（残堕荒原）两个维度；米米宝藏结构含 10 种固定怪物变体及其对应宝箱。
- **战斗与饰品**：自定义伤害类型、药水效果与箭矢；Curios 圣遗物槽位与神遗之书槽位；可放置的 Boss 头颅摆件。
- **模型与动画**：63 个 GeckoLib 模型、46 个动画文件，均由 Blockbench 制作。

## 安装

1. 安装 Minecraft 1.20.1 与 Forge 47.4.10+。
2. 将 **GeckoLib**、**Curios API** 与本模组 `mimiworld-<版本>.jar` 一同放入 `.minecraft/mods`。
3. 启动游戏；联机服务器的 `mods` 目录同样需要这三个 jar。

## 开发

- 构建：`gradlew build`；运行开发客户端：`gradlew runClient`。
- 配方、战利品表与标签由 `src/main/java/.../datagen` 生成，修改后需重跑 DataGenerators。
- 资源目录：`src/main/resources/assets/mimiworld/`。

## 许可证

本项目采用分许可策略：

| 内容 | 许可 | 文件 |
| --- | --- | --- |
| 源代码（Java、Gradle 脚本等） | **MIT** | [LICENSE](LICENSE) |
| 美术与音频资源（贴图、模型、动画、语言文件等） | **CC BY 4.0** | [LICENSE-ASSETS](LICENSE-ASSETS) |

使用本模组资源时，请按 CC BY 4.0 要求署名：**MimiWorld（米米世界）**。
