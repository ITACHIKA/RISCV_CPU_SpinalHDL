package riscv

import spinal.core._

/** Run with the included sbt build (SpinalHDL 1.12.3 / Scala 2.13.14).
  * Generated RTL defaults to the ignored generated/ output directory.
  * Arguments: cpu|soc|cmod|peripherals, output directory, external image path.
  * The optional --no-image argument deliberately omits IMEM initialization.
  * For FPGA builds pass the real bootloader image and include clk_wiz_0 IP.
  */
object Generate extends App {
  val target = args.headOption.getOrElse("cpu")
  val output = args.lift(1).getOrElse("generated")
  val config = SpinalConfig(targetDirectory = output)
  val image = args.lift(2) match {
    case Some("--no-image") => None
    case Some(path) => Some(path)
    case None => Some("bootloader.mem")
  }
  target match {
    case "cpu" => config.generateVerilog(new riscv_cpu)
    case "soc" => config.generateVerilog(new riscv_soc(image))
    case "cmod" => config.generateVerilog(new cmod_a7_top(image))
    case "peripherals" =>
      config.generateVerilog(new uart)
      config.generateVerilog(new timer)
      config.generateVerilog(new gpio)
      config.generateVerilog(new system_control)
      config.generateVerilog(new performance_counter)
      config.generateVerilog(new address_resolver_mem)
      config.generateVerilog(new imem_if(image))
      config.generateVerilog(new dmem_mem)
    case other => sys.error(s"Unknown target: $other")
  }
}
