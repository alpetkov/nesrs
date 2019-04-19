package nesrs.ppu;

public class SpriteMemory {
	private int[] _sprRam = new int[0x100]; // Sprite RAM (256b) (64 sprites)
	private int[] _sprTempMemory = new int[0x20]; // Sprite temporary Memory (32b) (8 sprites)

	private int _sprTempMemoryWriteAddress = 0;
	private boolean _isSprTempMemoryWriteEnabled = true;

	public int readMemory(int offset) {
		return _sprRam[offset];
	}

	public void writeMemory(int offset, int value) {
		_sprRam[offset] = value;
	}

	public int readTempMemory(int address) {
		return _sprTempMemory[address];
	}

	public void writeTempMemory(int address, int value) {
		_sprTempMemory[address] = value;
	}

	public int getTempMemoryWriteAddress() {
		return _sprTempMemoryWriteAddress;
	}

	public void setTempMemoryWriteAddress(int address) {
		_sprTempMemoryWriteAddress = address;
	}

	public boolean isTempMemoryWriteEnabled() {
		return _isSprTempMemoryWriteEnabled;
	}

	public void setTempMemoryWriteEnabled(boolean isWriteEnabled) {
		_isSprTempMemoryWriteEnabled = isWriteEnabled;
	}
}
