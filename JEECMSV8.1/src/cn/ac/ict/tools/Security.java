
package cn.ac.ict.tools;

import java.security.MessageDigest;


public class Security
{

    public Security()
    {
    }

    public static String makMd5Digest(String f)
    {
        try
        {
            MessageDigest alg = MessageDigest.getInstance("MD5");
            alg.update(f.getBytes());
            byte digest[] = alg.digest();
            return byte2hex(digest);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }
    
    private static String byte2hex(byte b[])
    {
        String hs = "";
        String stmp = "";
        for(int n = 0; n < b.length; n++)
        {
            stmp = Integer.toHexString(b[n] & 0xff);
            if(stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
        }

        return hs.toLowerCase();
    }

}