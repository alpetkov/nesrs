package nesrs.cpu;

import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BootTest extends TestCase {
	private CpuMemory memory;
	private Cpu cpu;

	@Before
	public void setUp() throws Exception {
		memory = new TestCPUMemory();
		cpu = new Cpu(memory, false);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInit() throws Exception {
		memory.writeCpuMemory(0xFFFC, 0x11);
		memory.writeCpuMemory(0xFFFD, 0x10);
		cpu.init();
		assertEquals(cpu._PC, 0x1011);
	}
}
