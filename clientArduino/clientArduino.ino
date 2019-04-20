#include <Ethernet.h>
#include <DHT.h>
#include <EthernetUdp.h>
#include <String.h>

DHT dht(50, DHT11);

// Enter a MAC address and IP address for your controller below.
// The IP address will be dependent on your local network:
byte mac[] = {
  0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED
};
IPAddress ip(192, 168, 1, 9);
IPAddress server(192, 168, 1, 62);

unsigned int localPort = 2001;      // local port to listen on
unsigned int externalPort = 2000;

// buffers for receiving and sending data
char packetBuffer[UDP_TX_PACKET_MAX_SIZE];  // buffer to hold incoming packet

// An EthernetUDP instance to let us send and receive packets over UDP
EthernetUDP Udp;

int temperature = 0;
int humidity = 0;
int luminosity = 0;
int weight = 0;
int randomNumber = 0;

String addend1 = "";
String addend2 = "";
String result = "";
String recivedTime = "";
boolean rest = false;

int parameters[5];
int parametersNumber = 0;

String message = "";
String id = "1";

int i = 0;

long delayTime = 3000;

void setup() {
  // You can use Ethernet.init(pin) to configure the CS pin
  //Ethernet.init(10);  // Most Arduino shields
  //Ethernet.init(5);   // MKR ETH shield
  //Ethernet.init(0);   // Teensy 2.0
  //Ethernet.init(20);  // Teensy++ 2.0
  //Ethernet.init(15);  // ESP8266 with Adafruit Featherwing Ethernet
  //Ethernet.init(33);  // ESP32 with Adafruit Featherwing Ethernet

  // start the Ethernet
  Ethernet.begin(mac, ip);

  // Open serial communications and wait for port to open:
  Serial.begin(9600);
  Serial.println("setup");

  randomSeed(analogRead(0));

  dht.begin();

  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }

  // Check for Ethernet hardware present
  if (Ethernet.hardwareStatus() == EthernetNoHardware) {
    Serial.println("Ethernet shield was not found.  Sorry, can't run without hardware. :(");
    while (true) {
      delay(1); // do nothing, no point running without Ethernet hardware
    }
  }
  if (Ethernet.linkStatus() == LinkOFF) {
    Serial.println("Ethernet cable is not connected.");
  }

  // start UDP
  Udp.begin(localPort);

  delay(10000);
  Serial.println("ready\n");

  while (true) {

    Udp.beginPacket(server, externalPort);
    Udp.write("HERE/0/1/0");
    Udp.endPacket();
    int packetSize = Udp.parsePacket();

    if (packetSize) {

      Serial.println("Answer received\n");
      Udp.read(packetBuffer, UDP_TX_PACKET_MAX_SIZE);

      addend1 = String(packetBuffer);
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

  delay(delayTime);

  parametersNumber = 0;

  temperature = dht.readTemperature();
  humidity = dht.readHumidity();
  luminosity = analogRead(A1);
  weight = analogRead(A2);
  randomNumber = random(1, 1001);

  for (i = 0; i < 5; i++) {
    if (random(2) == 1) {
      parameters[i] = 1;
      parametersNumber++;
    } else {
      parameters[i] = 0;
    }
  }

  message = "POST/" + String((char)(parametersNumber + 48)) + "/";

  if (parameters[0] == 1) {
    message += "TEMP/" + String(temperature, DEC) + "/";
  }
  if (parameters[1] == 1) {
    message += "HUMI/" + String(humidity, DEC) + "/";
  }
  if (parameters[2] == 1) {
    message += "LUMI/" + String(luminosity, DEC) + "/";
  }
  if (parameters[3] == 1) {
    message += "WEIG/" + String(weight, DEC) + "/";
  }
  if (parameters[4] == 1) {
    message += "RAND/" + String(randomNumber, DEC) + "/";
  }

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

  char support[message.length() + 1];
  message.toCharArray(support, message.length() + 1);

  Udp.beginPacket(server, externalPort);
  Udp.write(support);
  Udp.endPacket();

}
