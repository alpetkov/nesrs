package testroms.sprite_hit_tests.sprite_hit_tests_2005_10_05;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import testroms.AbstractRomTest;

@RunWith(Parameterized.class)
public class SpriteHitTest extends AbstractRomTest {

   private final String NES_PATH = "/sprite_hit_tests/sprite_hit_tests_2005.10.05/";
   private final String PPM_PATH = "/testroms" + "/sprite_hit_tests/sprite_hit_tests_2005_10_05/";
   
   @Parameters(name = "SpriteHitTest({0})")
   public static Collection<Object[]> data() {
       return Arrays.asList(
             new Object[][] {
                   { "01.basics.nes", "01.basics.ppm", 4 },
                   { "02.alignment.nes", "02.alignment.ppm", 2 },
                   { "03.corners.nes", "03.corners.ppm", 2 },
                   { "04.flip.nes", "04.flip.ppm", 2 },
                   { "05.left_clip.nes", "05.left_clip.ppm", 2 },
                   { "06.right_edge.nes", "06.right_edge.ppm", 2 },
                   { "07.screen_bottom.nes", "07.screen_bottom.ppm", 2 },
                   { "08.double_height.nes", "08.double_height.ppm", 2 },
                   { "09.timing_basics.nes", "09.timing_basics.ppm", 4 },
                   { "10.timing_order.nes", "10.timing_order.ppm", 2 },
                   { "11.edge_timing.nes", "11.edge_timing.ppm", 2 },
                   });
   }

   private String _romName;
   private String _ppmName;
   private int _seconds;
   
   public SpriteHitTest(String romName, String ppmName, int seconds) {
      _romName = romName;
      _ppmName = ppmName;
      _seconds = seconds;
   }
   
   @Test
   public void testSpriteHit() throws Exception {
      testRom(NES_PATH + _romName, PPM_PATH + _ppmName, _seconds);
   }
}
