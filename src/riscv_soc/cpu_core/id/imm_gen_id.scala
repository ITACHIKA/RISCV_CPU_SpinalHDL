package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\id\imm_gen_id.sv.
class imm_gen_id extends Component {
  setDefinitionName("imm_gen_id")
  val io = new Bundle {
    // Inputs
    val instruction = in(UInt(32 bits))
    val imm_type = in(UInt(3 bits))
    // Outputs
    val imm_out = out(UInt(32 bits))
  }
  noIoPrefix()
  import io._
  imm_out := 0
  switch(imm_type) {
    is(IMM_I) { imm_out := instruction(31 downto 20).asSInt.resize(32).asUInt }
    is(IMM_S) { imm_out := (instruction(31 downto 25) ## instruction(11 downto 7)).asSInt.resize(32).asUInt }
    is(IMM_B) { imm_out := (instruction(31) ## instruction(7) ## instruction(30 downto 25) ## instruction(11 downto 8) ## False).asSInt.resize(32).asUInt }
    is(IMM_U) { imm_out := (instruction(31 downto 12) ## U(0,12 bits)).asUInt }
    is(IMM_J) { imm_out := (instruction(31) ## instruction(19 downto 12) ## instruction(20) ## instruction(30 downto 21) ## False).asSInt.resize(32).asUInt }
  }
}
