package riscv

import spinal.core._
import spinal.lib._
import riscv.RiscvPkg._

object UartRxState extends SpinalEnum(binarySequential) {
  val IDLE, START_BIT, DATA_BITS, STOP_BIT = newElement()
}

// Native SpinalHDL implementation of riscv_soc\uart.sv.
class uart extends Component {
  setDefinitionName("uart")
  val io = new Bundle {
    // Inputs
    val clk = in(Bool())
    val reset_n = in(Bool())
    val uart_addr = in(UInt(32 bits))
    val uart_wren = in(Bool())
    val uart_rden = in(Bool())
    val uart_wdata = in(UInt(32 bits))
    val uart_wstrb = in(UInt(4 bits))
    val uart_rx = in(Bool())
    // Outputs
    val uart_rdata = out(UInt(32 bits))
    val uart_tx = out(Bool())
  }
  noIoPrefix()
  import io._
  val logic = new ClockingArea(ClockDomain(clk, config = ClockDomainConfig(resetKind = BOOT))) {
    val uart_control_reg = Reg(UInt(32 bits))
    val uart_baud_reg = Reg(UInt(32 bits))
    val uart_baud_counter = Reg(UInt(32 bits))
    val uart_baud_tick = Reg(Bool())
    val uart_tx_busy = Reg(Bool())
    val tx_output = Reg(Bool())
    val read_data = Reg(UInt(32 bits))
    uart_tx := tx_output
    uart_rdata := read_data
    val uart_tx_fifo = Vec.fill(16)(Reg(UInt(8 bits)))
    val uart_rx_fifo = Vec.fill(16)(Reg(UInt(8 bits)))
    val uart_tx_fifo_wp = Reg(UInt(5 bits))
    val uart_tx_fifo_rp = Reg(UInt(5 bits))
    val uart_rx_fifo_wp = Reg(UInt(5 bits))
    val uart_rx_fifo_rp = Reg(UInt(5 bits))
    val uart_tx_fifo_count = Reg(UInt(5 bits))
    val uart_rx_fifo_count = Reg(UInt(5 bits))
    val uart_rx_byte_valid = Reg(Bool())
    val uart_rx_byte = Reg(UInt(8 bits))
    val tx_full = uart_tx_fifo_wp(3 downto 0) === uart_tx_fifo_rp(3 downto 0) && uart_tx_fifo_wp(4) =/= uart_tx_fifo_rp(4)
    val tx_empty = uart_tx_fifo_wp === uart_tx_fifo_rp
    val rx_full = uart_rx_fifo_wp(3 downto 0) === uart_rx_fifo_rp(3 downto 0) && uart_rx_fifo_wp(4) =/= uart_rx_fifo_rp(4)
    val rx_empty = uart_rx_fifo_wp === uart_rx_fifo_rp
    val offset = uart_addr(11 downto 0)
    val tx_push = !tx_full && uart_wren && offset === 20 && uart_wstrb(0)
    val tx_pop = !tx_empty && !uart_tx_busy && uart_baud_reg =/= 0 && uart_control_reg(0) && uart_control_reg(1)
    val rx_push = !rx_full && uart_rx_byte_valid
    val rx_pop = !rx_empty && uart_rden && offset === 24
    val soft_reset = uart_wren && offset === 4 && uart_wstrb(0) && uart_wdata(3)
    val reset = !reset_n || soft_reset
    val write_mask = UInt(32 bits)
    for(lane <- 0 until 4) write_mask(8*lane+7 downto 8*lane) := Mux(uart_wstrb(lane), U(255,8 bits), U(0,8 bits))
    val status = UInt(32 bits)
    status := 0
    status(0) := tx_full
    status(1) := tx_empty
    status(2) := rx_full
    status(3) := rx_empty
    status(4) := tx_empty && !uart_tx_busy
    
    when(reset) { uart_tx_fifo_count := 0; uart_rx_fifo_count := 0 }.otherwise {
      when(tx_push && !tx_pop) { uart_tx_fifo_count := uart_tx_fifo_count + 1 }
      .elsewhen(!tx_push && tx_pop) { uart_tx_fifo_count := uart_tx_fifo_count - 1 }
      when(rx_push && !rx_pop) { uart_rx_fifo_count := uart_rx_fifo_count + 1 }
      .elsewhen(!rx_push && rx_pop) { uart_rx_fifo_count := uart_rx_fifo_count - 1 }
    }
    when(reset) {
      uart_control_reg := 0; uart_baud_reg := 0; uart_tx_fifo_wp := 0; uart_rx_fifo_rp := 0
    }.otherwise {
      read_data := 0
      when(uart_wren) {
        switch(offset) {
          is(4) { uart_control_reg := uart_control_reg | (uart_wdata & write_mask) }
          is(8) { uart_control_reg := uart_control_reg & ~(uart_wdata & write_mask) }
          is(12) { uart_baud_reg := uart_wdata & write_mask }
          is(20) {
            when(tx_push) {
              uart_tx_fifo(uart_tx_fifo_wp(3 downto 0)) := uart_wdata(7 downto 0) & write_mask(7 downto 0)
              uart_tx_fifo_wp := uart_tx_fifo_wp + 1
            }
          }
        }
      }.elsewhen(uart_rden) {
        switch(offset) {
          is(0) { read_data := uart_control_reg }
          is(12) { read_data := uart_baud_reg }
          is(16) { read_data := status }
          is(24) {
            when(rx_pop) {
              read_data := uart_rx_fifo(uart_rx_fifo_rp(3 downto 0)).resize(32)
              uart_rx_fifo_rp := uart_rx_fifo_rp + 1
            }
          }
        }
      }
    }
    when(reset) { uart_baud_counter := 0; uart_baud_tick := False }.otherwise {
      uart_baud_tick := False
      uart_baud_counter := 0
      when(uart_control_reg(0) && uart_baud_reg =/= 0) {
        when(uart_baud_counter === uart_baud_reg - 1) { uart_baud_tick := True }
        .otherwise { uart_baud_counter := uart_baud_counter + 1 }
      }
    }
    val uart_tx_shift_reg = Reg(UInt(10 bits))
    val uart_tx_bit_count = Reg(UInt(4 bits))
    when(reset) {
      uart_tx_fifo_rp := 0; uart_tx_shift_reg := 1023; uart_tx_bit_count := 0; tx_output := True; uart_tx_busy := False
    }.otherwise {
      when(!uart_control_reg(0) || !uart_control_reg(1)) {
        tx_output := True; uart_tx_busy := False; uart_tx_bit_count := 0; uart_tx_shift_reg := 1023
      }.otherwise {
        when(tx_pop) {
          uart_tx_shift_reg := (True ## uart_tx_fifo(uart_tx_fifo_rp(3 downto 0)) ## False).asUInt
          uart_tx_fifo_rp := uart_tx_fifo_rp + 1
          uart_tx_bit_count := 0
          uart_tx_busy := True
        }.elsewhen(uart_baud_tick && uart_tx_busy) {
          tx_output := uart_tx_shift_reg(0)
          uart_tx_shift_reg := (True ## uart_tx_shift_reg(9 downto 1)).asUInt
          uart_tx_bit_count := uart_tx_bit_count + 1
          when(uart_tx_bit_count === 9) {
            uart_tx_busy := False; tx_output := True; uart_tx_bit_count := 0; uart_tx_shift_reg := 1023
          }
        }
      }
    }
    val uart_rx_sync1 = Reg(Bool())
    val uart_rx_stable = Reg(Bool())
    when(reset) { uart_rx_sync1 := True; uart_rx_stable := True }
    .otherwise { uart_rx_sync1 := uart_rx; uart_rx_stable := uart_rx_sync1 }
    // Preserve the original 16-bit RX divider and 32-bit comparison arithmetic.
    val uart_rx_states = Reg(UartRxState())
    val uart_rx_baud_count = Reg(UInt(16 bits))
    val uart_rx_bit_count = Reg(UInt(3 bits))
    when(reset) {
      uart_rx_states := UartRxState.IDLE; uart_rx_baud_count := 0; uart_rx_bit_count := 0; uart_rx_byte_valid := False; uart_rx_byte := 0
    }.otherwise {
      uart_rx_byte_valid := False
      when(!uart_control_reg(0) || !uart_control_reg(2) || uart_baud_reg === 0) {
        uart_rx_states := UartRxState.IDLE; uart_rx_baud_count := 0; uart_rx_bit_count := 0; uart_rx_byte := 0
      }.otherwise {
        switch(uart_rx_states) {
          is(UartRxState.IDLE) { when(!uart_rx_stable) { uart_rx_states := UartRxState.START_BIT } }
          is(UartRxState.START_BIT) {
            when(!uart_rx_stable) {
              when(uart_rx_baud_count.resize(32) === (uart_baud_reg |>> 1) - 1) { uart_rx_states := UartRxState.DATA_BITS; uart_rx_baud_count := 0 }
              .otherwise { uart_rx_baud_count := uart_rx_baud_count + 1 }
            }.otherwise { uart_rx_states := UartRxState.IDLE; uart_rx_baud_count := 0 }
          }
          is(UartRxState.DATA_BITS) {
            uart_rx_baud_count := uart_rx_baud_count + 1
            when(uart_rx_baud_count.resize(32) === uart_baud_reg - 1) {
              uart_rx_baud_count := 0
              uart_rx_byte := (uart_rx_stable ## uart_rx_byte(7 downto 1)).asUInt
              uart_rx_bit_count := uart_rx_bit_count + 1
              when(uart_rx_bit_count === 7) { uart_rx_states := UartRxState.STOP_BIT; uart_rx_bit_count := 0 }
            }
          }
          is(UartRxState.STOP_BIT) {
            uart_rx_baud_count := uart_rx_baud_count + 1
            when(uart_rx_baud_count.resize(32) === uart_baud_reg - 1) {
              uart_rx_baud_count := 0
              uart_rx_states := UartRxState.IDLE
              when(uart_rx_stable) { uart_rx_byte_valid := True }
            }
          }
        }
      }
    }
    when(reset) { uart_rx_fifo_wp := 0 }.elsewhen(rx_push) {
      uart_rx_fifo(uart_rx_fifo_wp(3 downto 0)) := uart_rx_byte
      uart_rx_fifo_wp := uart_rx_fifo_wp + 1
    }
  }
}
