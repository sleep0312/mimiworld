# Blockbench 大批量任务记录

> 创建时间：2026-10-12
> 用途：记录本轮大批量 Blockbench 任务的清单、进度与注意事项。

## 任务总览：GeckoLib 自定义盔甲（共 5 套，每套 4 件：头盔/胸甲/护腿/靴子）

> 强度等级（由弱到强）：饭晶合金套 < 超重结晶套 < 复合米饭套 < 清洁米饭套 < 超懒合金套
> 主题配色（按等级）：T1 白 < T2 绿 < T3 蓝 < T4 紫 < T5 金；华丽度随等级递增（T1 朴素 → T5 最华丽）。
> 目标：将以下 5 套现有盔甲从原版 2D 贴图升级为 GeckoLib 自定义 3D 模型（Blockbench 建模 + 绘制纹理 → Geo 模型/贴图四件套绑定）。
> 材料枚举已存在于 ModArmorMaterials，物品已注册，本批只做模型与纹理资源（动画暂不做）。

| # | 任务名称 | 材料 ID | 对应 Boss（仅借形状） | 主题色 | 强度定位 | 状态 | 备注 |
|---|---------|---------|----------------------|-------|---------|------|------|
| 1 | 饭晶合金套 | rice_crystal_alloy | 米米猴王 | 白 | T1 · 钻石同级，韧性 1 | 📦 已导出并绑定（compileJava 通过） | 修复材料：饭晶合金锭；.bbmodel 已备份 docs/blockbench/ |
| 2 | 超重结晶套 | super_heavy_crystal | 饭晶虎王 | 绿 | T2 · 高防高韧重甲，韧性 3，击退抗 0.05 | ⬜ 待开始 | 修复材料：超重结晶 |
| 3 | 复合米饭套 | compound_rice_meal | 超晶牛王 | 蓝 | T3 · 钻石同级，韧性 2，高附魔(18) | ⬜ 待开始 | 修复材料：复合米饭锭 |
| 4 | 清洁米饭套 | cleansing_compound | 重晶兔王 | 紫 | T4 · 下界合金同级，韧性 3.5，击退抗 0.1 | ⬜ 待开始 | 修复材料：清洁复合锭 |
| 5 | 超懒合金套 | super_lazy_alloy | 复晶天青蟒 | 金 | T5 · 终极套，总防 26，韧性 5，击退抗 0.15 | ⬜ 待开始 | 修复材料：超懒合金锭 |

## 状态图例

- ⬜ 待开始
- 🔨 模型搭建中
- 🎨 纹理绘制中
- 🎬 动画制作中
- 📦 已导出 / 已绑定
- ✅ 已完成
- ⏸️ 暂停 / 阻塞

## 任务明细

> 统一交付物（每套）：Blockbench 工程（.bbmodel 备份到 docs/blockbench/）+ 导出 geo.json（1 个，含 4 件的骨骼组）+ 纹理 PNG（整图）。
> **实际资源路径（2026-10-13 按残堕宗罪套既定约定，以此为准）**：geo → `assets/mimiworld/geo/<name>_armor.geo.json`（geo 根目录，非 geo/armor/）；贴图 → `assets/mimiworld/textures/armor/<name>_armor.png`（非 textures/models/armor/，后者只放原版 _layer 图）。GeoModel 类内用 ResourceLocation 指向这两处。
> GeckoLib 盔甲标准骨骼命名：`armorHead` / `armorBody` / `armorRightArm` / `armorLeftArm` / `armorRightLeg` / `armorLeftLeg` / `armorRightBoot` / `armorLeftBoot`（HumanoidModel 骨骼挂载点）。
>
> **Boss 特征借鉴规则（T1→T5 对应 5 只 Boss）**：只借鉴「形状/剪影」特征，点到为止（意思意思），**不照搬 Boss 配色**。
> - T1 米米猴王 / T2 饭晶虎王 / T3 超晶牛王 / T4 重晶兔王 / T5 复晶天青蟒
>
> **配色规则**：严格按等级主题色（白/绿/蓝/紫/金），主题色约占整套 50%；每套盔甲至少使用 5 种颜色（主题色 + 辅助/暗部/高光/金属/点缀等）。
>
> **质量底线**：即使是 T1，完成度也不得低于原版盔甲（不可比原版更粗糙、更丑）；T1 是「朴素」而非「简陋」，五套逐级提升精致/华丽度。
>
> **硬规则 1 · 露脸**：任何一套头盔，投影到面部区域（Steve 头正面矩形 x[-4,4]、y[24,32]，面积 64；凡前面到达 z<=-4 的 cube 都计入并集）的遮挡面积 **≤ 35%**，必须能明显看到完整的脸（眼睛不得被任何甲片盖住）。每套完成后用程序化网格采样实测遮挡率并记入进度。
>
> **硬规则 2 · 块数递增**：可见 cube 数量必须随等级严格递增——T1 < T2 < T3 < T4 < T5（T1 基线 = 32 个可见 cube），后续每套建模前先规划块数下限，华丽度与体量同步上升。
>
> **套装额外效果**：每套都有「4 件成套穿戴才触发」的组合效果（set bonus）。具体效果内容待用户给出；本期先记录需求，效果逻辑（四槽同材料判定 + tick 事件挂效果）在 Java 侧另行实现，参考项目既有约定优先用真实自定义 MobEffect 而非隐藏 AttributeModifier。
>
> **技术现状（2026-10-12 核查）**：5 套盔甲目前均为原版 `ArmorItem`（ModItems 中注册），仅 2D 贴图；项目已有 GeckoLib 动态盔甲完整范本——残堕宗罪套（item/RemnantFallenCardinalSinArmorItem.java + client/model/RemnantFallenCardinalSinArmorModel.java + client/renderer/RemnantFallenCardinalSinArmorRenderer.java），本批直接照此四件套模式改造；当前项目内尚无任何成套穿戴判定逻辑。

### 任务 1：饭晶合金套（rice_crystal_alloy，T1）

- 对应 Boss：米米猴王（mimihouwang）
- 需求：4 件全套 GeckoLib 3D 模型 + 纹理
- 模型要点：轻度借鉴猴王形状——头盔可带紧箍/头带式额饰或猴耳小轮廓，胸甲可有毫毛领口/护心圆镜意象；点到为止，不可做成猴头套
- 纹理要点：主色白（约 50%），至少 5 色；素净基础款
- 动画要点：本期不做
- 套装效果：（待用户给出，4 件成套触发）
- 导出与绑定：geo → assets/mimiworld/geo/rice_crystal_alloy_armor.geo.json；贴图 → textures/armor/rice_crystal_alloy_armor.png（见顶部统一路径约定）
- 进度记录：
  - 2026-10-12：模型与纹理完成（Blockbench 工程 `CustomArmor`，geckolib_model，box_uv=false 全 per-face UV；32 个可见 cube / 192 面，128×128 整图 0 失败装箱，工程尚未保存到磁盘）。
  - 模型构成：头部 9 cube（盔帽[前沿后退露额]/后片/收窄双颊护片、金色紧箍额带 + 凸出蓝宝石额饰、金色猴耳轮廓一对、顶饰，脸部敞开）；上身 7（胸甲主体、金色领圈、护心镜[象牙框+金面+蓝宝石]、金腰带、象牙点金带扣、前后裙甲）；双臂各 3（肩甲/臂甲/皮革护腕）；双腿各 3（股甲/金框膝甲/腰侧甲）；双靴各 2（白皮金沿靴筒 + 皮革带鞋带靴身，脚尖前伸）。**可见 cube 总数 32（T1 基线）**。
  - 面部遮挡实测 **27.2%**（紧箍额带 15% + 双颊 12.2%，眼睛 y≈27.7-28.4 完全外露；额带上沿 y28.6），满足硬规则 1（≤35%）。
  - 配色：白甲 plate #ECEEF2 / hi #FBFBFD / lo #C6CBD5；金 #E0BC57 系；皮革 #6B513D 系；蓝宝石 #6FA8DC 系；象牙 #F3EAD4——共 5 组 15 色。正面主面用 hi 白底抵消实体 0.8 面光衰减。
  - 猴王元素点到为止：紧箍环 + 额前蓝宝石 + 双耳轮廓 + 顶饰；护心镜意象。
  - 已正视/侧视/背视/3-4 透视自检：无共面闪烁（腿甲前后表面收到 ±2.3，裙甲 ±2.5/±2.1；宝石收进环带高度消除背面漏蓝），无硬穿模；预览器中心软缝为 AO，导出后实体渲染无 AO，不用处理。
  - 2026-10-13：**已导出并完成 GeckoLib 绑定，`gradlew compileJava` BUILD SUCCESSFUL**。
    - 资源：[rice_crystal_alloy_armor.geo.json](../../src/main/resources/assets/mimiworld/geo/rice_crystal_alloy_armor.geo.json)（identifier 已改名 geometry.rice_crystal_alloy_armor）+ [rice_crystal_alloy_armor.png](../../src/main/resources/assets/mimiworld/textures/armor/rice_crystal_alloy_armor.png)（128×128，4.4KB）；工程备份 [rice_crystal_alloy_armor.bbmodel](rice_crystal_alloy_armor.bbmodel)。
    - Java：[RiceCrystalAlloyArmorItem.java](../../src/main/java/com/mimiworld/mimiworld/item/RiceCrystalAlloyArmorItem.java)（extends ArmorItem implements GeoItem，registerControllers 留空=静态）+ [RiceCrystalAlloyArmorModel.java](../../src/main/java/com/mimiworld/mimiworld/client/model/RiceCrystalAlloyArmorModel.java)（动画返回公共空文件 animations/cooking_pots.animation.json）+ [RiceCrystalAlloyArmorRenderer.java](../../src/main/java/com/mimiworld/mimiworld/client/renderer/RiceCrystalAlloyArmorRenderer.java)；ModItems 中四件已从 ArmorItem 换成该类（材料/属性不变），无需 ClientSetup 注册（initializeClient 自带 renderer）。
    - 物品图标 models/item 与 lang 四件原本就有，无需新增；textures/models/armor 下旧 _layer 图保留（GeckoLib 渲染不再使用）。
    - 待办：游戏内实机穿戴验证；套装 set bonus（四槽同材料判定）待用户给效果后另做。

### 任务 2：超重结晶套（super_heavy_crystal，T2）

- 对应 Boss：饭晶虎王（rice_crystal_tiger_king / fanjinghuwang）
- 需求：4 件全套 GeckoLib 3D 模型 + 纹理；重甲风格，模型体量可更厚重
- 模型要点：轻度借鉴虎王形状——头盔可有虎耳尖角/獠牙护颊，肩甲厚重外张，甲面可刻虎纹状折线；点到为止
- 纹理要点：主色绿（约 50%），至少 5 色；厚重重甲感
- 动画要点：本期不做
- 套装效果：（待用户给出，4 件成套触发）
- 导出与绑定：同上
- 进度记录：

### 任务 3：复合米饭套（compound_rice_meal，T3）

- 对应 Boss：超晶牛王（super_crystal_cow_king）
- 需求：4 件全套 GeckoLib 3D 模型 + 纹理
- 模型要点：轻度借鉴牛王形状——头盔顶部可有一对弯角/晶角轮廓，胸甲宽厚稳重似牛胸；点到为止
- 纹理要点：主色蓝（约 50%），至少 5 色；精致附魔感
- 动画要点：本期不做
- 套装效果：（待用户给出，4 件成套触发）
- 导出与绑定：同上
- 进度记录：

### 任务 4：清洁米饭套（cleansing_compound，T4）

- 对应 Boss：重晶兔王（heavy_crystal_rabbit_king）
- 需求：4 件全套 GeckoLib 3D 模型 + 纹理
- 模型要点：轻度借鉴兔王形状——头盔可有一对修长兔耳轮廓（可后掠/收束避免过高），靴/腿甲可有蹬跃后肢的流线造型；点到为止
- 纹理要点：主色紫（约 50%），至少 5 色；华贵感
- 动画要点：本期不做
- 套装效果：（待用户给出，4 件成套触发）
- 导出与绑定：同上
- 进度记录：

### 任务 5：超懒合金套（super_lazy_alloy，T5）

- 对应 Boss：复晶天青蟒（compound_azure_python）
- 需求：4 件全套 GeckoLib 3D 模型 + 纹理；终极套，造型最华丽、识别度最高
- 模型要点：轻度借鉴天青蟒形状——头盔可有蛇首/额心棱脊轮廓，甲片可做覆瓦鳞片状叠层，腰侧或背可有收束蛇形饰条；点到为止，华丽但不臃肿
- 纹理要点：主色金（约 50%），至少 5 色；终极华丽感
- 动画要点：本期不做（后续可考虑套装特效动画）
- 套装效果：（待用户给出，4 件成套触发）
- 导出与绑定：同上
- 进度记录：

---

## 通用规范备忘

- 所有 cube 使用 per-face UV，避免 box-UV 引擎重算导致错位
- GeckoLib 盔甲骨骼名必须严格匹配 HumanoidModel 挂载点（armorHead/armorBody/armorRightArm/...），护腿与靴子分别用 armor*Leg / armor*Boot 骨骼
- Blockbench 建模时使用 GeckoLib 插件、Java Block/Entity 格式，模型贴合 Steve 体型基准框，避免穿模
- 实体动画键名需与导出的 animation.json 完全一致（可带前缀）
- position 关键帧填相对骨骼默认位置的偏移量，旋转单位为「度」
- 纹理贴合大米主题；盔甲按强度做视觉分级：T1 朴素 → T5 华丽厚重，五套之间剪影与配色要有明显区分
- 根目录临时导出资源绑定进 mod 后立即清理

### 程序化建模/纹理工作流经验（T1 实战，后续 T2–T5 复用）

#### A. 标准制作流水线（每套照此执行）

1. 建工程：create_project（geckolib_model 格式），确认 `Project.box_uv=false`（per-face UV 硬规范），纹理位 128×128；八个 armor* 挂载骨名严格匹配 HumanoidModel。
2. 搭几何：按部位分组建 cube（构成模板见 B），边搭边防穿模/共面（见 D）；左右对称件以 x=0 为界，右件 from.x=0、左件 to.x=0，避免中线重叠。
3. 规划块数：开工前先定可见 cube 数，严格满足 T1(32) < T2 < T3 < T4 < T5；T2 起建议 38+、T3 44+、T4 50+、T5 58+（含角/獠牙/耳/鳞片叠层等 Boss 剪影件）。
4. UV 打包：无间隙 MaxRects（见 E），写入全部 face.uv。
5. 程序化绘制：canvas 按材质分组绘制，g.save()+rect.clip() 逐面裁剪防越界（见 F/G）。
6. 上传纹理：新建纹理对象 → fromDataURL → 轮询确认 → 绑 192 面 → 删旧 → 改名（见 H，必须整体走一遍）。
7. 自检四视角：正视/背视（正交）、左右侧视、3/4 透视；头盔额外跑遮挡率脚本（见 C）。
8. 几何每改一次，必须从第 4 步完整重跑（pack→绘→新纹理），不要增量补丁。
9. 用户确认后才导出：geo → `assets/mimiworld/geo/armor/`，PNG → `assets/mimiworld/textures/models/armor/`；导出前必须经用户确认。

#### B. 四件构成模板（T1 用 32 块，后续在件数/叠层上加码）

- 头盔 9：盔帽（露额）/后片/双颊护片 + 紧箍式额带 + 凸面宝石 + 双耳轮廓 + 顶饰；脸部敞开。
- 上身 7：胸甲主体 / 领圈 / 护心镜（象牙框+金面+宝石）/ 腰带 / 带扣 / 前裙甲 / 后裙甲。
- 双臂各 3：肩甲（pauldron）/ 臂甲（armguard）/ 护腕（bracer，皮革）。
- 双腿各 3：股甲（thigh）/ 金框膝甲（knee）/ 腰侧甲（hip）。
- 双靴各 2：靴筒（bootcuff，主色+金沿）/ 靴身（boot，皮革+鞋带，脚尖 z 伸到 -4.6）。
- Boss 元素只放 2–4 个识别点（T1=紧箍+猴耳+顶饰+护心镜），随等级可增至 5–7 个，但不照搬 Boss 配色。

#### C. 露脸规则的设计范式与实测法（硬规则 1）

- **遮挡率定义**：面部矩形 x[-4,4] × y[24,32]（面积 64）；凡 cube 前面到 z<=-4 的投影计入并集，覆盖率必须 ≤35%，眼睛（y≈27.7–28.4）绝对不能有甲片。
- **推荐头盔开脸法（T1 验证可行）**：① 盔帽前沿后退到 z=-2.8（不盖脸，游戏里玩家皮肤在 z=-4，正好补满开口，预览里的灰色区域=将来的脸，不是空洞）；② 额带/紧箍整体上移到 y≥28.6，高度 1.0–1.2，只压发际线；③ 颊片只占 x|x|≥2.7 的颚侧、高度收到 y25.4–28.4；④ 宝石贴在额带正面（z 可凸到 -5.15）但 y 必须与额带齐平。T1 实测 27.2%（额带 15% + 双颊 12.2%）。
- **实测脚本思路**：risky_eval 中筛头盔相关 cube → 对每个前面 z<=-4 的 cube 求与面部矩形的交集 → 0.1 步长网格 80×80 采样求并集面积 /64，输出总遮挡率与各件贡献。

#### D. 几何防坑清单

- **共面 z-fight**：前后叠层表面至少差 0.2。T1 数值参考：裙甲 z±2.5/±2.1、大腿甲 ±2.3、护心镜 -2.7、腰带 -2.8、带扣 -3.4..-3.0。
- **缝隙露内部**：相邻片水平相接处让一片嵌入另一片 0.2–0.3（如裙甲顶 y=12.5 嵌入胸甲底 12.2/腰带区），不要留齐平缝。
- **小饰件越界投影**：贴脸/贴环带的饰件 y 范围必须被包覆件包含（宝石曾因低 0.3 在背视图漏出一条蓝色）。
- **中线对称**：左右件 x 起止严格在 0 汇合，不重叠不留缝。
- 预览器中心的深色软缝是 AO，GeckoLib 实体渲染无 AO，不用处理；缩放截图的摩尔纹放大即消失。

#### E. UV 打包

- per-face 面尺寸=对应 cube 边长 ×2 取整（north/south 取 Δx×Δy，east/west 取 Δz×Δy，up/down 取 Δx×Δz）。
- 无间隙 MaxRects 装 128×128：矩形按面积降序，**必须对每个相交空闲矩形做 4 向 split + 包含关系剪枝**——漏 split 会产生重叠矩形，直接导致皮革色串到白甲上（T1 踩过）。
- 写 face.uv 时四边内缩 0.25 纹素：`[x+.25, y+.25, x+w-.25, y+h-.25]`，防采样溢出串色。
- face.uv 单位就是纹理像素；早期「误除 2」是经典错误。
- 程序化 cube 上 BarItems.uv_auto 无效，必须自写打包器。

#### F. 材质配色模板（T1 的 5 组 15 色，后续套换主色即可）

- 主甲 4 色：plate 平色 / hi 高光（正面主面底色）/ lo 暗部 / line 描边。
- 金属金 5 色：gold / ghi / glo / gline / gdark。
- 皮革 4 色：leather / lhi / llo / lline（护腕、靴身用）。
- 宝石 3 色：gem / gemhi / gemlo（额饰、护心镜心用）。
- 点缀 1 色：ivory 象牙（护心镜框、带扣点、鞋带孔）。
- 规则：主题色约 50%、每套 ≥5 色；金色件/皮革件/宝石件用集合名单驱动同一套绘制函数，后续 T2–T5 只换色值与集合。

#### G. 绘制要点

- 每个面 g.save()→rect(px,py,w,h).clip()→translate 后局部坐标绘制→g.restore()，杜绝越界污染。
- 北面（正面）用 hi 色满底抵消实体 ~×0.8 面光（纯白 #FFFFFF 在游戏里≈浅灰，和原版铁甲同理）；南面用 plate+上下明暗；上下面单独处理（顶面 hi+描边、底面 lo）。
- 细节克制：胸甲单线框+四角铆钉+上下装饰线即可，双线满框会显脏；小面（≤6px 宽）暗条最多 1px。
- 皮革面用 1px 象牙点做鞋带/铆钉，两排即可。

#### H. Blockbench 纹理上传（最大坑，务必照做）

- **症状**：UV 大改后对同一 texture 反复 fromDataURL，canvas/img 像素正确，但视口 GL 停留旧布局（1px 周期条纹/色块错位）；updateSource()、updateAllImages()、直接往 t.canvas drawImage 全部无效。
- **唯一可靠流程**：create_texture 新建（不能传 uv_width/height，分辨率参数用 resolution:[128,128]）→ `fromDataURL(dataURL)` → **异步轮询**（临时 canvas drawImage(newTex.img) 后 getImageData，颜色数 >10 且 naturalWidth=128 才算完成，约 400ms/1500ms 两次轮询）→ 遍历所有可见 cube 六面把 `face.texture` 设为新纹理**对象**（192 面）→ 旧纹理 `remove()` → 新纹理 name=正式名并 rename → 确认 `Texture.all` 只剩一个。
- 任何几何改动后：重跑 E（pack）→ 重生成 dataURL → 整体重走本节。

#### I. 诊断手段

- 坐标编码纹理（R=u×2, G=v×2, B=128，叠加 8px 白网格）：从渲染图直接读出 GL 实际采样坐标，区分「UV 错」还是「纹理没上传」。
- 四色块（红绿蓝黄）全不透明诊断图：渲染干净即可排除 UV/几何问题。
- 怀疑某面脏：把该面 UV 矩形从 t.img 裁出 getImageData 逐像素打印，先确认源纹理再查几何。

#### J. MCP / 操作备忘

- set_camera_angle 的返回值直接带截图，无需再调 capture_screenshot；target 有时被重置成 [0,4,0]，每次显式传。
- 好用视角：正视正交 [0,16,-60]、背视 [0,16,60]、局部放大正交（位置与 target 同点、距离 10–12）、3/4 透视 [-40,20,-45]。
- risky_eval 长脚本用 window.__xxx 暂存 pack/dataURL；但 Trae 崩溃后 window 状态可能丢失（打包器函数不持久，只留变量），恢复时重建脚本即可。
- MCP 偶发「server is not found」瞬断，原样重试一次即可。
- 工程未保存磁盘时崩溃风险高；每定稿一套应尽快导出 geo+PNG 并另存 .bbmodel。

#### K. Java 绑定范本（导出后再做，本期不做）

- item：`src/main/java/com/mimiworld/mimiworld/item/RemnantFallenCardinalSinArmorItem.java`（extends ArmorItem implements GeoItem，四件共用一个 geo）。
- client：`client/model/RemnantFallenCardinalSinArmorModel.java`、`client/renderer/RemnantFallenCardinalSinArmorRenderer.java`（extends GeoArmorRenderer，按 EquipmentSlot 自动隐藏无关 armor* 骨）。
- geo 范本：`src/main/resources/assets/mimiworld/geo/remnant_fallen_cardinal_sin_armor.geo.json`。
- 5 套物品已在 `ModItems.java`（约 523–654 行）注册为原版 ArmorItem，材料在 `ModArmorMaterials.java`；成套穿戴判定项目中尚不存在，set bonus 待用户给效果后另做。
