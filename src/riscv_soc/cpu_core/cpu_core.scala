package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\cpu_core.sv.
class riscv_cpu extends Component {
  setDefinitionName("riscv_cpu")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val imem_req_ready_if = in(Bool())
    val imem_resp_valid_if = in(Bool())
    val imem_resp_data_if = in(UInt(32 bits))
    val data_resp_rdata_wb = in(UInt(32 bits))
    val reset_vector = in(UInt(32 bits))
    // Outputs
    val imem_req_valid_if = out(Bool())
    val imem_req_addr_if = out(UInt(32 bits))
    val imem_resp_ready_if = out(Bool())
    val imem_flush_if = out(Bool())
    val data_req_valid_mem = out(Bool())
    val data_req_write_mem = out(Bool())
    val data_req_addr_mem = out(UInt(32 bits))
    val data_req_wdata_mem = out(UInt(32 bits))
    val data_req_wstrb_mem = out(UInt(4 bits))
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val if_id_reg_q = if_id_reg_t()
    val if_id_reg_d = if_id_reg_t()
    val id_ex_reg_q = id_ex_reg_t()
    val id_ex_reg_d = id_ex_reg_t()
    val ex_mem_reg_q = ex_mem_reg_t()
    val ex_mem_reg_d = ex_mem_reg_t()
    val mem_wb_reg_q = mem_wb_reg_t()
    val mem_wb_reg_d = mem_wb_reg_t()
    val redirect_pc_request_mem = Bool()
    val redirect_next_pc_mem = UInt(32 bits)
    val load_use_stall_if = Bool()
    val rs1_forward_mux_sel_id = RsForwardSel()
    val rs2_forward_mux_sel_id = RsForwardSel()
    val rd_addr_wb = UInt(5 bits)
    val wb_data_wb = UInt(32 bits)
    val wb_forward_data_wb = UInt(32 bits)
    val wb_forward_valid_mem = Bool()
    val rd_we_wb = Bool()
    val illegal_instr_id = Bool()
    val load_misalign_except_mem = Bool()
    val store_misalign_except_mem = Bool()
    val exception_core = Bool()
    val cycle_count = UInt(64 bits)
    val branch_count = UInt(32 bits)
    val branch_miss_count = UInt(32 bits)
    val retired_instr_count = UInt(32 bits)
    val cpu_stall_count = UInt(32 bits)
    val if_stage = new instruction_fetch_stage_if
    if_stage.setName("if_stage")
    // Inputs
    if_stage.io.clk := clk
    if_stage.io.reset_n := reset_n
    if_stage.io.stall_if := load_use_stall_if
    if_stage.io.redirect_request_mem := redirect_pc_request_mem
    if_stage.io.redirect_pc_mem := redirect_next_pc_mem
    if_stage.io.imem_req_ready_if := imem_req_ready_if
    if_stage.io.imem_resp_valid_if := imem_resp_valid_if
    if_stage.io.imem_resp_data_if := imem_resp_data_if
    if_stage.io.reset_vector := reset_vector
    if_stage.io.btb_feedback_pc_mem := ex_mem_reg_q.pc
    if_stage.io.btb_feedback_actual_target_mem := ex_mem_reg_q.btb_target_pc
    if_stage.io.btb_feedback_taken_mem := ex_mem_reg_q.actual_taken
    if_stage.io.btb_feedback_valid_mem := ex_mem_reg_q.btb_update_valid && ex_mem_reg_q.valid
    if_stage.io.btb_feedback_predict_type_mem := ex_mem_reg_q.btb_update_type
    // Outputs
    imem_req_valid_if := if_stage.io.imem_req_valid_if
    imem_req_addr_if := if_stage.io.imem_req_addr_if
    imem_resp_ready_if := if_stage.io.imem_resp_ready_if
    imem_flush_if := if_stage.io.imem_flush_if
    if_id_reg_d := if_stage.io.if_id_reg_d
    
    val decode_stage = new decode_stage_id
    decode_stage.setName("decode_stage")
    // Inputs
    decode_stage.io.clk := clk
    decode_stage.io.reset_n := reset_n
    decode_stage.io.if_id_reg_q := if_id_reg_q
    decode_stage.io.rs1_forward_mux_sel := rs1_forward_mux_sel_id
    decode_stage.io.rs2_forward_mux_sel := rs2_forward_mux_sel_id
    decode_stage.io.rd_addr_wb := rd_addr_wb
    decode_stage.io.rd_data_wb := wb_data_wb
    decode_stage.io.rd_we_wb := rd_we_wb
    // Outputs
    id_ex_reg_d := decode_stage.io.id_ex_reg_d
    illegal_instr_id := decode_stage.io.illegal_instr_id
    
    val execute_stage = new execute_stage_ex
    execute_stage.setName("execute_stage")
    // Inputs
    execute_stage.io.id_ex_reg_q := id_ex_reg_q
    execute_stage.io.ex_mem_reg_q := ex_mem_reg_q
    execute_stage.io.wb_data_wb := wb_forward_data_wb
    // Outputs
    ex_mem_reg_d := execute_stage.io.ex_mem_reg_d
    
    val memory_stage = new memory_stage_mem
    memory_stage.setName("memory_stage")
    // Inputs
    memory_stage.io.ex_mem_reg_q := ex_mem_reg_q
    // Outputs
    data_req_valid_mem := memory_stage.io.data_req_valid_mem
    data_req_write_mem := memory_stage.io.data_req_write_mem
    data_req_addr_mem := memory_stage.io.data_req_addr_mem
    data_req_wdata_mem := memory_stage.io.data_req_wdata_mem
    data_req_wstrb_mem := memory_stage.io.data_req_wstrb_mem
    mem_wb_reg_d := memory_stage.io.mem_wb_reg_d
    load_misalign_except_mem := memory_stage.io.load_misalign_except_mem
    store_misalign_except_mem := memory_stage.io.store_misalign_except_mem
    redirect_pc_request_mem := memory_stage.io.redirect_pc_request_mem
    redirect_next_pc_mem := memory_stage.io.redirect_next_pc_mem
    wb_forward_valid_mem := memory_stage.io.wb_forward_valid_mem
    
    val writeback_stage = new writeback_stage_wb
    writeback_stage.setName("writeback_stage")
    // Inputs
    writeback_stage.io.mem_wb_reg_q := mem_wb_reg_q
    writeback_stage.io.data_resp_rdata_wb := data_resp_rdata_wb
    // Outputs
    rd_addr_wb := writeback_stage.io.rd_addr_wb
    wb_data_wb := writeback_stage.io.wb_data_wb
    wb_forward_data_wb := writeback_stage.io.wb_forward_data_wb
    rd_we_wb := writeback_stage.io.rd_we_wb
    
    val hazard = new hazard
    hazard.setName("hazard")
    // Inputs
    hazard.io.rs1_id := id_ex_reg_d.rs1
    hazard.io.rs2_id := id_ex_reg_d.rs2
    hazard.io.rs1_ex := id_ex_reg_q.rs1
    hazard.io.rs2_ex := id_ex_reg_q.rs2
    hazard.io.rd_ex := id_ex_reg_q.rd
    hazard.io.rd_mem := ex_mem_reg_q.rd
    hazard.io.rd_wb := mem_wb_reg_q.rd
    hazard.io.rs1_mem := ex_mem_reg_q.rs1
    hazard.io.rs2_mem := ex_mem_reg_q.rs2
    hazard.io.reg_we_ex := id_ex_reg_q.reg_we
    hazard.io.reg_we_wb := mem_wb_reg_q.reg_we
    hazard.io.wb_forward_valid_mem := wb_forward_valid_mem
    hazard.io.valid_id := if_id_reg_q.valid
    hazard.io.valid_ex := id_ex_reg_q.valid
    hazard.io.valid_mem := ex_mem_reg_q.valid
    hazard.io.valid_wb := mem_wb_reg_q.valid
    hazard.io.mem_rden_ex := id_ex_reg_q.mem_re
    hazard.io.mem_rden_mem := ex_mem_reg_q.mem_re
    hazard.io.uses_rs1_id := id_ex_reg_d.uses_rs1
    hazard.io.uses_rs2_id := id_ex_reg_d.uses_rs2
    hazard.io.uses_rs1_ex := id_ex_reg_q.uses_rs1
    hazard.io.uses_rs2_ex := id_ex_reg_q.uses_rs2
    // Outputs
    rs1_forward_mux_sel_id := hazard.io.rs1_forward_mux_sel
    rs2_forward_mux_sel_id := hazard.io.rs2_forward_mux_sel
    load_use_stall_if := hazard.io.load_use_stall_if
    
    val pipeline_regs = new pipeline_registers
    pipeline_regs.setName("pipeline_regs")
    // Inputs
    pipeline_regs.io.clk := clk
    pipeline_regs.io.reset_n := reset_n
    pipeline_regs.io.redirect_request_mem := redirect_pc_request_mem
    pipeline_regs.io.load_use_stall_if := load_use_stall_if
    pipeline_regs.io.if_id_reg_d := if_id_reg_d
    pipeline_regs.io.id_ex_reg_d := id_ex_reg_d
    pipeline_regs.io.ex_mem_reg_d := ex_mem_reg_d
    pipeline_regs.io.mem_wb_reg_d := mem_wb_reg_d
    // Outputs
    if_id_reg_q := pipeline_regs.io.if_id_reg_q
    id_ex_reg_q := pipeline_regs.io.id_ex_reg_q
    ex_mem_reg_q := pipeline_regs.io.ex_mem_reg_q
    mem_wb_reg_q := pipeline_regs.io.mem_wb_reg_q
    
    val hw_perf_counter = new hw_perf_counter
    hw_perf_counter.setName("hw_perf_counter")
    // Inputs
    hw_perf_counter.io.clk := clk
    hw_perf_counter.io.reset_n := reset_n
    hw_perf_counter.io.branch := id_ex_reg_q.predicted_taken && id_ex_reg_q.valid
    hw_perf_counter.io.branch_miss := ex_mem_reg_q.redirect_request && ex_mem_reg_q.valid
    hw_perf_counter.io.retired_instr := mem_wb_reg_d.valid
    hw_perf_counter.io.cpu_stall := load_use_stall_if || redirect_pc_request_mem
    // Outputs
    cycle_count := hw_perf_counter.io.cycle_count
    branch_count := hw_perf_counter.io.branch_count
    branch_miss_count := hw_perf_counter.io.branch_miss_count
    retired_instr_count := hw_perf_counter.io.retired_instr_count
    cpu_stall_count := hw_perf_counter.io.cpu_stall_count
    
    exception_core := illegal_instr_id || load_misalign_except_mem || store_misalign_except_mem
    hw_perf_counter.addAttribute("DONT_TOUCH", "yes")
  }
}
