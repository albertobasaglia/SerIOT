import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.net.*;

public class Main {

    public static final String[] prefixesType = {"HERE", "POST"};
    public static final String[] parametersType = {"TEMP", "HUMI", "LUMI", "WEIG", "RAND"};

    public static void main(String[] args) throws IOException {

        MongoClient mongoClient = MongoClients.create(Credentials.uri);
        MongoDatabase database = mongoClient.getDatabase("seriot");
        MongoCollection<Document> records = database.getCollection("records");

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

            for (int i = 0; i < data.length; i++) {
                data[i] = "";
            }

            dp = new DatagramPacket(buf, 1024);
            ds.receive(dp);
            str = new String(dp.getData(), 0, dp.getLength());

            if (getPosition(0, str) == -1) {

                str = "" + System.currentTimeMillis();

                dp = new DatagramPacket(str.getBytes(), str.length(), ip, 2001);
                ds.send(dp);

            } else if (getPosition(0, str) == -2) {

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
                            data[parametersType.length + 1] += str.charAt(i++);
                        }
                    }
                }

                Document document = new Document();
                for (int i = 0; i < data.length - 2; i++) {
                    if (!data[i].equals("")) {
                        document.append(parametersType[i], data[i]);
                    }
                }
                document.append("ADDR", data[data.length - 2]);
                document.append("TIME", data[data.length - 1]);
                records.insertOne(document);

                for (String s : data) {
                    System.out.println(s);
                }
                System.out.println("");

            }

        }

    }

    public static int getPosition(int pos, String str) {

        for (int i = prefixesType.length - 1; i > -1; i--) {
            if (str.charAt(pos) == prefixesType[i].charAt(0) &&
                    str.charAt(pos + 1) == prefixesType[i].charAt(1) &&
                    str.charAt(pos + 2) == prefixesType[i].charAt(2) &&
                    str.charAt(pos + 3) == prefixesType[i].charAt(3)) {
                return -i - 1;
            }
        }
        for (int i = 0; i < parametersType.length; i++) {
            if (str.charAt(pos) == parametersType[i].charAt(0) &&
                    str.charAt(pos + 1) == parametersType[i].charAt(1) &&
                    str.charAt(pos + 2) == parametersType[i].charAt(2) &&
                    str.charAt(pos + 3) == parametersType[i].charAt(3)) {
                return i;
            }
        }

        return -1;

    }
}
