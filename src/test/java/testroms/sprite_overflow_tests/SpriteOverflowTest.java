package testroms.sprite_overflow_tests;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import testroms.AbstractRomTest;

@RunWith(Parameterized.class)
public class SpriteOverflowTest extends AbstractRomTest {

   private final String NES_PATH = "/testroms/sprite_overflow_tests/";
   private final String PPM_PATH = NES_PATH;
   
   @Parameters(name = "SpriteOverflowTest({0})")
   public static Collection<Object[]> data() {
       return Arrays.asList(
             new Object[][] {
                   { "1.Basics.nes", "1.Basics.ppm", 2 },
                   { "2.Details.nes", "2.Details.ppm", 2 },
                   { "3.Timing.nes", "3.Timing.ppm", 2 },
                   { "4.Obscure.nes", "4.Obscure.ppm", 2 },
                   { "5.Emulator.nes", "5.Emulator.ppm", 2 }
                   });
   }

   private String _romName;
   private String _ppmName;
   private int _seconds;
   
   public SpriteOverflowTest(String romName, String ppmName, int seconds) {
      _romName = romName;
      _ppmName = ppmName;
      _seconds = seconds;
   }
   
   @Test
   public void testSpriteOverflow() throws Exception {
      testRom(NES_PATH + _romName, PPM_PATH + _ppmName, _seconds);
   }
}
