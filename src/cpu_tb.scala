package riscv

import spinal.core._
import spinal.core.sim._

/** Waveform testbench corresponding to the original cpu_tb.sv.
  * Arguments: external boot image, simulation output directory, cycle count.
  * Uses a 100 MHz clock, two reset cycles, and an idle UART input.
  * This is a waveform runner; differential regression results are recorded
  * in the migration memory next to this project directory.
  */
object cpu_tb extends App {
  val image = args.headOption.getOrElse("bootloader.mem")
  val workspace = args.lift(1).getOrElse("simWorkspace")
  val cycles = args.lift(2).map(_.toInt).getOrElse(50000)
  require(cycles > 0, "Simulation cycle count must be positive")
  SimConfig.withIVerilog.withWave.workspacePath(workspace)
    .compile(new riscv_soc(Some(image))).doSim { dut =>
      dut.io.clk #= false
      dut.io.reset_n #= false
      dut.io.gpio_btn_in #= false
      dut.io.uart_rx #= true
      fork {
        while(true) {
          sleep(5)
          dut.io.clk #= true
          sleep(5)
          dut.io.clk #= false
        }
      }
      sleep(20)
      dut.io.reset_n #= true
      sleep(cycles.toLong * 10)
    }
}
