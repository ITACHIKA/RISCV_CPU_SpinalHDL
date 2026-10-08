package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\wb\writeback_stage_wb.sv.
class writeback_stage_wb extends Component {
  setDefinitionName("writeback_stage_wb")
  val io = new Bundle {
    // Inputs
    val mem_wb_reg_q = in(mem_wb_reg_t())
    val data_resp_rdata_wb = in(UInt(32 bits))
    // Outputs
    val rd_addr_wb = out(UInt(5 bits))
    val wb_data_wb = out(UInt(32 bits))
    val wb_forward_data_wb = out(UInt(32 bits))
    val rd_we_wb = out(Bool())
  }
  noIoPrefix()
  import io._
  val load_data_wb = UInt(32 bits)
  val lsu_wb = new lsu_wb
  lsu_wb.setName("lsu_wb")
  // Inputs
  lsu_wb.io.mem_raw_data := data_resp_rdata_wb
  lsu_wb.io.mem_addr := mem_wb_reg_q.alu_result
  lsu_wb.io.memsize := mem_wb_reg_q.memsize
  lsu_wb.io.memsign := mem_wb_reg_q.memsign
  lsu_wb.io.rden := mem_wb_reg_q.mmio_rden && mem_wb_reg_q.valid
  // Outputs
  load_data_wb := lsu_wb.io.load_data
  
  wb_data_wb := 0
  wb_forward_data_wb := 0
  switch(mem_wb_reg_q.wb_sel) {
    is(WbSel.ALU) { wb_data_wb := mem_wb_reg_q.alu_result; wb_forward_data_wb := mem_wb_reg_q.alu_result }
    is(WbSel.MEM) { wb_data_wb := load_data_wb }
    is(WbSel.PC) { wb_data_wb := mem_wb_reg_q.pcplus4; wb_forward_data_wb := mem_wb_reg_q.pcplus4 }
  }
  rd_addr_wb := mem_wb_reg_q.rd
  rd_we_wb := mem_wb_reg_q.reg_we && mem_wb_reg_q.valid
}
