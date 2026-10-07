package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\pipeline_registers.sv.
class pipeline_registers extends Component {
  setDefinitionName("pipeline_registers")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val redirect_request_mem = in(Bool())
    val load_use_stall_if = in(Bool())
    val if_id_reg_d = in(if_id_reg_t())
    val id_ex_reg_d = in(id_ex_reg_t())
    val ex_mem_reg_d = in(ex_mem_reg_t())
    val mem_wb_reg_d = in(mem_wb_reg_t())
    // Outputs
    val if_id_reg_q = out(if_id_reg_t())
    val id_ex_reg_q = out(id_ex_reg_t())
    val ex_mem_reg_q = out(ex_mem_reg_t())
    val mem_wb_reg_q = out(mem_wb_reg_t())
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val if_q = Reg(if_id_reg_t())
    val id_q = Reg(id_ex_reg_t())
    val ex_q = Reg(ex_mem_reg_t())
    val wb_q = Reg(mem_wb_reg_t())
    if_id_reg_q := if_q
    id_ex_reg_q := id_q
    ex_mem_reg_q := ex_q
    mem_wb_reg_q := wb_q
    when(!reset_n) {
      if_q.assignFromBits(B(0,if_q.getBitsWidth bits))
      id_q.assignFromBits(B(0,id_q.getBitsWidth bits))
      ex_q.assignFromBits(B(0,ex_q.getBitsWidth bits))
      wb_q.assignFromBits(B(0,wb_q.getBitsWidth bits))
    }.otherwise {
      if_q := if_id_reg_d
      id_q := id_ex_reg_d
      ex_q := ex_mem_reg_d
      wb_q := mem_wb_reg_d
      when(load_use_stall_if) { if_q := if_q; id_q.valid := False }
      when(redirect_request_mem) { if_q.valid := False; id_q.valid := False; ex_q.valid := False }
    }
  }
}
