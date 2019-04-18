package nesrs.cartridge;

import nesrs.cartridge.NameTableMirroring.NameTableMirroringType;

public class CartridgeMemory {
   public final int[][] prgRom;
   public final int[] prgRam;
   public final boolean isPrgRamBatteryBacked;

   public final int[][] chrMem;
   public boolean isChrMemRam;

   public NameTableMirroringType ntMirroringType;

   public CartridgeMemory(
         int[][] prgRom,
         int[] prgRam,
         boolean isPrgRamBatteryBacked,
         int[][] chrMem,
         boolean isChrMemRam,
         NameTableMirroringType ntMirroringType) {

      this.prgRom = prgRom;
      this.prgRam = prgRam;
      this.isPrgRamBatteryBacked = isPrgRamBatteryBacked;

      this.chrMem = chrMem;
      this.isChrMemRam = isChrMemRam;

      this.ntMirroringType = ntMirroringType;
   }
}
