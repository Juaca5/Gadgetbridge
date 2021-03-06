/*  Copyright (C) 2017 Andreas Shimokawa

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.amazfitbip;

import java.util.HashMap;
import java.util.Map;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiFirmwareInfo;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiFirmwareType;
import nodomain.freeyourgadget.gadgetbridge.util.ArrayUtils;

public class AmazfitBipFirmwareInfo extends HuamiFirmwareInfo {
    // total crap maybe
    private static final byte[] GPS_HEADER = new byte[]{
            (byte) 0xcb, 0x51, (byte) 0xc1, 0x30, 0x41, (byte) 0x9e, 0x5e, (byte) 0xd3,
            0x51, 0x35, (byte) 0xdf, 0x66, (byte) 0xed, (byte) 0xd9, 0x5f, (byte) 0xa7
    };
    private static final byte[] GPS_HEADER2 = new byte[]{
            0x10, 0x50, 0x26, 0x76, (byte) 0x8f, 0x4a, (byte) 0xa1, 0x49,
            (byte) 0xa7, 0x26, (byte) 0xd0, (byte) 0xe6, 0x4a, 0x21, (byte) 0x88, (byte) 0xd4
    };

    // guessed - at least it is the same across versions from 0.0.7.x to 0.0.9.x
    // and different from other devices
    private static final byte[] FW_HEADER = new byte[]{
            0x68, 0x46, 0x70, 0x47, 0x68, 0x46, 0x70, 0x47,
            0x68, 0x46, 0x70, 0x47, 0x68, 0x46, 0x70, 0x47
    };

    // guessed - this is true for 0.1.0.11
    private static final byte[] FW_HEADER_NEW = new byte[]{
            0x60, (byte) 0xeb, 0x03, 0x0c, 0x70, 0x46, 0x31, 0x46,
            0x3a, 0x46, 0x63, 0x46, (byte) 0xbd, (byte) 0xe8, (byte) 0xf0, (byte) 0x81
    };

    private static final int FW_HEADER_OFFSET = 0x9330;

    private static final byte[] GPS_ALMANAC_HEADER = new byte[]{ // probably wrong
            (byte) 0xa0, (byte) 0x80, 0x08, 0x00, (byte) 0x8b, 0x07
    };

    private static final byte[] GPS_CEP_HEADER = new byte[]{ // probably wrong
            0x2a, 0x12, (byte) 0xa0, 0x02
    };

    private static Map<Integer, String> crcToVersion = new HashMap<>();
    static {
        // firmware
        crcToVersion.put(25257, "0.0.8.74");
        crcToVersion.put(57724, "0.0.8.88");
        crcToVersion.put(27668, "0.0.8.96");
        crcToVersion.put(60173, "0.0.8.97");
        crcToVersion.put(3462, "0.0.8.98");
        crcToVersion.put(55420, "0.0.9.14");
        crcToVersion.put(39465, "0.0.9.26");
        crcToVersion.put(27394, "0.0.9.40");
        crcToVersion.put(24736, "0.0.9.49");
        crcToVersion.put(49555, "0.0.9.59");
        crcToVersion.put(26714, "0.1.0.11");

        // resources
        crcToVersion.put(12586, "RES 0.0.8.74");
        crcToVersion.put(34068, "RES 0.0.8.88");
        crcToVersion.put(59839, "RES 0.0.8.96-98");
        crcToVersion.put(50401, "RES 0.0.9.14-26");
        crcToVersion.put(22051, "RES 0.0.9.40");
        crcToVersion.put(46233, "RES 0.0.9.49-0.1.0.11");

        // gps
        crcToVersion.put(61520, "GPS 9367,8f79a91,0,0,");
        crcToVersion.put(8784, "GPS 9565,dfbd8fa,0,0,");
    }

    public AmazfitBipFirmwareInfo(byte[] bytes) {
        super(bytes);
    }

    @Override
    protected HuamiFirmwareType determineFirmwareType(byte[] bytes) {
        if (ArrayUtils.startsWith(bytes, RES_HEADER)) {
            if (bytes.length > 500000) { // dont know how to distinguish from Cor .res
                return HuamiFirmwareType.INVALID;
            }
            return HuamiFirmwareType.RES;
        }
        if (ArrayUtils.startsWith(bytes, GPS_HEADER) || ArrayUtils.startsWith(bytes, GPS_HEADER2)) {
            return HuamiFirmwareType.GPS;
        }
        if (ArrayUtils.startsWith(bytes, GPS_ALMANAC_HEADER)) {
            return HuamiFirmwareType.GPS_ALMANAC;
        }
        if (ArrayUtils.startsWith(bytes, GPS_CEP_HEADER)) {
            return HuamiFirmwareType.GPS_CEP;
        }
        if (ArrayUtils.equals(bytes, FW_HEADER, FW_HEADER_OFFSET) || ArrayUtils.equals(bytes, FW_HEADER_NEW, FW_HEADER_OFFSET)) {
            // TODO: this is certainly not a correct validation, but it works for now
            return HuamiFirmwareType.FIRMWARE;
        }
        if (ArrayUtils.startsWith(bytes, WATCHFACE_HEADER)) {
            return HuamiFirmwareType.WATCHFACE;
        }
        return HuamiFirmwareType.INVALID;
    }

    @Override
    public boolean isGenerallyCompatibleWith(GBDevice device) {
        return isHeaderValid() && device.getType() == DeviceType.AMAZFITBIP;
    }

    @Override
    protected Map<Integer, String> getCrcMap() {
        return crcToVersion;
    }
}
