# Project memory

## User requirements

- Update this repository-root memory as work progresses.
- Native SpinalHDL migration of the original RV32I CPU, SoC, and Cmod A7 wrapper. Z7 is excluded.
- Keep original module hierarchy under `src/`; keep sbt metadata at project root.
- Do not migrate C, assembly, utilities, or production firmware into the project.
- Normal generation requires a bootloader: `sbt run` defaults to Cmod with `bootloader.mem`; the public generator rejects `--no-image`. CPU-only generation requires no image.
- Preserve unrelated user changes. The project now has its own Git repository.

## Completed work: native enumerations

- Replaced 12 numeric control groups in `src/riscv_soc/riscv_pkg.scala` with typed `SpinalEnum` objects: `ImmType`, `WbSel`, `PcSel`, `MemSize`, `MemSign`, `AluOp`, `AluSrcA`, `AluSrcB`, `BranchPredictType`, `RsForwardSel`, `MmioWbSel`, and `BootMode`.
- Updated module ports, internal control wires, forwarding helper types, pipeline Bundles, and boot-mode register to use enum types and qualified members such as `AluOp.ADD`.
- UART receive state also uses native `UartRxState` instead of numeric states.
- `svBinary(width)` preserves original SV declaration-order codes and reserved storage widths. Plain `binarySequential` would shrink some original interfaces.
- Emitted names `MemSizeEnum`, `MemSignEnum`, and `BootModeEnum` prevent case-insensitive name conflicts with original module ports; Scala uses the shorter type names.
- Opcode/funct fields remain encoded ISA bit patterns, now written in binary notation.
- Compilation and elaboration passed. Final encoding audit verified all 50 package enum members, 23 original enum ports (including names and widths), and four UART states against the original SV definitions.
- Differential regressions passed: 10 CPU runs (five original assembly tests with/without fetch backpressure), 7,100 peripheral cycles with 47 nonzero UART loopback reads, five integrated SoC assembly tests, SoC MMIO/boot-reset smoke test, and 92 Cmod reset/button cycles. CPU and SoC regressions were rerun after the final generated-name fixes.
- Logs under `../../.spinal-migration/`: `enum-final-build.log`, `enum-encoding-audit.log`, `enum-cpu-diff.log`, `enum-peripheral-diff.log`, `enum-soc-diff.log`, and `enum-cmod-diff.log`.
- Refactor is complete. Generated outputs used for verification are in the external harness; regenerate project RTL with the real bootloader before FPGA use. No production firmware or vendor IP was changed.
- External logs, harnesses, and pre-refactor source snapshot are under `../../.spinal-migration/`; they are not delivered HDL sources.

## Build and verification

```sh
sbt compile
sbt run
sbt 'runMain riscv.Generate cpu generated/cpu'
sbt 'runMain riscv.Generate cmod generated/cmod /absolute/path/to/bootloader.mem'
```

- sbt 1.10.2, Scala 2.13.14, SpinalHDL 1.12.3, Java 17 in WSL.
- External images used in migration regression are test fixtures, not the production bootloader.
- Vivado synthesis, implementation, timing closure, and physical board validation have not been run.
- Earlier migration history: `../SPINAL_MIGRATION_MEMORY.md`; workspace summary: `../../MEMORY.md`.
