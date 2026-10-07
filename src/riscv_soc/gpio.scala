package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\gpio.sv.
class gpio extends Component {
  setDefinitionName("gpio")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val gpio_wren = in(Bool())
    val gpio_rden = in(Bool())
    val gpio_addr = in(UInt(32 bits))
    val gpio_wdata = in(UInt(32 bits))
    val gpio_wstrb = in(UInt(4 bits))
    val gpio_btn_in = in(Bool())
    // Outputs
    val rdata = out(UInt(32 bits))
    val gpio_led_out = out(Bool())
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val led_reg = Reg(UInt(32 bits))
    val read_data = Reg(UInt(32 bits))
    gpio_led_out := led_reg(0)
    rdata := read_data
    when(!reset_n) { led_reg := 0 }
    .elsewhen(gpio_wren && gpio_addr(12 downto 0) === 0) {
      for(lane <- 0 until 4) {
        when(gpio_wstrb(lane)) { led_reg(8*lane+7 downto 8*lane) := gpio_wdata(8*lane+7 downto 8*lane) }
      }
    }
    read_data := 0
    when(gpio_rden) {
      when(gpio_addr(12 downto 0) === 0) { read_data := led_reg }
      .elsewhen(gpio_addr(12 downto 0) === 4) { read_data := gpio_btn_in.asUInt.resize(32) }
    }
  }
}
