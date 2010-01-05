/*
 * LED Sign Control Code for Arduino Decimila
 *
 * Written by: Dan Fraser and Andrew Kilpatrick
 *
 * For more details visit: http://www.andrewkilpatrick.org
 *
 * Hardware Connections:
 *
 * mode: decimila pin 12
 * strobe: decimila pin 11
 * clock: decimila pin 10
 * data: decimila pin 9
 *
 * Limitations:
 *
 * - only controls weird surplus LED sign panels (could be adapted
 *   for other types of displays if you can reverse engineer them!)
 * - due to speed/RAM limitations, only supports 1bpp
 * - 57.6kbps serial speed is used for reliability and - this allows
 *   approx. 15fps max
 * 
 * Protocol:
 *
 * - messages are byte stuffed/unstuffed:
 *   - SOH (0x01) = start of a frame
 *   - EOT (0x04) = end of a frame
 *   - ESC (0x1b) = escape character
 *   - escape chars:
 *     - ESC_SOH (0x78)
 *     - ESC_EOT (0x79)
 *     - ESC_ESC (0x7a)
 * - message format:
 *   - byte 0 - SOH
 *   - 1...n  - pixel data (byte stuffed)
 *   - byte n + 1 -  EOT
 * - pixel alignment:
 *   - pixels are sent 8 bits to a byte - MSB is on the left side
 *   - pixels are loaded 1 panel at a time - left to right
 *   - pixels are loaded left to right each row, rows from top to bottom
 *
 */
#define NUM_BOARDS 6  // the number of panels connected (they daisy-chain)

// I/O definitions for high-speed pin control
#define MODE_A PORTB &= ~0x10
#define MODE_B PORTB |= 0x10
#define STROBE_HIGH PORTB |= 0x08
#define STROBE_LOW PORTB &= ~0x08
#define CLOCK_HIGH PORTB |= 0x04
#define CLOCK_LOW PORTB &= ~0x04
#define DATA_HIGH PORTB |= 0x02
#define DATA_LOW PORTB &= ~0x02

// protocol definitions
#define SOH 0x01
#define EOT 0x04
#define ESC 0x1b
#define ESC_SOH 0x78
#define ESC_EOT 0x79
#define ESC_ESC 0x7a

// framebuffer
char framebuffer[NUM_BOARDS][64];

// receiver stuff
#define STATE_IDLE 1
#define STATE_DATA 2
#define STATE_EOT 3
char rxState = STATE_IDLE;
char escFlag = 0;
char rxBufPos = 0;
char rxBoard = 0;

// set up stuff
void setup() 
{
  // set up the serial port - going faster causes data loss because of
  // crappy Arduino serial handler - this should be reimplemented
  Serial.begin(57600);
  
  // init I/Os
  DDRB = DDRB | B00111111;
  PORTB=0xFF;
  
  // clear framebuffer
  for (char board = 0; board < NUM_BOARDS; board++) {
    for (char i = 0; i < 64; i++) {
      framebuffer[board][i] = 0;
    }
  }
}

// run over and over again
void loop()
{
  // scan all rows
  char rowAddr = 1;
  char bufferPos = 0;
  for (char row = 0; row < 8; row++) {
    STROBE_HIGH;  // 2009-11-25 - this should be here - thanks Jed
    for (char bank = 3; bank >= 0; bank--) {
      // set a pixel bank
      MODE_B;
      clock_byte_lsb_first(bank);
      // load 16 pixels per board
      MODE_A;
      // STROBE_HIGH;
      for (char board = NUM_BOARDS; board > 0; board--) {
        clock_byte_msb_first(framebuffer[board-1][bufferPos+1]);
        clock_byte_msb_first(framebuffer[board-1][bufferPos]);
      }
      serialRx();
      bufferPos += 2;
    }
    // select row
    MODE_B;
    clock_byte_lsb_first(rowAddr);
    STROBE_LOW;
    MODE_A;
    for(char i = 0; i < 8; i++) {
      serialRx();
    }
    rowAddr = rowAddr << 1;
  }
}

void serialRx() {
  if (Serial.available()) {
    char rxData = Serial.read();
    // start of a frame
    if (rxData == SOH) {
      rxState = STATE_DATA;
      rxBufPos = 0;
      rxBoard = 0;
      return;
    }

    // escape character
    if (rxData == ESC) {
      escFlag = 1;
      return;
    }

    // handle escaped byte
    if (escFlag == 1) {
      if (rxData == ESC_SOH) {
        rxData = SOH;
      }
      else if (rxData == ESC_EOT) {
        rxData = EOT;
      }
      else if (rxData == ESC_ESC) {
        rxData = ESC;
      }
      else {
        rxState = STATE_IDLE;
      }
      escFlag = 0;
    }

    // handle pixel datum
    if (rxState == STATE_DATA) {
      framebuffer[rxBoard][rxBufPos] = rxData;
      rxBufPos++;
      if(rxBufPos == 64) {
        rxBufPos = 0;
        rxBoard++;
       }
       if(rxBoard == NUM_BOARDS) {
         rxState = STATE_IDLE;
       }
     }
   }
}

// convenience routine for clocking a byte LSB first
void clock_byte_lsb_first(char val) {
  for (char i = 0; i < 8; i++) {
    CLOCK_LOW;
    if (val & 0x01) {
      DATA_HIGH;
    } else {
      DATA_LOW;
    }
    CLOCK_HIGH;
    val = val >> 1;
  }
}

// convenience routine for clocking a byte MSB first
void clock_byte_msb_first(char val) {
  for (char i = 0; i < 8; i++) {
    CLOCK_LOW;
    if (val & 0x80) {
      DATA_HIGH;
    } else {
      DATA_LOW;
    }
    CLOCK_HIGH;
    val = val << 1;
  }
}
