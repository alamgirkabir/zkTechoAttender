package org.sheba;

import static org.sheba.ZkConst.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.sheba.model.Attendance;
import org.sheba.model.User;

public class Zk {

    private SocketAddress address;
    private String port;
    private DatagramSocket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int session_id = 0;
    private int reply_id = -1 + USHRT_MAX;

    public Zk(String ip, String PortNumber) {
        this.port = PortNumber;
        address = new InetSocketAddress(ip, Integer.parseInt(port));
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public <T> T executeCmd(int command, String command_string, ZkCallBack<T> zkCallBack) throws IOException {
        if (command == CMD_CONNECT) {
            this.reply_id = -1 + USHRT_MAX;
        }

        byte[] buf = createHeader(command, 0, session_id, reply_id, command_string);

        DatagramPacket sendDataPack = new DatagramPacket(buf, buf.length, address);

        clientSocket.send(sendDataPack);
        byte[] bufRev = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(bufRev, bufRev.length);
        clientSocket.receive(receivePacket);

        ByteBuffer buffer = ByteBuffer.allocate(receivePacket.getLength()).order(ByteOrder.LITTLE_ENDIAN)
                .put(receivePacket.getData(), 0, receivePacket.getLength());
        session_id = Short.toUnsignedInt(buffer.getShort(4));
        reply_id = Short.toUnsignedInt(buffer.getShort(6));

        int comBack = checkValid(buffer);
        if (CMD_ACK_OK == comBack || CMD_CONNECTED == comBack) {
            return zkCallBack.callBack(true, buffer);
        } else if (CMD_PREPARE_DATA == comBack) {
            long size = Integer.toUnsignedLong(buffer.getInt(8));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            long bytes = size;
            while (bytes > 0) {
                byte[] bufRevApp = new byte[1032];
                DatagramPacket receivePacketApp = new DatagramPacket(bufRevApp, bufRevApp.length);
                clientSocket.receive(receivePacketApp);
                byteArrayOutputStream.write(receivePacketApp.getData(), 8, receivePacketApp.getLength() - 8);
                bytes -= 1024;
            }

            byte[] bufRevApp = new byte[8];
            DatagramPacket receivePacketApp = new DatagramPacket(bufRevApp, bufRevApp.length);
            clientSocket.receive(receivePacketApp);

            byte[] bufRevAppOut = byteArrayOutputStream.toByteArray();
            return zkCallBack.callBack(true, ByteBuffer.allocate(bufRevAppOut.length).order(ByteOrder.LITTLE_ENDIAN).put(bufRevAppOut, 0, bufRevAppOut.length));
        } else {
            return zkCallBack.callBack(false, buffer);
        }
    }

    public byte[] executeCmd(int command, String command_string) throws IOException {
        return executeCmd(command, command_string, (status, data) -> {
            return data.array();
        });
    }

    public boolean connect() throws IOException {
        return executeCmd(CMD_CONNECT, "", (status, data) -> {
            return status;
        });
    }

    public boolean deviceEnable() throws IOException {
        return executeCmd(CMD_ENABLEDEVICE, "", (status, data) -> {
            return status;
        });
    }

    public boolean deviceDisable() throws IOException {
        return executeCmd(CMD_DISABLEDEVICE, "", (status, data) -> {
            return status;
        });
    }

    public boolean clearAttendance() throws IOException {
        return executeCmd(CMD_CLEAR_ATTLOG, "", (status, data) -> {
            return status;
        });
    }

    public boolean disconnect() throws IOException {
        return executeCmd(CMD_EXIT, "", (status, data) -> {
            return status;
        });
    }

    public String version() throws IOException {
        return executeCmd(CMD_VERSION, "", (status, data) -> {
            byte[] dataByte = data.array();
            ByteBuffer dataout = ByteBuffer.allocate(dataByte.length).put(dataByte, 8, dataByte.length - 8);
            if (status) {
                return new String(dataout.array());
            } else {
                return "fail";
            }
        });
    }

    public String platformVersion() throws IOException {
        return executeCmd(CMD_DEVICE, "~ZKFPVersion", (status, data) -> {
            byte[] dataByte = data.array();
            ByteBuffer dataout = ByteBuffer.allocate(dataByte.length).put(dataByte, 8, dataByte.length - 8);
            if (status) {
                return new String(dataout.array());
            } else {
                return "fail";
            }
        });
    }

    private final int userDataSize = 72;
    private final int offsetUserData = 3;

    public List<User> getUser() throws IOException {
        byte[] bytes = new byte[]{(byte) 0x05};

        return executeCmd(CMD_USERTEMP_RRQ, new String(bytes), (status, data) -> {

            List<User> users = new ArrayList<>();
            int offset = offsetUserData;
            byte[] dataUserByte = data.array();
            int length = dataUserByte.length;

            while (offset + userDataSize <= length) {
                ByteBuffer userByteBuffer = ByteBuffer.allocate(userDataSize).put(data.array(), offset, userDataSize);
                User user = User.encodeUser(userByteBuffer);

                offset = offset + userDataSize;
                users.add(user);
            }
            return users;
        });
    }

    private final int attendanceDataSize = 40;
    private final int offsetAttendanceData = 6;

    public List<Attendance> getAttendance() throws IOException {
        return executeCmd(CMD_ATTLOG_RRQ, "", (status, data) -> {

            if (data.capacity() <= 8) {
                return new ArrayList();
            }
            List<Attendance> attendances = new ArrayList<>();
            int offset = offsetAttendanceData;
            byte[] dataUserByte = data.array();
            int length = dataUserByte.length;

            while (length - offset > 0) {
                int size = attendanceDataSize;
                if (offset + attendanceDataSize >= length) {
                    size = length - offset;
                }
                ByteBuffer userByteBuffer = ByteBuffer.allocate(size).put(data.array(), offset, size);
                Attendance attendance = Attendance.encodeAttendance(userByteBuffer);

                offset = offset + attendanceDataSize;
                attendances.add(attendance);
            }
            return attendances;
        });
    }

    public String delUser() throws IOException {
        return executeCmd(CMD_DEVICE, "", (status, data) -> {
            return null;
        });
    }

    public String setUser(int uid, String password, String name, int userId) throws IOException {
        return executeCmd(CMD_DEVICE, "", (status, data) -> {
            return null;
        });
    }

    private int checkValid(ByteBuffer buffer) {
        int command = Short.toUnsignedInt(buffer.getShort(0));
        return command;
    }

    private int createChkSum(ByteBuffer p) {

        int chksum = 0;

        for (int i = 0; i < p.remaining(); i += 2) {
            if (i == p.remaining() - 1) {
                chksum += p.get(i);
            } else {
                chksum += p.getChar(i);
            }
            chksum %= USHRT_MAX;
        }

        chksum = USHRT_MAX - chksum - 1;

        return chksum;
    }

    public byte[] createHeader(int command, int chksum, int session_id, int reply_id, String command_string) {
        byte[] bytes_command_string = command_string.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(8 + bytes_command_string.length).order(ByteOrder.LITTLE_ENDIAN)
                .putChar(0, (char) command).putChar(2, (char) chksum).putChar(4, (char) session_id)
                .putChar(6, (char) reply_id);
        for (int i = 0; i < bytes_command_string.length; i++) {
            byte b = bytes_command_string[i];
            byteBuffer = byteBuffer.put(i + 8, b);
        }

        byteBuffer = byteBuffer.putChar(2, (char) createChkSum(byteBuffer));
        reply_id = (reply_id + 1) % USHRT_MAX;
        byteBuffer = byteBuffer.putChar(6, (char) reply_id);
        return byteBuffer.array();
    }

}
