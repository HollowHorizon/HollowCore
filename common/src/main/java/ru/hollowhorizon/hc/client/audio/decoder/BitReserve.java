package ru.hollowhorizon.hc.client.audio.decoder;

final class BitReserve {
    /**
     * Size of the internal buffer to store the reserved bits. Must be a power of 2. And x8, as each bit is stored as a single
     * entry.
     */
    private static final int BUFSIZE = 4096 * 8;

    /**
     * Mask that can be used to quickly implement the modulus operation on BUFSIZE.
     */
    private static final int BUFSIZE_MASK = BUFSIZE - 1;

    private int offset, totbit, buf_byte_idx;
    private final int[] buf = new int[BUFSIZE];

    BitReserve () {
        offset = 0;
        totbit = 0;
        buf_byte_idx = 0;
    }

    /**
     * Return totbit Field.
     */
    public int hsstell () {
        return totbit;
    }

    /**
     * Read a number bits from the bit stream.
     * @param N the number of
     */
    public int hgetbits (int N) {
        totbit += N;

        int val = 0;

        int pos = buf_byte_idx;
        if (pos + N < BUFSIZE)
            while (N-- > 0) {
                val <<= 1;
                val |= buf[pos++] != 0 ? 1 : 0;
            }
        else
            while (N-- > 0) {
                val <<= 1;
                val |= buf[pos] != 0 ? 1 : 0;
                pos = pos + 1 & BUFSIZE_MASK;
            }
        buf_byte_idx = pos;
        return val;
    }

    /*
     * public int hget1bit_old() { int val; totbit++; if (buf_bit_idx == 0) { buf_bit_idx = 8; buf_byte_idx++; } // BUFSIZE = 4096
     * = 2^12, so // buf_byte_idx%BUFSIZE == buf_byte_idx & 0xfff val = buf[buf_byte_idx & BUFSIZE_MASK] & putmask[buf_bit_idx];
     * buf_bit_idx--; val = val >>> buf_bit_idx; return val; }
     */
    /**
     * Returns next bit from reserve.
     * @returns 0 if next bit is reset, or 1 if next bit is set.
     */
    public int hget1bit () {
        totbit++;
        final int val = buf[buf_byte_idx];
        buf_byte_idx = buf_byte_idx + 1 & BUFSIZE_MASK;
        return val;
    }

    /*
     * public int readBits(int[] out, int len) { if (buf_bit_idx == 0) { buf_bit_idx = 8; buf_byte_idx++; current =
     * buf[buf_byte_idx & BUFSIZE_MASK]; }
     *
     *
     *
     * // save total number of bits returned len = buf_bit_idx; buf_bit_idx = 0;
     *
     * int b = current; int count = len-1;
     *
     * while (count >= 0) { out[count--] = (b & 0x1); b >>>= 1; }
     *
     * totbit += len; return len; }
     */

    /**
     * Write 8 bits into the bit stream.
     */
    public void hputbuf (int val) {
        int ofs = offset;
        buf[ofs++] = val & 0x80;
        buf[ofs++] = val & 0x40;
        buf[ofs++] = val & 0x20;
        buf[ofs++] = val & 0x10;
        buf[ofs++] = val & 0x08;
        buf[ofs++] = val & 0x04;
        buf[ofs++] = val & 0x02;
        buf[ofs++] = val & 0x01;

        if (ofs == BUFSIZE)
            offset = 0;
        else
            offset = ofs;

    }

    /**
     * Rewind N bits in Stream.
     */
    public void rewindNbits (int N) {
        totbit -= N;
        buf_byte_idx -= N;
        if (buf_byte_idx < 0) buf_byte_idx += BUFSIZE;
    }

    /**
     * Rewind N bytes in Stream.
     */
    public void rewindNbytes (int N) {
        final int bits = N << 3;
        totbit -= bits;
        buf_byte_idx -= bits;
        if (buf_byte_idx < 0) buf_byte_idx += BUFSIZE;
    }
}
