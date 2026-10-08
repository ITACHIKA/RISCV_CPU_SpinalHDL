package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\id\decoder_id.sv.
class decoder_id extends Component {
  setDefinitionName("decoder_id")
  val io = new Bundle {
    // Inputs
    val instruction = in(UInt(32 bits))
    // Outputs
    val opcode = out(UInt(7 bits))
    val imm_type = out(ImmType())
    val funct3 = out(UInt(3 bits))
    val funct7 = out(UInt(7 bits))
    val rs1 = out(UInt(5 bits))
    val rs2 = out(UInt(5 bits))
    val rd = out(UInt(5 bits))
  }
  noIoPrefix()
  import io._
  opcode := instruction(6 downto 0)
  funct3 := instruction(14 downto 12)
  funct7 := instruction(31 downto 25)
  rs1 := instruction(19 downto 15)
  rs2 := instruction(24 downto 20)
  rd := instruction(11 downto 7)
  imm_type := ImmType.NONE
  switch(opcode) {
    is(OPCODE_LOAD, OPCODE_OP_IMM, OPCODE_JALR) { imm_type := ImmType.I }
    is(OPCODE_STORE) { imm_type := ImmType.S }
    is(OPCODE_BRANCH) { imm_type := ImmType.B }
    is(OPCODE_JAL) { imm_type := ImmType.J }
    is(OPCODE_LUI, OPCODE_AUIPC) { imm_type := ImmType.U }
  }
}
