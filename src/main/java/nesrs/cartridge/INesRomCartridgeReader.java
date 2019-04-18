package nesrs.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import nesrs.cartridge.NameTableMirroring.NameTableMirroringType;
import nesrs.util.BitUtil;

public class INesRomCartridgeReader {
   //   The iNES format:
   //
   //   1. Header (16 bytes)
   //   2. Trainer, if present (0 or 512 bytes)
   //   3. PRG ROM data (16384 * x bytes)
   //   4. CHR ROM data, if present (8192 * y bytes)
   //   5. PlayChoice hint screen, if present (0 or 8192 bytes)
   //
   //  The format of the header is as follows:
   //
   //   * 0-3: Constant $4E $45 $53 $1A ("NES" followed by MS-DOS end-of-file)
   //   * 4: Size of PRG ROM in 16 KB units
   //   * 5: Size of CHR ROM in 8 KB units (Value 0 means the board uses CHR RAM)
   //   * 6: Flags 6
   //   * 7: Flags 7
   //   * 8: Size of PRG RAM in 8 KB units (Value 0 infers 8 KB for compatibility; see PRG RAM circuit)
   //   * 9: Flags 9
   //   * 10: Flags 10 (unofficial)
   //   * 11-15: Zero filled

   private static final byte BYTE_0 = 0x4E; // N
   private static final byte BYTE_1 = 0x45; // E
   private static final byte BYTE_2 = 0x53; // S
   private static final byte BYTE_3 = 0x1A; // *EOF"

   // Byte 6
   // 76543210
   // ||||||||
   // ||||+||+- 0xx0: vertical arrangement/horizontal mirroring
   // |||| ||   0xx1: horizontal arrangement/vertical mirroring
   // |||| ||   1xxx: four-screen mirroring
   // |||| |+-- 1: SRAM/PRG-RAM in CPU $6000-$7FFF, if present, is battery backed
   // |||| +--- 1: 512-byte trainer at $7000-$71FF (stored before PRG data)
   // ++++----- Lower nibble of mapper number
   private static final int BYTE_6_MAPPER_NUMBER_LOWER = 0xF0;
   private static final int BYTE_6_MIRRORING_2 = 0x8;
   private static final int BYTE_6_TRAINER = 0x4;
   private static final int BYTE_6_SRAM_BATTERY_BACKED = 0x2;
   private static final int BYTE_6_MIRRORING_1 = 0x1;

   // Byte 7
   // 76543210
   // ||||||||
   // |||||||+- VS Unisystem
   // ||||||+-- PlayChoice-10 (8KB of Hint Screen data stored after CHR data)
   // ||||++--- If equal to 2, flags 8-15 are in NES 2.0 format
   // ++++----- Upper nibble of mapper number
   private static final int BYTE_7_MAPPER_NUMBER_UPPER = 0xF0;

   // Byte 9
   // 76543210
   // ||||||||
   // |||||||+- TV system (0: NTSC; 1: PAL)
   // +++++++-- Reserved, set to zero

   // Byte 10
   // 76543210
   //   ||  ||
   //   ||  ++- TV system (0: NTSC; 2: PAL; 1/3: dual compatible)
   //   |+----- SRAM in CPU $6000-$7FFF is 0: present; 1: not present
   //   +------ 0: Board has no bus conflicts; 1: Board has bus conflicts

   private int _prgRomSize;
   private int _chrRomSize;

   private int _byte6;
   private int _byte7;

   private int _prgRAMSize;

   private int _byte9;
   private int _byte10;

   private boolean _isMapperNumberUpperNibbleSupported;

   private int[] _trainer;

   private int[][] _prgRom;
   private int[][] _chrRom;

   private final InputStream _inesRomStream;
   private final boolean _closeStream;

   public INesRomCartridgeReader(InputStream inesRomStream) {
      _inesRomStream = inesRomStream;
      _closeStream = false;
   }

   public INesRomCartridgeReader(File inesRomFile) throws IOException {
      _inesRomStream = new FileInputStream(inesRomFile);
      _closeStream = true;
   }

   public Cartridge readCartridge() {
      try {
         processHeader();

         readTrainerIfPresent();

         readPrgRomData();

         readChrRomDataIfPresent();

         return assembleCartridge();
      } catch (CartridgeReadException e) {
         return null;
      } finally {
         if (_closeStream) {
            try {
               _inesRomStream.close();
            } catch (IOException e) {
            }
         }
      }
   }

   private void processHeader() throws CartridgeReadException {
      byte[] headerBytes = new byte[16];
      int bytesRead;
      try {
         bytesRead = _inesRomStream.read(headerBytes);
      } catch (IOException e) {
         throw new CartridgeReadException(e);
      }
      if (bytesRead != 16) {
         throw new CartridgeReadException("Wrong header length");
      }

      // Validate bytes 0-3
      if (headerBytes[0] != BYTE_0 || headerBytes[1] != BYTE_1 ||
            headerBytes[2] != BYTE_2 || headerBytes[3] != BYTE_3) {
         throw new CartridgeReadException("Header should start with 'NES' followed by DOS EOF symbol");
      }

      _prgRomSize = headerBytes[4];
      _chrRomSize = headerBytes[5];

      _byte6 = headerBytes[6];
      _byte7 = headerBytes[7];

      _prgRAMSize = headerBytes[8];

      _byte9 = headerBytes[9];
      _byte10 = headerBytes[10];

      _isMapperNumberUpperNibbleSupported = true;
      for (int i = 11; i < headerBytes.length; i++) {
         if (headerBytes[i] != 0) {
            _isMapperNumberUpperNibbleSupported = false;
            break;
         }
      }
   }

   private void readTrainerIfPresent() throws CartridgeReadException {
      if ((_byte6 & BYTE_6_TRAINER) != 0) {
         byte[] bytes = new byte[512];
         try {
            int bytesRead = _inesRomStream.read(bytes);
            if (bytesRead != bytes.length) {
               throw new CartridgeReadException("Trainer read problem");
            }
            _trainer = BitUtil.byteToInt(bytes);
         } catch (IOException e) {
            throw new CartridgeReadException(e);
         }
      }
   }

   private void readPrgRomData() throws CartridgeReadException {
      if (_prgRomSize > 0) {
         _prgRom = new int[_prgRomSize * 16][1024];
         for (int i = 0; i < _prgRomSize * 16; i++) {
            byte[] prgRomBank = new byte[1024];
            try {
               int bytesRead = _inesRomStream.read(prgRomBank);
               if (bytesRead != prgRomBank.length) {
                  throw new CartridgeReadException("PRG ROM read problem");
               }

            } catch (IOException e) {
               throw new CartridgeReadException(e);
            }
            _prgRom[i] = BitUtil.byteToInt(prgRomBank);
         }
      }
   }

   private void readChrRomDataIfPresent() throws CartridgeReadException {
      if (_chrRomSize > 0) {
         _chrRom = new int[_chrRomSize * 8][1024];
         for (int i = 0; i < _chrRom.length; i++) {
            byte[] chrRomBank = new byte[1024];
            try {
               int bytesRead = _inesRomStream.read(chrRomBank);
               if (bytesRead != chrRomBank.length) {
                  throw new CartridgeReadException("CHR ROM bytes read problem");
               }

            } catch (IOException e) {
               throw new CartridgeReadException(e);
            }

            _chrRom[i] = BitUtil.byteToInt(chrRomBank);
         }
      }
   }

   private Cartridge assembleCartridge() {
      int[] prgRam;
      if (_prgRAMSize == 0) { // 0 implies 8 KB for compatibility
         prgRam = new int[8 * 1024];
      } else {
         prgRam = new int[_prgRAMSize * 8 * 1024];
      }

      boolean isPrgRamBatteryBacked = (_byte6 & BYTE_6_SRAM_BATTERY_BACKED) != 0;

      int[][] chrMem;
      boolean isChrMemRam;
      if (_chrRomSize != 0) {
         chrMem = _chrRom;
         isChrMemRam = false;
      } else {
         chrMem = new int[8][1024];
         isChrMemRam = true;
      }

      NameTableMirroringType ntMirroringType;
      if ((_byte6 & BYTE_6_MIRRORING_2) != 0) {
         ntMirroringType = NameTableMirroringType.FOUR_SCREEN;
      } else {
         if ((_byte6 & BYTE_6_MIRRORING_1) == 0) {
            ntMirroringType = NameTableMirroringType.HORIZONTAL;
         } else {
            ntMirroringType = NameTableMirroringType.VERTICAL;
         }
      }

      CartridgeMemory cartridgeMemory =
            new CartridgeMemory(
                  _prgRom,
                  prgRam,
                  isPrgRamBatteryBacked,
                  chrMem,
                  isChrMemRam,
                  ntMirroringType);

      int mapperNumber = ((_byte6 & BYTE_6_MAPPER_NUMBER_LOWER) >> 4) & 0x0F;
      if (_isMapperNumberUpperNibbleSupported) {
         mapperNumber += _byte7 & BYTE_7_MAPPER_NUMBER_UPPER;
      }

      return new Cartridge(cartridgeMemory, mapperNumber);
   }

   private static class CartridgeReadException extends Exception {
      private static final long serialVersionUID = 1L;

      public CartridgeReadException(Throwable cause) {
         super(cause);
      }

      public CartridgeReadException(String message) {
         super(message);
      }
   }
}
