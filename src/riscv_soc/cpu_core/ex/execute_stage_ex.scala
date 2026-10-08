package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\ex\execute_stage_ex.sv.
class execute_stage_ex extends Component {
  setDefinitionName("execute_stage_ex")
  val io = new Bundle {
    // Inputs
    val id_ex_reg_q = in(id_ex_reg_t())
    val ex_mem_reg_q = in(ex_mem_reg_t())
    val wb_data_wb = in(UInt(32 bits))
    // Outputs
    val ex_mem_reg_d = out(ex_mem_reg_t())
  }
  noIoPrefix()
  import io._
  val mem_forward_result_ex = UInt(32 bits)
  val rs1_forward_result_ex = UInt(32 bits)
  val rs2_forward_result_ex = UInt(32 bits)
  val alu_a_ex = UInt(32 bits)
  val alu_b_ex = UInt(32 bits)
  val alu_result_ex = UInt(32 bits)
  val eq_ex = Bool()
  val less_signed_ex = Bool()
  val less_unsigned_ex = Bool()
  val branch_taken_ex = Bool()
  val redirect_pc_request_ex = Bool()
  val redirect_next_pc_ex = UInt(32 bits)
  val btb_target_pc_ex = UInt(32 bits)
  val btb_update_type_ex = BranchPredictType()
  val btb_update_valid_ex = Bool()
  val btb_actual_taken_ex = Bool()
  val comparator = new comparator_ex
  comparator.setName("comparator")
  // Inputs
  comparator.io.a := alu_a_ex
  comparator.io.b := alu_b_ex
  // Outputs
  eq_ex := comparator.io.eq
  less_signed_ex := comparator.io.less_signed
  less_unsigned_ex := comparator.io.less_unsigned
  
  val branch = new branch_ex
  branch.setName("branch")
  // Inputs
  branch.io.funct3 := id_ex_reg_q.funct3
  branch.io.eq := eq_ex
  branch.io.less_signed := less_signed_ex
  branch.io.less_unsigned := less_unsigned_ex
  // Outputs
  branch_taken_ex := branch.io.take
  
  val alu = new alu_ex
  alu.setName("alu")
  // Inputs
  alu.io.a := alu_a_ex
  alu.io.b := alu_b_ex
  alu.io.alu_op := id_ex_reg_q.alu_op
  alu.io.less_signed := less_signed_ex
  alu.io.less_unsigned := less_unsigned_ex
  // Outputs
  alu_result_ex := alu.io.result
  
  val control_flow_resolver = new control_flow_resolver_ex
  control_flow_resolver.setName("control_flow_resolver")
  // Inputs
  control_flow_resolver.io.current_pc := id_ex_reg_q.pc
  control_flow_resolver.io.predicted_pc := id_ex_reg_q.predicted_pc
  control_flow_resolver.io.predicted_taken := id_ex_reg_q.predicted_taken
  control_flow_resolver.io.branch_taken := branch_taken_ex
  control_flow_resolver.io.alu_result := alu_result_ex
  control_flow_resolver.io.imm := id_ex_reg_q.imm
  control_flow_resolver.io.pc_sel := id_ex_reg_q.pc_sel
  control_flow_resolver.io.valid_ex := id_ex_reg_q.valid
  // Outputs
  redirect_pc_request_ex := control_flow_resolver.io.redirect_pc_request
  redirect_next_pc_ex := control_flow_resolver.io.redirect_next_pc
  btb_target_pc_ex := control_flow_resolver.io.btb_target_pc
  btb_update_valid_ex := control_flow_resolver.io.btb_update_valid
  btb_update_type_ex := control_flow_resolver.io.btb_update_type
  btb_actual_taken_ex := control_flow_resolver.io.btb_actual_taken
  
  mem_forward_result_ex := ex_mem_reg_q.forward_data
  def forward(sel: SpinalEnumCraft[RsForwardSel.type], original: UInt): UInt = {
    val value = UInt(32 bits)
    value := 0
    switch(sel) {
      is(RsForwardSel.NONE) { value := original }
      is(RsForwardSel.MEM) { value := mem_forward_result_ex }
      is(RsForwardSel.WB) { value := wb_data_wb }
    }
    value
  }
  rs1_forward_result_ex := forward(id_ex_reg_q.rs1_forward_mux_sel, id_ex_reg_q.rs1_data)
  rs2_forward_result_ex := forward(id_ex_reg_q.rs2_forward_mux_sel, id_ex_reg_q.rs2_data)
  alu_a_ex := 0
  switch(id_ex_reg_q.alu_src_a_sel) {
    is(AluSrcA.RS1) { alu_a_ex := rs1_forward_result_ex }
    is(AluSrcA.PC) { alu_a_ex := id_ex_reg_q.pc }
  }
  alu_b_ex := 0
  switch(id_ex_reg_q.alu_src_b_sel) {
    is(AluSrcB.RS2) { alu_b_ex := rs2_forward_result_ex }
    is(AluSrcB.IMM) { alu_b_ex := id_ex_reg_q.imm }
  }
  ex_mem_reg_d.forward_data := Mux(id_ex_reg_q.wb_sel === WbSel.PC, id_ex_reg_q.pcplus4, alu_result_ex)
  ex_mem_reg_d.pc := id_ex_reg_q.pc
  ex_mem_reg_d.pcplus4 := id_ex_reg_q.pcplus4
  ex_mem_reg_d.alu_result := alu_result_ex
  ex_mem_reg_d.rs2_data := rs2_forward_result_ex
  ex_mem_reg_d.rd := id_ex_reg_q.rd
  ex_mem_reg_d.rs1 := id_ex_reg_q.rs1
  ex_mem_reg_d.rs2 := id_ex_reg_q.rs2
  ex_mem_reg_d.reg_we := id_ex_reg_q.reg_we
  ex_mem_reg_d.mem_re := id_ex_reg_q.mem_re
  ex_mem_reg_d.mem_we := id_ex_reg_q.mem_we
  ex_mem_reg_d.memsize := id_ex_reg_q.memsize
  ex_mem_reg_d.memsign := id_ex_reg_q.memsign
  ex_mem_reg_d.wb_sel := id_ex_reg_q.wb_sel
  ex_mem_reg_d.predicted_taken := id_ex_reg_q.predicted_taken
  ex_mem_reg_d.actual_taken := btb_actual_taken_ex
  ex_mem_reg_d.predicted_pc := id_ex_reg_q.predicted_pc
  ex_mem_reg_d.redirect_request := redirect_pc_request_ex
  ex_mem_reg_d.redirect_request_pc := redirect_next_pc_ex
  ex_mem_reg_d.btb_target_pc := btb_target_pc_ex
  ex_mem_reg_d.btb_update_valid := btb_update_valid_ex
  ex_mem_reg_d.btb_update_type := btb_update_type_ex
  ex_mem_reg_d.valid := id_ex_reg_q.valid
}
