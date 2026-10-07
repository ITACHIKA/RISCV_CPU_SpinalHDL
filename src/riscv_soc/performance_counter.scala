package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\performance_counter.sv.
class performance_counter extends Component {
  setDefinitionName("performance_counter")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val perf_addr = in(UInt(32 bits))
    val perf_wren = in(Bool())
    val perf_rden = in(Bool())
    val perf_wdata = in(UInt(32 bits))
    val perf_wstrb = in(UInt(4 bits))
    val cpu_perf_events = in(cpu_perf_t())
    // Outputs
    val perf_rdata = out(UInt(32 bits))
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val perf_ctrl_reg = Reg(UInt(32 bits))
    val cycle_counter = Reg(UInt(64 bits))
    val retired_instr_counter = Reg(UInt(32 bits))
    val branch_counter = Reg(UInt(32 bits))
    val branch_miss_counter = Reg(UInt(32 bits))
    val cpu_stall_counter = Reg(UInt(32 bits))
    val read_data = Reg(UInt(32 bits))
    perf_rdata := read_data
    val write_mask = UInt(32 bits)
    for(lane <- 0 until 4) write_mask(8*lane+7 downto 8*lane) := Mux(perf_wstrb(lane), U(255,8 bits), U(0,8 bits))
    val offset = perf_addr(11 downto 0)
    // Original module allows a simultaneous write to override control reset.
    when(!reset_n) { perf_ctrl_reg := 0 }
    when(perf_wren) {
      switch(offset) {
        is(0) { perf_ctrl_reg := perf_wdata }
        is(4) { perf_ctrl_reg := perf_ctrl_reg | (perf_wdata & write_mask) }
        is(8) { perf_ctrl_reg := perf_ctrl_reg & ~(perf_wdata & write_mask) }
      }
    }.elsewhen(perf_rden) {
      switch(offset) {
        is(0) { read_data := perf_ctrl_reg }
        is(12) { read_data := cycle_counter(31 downto 0) }
        is(16) { read_data := cycle_counter(63 downto 32) }
        is(20) { read_data := retired_instr_counter }
        is(24) { read_data := branch_counter }
        is(28) { read_data := branch_miss_counter }
        is(32) { read_data := cpu_stall_counter }
      }
    }
    when(!reset_n) {
      cycle_counter := 0; retired_instr_counter := 0; branch_counter := 0; branch_miss_counter := 0; cpu_stall_counter := 0
    }.elsewhen(perf_ctrl_reg(0)) {
      cycle_counter := cycle_counter + 1
      when(cpu_perf_events.retired_instr) { retired_instr_counter := retired_instr_counter + 1 }
      when(cpu_perf_events.branch_taken) { branch_counter := branch_counter + 1 }
      when(cpu_perf_events.branch_miss) { branch_miss_counter := branch_miss_counter + 1 }
      when(cpu_perf_events.cpu_stall) { cpu_stall_counter := cpu_stall_counter + 1 }
    }
  }
}
