package nesrs.cpu;

import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpJSRTest extends TestCase {
	private CpuMemory memory;
	private Cpu cpu;

	@Before
	public void setUp() throws Exception {
		memory = new TestCPUMemory();
		cpu = new Cpu(memory, false);
		cpu.init();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testJSR_ABS() throws Exception {
		memory.writeCpuMemory(0x2001, 0x20); // Op
		memory.writeCpuMemory(0x2002, 0x11);
		memory.writeCpuMemory(0x2003, 0x8E);

		cpu._PC = 0x2001;
		cpu.executeOp();

		int savedAddressLow = cpu.pop();
		int savedAddressHigh = cpu.pop();

		assertEquals(6, cpu.getOpCycles());
		assertEquals(0x8E11, cpu._PC);
		assertEquals(0x2003, (savedAddressHigh << 8) | savedAddressLow);
	}
}
