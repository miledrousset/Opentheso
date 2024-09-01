package fr.cnrs.opentheso.utils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;


public class EmailUtils {


    public static boolean isValidEmailAddress(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }

}
