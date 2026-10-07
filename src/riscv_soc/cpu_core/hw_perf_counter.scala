package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\hw_perf_counter.sv.
class hw_perf_counter extends Component {
  setDefinitionName("hw_perf_counter")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val branch = in(Bool())
    val branch_miss = in(Bool())
    val retired_instr = in(Bool())
    val cpu_stall = in(Bool())
    // Outputs
    val cycle_count = out(UInt(64 bits))
    val branch_count = out(UInt(32 bits))
    val branch_miss_count = out(UInt(32 bits))
    val retired_instr_count = out(UInt(32 bits))
    val cpu_stall_count = out(UInt(32 bits))
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val cycle_counter = Reg(UInt(64 bits))
    val branch_counter = Reg(UInt(32 bits))
    val branch_miss_counter = Reg(UInt(32 bits))
    val retired_instr_counter = Reg(UInt(32 bits))
    val cpu_stall_counter = Reg(UInt(32 bits))
    cycle_count := cycle_counter
    branch_count := branch_counter
    branch_miss_count := branch_miss_counter
    retired_instr_count := retired_instr_counter
    cpu_stall_count := cpu_stall_counter
    when(!reset_n) {
      cycle_counter := 0; branch_counter := 0; branch_miss_counter := 0; retired_instr_counter := 0; cpu_stall_counter := 0
    }.otherwise {
      cycle_counter := cycle_counter + 1
      when(branch) { branch_counter := branch_counter + 1 }
      when(branch_miss) { branch_miss_counter := branch_miss_counter + 1 }
      when(retired_instr) { retired_instr_counter := retired_instr_counter + 1 }
      when(cpu_stall) { cpu_stall_counter := cpu_stall_counter + 1 }
    }
  }
}
