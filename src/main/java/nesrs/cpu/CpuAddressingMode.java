package nesrs.cpu;

public enum CpuAddressingMode {
   // 1. Accumulator addressing - ACC
   ACC,

   // 2. Implied addressing - IMPL
   IMPL,

   // 3. Immediate addressing - IMM
   IMM,

   // 4. Absolute addressing - ABS
   ABS,

   // 5. Zero page addressing - ZP
   ZP,

   // 6. Indexed zero page addressing with register X - ZP,X
   ZPX,

   // 7. Indexed zero page addressing with register Y - ZP,Y
   ZPY,

   // 8. Indexed absolute addressing with register X - ABS,X
   ABSX, // countAdditionalCycleOnPageBoundaryCrossed - FALSE
   ABSX_, // countAdditionalCycleOnPageBoundaryCrossed - TRUE

   // 9. Indexed absolute addressing with register Y - ABS,Y
   ABSY, // countAdditionalCycleOnPageBoundaryCrossed - FALSE
   ABSY_, // countAdditionalCycleOnPageBoundaryCrossed - TRUE

   // 10. Relative addressing - REL
   REL,

   // 11. Indexed indirect (pre-indexed) addressing with register X - (IND,X)
   _INDX,

   // 12. Indirect indexed (post-indexed) addressing with register Y - (IND),Y
   _IND_Y, // countAdditionalCycleOnPageBoundaryCrossed - FALSE
   _IND_Y_, // countAdditionalCycleOnPageBoundaryCrossed - TRUE

   // 13. Absolute indirect addressing - IND
   IND, // isPageWrappingAllowed - TRUE
   IND_ // isPageWrappingAllowed - FALSE
}
