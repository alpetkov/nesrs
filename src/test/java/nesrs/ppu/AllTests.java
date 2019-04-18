package nesrs.ppu;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ PpuMemoryMirroringTest.class, PpuVramAddressAndScrollTest.class })
public class AllTests {

}
