package nesrs.ppu;

import junit.framework.TestCase;
import nesrs.ppu.PpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PpuMemoryMirroringTest extends TestCase {
   private PpuMemory memory;

   @Before
   public void setUp() throws Exception {
      memory = new PpuMemory(null);
   }

   @After
   public void tearDown() throws Exception {
      memory = null;
   }

   // $3F20 - $3FFF    224   bytes   Mirror of Image + Sprite Palettes ($3F00-$3F1F)
   // $3F10 - $3F1F     16   bytes   Sprite Palette
   // $3F00 - $3F0F     16   bytes   Image Palette
   // $3000 - $3EFF   3840   bytes   Mirror of $2000 - $2EFF
   // $2FC0 - $2FFF     64   bytes   Attribute Table 3
   // $2C00 - $2FBF    960   bytes   Name Table 3 (32x30 tiles)
   // $2BC0 - $2BFF     64   bytes   Attribute Table 2
   // $2800 - $2BBF    960   bytes   Name Table 2 (32x30 tiles)
   // $27C0 - $27FF     64   bytes   Attribute Table 1
   // $2400 - $27BF    960   bytes   Name Table 1 (32x30 tiles)
   // $23C0 - $23FF     64   bytes   Attribute Table 0
   // $2000 - $23BF    960   bytes   Name Table 0 (32x30 tiles)
   // $1000 - $1FFF   4096   bytes   Pattern Table 1 (256x2x8, may be VROM)
   // $0000 - $0FFF   4096   bytes   Pattern Table 0 (256x2x8, may be VROM)

   @Test
   public void testPalleteMirror() throws Exception {
      assertEquals(0x3F00, memory.decodeAddress(0x3F00));
      assertEquals(0x3F1F, memory.decodeAddress(0x3F1F));

      assertEquals(0x3F00, memory.decodeAddress(0x3F20));
      assertEquals(0x3F0F, memory.decodeAddress(0x3F2F));

      assertEquals(0x3F00, memory.decodeAddress(0x3F30));
      assertEquals(0x3F1F, memory.decodeAddress(0x3F3F));

      assertEquals(0x3F00, memory.decodeAddress(0x3F40));
      assertEquals(0x3F01, memory.decodeAddress(0x3F41));
      assertEquals(0x3F0F, memory.decodeAddress(0x3F4F));
      assertEquals(0x3F1F, memory.decodeAddress(0x3F5F));

      assertEquals(0x3F1F, memory.decodeAddress(0x3FFF));

      assertEquals(0x3F00, memory.decodeAddress(0x3F10));
   }
}
