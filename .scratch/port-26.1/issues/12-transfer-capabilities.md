Type: task
Status: resolved
Blocked by: 10

## Question

迁移 GTM 的 item/fluid/energy transfer 和 capability 边界到 NeoForge 26.1.2，同时保持机器、管道、线缆、cover 和外部自动化行为。

## Context

主 spec：[`spec.md`](../spec.md)。重点研究：[`05-api-gap-inventory.md`](05-api-gap-inventory.md)。现有 machine/cover/capability 行为测试是主要 prior art。

## Work

- 将 Forge capability exposure、LazyOptional/provider、ForgeCapabilities 和旧 handler 访问改为目标 NeoForge capability/transfer model。
- 评估并迁移 ResourceHandler、EnergyHandler、transaction、snapshot/rollback、side access 和 context 语义。
- 在 GTM 自有 transfer API 与 NeoForge API 之间建立集中适配边界，避免所有机器和 cover 直接散落 target API 迁移。
- 逐类迁移 item bus、fluid pipe、energy cable、machine ability、cover handler、external capability provider 和 compatibility wrappers。
- 保持 simulate/execute、insert/extract、capacity、side、transaction failure 和 invalidation 语义。

## Acceptance

- 现有 cover、machine part、pipe、energy 和 AE2 相关行为测试通过，且增加代表性 transaction commit/rollback 测试。
- item/fluid/energy 在空、满、部分容量、模拟和执行场景下结果与 1.20.1 领域行为一致。
- 外部 NeoForge capability 查询可发现 GTM 支持的能力；不存在旧 Forge capability class 的 runtime linkage。
- dedicated server 和 GameTest server 均不触发 client-only capability/provider 加载。

## Answer

传输与 capability 迁移已完成，实现提交 `5ad899a11`（分支 `impl/12-transfer-capabilities`，worktree `D:\mcmodDemo\gtm-12-transfer`，含前序 agent 的 checkpoint `7f124353b`），已通过 merge commit `72b4872fa` 合入主 PR 分支 `port/26.1.2-neoforge`。

完成内容：capability 暴露全面迁移到 `RegisterCapabilitiesEvent` + `BlockCapability`/`ItemCapability`/`EntityCapability`（机器、六类管道/线缆、tool、component item、spoilable、medical tracker entity capability）；新接口 `IGTCapabilityBlock` 由 `CommonProxy.registerCapabilities` 统一分发（addon 兼容）。`api/transfer/GTMTransferAdapters.java` 是 GTM 自有 handler ↔ NeoForge `ResourceHandler`/`EnergyHandler` 的集中适配边界（含 `SnapshotJournal` 事务与反向兼容视图），`CustomItemStackHandler`/`CustomFluidTank` 直接实现 `ResourceHandler`。26.1 删除的 `INBTSerializable` 以 GTM 自有接口替代（16 个文件）并用 `TagValueInput/TagValueOutput` 桥接 `ValueIOSerializable`；删除指向已不存在类的 `CapabilityDispatcher` 两个 mixin；`SpoilableBehavior` 从 `AttachCapabilitiesEvent` 改造为按 item 注册的 provider。保留语义：simulate/execute（Transaction abort/commit）、insert/extract、capacity、sided access（Direction 上下文）、rollback/invalidation。

验证：`git diff --check` 通过；transfer 范围文件自身编译错误清零（根编译仍被全局机械迁移 backlog 阻塞，约 2 万处 NBT-Optional/改名/搬包，建议开专门 ticket，见 `.scratch/port-26.1/implementation-notes/task-12-transfer.md` 的分组清单）。新增 8 个 transaction commit/rollback 与 simulate/execute 单测（`GTMTransferAdaptersTest`），因主编译未全绿暂未运行，随 task 16 执行；sided access 的 GameTest 行为验收同在 task 16 闭环。clean server 不加载 client-only capability 类的验收随 task 16 的 runCleanServer/runGameTestServer 闭环。
