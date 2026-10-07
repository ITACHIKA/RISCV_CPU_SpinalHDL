package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\id\control_id.sv.
class control_id extends Component {
  setDefinitionName("control_id")
  val io = new Bundle {
    // Inputs
    val rs1 = in(UInt(5 bits))
    val rs2 = in(UInt(5 bits))
    val rd = in(UInt(5 bits))
    val funct3 = in(UInt(3 bits))
    val funct7 = in(UInt(7 bits))
    val opcode = in(UInt(7 bits))
    // Outputs
    val reg_we = out(Bool())
    val mem_re = out(Bool())
    val mem_we = out(Bool())
    val wb_sel = out(UInt(3 bits))
    val alu_op = out(UInt(4 bits))
    val pc_sel = out(UInt(3 bits))
    val illegal_instr = out(Bool())
    val alu_src_a_sel = out(UInt(2 bits))
    val alu_src_b_sel = out(UInt(2 bits))
    val uses_rs1 = out(Bool())
    val uses_rs2 = out(Bool())
    val memsize = out(UInt(3 bits))
    val memsign = out(UInt(1 bits))
  }
  noIoPrefix()
  import io._
  reg_we := False
  mem_re := False
  mem_we := False
  wb_sel := WB_ALU
  alu_op := ALU_INVALID
  pc_sel := PC_NEXT
  memsize := MEM_BYTE
  memsign := MEM_SIGNED
  illegal_instr := False
  alu_src_a_sel := ALU_SRC_A_RS1
  alu_src_b_sel := ALU_SRC_B_RS2
  uses_rs1 := False
  uses_rs2 := False
  switch(opcode) {
    is(OPCODE_OP, OPCODE_OP_IMM) {
      reg_we := True
      uses_rs1 := True
      uses_rs2 := opcode === OPCODE_OP
      when(opcode === OPCODE_OP_IMM) { alu_src_b_sel := ALU_SRC_B_IMM }
      switch(funct3) {
        is(F3_ADD) {
          when(opcode === OPCODE_OP_IMM || funct7 === F7_ADD) { alu_op := ALU_ADD }
          .elsewhen(funct7 === F7_SUB) { alu_op := ALU_SUB }
          .otherwise { illegal_instr := True }
        }
        is(F3_SLL) {
          when(funct7 === F7_SLL) { alu_op := ALU_SLL }.otherwise { illegal_instr := True }
        }
        is(F3_SRL) {
          when(funct7 === F7_SRL) { alu_op := ALU_SRL }
          .elsewhen(funct7 === F7_SRA) { alu_op := ALU_SRA }
          .otherwise { illegal_instr := True }
        }
        is(F3_SLT, F3_SLTU, F3_XOR, F3_OR, F3_AND) {
          when(opcode === OPCODE_OP_IMM || funct7 === 0) {
            switch(funct3) {
              is(F3_SLT) { alu_op := ALU_SLT }
              is(F3_SLTU) { alu_op := ALU_SLTU }
              is(F3_XOR) { alu_op := ALU_XOR }
              is(F3_OR) { alu_op := ALU_OR }
              is(F3_AND) { alu_op := ALU_AND }
            }
          }.otherwise { illegal_instr := True }
        }
      }
    }
    is(OPCODE_LUI, OPCODE_AUIPC, OPCODE_JAL, OPCODE_JALR) {
      reg_we := True
      alu_src_b_sel := ALU_SRC_B_IMM
      alu_op := ALU_ADD
      switch(opcode) {
        is(OPCODE_LUI) { alu_op := ALU_COPY_B }
        is(OPCODE_AUIPC) { alu_src_a_sel := ALU_SRC_A_PC }
        is(OPCODE_JAL) { alu_src_a_sel := ALU_SRC_A_PC; wb_sel := WB_PC; pc_sel := PC_JAL }
        is(OPCODE_JALR) { uses_rs1 := True; wb_sel := WB_PC; pc_sel := PC_JALR }
      }
    }
    is(OPCODE_BRANCH) { pc_sel := PC_BRANCH; uses_rs1 := True; uses_rs2 := True }
    is(OPCODE_LOAD, OPCODE_STORE) {
      val load = opcode === OPCODE_LOAD
      reg_we := load
      mem_re := load
      mem_we := !load
      when(load) { wb_sel := WB_MEM }
      alu_src_b_sel := ALU_SRC_B_IMM
      alu_op := ALU_ADD
      uses_rs1 := True
      uses_rs2 := !load
      switch(funct3) {
        is(F3_LB) { memsize := MEM_BYTE }
        is(F3_LH) { memsize := MEM_HALF }
        is(F3_LW) { memsize := MEM_WORD }
        is(F3_LBU) {
          memsign := MEM_UNSIGNED
          when(!load) { memsize := MEM_HALF; illegal_instr := True }
        }
        is(F3_LHU) { memsize := MEM_HALF; memsign := MEM_UNSIGNED; illegal_instr := !load }
        default { memsize := MEM_HALF; memsign := MEM_UNSIGNED; illegal_instr := True }
      }
    }
    default { illegal_instr := True }
  }
}
