package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\address_resolver_mem.sv.
class address_resolver_mem extends Component {
  setDefinitionName("address_resolver_mem")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val addr = in(UInt(32 bits))
    val mmio_device_wren = in(Bool())
    val mmio_device_rden = in(Bool())
    // Outputs
    val mmio_wb_sel = out(UInt(3 bits))
    val dmem_resolved_wren = out(Bool())
    val dmem_resolved_rden = out(Bool())
    val imem_resolved_rden = out(Bool())
    val imem_resolved_wren = out(Bool())
    val gpio_resolved_wren = out(Bool())
    val gpio_resolved_rden = out(Bool())
    val uart_resolved_wren = out(Bool())
    val uart_resolved_rden = out(Bool())
    val timer_resolved_wren = out(Bool())
    val timer_resolved_rden = out(Bool())
    val sysctrl_resolved_wren = out(Bool())
    val sysctrl_resolved_rden = out(Bool())
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    // Preserve the original decoded windows, including aliasing and exclusive upper bounds.
    val write = mmio_device_wren
    val read = mmio_device_rden && !write
    def window(lo: Long, hi: Long): Bool = addr >= U(lo,32 bits) && addr < U(hi,32 bits)
    dmem_resolved_wren := write && window(0x80000000L,0x90000000L)
    dmem_resolved_rden := read && window(0x80000000L,0x8fffffffL)
    imem_resolved_wren := write && window(0x00001000L,0x10000000L)
    imem_resolved_rden := read && addr < U(0x0fffffffL,32 bits)
    gpio_resolved_wren := write && window(0x10000000L,0x10001000L)
    gpio_resolved_rden := read && window(0x10000000L,0x10001000L)
    uart_resolved_wren := write && window(0x10001000L,0x10002000L)
    uart_resolved_rden := read && window(0x10001000L,0x10002000L)
    timer_resolved_wren := write && window(0x10002000L,0x10003000L)
    timer_resolved_rden := read && window(0x10002000L,0x10003000L)
    sysctrl_resolved_wren := write && window(0x1000f000L,0x10010000L)
    sysctrl_resolved_rden := read && window(0x1000f000L,0x10010000L)
    val selected = Reg(UInt(3 bits))
    mmio_wb_sel := selected
    when(!reset_n) { selected := MMIO_WB_SEL_NONE }.otherwise {
      selected := MMIO_WB_SEL_NONE
      when(imem_resolved_rden) { selected := MMIO_WB_SEL_IMEM }
      .elsewhen(dmem_resolved_rden) { selected := MMIO_WB_SEL_DMEM }
      .elsewhen(gpio_resolved_rden) { selected := MMIO_WB_SEL_GPIO }
      .elsewhen(uart_resolved_rden) { selected := MMIO_WB_SEL_UART }
      .elsewhen(timer_resolved_rden) { selected := MMIO_WB_SEL_TIMER }
      .elsewhen(sysctrl_resolved_rden) { selected := MMIO_WB_SEL_SYSCTRL }
    }
  }
}
