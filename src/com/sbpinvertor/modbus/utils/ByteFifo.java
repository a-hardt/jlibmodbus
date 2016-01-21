package com.sbpinvertor.modbus.utils;

import com.sbpinvertor.utils.CRC16;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Copyright (c) 2015-2016 JSC "Zavod "Invertor"
 * [http://www.sbp-invertor.ru]
 * <p/>
 * This file is part of JLibModbus.
 * <p/>
 * JLibModbus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Authors: Vladislav Y. Kochedykov, software engineer.
 * email: vladislav.kochedykov@gmail.com
 */
final public class ByteFifo {

    final private ByteArrayOutputStream baos;
    final private ByteArrayInputStream bais;
    final private int capacity;
    private int crc;


    public ByteFifo(int size) {
        baos = new ByteArrayOutputStream(size);
        bais = new ByteArrayInputStream(baos.getByteBuffer());
        capacity = size;
        clear();
    }

    public int getCrc() {
        return crc;
    }

    public void clear() {
        baos.reset();
        bais.reset();
        crc = CRC16.INITIAL_VALUE;
    }

    public byte[] getByteBuffer() {
        return baos.getByteBuffer();
    }

    public byte[] toByteArray() {
        return baos.toByteArray();
    }

    public int size() {
        return baos.size();
    }

    public int read() {
        return available() > 0 ? bais.read() : -1;
    }

    public int read(byte[] b) throws IOException {
        return (available() < b.length) ? 0 : bais.read(b);
    }

    public int read(byte[] b, int offset, int length) throws IOException {
        return (available() < b.length) ? 0 : bais.read(b, offset, length);
    }

    public int readShortBE() {
        int h = read();
        int l = read();
        if (-1 == h || -1 == l)
            return -1;
        return DataUtils.toShort(h, l);
    }

    public int readShortLE() {
        int l = read();
        int h = read();
        if (-1 == h || -1 == l)
            return -1;
        return DataUtils.toShort(h, l);
    }

    public void writeShortBE(int s) {
        write(DataUtils.byteHigh(s));
        write(DataUtils.byteLow(s));
    }

    public void writeShortLE(int s) {
        write(DataUtils.byteLow(s));
        write(DataUtils.byteHigh(s));
    }

    public void write(int b) {
        if (size() < capacity) {
            baos.write(b);
            crc = CRC16.calc(crc, (byte) (b & 0xFF));
        }
    }

    public void write(byte[] b) throws IOException {
        int available = (capacity - size());
        if (available > 0) {
            int count = b.length < available ? b.length : available;
            baos.write(b, 0, count);
            crc = CRC16.calc(crc, b, 0, count);
        }
    }

    public int available() {
        return size() - (capacity - bais.available());
    }

    public void writeCRC() {
        writeShortLE(getCrc());
    }

    private class ByteArrayOutputStream extends java.io.ByteArrayOutputStream {

        public ByteArrayOutputStream(int size) {
            super(size);
        }

        public byte[] getByteBuffer() {
            return buf;
        }
    }
}