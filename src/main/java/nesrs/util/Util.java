package nesrs.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {
   public static String toHex(int i) {
      String hex = Integer.toHexString(i).toUpperCase();
      if (hex.length() % 2 != 0) {
           hex = "0" + hex;
      }

      return hex;
   }

   public static byte[] toByteArray(InputStream in) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      byte[] buf = new byte[4096];
      int bytesRead;
      while ((bytesRead = in.read(buf)) != -1) {
         out.write(buf, 0, bytesRead);
      }

      return out.toByteArray();
   }
}
