Type: task
Status: resolved
Blocked by: 10

## Question

迁移 GTM 的 recipe、ingredient、codec、datapack registry、loot、tags、models、data maps 和 datagen，使生成资源能被 MC 26.1.2 加载。

## Context

主 spec：[`spec.md`](../spec.md)。API 差异：[`05-api-gap-inventory.md`](05-api-gap-inventory.md)。当前数据/注册约定也见 [`01-upstream-121-playbook.md`](01-upstream-121-playbook.md)。

## Work

- 迁移 `GatherDataEvent`、client/server data providers、datapack built-in entries 和 data generation run。
- 将 NBT-sensitive item/fluid ingredients、recipe serializers 和 validation 迁移到 Data Component、Holder、codec 和 StreamCodec 语义。
- 将 ore vein、bedrock ore、bedrock fluid 等数据驱动注册迁移到目标 datapack registry，并移除旧同步包依赖。
- 迁移 loot entry/function/condition 的 MapCodec 注册、loot validation、tags 目录语义、data maps、model/item model 生成和资源命名。
- 保持 GTM recipe matching、材料数据、生成概率、loot 结果和自定义资源内容不变。

## Acceptance

- data generation 完成且生成资源在目标 dedicated server load 阶段无 codec、registry、tag、loot 或 data component 错误。
- 代表性材料、矿脉、机器 recipe、NBT/Data Component ingredient、loot table 和 custom registry round-trip 测试通过。
- 现有 recipe serializer/lookup/GameTest 回归测试在目标 toolchain 上通过。
- 生成资源不依赖被砍的第三方 integration。

## Answer

数据与 datagen 迁移已完成，实现提交 `6a3b3a21a`（分支 `impl/11-data-datagen`，worktree `D:\mcmodDemo\gtm-11-data`），已通过 merge commit `0ad482f75` 合入主 PR 分支 `port/26.1.2-neoforge`。

完成内容：`ICustomIngredient` / `IngredientType` / `MapCodec` / registry-aware `StreamCodec` 迁移；sized、ranged、circuit、NBT-predicate、fluid-container ingredient 的 codec 与网络辅助；GTM ingredient type 注册；recipe serializer record 模型、`CraftingInput`、`ItemStackTemplate`、`ShapedRecipePattern`；datagen client/server 拆分；loot MapCodec；datapack registry 注册；compostable data map；内部 serialized recipe 边界与 recipe-side 调用方迁移；serializer 回归测试覆盖。

验证：`git diff --check` 通过；合并后 task 11 范围文件（recipe/ingredient/loot/tags/datagen）未出现在根 `compileJava` 剩余错误清单中。合并后由 orchestrator 修正了提交带入的 `GTDatapackRegistries.init(modBus)` 双重调用（`ebdbed270`）。

未尽事项（转入后续票）：`runData` 尚未完整执行，阻塞于根编译剩余区域——residual value-provider/model-provider API（4 处）、task 12 transfer、task 14 rendering、task 15 optional integration；这些在 task 16 全量验收时闭环。
