package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\hazard.sv.
class hazard extends Component {
  setDefinitionName("hazard")
  val io = new Bundle {
    // Inputs
    val rs1_id = in(UInt(5 bits))
    val rs2_id = in(UInt(5 bits))
    val rs1_ex = in(UInt(5 bits))
    val rs2_ex = in(UInt(5 bits))
    val rs1_mem = in(UInt(5 bits))
    val rs2_mem = in(UInt(5 bits))
    val rd_ex = in(UInt(5 bits))
    val rd_mem = in(UInt(5 bits))
    val rd_wb = in(UInt(5 bits))
    val reg_we_ex = in(Bool())
    val reg_we_wb = in(Bool())
    val wb_forward_valid_mem = in(Bool())
    val uses_rs1_id = in(Bool())
    val uses_rs2_id = in(Bool())
    val uses_rs1_ex = in(Bool())
    val uses_rs2_ex = in(Bool())
    val mem_rden_ex = in(Bool())
    val mem_rden_mem = in(Bool())
    val valid_id = in(Bool())
    val valid_ex = in(Bool())
    val valid_mem = in(Bool())
    val valid_wb = in(Bool())
    // Outputs
    val rs1_forward_mux_sel = out(UInt(2 bits))
    val rs2_forward_mux_sel = out(UInt(2 bits))
    val load_use_stall_if = out(Bool())
  }
  noIoPrefix()
  import io._
  val mem_forward_valid = valid_ex && reg_we_ex && !mem_rden_ex && rd_ex =/= 0
  def select(rs: UInt): UInt = Mux(mem_forward_valid && rs === rd_ex, RS_FORWARD_MEM,
    Mux(wb_forward_valid_mem && rd_mem =/= 0 && rs === rd_mem, RS_FORWARD_WB, RS_FORWARD_NONE))
  rs1_forward_mux_sel := select(rs1_id)
  rs2_forward_mux_sel := select(rs2_id)
  def dependency(rd: UInt): Bool = rd =/= 0 && ((uses_rs1_id && rs1_id === rd) || (uses_rs2_id && rs2_id === rd))
  // Two cycles: wait for loads in both EX and MEM. WB bypass supplies ID.
  load_use_stall_if := valid_id && ((valid_ex && mem_rden_ex && dependency(rd_ex)) || (valid_mem && mem_rden_mem && dependency(rd_mem)))
}
