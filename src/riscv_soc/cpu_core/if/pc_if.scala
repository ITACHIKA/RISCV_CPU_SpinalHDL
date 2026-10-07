package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\if\pc_if.sv.
class pc_if extends Component {
  setDefinitionName("pc_if")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val next_pc = in(UInt(32 bits))
    val reset_vector = in(UInt(32 bits))
    val pc_update_enable = in(Bool())
    // Outputs
    val current_pc = out(UInt(32 bits))
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val pc = Reg(UInt(32 bits))
    when(!reset_n) { pc := reset_vector }.elsewhen(pc_update_enable) { pc := next_pc }
    current_pc := pc
  }
}
