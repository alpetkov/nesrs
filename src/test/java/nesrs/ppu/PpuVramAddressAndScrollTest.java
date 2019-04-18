package nesrs.ppu;

import junit.framework.TestCase;
import nesrs.ppu.Ppu;
import nesrs.ppu.PpuMemory;
import nesrs.ppu.PpuPin;
import nesrs.ppu.registers.VramAddressScrollRegister;

public class PpuVramAddressAndScrollTest extends TestCase {
	public void testScrollRegValues() {
		VramAddressScrollRegister scrollReg = new VramAddressScrollRegister();

		scrollReg._address = 0x7F1F;

		assertEquals(0x7, scrollReg.getBackgroundFineY());
		assertEquals(0x3, scrollReg.getNameTableIndex());
		assertEquals(0x18, scrollReg.getBackgroundTileY());
		assertEquals(0x1F, scrollReg.getBackgroundTileX());
	}

	public void testScrollRegWrite() {
	   Ppu ppu = new Ppu(new PpuMemory(null));

		ppu.init();

		// Total bits affected with $2005:
		// temp        %0yyy --YY   YYYX XXXX
		// fineX

		// temp      %0--- ---- ---X XXXX
		// Fine X    %xxx
		// toggle    1
		// t:0000000000011111=d:11111000
		// x=d:00000111

		// $2005     W %YYYY Yyyy (toggle is set)
		// temp      %0yyy --YY YYY- ----
		// toggle    0

		VramAddressScrollRegister vramAddressScrollReg = ppu._vramAddressScrollReg;

		vramAddressScrollReg._tempAddress = 0x0FF3;
		vramAddressScrollReg._bgFineX = 0x1;

		// 1st write
		ppu.writeRegister(PpuPin.REG_SCROLL, 0x76);
		assertEquals(0x6, vramAddressScrollReg.getBackgroundFineX());
		assertEquals(0x0FEE, vramAddressScrollReg._tempAddress);

		// 2nd write
		ppu.writeRegister(PpuPin.REG_SCROLL, 0x27);

		assertEquals(0x6, vramAddressScrollReg.getBackgroundFineX());
		assertEquals(0x7C8E, vramAddressScrollReg._tempAddress);
	}

	public void testVramAddressRegWrite() {
		Ppu ppu = new Ppu(new PpuMemory(null));

		ppu.init();

		// Total bits affected with $2006:
		// temp      %00yy NNYY YYYX XXXX
		// address   temp

		// $2006     W %--yy NNYY (toggle is cleared)
      // temp      %00yy NNYY ---- ----
		// t:0011111100000000=d:00111111
      // t:1100000000000000=0

		// $2006     W %YYYX XXXX (toggle is set)
      // temp      %0--- ----   YYYX XXXX
		// address   temp
		// t:0000000011111111=d:11111111
      // v=t

		VramAddressScrollRegister vramAddressScrollReg = ppu._vramAddressScrollReg;

		vramAddressScrollReg._tempAddress = 0x6FF3;
		vramAddressScrollReg._bgFineX = 0x1;

		// 1st write
		ppu.writeRegister(PpuPin.REG_VRAM_ADDR, 0x32);

		assertEquals(0x32F3, vramAddressScrollReg._tempAddress);
		assertEquals(0x0, vramAddressScrollReg._address);

		// 2nd write
		ppu.writeRegister(PpuPin.REG_VRAM_ADDR, 0x0C);

		assertEquals(0x0C, vramAddressScrollReg.getBackgroundTileX());
		assertEquals(0x0, vramAddressScrollReg.getNameTableIndex());
		assertEquals(0x3, vramAddressScrollReg.getBackgroundFineY());
		assertEquals(0x10, vramAddressScrollReg.getBackgroundTileY());

		assertEquals(0x1, vramAddressScrollReg.getBackgroundFineX());
		assertEquals(0x320C, vramAddressScrollReg._address);
	}
}
