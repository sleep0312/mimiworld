# 超懒神翼胸甲 → 鞘翅式滑翔装备 实施计划

## Context（背景与目标）

T5 超懒神翼胸甲当前是 GeckoLib 自定义金甲（450 cubes，背后一对冷蓝白立体鹰翼），动画只有一个大幅扇翅循环。用户要求改成**类似鞘翅的装备**：

- 空中跳跃滑翔时播放现有大幅扇翅动画（已定名 `fly`，Blockbench 工程内已改名完成）；
- 地面待机时翅膀像鞘翅一样**收起贴背、翼尖顺背斜下**（新建 `idle` 动画）；
- 物品获得鞘翅滑翔能力，收/展平滑过渡。

已用 10 轮单骨旋转+缩放实验证实：现有翅膀是**单节骨 + 羽毛从一点扇形放射**，纯旋转无法折成鞘翅轮廓。用户已选定方案：**每片翼加一段中段关节骨，做二段折叠**（效果最接近鞘翅）。

Java 侧 API 已从 Gradle 缓存中的 Forge 47.4.10 / GeckoLib 4.8.4 源码逐一核实（见下）。

## 约束（用户既定规则）

- 几何/动画每轮改动必须截图发用户确认；**未确认不导出正式资源、不覆盖正式 bbmodel**。
- 左右翼镜像 maxDelta 必须为 0。
- 保存 bbmodel 时 `Codecs.project.compile({})` 返回对象只 `JSON.stringify` 一次（旧备份有双重编码 bug）。
- 不导出 gif。

---

## 第 1 步：Blockbench 几何改造（加二段关节）

工程当前在 Blockbench 应用内（含 `fly` + 空 `idle` 两动画）。若应用处于无项目状态，按既定恢复流程：`newProject(Formats.geckolib_model)` → 读 checkpoint（`c:\mimiworld\docs\blockbench\checkpoints\super_lazy_wings_chestplate_final_20261014.bbmodel`，双重 JSON 编码需二次 `JSON.parse`）→ `Codecs.project.parse(m)`。

1. **先存改造前备份**：`docs\blockbench\checkpoints\super_lazy_wings_chestplate_pre2segment_20261014.bbmodel`（单次 stringify）。
2. **新建中段骨**（脚本 risky_eval，Blockbench `new Group` + `addTo`）：
   - 右翼：`wingRightMid`，parent = `wingRight`；左翼：`wingLeftMid`，parent = `wingLeft`。
   - pivot 放在翼展约 1/3 处（先读翼骨 pivot 实际 origin 与最远翼尖坐标，按根→尖方向算出约 12 格处分节点；左翼取镜像）。
3. **迁移外段羽毛 cube**：遍历两翼 `children`（Cube），把 cube 中心沿"根→翼尖"方向投影距离超过阈值的（约外 2/3，即全部长飞羽+中段覆羽）用 `cube.addTo(midGroup)` 移到中骨；靠近翼根的短覆羽留在根骨。左翼用同一比例阈值镜像处理。cube 绝对坐标与 UV 不变，新骨静态 rotation = [0,0,0]，故展开外观与 fly 动画**零变化**。
4. **翼骨换父节点**（关键修正）：把 `wingRight`/`wingLeft` 的 parent 从 `bipedBody` 改为 `armorBody`。GeoArmorRenderer 只把身体姿态（走路摆动、滑翔俯冲）写到标准骨 `armorBody`（已核源码 `getBodyBone()` 与 `applyBaseTransformations`）；挂在 `bipedBody` 下滑翔时翅膀不会随身体压平。
5. **校验**：cubes 总数仍为 450；左右中骨 pivot、cube 数、镜像 maxDelta = 0；默认姿态 + fly t=0 截图与改造前对比无外观差异，**截图发用户确认**后才继续。

## 第 2 步：idle 二段折叠动画（迭代至用户确认）

在 `animation.super_lazy_wings_chestplate.idle`（loop，静态折叠，仅需 t=0 单帧）上调参，fly 动画关键帧一律不动：

- 翼根骨：绕 Y 向后折（已验证符号：右翼 +Y、左翼 −Y，约 80~90°），把翼整体转到背后；
- 中段骨：第二段反向折，让外段羽尖沿脊背斜向下，目标——背视呈梯形（比肩略宽、尖收到腰/膝附近）、侧视顺背后倾约 45~60°、不水平拖后、不交叉；
- 若二段折后整体仍偏大，再在两段骨上加对称 scale（备选，先不加）。
- 方法沿用已验证管线：`idle.playing=true; Animator.playing=true` → `Animator.preview()` → 读两段翼尖世界坐标 + 离屏视图背/侧/3-4 视角截图（机位 back `[0,28,120]→[0,23,4]`、side `[96,26,10]→[0,23,4]`、q3 `[-64,40,-60]→[0,22,4]`，渲染后 setTimeout 300ms 取图）→ 解码目检。
- 每轮把图存 `c:\mimiworld\docs\blockbench\wing_preview\` 并以 `computer://` 链接发用户，迭代到用户明确确认。

## 第 3 步：导出资源 + 存工程备份（用户确认姿态后）

1. Blockbench 导出 geo 与动画到：
   - `src\main\resources\assets\mimiworld\geo\super_lazy_wings_chestplate.geo.json`
   - `src\main\resources\assets\mimiworld\animations\super_lazy_wings_chestplate.animation.json`
   （动画用 geckolib 导出 compile/max_content_length=0 直接写盘；geo 导出名执行时用 `list_export_formats` 确认。贴图无变化不重导。）
2. 导出后 Read 校验：geo identifier 仍为 `geometry.super_lazy_wings_chestplate`；含 `wingRightMid/wingLeftMid` 且 parent 正确；cubes 总数 450；animation 含 `idle` + `fly` 且均 `"loop": true`，fly 关键帧与现值一致（0/0.8/1.6/2.4/3.2s）。
3. 同步两份资源到 `build\resources\main\...` 与 `bin\main\...`（Copy-Item）。
4. 存最终工程：`docs\blockbench\checkpoints\super_lazy_wings_chestplate_elytra_20261014.bbmodel`（单次 stringify）。

## 第 4 步：Java 改造（仅 1 个文件）

文件：[SuperLazyWingsChestplateItem.java](file:///c:/mimiworld/src/main/java/com/mimiworld/mimiworld/item/SuperLazyWingsChestplateItem.java)

- 新增 import：`net.minecraft.world.entity.Entity`、`software.bernie.geckolib.constant.DataTickets`、`software.bernie.geckolib.core.animation.AnimationState`（包名已经源码核实）。
- 新增 `FLY = RawAnimation.begin().thenLoop("animation.super_lazy_wings_chestplate.fly")`，保留 IDLE。
- 控制器过渡 tick 从 0 改为 **8**（0.4s 交叉混合，收/展不跳变）；predicate 改为：
  `Entity e = state.getData(DataTickets.ENTITY);` 若 `e instanceof LivingEntity living && living.isFallFlying()` → `setAndContinue(FLY)`，否则 IDLE。写法对齐先例 [FallenRicePhoenixEntity.java L126-137](file:///c:/mimiworld/src/main/java/com/mimiworld/mimiworld/entity/FallenRicePhoenixEntity.java#L126-L137)。
- 重写两个 IForgeItem 钩子（Forge 47.4.10 已实证签名）：
  - `canElytraFly(ItemStack, LivingEntity) → true`（空中按跳跃即可滑翔）；
  - `elytraFlightTick(ItemStack, LivingEntity, int) → true`，**不消耗耐久**（50 点耐久只走战斗受伤路径；以后想损耐久就在此仿原版每 20 tick `hurtAndBreak(1,...)`，计划中注明位置）。
- Renderer/Model/ModItems/注册/材质/配方均**不改**。原版 ElytraLayer 硬编码只认 `Items.ELYTRA`，不会重复渲染翅膀；烟花助推、俯冲朝向开箱即用。

## 第 5 步：验证

1. `.\gradlew.bat compileJava`（PowerShell 用 .bat）BUILD SUCCESSFUL；GetDiagnostics 无新增错误。
2. 游戏内实测清单（交付用户执行）：
   - 地面站立/行走：背视双翼收合 idle，随身体微动；
   - 高处跳出按住跳跃：进入滑翔，0.4s 平滑展翼播 fly，双翼随身体俯冲压平；
   - 落地/入水/骑乘：平滑收回 idle；
   - 滑翔 1 分钟耐久不下降，正常受击护甲耐久照常掉；
   - 第三人称背/侧视、盔甲架展示、物品栏图标均正常。

## 关键文件

- 改：`src\main\java\com\mimiworld\mimiworld\item\SuperLazyWingsChestplateItem.java`
- 覆盖：`src\main\resources\assets\mimiworld\geo\super_lazy_wings_chestplate.geo.json`、`...\animations\super_lazy_wings_chestplate.animation.json`（+同步 build/bin）
- 不改：Model/Renderer、ModItems、贴图、物品图标、lang、配方
- 备份目录：`docs\blockbench\checkpoints\`；预览图：`docs\blockbench\wing_preview\`
