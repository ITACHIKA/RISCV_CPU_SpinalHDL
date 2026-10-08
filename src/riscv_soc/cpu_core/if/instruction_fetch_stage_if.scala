package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\if\instruction_fetch_stage_if.sv.
class instruction_fetch_stage_if extends Component {
  setDefinitionName("instruction_fetch_stage_if")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val stall_if = in(Bool())
    val redirect_request_mem = in(Bool())
    val redirect_pc_mem = in(UInt(32 bits))
    val imem_req_ready_if = in(Bool())
    val imem_resp_valid_if = in(Bool())
    val imem_resp_data_if = in(UInt(32 bits))
    val reset_vector = in(UInt(32 bits))
    val btb_feedback_pc_mem = in(UInt(32 bits))
    val btb_feedback_actual_target_mem = in(UInt(32 bits))
    val btb_feedback_taken_mem = in(Bool())
    val btb_feedback_valid_mem = in(Bool())
    val btb_feedback_predict_type_mem = in(BranchPredictType())
    // Outputs
    val imem_req_valid_if = out(Bool())
    val imem_req_addr_if = out(UInt(32 bits))
    val imem_resp_ready_if = out(Bool())
    val imem_flush_if = out(Bool())
    val if_id_reg_d = out(if_id_reg_t())
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val current_pc_if = UInt(32 bits)
    val current_pc_imem_if = UInt(32 bits)
    val next_pc_if = UInt(32 bits)
    val imem_req_fire_if = Bool()
    val imem_resp_fire_if = Bool()
    val pc_update_enable_if = Bool()
    val pc_predict_result_if = pc_predict_result_t()
    val pc_predict_result_imem_if = pc_predict_result_t()
    val fetch_pc_if = UInt(32 bits)
    val branch_predict = new branch_predict_if
    branch_predict.setName("branch_predict")
    // Inputs
    branch_predict.io.clk := clk
    branch_predict.io.reset_n := reset_n
    branch_predict.io.current_pc := fetch_pc_if
    branch_predict.io.btb_feedback_pc := btb_feedback_pc_mem
    branch_predict.io.btb_feedback_actual_target := btb_feedback_actual_target_mem
    branch_predict.io.btb_feedback_taken := btb_feedback_taken_mem
    branch_predict.io.btb_feedback_valid := btb_feedback_valid_mem
    branch_predict.io.btb_feedback_predict_type := btb_feedback_predict_type_mem
    // Outputs
    pc_predict_result_if := branch_predict.io.branch_predict_result
    
    val pc = new pc_if
    pc.setName("pc")
    // Inputs
    pc.io.clk := clk
    pc.io.reset_n := reset_n
    pc.io.next_pc := next_pc_if
    pc.io.reset_vector := reset_vector
    pc.io.pc_update_enable := pc_update_enable_if
    // Outputs
    current_pc_if := pc.io.current_pc
    
    fetch_pc_if := Mux(redirect_request_mem, redirect_pc_mem, current_pc_if)
    imem_req_valid_if := !stall_if || redirect_request_mem
    imem_resp_ready_if := !stall_if
    imem_flush_if := redirect_request_mem
    imem_req_addr_if := fetch_pc_if
    imem_req_fire_if := imem_req_valid_if && imem_req_ready_if
    imem_resp_fire_if := imem_resp_valid_if && imem_resp_ready_if
    next_pc_if := pc_predict_result_if.predicted_pc
    pc_update_enable_if := imem_req_fire_if
    val accepted_pc = Reg(UInt(32 bits))
    val accepted_prediction = Reg(pc_predict_result_t())
    current_pc_imem_if := accepted_pc
    pc_predict_result_imem_if := accepted_prediction
    when(!reset_n) {
      accepted_pc := 0
      accepted_prediction.assignFromBits(B(0,accepted_prediction.getBitsWidth bits))
    }.elsewhen(imem_req_fire_if) {
      accepted_pc := fetch_pc_if
      accepted_prediction := pc_predict_result_if
    }
    if_id_reg_d.pc := current_pc_imem_if
    if_id_reg_d.pcplus4 := current_pc_imem_if + 4
    if_id_reg_d.instruction := imem_resp_data_if
    if_id_reg_d.predicted_pc := pc_predict_result_imem_if.predicted_pc
    if_id_reg_d.predicted_taken := pc_predict_result_imem_if.predict_taken
    if_id_reg_d.valid := imem_resp_fire_if
  }
}
