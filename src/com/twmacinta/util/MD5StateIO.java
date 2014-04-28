package com.twmacinta.util;

/**
 * Fast implementation of RSA's MD5 hash generator in Java JDK Beta-2 or higher<br>
 * Originally written by Santeri Paavolainen, Helsinki Finland 1996 <br>
 * (c) Santeri Paavolainen, Helsinki Finland 1996 <br>
 * Some changes Copyright (c) 2002 Timothy W Macinta <br>
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 * <p>
 * See http://www.twmacinta.com/myjava/fast_md5.php for more information on this
 * file.
 * <p>
 * This class does serialization and deserialization of MD5State object.
 * 
 * @author Peter Stibrany <pstibrany@gmail.com>
 **/

public class MD5StateIO {
    /**
     * @return deserialized MD5State from supplied bytes (created by
     *         {@link #serialize(MD5State))} method).
     */
    public static MD5State deserialize(byte[] data) {
        if (data.length != 88) {
            throw new IllegalArgumentException("Illegal serialized data length");
        }
        MD5State s = new MD5State();
        s.state[0] = readInt(data, 0);
        s.state[1] = readInt(data, 4);
        s.state[2] = readInt(data, 8);
        s.state[3] = readInt(data, 12);

        s.count = readLong(data, 16);

        for (int i = 0; i < s.buffer.length; i++) {
            s.buffer[i] = data[24 + i];
        }
        return s;
    }

    /**
     * @return representation of this state serialized into 88-bytes array.
     *         WARNING: don't change this!
     */
    public static byte[] serialize(MD5State state) {
        byte[] result = new byte[88];
        writeInt(state.state[0], result, 0);
        writeInt(state.state[1], result, 4);
        writeInt(state.state[2], result, 8);
        writeInt(state.state[3], result, 12);
        writeLong(state.count, result, 16);
        for (int i = 0; i < state.buffer.length; i++) {
            result[24 + i] = state.buffer[i];
        }

        return result;
    }

    private static void writeInt(int val, byte[] arr, int off) {
        arr[off] = (byte) (val >> 24);
        arr[off + 1] = (byte) (val >> 16);
        arr[off + 2] = (byte) (val >> 8);
        arr[off + 3] = (byte) val;
    }

    private static int readInt(byte[] arr, int off) {
        return arr[off] << 24 | (arr[off + 1] & 0xFF) << 16
                | (arr[off + 2] & 0xFF) << 8 | (arr[off + 3] & 0xFF);
    }

    private static void writeLong(long val, byte[] arr, int off) {
        arr[off] = (byte) (val >> 56);
        arr[off + 1] = (byte) (val >> 48);
        arr[off + 2] = (byte) (val >> 40);
        arr[off + 3] = (byte) (val >> 32);
        arr[off + 4] = (byte) (val >> 24);
        arr[off + 5] = (byte) (val >> 16);
        arr[off + 6] = (byte) (val >> 8);
        arr[off + 7] = (byte) val;
    }

    private static long readLong(byte[] arr, int off) {
        return (arr[off] & 0xFFL) << 56 | (arr[off + 1] & 0xFFL) << 48
                | (arr[off + 2] & 0xFFL) << 40 | (arr[off + 3] & 0xFFL) << 32
                | (arr[off + 4] & 0xFFL) << 24 | (arr[off + 5] & 0xFFL) << 16
                | (arr[off + 6] & 0xFFL) << 8 | (arr[off + 7] & 0xFFL);
    }
}
