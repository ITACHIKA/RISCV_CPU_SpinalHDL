package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\dmem_mem.sv.
class dmem_mem extends Component {
  setDefinitionName("dmem_mem")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val wren = in(Bool())
    val rden = in(Bool())
    val addr = in(UInt(32 bits))
    val wdata = in(UInt(32 bits))
    val wstrb = in(UInt(4 bits))
    // Outputs
    val rdata = out(UInt(32 bits))
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val data_ram = Mem(UInt(32 bits), 8192)
    data_ram.initBigInt(Seq.fill(8192)(BigInt(0)))
    data_ram.write(addr(14 downto 2), wdata, enable = wren, mask = wstrb.asBits)
    rdata := data_ram.readSync(addr(14 downto 2), rden, readUnderWrite = readFirst)
  }
}
