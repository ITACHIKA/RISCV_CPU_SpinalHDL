package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\ex\branch_ex.sv.
class branch_ex extends Component {
  setDefinitionName("branch_ex")
  val io = new Bundle {
    // Inputs
    val funct3 = in(UInt(3 bits))
    val eq = in(Bool())
    val less_signed = in(Bool())
    val less_unsigned = in(Bool())
    // Outputs
    val take = out(Bool())
  }
  noIoPrefix()
  import io._
  take := False
  switch(funct3) {
    is(F3_BEQ) { take := io.eq }
    is(F3_BNE) { take := !io.eq }
    is(F3_BLT) { take := less_signed }
    is(F3_BGE) { take := !less_signed }
    is(F3_BLTU) { take := less_unsigned }
    is(F3_BGEU) { take := !less_unsigned }
  }
}
