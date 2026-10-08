package riscv

import spinal.core._
import riscv.RiscvPkg._

// Encodings and payload widths match the original SystemVerilog package.
object RiscvPkg {
  val XLEN = 32
  val PC_START = 0x00000000L
  val BTB_BITS = 3
  val BTB_ENTRIES = 8
  val BHT_BITS = 5
  val BHT_ENTRIES = 32
  // Declaration-order binary codes with the original SV storage widths.
  // binarySequential alone would shrink WB/MEM-size and ALU-source controls.
  private def svBinary(width: Int): SpinalEnumEncoding = new SpinalEnumEncoding {
    setName(s"svBinary$width")
    override def getWidth(enum: SpinalEnum): Int = {
      require(binarySequential.getWidth(enum) <= width, "Enum exceeds its SV width")
      width
    }
    override def getValue[T <: SpinalEnum](element: SpinalEnumElement[T]): BigInt =
      binarySequential.getValue(element)
    override def getElement[T <: SpinalEnum](value: BigInt, enum: T): SpinalEnumElement[T] =
      binarySequential.getElement(value, enum)
  }

  object ImmType extends SpinalEnum(svBinary(3)) {
    val NONE, I, S, B, U, J = newElement()
  }

  object WbSel extends SpinalEnum(svBinary(3)) {
    val ALU, MEM, PC = newElement()
  }

  object PcSel extends SpinalEnum(svBinary(3)) {
    val NEXT, BRANCH, JAL, JALR, TRAP = newElement()
  }

  object MemSize extends SpinalEnum(svBinary(3)) {
    setName("MemSizeEnum")
    val BYTE, HALF, WORD = newElement()
  }

  object MemSign extends SpinalEnum(svBinary(1)) {
    setName("MemSignEnum")
    val UNSIGNED, SIGNED = newElement()
  }

  object AluOp extends SpinalEnum(svBinary(4)) {
    val ADD, SUB, XOR, OR, AND, SLL = newElement()
    val SRL, SRA, SLT, SLTU, COPY_B, INVALID = newElement()
  }

  object AluSrcA extends SpinalEnum(svBinary(2)) {
    val RS1, PC = newElement()
  }

  object AluSrcB extends SpinalEnum(svBinary(2)) {
    val RS2, IMM = newElement()
  }

  object BranchPredictType extends SpinalEnum(svBinary(2)) {
    val NONE, CONDITIONAL, JAL = newElement()
  }

  object RsForwardSel extends SpinalEnum(svBinary(2)) {
    val NONE, MEM, WB = newElement()
  }

  object MmioWbSel extends SpinalEnum(svBinary(3)) {
    val NONE, IMEM, DMEM, GPIO, UART, TIMER = newElement()
    val SYSCTRL = newElement()
  }

  object BootMode extends SpinalEnum(svBinary(1)) {
    // Keep the original "bootmode" port name in case-insensitive backends.
    setName("BootModeEnum")
    val DOWNLOAD, NORMAL = newElement()
  }

  // Instruction bit patterns are encoded ISA fields, not internal control enums.
  def OPCODE_LOAD = U("7'b0000011")
  def OPCODE_STORE = U("7'b0100011")
  def OPCODE_OP = U("7'b0110011")
  def OPCODE_OP_IMM = U("7'b0010011")
  def OPCODE_AUIPC = U("7'b0010111")
  def OPCODE_LUI = U("7'b0110111")
  def OPCODE_BRANCH = U("7'b1100011")
  def OPCODE_JALR = U("7'b1100111")
  def OPCODE_JAL = U("7'b1101111")
  def F3_BEQ = U("3'b000")
  def F3_BNE = U("3'b001")
  def F3_BLT = U("3'b100")
  def F3_BGE = U("3'b101")
  def F3_BLTU = U("3'b110")
  def F3_BGEU = U("3'b111")
  def F3_LB = U("3'b000")
  def F3_LH = U("3'b001")
  def F3_LW = U("3'b010")
  def F3_LBU = U("3'b100")
  def F3_LHU = U("3'b101")
  def F3_SB = U("3'b000")
  def F3_SH = U("3'b001")
  def F3_SW = U("3'b010")
  def F3_ADDI = U("3'b000")
  def F3_SLTI = U("3'b010")
  def F3_SLTIU = U("3'b011")
  def F3_XORI = U("3'b100")
  def F3_ORI = U("3'b110")
  def F3_ANDI = U("3'b111")
  def F3_SLLI = U("3'b001")
  def F3_SRLI = U("3'b101")
  def F3_SRAI = U("3'b101")
  def F7_SLLI = U("7'b0000000")
  def F7_SRLI = U("7'b0000000")
  def F7_SRAI = U("7'b0100000")
  def F3_ADD = U("3'b000")
  def F3_SUB = U("3'b000")
  def F3_SLL = U("3'b001")
  def F3_SLT = U("3'b010")
  def F3_SLTU = U("3'b011")
  def F3_XOR = U("3'b100")
  def F3_SRL = U("3'b101")
  def F3_SRA = U("3'b101")
  def F3_OR = U("3'b110")
  def F3_AND = U("3'b111")
  def F7_ADD = U("7'b0000000")
  def F7_SUB = U("7'b0100000")
  def F7_SLL = U("7'b0000000")
  def F7_SLT = U("7'b0000000")
  def F7_SLTU = U("7'b0000000")
  def F7_XOR = U("7'b0000000")
  def F7_SRL = U("7'b0000000")
  def F7_SRA = U("7'b0100000")
  def F7_OR = U("7'b0000000")
  def F7_AND = U("7'b0000000")
}

case class if_id_reg_t() extends Bundle {
  val valid = Bool()
  val pc = UInt(32 bits)
  val pcplus4 = UInt(32 bits)
  val instruction = UInt(32 bits)
  val predicted_pc = UInt(32 bits)
  val predicted_taken = Bool()
}

case class id_ex_reg_t() extends Bundle {
  val valid = Bool()
  val pc = UInt(32 bits)
  val pcplus4 = UInt(32 bits)
  val rs1_data = UInt(32 bits)
  val rs2_data = UInt(32 bits)
  val rd = UInt(5 bits)
  val imm = UInt(32 bits)
  val rs1 = UInt(5 bits)
  val rs2 = UInt(5 bits)
  val uses_rs1 = Bool()
  val uses_rs2 = Bool()
  val funct3 = UInt(3 bits)
  val alu_src_a_sel = AluSrcA()
  val alu_src_b_sel = AluSrcB()
  val alu_op = AluOp()
  val reg_we = Bool()
  val mem_re = Bool()
  val mem_we = Bool()
  val memsize = MemSize()
  val memsign = MemSign()
  val wb_sel = WbSel()
  val pc_sel = PcSel()
  val predicted_pc = UInt(32 bits)
  val predicted_taken = Bool()
  val rs1_forward_mux_sel = RsForwardSel()
  val rs2_forward_mux_sel = RsForwardSel()
}

case class ex_mem_reg_t() extends Bundle {
  val valid = Bool()
  val pc = UInt(32 bits)
  val pcplus4 = UInt(32 bits)
  val alu_result = UInt(32 bits)
  val rs2_data = UInt(32 bits)
  val rd = UInt(5 bits)
  val rs1 = UInt(5 bits)
  val rs2 = UInt(5 bits)
  val reg_we = Bool()
  val mem_re = Bool()
  val mem_we = Bool()
  val memsize = MemSize()
  val memsign = MemSign()
  val wb_sel = WbSel()
  val forward_data = UInt(32 bits)
  val redirect_request = Bool()
  val redirect_request_pc = UInt(32 bits)
  val predicted_pc = UInt(32 bits)
  val predicted_taken = Bool()
  val actual_taken = Bool()
  val btb_update_valid = Bool()
  val btb_target_pc = UInt(32 bits)
  val btb_update_type = BranchPredictType()
}

case class mem_wb_reg_t() extends Bundle {
  val valid = Bool()
  val pcplus4 = UInt(32 bits)
  val alu_result = UInt(32 bits)
  val rs2_data = UInt(32 bits)
  val rd = UInt(5 bits)
  val memsize = MemSize()
  val memsign = MemSign()
  val mmio_rden = Bool()
  val reg_we = Bool()
  val wb_sel = WbSel()
}

case class pc_predict_result_t() extends Bundle {
  val predict_taken = Bool()
  val predicted_pc = UInt(32 bits)
}

case class btb_entry_t() extends Bundle {
  val valid = Bool()
  val tag = UInt(10 bits)
  val target_pc = UInt(32 bits)
  val predict_type = BranchPredictType()
}

case class cpu_perf_t() extends Bundle {
  val retired_instr = Bool()
  val branch_taken = Bool()
  val branch_miss = Bool()
  val cpu_stall = Bool()
}
