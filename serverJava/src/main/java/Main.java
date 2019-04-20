import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.net.*;

public class Main {

    public static final String[] parametersType = {"TEMP", "HUMI", "LUMI", "WEIG", "RAND"};

    public static void main(String[] args) throws IOException {

        //connection to database
        MongoClient mongoClient = MongoClients.create(Credentials.uri);
        MongoDatabase database = mongoClient.getDatabase("seriot");

        MongoCollection<Document> records = database.getCollection("records");

        Document document = new Document();
        document.append("anni",15);
        document.append("nome","Giulio");
        records.insertOne(document);


        String str = "";
        DatagramSocket ds = new DatagramSocket(2000);
        byte[] buf = new byte[1024];
        InetAddress ip = InetAddress.getByName("192.168.1.9");

        int pos = 0;
        int parametersNumber = 0;

        DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), ip, 2001);
        ds.send(dp);

        while (true) {

            String[] data = new String[parametersType.length + 2];

            pos = 0;
            for(String s: parametersType){
                data[pos++] = s + ": ";
            }
            data[parametersType.length] = "ID: ";
            data[parametersType.length+1] = "TIME: ";

            dp = new DatagramPacket(buf, 1024);
            ds.receive(dp);
            str = new String(dp.getData(), 0, dp.getLength());

            if (str.charAt(0) == 'H' && str.charAt(1) == 'E' && str.charAt(2) == 'R' && str.charAt(3) == 'E') {

                str = "" + System.currentTimeMillis();

                dp = new DatagramPacket(str.getBytes(), str.length(), ip, 2001);
                ds.send(dp);

            } else if (str.charAt(0) == 'P' && str.charAt(1) == 'O' && str.charAt(2) == 'S' && str.charAt(3) == 'T') {

                parametersNumber = (int) str.charAt(5) - 48;

                for (int i = 7; i < str.length(); i++) {
                    if ((int) str.charAt(i) > 64 && (int) str.charAt(i) < 91) {
                        parametersNumber--;
                        pos = getPosition(i, str);
                        i += 5;
                        while ((int) str.charAt(i) != '/') {
                            data[pos] += str.charAt(i++);
                        }
                    } else if (parametersNumber == 0) {
                        data[parametersType.length] += str.charAt(i);
                        i += 2;
                        while (i < str.length()) {
                            data[parametersType.length+1] += str.charAt(i++);
                        }
                    }
                }

                for (String s : data) {
                    System.out.println(s);
                }
                System.out.println("");

            }

        }

    }

    public static int getPosition(int pos, String str) {

        for (int i = 0; i < parametersType.length; i++) {
            if (str.charAt(pos) == parametersType[i].charAt(0) &&
                    str.charAt(pos + 1) == parametersType[i].charAt(1) &&
                    str.charAt(pos + 2) == parametersType[i].charAt(2) &&
                    str.charAt(pos + 3) == parametersType[i].charAt(3)){
                return i;
            }
        }

        return -1;

    }
}
