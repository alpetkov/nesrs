package testroms.branch_timing_tests;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import testroms.AbstractRomTest;

@RunWith(Parameterized.class)
public class BranchTimingTest extends AbstractRomTest {

   private final String NES_PATH = "/testroms/branch_timing_tests/";
   private final String PPM_PATH = NES_PATH;

   @Parameters(name = "BranchTimingTest({0})")
   public static Collection<Object[]> data() {
       return Arrays.asList(
             new Object[][] {
                   { "1.Branch_Basics.nes", "1.Branch_Basics.ppm", 2 },
                   { "2.Backward_Branch.nes", "2.Backward_Branch.ppm", 2 },
                   { "3.Forward_Branch.nes", "3.Forward_Branch.ppm", 2 }
                   });
   }

   private String _romName;
   private String _ppmName;
   private int _seconds;
   
   public BranchTimingTest(String romName, String ppmName, int seconds) {
      _romName = romName;
      _ppmName = ppmName;
      _seconds = seconds;
   }

   @Test
   public void testBranchTiming() throws Exception {
      testRom(NES_PATH + _romName, PPM_PATH + _ppmName, _seconds);
   }
}
