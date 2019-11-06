package nesrs.cpu;

public class Cpu implements CpuPin {
   // Status register's flags
   public static final int C_FLAG = 0x01; // Carry flag. 1 -> Carry occurred
   public static final int Z_FLAG = 0x02; // Zero flag. 1 -> Result is zero
   public static final int I_FLAG = 0x04; // Interrupt flag. 1 -> IRQ disabled
   public static final int D_FLAG = 0x08; // Decimal mode flag. 1 -> Decimal arithmetic
   public static final int B_FLAG = 0x10; // Break flag. 1 -> BRK instruction occurred
   public static final int R_FLAG = 0x20; // Not used. Always is set to 1
   public static final int V_FLAG = 0x40; // Overflow flag. 1 -> Overflow occurred
   public static final int N_FLAG = 0x80; // Negative flag. 1 -> Result is negative

   // Interrupt types
   private enum InterruptType {
      RESET,
      NMI,
      IRQ
   }

   // Operations' cycles
   private static final int[] OP_CYCLES = new int[] {
   /*       0 1 2 3 4 5 6 7 8 9 A B C D E F*/
   /*0x00*/ 7,6,2,8,3,3,5,5,3,2,2,2,4,4,6,6,
   /*0x10*/ 2,5,2,8,4,4,6,6,2,4,2,7,4,4,7,7,
   /*0x20*/ 6,6,2,8,3,3,5,5,4,2,2,2,4,4,6,6,
   /*0x30*/ 2,5,2,8,4,4,6,6,2,4,2,7,4,4,7,7,
   /*0x40*/ 6,6,2,8,3,3,5,5,3,2,2,2,3,4,6,6,
   /*0x50*/ 2,5,2,8,4,4,6,6,2,4,2,7,4,4,7,7,
   /*0x60*/ 6,6,2,8,3,3,5,5,4,2,2,2,5,4,6,6,
   /*0x70*/ 2,5,2,8,4,4,6,6,2,4,2,7,4,4,7,7,
   /*0x80*/ 2,6,2,6,3,3,3,3,2,2,2,2,4,4,4,4,
   /*0x90*/ 2,6,2,6,4,4,4,4,2,5,2,5,5,5,5,5,
   /*0xA0*/ 2,6,2,6,3,3,3,3,2,2,2,2,4,4,4,4,
   /*0xB0*/ 2,5,2,5,4,4,4,4,2,4,2,4,4,4,4,4,
   /*0xC0*/ 2,6,2,8,3,3,5,5,2,2,2,2,4,4,6,6,
   /*0xD0*/ 2,5,2,8,4,4,6,6,2,4,2,7,4,4,7,7,
   /*0xE0*/ 2,6,3,8,3,3,5,5,2,2,2,2,4,4,6,6,
   /*0xF0*/ 2,5,2,8,4,4,6,6,2,4,2,7,4,4,7,7,
   };

   // Operations' names
   private static final String[] OP_NAMES = new String[] {
   /*           0     1     2     3     4     5     6     7     8     9     A     B     C     D     E     F*/
   /*0x00*/ "BRK","ORA","NOP","SLO","DOP","ORA","ASL","SLO","PHP","ORA","ASL","NOP","TOP","ORA","ASL","SLO",
   /*0x10*/ "BPL","ORA","NOP","SLO","DOP","ORA","ASL","SLO","CLC","ORA","NOP","SLO","TOP","ORA","ASL","SLO",
   /*0x20*/ "JSR","AND","NOP","RLA","BIT","AND","ROL","RLA","PLP","AND","ROL","NOP","BIT","AND","ROL","RLA",
   /*0x30*/ "BMI","AND","NOP","RLA","DOP","AND","ROL","RLA","SEC","AND","NOP","RLA","TOP","AND","ROL","RLA",
   /*0x40*/ "RTI","EOR","NOP","SRE","DOP","EOR","LSR","SRE","PHA","EOR","LSR","NOP","JMP","EOR","LSR","SRE",
   /*0x50*/ "BVC","EOR","NOP","SRE","DOP","EOR","LSR","SRE","CLI","EOR","NOP","SRE","TOP","EOR","LSR","SRE",
   /*0x60*/ "RTS","ADC","NOP","RRA","DOP","ADC","ROR","RRA","PLA","ADC","ROR","NOP","JMP","ADC","ROR","RRA",
   /*0x70*/ "BVS","ADC","NOP","RRA","DOP","ADC","ROR","RRA","SEI","ADC","NOP","RRA","TOP","ADC","ROR","RRA",
   /*0x80*/ "DOP","STA","DOP","AAX","STY","STA","STX","AAX","DEY","DOP","TXA","NOP","STY","STA","STX","AAX",
   /*0x90*/ "BCC","STA","NOP","NOP","STY","STA","STX","AAX","TYA","STA","TXS","NOP","NOP","STA","NOP","NOP",
   /*0xA0*/ "LDY","LDA","LDX","LAX","LDY","LDA","LDX","LAX","TAY","LDA","TAX","NOP","LDY","LDA","LDX","LAX",
   /*0xB0*/ "BCS","LDA","NOP","LAX","LDY","LDA","LDX","LAX","CLV","LDA","TSX","NOP","LDY","LDA","LDX","LAX",
   /*0xC0*/ "CPY","CMP","DOP","DCP","CPY","CMP","DEC","DCP","INY","CMP","DEX","NOP","CPY","CMP","DEC","DCP",
   /*0xD0*/ "BNE","CMP","NOP","DCP","DOP","CMP","DEC","DCP","CLD","CMP","NOP","DCP","TOP","CMP","DEC","DCP",
   /*0xE0*/ "CPX","SBC","DOP","ISC","CPX","SBC","INC","ISC","INX","SBC","NOP","SBC","CPX","SBC","INC","ISC",
   /*0xF0*/ "BEQ","SBC","NOP","ISC","DOP","SBC","INC","ISC","SED","SBC","NOP","ISC","TOP","SBC","INC","ISC",
   };

   // CPU's registers
   /*package*/ int _A;  // Accumulator (8 bits)
   /*package*/ int _X;  // X index register (8 bits)
   /*package*/ int _Y;  // Y index register (8 bits)
   /*package*/ int _S;  // Stack pointer (8 bits)
   /*package*/ int _P;  // Status register (set of flags) (8 bits)
   /*package*/ int _PC; // Program counter (16 bits)

   // Number of cycles of the last executed op
   private int _opCycles;

   // Pending interrupt
   private InterruptType _pendingInterrupt;

   // 64Kb of CPU's addressable memory
   private final CpuMemory _memory;

   private final boolean _decimalModeSupported;

   //
   // Constructors
   //

   public Cpu(CpuMemory memory) {
      this(memory, false/*decimalModeSupported*/);
   }

   public Cpu(CpuMemory memory, boolean decimalModeSupported) {
      _memory = memory;
      _decimalModeSupported = decimalModeSupported;
   }

   public int getA() {
      return _A;
   }

   public int getX() {
      return _X;
   }

   public int getY() {
      return _Y;
   }

   public int getS() {
      return _S;
   }

   public int getP() {
      return _P;
   }

   public int getPC() {
      return _PC;
   }

   //
   // Actions
   //

   public void init(int A, int X, int Y, int S, int P, int PC) {
      _opCycles = 0;
      _A = A;
      _X = X;
      _Y = Y;
      _S = S;
      _P = P;
      _PC = PC;
   }

   @Override
   public int init() {
      _opCycles = 7;
      _A = 0x00;
      _X = 0x00;
      _Y = 0x00;
      _S = 0xFF;
      _P = B_FLAG | R_FLAG | I_FLAG;
      _PC = (readMemory(0xFFFD) << 8) | readMemory(0xFFFC);

      return _opCycles;
   }

   @Override
   public void reset() {
      requestInterrupt(InterruptType.RESET);
   }

   @Override
   public void nmi() {
      requestInterrupt(InterruptType.NMI);
   }

   @Override
   public void irq() {
      requestInterrupt(InterruptType.IRQ);
   }

   @Override
   public int executeOp() {
      if (hasPendingInterrupt()) {
         executePendingInterruptOp();
      } else {
         int opCode = readMemory(_PC++);
         _opCycles = OP_CYCLES[opCode];
         executeOp(opCode);
      }

      return _opCycles;
   }

   @Override
   public int getOpCycles() {
      return _opCycles;
   }

   //
   // Interrupt controller
   //

   private void requestInterrupt(InterruptType type) {
      if (_pendingInterrupt == null) {
         _pendingInterrupt = type;

      } else if (_pendingInterrupt == InterruptType.IRQ) {
         _pendingInterrupt = type;

      } else if (_pendingInterrupt == InterruptType.NMI) {
         if (type == InterruptType.RESET) {
            _pendingInterrupt = type;
         }

      } else if (_pendingInterrupt == InterruptType.RESET) {
         // Already requested
      }
   }

   private void executePendingInterruptOp() {
      _opCycles = 0;

      if (_pendingInterrupt == InterruptType.RESET) {
         _opCycles = 7;
         _A = 0x00;
         _X = 0x00;
         _Y = 0x00;
         _S = 0xFF;
         _P = Z_FLAG | R_FLAG;
         _PC = (readMemory(0xFFFD) << 8) | readMemory(0xFFFC);

      } else if (_pendingInterrupt == InterruptType.NMI) {
         _opCycles = 7;
         push((_PC >> 8) & 0xFF);
         push(_PC & 0x00FF);
         push(_P & ~B_FLAG);
         _P = _P & ~D_FLAG;
         _PC = (readMemory(0xFFFB) << 8) | readMemory(0xFFFA);

      } else if (_pendingInterrupt == InterruptType.IRQ && ((_P & I_FLAG) == 0)) {
         _opCycles = 7;
         push((_PC >> 8) & 0xFF);
         push(_PC & 0x00FF);
         push(_P & ~B_FLAG);
         _P = _P & ~D_FLAG;
         _P = _P & ~I_FLAG;
         _PC = (readMemory(0xFFFF) << 8) | readMemory(0xFFFE);
      }

      _pendingInterrupt = null;
   }

   private boolean hasPendingInterrupt() {
      return _pendingInterrupt != null;
   }

   //
   // Memory management
   //

   @Override
   public int readMemory(int address) {
      return _memory.read(address);
   }

   @Override
   public void writeMemory(int address, int value) {
      int additionalWriteCycles = _memory.write(address, value);
      if (additionalWriteCycles > 0) {
         _opCycles += additionalWriteCycles;
      }
   }

   private boolean isPageBoundaryCrossed(int address1, int address2) {
      return (address1 >> 8) != (address2 >> 8);
   }

   //
   // Addressing modes
   //

   // 1. Accumulator addressing - ACC
   // 2. Implied addressing - IMPL

   // 3. Immediate addressing - IMM
   private int calculateMemoryAddressIMM() {
      return _PC++;
   }

   // 4. Absolute addressing - ABS
   private int calculateMemoryAddressABS() {
      int low = readMemory(_PC++);
      int high = readMemory(_PC++);
      return 0xFFFF & ((high << 8) | low);
   }

   // 5. Zero page addressing - ZP
   private int calculateMemoryAddressZP() {
      int low = readMemory(_PC++);
      return low;
   }

   // 6. Indexed zero page addressing with register X - ZP,X
   private int calculateMemoryAddressZPX() {
      int low = readMemory(_PC++);
      return 0x00FF & (_X + low);
   }

   // 7. Indexed zero page addressing with register Y - ZP,Y
   private int calculateMemoryAddressZPY() {
      int low = readMemory(_PC++);
      return 0x00FF & (_Y + low);
   }

   // 8. Indexed absolute addressing with register X - ABS,X
   private int calculateMemoryAddressABSX(
         boolean countAdditionalCycleOnPageBoundaryCrossed) {
      int low = readMemory(_PC++);
      int high = readMemory(_PC++);
      int address = 0xFFFF & ((high << 8) | low);
      int resultAddress = 0xFFFF & (address + _X);
      if (countAdditionalCycleOnPageBoundaryCrossed
            && isPageBoundaryCrossed(address, resultAddress)) {
         _opCycles++;
      }

      return resultAddress;
   }

   // 9. Indexed absolute addressing with register Y - ABS,Y
   private int calculateMemoryAddressABSY(
         boolean countAdditionalCycleOnPageBoundaryCrossed) {
      int low = readMemory(_PC++);
      int high = readMemory(_PC++);
      int address = 0xFFFF & ((high << 8) | low);
      int resultAddress = 0xFFFF & (address + _Y);
      if (countAdditionalCycleOnPageBoundaryCrossed
            && isPageBoundaryCrossed(address, resultAddress)) {
         _opCycles++;
      }

      return resultAddress;
   }

   // 10. Relative addressing - REL
   private int calculateMemoryAddressREL() {
      int inc = readMemory(_PC++);
      int offset = 0;
      boolean isPositive = true;
      if ((inc & 0x80) == 0) {
         // Positive or Zero
         offset = inc & 0x7F;
      } else {
         // Negative
         offset = 0x7F + 1 - (inc & 0x7F);
         isPositive = false;
      }

      int address = isPositive ? _PC + offset : _PC - offset;

      return 0xFFFF & address;
   }

   // 11. Indexed indirect (pre-indexed) addressing with register X - (IND,X)
   private int calculateMemoryAddress_INDX_() {
      int low = readMemory(_PC++);
      int address = 0x00FF & (low + _X);
      int nextAddress = 0x00FF & (address + 1);
      return 0xFFFF & ((readMemory(nextAddress) << 8) | readMemory(address));
   }

   // 12. Indirect indexed (post-indexed) addressing with register Y - (IND),Y
   private int calculateMemoryAddress_IND_Y(
         boolean countAdditionalCycleOnPageBoundaryCrossed) {
      int low = readMemory(_PC++);

      int lowAddress = readMemory(low);
      int highAddress = readMemory(0x00FF & (low + 1));
      int address = 0xFFFF & ((highAddress << 8) | lowAddress);
      int resultAddress = 0xFFFF & (address + _Y);
      if (countAdditionalCycleOnPageBoundaryCrossed
            && isPageBoundaryCrossed(address, resultAddress)) {
         _opCycles++;
      }

      return resultAddress;
   }

   // 13. Absolute indirect addressing - IND
   private int calculateMemoryAddressIND(boolean isPageWrappingNotAllowed) {
      int low = readMemory(_PC++);
      int high = readMemory(_PC++);
      int address = 0xFFFF & ((high << 8) | low);
      int nextAddress = address + 1;
      if ((address & 0xFF) == 0xFF) {
         if (isPageWrappingNotAllowed) {
            nextAddress = address & 0xFF00;
         }
      }
      return 0xFFFF & ((readMemory(nextAddress) << 8) | readMemory(address));
   }

   private int calculateMemoryAddress(CpuAddressingMode mode) {
      int address = 0;

      switch (mode) {
         case ACC: break;
         case IMPL: break;
         case IMM: address = calculateMemoryAddressIMM(); break;
         case ABS: address = calculateMemoryAddressABS(); break;
         case ZP: address = calculateMemoryAddressZP(); break;
         case ZPX: address = calculateMemoryAddressZPX(); break;
         case ZPY: address = calculateMemoryAddressZPY(); break;
         case ABSX: address = calculateMemoryAddressABSX(false); break;
         case ABSX_: address = calculateMemoryAddressABSX(true); break;
         case ABSY: address = calculateMemoryAddressABSY(false); break;
         case ABSY_: address = calculateMemoryAddressABSY(true); break;
         case REL: address = calculateMemoryAddressREL(); break;
         case _INDX: address = calculateMemoryAddress_INDX_(); break;
         case _IND_Y: address = calculateMemoryAddress_IND_Y(false); break;
         case _IND_Y_: address = calculateMemoryAddress_IND_Y(true); break;
         case IND: address = calculateMemoryAddressIND(false); break;
         case IND_: address = calculateMemoryAddressIND(true); break;
      }

      return address;
   }

   //
   // Stack manipulations
   //

   /*package*/ void push(int value) {
      writeMemory(0x0100 | _S, value);
      if (_S == 0x00) {
         _S = 0xFF;
      } else {
         _S--;
      }
   }

   /*package*/ int pop() {
      if (_S == 0xFF) {
         _S = 0x00;
      } else {
         _S++;
      }

      return readMemory(0x0100 | _S);
   }

   //
   // CPU running cycle
   //

   private void executeOp(int opCode) {
      switch (opCode) {

      /*1.ADC*/
      case 0x69/*IMM*/: opADC(CpuAddressingMode.IMM); break;
      case 0x65/*ZP*/: opADC(CpuAddressingMode.ZP); break;
      case 0x75/*ZP,X*/: opADC(CpuAddressingMode.ZPX); break;
      case 0x6D/*ABS*/: opADC(CpuAddressingMode.ABS); break;
      case 0x7D/*ABS,X+*/:opADC(CpuAddressingMode.ABSX_); break;
      case 0x79/*ABS,Y+*/:opADC(CpuAddressingMode.ABSY_); break;
      case 0x61/*(IND,X)*/:opADC(CpuAddressingMode._INDX); break;
      case 0x71/*(IND),Y+*/:opADC(CpuAddressingMode._IND_Y_); break;

      /*2.AND*/
      case 0x29/*IMM*/: opAND(CpuAddressingMode.IMM); break;
      case 0x25/*ZP*/: opAND(CpuAddressingMode.ZP); break;
      case 0x35/*ZP,X*/: opAND(CpuAddressingMode.ZPX); break;
      case 0x2D/*ABS*/: opAND(CpuAddressingMode.ABS); break;
      case 0x3D/*ABS,X+*/: opAND(CpuAddressingMode.ABSX_); break;
      case 0x39/*ABS,Y+*/: opAND(CpuAddressingMode.ABSY_); break;
      case 0x21/*(IND,X)*/: opAND(CpuAddressingMode._INDX); break;
      case 0x31/*(IND),Y+*/: opAND(CpuAddressingMode._IND_Y_); break;

      /*3.ASL*/
      case 0x0A/*ACC*/: opASL(CpuAddressingMode.ACC); break;
      case 0x06/*ZP*/: opASL(CpuAddressingMode.ZP); break;
      case 0x16/*ZP,X*/: opASL(CpuAddressingMode.ZPX); break;
      case 0x0E/*ABS*/: opASL(CpuAddressingMode.ABS); break;
      case 0x1E/*ABS,X*/: opASL(CpuAddressingMode.ABSX); break;

      /*4.BCC*/
      case 0x90/*REL*/: opBCC(CpuAddressingMode.REL); break;

      /*5.BCS*/
      case 0xB0/*REL*/: opBCS(CpuAddressingMode.REL); break;

      /*6.BEQ*/
      case 0xF0/*REL*/: opBEQ(CpuAddressingMode.REL); break;

      /*7.BIT*/
      case 0x24/*ZP*/: opBIT(CpuAddressingMode.ZP); break;
      case 0x2C/*ABS*/: opBIT(CpuAddressingMode.ABS); break;

      /*8.BMI*/
      case 0x30/*REL*/: opBMI(CpuAddressingMode.REL); break;

      /*9.BNE*/
      case 0xD0/*REL*/: opBNE(CpuAddressingMode.REL); break;

      /*10.BPL*/
      case 0x10/*REL*/: opBPL(CpuAddressingMode.REL); break;

      /*11.BRK*/
      case 0x00/*IMPL*/: opBRK(); break;

      /*12.BVC*/
      case 0x50/*REL*/: opBVC(CpuAddressingMode.REL); break;

      /*13.BVS*/
      case 0x70/*REL*/: opBVS(CpuAddressingMode.REL); break;

      /*14.CLC*/
      case 0x18/*IMPL*/: _P = _P & ~C_FLAG; break;

      /*15.CLD*/
      case 0xD8/*IMPL*/: _P = _P & ~D_FLAG; break;

      /*16.CLI*/
      case 0x58/*IMPL*/: _P = _P & ~I_FLAG; break;

      /*17.CLV*/
      case 0xB8/*IMPL*/: _P = _P & ~V_FLAG; break;

      /*18.CMP*/
      case 0xC9/*IMM*/: opCMP(CpuAddressingMode.IMM); break;
      case 0xC5/*ZP*/: opCMP(CpuAddressingMode.ZP); break;
      case 0xD5/*ZP,X*/: opCMP(CpuAddressingMode.ZPX); break;
      case 0xCD/*ABS*/: opCMP(CpuAddressingMode.ABS); break;
      case 0xDD/*ABS,X+*/: opCMP(CpuAddressingMode.ABSX_); break;
      case 0xD9/*ABS,Y+*/: opCMP(CpuAddressingMode.ABSY_); break;
      case 0xC1/*(IND,X)*/: opCMP(CpuAddressingMode._INDX); break;
      case 0xD1/*(IND),Y+*/: opCMP(CpuAddressingMode._IND_Y_); break;

      /*19.CPX*/
      case 0xE0/*IMM*/: opCPX(CpuAddressingMode.IMM); break;
      case 0xE4/*ZP*/: opCPX(CpuAddressingMode.ZP); break;
      case 0xEC/*ABS*/: opCPX(CpuAddressingMode.ABS); break;

      /*20.CPY*/
      case 0xC0/*IMM*/: opCPY(CpuAddressingMode.IMM); break;
      case 0xC4/*ZP*/: opCPY(CpuAddressingMode.ZP); break;
      case 0xCC/*ABS*/: opCPY(CpuAddressingMode.ABS); break;

      /*21.DEC*/
      case 0xC6/*ZP*/: opDEC(CpuAddressingMode.ZP); break;
      case 0xD6/*ZP,X*/: opDEC(CpuAddressingMode.ZPX); break;
      case 0xCE/*ABS*/: opDEC(CpuAddressingMode.ABS); break;
      case 0xDE/*ABS,X*/: opDEC(CpuAddressingMode.ABSX); break;

      /*22.DEX*/
      case 0xCA/*IMPL*/: _X = opDecrease(_X); break;

      /*23.DEY*/
      case 0x88/*IMPL*/: _Y = opDecrease(_Y); break;

      /*24.EOR*/
      case 0x49/*IMM*/: opEOR(CpuAddressingMode.IMM); break;
      case 0x45/*ZP*/: opEOR(CpuAddressingMode.ZP); break;
      case 0x55/*ZP,X*/: opEOR(CpuAddressingMode.ZPX); break;
      case 0x4D/*ABS*/: opEOR(CpuAddressingMode.ABS); break;
      case 0x5D/*ABS,X+*/: opEOR(CpuAddressingMode.ABSX_); break;
      case 0x59/*ABS,Y+*/: opEOR(CpuAddressingMode.ABSY_); break;
      case 0x41/*(IND,X)*/: opEOR(CpuAddressingMode._INDX); break;
      case 0x51/*(IND),Y+*/: opEOR(CpuAddressingMode._IND_Y_); break;

      /*25.INC*/
      case 0xE6/*ZP*/: opINC(CpuAddressingMode.ZP); break;
      case 0xF6/*ZP,X*/: opINC(CpuAddressingMode.ZPX); break;
      case 0xEE/*ABS*/: opINC(CpuAddressingMode.ABS); break;
      case 0xFE/*ABS,X*/: opINC(CpuAddressingMode.ABSX); break;

      /*26.INX*/
      case 0xE8/*IMPL*/: _X = opIncrease(_X); break;

      /*27.INY*/
      case 0xC8/*IMPL*/: _Y = opIncrease(_Y); break;

      /*28.JMP*/
      case 0x4C/*ABS*/: opJMP(CpuAddressingMode.ABS); break;
      case 0x6C/*IND*/: opJMP(CpuAddressingMode.IND_); break;

      /*29.JSR*/
      case 0x20/*ABS*/:opJSR(CpuAddressingMode.ABS); break;

      /*30.LDA*/
      case 0xA9/*IMM*/: opLDA(CpuAddressingMode.IMM); break;
      case 0xA5/*ZP*/: opLDA(CpuAddressingMode.ZP); break;
      case 0xB5/*ZP,X*/: opLDA(CpuAddressingMode.ZPX); break;
      case 0xAD/*ABS*/: opLDA(CpuAddressingMode.ABS); break;
      case 0xBD/*ABS,X+*/: opLDA(CpuAddressingMode.ABSX_); break;
      case 0xB9/*ABS,Y+*/: opLDA(CpuAddressingMode.ABSY_); break;
      case 0xA1/*(IND,X)*/: opLDA(CpuAddressingMode._INDX); break;
      case 0xB1/*(IND),Y+*/: opLDA(CpuAddressingMode._IND_Y_); break;

      /*31.LDX*/
      case 0xA2/*IMM*/: opLDX(CpuAddressingMode.IMM); break;
      case 0xA6/*ZP*/: opLDX(CpuAddressingMode.ZP); break;
      case 0xB6/*ZP,Y*/: opLDX(CpuAddressingMode.ZPY); break;
      case 0xAE/*ABS*/: opLDX(CpuAddressingMode.ABS); break;
      case 0xBE/*ABS,Y+*/: opLDX(CpuAddressingMode.ABSY_); break;

      /*32.LDY*/
      case 0xA0/*IMM*/: opLDY(CpuAddressingMode.IMM); break;
      case 0xA4/*ZP*/: opLDY(CpuAddressingMode.ZP); break;
      case 0xB4/*ZP,X*/: opLDY(CpuAddressingMode.ZPX); break;
      case 0xAC/*ABS*/: opLDY(CpuAddressingMode.ABS); break;
      case 0xBC/*ABS,X+*/: opLDY(CpuAddressingMode.ABSX_); break;

      /*33.LSR*/
      case 0x4A/*ACC*/: opLSR(CpuAddressingMode.ACC); break;
      case 0x46/*ZP*/: opLSR(CpuAddressingMode.ZP); break;
      case 0x56/*ZP,X*/: opLSR(CpuAddressingMode.ZPX); break;
      case 0x4E/*ABS*/: opLSR(CpuAddressingMode.ABS); break;
      case 0x5E/*ABS,X*/: opLSR(CpuAddressingMode.ABSX); break;

      /*34.NOP*/
      case 0xEA/*IMPL*/: break;

      /*35.ORA*/
      case 0x09/*IMM*/: opORA(CpuAddressingMode.IMM); break;
      case 0x05/*ZP*/: opORA(CpuAddressingMode.ZP); break;
      case 0x15/*ZP,X*/: opORA(CpuAddressingMode.ZPX); break;
      case 0x0D/*ABS*/: opORA(CpuAddressingMode.ABS); break;
      case 0x1D/*ABS,X+*/: opORA(CpuAddressingMode.ABSX_); break;
      case 0x19/*ABS,Y+*/: opORA(CpuAddressingMode.ABSY_); break;
      case 0x01/*(IND,X)*/: opORA(CpuAddressingMode._INDX); break;
      case 0x11/*(IND),Y+*/: opORA(CpuAddressingMode._IND_Y_);break;

      /*36.PHA*/
      case 0x48/*IMPL*/: opPHA(); break;

      /*37.PHP*/
      case 0x08/*IMPL*/: opPHP(); break;

      /*38.PLA*/
      case 0x68/*IMPL*/: opPLA(); break;

      /*39.PLP*/
      case 0x28/*IMPL*/: opPLP(); break;

      /*40.ROL*/
      case 0x2A/*ACC*/: opROL(CpuAddressingMode.ACC); break;
      case 0x26/*ZP*/: opROL(CpuAddressingMode.ZP); break;
      case 0x36/*ZP,X*/: opROL(CpuAddressingMode.ZPX); break;
      case 0x2E/*ABS*/: opROL(CpuAddressingMode.ABS); break;
      case 0x3E/*ABS,X*/: opROL(CpuAddressingMode.ABSX); break;

      /*41.ROR*/
      case 0x6A/*ACC*/: opROR(CpuAddressingMode.ACC); break;
      case 0x66/*ZP*/: opROR(CpuAddressingMode.ZP); break;
      case 0x76/*ZP,X*/: opROR(CpuAddressingMode.ZPX); break;
      case 0x6E/*ABS*/: opROR(CpuAddressingMode.ABS); break;
      case 0x7E/*ABS,X*/: opROR(CpuAddressingMode.ABSX); break;

      /*42.RTI*/
      case 0x40/*IMPL*/: opRTI(); break;

      /*43.RTS*/
      case 0x60/*IMPL*/: opRTS(); break;

      /*44.SBC*/
      case 0xE9/*IMM*/: opSBC(CpuAddressingMode.IMM); break;
      case 0xE5/*ZP*/: opSBC(CpuAddressingMode.ZP); break;
      case 0xF5/*ZP,X*/: opSBC(CpuAddressingMode.ZPX); break;
      case 0xED/*ABS*/: opSBC(CpuAddressingMode.ABS); break;
      case 0xFD/*ABS,X+*/: opSBC(CpuAddressingMode.ABSX_); break;
      case 0xF9/*ABS,Y+*/: opSBC(CpuAddressingMode.ABSY_); break;
      case 0xE1/*(IND,X)*/: opSBC(CpuAddressingMode._INDX); break;
      case 0xF1/*(IND),Y+*/: opSBC(CpuAddressingMode._IND_Y_); break;

      /*45.SEC*/
      case 0x38/*IMPL*/: _P = _P | C_FLAG; break;

      /*46.SED*/
      case 0xF8/*IMPL*/: _P = _P | D_FLAG; break;

      /*47.SEI*/
      case 0x78/*IMPL*/: _P = _P | I_FLAG; break;

      /*48.STA*/
      case 0x85/*ZP*/: opSTA(CpuAddressingMode.ZP); break;
      case 0x95/*ZP,X*/: opSTA(CpuAddressingMode.ZPX); break;
      case 0x8D/*ABS*/: opSTA(CpuAddressingMode.ABS); break;
      case 0x9D/*ABS,X*/: opSTA(CpuAddressingMode.ABSX); break;
      case 0x99/*ABS,Y*/: opSTA(CpuAddressingMode.ABSY); break;
      case 0x81/*(IND,X)*/: opSTA(CpuAddressingMode._INDX); break;
      case 0x91/*(IND),Y*/: opSTA(CpuAddressingMode._IND_Y); break;

      /*49.STX*/
      case 0x86/*ZP*/: opSTX(CpuAddressingMode.ZP); break;
      case 0x96/*ZP,Y*/: opSTX(CpuAddressingMode.ZPY); break;
      case 0x8E/*ABS*/: opSTX(CpuAddressingMode.ABS); break;

      /*50.STY*/
      case 0x84/*ZP*/: opSTY(CpuAddressingMode.ZP); break;
      case 0x94/*ZP,X*/: opSTY(CpuAddressingMode.ZPX); break;
      case 0x8C/*ABS*/: opSTY(CpuAddressingMode.ABS); break;

      /*51.TAX*/
      case 0xAA/*IMPL*/:opTAX();break;

      /*52.TAY*/
      case 0xA8/*IMPL*/:opTAY();break;

      /*53.TSX*/
      case 0xBA/*IMPL*/:opTSX();break;

      /*54.TXA*/
      case 0x8A/*IMPL*/:opTXA();break;

      /*55.TXS*/
      case 0x9A/*IMPL*/:opTXS();break;

      /*56.TYA*/
      case 0x98/*IMPL*/:opTYA();break;

//      /*
//       * Unofficial opcodes
//       */

      /*DOP*/
      case 0x04/*ZP*/: opDOP(CpuAddressingMode.ZP); break;
      case 0x14/*ZP,X*/: opDOP(CpuAddressingMode.ZPX); break;
      case 0x34/*ZP,X*/: opDOP(CpuAddressingMode.ZPX); break;
      case 0x44/*ZP*/: opDOP(CpuAddressingMode.ZP); break;
      case 0x54/*ZP,X*/: opDOP(CpuAddressingMode.ZPX); break;
      case 0x64/*ZP*/: opDOP(CpuAddressingMode.ZP); break;
      case 0x74/*ZP,X*/: opDOP(CpuAddressingMode.ZPX); break;
      case 0x80/*IMM*/: opDOP(CpuAddressingMode.IMM); break;
      case 0x82/*IMM*/: opDOP(CpuAddressingMode.IMM); break;
      case 0x89/*IMM*/: opDOP(CpuAddressingMode.IMM); break;
      case 0xC2/*IMM*/: opDOP(CpuAddressingMode.IMM); break;
      case 0xD4/*ZP,X*/: opDOP(CpuAddressingMode.ZPX); break;
      case 0xE2/*IMM*/: opDOP(CpuAddressingMode.IMM); break;
      case 0xF4/*ZP,X*/: opDOP(CpuAddressingMode.ZPX); break;

      /*TOP*/
      case 0x0C/*ABS*/: opTOP(CpuAddressingMode.ABS); break;
      case 0x1C/*ABS,X*/: opTOP(CpuAddressingMode.ABSX_); break;
      case 0x3C/*ABS,X*/: opTOP(CpuAddressingMode.ABSX_); break;
      case 0x5C/*ABS,X*/: opTOP(CpuAddressingMode.ABSX_); break;
      case 0x7C/*ABS,X*/: opTOP(CpuAddressingMode.ABSX_); break;
      case 0xDC/*ABS,X*/: opTOP(CpuAddressingMode.ABSX_); break;
      case 0xFC/*ABS,X*/: opTOP(CpuAddressingMode.ABSX_); break;

      /*LAX*/
      case 0xA7/*ZP*/: opLAX(CpuAddressingMode.ZP); break;
      case 0xB7/*ZP,Y*/: opLAX(CpuAddressingMode.ZPY); break;
      case 0xAF/*ABS*/: opLAX(CpuAddressingMode.ABS); break;
      case 0xBF/*ABS,Y*/: opLAX(CpuAddressingMode.ABSY); break;
      case 0xA3/*(IND,X)*/: opLAX(CpuAddressingMode._INDX); break;
      case 0xB3/*(IND),Y+*/: opLAX(CpuAddressingMode._IND_Y_); break;

      /*AAX*/
      case 0x87/*ZP*/: opAAX(CpuAddressingMode.ZP); break;
      case 0x97/*ZP,Y*/: opAAX(CpuAddressingMode.ZPY); break;
      case 0x83/*(IND,X)*/: opAAX(CpuAddressingMode._INDX); break;
      case 0x8F/*ABS*/: opAAX(CpuAddressingMode.ABS); break;

      /*SBC*/
      case 0xEB/*IMM*/: opSBC(CpuAddressingMode.IMM); break;

      /*DCP*/
      case 0xC7/*ZP*/: opDCP(CpuAddressingMode.ZP); break;
      case 0xD7/*ZP,X*/: opDCP(CpuAddressingMode.ZPX); break;
      case 0xCF/*ABS*/: opDCP(CpuAddressingMode.ABS); break;
      case 0xDF/*ABS,X*/: opDCP(CpuAddressingMode.ABSX); break;
      case 0xDB/*ABS,Y*/: opDCP(CpuAddressingMode.ABSY); break;
      case 0xC3/*(IND,X)*/: opDCP(CpuAddressingMode._INDX); break;
      case 0xD3/*(IND),Y*/: opDCP(CpuAddressingMode._IND_Y); break;

      /*ISC*/
      case 0xE7/*ZP*/: opISC(CpuAddressingMode.ZP); break;
      case 0xF7/*ZPX*/: opISC(CpuAddressingMode.ZPX); break;
      case 0xEF/*ABS*/: opISC(CpuAddressingMode.ABS); break;
      case 0xFF/*ABS,X*/: opISC(CpuAddressingMode.ABSX); break;
      case 0xFB/*ABS,Y*/: opISC(CpuAddressingMode.ABSY); break;
      case 0xE3/*(IND,X)*/: opISC(CpuAddressingMode._INDX); break;
      case 0xF3/*(IND),Y*/: opISC(CpuAddressingMode._IND_Y); break;

      /*SLO*/
      case 0x07/*ZP*/: opSLO(CpuAddressingMode.ZP); break;
      case 0x17/*ZP,X*/: opSLO(CpuAddressingMode.ZPX); break;
      case 0x0F/*ABS*/: opSLO(CpuAddressingMode.ABS); break;
      case 0x1F/*ABS,X*/: opSLO(CpuAddressingMode.ABSX); break;
      case 0x1B/*ABS,Y*/: opSLO(CpuAddressingMode.ABSY); break;
      case 0x03/*(IND,X)*/: opSLO(CpuAddressingMode._INDX); break;
      case 0x13/*(IND),Y*/: opSLO(CpuAddressingMode._IND_Y); break;

      /*RLA*/
      case 0x27/*ZP*/: opRLA(CpuAddressingMode.ZP); break;
      case 0x37/*ZP,X*/: opRLA(CpuAddressingMode.ZPX); break;
      case 0x2F/*ABS*/: opRLA(CpuAddressingMode.ABS); break;
      case 0x3F/*ABS,X*/: opRLA(CpuAddressingMode.ABSX); break;
      case 0x3B/*ABS,Y*/: opRLA(CpuAddressingMode.ABSY); break;
      case 0x23/*(IND,X)*/: opRLA(CpuAddressingMode._INDX); break;
      case 0x33/*(IND),Y*/: opRLA(CpuAddressingMode._IND_Y); break;

      /*SRE*/
      case 0x47/*ZP*/: opSRE(CpuAddressingMode.ZP); break;
      case 0x57/*ZP,X*/: opSRE(CpuAddressingMode.ZPX); break;
      case 0x4F/*ABS*/: opSRE(CpuAddressingMode.ABS); break;
      case 0x5F/*ABS,X*/: opSRE(CpuAddressingMode.ABSX); break;
      case 0x5B/*ABS,Y*/: opSRE(CpuAddressingMode.ABSY); break;
      case 0x43/*(IND,X)*/: opSRE(CpuAddressingMode._INDX); break;
      case 0x53/*(IND),Y*/: opSRE(CpuAddressingMode._IND_Y); break;

      /*RRA*/
      case 0x67/*ZP*/: opRRA(CpuAddressingMode.ZP); break;
      case 0x77/*ZP,X*/: opRRA(CpuAddressingMode.ZPX); break;
      case 0x6F/*ABS*/: opRRA(CpuAddressingMode.ABS); break;
      case 0x7F/*ABS,X*/: opRRA(CpuAddressingMode.ABSX); break;
      case 0x7B/*ABS,Y*/: opRRA(CpuAddressingMode.ABSY); break;
      case 0x63/*(IND,X)*/: opRRA(CpuAddressingMode._INDX); break;
      case 0x73/*(IND),Y*/: opRRA(CpuAddressingMode._IND_Y); break;
      }
   }

   private void opADC(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);

      if ((_P & D_FLAG) == 0 || !_decimalModeSupported) {
         int res = _A + value + ((_P & C_FLAG) != 0 ? 1 : 0);

         _P = _P & ~(N_FLAG | V_FLAG | Z_FLAG | C_FLAG); // Clear flags
         _P = _P | (res & N_FLAG); // N
         _P = _P | ((~(_A ^ value) & (_A ^ (res & 0xFF)) & 0x80) != 0 ? V_FLAG : 0); // V
         _P = _P | ((res & 0xFF) == 0 ? Z_FLAG : 0); // Z
         _P = _P | (res > 0xFF ? C_FLAG : 0); // C

         _A = res & 0xFF;
      } else {
         int carry = (_P & C_FLAG) != 0 ? 1 : 0;
         int AL = (_A & 15) + (value & 15) + carry; // Calculate the lower nybble.
         int AH = (_A >> 4) + (value >> 4) + (AL > 15 ? 1 : 0); // Calculate the upper nybble.

           if (AL > 9) {
              AL += 6; // BCD fix up for lower nybble.
           }

           /* Negative and Overflow flags are set with the same logic than in
              Binary mode, but after fixing the lower nybble. */
           _P = _P | ((AH & 8) != 0 ? N_FLAG : 0); // N
           _P = _P | (((((AH << 4) ^ _A) & 128) != 0) && (((_A ^ value) & 128) == 0) ? V_FLAG : 0); // V
           // Z flag is set just like in Binary mode.
           _P = _P | (_A + value + carry != 0 ? Z_FLAG : 0); // Z

           if (AH > 9) {
              AH += 6; // BCD fix up for upper nybble.
           }
           /* Carry is the only flag set after fixing the result. */
           _P = _P | (AH > 15 ? C_FLAG : 0); // C
           _A = ((AH << 4) | (AL & 15)) & 255;
      }
   }

   private void opAND(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);

      _A = _A & value;
      _P = _P & ~(N_FLAG | Z_FLAG); // Clear flags
      _P = _P | ((_A & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (_A == 0 ? Z_FLAG : 0); // Z
   }

   private void opASL(CpuAddressingMode mode) {
      if (mode == CpuAddressingMode.ACC) {
         _A = opASL(_A);
      } else {
         int address = calculateMemoryAddress(mode);
         int value = readMemory(address);

         int newValue = opASL(value);
         writeMemory(address, newValue);
      }
   }

   private int opASL(int value) {
      int res = (value << 1) & 0xFF;
      _P = _P & ~(N_FLAG | Z_FLAG | C_FLAG); // Clear flags
      _P = _P | ((res & 0x80) > 0 ? N_FLAG : 0); // N
      _P = _P | (res == 0 ? Z_FLAG : 0); // Z
      _P = _P | ((value & 0x80) > 0 ? C_FLAG : 0); // C

      return res;
   }

   private void opBCC(CpuAddressingMode mode) {
      opBranch((_P & C_FLAG) == 0, calculateMemoryAddress(mode));
   }

   private void opBCS(CpuAddressingMode mode) {
      opBranch((_P & C_FLAG) != 0, calculateMemoryAddress(mode));
   }

   private void opBEQ(CpuAddressingMode mode) {
      opBranch((_P & Z_FLAG) != 0, calculateMemoryAddress(mode));
   }

   private void opBMI(CpuAddressingMode mode) {
      opBranch((_P & N_FLAG) != 0, calculateMemoryAddress(mode));
   }

   private void opBNE(CpuAddressingMode mode) {
      opBranch((_P & Z_FLAG) == 0, calculateMemoryAddress(mode));
   }

   private void opBPL(CpuAddressingMode mode) {
      opBranch((_P & N_FLAG) == 0, calculateMemoryAddress(mode));
   }

   private void opBVC(CpuAddressingMode mode) {
      opBranch((_P & V_FLAG) == 0, calculateMemoryAddress(mode));
   }

   private void opBVS(CpuAddressingMode mode) {
      opBranch((_P & V_FLAG) != 0, calculateMemoryAddress(mode));
   }

   private void opBranch(boolean condition, int jumpAddress) {
      if (condition) {
         if (isPageBoundaryCrossed(_PC, jumpAddress)) {
            _opCycles += 2;
         } else {
            _opCycles += 1;
         }

         _PC = jumpAddress;
      }
   }

   private void opBIT(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);

      _P = _P & ~(N_FLAG | V_FLAG | Z_FLAG); // Clear flags
      _P = _P | ((value & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | ((value & 0x40) != 0 ? V_FLAG : 0); // V
      _P = _P | ((_A & value) == 0 ? Z_FLAG : 0); // Z
   }

   private void opBRK() {
      _PC++; // skip next bite (usually it is a NOP or number that is analyzed by the interrupt handler)
      push(_PC >> 8); // push high bits
      push(_PC & 0xFF); // push low bits
      _P = _P | B_FLAG; // B
      push(_P);
      _PC = (readMemory(0xFFFF) << 8)|readMemory(0xFFFE);
   }

   private void opCMP(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);
      opCompare(_A, value);
   }

   private void opCPX(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);
      opCompare(_X, value);
   }

   private void opCPY(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);
      opCompare(_Y, value);
   }

   private void opCompare(int register, int value) {
      int res = (register - value) & 0xFF;
      _P = _P & ~(N_FLAG | Z_FLAG | C_FLAG); // Clear flags
      _P = _P | ((res & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (res == 0 ? Z_FLAG : 0); // Z
      _P = _P | (register >= value ? C_FLAG : 0); // C
   }

   private void opDEC(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);
      writeMemory(address, opDecrease(value));
   }

   private int opDecrease(int value) {
      int res = (value - 1) & 0xFF;
      _P = _P & ~(N_FLAG | Z_FLAG); // Clear flags
      _P = _P | ((res & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (res == 0 ? Z_FLAG : 0); // Z
      return res;
   }

   private void opEOR(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);
      opEOR(value);
   }

   private void opEOR(int value) {
      _A = _A ^ value;
      _P = _P & ~(N_FLAG | Z_FLAG); // Clear flags
      _P = _P | ((_A & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (_A == 0 ? Z_FLAG : 0); // Z
   }

   private void opINC(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);
      writeMemory(address, opIncrease(value));
   }

   private int opIncrease(int value) {
      int res = (value + 1) & 0xFF;
      _P = _P & ~(N_FLAG | Z_FLAG); // Clear flags
      _P = _P | ((res & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (res == 0 ? Z_FLAG : 0); // Z
      return res;
   }

   private void opJMP(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      _PC = address;
   }

   private void opJSR(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);

      _PC--;
      push(_PC >> 8);
      push(_PC & 0xFF);
      _PC = address;
   }

   private void opLDA(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);

      _A = opLoad(value);
   }

   private void opLDX(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);

      _X = opLoad(value);
   }

   private void opLDY(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);

      _Y = opLoad(value);
   }

   private int opLoad(int value) {
      _P = _P & ~(N_FLAG | Z_FLAG); // Clear flags
      _P = _P | ((value & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (value == 0 ? Z_FLAG : 0); // Z
      return value;
   }

   private void opLSR(CpuAddressingMode mode) {
      if (mode == CpuAddressingMode.ACC) {
         _A = opLSR(_A);
      } else {
         int address = calculateMemoryAddress(mode);
         int value = readMemory(address);

         int newValue = opLSR(value);
         writeMemory(address, newValue);
      }
   }

   private int opLSR(int value) {
      int res = 0x7F & (value >> 1);
      _P = _P & ~(N_FLAG | Z_FLAG | C_FLAG); // clear flags
      _P = _P | (res == 0 ? Z_FLAG : 0); // Z
      _P = _P | ((value & 0x01) != 0 ? C_FLAG : 0); // C

      return res;
   }

   private void opORA(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);
      opORA(value);
   }

   private void opORA(int value) {
      _A = _A | value;
      _P = _P & ~(N_FLAG | Z_FLAG); // Clear flags
      _P = _P | ((_A & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (_A == 0 ? Z_FLAG : 0); // Z
   }

   private void opPHA() {
      push(_A);
   }

   private void opPHP() {
      push(_P | B_FLAG);
   }

   private void opPLA() {
      _A = pop();

      _P = _P & ~(N_FLAG | Z_FLAG); // Clear flags
      _P = _P | (_A & N_FLAG); // N
      _P = _P | (_A == 0 ? Z_FLAG : 0); // Z;
   }

   private void opPLP() {
      _P = pop() & ~B_FLAG | R_FLAG;
   }

   private void opROL(CpuAddressingMode mode) {
      if (mode == CpuAddressingMode.ACC) {
         _A = opROL(_A);
      } else {
         int address = calculateMemoryAddress(mode);
         int value = readMemory(address);

         int newValue = opROL(value);
         writeMemory(address, newValue);
      }
   }

   private int opROL(int value) {
      int res = (value << 1) & 0xFF;
      res = res | ((_P & C_FLAG) != 0 ? 1 : 0);
      _P = _P & ~(N_FLAG | Z_FLAG | C_FLAG); // Clear flags

      _P = _P | ((res & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (res == 0 ? Z_FLAG : 0); // Z;
      _P = _P | ((value & 0x80) != 0 ? C_FLAG : 0); // C

      return res;
   }

   private void opROR(CpuAddressingMode mode) {
      if (mode == CpuAddressingMode.ACC) {
         _A = opROR(_A);
      } else {
         int address = calculateMemoryAddress(mode);
         int value = readMemory(address);

         int newValue = opROR(value);
         writeMemory(address, newValue);
      }
   }

   private int opROR(int value) {
      int res = (value >> 1) & 0xFF;
      res = res | ((_P & C_FLAG) != 0 ? 0x80 : 0);
      _P = _P & ~(N_FLAG | Z_FLAG | C_FLAG); // Clear flags
      _P = _P | ((res & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (res == 0 ? Z_FLAG : 0); // Z;
      _P = _P | ((value & 0x01) != 0 ? C_FLAG : 0); // C

      return res;
   }

   private void opRTI() {
      _P = pop() & ~B_FLAG;
      _P = _P | R_FLAG;
      int PCL = pop();
      int PCH = pop();
      _PC = (PCH << 8) | PCL;
   }

   private void opRTS() {
      int PCL = pop();
      int PCH = pop();
      _PC = (PCH << 8) | PCL;
      _PC++;
   }

   private void opSBC(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      int value = readMemory(address);

      if ((_P & D_FLAG) == 0 || !_decimalModeSupported) {
         int res = _A - value - ((_P & C_FLAG) != 0 ? 0 : 1);

         _P = _P & ~(N_FLAG | V_FLAG | Z_FLAG | C_FLAG); // Clear flags
         _P = _P | (res & N_FLAG); // N
         _P = _P | (((_A ^ value) & (_A ^ (0xFF & res)) & 0x80) != 0 ? V_FLAG : 0); // V
         _P = _P | ((res & 0xFF) == 0 ? Z_FLAG : 0); // Z
         _P = _P | ((res & 0x100) != 0 ? 0: C_FLAG); // C
         //_P = _P | (res > 0xFF ? C_FLAG : 0); // C

         _A = 0xFF & res;
      } else {
         int borrow = ((_P & C_FLAG) != 0 ? 0 : 1);
         int AL = (_A & 15) - (value & 15) - borrow; // Calculate the lower nybble.
         if ((AL & 16) != 0) {
            AL -= 6; // BCD fix up for lower nybble.
         }

         int AH = (_A >> 4) - (value >> 4) - (AL & 16); // Calculate the upper nybble.
         if ((AH & 16) != 0) {
            AH -= 6; // BCD fix up for upper nybble.
         }

         _P = _P | (((_A - value - borrow) & 128) != 0 ? N_FLAG : 0); // N
         _P = _P | ((((_A - value - borrow) ^ value) & 128) != 0 && ((_A ^ value) & 128) != 0 ? V_FLAG : 0); // V
         _P = _P | (((_A - value - borrow) & 255) != 0 ? Z_FLAG : 0); // Z
         _P = _P | (((_A - value - borrow) & 256) != 0 ? C_FLAG : 0); // C

         _A = ((AH << 4) | (AL & 15)) & 255;
      }
   }

   private void opSTA(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      writeMemory(address, _A);
   }

   private void opSTX(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      writeMemory(address, _X);
   }

   private void opSTY(CpuAddressingMode mode) {
      int address = calculateMemoryAddress(mode);
      writeMemory(address, _Y);
   }

   private void opTAX() {
      _X = _A;
      transfer(_X);
   }

   private void opTAY() {
      _Y = _A;
      transfer(_Y);
   }

   private void opTSX() {
      _X = _S;
      transfer(_X);
   }

   private void opTXA() {
      _A = _X;
      transfer(_A);
   }

   private void opTXS() {
      _S = _X;
   }

   private void opTYA() {
      _A = _Y;
      transfer(_A);
   }

   private void transfer(int toRegister) {
      _P = _P & ~(N_FLAG | Z_FLAG); // Clear flags
      _P = _P | ((toRegister & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (toRegister == 0 ? Z_FLAG : 0); // Z
   }

   private void opDOP(CpuAddressingMode mode) {
      /*DOP double NOP*/
      readMemory(calculateMemoryAddress(mode));
   }

   private void opTOP(CpuAddressingMode mode) {
      /*TOP triple NOP*/
      readMemory(calculateMemoryAddress(mode));
   }

   private void opLAX(CpuAddressingMode mode) {
      /*LAX Load accumulator and X register with memory
        Status flags: N,Z
       */

      int address = calculateMemoryAddress(mode);
      _A = opLoad(readMemory(address));
      _X = _A;
   }

   private void opAAX(CpuAddressingMode mode) {
      /*AAX (SAX) [AXS] AND X register with accumulator and store result in memory. */
      int address = calculateMemoryAddress(mode);

      int result = _A & _X;
      writeMemory(address, result);
   }

   private void opDCP(CpuAddressingMode mode) {
      /*DCP (DCP) [DCM]*/

      int address = calculateMemoryAddress(mode);

      int value = readMemory(address);
      value = 0xFF & (value - 1);
//      if (value != 0) {
//         value--;
//      } else {
//         value = 0xFF;
//      }

      int valueToTest = _A - value;

      _P = _P & ~(N_FLAG | Z_FLAG | C_FLAG); // Clear flags
      _P = _P | ((valueToTest & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | ((valueToTest == 0) ? Z_FLAG : 0); // Z
      _P = _P | ((valueToTest & 0x100) != 0 ? 0 : C_FLAG); // C

      writeMemory(address, value);
   }

   private void opISC(CpuAddressingMode mode) {
      /*ISC (ISB) [INS] Increase memory by one, then subtract memory from accumulator (with
        borrow). Status flags: N,V,Z,C*/

      int address = calculateMemoryAddress(mode);

      int value = readMemory(address);
      value = 0xFF & (value + 1);

      //int result = _A - value + ((_P & C_FLAG) != 0 ? 1 : 0);
      int result = _A - value - ((_P & C_FLAG) != 0 ? 0 : 1);

      _P = _P & ~(N_FLAG | V_FLAG | Z_FLAG | C_FLAG); // Clear flags
      _P = _P | (((_A ^ value) & (_A ^ (result & 0xFF)) & 0x80) != 0 ? V_FLAG : 0); // V
      _P = _P | ((result & 0x100) != 0 ? 0 : C_FLAG); // C
      _A = result & 0xFF;
      _P = _P | ((_A & 0xFF) == 0 ? Z_FLAG : 0); // Z
      _P = _P | (_A & N_FLAG); // N


//      int res = _A - value - ((_P & C_FLAG) != 0 ? 0 : 1);

//      _P = _P & ~(N_FLAG | V_FLAG | Z_FLAG | C_FLAG); // Clear flags
//      _P = _P | (res & N_FLAG); // N
//      _P = _P | (((_A ^ value) & (_A ^ (0xFF & res)) & 0x80) != 0 ? V_FLAG : 0); // V
//      _P = _P | ((res & 0xFF) == 0 ? Z_FLAG : 0); // Z
//      _P = _P | ((res & 0x100) != 0 ? 0: C_FLAG); // C
//
//      _A = 0xFF & res;

      writeMemory(address, value);
   }

   private void opSLO(CpuAddressingMode mode) {
      /*SLO (SLO) [ASO]
      Shift left one bit in memory, then OR accumulator with memory. =
      Status flags: N,Z,C*/
      int address = calculateMemoryAddress(mode);

      int value = readMemory(address);
      int result = (value << 1) & 0xFF;

      _A = _A | result;

      _P = _P & ~(N_FLAG | Z_FLAG | C_FLAG); // Clear flags
      _P = _P | (_A & N_FLAG); // N
      _P = _P | (_A == 0 ? Z_FLAG : 0); // Z
      _P = _P | ((value & 0x80) != 0 ? C_FLAG : 0); // C

      writeMemory(address, result);
   }

   private void opRLA(CpuAddressingMode mode) {
      /*RLA (RLA) [RLA]
      Rotate one bit left in memory, then AND accumulator with memory. Status
      flags: N,Z,C */
      int address = calculateMemoryAddress(mode);

      int value = readMemory(address);


      int res = (value << 1) & 0xFF;
      res = res | ((_P & C_FLAG) != 0 ? 1 : 0);

      _A = _A & res;

      _P = _P & ~(N_FLAG | Z_FLAG | C_FLAG); // Clear flags
      _P = _P | ((_A & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (_A == 0 ? Z_FLAG : 0); // Z;
      _P = _P | ((value & 0x80) != 0 ? C_FLAG : 0); // C

      writeMemory(address, res);
   }

   private void opSRE(CpuAddressingMode mode) {
      /*SRE (SRE) [LSE]
      Shift right one bit in memory, then EOR accumulator with memory. Status
      flags: N,Z,C*/
      int address = calculateMemoryAddress(mode);

      int value = readMemory(address);

      int res = 0x7F & (value >> 1);
      _A = _A ^ res;

      _P = _P & ~(N_FLAG | Z_FLAG | C_FLAG); // clear flags
      _P = _P | ((_A & 0x80) != 0 ? N_FLAG : 0); // N
      _P = _P | (res == 0 ? Z_FLAG : 0); // Z
      _P = _P | ((value & 0x01) != 0 ? C_FLAG : 0); // C

      writeMemory(address, res);

   }

   private void opRRA(CpuAddressingMode mode) {
      /*RRA (RRA) [RRA]
      Rotate one bit right in memory, then add memory to accumulator (with carry).
      Status flags: N,V,Z,C*/
      int address = calculateMemoryAddress(mode);

      int value1 = readMemory(address);
      int value = (value1 >> 1) & 0xFF;
      value = value | ((_P & C_FLAG) != 0 ? 0x80 : 0);

      int res = _A + value + ((value1 & 0x01) != 0 ? 1 : 0);

      _P = _P & ~(N_FLAG | V_FLAG | Z_FLAG | C_FLAG); // Clear flags
      _P = _P | (res & N_FLAG); // N
      _P = _P | ((~(_A ^ value) & (_A ^ (res & 0xFF)) & 0x80) != 0 ? V_FLAG : 0); // V
      _P = _P | ((res & 0xFF) == 0 ? Z_FLAG : 0); // Z
      _P = _P | (res > 0xFF ? C_FLAG : 0); // C

      _A = res & 0xFF;

      writeMemory(address, value);
   }
}
