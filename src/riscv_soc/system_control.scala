package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\system_control.sv.
class system_control extends Component {
  setDefinitionName("system_control")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val sysctrl_wren = in(Bool())
    val sysctrl_rden = in(Bool())
    val sysctrl_addr = in(UInt(32 bits))
    val sysctrl_wdata = in(UInt(32 bits))
    val sysctrl_wstrb = in(UInt(4 bits))
    // Outputs
    val rdata = out(UInt(32 bits))
    val bootmode = out(BootMode())
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    // Configuration-time initialization only: external CPU reset must retain boot mode.
    val bootmode_reg = Reg(BootMode()) init(BootMode.DOWNLOAD)
    bootmode := bootmode_reg
    when(sysctrl_wren && sysctrl_addr === U(0x1000f000L,32 bits)) {
      bootmode_reg := Mux(sysctrl_wdata(0), BootMode.NORMAL, BootMode.DOWNLOAD)
    }
    // Original RTL leaves this output undriven. Define reserved reads as zero.
    // Preserve original writes, which ignore byte strobes and reset_n.
    rdata := 0
  }
}
