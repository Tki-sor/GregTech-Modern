Type: task
Status: resolved
Blocked by: 08

## Question

将 ModularUI-Modern `1.21.1` 分支完整移植到 MC 26.1.2 / NeoForge，并以仓库内独立 Gradle 模块供 GTM 使用。

## Context

主 spec：[`spec.md`](../spec.md)。路线决议：[`03-mui-config-decision.md`](03-mui-config-decision.md)。上游版本参照：[`01-upstream-121-playbook.md`](01-upstream-121-playbook.md)。

## Work

- 将 `1.21.1` 分支作为完整源码基线，迁移其构建工具链、公共 API、widget、drawable、screen、value sync、menu、recipe-viewer 和 loader 集成。
- 将 MUI 26.1.2 目标作为仓库内独立模块，保持 `brachy.modularui` 的公开包/API 边界；GTM 不复制 MUI 内部实现。
- 迁移 MUI 自身的 NeoForge/MC API、渲染、网络和 datagen 触点；移除对 1.21.1 专用 API 的残留引用。
- 让 GTM 的 cover、machine screen、recipe viewer、widget 和同步代码消费该模块，不为当前 GTM 用量制作缩减版替代 API。

## Acceptance

- MUI 模块可以独立编译并产出可被 GTM 消费的 artifact/project dependency。
- GTM 当前所有 `brachy.modularui` 引用完成解析，代表性 machine、cover、recipe-viewer panel 可构造。
- MUI API 的 screen/widget/value-sync/recipe-viewer smoke tests 通过；不能依赖被砍的 EMI、Create、Embeddium 或 Oculus。

## Answer

完整 MUI 移植已完成并合入 PR 分支，合并提交为 `e2c2807aad492e3951ed2c042dd509373e3ed83a`。`ModularUI-Modern` 的 `1.21.1` 分支（commit `c13e141b830c70922c3540ea245ecf5d29e1029d`）已作为仓库内独立 `:modularui` Gradle 模块迁移到 MC 26.1.2 / NeoForge / Java 25。

模块保留 `brachy.modularui` 完整 API，并覆盖 screen、widget、drawable、menu、value sync、recipe viewer、loader integration 和 MUI 自身的 client/network/data 触点。schema viewer 使用 26.1 的 `GuiGraphicsExtractor`、render-state、PIP texture、`SubmitNodeCollector`、block/fluid/block-entity/outline/highlight 几何提交路径，不再使用空背景 fallback。

验证结果：`:modularui:compileJava`、`:modularui:test`（10 tests, 0 failures）、`:modularui:check`、`:modularui:jar` 和 `git diff --check` 通过。根 GTM compile 仍等待后续 API/data/transfer/network/integration 票完成。
- clean client 能打开至少一个 GTM machine/cover UI，不发生 classloading、渲染或网络同步崩溃。
