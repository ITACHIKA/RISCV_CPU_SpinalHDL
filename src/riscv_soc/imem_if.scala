package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

// Native SpinalHDL implementation of riscv_soc\imem_if.sv.
class imem_if(bootImage: Option[String] = Some("bootloader.mem")) extends Component {
  setDefinitionName("imem_if")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val req_valid = in(Bool())
    val resp_ready = in(Bool())
    val flush = in(Bool())
    val porta_addr = in(UInt(32 bits))
    val portb_addr = in(UInt(32 bits))
    val portb_wdata = in(UInt(32 bits))
    val portb_wstrb = in(UInt(4 bits))
    val portb_rden = in(Bool())
    val portb_wren = in(Bool())
    // Outputs
    val porta_rdata = out(UInt(32 bits))
    val req_ready = out(Bool())
    val resp_valid = out(Bool())
    val portb_rdata = out(UInt(32 bits))
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val instr_rom = Mem(UInt(32 bits), 8192)
    instr_rom.addAttribute("ram_style", "block")
    bootImage.foreach(path => instr_rom.initBigInt(BootImage.read(path)))
    val response = Reg(Bool())
    resp_valid := response
    req_ready := !response || resp_ready || flush
    val rden = req_valid && req_ready
    // Native synchronous memory ports; reads hold their output when disabled.
    porta_rdata := instr_rom.readSync(porta_addr(14 downto 2), rden)
    portb_rdata := instr_rom.readSync(portb_addr(14 downto 2), portb_rden)
    instr_rom.write(portb_addr(14 downto 2), portb_wdata,
      enable = portb_wren && !portb_rden, mask = portb_wstrb.asBits)
    when(!reset_n) { response := False }
    .elsewhen(flush) { response := rden }
    .elsewhen(req_ready) { response := req_valid }
  }
}

/** Reads the original word-addressed $readmemh image without copying firmware.
  * Unspecified locations in a supplied sparse image are initialized to zero.
  * None leaves IMEM uninitialized, for an external simulation/programming flow.
  */
object BootImage {
  def read(path: String): Seq[BigInt] = {
    val source = scala.io.Source.fromFile(path)
    val text = try source.mkString finally source.close()
    val words = Array.fill[BigInt](8192)(BigInt(0))
    var address = 0
    val tokens = text.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("//[^\\n]*", " ").split("\\s+").filter(_.nonEmpty)
    for(token <- tokens) {
      if(token.startsWith("@")) address = Integer.parseInt(token.drop(1),16)
      else {
        require(address >= 0 && address < words.length, s"IMEM image address out of range: $address")
        val word = BigInt(token.replace("_", ""),16)
        require(word >= 0 && word.bitLength <= 32, s"Invalid IMEM word: $token")
        words(address) = word
        address += 1
      }
    }
    words.toSeq
  }
}
