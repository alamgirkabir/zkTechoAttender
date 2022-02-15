package org.sheba;

import org.sheba.Zk;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.List;

import org.sheba.model.Attendance;

/**
 * Unit test for simple App.
 */
public class ZkTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ZkTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ZkTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        Zk zk = new Zk("103.197.207.10", "4370");

        try {
            if (zk.connect()) {
                zk.deviceEnable();
//                zk.clearAttendance();
                List<Attendance> attendances = zk.getAttendance();
                for (Attendance attendance : attendances) {
                    String state = attendance.getState() == 0 ? "Check In" : "Check Out";
                    System.out.println(" [UID " + attendance.getUid() + "]: Time :" + attendance.getTime() + ", Status: " + state);
                }
                zk.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(true);
    }

}
