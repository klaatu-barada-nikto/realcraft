# Realcraft (Realcraft Voxel Builder)

一个基于 [Fabric](https://fabricmc.net/) 的 Minecraft 模组，可通过管理员命令从远程 JSON 数据源拉取体素模型数据，并在世界中自动批量构建方块模型。

## 功能特性

- **远程数据构建**：通过 HTTP 下载 JSON 格式的体素模型数据，无需内置模型文件
- **异步下载**：使用 `HttpClient` 异步拉取数据，不阻塞游戏主线程
- **分批放置**：每个游戏 tick 最多放置 1000 个方块（`BuildTickHandler.MAX_BLOCKS_PER_TICK`），避免瞬间放置大量方块导致卡顿
- **任务队列**：支持多个构建任务排队执行，按 FIFO 顺序依次完成
- **构建反馈**：下载、开始构建、完成均会以彩色消息反馈给执行命令的玩家

## 环境要求

| 依赖 | 版本 |
| --- | --- |
| Java | 21+ |
| Minecraft | 1.21.1 |
| Fabric Loader | >= 0.16.9 |
| Fabric API | 任意兼容版本 |

## 构建

在项目根目录执行：

```bash
./gradlew build
```

构建产物位于 `build/libs/`，即 `realcraft-1.0.0.jar`。

## 安装

1. 安装 [Fabric Loader](https://fabricmc.net/use/)（1.21.1 对应版本）并启动一次游戏
2. 将 [Fabric API](https://modrinth.com/mod/fabric-api) 放入 `mods/` 目录
3. 将构建出的 `realcraft-1.0.0.jar` 放入 `mods/` 目录
4. 启动游戏

## 使用方法

在游戏中以玩家身份执行命令：

```
/buildmodel <url>
```

- `<url>`：指向 JSON 体素模型数据的完整 HTTP(S) 地址，参数之间可含空格
- 该指令只能由玩家执行，服务端控制台无法执行

执行后模组会以执行者所在位置为原点构建模型：

- 原点偏移：`x + 1`、`y + 0`、`z + 1`（相对玩家方块坐标）

### JSON 数据格式

请求的 URL 需返回一个 JSON 数组，每个元素描述一个方块（紧凑数组，元素顺序为 `id, x, y, z`）：

```json
[
  ["minecraft:stone", 0, 0, 0],
  ["minecraft:cobblestone", 1, 0, 0],
  ["minecraft:glass", 0, 1, 0]
]
```

元素说明：

| 数组下标 | 类型 | 说明 |
| --- | --- | --- |
| 0 | string | 方块注册 ID，如 `minecraft:stone` |
| 1 / 2 / 3 | int | 相对原点（玩家位置）的偏移坐标 x / y / z |

注意事项：

- 每个元素必须是 4 个字段的数组，格式错误会被判为解析失败
- `id` 必须为游戏内已注册的方块 ID，无法解析的方块（如 ID 非法、方块不存在、为空）会被跳过
- 数据为空或 JSON 解析失败时会在聊天栏提示错误
- HTTP 非 200 状态码、连接超时（10 秒）、请求超时（30 秒）均视为获取失败

## 项目结构

```
src/main/java/com/realcraft/buildmodel/
├── RealcraftMod.java          # 模组入口，注册命令与 tick 处理器
├── command/
│   └── BuildModelCommand.java # /buildmodel 命令注册与参数解析
├── service/
│   ├── ModelBuildService.java # 构建流程编排：下载 -> 入队
│   ├── ModelDownloader.java   # HTTP 异步下载与 JSON 解析
│   └── ModelDownloadException.java
├── model/
│   ├── VoxelBlock.java        # 体素方块数据（id/x/y/z）
│   └── BlockPlacement.java    # 世界内实际放置位置与方块状态
└── queue/
    ├── BuildTaskQueue.java    # 构建任务队列
    ├── ModelBuildJob.java     # 单个构建任务（含坐标换算、方块解析）
    └── BuildTickHandler.java  # 每 tick 驱动队列放置方块
```

## License

[MIT](https://opensource.org/licenses/MIT)
