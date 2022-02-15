package org.sheba.model;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import org.apache.commons.lang3.ArrayUtils;
import org.sheba.utils.ByteUtils;
import org.sheba.utils.TimeUtils;

public class Attendance {

    private int uid;
    private int state;
    private Date time;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public static Attendance encodeAttendance(ByteBuffer buffer) {
        Attendance attendance = new Attendance();
        attendance.setUid(Short.toUnsignedInt(buffer.getShort(0)));
        byte state = buffer.get(29);
        attendance.setState(state);
        byte[] data = buffer.array();

        byte[] userid = Arrays.copyOfRange(data, 0, 4);
        String userIdStr = new String(userid);
        userIdStr = userIdStr.split(new String("\0"))[0];
        attendance.setUid(Integer.parseInt(userIdStr));
        byte[] timeByte = Arrays.copyOfRange(data, 25, 29);
        ArrayUtils.reverse(timeByte);
        Long timeLong = Long.parseLong(ByteUtils.bytesToHex(timeByte), 16);
        Date date = TimeUtils.decodeTime(timeLong);
        attendance.setTime(date);
        return attendance;
    }

    @Override
    public String toString() {
        return "Attendance [uid=" + uid + ", state=" + state + ", time=" + time + "]";
    }

}
