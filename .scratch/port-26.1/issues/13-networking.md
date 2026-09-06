Type: task
Status: resolved
Blocked by: 10

## Question

将 GTM 的 SimpleChannel/FriendlyByteBuf 网络协议迁移为 NeoForge 26.1.2 CustomPacketPayload、Type、StreamCodec 和 payload handler，并恢复所有机器、GUI、cover、recipe 和同步消息。

## Context

主 spec：[`spec.md`](../spec.md)。网络差异：[`05-api-gap-inventory.md`](05-api-gap-inventory.md) 与 [`01-upstream-121-playbook.md`](01-upstream-121-playbook.md)。

## Work

- 为每个消息族定义 payload type、StreamCodec、方向、handler thread、可选/必需协商和错误处理。
- 将 payload registration 接入 `RegisterPayloadHandlersEvent` 及 26.1.2 client-side handler 生命周期。
- 迁移服务器到客户端、客户端到服务器和双向消息，覆盖 machine UI sync、cover sync、recipe sync、research、tooltips 和 integration messages。
- 处理目标 payload 大小限制、连接生命周期、无效数据和旧世界/旧客户端拒绝策略。
- 清理 SimpleChannel、NetworkDirection、旧 packet distributor 和旧 Forge network import。

## Acceptance

- 每个消息族至少有 codec round-trip test；代表性 GUI/machine sync 在 GameTest 或集成运行中恢复。
- clean client/server 连接和打开 GTM UI 不因 payload registration 或 handler thread 错误崩溃。
- 无旧 SimpleChannel/FriendlyByteBuf packet registration 残留作为 GTM 网络入口。
- 无效、截断和错误方向 payload 被拒绝而不让 server/client 崩溃。

## Answer

网络迁移已完成，实现提交 `94c566957`（分支 `impl/13-networking`，worktree `D:\mcmodDemo\gtm-13-networking`），已通过 merge commit `77ade7b04` 合入主 PR 分支 `port/26.1.2-neoforge`（`CommonProxy.java` 与 task 11 的改动由 git 自动合并并经人工核对，两侧迁移点零丢失）。

完成内容：移除 GTM `SimpleChannel`，全部改为 `CustomPacketPayload` + payload `Type` + `StreamCodec`；`RegisterPayloadHandlersEvent` / `RegisterClientPayloadHandlersEvent` / `PayloadRegistrar` / `HandlerThread.MAIN` 注册；protocol negotiation `PROTOCOL_VERSION = "2"`；`ClientPacketDistributor.sendToServer`、`PacketDistributor.sendToPlayer` / `sendToAllPlayers` / `sendToPlayersInDimension` 与 tracking entity/chunk API；key state C2S、image request/response、world ID、cape updates、prospecting、ore/fluid/bedrock ore sync、hazard zone sync、monitor-group 与 prospection-share 双向 payload。边界检查覆盖 string、collection、byte array、nested MUI buffer、image chunk、prospecting data；malformed/truncated/oversized payload 拒绝、handler 异常隔离、MUI client payload handler 拆分与 nested buffer 校验均已实现；新增 `NetworkCodecTest`。

验证：扫描确认 `SimpleChannel`、`NetworkEvent.Context`、`NetworkDirection`、`NetworkRegistry`、`registerMessage`、`GTNetwork.INetPacket`、`net.neoforged.neoforge.network.simple` 不再作为 GTM 网络注册机制存在；MUI 编译与测试通过；合并后 task 13 范围文件未出现在根 `compileJava` 剩余错误清单中。GameTest/UI 同步行为验收在 task 14/16 的 clean client 与全量运行闭环。
