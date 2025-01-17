package shared;


public class Config {
    //TODO: Fix buffer submitting before actual data can be read due to non-growing size, actual size 128
    public static final int   MIN_TRANS_BUFFER_SIZE = 5120;
    public static final byte  CONSOLE_PADDING_SIZE  = 2;
    public static final short INPUT_REFRESH_MILLIS  = 300;

    private Config() {}

}