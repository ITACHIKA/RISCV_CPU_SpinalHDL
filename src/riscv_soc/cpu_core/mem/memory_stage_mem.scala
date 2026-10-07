package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\mem\memory_stage_mem.sv.
class memory_stage_mem extends Component {
  setDefinitionName("memory_stage_mem")
  val io = new Bundle {
    // Inputs
    val ex_mem_reg_q = in(ex_mem_reg_t())
    // Outputs
    val data_req_valid_mem = out(Bool())
    val data_req_write_mem = out(Bool())
    val data_req_addr_mem = out(UInt(32 bits))
    val data_req_wdata_mem = out(UInt(32 bits))
    val data_req_wstrb_mem = out(UInt(4 bits))
    val mem_wb_reg_d = out(mem_wb_reg_t())
    val load_misalign_except_mem = out(Bool())
    val store_misalign_except_mem = out(Bool())
    val redirect_pc_request_mem = out(Bool())
    val redirect_next_pc_mem = out(UInt(32 bits))
    val wb_forward_valid_mem = out(Bool())
  }
  noIoPrefix()
  import io._
  val mem_read_mem = Bool()
  val mem_write_mem = Bool()
  val lsu_mem = new lsu_mem
  lsu_mem.setName("lsu_mem")
  // Inputs
  lsu_mem.io.wren := mem_write_mem
  lsu_mem.io.rden := mem_read_mem
  lsu_mem.io.addr := ex_mem_reg_q.alu_result
  lsu_mem.io.store_data := ex_mem_reg_q.rs2_data
  lsu_mem.io.memsize := ex_mem_reg_q.memsize
  // Outputs
  data_req_wstrb_mem := lsu_mem.io.wstrb
  data_req_wdata_mem := lsu_mem.io.mem_wdata
  load_misalign_except_mem := lsu_mem.io.load_misalign_except
  store_misalign_except_mem := lsu_mem.io.store_misalign_except
  
  wb_forward_valid_mem := ex_mem_reg_q.valid && ex_mem_reg_q.reg_we && (ex_mem_reg_q.wb_sel =/= WB_MEM)
  mem_read_mem := ex_mem_reg_q.mem_re && ex_mem_reg_q.valid
  mem_write_mem := ex_mem_reg_q.mem_we && ex_mem_reg_q.valid
  data_req_valid_mem := mem_read_mem || mem_write_mem
  data_req_write_mem := mem_write_mem
  data_req_addr_mem := ex_mem_reg_q.alu_result
  redirect_pc_request_mem := ex_mem_reg_q.redirect_request && ex_mem_reg_q.valid
  redirect_next_pc_mem := ex_mem_reg_q.redirect_request_pc
  mem_wb_reg_d.pcplus4 := ex_mem_reg_q.pcplus4
  mem_wb_reg_d.alu_result := ex_mem_reg_q.alu_result
  mem_wb_reg_d.rs2_data := ex_mem_reg_q.rs2_data
  mem_wb_reg_d.rd := ex_mem_reg_q.rd
  mem_wb_reg_d.memsize := ex_mem_reg_q.memsize
  mem_wb_reg_d.memsign := ex_mem_reg_q.memsign
  mem_wb_reg_d.mmio_rden := ex_mem_reg_q.mem_re
  mem_wb_reg_d.reg_we := ex_mem_reg_q.reg_we
  mem_wb_reg_d.wb_sel := ex_mem_reg_q.wb_sel
  mem_wb_reg_d.valid := ex_mem_reg_q.valid
}
