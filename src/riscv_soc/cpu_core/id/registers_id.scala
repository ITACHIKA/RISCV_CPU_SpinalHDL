package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\id\registers_id.sv.
class registers_id_wb extends Component {
  setDefinitionName("registers_id_wb")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val rs1_addr = in(UInt(5 bits))
    val rs2_addr = in(UInt(5 bits))
    val rd_addr = in(UInt(5 bits))
    val rd_data = in(UInt(32 bits))
    val rd_we = in(Bool())
    // Outputs
    val rs1_data = out(UInt(32 bits))
    val rs2_data = out(UInt(32 bits))
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val registers = Vec.fill(32)(Reg(UInt(32 bits)) init(0))
    when(!reset_n) { registers.foreach(_ := 0) }
    .elsewhen(rd_we && rd_addr =/= 0) { registers(rd_addr) := rd_data }
    def read(addr: UInt): UInt = Mux(addr === 0, U(0,32 bits), Mux(rd_we && rd_addr =/= 0 && addr === rd_addr, rd_data, registers(addr)))
    rs1_data := read(rs1_addr)
    rs2_data := read(rs2_addr)
  }
}
