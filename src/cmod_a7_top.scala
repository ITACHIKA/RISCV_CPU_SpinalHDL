package riscv

import spinal.core._

class cmod_a7_top(bootImage: Option[String] = Some("bootloader.mem")) extends Component {
  setDefinitionName("cmod_a7_top")
  val io = new Bundle {
    // Inputs
    val sysclk = in Bool()
    val reset = in Bool()
    val btn = in UInt(2 bits)
    val pio2 = in Bool()
    // Outputs
    val led = out UInt(1 bits)
    val pio1 = out Bool()
  }
  noIoPrefix()

  val clk_wiz_inst = new clk_wiz_0
  // Inputs
  clk_wiz_inst.io.clk_in1 := io.sysclk
  clk_wiz_inst.io.reset := io.reset

  // Outputs
  val clk = clk_wiz_inst.io.clk_out1
  val clk_locked = clk_wiz_inst.io.locked
  val resetRelease = new ClockingArea(ClockDomain(
    clock = clk, reset = clk_locked,
    config = ClockDomainConfig(resetKind = ASYNC, resetActiveLevel = LOW))) {
    val reset_sync_ff = Reg(UInt(2 bits)) init(0)
    reset_sync_ff.initial(U(0, 2 bits))
    reset_sync_ff := (reset_sync_ff(0) ## True).asUInt
  }
  val reset_n = resetRelease.reset_sync_ff(1)
  val buttonSync = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val gpio_btn_sync_ff = Reg(UInt(2 bits))
    gpio_btn_sync_ff.addAttribute("ASYNC_REG", "TRUE")
    when(!reset_n) { gpio_btn_sync_ff := 0 }
    .otherwise { gpio_btn_sync_ff := (gpio_btn_sync_ff(0) ## io.btn(1)).asUInt }
  }

  val soc = new riscv_soc(bootImage)
  // Inputs
  soc.io.clk := clk
  soc.io.reset_n := reset_n
  soc.io.gpio_btn_in := buttonSync.gpio_btn_sync_ff(1)
  soc.io.uart_rx := io.pio2

  // Outputs
  io.led(0) := soc.io.gpio_led_out
  io.pio1 := soc.io.uart_tx
}
