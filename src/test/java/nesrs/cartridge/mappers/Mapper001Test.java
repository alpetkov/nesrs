package nesrs.cartridge.mappers;

import org.junit.Assert;
import org.junit.Test;

import nesrs.cartridge.CartridgeMemory;
import nesrs.cartridge.NameTableMirroring.NameTableMirroringType;

public class Mapper001Test {

   @Test
   public void testXXX() {
      CartridgeMemory cartridgeMemory = new CartridgeMemory(
            new int[64][1024],
            new int[1024],
            false,
            new int[8][1024],
            true,
            NameTableMirroringType.ONE_SCREEN_A);

      Mapper001 mapper = new Mapper001(cartridgeMemory);
      Assert.assertEquals(8, mapper._chrMemMap.length);
      Assert.assertArrayEquals(
            new int[] {0, 1, 2, 3, 4, 5, 6, 7},
            mapper._chrMemMap);

      Assert.assertArrayEquals(
            new int[] {
                  0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                  48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63},
            mapper._prgRomMap);
   }
}
