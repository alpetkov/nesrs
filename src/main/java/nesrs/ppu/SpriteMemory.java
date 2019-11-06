package nesrs.ppu;

public class SpriteMemory {
   private int[] _sprRam = new int[0x100]; // Sprite RAM (256b) (64 sprites)

   public int readMemory(int offset) {
      return _sprRam[offset];
   }

   public void writeMemory(int offset, int value) {
      _sprRam[offset] = value;
   }
}
