package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\wb\lsu_wb.sv.
class lsu_wb extends Component {
  setDefinitionName("lsu_wb")
  val io = new Bundle {
    // Inputs
    val mem_raw_data = in(UInt(32 bits))
    val mem_addr = in(UInt(32 bits))
    val memsize = in(MemSize())
    val memsign = in(MemSign())
    val rden = in(Bool())
    // Outputs
    val load_data = out(UInt(32 bits))
  }
  noIoPrefix()
  import io._
  val byte = (mem_raw_data |>> (mem_addr(1 downto 0) @@ U(0,3 bits)))(7 downto 0)
  val half = Mux(mem_addr(1), mem_raw_data(31 downto 16), mem_raw_data(15 downto 0))
  load_data := 0
  when(rden) {
    switch(memsize) {
      is(MemSize.BYTE) { load_data := Mux(memsign === MemSign.SIGNED, byte.asSInt.resize(32).asUInt, byte.resize(32)) }
      is(MemSize.HALF) { load_data := Mux(memsign === MemSign.SIGNED, half.asSInt.resize(32).asUInt, half.resize(32)) }
      is(MemSize.WORD) { load_data := mem_raw_data }
    }
  }
}
