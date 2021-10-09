package testroms.blargg_ppu_tests_2005_09_15b;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import testroms.AbstractRomTest;

@RunWith(Parameterized.class)
public class PpuTest extends AbstractRomTest {

   private final String NES_PATH = "/testroms" + "/blargg_ppu_tests_2005.09.15b/";
   private final String PPM_PATH = "/testroms" + "/blargg_ppu_tests_2005_09_15b/";

   @Parameters(name = "PpuTest({0})")
   public static Collection<Object[]> data() {
       return Arrays.asList(
             new Object[][] {
                   { "palette_ram.nes", "palette_ram.ppm", 2 },
                   { "power_up_palette.nes", "power_up_palette.ppm", 2 },
                   { "sprite_ram.nes", "sprite_ram.ppm", 2 },
                   { "vbl_clear_time.nes", "vbl_clear_time.ppm", 2 },
                   { "vram_access.nes", "vram_access.ppm", 2 },
                   });
   }

   private String _romName;
   private String _ppmName;
   private int _seconds;
   
   public PpuTest(String romName, String ppmName, int seconds) {
      _romName = romName;
      _ppmName = ppmName;
      _seconds = seconds;
   }

   @Test
   public void testPpu() throws Exception {
      testRom(NES_PATH + _romName, PPM_PATH + _ppmName, _seconds);
   }
}
