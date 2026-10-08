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
    val wb_sel = out(WbSel())
    val alu_op = out(AluOp())
    val pc_sel = out(PcSel())
    val illegal_instr = out(Bool())
    val alu_src_a_sel = out(AluSrcA())
    val alu_src_b_sel = out(AluSrcB())
    val uses_rs1 = out(Bool())
    val uses_rs2 = out(Bool())
    val memsize = out(MemSize())
    val memsign = out(MemSign())
  }
  noIoPrefix()
  import io._
  reg_we := False
  mem_re := False
  mem_we := False
  wb_sel := WbSel.ALU
  alu_op := AluOp.INVALID
  pc_sel := PcSel.NEXT
  memsize := MemSize.BYTE
  memsign := MemSign.SIGNED
  illegal_instr := False
  alu_src_a_sel := AluSrcA.RS1
  alu_src_b_sel := AluSrcB.RS2
  uses_rs1 := False
  uses_rs2 := False
  switch(opcode) {
    is(OPCODE_OP, OPCODE_OP_IMM) {
      reg_we := True
      uses_rs1 := True
      uses_rs2 := opcode === OPCODE_OP
      when(opcode === OPCODE_OP_IMM) { alu_src_b_sel := AluSrcB.IMM }
      switch(funct3) {
        is(F3_ADD) {
          when(opcode === OPCODE_OP_IMM || funct7 === F7_ADD) { alu_op := AluOp.ADD }
          .elsewhen(funct7 === F7_SUB) { alu_op := AluOp.SUB }
          .otherwise { illegal_instr := True }
        }
        is(F3_SLL) {
          when(funct7 === F7_SLL) { alu_op := AluOp.SLL }.otherwise { illegal_instr := True }
        }
        is(F3_SRL) {
          when(funct7 === F7_SRL) { alu_op := AluOp.SRL }
          .elsewhen(funct7 === F7_SRA) { alu_op := AluOp.SRA }
          .otherwise { illegal_instr := True }
        }
        is(F3_SLT, F3_SLTU, F3_XOR, F3_OR, F3_AND) {
          when(opcode === OPCODE_OP_IMM || funct7 === 0) {
            switch(funct3) {
              is(F3_SLT) { alu_op := AluOp.SLT }
              is(F3_SLTU) { alu_op := AluOp.SLTU }
              is(F3_XOR) { alu_op := AluOp.XOR }
              is(F3_OR) { alu_op := AluOp.OR }
              is(F3_AND) { alu_op := AluOp.AND }
            }
          }.otherwise { illegal_instr := True }
        }
      }
    }
    is(OPCODE_LUI, OPCODE_AUIPC, OPCODE_JAL, OPCODE_JALR) {
      reg_we := True
      alu_src_b_sel := AluSrcB.IMM
      alu_op := AluOp.ADD
      switch(opcode) {
        is(OPCODE_LUI) { alu_op := AluOp.COPY_B }
        is(OPCODE_AUIPC) { alu_src_a_sel := AluSrcA.PC }
        is(OPCODE_JAL) { alu_src_a_sel := AluSrcA.PC; wb_sel := WbSel.PC; pc_sel := PcSel.JAL }
        is(OPCODE_JALR) { uses_rs1 := True; wb_sel := WbSel.PC; pc_sel := PcSel.JALR }
      }
    }
    is(OPCODE_BRANCH) { pc_sel := PcSel.BRANCH; uses_rs1 := True; uses_rs2 := True }
    is(OPCODE_LOAD, OPCODE_STORE) {
      val load = opcode === OPCODE_LOAD
      reg_we := load
      mem_re := load
      mem_we := !load
      when(load) { wb_sel := WbSel.MEM }
      alu_src_b_sel := AluSrcB.IMM
      alu_op := AluOp.ADD
      uses_rs1 := True
      uses_rs2 := !load
      switch(funct3) {
        is(F3_LB) { memsize := MemSize.BYTE }
        is(F3_LH) { memsize := MemSize.HALF }
        is(F3_LW) { memsize := MemSize.WORD }
        is(F3_LBU) {
          memsign := MemSign.UNSIGNED
          when(!load) { memsize := MemSize.HALF; illegal_instr := True }
        }
        is(F3_LHU) { memsize := MemSize.HALF; memsign := MemSign.UNSIGNED; illegal_instr := !load }
        default { memsize := MemSize.HALF; memsign := MemSign.UNSIGNED; illegal_instr := True }
      }
    }
    default { illegal_instr := True }
  }
}
