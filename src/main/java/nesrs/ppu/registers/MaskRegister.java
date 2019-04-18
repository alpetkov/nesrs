package nesrs.ppu.registers;

public class MaskRegister {
	// Mask Register (W) flags
	// This register controls screen enable, masking, and intensity.
	//
	// bit 7,6,5	Color Intensity (when D0 == 0)			000 = None;
	//					{Do not use more than one type}        100 = Intensify blue;
	//                                                    010 = Intensify green;
	//                                                    001 = Intensify red
	//
	//					Full Background Color (when D0 == 1)	000 = None;
	//					{Do not use more than one type}        100 = blue;
	//                                                    010 = green;
	//                                                    001 = red
	//
	// bit 4			Sprite Visibility						      0 = Sprites not displayed;
	//                                                    1 = Sprites visible
	//
	// bit 3			Background Visibility					   0 = Background not displayed;
	//                                                    1 = Background visible
	//
	// bit 2			Sprite Clipping							   0 = Sprites invisible in left 8-pixel column;
	//                                                    1 = No clipping
	//
	// bit 1			Background Clipping						   0 = BG invisible in left 8-pixel column;
	//                                                    1 = No clipping
	//
	// bit 0			Disable composite color burst.		   0 = Color display;
	//					Effectively causes display to go       1 = Monochrome display
	//					black & white

	public static final int COLOR_INTENSITY = 0xE0;       // bits 5,6 and 7
	public static final int SPRITE_VISIBILITY = 0x10;     // bit 4
	public static final int BACKGROUND_VISIBILITY = 0x08; // bit 3
	public static final int SPRITE_CLIPPING = 0x04;       // bit 2
	public static final int BACKGROUND_CLIPPING = 0x02;   // bit 1
	public static final int DISABLE_COLORBURST = 0x01;    // bit 0.

	public int value;

	public boolean isBackgroundVisibilityEnabled() {
		return (value & MaskRegister.BACKGROUND_VISIBILITY) != 0;
	}

	public boolean isSpriteVisibilityEnabled() {
		return (value & MaskRegister.SPRITE_VISIBILITY) != 0;
	}
}
