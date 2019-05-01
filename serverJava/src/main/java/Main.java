/**
 * Questo programma funge da server per la recezione di alcuni dati inviati da una scheda Arduino.
 * I dati rappresentano dei valori ricavati tramite diversi sensori, rispettivamente:
 * TEMPERATURA, UMIDITÀ, LUMINOSITÀ, PESO, NUMERO RANDOM.
 * Quando il server viene a conoscenza della presenza dell'Arduino gli invia il tempo.
 * Il programma si occupa anche di salvare queste misurazioni su un file .csv e su un database MongodDB
 *
 * @author Campagnol Leonardo e Basaglia Alberto
 * @version 1.0
 * @since 21-04-2019
 */

import java.io.*;
import java.net.*;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Main {

    public static void main(String[] args) throws IOException {

        //Inizializzazione della cominicazione con il database e con la rispettiva collection
        MongoClient mongoClient = MongoClients.create(Credentials.uri);
        MongoDatabase database = mongoClient.getDatabase("seriot");
        MongoCollection<Document> records = database.getCollection("records");

        String str = "";
        DatagramSocket ds = new DatagramSocket(2000);
        byte[] buf = new byte[1024];

        int pos;             //posizione in cui inserire il valore
        int i;               //contatore
        String support = ""; //stringa di supporto per l'inserimento dei valori

        DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), InetAddress.getByName("255.255.255.255"), 2001);
        ds.send(dp);

        try {

            File file = new File("records.csv");
            FileWriter fw = new FileWriter(file, true);
            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            //se la prima riga del file è vuota viene scitta l'intestazione
            if (br.readLine() == null) {

                pw.println("Temperature,Humidity,Luminosity,Weigth,Random,Id,Time");
                pw.flush();

            }

            while (true) {

                dp = new DatagramPacket(buf, 1024);
                ds.receive(dp);
                str = new String(dp.getData(), 0, dp.getLength());

                //secondo il protocollo utilizzato HERE significa che l'Arduino ha bisogno di ricevere il tempo
                if (str.substring(0, 4).equals("HERE")) {

                    str = "" + System.currentTimeMillis();

                    dp = new DatagramPacket(str.getBytes(), str.length(), dp.getAddress(), 2001);
                    ds.send(dp);

                }
                //secondo il protocollo utilizzato POST identifica un messaggio contenente dei valori
                else if (str.substring(0, 4).equals("POST")) {

                    int analizedParameters = 0; //numero di parametri che sono stati analizzati

                    long[] data = new long[7]; //array contenente tutti i valori passati dall'Arduino

                    //inizializzazione di tutti i valori dell'array
                    for (i = 0; i < data.length; i++) {
                        data[i] = 0;
                    }

                    /**inserimento delle misurazioni nell'array
                     * il ciclo esce una volta che tutti i parametri sono stati analizzati
                     */
                    for (i = 5; analizedParameters != 5; i++) {

                        if ((int) str.charAt(i) > 64 && (int) str.charAt(i) < 91) {

                            pos = getPosition(i, str);

                            i += 5; //viene raggiunta la posizione da cui il valore inizia

                            //inserimento del parametro nell'array
                            support = "";
                            while (str.charAt(i) != '/') {
                                support += str.charAt(i++);
                            }
                            data[pos] = Long.parseLong(support);

                            analizedParameters++;

                        }

                    }

                    //inserimento dell'id nell'array
                    support = "";
                    while (str.charAt(i) != '/') {
                        support += str.charAt(i++);
                    }
                    data[5] = Long.parseLong(support);

                    i++;

                    //inserimento del tempo nell'array
                    support = "";
                    while (i < str.length()) {
                        support += str.charAt(i++);
                    }
                    data[6] = Long.parseLong(support);

                    //salvataggio dei dati sul database
                    Document document = new Document();
                    document.append("temp", data[0]);
                    document.append("humi", data[1]);
                    document.append("lumi", data[2]);
                    document.append("weig", data[3]);
                    document.append("rand", data[4]);
                    document.append("addr", data[5]);
                    document.append("time", data[6]);
                    records.insertOne(document);

                    //salvataggio dei dati su file .csv
                    pw.println(
                            data[0] + "," +
                            data[1] + "," +
                            data[2] + "," +
                            data[3] + "," +
                            data[4] + "," +
                            data[5] + "," +
                            data[6]);
                    pw.flush();

                    //scrittura dei dati su console
                    System.out.println("Temperature: " + data[0]);
                    System.out.println("Humidity:    " + data[1]);
                    System.out.println("Luminosity:  " + data[2]);
                    System.out.println("Weigth:      " + data[3]);
                    System.out.println("Random:      " + data[4]);
                    System.out.println("Address:     " + data[5]);
                    System.out.println("Time:        " + data[6] + "\n");

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Funzione che restituisce la posizione dell'array corrispondente al parametro desiderato
     *
     * @param i   ovvero la posizione della stringa da cui iniziare la verifica dell'etichetta del parametro
     * @param str ovvero la stringa da analizzare
     * @return un numero corrispondente alla posizione in cui dovrà essere inserito il valore ricavato
     */
    private static int getPosition(int i, String str) {

        if (str.substring(i, i + 4).equals("TEMP")) {
            return 0;
        } else if (str.substring(i, i + 4).equals("HUMI")) {
            return 1;
        } else if (str.substring(i, i + 4).equals("LUMI")) {
            return 2;
        } else if (str.substring(i, i + 4).equals("WEIG")) {
            return 3;
        } else if (str.substring(i, i + 4).equals("RAND")) {
            return 4;
        }

        return -1;
    }

}
