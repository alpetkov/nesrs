package testroms.vbl_nmi_timing;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import testroms.AbstractRomTest;

@RunWith(Parameterized.class)
public class VblNmiTimingTest extends AbstractRomTest {

   private final String NES_PATH = "/vbl_nmi_timing/";
   private final String PPM_PATH = "/testroms" + NES_PATH;
   
   @Parameters(name = "VblNmiTimingTest({0})")
   public static Collection<Object[]> data() {
       return Arrays.asList(
             new Object[][] {
//                   { "1.frame_basics.nes", "1.frame_basics.ppm", 10 },
//                   { "2.vbl_timing.nes", "2.vbl_timing.ppm", 6 },
//                   { "3.even_odd_frames.nes", "3.even_odd_frames.ppm", 5 },
//                   { "4.vbl_clear_timing.nes", "4.vbl_clear_timing.ppm", 5 },
                   { "5.nmi_suppression.nes", "5.nmi_suppression.ppm", 5 },
//                   { "6.nmi_disable.nes", "6.nmi_disable.ppm", 3 },
//                   { "7.nmi_timing.nes", "7.nmi_timing.ppm", 3 }
                   });
   }

   private String _romName;
   private String _ppmName;
   private int _seconds;
   
   public VblNmiTimingTest(String romName, String ppmName, int seconds) {
      _romName = romName;
      _ppmName = ppmName;
      _seconds = seconds;
   }

   @Test
   public void testVblNmiTiming() throws Exception {
      testRom(NES_PATH + _romName, PPM_PATH + _ppmName, _seconds);
   }
}
