package nesrs.util;

public class BitUtil {
   public static int reverseByte(int b) {
      b = Integer.reverse(b);
      b = (b >> 24) & 0xFF;
      return b;
   }

   public static int byteToInt(byte b) {
      return (b | (b & 0x80)) & 0xFF;
   }

   public static int[] byteToInt(byte[] b) {
      int[] result = new int[b.length];

      for (int i = 0; i < b.length; i++) {
         result[i] = byteToInt(b[i]);
      }

      return result;
   }
}
