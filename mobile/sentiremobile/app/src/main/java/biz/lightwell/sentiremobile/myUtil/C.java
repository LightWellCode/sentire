package biz.lightwell.sentiremobile.myUtil;

import java.util.UUID;

/**
 * Created by stew on 3/23/2017.
 * Purpose: Store Constants
 * Notes:
 */

public final class C {

    // START logging -------------------------- logging
        public static final String LOGTAG   = "SENTIRE";
        public static final Boolean LOGGING = true;
        public static final boolean RUNSERVICE = true;
    // FINISH logging -------------------------- logging


    // START BLE ------------------------------- BLE
        public static final String DEVICE_NAME = "SENTIRE";
        /* Humidity Service */
        public static final UUID HUMIDITY_SERVICE = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
        public static final UUID HUMIDITY_DATA_CHAR = UUID.fromString("f000aa21-0451-4000-b000-000000000000");
        public static final UUID HUMIDITY_CONFIG_CHAR = UUID.fromString("f000aa22-0451-4000-b000-000000000000");
        /* Barometric Pressure Service */
        public static final UUID PRESSURE_SERVICE = UUID.fromString("f000aa40-0451-4000-b000-000000000000");
        public static final UUID PRESSURE_DATA_CHAR = UUID.fromString("f000aa41-0451-4000-b000-000000000000");
        public static final UUID PRESSURE_CONFIG_CHAR = UUID.fromString("f000aa42-0451-4000-b000-000000000000");
        public static final UUID PRESSURE_CAL_CHAR = UUID.fromString("f000aa43-0451-4000-b000-000000000000");
        /* Client Configuration Descriptor */
        public static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        /* LED */
        public static final UUID LED_SERVICE = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214");
        public static final UUID LED_DATA_CHAR = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214");
        /* Gas sensors */
        public static final UUID MQ2_SERVICE = UUID.fromString("41ef6040-e249-4035-81ee-8999024d88ed");
        public static final UUID MQ2_DATA_CHAR = UUID.fromString("41ef6041-e249-4035-81ee-8999024d88ed");
        public static final UUID MQ2_CONFIG_CHAR = UUID.fromString("41ef6042-e249-4035-81ee-8999024d88ed");


        /* Internal handler messages */
        public static final int MSG_HUMIDITY = 101;
        public static final int MSG_PRESSURE = 102;
        public static final int MSG_PRESSURE_CAL = 103;
        public static final int MSG_PROGRESS = 201;
        public static final int MSG_DISMISS = 202;
        public static final int MSG_CLEAR = 301;
        public static final int MSG_LED = 999;
        public static final int MSG_MQ2 = 998;

        public static final int MSG_STARTSCAN = 1;
        public static final int MSG_STOPSCAN = 2;
        public static final int MSG_CONNECTDEVICE = 3;

    // FINISH BLE ------------------------------- BLE




    public C() {}

}
