import java.io.IOException;
import java.net.*;

public class Main {
    public static void main(String[] args) throws IOException {

        String str = "";
        DatagramSocket ds = new DatagramSocket(2000);
        byte[] buf = new byte[1024];
        InetAddress ip = InetAddress.getByName("192.168.1.9");

        int pos = 0;
        int parameters = 0;

        DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), ip, 2001);
        ds.send(dp);

        while (true) {

            String[] data = {"TEMP: ","HUMI: ","LUMI: ","WEIG: ","RAND: ","ID: ","TIME: "};

            dp = new DatagramPacket(buf, 1024);
            ds.receive(dp);
            str = new String(dp.getData(), 0, dp.getLength());

            if (str.charAt(0) == 'H' && str.charAt(1) == 'E' && str.charAt(2) == 'R' && str.charAt(3) == 'E') {

                str = "" + System.currentTimeMillis();

                dp = new DatagramPacket(str.getBytes(), str.length(), ip, 2001);
                ds.send(dp);

            } else if (str.charAt(0) == 'P' && str.charAt(1) == 'O' && str.charAt(2) == 'S' && str.charAt(3) == 'T') {

                parameters = (int)str.charAt(5) - 48;

                for(int i = 7; i < str.length(); i++){
                    if((int)str.charAt(i) > 64 && (int)str.charAt(i) < 91){
                        parameters--;
                        pos = getPosition(i, str);
                        i += 5;
                        while((int)str.charAt(i) != '/'){
                            data[pos] += str.charAt(i++);
                        }
                    }
                    else if(parameters == 0){
                        data[5] += str.charAt(i);
                        i += 2;
                        while(i < str.length()){
                            data[6] += str.charAt(i++);
                        }
                    }
                }

                for(String s : data){
                    System.out.println(s);
                }
                System.out.println("");

                //System.out.println(str);

            }

        }

    }

    public static int getPosition(int i, String s) {

        if (s.charAt(i) == 'T' && s.charAt(i + 1) == 'E' && s.charAt(i + 2) == 'M' && s.charAt(i + 3) == 'P')
            return 0;
        else if (s.charAt(i) == 'H' && s.charAt(i + 1) == 'U' && s.charAt(i + 2) == 'M' && s.charAt(i + 3) == 'I')
            return 1;
        else if (s.charAt(i) == 'L' && s.charAt(i + 1) == 'U' && s.charAt(i + 2) == 'M' && s.charAt(i + 3) == 'I')
            return 2;
        else if (s.charAt(i) == 'W' && s.charAt(i + 1) == 'E' && s.charAt(i + 2) == 'I' && s.charAt(i + 3) == 'G')
            return 3;
        else if (s.charAt(i) == 'R' && s.charAt(i + 1) == 'A' && s.charAt(i + 2) == 'N' && s.charAt(i + 3) == 'D')
            return 4;
        else return -1;
    }
}
