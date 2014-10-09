package hu.hendricha.consodroid;

import android.util.Log;

import java.net.*;
import java.util.*;
import org.apache.http.conn.util.InetAddressUtils;

public class IpUtil {
    /**
     * Get IP address from all non-localhost interface
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static List<Map.Entry<String,String>> getIPAddresses(boolean useIPv4) {
        List<Map.Entry<String,String>> addresses = new ArrayList<Map.Entry<String,String>>();

        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                Log.d("ConsoDroid", "Found network interface: " + intf.getDisplayName());
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr) && !sAddr.contains("%");
                        if (useIPv4) {
                            if (isIPv4) {
                                addresses.add(new AbstractMap.SimpleEntry<String,String>(intf.getDisplayName(), sAddr));
                                Log.d("ConsoDroid", "Found ip v4 address:" + sAddr);
                            }
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                if (delim >= 0) {
                                    sAddr = sAddr.substring(0, delim);
                                }
                                addresses.add(new AbstractMap.SimpleEntry<String,String>(intf.getDisplayName(), sAddr));
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return addresses;
    }

    /**
     * Get the first IPv4 address that is ideal for accessing ConsoDroid from
     * @return  address or empty string
     */
    public static String getIdealIPAddress() {
        List<Map.Entry<String,String>> addresses = getIPAddresses(true);
        for (String ifaceType: Arrays.asList("wlan", "eth", "rmnet")) {
            for (Map.Entry<String,String> address: addresses) {
                if (address.getKey().contains(ifaceType)) {
                    return address.getValue();
                }
            }
        }
        for (Map.Entry<String,String> address: addresses) {
            return address.getValue();
        }
        return "";
    }


}