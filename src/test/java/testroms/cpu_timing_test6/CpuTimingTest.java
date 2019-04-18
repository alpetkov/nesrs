package testroms.cpu_timing_test6;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import testroms.AbstractRomTest;

@RunWith(Parameterized.class)
public class CpuTimingTest extends AbstractRomTest {

   private final String NES_PATH = "/cpu_timing_test6/";
   private final String PPM_PATH = "/testroms" + NES_PATH;

   @Parameters(name = "CpuTimingTest({0})")
   public static Collection<Object[]> data() {
       return Arrays.asList(
             new Object[][] {
                   { "cpu_timing_test.nes", "cpu_timing_test.ppm", 24 }
                   });
   }

   private String _romName;
   private String _ppmName;
   private int _seconds;
   
   public CpuTimingTest(String romName, String ppmName, int seconds) {
      _romName = romName;
      _ppmName = ppmName;
      _seconds = seconds;
   }

   @Test
   public void testCpuTiming() throws Exception {
      testRom(NES_PATH + _romName, PPM_PATH + _ppmName, _seconds);
   }
}
