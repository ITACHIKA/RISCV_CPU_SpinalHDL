package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\cpu_core\mem\lsu_mem.sv.
class lsu_mem extends Component {
  setDefinitionName("lsu_mem")
  val io = new Bundle {
    // Inputs
    val wren = in(Bool())
    val rden = in(Bool())
    val addr = in(UInt(32 bits))
    val store_data = in(UInt(32 bits))
    val memsize = in(UInt(3 bits))
    // Outputs
    val wstrb = out(UInt(4 bits))
    val mem_wdata = out(UInt(32 bits))
    val load_misalign_except = out(Bool())
    val store_misalign_except = out(Bool())
  }
  noIoPrefix()
  import io._
  val misaligned = (memsize === MEM_HALF && addr(0)) || (memsize === MEM_WORD && addr(1 downto 0) =/= 0)
  load_misalign_except := rden && misaligned
  store_misalign_except := wren && misaligned
  wstrb := 0
  mem_wdata := 0
  when(wren) {
    switch(memsize) {
      is(MEM_BYTE) {
        wstrb := (U(1,4 bits) |<< addr(1 downto 0)).resized
        mem_wdata := (store_data(7 downto 0).resize(32) |<< (addr(1 downto 0) @@ U(0,3 bits))).resized
      }
      is(MEM_HALF) {
        wstrb := Mux(addr(1), U(12,4 bits), U(3,4 bits))
        mem_wdata := Mux(addr(1), (store_data(15 downto 0) @@ U(0,16 bits)), store_data(15 downto 0).resize(32))
      }
      is(MEM_WORD) { wstrb := 15; mem_wdata := store_data }
    }
  }
}
