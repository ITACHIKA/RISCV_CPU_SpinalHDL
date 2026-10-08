package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\ex\alu_ex.sv.
class alu_ex extends Component {
  setDefinitionName("alu_ex")
  val io = new Bundle {
    // Inputs
    val a = in(UInt(32 bits))
    val b = in(UInt(32 bits))
    val alu_op = in(AluOp())
    val less_signed = in(Bool())
    val less_unsigned = in(Bool())
    // Outputs
    val result = out(UInt(32 bits))
  }
  noIoPrefix()
  import io._
  result := 0
  switch(alu_op) {
    is(AluOp.ADD) { result := a + b }
    is(AluOp.SUB) { result := a - b }
    is(AluOp.AND) { result := a & b }
    is(AluOp.OR) { result := a | b }
    is(AluOp.XOR) { result := a ^ b }
    is(AluOp.SLL) { result := (a |<< b(4 downto 0)).resized }
    is(AluOp.SRL) { result := a |>> b(4 downto 0) }
    is(AluOp.SRA) { result := (a.asSInt >> b(4 downto 0)).asUInt }
    is(AluOp.SLT) { result := less_signed.asUInt.resize(32) }
    is(AluOp.SLTU) { result := less_unsigned.asUInt.resize(32) }
    is(AluOp.COPY_B) { result := b }
  }
}
