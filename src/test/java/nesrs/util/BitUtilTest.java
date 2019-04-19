package nesrs.util;

import nesrs.util.BitUtil;

import org.junit.Test;

import junit.framework.TestCase;

public class BitUtilTest extends TestCase {

	@Test
	public void testReverse() throws Exception {
		int k = 0xCD;
		k = BitUtil.reverseByte(k);
		assertEquals(0xB3, k);
	}

	@Test
	public void testBitToInt() throws Exception {
		byte b = (byte)0xFF;
		int result = BitUtil.byteToInt(b);
		assertEquals(0xFF, result);
	}
}
