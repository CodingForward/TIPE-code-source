#include <WiFiNINA.h>
#include "arduino_secrets.h" 
#include "carte_3.h"

int status = WL_IDLE_STATUS;

char serverAddress[] = "78.193.132.197";
int serverPort = 80;
WiFiClient client;

void setup()
{
  Serial.begin(9600);
  //while (!Serial);

  if (client.connected())
  {
    Serial.println("Stop connection.");
    client.print("stop");
    client.stop();
  }

  if (WiFi.status() == WL_NO_MODULE)
  {
    Serial.println("Communication with WiFi module failed!");
    while (true);
  }

  String fv = WiFi.firmwareVersion();
  
  if (fv < "1.0.0") Serial.println("Please upgrade the firmware");
  
  while (status != WL_CONNECTED)
  {
    Serial.print("Attempting to connect to WEP network, SSID: ");
    Serial.println(SECRET_SSID);

    #ifdef WEP
      status = WiFi.begin(SECRET_SSID, KEY_INDEX, SECRET_PASS);
    #else
      status = WiFi.begin(SECRET_SSID, SECRET_PASS);
    #endif
    
    delay(1000);
  }

  Serial.println("You're connected to the network");
  printCurrentNet();
  printWifiData();
  Serial.println("");

  setupClient();
  delay(1000);
}

void setupClient()
{
  Serial.println("Tentative de connection au serveur " + String(serverAddress));
  
  if (client.connect(serverAddress, serverPort))
  {
    Serial.println("Connexion effectuée.");
    client.print("arduino");
    String msg = "card_id " + String(CARTE_ID) + " " + createRequest();
    client.print(msg);
    Serial.println(msg);
  }
  else
  {
    Serial.println("Connexion échouée.");
  }
}

String createRequest()
{
  String msg = "";
  msg += "X: " + String(POS_X) + ", ";
  msg += "Y: " + String(POS_Y) + ", ";
  msg += "RSSI: " + String(WiFi.RSSI()) + ", ";
  return msg;
}

void loop()
{
  if (client.available())
  {
    Serial.print("Le serveur a envoye : \"");
    
    while (client.available())
    {
      char c = client.read();
      Serial.print(c);
    }

    Serial.println("\"");
  }

  if (client.connected())
  {
    String msg = createRequest();
    client.print(msg);
    Serial.println(msg);
  }
  
  delay(50);
}

void printWifiData()
{
  // print your board's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);
  Serial.println(ip);

  // print your MAC address:
  byte mac[6];
  WiFi.macAddress(mac);
  Serial.print("MAC address: ");
  printMacAddress(mac);
}

void printRSSI()
{
  Serial.print("signal strength (RSSI):");
  Serial.println(WiFi.RSSI());
}

void printCurrentNet()
{
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print the MAC address of the router you're attached to:
  byte bssid[6];
  WiFi.BSSID(bssid);
  Serial.print("BSSID: ");
  printMacAddress(bssid);

  // print the received signal strength:
  printRSSI();

  // print the encryption type:
  byte encryption = WiFi.encryptionType();
  Serial.print("Encryption Type:");
  Serial.println(encryption, HEX);
  Serial.println();
}

void printMacAddress(byte mac[])
{
  for (int i = 5; i >= 0; i--) {
    if (mac[i] < 16) {
      Serial.print("0");
    }
    Serial.print(mac[i], HEX);
    if (i > 0) {
      Serial.print(":");
    }
  }
  Serial.println();
}
