package com.longx.intelligent.app.imessage.server.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Created by LONG on 2024/1/13 at 10:11 PM.
 */
public class PasswordCrypto {

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

}
