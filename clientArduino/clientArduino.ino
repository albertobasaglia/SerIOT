// Programma per l'invio, tramite shield Ethernet, di alcuni dati rilevati dall'Arduino con diversi sensori, utilizzando un protocollo
// Struttura del protocollo: TYPE/PARAM/VALUE OF THE PARAMETER/PARAM/VALUE OF THE PARAMETER/ID/TIME
// Autori: Campagnol Leonardo e Basaglia Alberto

#include <Ethernet.h>
#include <DHT.h>
#include <LiquidCrystal_I2C.h>
#include <EthernetUdp.h>

DHT dht(6, DHT11);                 //sensore di temperatura
LiquidCrystal_I2C lcd(0x27, 20, 4); //schermo LCD

byte mac[] = {0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED}; //impostazione dell'indirizzo MAC

IPAddress ip(192, 168, 1, 9);      //indirizzo ip della scheda arduino
IPAddress server(192, 168, 1, 62); //indirizzo ip del server

unsigned int localPort = 2001;      // local port to listen on
unsigned int externalPort = 2000;

char packetBuffer[UDP_TX_PACKET_MAX_SIZE]; //buffer per l'immagazzinamento dei pacchetti in entrata
int packetSize = 0; //dimensione del pacchetto ricevuto

EthernetUDP Udp; //EthernetUDP che permette la recezione e l'invio dei dati

String message = ""; //il messaggio che varrà inviato al server

int temperature = 0;  //parametro contenente il valore della temperatura
int humidity = 0;     //parametro contenente il valore dell'umidità
int luminosity = 0;   //parametro contenente il valore della luminosità
int weight = 0;       //parametro contenente il valore del peso
int randomNumber = 0; //parametro contenente un valore casuale generato tra 0 e 1000, estremi compresi

//campi necessari per eseguire la somma di due stringhe rappresentati il tempo trascorso
String addend1 = "";
String addend2 = "";
String result = "";
String recivedTime = "";
boolean rest = false;

String id = "1"; //id dell'Arduino

long delayTime = 10000; //tempo che dovrà aspettare prima di inviare il successivo messaggio

int i = 0; //contatore

void setup() {

  Serial.begin(9600);
  Serial.println("setup");

  Ethernet.begin(mac, ip);
  Udp.begin(localPort);

  dht.begin();

  //accensione display LCD
  lcd.init();
  lcd.backlight();

  randomSeed(analogRead(0)); //generazione di un seme casuale

  while (true) {

    Udp.beginPacket(server, externalPort);
    Udp.write("HERE/0/1/0");
    Udp.endPacket();

    packetSize = Udp.parsePacket();

    if (packetSize) {

      Serial.println("Ricevuto valore del tempo dal server\n");
      Udp.read(packetBuffer, UDP_TX_PACKET_MAX_SIZE);

      addend1 = String(packetBuffer); //il primo addendo diventa il tempo ricevuto dal server
      //il secondo addendo diventa una stringa della stessa dimesione del primo ma contenente solo il tempo d'attesa prestabilito
      for (i = 0; i < addend1.length() - String(delayTime).length(); i++) {
        addend2 += "0";
      }
      addend2 += delayTime;

      break;

    }

    delay(500);

  }

}

void loop() {

  lcd.clear();

  temperature = dht.readTemperature();
  humidity = dht.readHumidity();
  luminosity = analogRead(A1);
  weight = analogRead(A2);
  randomNumber = random(1, 1001);

  message = "POST/";

  message += "TEMP/" + String(temperature, DEC) + "/";
  lcd.setCursor(0, 0);
  lcd.print("TEMP:" + String(temperature, DEC) + char(0xDF));

  message += "HUMI/" + String(humidity, DEC) + "/";
  lcd.setCursor(11, 0);
  lcd.print("HUMI:" + String(humidity, DEC) + "%");

  message += "LUMI/" + String(luminosity, DEC) + "/";
  lcd.setCursor(0, 1);
  lcd.print("LUMI:" + String(luminosity, DEC));

  message += "WEIG/" + String(weight, DEC) + "/";
  lcd.setCursor(11, 1);
  lcd.print("WEIG:" + String(weight, DEC));

  message += "RAND/" + String(randomNumber, DEC) + "/";
  lcd.setCursor(0, 2);
  lcd.print("RAND:" + String(randomNumber, DEC));


  //algoritmo per la somma di due stringhe
  result = "";
  recivedTime = "";
  for (i = addend1.length() - 1; i > -1; i--) {
    if ((int)(addend1.charAt(i) - 48) + (int)(addend2.charAt(i) - 48) > 9) {
      result += (char)(((int)(addend1.charAt(i) - 48) + (int)(addend2.charAt(i) - 48) - 10) + 48);
      if (i == 0) {
        result += "1";
      } else rest = true;
    }
    else {
      if (rest) {
        if ((int)(addend1.charAt(i) - 48) + (int)(addend2.charAt(i) - 48) + 1 > 9) {
          result += "0";
        }
        else {
          result += (char)(((int)(addend1.charAt(i) - 48) + (int)(addend2.charAt(i) - 48) + 1) + 48);
          rest = false;
        }
      }
      else result += (char)(((int)(addend1.charAt(i) - 48) + (int)(addend2.charAt(i) - 48)) + 48);
    }
  }
  for (i = result.length() - 1; i > -1; i--) {
    recivedTime += result.charAt(i);
  }
  addend1 = recivedTime;

  message += id + "/" + recivedTime;
  lcd.setCursor(11, 2);
  lcd.print("ADDR:" + id);
  
  lcd.setCursor(0, 3);
  lcd.print("TIME:" + recivedTime);

  Serial.println("Inviato messaggio: " + message);

  char support[message.length() + 1];
  message.toCharArray(support, message.length() + 1);

  Udp.beginPacket(server, externalPort);
  Udp.write(support);
  Udp.endPacket();

  delay(delayTime);

}
