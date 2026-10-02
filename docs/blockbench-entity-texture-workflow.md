# Blockbench 实体模型贴图作业规范（mimiworld）

> 用途：以后在 Blockbench（MCP 连接）里给本 mod 的**任意实体模型**重展 UV / 重绘画布贴图时，照本文件执行，避免重复踩坑。
> 首个实践案例：啄米鸡 zhuomi_chicken（2026-10-02，17 cubes / 128×128）。

## 0. 硬性规则（用户定下，每次必须满足）

1. **所有 UV 坐标必须是整数**，禁止小数。
2. **每个立方体的 UV 岛屿（6 个面）不得与其他立方体岛屿重叠**（同一立方体内部的六面排布由 uv_auto 负责）。
3. **必须先调用 `uv_auto` 自动展开**，再做后续；禁止手写大量 `uv_offset`。本版 Blockbench 无全局打包器时，允许在 uv_auto 结果上对整岛做平移装箱。
4. 贴图画布尺寸需显式确定（实体常用 **128×128**；小模型 64×64），所有 UV 必须落在 `0..画布宽/高` 内，禁止越界。
5. 流程顺序：保存检查点 → 转面 UV → uv_auto → 装箱平移 → 画贴图 → 多视角渲染验证 → 校验脚本全绿 → 导出。

## 1. 标准流水线（MCP：mcp_blockbench）

```
save_checkpoint            # 动手前留 undo 点
set_mode(edit)             # 必须用 MCP 工具切模式，勿在 risky_eval 里调 Modes.*
全部 cube setUVMode(false)  # box_uv → 面 UV
trigger_action(select_all) # 循环里 cube.select(true) 只会生效 1 个，必须用 action
trigger_action(uv_auto)    # 逐立方体六面归位（注意：本版本不是全局打包！）
装箱算法平移各岛（见 §4）   # shelf-packer，岛间留 1px 间距
Texture.all[0].edit(...)   # 画贴图（见 §3，禁止直接替换 canvas）
create_offscreen_view + set_camera_angle  # 正/侧/背/3-4 视角截图验收
跑 §5 校验脚本（整数/越界/重叠）
fs.writeFileSync 导出 PNG 到资源目录（见 §6）
trigger_action(save_project)  # 保存 .bbmodel（action 名是 save_project，不是 save）
delete_offscreen_view      # 清理离屏视图
```

## 2. 本版 Blockbench / MCP 的坑（实测）

- **`uv_auto` 只做"每个立方体各自六面归位"，不做全局打包**：执行后各岛仍大面积重叠。`uv_rel_auto` / `uv_maximize` 同理；`UVGenerator` 未定义；`Q` 就是 UVEditor 单例，无全局 packer。→ 用 §4 装箱脚本平移岛屿（保留 uv_auto 的六面结构，仅整岛平移，符合规则 3）。
- **禁止直接 `texture.canvas = newCanvas`**：Blockbench 内部真实位图不会更新（仍是旧尺寸，如 64×32），UV 超出旧边界的面在游戏里全透明。**唯一可靠写法**：

  ```js
  var T = Texture.all[0];
  T.edit(function(cv){
      cv.width = 128; cv.height = 128;
      var ctx = cv.getContext('2d');
      // ...逐像素/矩形绘制...
  });
  T.width = 128; T.height = 128; T.keep_size = true;
  ```

  `edit()` 自动管理 Undo 与材质刷新。
- **模式切换只能用 MCP `set_mode` 工具**；risky_eval 里的 `Modes` 被 Vue 包装、原型方法丢失（`Modes.edit.select()` undefined）。
- **多选立方体**：`cube.select(true)` 循环里只有最后一个生效，用 `trigger_action: select_all`。
- **risky_eval 对括号/引号敏感**：代码尽量包 IIFE；中断后若留下悬挂 Undo 事务，执行 `try{Undo.finishEdit('cleanup')}catch(e){}` 清理。
- **保存工程的 action 名是 `save_project`**（`save` 不存在）。保存后确认 .bbmodel 里 `"resolution":{"width":128,"height":128}` 与各 cube `"box_uv":false` 已落盘。
- 删除离屏视图若报 `onContextMenu is not defined`，用 `list_views` 确认实际是否已删（重启 Blockbench 也会清掉 offscreen view）。

## 3. 画贴图的通用手法

- 取某立方体某面的整数 UV 矩形：

  ```js
  function rc(name, face){
      var u = Cube.all.find(c=>c.name===name).faces[face].uv;
      return {x:Math.min(u[0],u[2]), y:Math.min(u[1],u[3]),
              w:Math.abs(u[2]-u[0]), h:Math.abs(u[3]-u[1])};
  }
  ```

- 底色用 `fillRect`，细节一律逐像素 `fillRect(x,y,1,1)`（MC 风）；每个面画 1px 明暗边（上/左亮、下/右暗）即可出体块感。
- 阴影里的深色（朝下/朝北的面）容易发黑：朝下的面用**提亮一档**的颜色补偿（例：喙 down 面 #F2AB4D 比正面亮）。
- 头部分辨方向性：眼睛/腮红只画在朝前脸（north）和需要的东西侧面，**south（后脑勺）必须恢复纯羽毛色**，否则背面穿帮。
- 画完必须多视角截图：正面、左/右侧、背面、3/4 透视（`set_camera_angle`，正交看像素、透视看整体）。

## 4. 岛屿装箱脚本（uv_auto 之后执行一次）

思路：每个立方体 uv_auto 后六面构成一个包围盒岛；记录每岛当前 minX/minY，按 shelf-packer（缩放 2×、间距 1px）排进画布，再把差值平移回该立方体的全部面。首次运行先 `console.log` 出每岛 `(name, w, h)`，确认总尺寸选画布（17 cubes 实体 → 128×128 够；最终占用 maxX=116 / maxY=70）。

```js
(function(){
  var SIZE = 128, PAD = 1;
  // 1) 收集每岛包围盒与各面当前 UV
  var islands = Cube.all.map(function(c){
    var x0=1e9,y0=1e9,x1=-1e9,y1=-1e9, faces=[];
    Object.keys(c.faces).forEach(function(k){
      var u=c.faces[k].uv;
      x0=Math.min(x0,u[0],u[2]); y0=Math.min(y0,u[1],u[3]);
      x1=Math.max(x1,u[0],u[2]); y1=Math.max(y1,u[1],u[3]);
      faces.push([c.faces[k], u]);
    });
    return {c:c, x0:x0, y0:y0, w:Math.ceil(x1-x0), h:Math.ceil(y1-y0), faces:faces};
  });
  // 2) shelf packer（高优先排序）
  islands.sort(function(a,b){ return b.h-a.h || b.w-a.w; });
  var sx=PAD, sy=PAD, shelfH=0;
  islands.forEach(function(is){
    if (sx + is.w + PAD > SIZE) { sx=PAD; sy+=shelfH+PAD; shelfH=0; }
    is.tx = sx - is.x0; is.ty = sy - is.y0;
    sx += is.w + PAD; shelfH = Math.max(shelfH, is.h);
  });
  // 3) 整岛平移（只移动 uv_auto 布局，不重写单面 offset）
  Undo.initEdit({elements: Cube.all, uv_only: true});
  islands.forEach(function(is){
    is.faces.forEach(function(p){
      var f=p[0], u=p[1];
      f.uv=[u[0]+is.tx, u[1]+is.ty, u[2]+is.tx, u[3]+is.ty];
    });
  });
  Undo.finishEdit('shelf pack islands');
  return islands.map(function(i){return i.c.name+' '+i.w+'x'+i.h;});
})();
```

> 装完若 maxY 超画布：换更大画布（必须同步用 `texture.edit` 改 cv 尺寸），或按同比例 2× 缩放 UV 后重排。禁止让任何 UV > 画布尺寸。

## 5. 交付前校验脚本（必须四项全绿）

```js
(function(){
  var badInt=[], oob=[], rects=[], faces=0, SIZE=128;
  Cube.all.forEach(function(c){ Object.keys(c.faces).forEach(function(k){
    var f=c.faces[k]; if(!f.uv) return; faces++;
    var u=f.uv;
    [u[0],u[1],u[2],u[3]].forEach(function(v){
      if(v%1!==0) badInt.push(c.name+'.'+k);
      if(v<0||v>SIZE) oob.push(c.name+'.'+k+':'+v);
    });
    rects.push({n:c.name+'.'+k, x0:Math.min(u[0],u[2]), y0:Math.min(u[1],u[3]),
      x1:Math.max(u[0],u[2]), y1:Math.max(u[1],u[3])});
  });});
  // 注意：重叠检测要按"立方体岛"而非单面——同立方体六面本就相邻。
  // 简单做法：检测不同 name 的面矩形相交。
  var overlaps=[];
  for(var i=0;i<rects.length;i++) for(var j=i+1;j<rects.length;j++){
    var a=rects[i], b=rects[j];
    if(a.n.split('.')[0]===b.n.split('.')[0]) continue;
    if(a.x0<b.x1 && b.x0<a.x1 && a.y0<b.y1 && b.y0<a.y1) overlaps.push(a.n+' vs '+b.n);
  }
  var maxX=0,maxY=0; rects.forEach(function(r){maxX=Math.max(maxX,r.x1);maxY=Math.max(maxY,r.y1);});
  return {faces:faces, badInt:badInt, oob:oob, overlapCount:overlaps.length,
          overlaps:overlaps.slice(0,10), maxX:maxX, maxY:maxY,
          canvas:Texture.all[0].canvas.width+'x'+Texture.all[0].canvas.height};
})();
```

验收标准：`badInt=[]`、`oob=[]`、`overlapCount=0`、`maxX/maxY ≤ SIZE`、canvas 尺寸正确。
另做一次**像素采样抽查**：从 canvas 直接 getImageData 取几个岛的代表像素，确认不是 `rgba(0,0,0,0)`（透明=贴图没画上去或走错 canvas，见 §2 第一条坑）。

## 6. 导出与 mod 资源约定

- **贴图落位**：`src/main/resources/assets/mimiworld/textures/entity/<实体注册名>.png`。注册名查 `ModEntities.java`（例：啄米鸡 #1 = `zhuomi_chicken` → `zhuomi_chicken.png`），与 GeckoLib 系现有约定一致（参考 `MicroDuckModel` 的 `textures/entity/microduck.png`）。
- **不要用 PowerShell 手抄 base64 落盘**（易截断/长度非法）。直接在 Blockbench 进程内写：

  ```js
  (function(){
    var fs=require('fs');
    var b64=Texture.all[0].canvas.toDataURL('image/png').split(',')[1];
    var p='c:\\mimiworld\\src\\main\\resources\\assets\\mimiworld\\textures\\entity\\<id>.png';
    fs.writeFileSync(p, Buffer.from(b64,'base64'));
    return 'written '+fs.statSync(p).size+' bytes';
  })();
  ```

  落盘后用 System.Drawing 或文件头校验 PNG 宽高与画布一致。
- **.bbmodel 工程**另存于 `C:\bbmodels\<中文名>\<id>.bbmodel`，贴图源文件同名 .png 放同目录；临时截图检查完即删。
- **渲染器现状提醒**：`ModEntities` 里除 mimi_sheep / sticky_rice_spider / rice_stealer(microduck) / laziest_final_dragon 外，其余实体（含 zhuomi_chicken）仍是 `PlaceholderRiceMobEntity`，**没有 Model/Renderer，游戏内隐身**——贴图画好只是资产就位，实体可见还需后续补 GeoLib（geo/animations/textures 三件套 + ClientSetup RegisterRenderers）或原版 LayerDefinition。汇报时必须说明这一点。

## 8. GeckoLib 格式转换的坑（2026-10-02 实测）

- Blockbench「文件 → 转换项目」转成 `geckolib_model` 后：**UV 被重置为自动 box UV**（`box_uv=true`，`autouv=0` 所有岛屿挤在 0,0 起、互相重叠），**贴图列表清空**（`textures:0`，模型显示随机色块）；骨骼、立方体尺寸、动画保留。
- 转换后模型立方体**尺寸通常全是整数**（即使 from/to 含 0.5 偏移，跨度仍是整数），此时 box UV 每面尺寸 = 立方体真实尺寸，逐面分配 UV 必然全整数，天然满足整数规则。
- **MCP 工具集缩减时的文件层面方案**（部分会话 mcp_blockbench 只有动画工具，无 risky_eval/set_mode/UV/贴图工具；Exec 的 run_mcp 桥也不通）：直接读写 `.bbmodel` JSON——
  1. 逐立方体按 box-uv 模板算六面 UV（up/down 顶行、四侧面中行），shelf-packer 装箱进画布；
  2. `box_uv=false` 使 faces 的 `uv:[x1,y1,x2,y2]` 生效（box_uv=true 时 Blockbench 忽略 faces uv 重新自动生成）；
  3. `textures` 数组补回条目（字段结构从旧 bbmodel 深拷贝），`source` 内嵌 `data:image/png;base64,...`，`saved:false`；
  4. PNG 用 Java 单文件 + BufferedImage 逐像素画（gson jar 从 `~/.gradle/caches/.../gson-2.10.jar` 复制；javac 必须加 `-encoding UTF-8`，否则中文路径/注释在 GBK 下编译失败）；
  5. 写回前备份 `.bak`。脚本可复用模板：`C:\bbmodels\啄米鸡\FixChicken.java`。
- GeckoLib 实体转正三件套：`assets/mimiworld/geo/<id>.geo.json`、`animations/<id>.animation.json`（键名裸名 idle/walk，勿带 animation. 前缀）、`textures/entity/<id>.png`。

## 7. mimiworld 大米主题调色板（后续实体保持统一）

| 用途 | 色值 |
|---|---|
| 米白羽毛/主体 | `#FFFCF2`，鳞纹暗纹 `#EBDFC2` |
| 米粒点缀（竖排 3 像素渐变） | `#F7E095 → #EBC55E → #C89632` |
| 稻叶绿（翅膀/尾羽/植物部件） | `#7DAE5E / #93C070 / #B5DC94`（上→下渐变），叶脉 `#6B9550` |
| 鸡冠/肉垂/红色部件 | 红 `#DC5138`，暗 `#C74A37`，高光 `#ED7B62` |
| 喙/脚/木质暖橙 | `#E8993A`，暗面 `#C47B28`，朝下提亮 `#F2AB4D` |
| 眼睛 | `#33261C`（可加 1px 高光），腮红 `#EFA28D` |

原则：白米打底 + 稻金点缀 + 叶绿呼应水稻 + 少量红/橙点睛；同屏多实体时主色统一在米白/暖米色系内。
