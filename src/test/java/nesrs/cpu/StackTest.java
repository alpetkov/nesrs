package nesrs.cpu;


import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StackTest extends TestCase {
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
	public void testStack() throws Exception {
		for (int s = 0xFF; s > 0; s--) {
			int value = s;
			cpu.push(value);
			assertEquals(s - 1, cpu._S);
			assertEquals(value, memory.readCpuMemory(0x0100 | s));
		}
		assertEquals(0, cpu._S);
		cpu.push(100);
		for (int s = 0xFF; s > 0; s--) {
			int value = s;
			cpu.push(value);
			assertEquals(s - 1, cpu._S);
			assertEquals(value, memory.readCpuMemory(0x0100 | s));
		}
	}
}
