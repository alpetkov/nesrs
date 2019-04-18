package nesrs.ppu.registers;

public class StatusRegister {
	// Status Register (R) flags
	//
	// bit 7	VBlank Occurrence		   0 = Not In VBlank; 1 = In VBlank
	// bit 6	Sprite #0 Occurrence	   0 = Sprite #0 not found; 1 = PPU has hit Sprite #0
	//									      Set when a nonzero pixel of sprite 0 'hits'
	//                               a nonzero background pixel.  Used for raster timing.
	//
	// bit 5	Scanline Sprite Count	0 = Eight (8) sprites or less on current scanline,
	//                               1 = More than 8 sprites on current scanline
	// bit 4	VRAM Write Flag			0 = Writes to VRAM are respected;
	//                               1 = Writes to VRAM are ignored
	public static final int VBLANK_OCCURRENCE = 0x80;      // bit 7
	public static final int SPRITE_ZERO_OCCURRENCE = 0x40; // bit 6
	public static final int SCANLINE_SPRITE_COUNT = 0x20;  // bit 5
	public static final int VRAM_WRITE_FLAG = 0x10;        // bit 4

	public int value;

	public boolean isInVblank() {
		return (value & VBLANK_OCCURRENCE) != 0;
	}

	public void setInVblank(boolean hasOccurred) {
		if (hasOccurred) {
			value |= VBLANK_OCCURRENCE;
		} else {
			value &= ~VBLANK_OCCURRENCE;
		}
	}
}
