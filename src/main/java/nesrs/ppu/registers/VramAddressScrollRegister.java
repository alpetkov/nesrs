package nesrs.ppu.registers;

public class VramAddressScrollRegister {
	// VRAM Scroll Register (W2)
	// VRAM Address Register (W2)
	public int _address; // VRAM address entered.
	public int _lastValue; // Stores the last read VRAM value
	public int _tempAddress; // VRAM temp address. VRAM is entered in two steps. Also can be interpret as 0yyy NNYY YYYX XXXX (fineY, name table, tileY, tileX)
	public boolean _toggle; // VRAM address step toggle
	public int _bgFineX; // xxx (Background fineX)

	public int getBackgroundFineX() {
		return _bgFineX;
	}

	public int getBackgroundFineY() {
		// temp 0yyy NNYY YYYX XXXX
		return (_address >> 12) & 0x7;
	}

	public void setBackgroundFineY(int fineY) {
		// temp 0yyy NNYY YYYX XXXX
		_address &= 0x8FFF;
		_address |= fineY << 12;
	}

	public int getBackgroundTileX() {
		// temp 0yyy NNYY YYYX XXXX
		return _address & 0x1F;
	}

	public void setBackgroundTileX(int tileX) {
		// temp 0yyy NNYY YYYX XXXX
		_address &= 0xFFE0;
		_address |= tileX;
	}

	public int getBackgroundTileY() {
		// temp 0yyy NNYY YYYX XXXX
		return (_address >> 5) & 0x1F;
	}

	public void setBackgroundTileY(int tileY) {
		// temp 0yyy NNYY YYYX XXXX
		_address &= 0xFC1F;
		_address |= tileY << 5;
	}

	public int getNameTableIndex() {
		return (_address >> 10) & 0x3;
	}

	public void setNameTableIndex(int nameTableIndex) {
		_address &= 0xF3FF;
		_address |= nameTableIndex << 10;
	}
}
