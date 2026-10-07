package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\ex\control_flow_resolver_ex.sv.
class control_flow_resolver_ex extends Component {
  setDefinitionName("control_flow_resolver_ex")
  val io = new Bundle {
    // Inputs
    val current_pc = in(UInt(32 bits))
    val predicted_pc = in(UInt(32 bits))
    val predicted_taken = in(Bool())
    val branch_taken = in(Bool())
    val alu_result = in(UInt(32 bits))
    val imm = in(UInt(32 bits))
    val pc_sel = in(UInt(3 bits))
    val valid_ex = in(Bool())
    // Outputs
    val redirect_pc_request = out(Bool())
    val redirect_next_pc = out(UInt(32 bits))
    val btb_target_pc = out(UInt(32 bits))
    val btb_update_valid = out(Bool())
    val btb_update_type = out(UInt(2 bits))
    val btb_actual_taken = out(Bool())
  }
  noIoPrefix()
  import io._
  redirect_next_pc := current_pc + 4
  redirect_pc_request := False
  btb_update_valid := False
  btb_update_type := BP_NONE
  btb_target_pc := 0
  btb_actual_taken := False
  // Preserve direction-only recovery and unconditional JALR recovery.
  switch(pc_sel) {
    is(PC_BRANCH) {
      redirect_next_pc := Mux(branch_taken, current_pc + imm, current_pc + 4)
      redirect_pc_request := valid_ex && (branch_taken =/= predicted_taken)
      btb_update_valid := True
      btb_update_type := BP_CONDITIONAL
      btb_target_pc := current_pc + imm
      btb_actual_taken := branch_taken
    }
    is(PC_JAL) {
      redirect_next_pc := alu_result
      redirect_pc_request := valid_ex && !predicted_taken
      btb_update_valid := True
      btb_update_type := BP_JAL
      btb_target_pc := alu_result
      btb_actual_taken := True
    }
    is(PC_JALR) { redirect_next_pc := alu_result & U(0xfffffffeL,32 bits); redirect_pc_request := valid_ex }
    is(PC_TRAP) { redirect_next_pc := 0; redirect_pc_request := valid_ex && !predicted_taken }
  }
}
