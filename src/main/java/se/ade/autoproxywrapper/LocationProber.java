package se.ade.autoproxywrapper;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class LocationProber {
    public boolean isAddressResolvable(String address) {
        try {
            InetAddress.getByName(address);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
