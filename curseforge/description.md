# Rematch Cage

**Rematch Cage** is a lightweight utility mod designed for boss rushes, modpacks, and repeatable challenges. Configure flexible one-to-many mappings from awakening tribute items (or item tags) to target bosses and mobs: whenever a mapped entity dies, a **Rematch Cage** spawns at its location displaying a spinning miniature model of that creature. Right-click the cage with its configured item to awaken and fight it again.

## Features

- **Flexible Item-to-Entities Mapping**: Map any item or `#item_tag` to a group of entities or `#entity_type_tag` in `rematchcage-common.toml` (for example, `minecraft:nether_star = minecraft:wither, minecraft:elder_guardian` and `minecraft:echo_shard = minecraft:warden`).
- **Automatic Cage Spawning on Death**:
  - Listens at lowest priority so canceled deaths (such as totems or mod revive mechanics) do not trigger false cages.
  - Supports mid-air ground snapping so flying bosses leave reachable cages on the ground below.
  - Protects existing block entities (chests, graves, machines) by searching nearby replaceable spaces instead of overwriting them.
- **Miniature Entity Display**: Renders an automatically scaled, gently bobbing and spinning model of the bound creature inside the spawner cage, even in dim boss arenas.
- **Instant Rematch & Clean Arena**:
  - Right-click with the configured item to consume 1 tribute (none in Creative), remove the cage so it never blocks combat, and summon the entity with full `finalizeSpawn` initialization and persistence enabled.
  - Defeating the resummoned creature spawns a new Rematch Cage again.
  - Right-clicking with the wrong item or an empty hand displays the required localized item and entity name on the action bar.
- **Explosion & Boss Proof, Pickaxe Removable**: Immune to explosions, Wither, and Ender Dragon destruction, while players can cleanly mine unwanted cages with a pickaxe (drops nothing).
- **Creative & Mapmaker Friendly**: Right-click an empty Rematch Cage with any Spawn Egg in Creative Mode to bind it directly, and middle-click to clone cages with bound entity data and tooltips.

## License & Source

- **License**: All Rights Reserved
- **Source Code**: [GitHub - Rematch Cage](https://github.com/hp3081925122/Rematch-Cage)

---

# 重战牢笼（Rematch Cage）

**重战牢笼（Rematch Cage）** 是一款专为反复挑战 Boss、精英怪与整合包流程设计的轻量实用模组。支持在配置文件中自由编写「唤醒物品（或物品标签）→ 生物群组（或生物标签）」的一对多映射表：当表内任意生物死亡时，会在原地生成一座内部悬浮展示该生物微缩模型的特殊刷怪笼；手持对应的配置物品右键即可直接再次唤醒该生物。

## 核心功能

- **灵活的一对多映射配置**：在 `rematchcage-common.toml` 中按 `物品ID或#物品标签 = 生物ID1, 生物ID2, #生物标签` 格式自由配置（内置示例：`minecraft:nether_star = minecraft:wither, minecraft:elder_guardian` 与 `minecraft:echo_shard = minecraft:warden`）。
- **死亡自动生成与安全落点**：
  - 采用最低优先级监听死亡事件，避免被不死图腾或其他模组免死机制取消的死亡误生成牢笼。
  - 支持半空死亡自动向下吸附到最近地面，防止飞行 Boss 死在半空导致玩家无法右键交互。
  - 自动避开已有方块实体（箱子、墓碑、机器等），就近寻找可替换空位放置。
- **笼内微缩模型展示**：在刷怪笼内渲染自适应缩放、缓慢自转并轻微悬浮的目标生物模型，暗处也会补光方便辨认。
- **一键再战不卡场地**：
  - 手持对应配置物品右键消耗 1 个祭品（创造模式不消耗），牢笼自动消失防止战斗卡位，并在原地完整执行 `finalizeSpawn` 初始化与防刷没标记后重新召唤该生物。
  - 再次击杀召唤出的生物后会重新生成重战牢笼，实现无限次反复挑战。
  - 空手或拿错物品右键时，Action Bar 会直接显示所需的本地化物品与生物名称。
- **防爆防 Boss 破坏、可镐子清理**：免疫爆炸、凋灵与末影龙破坏；若挡路可用镐子正常挖掘清除（不掉落本体）。
- **创造与地图制作友好**：创造模式下可用任意刷怪蛋右键空牢笼直接绑定生物，支持鼠标中键带数据复制与物品提示显示封存灵魂及唤醒祭品。

## 许可与源码

- **许可协议**：All Rights Reserved
- **源码仓库**：[GitHub - Rematch Cage](https://github.com/hp3081925122/Rematch-Cage)
