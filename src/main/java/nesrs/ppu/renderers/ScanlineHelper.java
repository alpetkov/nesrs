package nesrs.ppu.renderers;

public class ScanlineHelper {
	public static final int SCANLINES_COUNT_IN_FRAME = 262;
	public static final int CYCLES_COUNT_IN_SCANLINE = 341;

	private static final int VBLANK_SCANLINES_IN_FRAME = 20;

	// Variant 1
   // 0-19 Vblank
   // 20 dummy scanline
   // 21-260 render scanlines
   // 261 waste scanline

   // Variant 2
   // 0-239 render scanline
   // 240 waste scanline
   // 241-260 Vblank
   // 261 dummy scanline

	// Platoon works only with vblank start set to 0 right now.
	public static int VBLANK_START_SCANLINE = 0; //241
	public static int VBLANK_END_SCANLINE =
	   VBLANK_START_SCANLINE + VBLANK_SCANLINES_IN_FRAME - 1;

	public static int DUMMY_RENDER_SCANLINE = VBLANK_END_SCANLINE + 1;
	public static int FIRST_RENDER_SCANLINE =
      (DUMMY_RENDER_SCANLINE + 1) % SCANLINES_COUNT_IN_FRAME;
   public static int LAST_RENDER_SCANLINE = FIRST_RENDER_SCANLINE + 239;

   public static int WASTE_SCANLINE = LAST_RENDER_SCANLINE + 1;
}
