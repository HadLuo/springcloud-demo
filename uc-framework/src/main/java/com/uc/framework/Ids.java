package com.uc.framework;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ids implements Serializable {

    private static final long serialVersionUID = -985349909068095474L;

    private static final Logger logger = LoggerFactory.getLogger(Ids.class);

    private static final int LOW_ORDER_THREE_BYTES = 16777215;

    private static final int MACHINE_IDENTIFIER;

    private static final short PROCESS_IDENTIFIER;

    private static final AtomicInteger NEXT_COUNTER = new AtomicInteger(new SecureRandom().nextInt());

    static {
        try {
            MACHINE_IDENTIFIER = createMachineIdentifier();
            PROCESS_IDENTIFIER = createProcessIdentifier();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final int timestamp;
    private final int machineIdentifier;
    private final short processIdentifier;
    private final int counter;

    private Ids() {
        this(new Date());
    }

    private Ids(Date date) {
        this(dateToTimestampSeconds(date), MACHINE_IDENTIFIER, PROCESS_IDENTIFIER, NEXT_COUNTER
                .getAndIncrement(), false);
    }

    private Ids(Date date, int counter) {
        this(date, MACHINE_IDENTIFIER, PROCESS_IDENTIFIER, counter);
    }

    private Ids(Date date, int machineIdentifier, short processIdentifier, int counter) {
        this(dateToTimestampSeconds(date), machineIdentifier, processIdentifier, counter);
    }

    private Ids(int timestamp, int machineIdentifier, short processIdentifier, int counter) {
        this(timestamp, machineIdentifier, processIdentifier, counter, true);
    }

    private Ids(int timestamp, int machineIdentifier, short processIdentifier, int counter,
            boolean checkCounter) {
        if ((machineIdentifier & 0xFF000000) != 0) {
            throw new IllegalArgumentException(
                    "The machine identifier must be between 0 and 16777215 (it must fit in three bytes).");
        }

        if ((checkCounter) && ((counter & 0xFF000000) != 0)) {
            throw new IllegalArgumentException(
                    "The counter must be between 0 and 16777215 (it must fit in three bytes).");
        }

        this.timestamp = timestamp;
        this.machineIdentifier = machineIdentifier;
        this.processIdentifier = processIdentifier;
        this.counter = (counter & 0xFFFFFF);
    }

    private Ids(String hexString) {
        this(parseHexString(hexString));
    }

    private Ids(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException();
        }
        if (bytes.length != 12) {
            throw new IllegalArgumentException("need 12 bytes");
        }
        this.timestamp = makeInt(bytes[0], bytes[1], bytes[2], bytes[3]);
        this.machineIdentifier = makeInt((byte) 0, bytes[4], bytes[5], bytes[6]);
        this.processIdentifier = (short) makeInt((byte) 0, (byte) 0, bytes[7], bytes[8]);
        this.counter = makeInt((byte) 0, bytes[9], bytes[10], bytes[11]);
    }

    private Ids(int timestamp, int machineAndProcessIdentifier, int counter) {
        this(legacyToBytes(timestamp, machineAndProcessIdentifier, counter));
    }

    private static Ids get() {
        return new Ids();
    }

    public static String getId() {
        return get().toHexString();
    }

    private static boolean isValid(String hexString) {
        if (hexString == null) {
            throw new IllegalArgumentException();
        }
        int len = hexString.length();
        if (len != 24) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            char c = hexString.charAt(i);
            if ((c < '0') || (c > '9')) {
                if ((c < 'a') || (c > 'f')) {
                    if ((c < 'A') || (c > 'F')) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static int getMachineIdentifier() {
        return MACHINE_IDENTIFIER;
    }

    public static int getProcessIdentifier() {
        return PROCESS_IDENTIFIER;
    }

    private static byte[] legacyToBytes(int timestamp, int machineAndProcessIdentifier, int counter) {
        byte[] bytes = new byte[12];
        bytes[0] = int3(timestamp);
        bytes[1] = int2(timestamp);
        bytes[2] = int1(timestamp);
        bytes[3] = int0(timestamp);
        bytes[4] = int3(machineAndProcessIdentifier);
        bytes[5] = int2(machineAndProcessIdentifier);
        bytes[6] = int1(machineAndProcessIdentifier);
        bytes[7] = int0(machineAndProcessIdentifier);
        bytes[8] = int3(counter);
        bytes[9] = int2(counter);
        bytes[10] = int1(counter);
        bytes[11] = int0(counter);
        return bytes;
    }

    private static int createMachineIdentifier() {
        int machinePiece;
        try {
            StringBuilder sb = new StringBuilder();
            @SuppressWarnings("rawtypes")
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) e.nextElement();
                sb.append(ni.toString());
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    ByteBuffer bb = ByteBuffer.wrap(mac);
                    try {
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                    } catch (BufferUnderflowException shortHardwareAddressException) {
                    }
                }
            }
            machinePiece = sb.toString().hashCode();
        } catch (Throwable t) {
            machinePiece = new SecureRandom().nextInt();
            logger.error(
                    "Failed to get machine identifier from network interface, using random number instead", t);
        }
        machinePiece &= LOW_ORDER_THREE_BYTES;
        return machinePiece;
    }

    private static short createProcessIdentifier() {
        short processId;
        try {
            String processName = ManagementFactory.getRuntimeMXBean().getName();
            if (processName.contains("@"))
                processId = (short) Integer.parseInt(processName.substring(0, processName.indexOf('@')));
            else
                processId = (short) ManagementFactory.getRuntimeMXBean().getName().hashCode();
        } catch (Throwable t) {
            processId = (short) new SecureRandom().nextInt();
            logger.error("Failed to get process identifier from JMX, using random number instead", t);
        }

        return processId;
    }

    private static byte[] parseHexString(String s) {
        if (!isValid(s)) {
            throw new IllegalArgumentException(new StringBuilder()
                    .append("invalid hexadecimal representation of an ObjectId: [").append(s).append("]")
                    .toString());
        }
        byte[] b = new byte[12];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        return b;
    }

    private static int dateToTimestampSeconds(Date time) {
        return (int) (time.getTime() / 1000L);
    }

    private static int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return b3 << 24 | (b2 & 0xFF) << 16 | (b1 & 0xFF) << 8 | b0 & 0xFF;
    }

    private static byte int3(int x) {
        return (byte) (x >> 24);
    }

    private static byte int2(int x) {
        return (byte) (x >> 16);
    }

    private static byte int1(int x) {
        return (byte) (x >> 8);
    }

    private static byte int0(int x) {
        return (byte) x;
    }

    private static byte short1(short x) {
        return (byte) (x >> 8);
    }

    private static byte short0(short x) {
        return (byte) x;
    }

    private byte[] toByteArray() {
        byte[] bytes = new byte[12];
        bytes[0] = int3(this.timestamp);
        bytes[1] = int2(this.timestamp);
        bytes[2] = int1(this.timestamp);
        bytes[3] = int0(this.timestamp);
        bytes[4] = int2(this.machineIdentifier);
        bytes[5] = int1(this.machineIdentifier);
        bytes[6] = int0(this.machineIdentifier);
        bytes[7] = short1(this.processIdentifier);
        bytes[8] = short0(this.processIdentifier);
        bytes[9] = int2(this.counter);
        bytes[10] = int1(this.counter);
        bytes[11] = int0(this.counter);
        return bytes;
    }

    private String toHexString() {
        StringBuilder buf = new StringBuilder(24);
        for (byte b : toByteArray()) {
            buf.append(String.format("%02x", new Object[] { Integer.valueOf(b & 0xFF) }));
        }
        return buf.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        Ids objectId = (Ids) o;
        if (this.counter != objectId.counter) {
            return false;
        }
        if (this.machineIdentifier != objectId.machineIdentifier) {
            return false;
        }
        if (this.processIdentifier != objectId.processIdentifier) {
            return false;
        }
        if (this.timestamp != objectId.timestamp) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = this.timestamp;
        result = 31 * result + this.machineIdentifier;
        result = 31 * result + this.processIdentifier;
        result = 31 * result + this.counter;
        return result;
    }

    public int compareTo(Ids other) {
        if (other == null) {
            throw new NullPointerException();
        }
        byte[] byteArray = toByteArray();
        byte[] otherByteArray = other.toByteArray();
        for (int i = 0; i < 12; i++) {
            if (byteArray[i] != otherByteArray[i]) {
                return (byteArray[i] & 0xFF) < (otherByteArray[i] & 0xFF) ? -1 : 1;
            }
        }
        return 0;
    }

    public String toString() {
        return toHexString();
    }
}
