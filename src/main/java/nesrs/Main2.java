package nesrs;

import java.io.InputStream;

import nesrs.Main.Application;
import nesrs.util.Util;

public class Main2 {
   public static void main(String[] args) throws Exception {
      byte[] nesRom;
      try (InputStream in = RomProvider.getRom()) {
         nesRom = Util.toByteArray(in);
      }

      new Application().start(nesRom);
   }
}
