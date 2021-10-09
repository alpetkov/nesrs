package testroms.apu_test.rom_singles;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import testroms.AbstractRomTest;

@RunWith(Parameterized.class)
public class ApuTest extends AbstractRomTest {

   private final String NES_PATH = "/testroms/apu_test/rom_singles/";
   private final String PPM_PATH = NES_PATH;
   
   @Parameters(name = "ApuTest({0})")
   public static Collection<Object[]> data() {
       return Arrays.asList(
             new Object[][] {
                   { "1-len_ctr.nes", "1-len_ctr.ppm", 2 },
                   { "2-len_table.nes", "2-len_table.ppm", 2 },
                   { "3-irq_flag.nes", "3-irq_flag.ppm", 2 },
                   { "4-jitter.nes", "4-jitter.ppm", 4 },
                   { "5-len_timing.nes", "5-len_timing.ppm", 4 },
                   { "6-irq_flag_timing.nes", "6-irq_flag_timing.ppm", 4 },
                   { "7-dmc_basics.nes", "7-dmc_basics.ppm", 2 },
                   { "8-dmc_rates.nes", "8-dmc_rates.ppm", 2 },
                   });
   }

   private String _romName;
   private String _ppmName;
   private int _seconds;

   public ApuTest(String romName, String ppmName, int seconds) {
      _romName = romName;
      _ppmName = ppmName;
      _seconds = seconds;
   }
   
   @Test
   public void testApu() throws Exception {
      testRom(NES_PATH + _romName,  PPM_PATH + _ppmName, _seconds);
   }
}
