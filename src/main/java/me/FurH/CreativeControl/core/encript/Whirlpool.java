package me.FurH.CreativeControl.core.encript;

import java.util.*;

class Whirlpool
{
    public static final int DIGESTBITS = 512;
    public static final int DIGESTBYTES = 64;
    protected static final int R = 10;
    private static final String sbox = "\u1823\uc6e8\u87b8\u014f\u36a6\ud2f5\u796f\u9152\u60bc\u9b8e\ua30c\u7b35\u1de0\ud7c2\u2e4b\ufe57\u1577\u37e5\u9ff0\u4ada\u58c9\u290a\ub1a0\u6b85\ubd5d\u10f4\ucb3e\u0567\ue427\u418b\ua77d\u95d8\ufbee\u7c66\udd17\u479e\uca2d\ubf07\uad5a\u8333\u6302\uaa71\uc819\u49d9\uf2e3\u5b88\u9a26\u32b0\ue90f\ud580\ubecd\u3448\uff7a\u905f\u2068\u1aae\ub454\u9322\u64f1\u7312\u4008\uc3ec\udba1\u8d3d\u9700\ucf2b\u7682\ud61b\ub5af\u6a50\u45f3\u30ef\u3f55\ua2ea\u65ba\u2fc0\ude1c\ufd4d\u9275\u068a\ub2e6\u0e1f\u62d4\ua896\uf9c5\u2559\u8472\u394c\u5e78\u388c\ud1a5\ue261\ub321\u9c1e\u43c7\ufc04\u5199\u6d0d\ufadf\u7e24\u3bab\uce11\u8f4e\ub7eb\u3c81\u94f7\ub913\u2cd3\ue76e\uc403\u5644\u7fa9\u2abb\uc153\udc0b\u9d6c\u3174\uf646\uac89\u14e1\u163a\u6909\u70b6\ud0ed\ucc42\u98a4\u285c\uf886";
    private static long[][] C;
    private static long[] rc;
    protected byte[] bitLength;
    protected byte[] buffer;
    protected int bufferBits;
    protected int bufferPos;
    protected long[] hash;
    protected long[] K;
    protected long[] L;
    protected long[] block;
    protected long[] state;
    
    Whirlpool() {
        super();
        this.bitLength = new byte[32];
        this.buffer = new byte[64];
        this.bufferBits = 0;
        this.bufferPos = 0;
        this.hash = new long[8];
        this.K = new long[8];
        this.L = new long[8];
        this.block = new long[8];
        this.state = new long[8];
    }
    
    protected void processBuffer() {
        for (int i = 0, j = 0; i < 8; ++i, j += 8) {
            this.block[i] = (this.buffer[j] << 56 ^ (this.buffer[j + 1] & 0xFFL) << 48 ^ (this.buffer[j + 2] & 0xFFL) << 40 ^ (this.buffer[j + 3] & 0xFFL) << 32 ^ (this.buffer[j + 4] & 0xFFL) << 24 ^ (this.buffer[j + 5] & 0xFFL) << 16 ^ (this.buffer[j + 6] & 0xFFL) << 8 ^ (this.buffer[j + 7] & 0xFFL));
        }
        for (int i = 0; i < 8; ++i) {
            this.state[i] = (this.block[i] ^ (this.K[i] = this.hash[i]));
        }
        for (int r = 1; r <= 10; ++r) {
            for (int k = 0; k < 8; ++k) {
                this.L[k] = 0L;
                for (int t = 0, s = 56; t < 8; ++t, s -= 8) {
                    final long[] l = this.L;
                    final int n = k;
                    l[n] ^= Whirlpool.C[t][(int)(this.K[k - t & 0x7] >>> s) & 0xFF];
                }
            }
            for (int k = 0; k < 8; ++k) {
                this.K[k] = this.L[k];
            }
            final long[] m = this.K;
            final int n2 = 0;
            m[n2] ^= Whirlpool.rc[r];
            for (int k = 0; k < 8; ++k) {
                this.L[k] = this.K[k];
                for (int t = 0, s = 56; t < 8; ++t, s -= 8) {
                    final long[] l2 = this.L;
                    final int n3 = k;
                    l2[n3] ^= Whirlpool.C[t][(int)(this.state[k - t & 0x7] >>> s) & 0xFF];
                }
            }
            for (int k = 0; k < 8; ++k) {
                this.state[k] = this.L[k];
            }
        }
        for (int i = 0; i < 8; ++i) {
            final long[] hash = this.hash;
            final int n4 = i;
            hash[n4] ^= (this.state[i] ^ this.block[i]);
        }
    }
    
    public void NESSIEinit() {
        Arrays.fill(this.bitLength, (byte)0);
        final boolean b = false;
        this.bufferPos = (b ? 1 : 0);
        this.bufferBits = (b ? 1 : 0);
        this.buffer[0] = 0;
        Arrays.fill(this.hash, 0L);
    }
    
    public void NESSIEadd(final byte[] source, long sourceBits) {
        int sourcePos = 0;
        final int sourceGap = 8 - ((int)sourceBits & 0x7) & 0x7;
        final int bufferRem = this.bufferBits & 0x7;
        long value = sourceBits;
        int i = 31;
        int carry = 0;
        while (i >= 0) {
            carry += (this.bitLength[i] & 0xFF) + ((int)value & 0xFF);
            this.bitLength[i] = (byte)carry;
            carry >>>= 8;
            value >>>= 8;
            --i;
        }
        while (sourceBits > 8L) {
            final int b = (source[sourcePos] << sourceGap & 0xFF) | (source[sourcePos + 1] & 0xFF) >>> 8 - sourceGap;
            if (b < 0 || b >= 256) {
                throw new RuntimeException("LOGIC ERROR");
            }
            final byte[] buffer = this.buffer;
            final int n = this.bufferPos++;
            buffer[n] |= (byte)(b >>> bufferRem);
            this.bufferBits += 8 - bufferRem;
            if (this.bufferBits == 512) {
                this.processBuffer();
                final boolean b2 = false;
                this.bufferPos = (b2 ? 1 : 0);
                this.bufferBits = (b2 ? 1 : 0);
            }
            this.buffer[this.bufferPos] = (byte)(b << 8 - bufferRem & 0xFF);
            this.bufferBits += bufferRem;
            sourceBits -= 8L;
            ++sourcePos;
        }
        int b;
        if (sourceBits > 0L) {
            b = (source[sourcePos] << sourceGap & 0xFF);
            final byte[] buffer2 = this.buffer;
            final int bufferPos = this.bufferPos;
            buffer2[bufferPos] |= (byte)(b >>> bufferRem);
        }
        else {
            b = 0;
        }
        if (bufferRem + sourceBits < 8L) {
            this.bufferBits += (int)sourceBits;
        }
        else {
            ++this.bufferPos;
            this.bufferBits += 8 - bufferRem;
            sourceBits -= 8 - bufferRem;
            if (this.bufferBits == 512) {
                this.processBuffer();
                final boolean b3 = false;
                this.bufferPos = (b3 ? 1 : 0);
                this.bufferBits = (b3 ? 1 : 0);
            }
            this.buffer[this.bufferPos] = (byte)(b << 8 - bufferRem & 0xFF);
            this.bufferBits += (int)sourceBits;
        }
    }
    
    public void NESSIEfinalize(final byte[] digest) {
        final byte[] buffer = this.buffer;
        final int bufferPos = this.bufferPos;
        buffer[bufferPos] |= (byte)(128 >>> (this.bufferBits & 0x7));
        ++this.bufferPos;
        if (this.bufferPos > 32) {
            while (this.bufferPos < 64) {
                this.buffer[this.bufferPos++] = 0;
            }
            this.processBuffer();
            this.bufferPos = 0;
        }
        while (this.bufferPos < 32) {
            this.buffer[this.bufferPos++] = 0;
        }
        System.arraycopy(this.bitLength, 0, this.buffer, 32, 32);
        this.processBuffer();
        for (int i = 0, j = 0; i < 8; ++i, j += 8) {
            final long h = this.hash[i];
            digest[j] = (byte)(h >>> 56);
            digest[j + 1] = (byte)(h >>> 48);
            digest[j + 2] = (byte)(h >>> 40);
            digest[j + 3] = (byte)(h >>> 32);
            digest[j + 4] = (byte)(h >>> 24);
            digest[j + 5] = (byte)(h >>> 16);
            digest[j + 6] = (byte)(h >>> 8);
            digest[j + 7] = (byte)h;
        }
    }
    
    public void NESSIEadd(final String source) {
        if (source.length() > 0) {
            final byte[] data = new byte[source.length()];
            for (int i = 0; i < source.length(); ++i) {
                data[i] = (byte)source.charAt(i);
            }
            this.NESSIEadd(data, 8 * data.length);
        }
    }
    
    public static String display(final byte[] array) {
        final char[] val = new char[2 * array.length];
        final String hex = "0123456789abcdef";
        for (int i = 0; i < array.length; ++i) {
            final int b = array[i] & 0xFF;
            val[2 * i] = hex.charAt(b >>> 4);
            val[2 * i + 1] = hex.charAt(b & 0xF);
        }
        return String.valueOf(val);
    }
    
    static {
        Whirlpool.C = new long[8][256];
        Whirlpool.rc = new long[11];
        for (int x = 0; x < 256; ++x) {
            final char c = "\u1823\uc6e8\u87b8\u014f\u36a6\ud2f5\u796f\u9152\u60bc\u9b8e\ua30c\u7b35\u1de0\ud7c2\u2e4b\ufe57\u1577\u37e5\u9ff0\u4ada\u58c9\u290a\ub1a0\u6b85\ubd5d\u10f4\ucb3e\u0567\ue427\u418b\ua77d\u95d8\ufbee\u7c66\udd17\u479e\uca2d\ubf07\uad5a\u8333\u6302\uaa71\uc819\u49d9\uf2e3\u5b88\u9a26\u32b0\ue90f\ud580\ubecd\u3448\uff7a\u905f\u2068\u1aae\ub454\u9322\u64f1\u7312\u4008\uc3ec\udba1\u8d3d\u9700\ucf2b\u7682\ud61b\ub5af\u6a50\u45f3\u30ef\u3f55\ua2ea\u65ba\u2fc0\ude1c\ufd4d\u9275\u068a\ub2e6\u0e1f\u62d4\ua896\uf9c5\u2559\u8472\u394c\u5e78\u388c\ud1a5\ue261\ub321\u9c1e\u43c7\ufc04\u5199\u6d0d\ufadf\u7e24\u3bab\uce11\u8f4e\ub7eb\u3c81\u94f7\ub913\u2cd3\ue76e\uc403\u5644\u7fa9\u2abb\uc153\udc0b\u9d6c\u3174\uf646\uac89\u14e1\u163a\u6909\u70b6\ud0ed\ucc42\u98a4\u285c\uf886".charAt(x / 2);
            final long v1 = ((x & 0x1) == 0x0) ? (c >>> 8) : ((long)(c & '\u00ff'));
            long v2 = v1 << 1;
            if (v2 >= 256L) {
                v2 ^= 0x11DL;
            }
            long v3 = v2 << 1;
            if (v3 >= 256L) {
                v3 ^= 0x11DL;
            }
            final long v4 = v3 ^ v1;
            long v5 = v3 << 1;
            if (v5 >= 256L) {
                v5 ^= 0x11DL;
            }
            final long v6 = v5 ^ v1;
            Whirlpool.C[0][x] = (v1 << 56 | v1 << 48 | v3 << 40 | v1 << 32 | v5 << 24 | v4 << 16 | v2 << 8 | v6);
            for (int t = 1; t < 8; ++t) {
                Whirlpool.C[t][x] = (Whirlpool.C[t - 1][x] >>> 8 | Whirlpool.C[t - 1][x] << 56);
            }
        }
        Whirlpool.rc[0] = 0L;
        for (int r = 1; r <= 10; ++r) {
            final int i = 8 * (r - 1);
            Whirlpool.rc[r] = ((Whirlpool.C[0][i] & 0xFF00000000000000L) ^ (Whirlpool.C[1][i + 1] & 0xFF000000000000L) ^ (Whirlpool.C[2][i + 2] & 0xFF0000000000L) ^ (Whirlpool.C[3][i + 3] & 0xFF00000000L) ^ (Whirlpool.C[4][i + 4] & 0xFF000000L) ^ (Whirlpool.C[5][i + 5] & 0xFF0000L) ^ (Whirlpool.C[6][i + 6] & 0xFF00L) ^ (Whirlpool.C[7][i + 7] & 0xFFL));
        }
    }
}
