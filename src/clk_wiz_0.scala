package riscv

import spinal.core._

/** External Vivado Clocking Wizard IP, using the original project configuration.
  * This is the only vendor BlackBox; the CPU and SoC are native SpinalHDL.
  * Supply the existing clk_wiz_0 IP in the FPGA project (100 MHz CPU clock).
  */
class clk_wiz_0 extends BlackBox {
  setDefinitionName("clk_wiz_0")
  val io = new Bundle {
    // Inputs
    val clk_in1 = in Bool()
    val reset = in Bool()
    // Outputs
    val clk_out1 = out Bool()
    val locked = out Bool()
  }
  noIoPrefix()
}
