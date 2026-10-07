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
    val alu_op = in(UInt(4 bits))
    val less_signed = in(Bool())
    val less_unsigned = in(Bool())
    // Outputs
    val result = out(UInt(32 bits))
  }
  noIoPrefix()
  import io._
  result := 0
  switch(alu_op) {
    is(ALU_ADD) { result := a + b }
    is(ALU_SUB) { result := a - b }
    is(ALU_AND) { result := a & b }
    is(ALU_OR) { result := a | b }
    is(ALU_XOR) { result := a ^ b }
    is(ALU_SLL) { result := (a |<< b(4 downto 0)).resized }
    is(ALU_SRL) { result := a |>> b(4 downto 0) }
    is(ALU_SRA) { result := (a.asSInt >> b(4 downto 0)).asUInt }
    is(ALU_SLT) { result := less_signed.asUInt.resize(32) }
    is(ALU_SLTU) { result := less_unsigned.asUInt.resize(32) }
    is(ALU_COPY_B) { result := b }
  }
}
