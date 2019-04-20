import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(2000);
        byte[] buf = new byte[1024];
        InetAddress ip = InetAddress.getByName("192.168.1.9");
        Scanner scanner=new Scanner(System.in);
        String str = "";
        DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), ip, 2001);
        ds.send(dp);
        while (true) {
            dp = new DatagramPacket(buf, 1024);
            ds.receive(dp);
            str = new String(dp.getData(), 0, dp.getLength());
            if(str.equals("HERE/0/1/0")){
                str = "" + System.currentTimeMillis();
                //str = "10";
                dp = new DatagramPacket(str.getBytes(), str.length(), ip, 2001);
                ds.send(dp);
            } else if(str.charAt(0) == 'P'){
                System.out.println(str);
            }
        }


    }
}
