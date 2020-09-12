package dev.hevav.tchubbot.voice;

//https://stackoverflow.com/questions/34652746/convert-pcm-raw-bytes-to-wav-bytes/54834583#54834583
public class WAVHeader {

    private static final int CHUNK_SIZE = 36;
    private static final int BIT_RATE_16 = 16;
    private static final int MONO = 1;
    private static final int HEADER_SIZE = 44;

    public static byte[] build(int sampleRate,int inputStreamLength) {

        byte[] header = new byte[HEADER_SIZE];
        int channel = MONO;
        int format = BIT_RATE_16;

        long totalDataLen = (long) inputStreamLength + CHUNK_SIZE;
        long bitrate = sampleRate * channel * format;

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = (byte) format;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channel;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) ((bitrate / 8) & 0xff);
        header[29] = (byte) (((bitrate / 8) >> 8) & 0xff);
        header[30] = (byte) (((bitrate / 8) >> 16) & 0xff);
        header[31] = (byte) (((bitrate / 8) >> 24) & 0xff);
        header[32] = (byte) ((channel * format) / 8);
        header[33] = 0;
        header[34] = 16;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) ((long) inputStreamLength & 0xff);
        header[41] = (byte) (((long) inputStreamLength >> 8) & 0xff);
        header[42] = (byte) (((long) inputStreamLength >> 16) & 0xff);
        header[43] = (byte) (((long) inputStreamLength >> 24) & 0xff);
        return header;
    }
}