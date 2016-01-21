package com.sbpinvertor.modbus.net;

import com.sbpinvertor.conn.SerialPort;
import com.sbpinvertor.conn.SerialPortException;
import com.sbpinvertor.modbus.Modbus;
import com.sbpinvertor.modbus.exception.ModbusTransportException;
import com.sbpinvertor.modbus.utils.ByteFifo;

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
public class ModbusTransportRTU extends ModbusTransport {
    final private SerialPort port;

    public ModbusTransportRTU(SerialPort port) throws SerialPortException {
        this.port = port;
        port.open();
    }

    @Override
    synchronized public void send(ByteFifo pdu) throws ModbusTransportException {
        try {
            port.purgeRx();
            pdu.writeCRC();
            port.write(pdu.toByteArray());
        } catch (Exception e) {
            throw new ModbusTransportException(e);
        }
    }

    @Override
    synchronized public void recv(ByteFifo pdu) throws ModbusTransportException {
        try {
            byte[] buffer = port.readBytes(Modbus.MAX_RESPONSE_TIMEOUT);
            if (buffer != null)
                pdu.write(buffer);
        } catch (Exception e) {
            throw new ModbusTransportException(e);
        }
        if (pdu.getCrc() != 0) {
            throw new ModbusTransportException("CRC check failed");
        }
    }
}