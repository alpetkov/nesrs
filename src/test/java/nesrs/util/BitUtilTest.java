package nesrs.util;

import nesrs.util.BitUtil;
import junit.framework.TestCase;

public class BitUtilTest extends TestCase {
	public void testReverse() throws Exception {
		int k = 0xCD;
		k = BitUtil.reverseByte(k);
		assertEquals(0xB3, k);
	}

	public void testBitToInt() throws Exception {
		byte b = (byte)0xFF;
		int result = BitUtil.byteToInt(b);
		assertEquals(0xFF, result);
	}

	public void testByte() {
		byte a = (byte)0xAA;
		a = (byte)(a << 1);
		for (int i = Byte.SIZE - 1; i >= 0; i--) {
			System.out.print((a >> i) & 0x1);
		}
	}
}
