/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.sheba;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.junit.Test;
import org.sheba.Zk;
import org.sheba.model.User;

/**
 *
 * @author kabir
 */
public class ZKUserTest {

    @Test
    public void test() {

        try {
            Zk zk = new Zk("103.197.207.10", "4370");
            if (zk.connect()) {

                zk.deviceEnable();
                List<User> users = zk.getUser();
                for (User user : users) {
                    System.out.println(user.getUserid());
                }
                zk.disconnect();
            } else {
                System.out.println("Not connected");
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
