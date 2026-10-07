# RISC-V SpinalHDL

Standalone sbt project for the migrated five-stage RV32I CPU, SoC, and Cmod A7 top. The Scala source hierarchy follows the original SystemVerilog project. Z7, firmware sources, and software utilities are not included.

## Requirements

- Java 17, sbt 1.10.2, Scala 2.13.14, SpinalHDL 1.12.3.
- Icarus Verilog and a native C/C++ toolchain for the Scala waveform simulation.
- The existing WSL environment provides these tools. sbt resolves the Scala and SpinalHDL dependencies declared in `build.sbt`.

## Compile and generate RTL

Run from WSL:

```sh
cd /mnt/e/ITACHIKA/FPGA/cpu/RISCV_SpinalHDL
sbt compile
sbt 'runMain riscv.Generate cpu generated/cpu'
sbt 'runMain riscv.Generate soc generated/soc --no-image'
sbt 'runMain riscv.Generate cmod generated/cmod --no-image'
```

`--no-image` permits hardware elaboration without firmware; it leaves instruction memory uninitialized. For a bootable FPGA build, replace it with the actual external bootloader image:

```sh
sbt 'runMain riscv.Generate cmod generated/cmod /absolute/path/to/bootloader.mem'
```

The generator accepts `cpu`, `soc`, `cmod`, or `peripherals`, followed by an output directory and an optional image path. If the image argument is omitted, it uses `bootloader.mem` in the working directory. CPU-only generation does not require an image.

Images use the original word-addressed hexadecimal format, including `@` address directives, as produced by `objcopy --verilog-data-width=4`. Missing locations in a supplied sparse image are initialized to zero. Firmware is supplied externally and is not bundled here.

## Waveform simulation

```sh
sbt 'runMain riscv.cpu_tb /absolute/path/to/bootloader.mem simWorkspace 50000'
```

This runs the SoC with a 100 MHz clock, two reset cycles, and UART RX idle high. The optional final argument is the simulation cycle count. Waveforms are written under `simWorkspace/riscv_soc/test/`. This runner generates waveforms; it is not a self-checking ISA test.

## Layout

```text
build.sbt                       Dependency and source configuration
project/build.properties        sbt version
src/                            Scala source root
  Generate.scala                RTL generation entry point
  cpu_tb.scala                  Scala waveform simulation entry point
  cmod_a7_top.scala              Cmod A7 board wrapper
  clk_wiz_0.scala                Existing Vivado clock-IP interface
  riscv_soc/                    SoC integration, memories, peripherals, types
    cpu_core/                   Core integration, pipeline registers, hazards
      if/ id/ ex/ mem/ wb/       Original pipeline stage hierarchy
```

Compilation creates `target/`; generation creates RTL and memory initialization files in the selected output directory. These are build outputs, not source files.

## FPGA integration and validation

The CPU and SoC are native SpinalHDL. Cmod clock generation uses the existing Vivado `clk_wiz_0` IP through a Scala BlackBox declaration. A Vivado FPGA build also requires the original Cmod clock-IP configuration, board constraints, and a real bootloader image. Include generated memory initialization files alongside generated RTL and ensure their paths resolve.

Migration validation includes CPU and peripheral cycle differential simulations, integrated SoC execution, and Cmod reset/button checks. Vivado synthesis, implementation, timing closure, and physical board validation have not been performed for the migrated design. Detailed results and migration history are in `../SPINAL_MIGRATION_MEMORY.md`.
