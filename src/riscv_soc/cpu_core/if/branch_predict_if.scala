package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\if\branch_predict_if.sv.
class branch_predict_if extends Component {
  setDefinitionName("branch_predict_if")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val current_pc = in(UInt(32 bits))
    val btb_feedback_pc = in(UInt(32 bits))
    val btb_feedback_actual_target = in(UInt(32 bits))
    val btb_feedback_taken = in(Bool())
    val btb_feedback_valid = in(Bool())
    val btb_feedback_predict_type = in(BranchPredictType())
    // Outputs
    val branch_predict_result = out(pc_predict_result_t())
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val btb_table = Vec.fill(BTB_ENTRIES)(Reg(btb_entry_t()))
    val bht_table = Vec.fill(BHT_ENTRIES)(Reg(UInt(2 bits)) init(1))
    val query = btb_table(current_pc(4 downto 2))
    val update = btb_feedback_pc(4 downto 2)
    val history = btb_feedback_pc(6 downto 2)
    val hit = query.valid && query.tag === current_pc(14 downto 5)
    val taken = hit && (query.predict_type === BranchPredictType.JAL || (query.predict_type === BranchPredictType.CONDITIONAL && bht_table(current_pc(6 downto 2))(1)))
    branch_predict_result.predict_taken := taken
    branch_predict_result.predicted_pc := Mux(taken, query.target_pc, current_pc + 4)
    when(!reset_n) {
      btb_table.foreach(entry => entry.assignFromBits(B(0,entry.getBitsWidth bits)))
      bht_table.foreach(_ := 1)
    }.otherwise {
      when(btb_feedback_valid && (btb_feedback_predict_type === BranchPredictType.JAL || (btb_feedback_taken && btb_feedback_predict_type === BranchPredictType.CONDITIONAL))) {
        btb_table(update).valid := True
        btb_table(update).tag := btb_feedback_pc(14 downto 5)
        btb_table(update).target_pc := btb_feedback_actual_target
        btb_table(update).predict_type := btb_feedback_predict_type
      }
      when(btb_feedback_valid && btb_feedback_predict_type === BranchPredictType.CONDITIONAL) {
        when(btb_feedback_taken) {
          when(bht_table(history) =/= 3) { bht_table(history) := bht_table(history) + 1 }
        }.otherwise {
          when(bht_table(history) =/= 0) { bht_table(history) := bht_table(history) - 1 }
        }
      }
    }
  }
}
