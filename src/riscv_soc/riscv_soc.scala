package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\riscv_soc.sv.
class riscv_soc(bootImage: Option[String] = Some("bootloader.mem")) extends Component {
  setDefinitionName("riscv_soc")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val gpio_btn_in = in(Bool())
    val uart_rx = in(Bool())
    // Outputs
    val gpio_led_out = out(Bool())
    val uart_tx = out(Bool())
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val imem_req_valid_if = Bool()
    val imem_req_ready_if = Bool()
    val imem_req_addr_if = UInt(32 bits)
    val imem_resp_valid_if = Bool()
    val imem_resp_ready_if = Bool()
    val imem_resp_data_if = UInt(32 bits)
    val imem_flush_if = Bool()
    val data_req_valid_mem = Bool()
    val data_req_write_mem = Bool()
    val data_req_addr_mem = UInt(32 bits)
    val data_req_wdata_mem = UInt(32 bits)
    val data_req_wstrb_mem = UInt(4 bits)
    val data_resp_rdata_wb = UInt(32 bits)
    val imem_resolved_rden_mem = Bool()
    val imem_resolved_wren_mem = Bool()
    val dmem_resolved_wren_mem = Bool()
    val dmem_resolved_rden_mem = Bool()
    val gpio_resolved_wren_mem = Bool()
    val gpio_resolved_rden_mem = Bool()
    val uart_resolved_wren_mem = Bool()
    val uart_resolved_rden_mem = Bool()
    val timer_resolved_wren_mem = Bool()
    val timer_resolved_rden_mem = Bool()
    val sysctrl_resolved_wren = Bool()
    val sysctrl_resolved_rden = Bool()
    val imem_rdata_wb = UInt(32 bits)
    val dmem_rdata_wb = UInt(32 bits)
    val gpio_rdata_wb = UInt(32 bits)
    val uart_rdata_wb = UInt(32 bits)
    val timer_rdata_wb = UInt(32 bits)
    val sysctrl_rdata_wb = UInt(32 bits)
    val mmio_wb_sel_wb = UInt(3 bits)
    val reset_vector = UInt(32 bits)
    val bootmode = UInt(1 bits)
    val core = new riscv_cpu
    core.setName("core")
    // Inputs
    core.io.clk := clk
    core.io.reset_n := reset_n
    core.io.imem_req_ready_if := imem_req_ready_if
    core.io.imem_resp_valid_if := imem_resp_valid_if
    core.io.imem_resp_data_if := imem_resp_data_if
    core.io.data_resp_rdata_wb := data_resp_rdata_wb
    core.io.reset_vector := reset_vector
    // Outputs
    imem_req_valid_if := core.io.imem_req_valid_if
    imem_req_addr_if := core.io.imem_req_addr_if
    imem_resp_ready_if := core.io.imem_resp_ready_if
    imem_flush_if := core.io.imem_flush_if
    data_req_valid_mem := core.io.data_req_valid_mem
    data_req_write_mem := core.io.data_req_write_mem
    data_req_addr_mem := core.io.data_req_addr_mem
    data_req_wdata_mem := core.io.data_req_wdata_mem
    data_req_wstrb_mem := core.io.data_req_wstrb_mem
    
    val imem = new imem_if(bootImage)
    imem.setName("imem")
    // Inputs
    imem.io.clk := clk
    imem.io.reset_n := reset_n
    imem.io.porta_addr := imem_req_addr_if
    imem.io.req_valid := imem_req_valid_if
    imem.io.resp_ready := imem_resp_ready_if
    imem.io.flush := imem_flush_if
    imem.io.portb_addr := data_req_addr_mem
    imem.io.portb_rden := imem_resolved_rden_mem
    imem.io.portb_wren := imem_resolved_wren_mem
    imem.io.portb_wdata := data_req_wdata_mem
    imem.io.portb_wstrb := data_req_wstrb_mem
    // Outputs
    imem_resp_data_if := imem.io.porta_rdata
    imem_req_ready_if := imem.io.req_ready
    imem_resp_valid_if := imem.io.resp_valid
    imem_rdata_wb := imem.io.portb_rdata
    
    val address_resolver_mem = new address_resolver_mem
    address_resolver_mem.setName("address_resolver_mem")
    // Inputs
    address_resolver_mem.io.clk := clk
    address_resolver_mem.io.reset_n := reset_n
    address_resolver_mem.io.addr := data_req_addr_mem
    address_resolver_mem.io.mmio_device_wren := data_req_valid_mem && data_req_write_mem
    address_resolver_mem.io.mmio_device_rden := data_req_valid_mem && !data_req_write_mem
    // Outputs
    mmio_wb_sel_wb := address_resolver_mem.io.mmio_wb_sel
    imem_resolved_rden_mem := address_resolver_mem.io.imem_resolved_rden
    imem_resolved_wren_mem := address_resolver_mem.io.imem_resolved_wren
    dmem_resolved_wren_mem := address_resolver_mem.io.dmem_resolved_wren
    dmem_resolved_rden_mem := address_resolver_mem.io.dmem_resolved_rden
    gpio_resolved_wren_mem := address_resolver_mem.io.gpio_resolved_wren
    gpio_resolved_rden_mem := address_resolver_mem.io.gpio_resolved_rden
    uart_resolved_wren_mem := address_resolver_mem.io.uart_resolved_wren
    uart_resolved_rden_mem := address_resolver_mem.io.uart_resolved_rden
    timer_resolved_wren_mem := address_resolver_mem.io.timer_resolved_wren
    timer_resolved_rden_mem := address_resolver_mem.io.timer_resolved_rden
    sysctrl_resolved_wren := address_resolver_mem.io.sysctrl_resolved_wren
    sysctrl_resolved_rden := address_resolver_mem.io.sysctrl_resolved_rden
    
    val dmem = new dmem_mem
    dmem.setName("dmem")
    // Inputs
    dmem.io.clk := clk
    dmem.io.wren := dmem_resolved_wren_mem
    dmem.io.rden := dmem_resolved_rden_mem
    dmem.io.addr := data_req_addr_mem
    dmem.io.wdata := data_req_wdata_mem
    dmem.io.wstrb := data_req_wstrb_mem
    // Outputs
    dmem_rdata_wb := dmem.io.rdata
    
    val gpio = new gpio
    gpio.setName("gpio")
    // Inputs
    gpio.io.clk := clk
    gpio.io.reset_n := reset_n
    gpio.io.gpio_wren := gpio_resolved_wren_mem
    gpio.io.gpio_rden := gpio_resolved_rden_mem
    gpio.io.gpio_addr := data_req_addr_mem
    gpio.io.gpio_wdata := data_req_wdata_mem
    gpio.io.gpio_wstrb := data_req_wstrb_mem
    gpio.io.gpio_btn_in := gpio_btn_in
    // Outputs
    gpio_rdata_wb := gpio.io.rdata
    gpio_led_out := gpio.io.gpio_led_out
    
    val uart0 = new uart
    uart0.setName("uart0")
    // Inputs
    uart0.io.clk := clk
    uart0.io.reset_n := reset_n
    uart0.io.uart_wren := uart_resolved_wren_mem
    uart0.io.uart_rden := uart_resolved_rden_mem
    uart0.io.uart_addr := data_req_addr_mem
    uart0.io.uart_wdata := data_req_wdata_mem
    uart0.io.uart_wstrb := data_req_wstrb_mem
    uart0.io.uart_rx := uart_rx
    // Outputs
    uart_rdata_wb := uart0.io.uart_rdata
    uart_tx := uart0.io.uart_tx
    
    val timer = new timer
    timer.setName("timer")
    // Inputs
    timer.io.clk := clk
    timer.io.reset_n := reset_n
    timer.io.timer_addr := data_req_addr_mem
    timer.io.timer_wren := timer_resolved_wren_mem
    timer.io.timer_rden := timer_resolved_rden_mem
    timer.io.timer_wdata := data_req_wdata_mem
    timer.io.timer_wstrb := data_req_wstrb_mem
    // Outputs
    timer_rdata_wb := timer.io.timer_rdata
    
    val system_control = new system_control
    system_control.setName("system_control")
    // Inputs
    system_control.io.clk := clk
    system_control.io.reset_n := reset_n
    system_control.io.sysctrl_wren := sysctrl_resolved_wren
    system_control.io.sysctrl_rden := sysctrl_resolved_rden
    system_control.io.sysctrl_addr := data_req_addr_mem
    system_control.io.sysctrl_wdata := data_req_wdata_mem
    system_control.io.sysctrl_wstrb := data_req_wstrb_mem
    // Outputs
    sysctrl_rdata_wb := system_control.io.rdata
    bootmode := system_control.io.bootmode
    
    data_resp_rdata_wb := 0
    switch(mmio_wb_sel_wb) {
      is(MMIO_WB_SEL_IMEM) { data_resp_rdata_wb := imem_rdata_wb }
      is(MMIO_WB_SEL_DMEM) { data_resp_rdata_wb := dmem_rdata_wb }
      is(MMIO_WB_SEL_GPIO) { data_resp_rdata_wb := gpio_rdata_wb }
      is(MMIO_WB_SEL_UART) { data_resp_rdata_wb := uart_rdata_wb }
      is(MMIO_WB_SEL_TIMER) { data_resp_rdata_wb := timer_rdata_wb }
      is(MMIO_WB_SEL_SYSCTRL) { data_resp_rdata_wb := sysctrl_rdata_wb }
    }
    reset_vector := Mux(bootmode === BOOTMODE_DOWNLOAD, U(0,32 bits), U(0x1000,32 bits))
  }
}
