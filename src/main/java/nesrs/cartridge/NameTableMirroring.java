package nesrs.cartridge;

public class NameTableMirroring {

   public enum NameTableIndex {
      A,
      B,
      C,
      D
   }

   public enum NameTableMirroringType {
      HORIZONTAL,
      VERTICAL,
      ONE_SCREEN_A,
      ONE_SCREEN_B,
      FOUR_SCREEN
   }

   public static int getNameTableOffset(int address) {
      // [0x2000, 0x23FF] -> address - 0x2000, [0x2400, 0x27FF] -> address - 0x2400
      // [0x2800, 0x2BFF] -> address - 0x2800, [0x2C00, 0x2FFF] -> address - 0x2C00
      return (address & 0x03FF);
   }

   public static NameTableIndex getNameTableIndex(
         int ppuAddress,
         NameTableMirroringType mirroringType) {

      switch (mirroringType) {
      case HORIZONTAL:

         // Horizontal Mirroring:
         // [0x2000, 0x23FF] -> NTA, [0x2400, 0x27FF] -> NTA
         // [0x2800, 0x2BFF] -> NTB, [0x2C00, 0x2FFF] -> NTB
         if ((ppuAddress & 0x0800) == 0) {
            return NameTableIndex.A;
         } else {
            return NameTableIndex.B;
         }

      case VERTICAL:
         // Vertical Mirroring:
         // [0x2000, 0x23FF] -> NTA, [0x2400, 0x27FF] -> NTB
         // [0x2800, 0x2BFF] -> NTA, [0x2C00, 0x2FFF] -> NTB
         if ((ppuAddress & 0x0400) == 0) {
            return NameTableIndex.A;
         } else {
            return NameTableIndex.B;
         }

      case ONE_SCREEN_A:
         // One Screen Mirroring: All address points to the same data.
         // [0x2000, 0x23FF] -> NTA, [0x2400, 0x27FF] -> NTA
         // [0x2800, 0x2BFF] -> NTA, [0x2C00, 0x2FFF] -> NTA
         // Enabled by a mapper usually.
         return NameTableIndex.A;

      case ONE_SCREEN_B:
         // One Screen Mirroring: All address points to the same data.
         // [0x2000, 0x23FF] -> NTB, [0x2400, 0x27FF] -> NTB
         // [0x2800, 0x2BFF] -> NTB, [0x2C00, 0x2FFF] -> NTB
         // Enabled by a mapper usually.
         return NameTableIndex.B;

      case FOUR_SCREEN:
         // 4 screen Mirroring: Each addresses have their own memory space.
         // Enable by a mapper usually.
         // [0x2000, 0x23FF] -> NTA, [0x2400, 0x27FF] -> NTB
         // [0x2800, 0x2BFF] -> NTC, [0x2C00, 0x2FFF] -> NTD
         int bucket = (ppuAddress & 0x0C00);

         switch (bucket) {
         case 0x0000:
            return NameTableIndex.A;

         case 0x0300:
            return NameTableIndex.B;

         case 0x0800:
            return NameTableIndex.C;

         default:
            return NameTableIndex.D;
         }
      }

      // Keep compiler silent
      return NameTableIndex.A;
   }
}
