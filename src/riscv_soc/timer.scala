package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\timer.sv.
class timer extends Component {
  setDefinitionName("timer")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val timer_addr = in(UInt(32 bits))
    val timer_wren = in(Bool())
    val timer_rden = in(Bool())
    val timer_wdata = in(UInt(32 bits))
    val timer_wstrb = in(UInt(4 bits))
    // Outputs
    val timer_rdata = out(UInt(32 bits))
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val timer_ctrl_reg = Reg(UInt(32 bits))
    val time_count = Reg(UInt(64 bits))
    val read_data = Reg(UInt(32 bits))
    timer_rdata := read_data
    val write_mask = UInt(32 bits)
    for(lane <- 0 until 4) write_mask(8*lane+7 downto 8*lane) := Mux(timer_wstrb(lane), U(255,8 bits), U(0,8 bits))
    val offset = timer_addr(11 downto 0)
    val soft_reset = timer_wren && offset === 4 && timer_wstrb(0) && timer_wdata(2)
    val result_clear = timer_wren && offset === 4 && timer_wstrb(0) && timer_wdata(1)
    when(!reset_n || soft_reset) { timer_ctrl_reg := 0; read_data := 0 }
    .otherwise {
      when(timer_wren) {
        when(offset === 4) { timer_ctrl_reg := timer_ctrl_reg | (write_mask & timer_wdata) }
        .elsewhen(offset === 8) { timer_ctrl_reg := timer_ctrl_reg & ~(write_mask & timer_wdata) }
      }.elsewhen(timer_rden) {
        switch(offset) {
          is(0) { read_data := timer_ctrl_reg }
          is(12) { read_data := time_count(31 downto 0) }
          is(16) { read_data := time_count(63 downto 32) }
        }
      }
    }
    when(!reset_n || soft_reset || result_clear) { time_count := 0 }
    .elsewhen(timer_ctrl_reg(0)) { time_count := time_count + 1 }
  }
}
