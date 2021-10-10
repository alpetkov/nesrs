package nesrs.apu.devices;

public class Mixer {
    private final int[] SQUARE_OUT_TABLE = new int[31];
    private final int[] TND_OUT_TABLE = new int[203];

    private int unsignedMaxValue;
    private boolean signed;

    public Mixer(int unsignedMaxValue, boolean signed) {
        SQUARE_OUT_TABLE[0] = 0;
        for (int i = 1; i < 31; i++) {
            SQUARE_OUT_TABLE[i] = (int)((95.52 / (8128.0 / i + 100)) * unsignedMaxValue);
        }

        TND_OUT_TABLE[0] = 0;
        for (int i = 1; i < 203; i++) {
            TND_OUT_TABLE[i] = (int)((163.67 / (24329.0 / i + 100)) * unsignedMaxValue);
        }

        this.unsignedMaxValue = unsignedMaxValue;
        this.signed = signed;
    }

    public int mix(int rec1Dac, int rec2Dac, int triDac, int randomDac, int dmcDac) {
//      rec1Dac = 0;
//      rec2Dac = 0;
//      triDac = 0;
//      randomDac = 0;
//      dmcDac = 0;

        int routAudio = SQUARE_OUT_TABLE[rec1Dac + rec2Dac];
        int coutAudio = TND_OUT_TABLE[3 * triDac + 2 * randomDac + dmcDac];
        int sample = routAudio + coutAudio;
        if (signed) {
            sample = sample - (unsignedMaxValue / 2);
        }

        return sample;
    }
}
