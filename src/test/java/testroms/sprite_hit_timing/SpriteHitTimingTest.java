package testroms.sprite_hit_timing;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import testroms.AbstractRomTest;

@RunWith(Parameterized.class)
public class SpriteHitTimingTest extends AbstractRomTest {

   private final String NES_PATH = "/testroms/sprite_hit_timing/";
   private final String PPM_PATH = NES_PATH;

   @Parameters(name = "SpriteHitTimingTest({0})")
   public static Collection<Object[]> data() {
       return Arrays.asList(
             new Object[][] {
                   { "sprite_hit_timing.nes", "sprite_hit_timing.ppm", 4 }
                   });
   }

   private String _romName;
   private String _ppmName;
   private int _seconds;
   
   public SpriteHitTimingTest(String romName, String ppmName, int seconds) {
      _romName = romName;
      _ppmName = ppmName;
      _seconds = seconds;
   }

   @Test
   public void testSpriteHitTiming() throws Exception {
      testRom(NES_PATH + _romName, PPM_PATH + _ppmName, _seconds);
   }
}
