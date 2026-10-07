package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\id\decode_stage_id.sv.
class decode_stage_id extends Component {
  setDefinitionName("decode_stage_id")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val if_id_reg_q = in(if_id_reg_t())
    val rd_addr_wb = in(UInt(5 bits))
    val rd_data_wb = in(UInt(32 bits))
    val rd_we_wb = in(Bool())
    val rs1_forward_mux_sel = in(UInt(2 bits))
    val rs2_forward_mux_sel = in(UInt(2 bits))
    // Outputs
    val id_ex_reg_d = out(id_ex_reg_t())
    val illegal_instr_id = out(Bool())
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val opcode_id = UInt(7 bits)
    val funct3_id = UInt(3 bits)
    val funct7_id = UInt(7 bits)
    val rs1_id = UInt(5 bits)
    val rs2_id = UInt(5 bits)
    val rd_id = UInt(5 bits)
    val imm_type_id = UInt(3 bits)
    val reg_we_id = Bool()
    val mem_re_id = Bool()
    val mem_we_id = Bool()
    val wb_sel_id = UInt(3 bits)
    val alu_op_id = UInt(4 bits)
    val pc_sel_id = UInt(3 bits)
    val alu_src_a_sel_id = UInt(2 bits)
    val alu_src_b_sel_id = UInt(2 bits)
    val memsize_id = UInt(3 bits)
    val memsign_id = UInt(1 bits)
    val uses_rs1_id = Bool()
    val uses_rs2_id = Bool()
    val rs1_data_id = UInt(32 bits)
    val rs2_data_id = UInt(32 bits)
    val imm_id = UInt(32 bits)
    val decoder = new decoder_id
    decoder.setName("decoder")
    // Inputs
    decoder.io.instruction := if_id_reg_q.instruction
    // Outputs
    opcode_id := decoder.io.opcode
    imm_type_id := decoder.io.imm_type
    funct3_id := decoder.io.funct3
    funct7_id := decoder.io.funct7
    rs1_id := decoder.io.rs1
    rs2_id := decoder.io.rs2
    rd_id := decoder.io.rd
    
    val control = new control_id
    control.setName("control")
    // Inputs
    control.io.rs1 := rs1_id
    control.io.rs2 := rs2_id
    control.io.rd := rd_id
    control.io.funct3 := funct3_id
    control.io.funct7 := funct7_id
    control.io.opcode := opcode_id
    // Outputs
    reg_we_id := control.io.reg_we
    mem_re_id := control.io.mem_re
    mem_we_id := control.io.mem_we
    wb_sel_id := control.io.wb_sel
    alu_op_id := control.io.alu_op
    pc_sel_id := control.io.pc_sel
    illegal_instr_id := control.io.illegal_instr
    alu_src_a_sel_id := control.io.alu_src_a_sel
    alu_src_b_sel_id := control.io.alu_src_b_sel
    memsize_id := control.io.memsize
    memsign_id := control.io.memsign
    uses_rs1_id := control.io.uses_rs1
    uses_rs2_id := control.io.uses_rs2
    
    val registers = new registers_id_wb
    registers.setName("registers")
    // Inputs
    registers.io.clk := clk
    registers.io.reset_n := reset_n
    registers.io.rs1_addr := rs1_id
    registers.io.rs2_addr := rs2_id
    registers.io.rd_addr := rd_addr_wb
    registers.io.rd_data := rd_data_wb
    registers.io.rd_we := rd_we_wb
    // Outputs
    rs1_data_id := registers.io.rs1_data
    rs2_data_id := registers.io.rs2_data
    
    val imm_gen = new imm_gen_id
    imm_gen.setName("imm_gen")
    // Inputs
    imm_gen.io.instruction := if_id_reg_q.instruction
    imm_gen.io.imm_type := imm_type_id
    // Outputs
    imm_id := imm_gen.io.imm_out
    
    id_ex_reg_d.pc := if_id_reg_q.pc
    id_ex_reg_d.pcplus4 := if_id_reg_q.pcplus4
    id_ex_reg_d.rs1_data := rs1_data_id
    id_ex_reg_d.rs2_data := rs2_data_id
    id_ex_reg_d.rd := rd_id
    id_ex_reg_d.imm := imm_id
    id_ex_reg_d.rs1 := rs1_id
    id_ex_reg_d.rs2 := rs2_id
    id_ex_reg_d.uses_rs1 := uses_rs1_id
    id_ex_reg_d.uses_rs2 := uses_rs2_id
    id_ex_reg_d.alu_src_a_sel := alu_src_a_sel_id
    id_ex_reg_d.alu_src_b_sel := alu_src_b_sel_id
    id_ex_reg_d.alu_op := alu_op_id
    id_ex_reg_d.reg_we := reg_we_id
    id_ex_reg_d.mem_re := mem_re_id
    id_ex_reg_d.mem_we := mem_we_id
    id_ex_reg_d.memsize := memsize_id
    id_ex_reg_d.memsign := memsign_id
    id_ex_reg_d.wb_sel := wb_sel_id
    id_ex_reg_d.pc_sel := pc_sel_id
    id_ex_reg_d.predicted_pc := if_id_reg_q.predicted_pc
    id_ex_reg_d.predicted_taken := if_id_reg_q.predicted_taken
    id_ex_reg_d.funct3 := funct3_id
    id_ex_reg_d.rs1_forward_mux_sel := rs1_forward_mux_sel
    id_ex_reg_d.rs2_forward_mux_sel := rs2_forward_mux_sel
    id_ex_reg_d.valid := if_id_reg_q.valid
  }
}
