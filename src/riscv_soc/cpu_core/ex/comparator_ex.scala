package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\ex\comparator_ex.sv.
class comparator_ex extends Component {
  setDefinitionName("comparator_ex")
  val io = new Bundle {
    // Inputs
    val a = in(UInt(32 bits))
    val b = in(UInt(32 bits))
    // Outputs
    val eq = out(Bool())
    val less_signed = out(Bool())
    val less_unsigned = out(Bool())
  }
  noIoPrefix()
  import io._
  io.eq := a === b
  less_signed := a.asSInt < b.asSInt
  less_unsigned := a < b
}
