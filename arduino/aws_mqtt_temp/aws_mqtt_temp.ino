#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <PubSubClient.h>
#include <time.h>
#include <FS.h>
#include <ArduinoJson.h>
#include <DHT.h>
#include <DHT_U.h>

#define PIN_DHT D4

ESP8266WebServer server;
//센서 연결
uint8_t pin_led = 13;   // D7 = GPIO13
DHT DHTsensor(PIN_DHT, DHT11); // 온습도 센서 연결
uint8_t button = 12; // D6 = GPIO12
int btn = 0; // 버튼 클릭 값을 저장할 변수
char* ch_value = "";

//AP - STA 모드 연결 위한 세팅
char* ssid = "YOUR_SSID"; //굳이 설정해주지 않아도 됌.
char* mySsid = "AirCon0000"; // AP ssid설정
char* _status = "WIFI_AP";

//AWS IoT를 위한 세팅
const char *thingId = "AirCon";          // 사물 이름 (thing ID) 
const char *host = "a2lewy1etbgc6q-ats.iot.ap-northeast-2.amazonaws.com"; // AWS IoT Core 주소
const char *topic = "outTopic/AirCon"; // 보낼 토픽

// 사물 인증서 (파일 이름: xxxxxxxxxx-certificate.pem.crt)
const char cert_str[] PROGMEM = R"EOF(
-----BEGIN CERTIFICATE-----
MIIDWjCCAkKgAwIBAgIVAM2FQpKX4WjtzB4qs8l23UtmQevCMA0GCSqGSIb3DQEB
CwUAME0xSzBJBgNVBAsMQkFtYXpvbiBXZWIgU2VydmljZXMgTz1BbWF6b24uY29t
IEluYy4gTD1TZWF0dGxlIFNUPVdhc2hpbmd0b24gQz1VUzAeFw0xOTA5MzAxMTA5
MDVaFw00OTEyMzEyMzU5NTlaMB4xHDAaBgNVBAMME0FXUyBJb1QgQ2VydGlmaWNh
dGUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC8tnMRQ9yXSe6mYPic
aQmolUlX+0qTGxhS9mqBy0zHKBQUt/f2t9i0I3ZYyjYYZhEF14EqdynavZZd/ck1
Ntmp4eq3A2MudHLGO0dY0xT414yKHkmSAUt/tVTCRX/MDrwJMWG0LG4Ky93FgPre
obQrbPyOOuHLRtVI8k/hfuSONE8DXH5wiGnKrvCYKe2w3ntaSS7G+iTnfMWbtXRO
yvpIFT1eZczs1PNKLbTxB1nrPWXPR2uIFGmXnK3B76FB4uDrIBXnoo0QXVhGhzb9
ZDv6li1lwaicNYa1M6GtNbjJHv2ZXO3OLINqTLB3eLgmdEn4xuzad2pL3SEXMv+v
dLaDAgMBAAGjYDBeMB8GA1UdIwQYMBaAFHu6EaW2QBsLi3J5rQJxEOp+9lzfMB0G
A1UdDgQWBBS9Q8L2kh0NmJyEvExzmKViDEBKNzAMBgNVHRMBAf8EAjAAMA4GA1Ud
DwEB/wQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAQEAHrQzl5GaZkUpw9mdaTvhhnwR
KyZp8B56H1JM7VlQxmt+SLPJWkseyoqKvOopyAM4JaQlb5qysZPf4cZZvdR9k3s8
b+sp9qzaFTj2KijxRVqni7aCVaFY1vS0iRWMDW1rnEhj896l5Fs0Z+ejySJtbJtY
7FMASKK1rvUtgPnEneNwZqvMp5BOKpjsJOsEmkeLaLjdpm7D+JMJ+JpUkIEogizg
XitjfkxjWRzccDmBKsLAvN9FLDQy/lnV0ivkfvC4GY+dbSFbgeB4WRxd10/SywRB
JPgU3K58sx2zlBl/CYX9DdjkIWEp5uup1dBLxiKoIVehsr7WaN7EhgJAWFXcWw==
-----END CERTIFICATE-----
)EOF";
// 사물 인증서 프라이빗 키 (파일 이름: xxxxxxxxxx-private.pem.key)
const char key_str[] PROGMEM = R"EOF(
-----BEGIN RSA PRIVATE KEY-----
MIIEowIBAAKCAQEAvLZzEUPcl0nupmD4nGkJqJVJV/tKkxsYUvZqgctMxygUFLf3
9rfYtCN2WMo2GGYRBdeBKncp2r2WXf3JNTbZqeHqtwNjLnRyxjtHWNMU+NeMih5J
kgFLf7VUwkV/zA68CTFhtCxuCsvdxYD63qG0K2z8jjrhy0bVSPJP4X7kjjRPA1x+
cIhpyq7wmCntsN57Wkkuxvok53zFm7V0Tsr6SBU9XmXM7NTzSi208QdZ6z1lz0dr
iBRpl5ytwe+hQeLg6yAV56KNEF1YRoc2/WQ7+pYtZcGonDWGtTOhrTW4yR79mVzt
ziyDakywd3i4JnRJ+Mbs2ndqS90hFzL/r3S2gwIDAQABAoIBAGsB4hnGK022ZJb/
obm/bfSkQmSbX9wunwpcJGTZDP5ZYZml//tsoHr1KOSMNUR8i+aOHvYfgCJDIwLF
J9T+90n3iwqf5xuueG6jqn9ZoijAwTaImhsqlM8j2Z4XnrHtIP7te4OZNgT1ORoH
Wznm461ELanRBbwovESS8mzhcWSYliCeepaiBX9nkuGzt3y0VEoDjAgj/jWs1LhF
z7DGXnRlI8j1siot4Wbrx1k6nsUawre21aGNp7mHCKsEM5KSQzoLTEvwzLRKw2aX
KAPynPIqMJ9ox/g8Moy+hdvtYpyqJzvAIcGIjdV3Iarm60PcsuiNk8Af5Nv+bein
6usZ/0ECgYEA89G51UMYXdXJPum8cupQof5GceFhTvE3aJJCKKEiD1IVV9dm1xOQ
AbSZNFfuw2qsqeu4nc7PyrbgdZi3hj+YoBO42xTVzozVjGHr3OOWj5cK1HkZgGH1
YO8qW+0d394CiI0hGlC5npi6cnvDaxZKaVEDJJJ1jl08YCmBG6nIEhECgYEAxiPz
PLodnWxv9pZzXkDUr6qSOvogFlU0YwXEo2jXOra1X24agfP/VCuxFr0Ksz52zk5g
w3+Egw2MWt3A9BiOPyiQHn0Db71/Ai5MhtCj9nOPS8wXRu49JwVWJZct3C7uvn8S
2u6kid+Fb1yS6URofbXbF4EI65IFOE2debGuK1MCgYEApMjY5Oac4EZt5gRI5g2x
A1r8N1lQz+69OQKojhchEuOyIxUknzXMfSqW5MaNSyucmJcExjsROvtQRoo5dxUi
TQYnSIITYq7SYTeJFlgG2PMHUcSq1JbgJWxe35QpPbUpVyqy9bsbSLIGSPuWfpip
H/atvhjyNI6VEJTcelrMKlECgYBKyc0I8fM1tXV5bQZ04CGngMmbS0gLRfP1IHs9
dtguiEJjl+qXfKK2gZthSs+cIoqzinAJursowFdnAObQ++fNPSFQ8Lz4U116VEF8
bYpF/w5qfMmYc1pPvTrsC2k6/9gLvWk/i+49QEpcY2Psem66bIcbExBr4nzn6Jv1
EDT+kwKBgHxWiOv9OvGk8Mk1v/gyQcSsV23NH5QodiDDt+/ojcHYBkxaACS8vy0B
Dk1lte8CODAumNwdihSyiCHVUV3IQgMTYv/JrjDya4UAX7VBXNV4Y4aQNacSL2MU
sE1CBEG27jyPNaLeAluRfn7BxDR6jwzsu4LO/d0uJXxcDjj/3kU+
-----END RSA PRIVATE KEY-----
)EOF";
// Amazon Trust Services(ATS) 엔드포인트 CA 인증서 (서버인증 > "RSA 2048비트 키: Amazon Root CA 1" 다운로드)
const char ca_str[] PROGMEM = R"EOF(
-----BEGIN CERTIFICATE-----
MIIDQTCCAimgAwIBAgITBmyfz5m/jAo54vB4ikPmljZbyjANBgkqhkiG9w0BAQsF
ADA5MQswCQYDVQQGEwJVUzEPMA0GA1UEChMGQW1hem9uMRkwFwYDVQQDExBBbWF6
b24gUm9vdCBDQSAxMB4XDTE1MDUyNjAwMDAwMFoXDTM4MDExNzAwMDAwMFowOTEL
MAkGA1UEBhMCVVMxDzANBgNVBAoTBkFtYXpvbjEZMBcGA1UEAxMQQW1hem9uIFJv
b3QgQ0EgMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALJ4gHHKeNXj
ca9HgFB0fW7Y14h29Jlo91ghYPl0hAEvrAIthtOgQ3pOsqTQNroBvo3bSMgHFzZM
9O6II8c+6zf1tRn4SWiw3te5djgdYZ6k/oI2peVKVuRF4fn9tBb6dNqcmzU5L/qw
IFAGbHrQgLKm+a/sRxmPUDgH3KKHOVj4utWp+UhnMJbulHheb4mjUcAwhmahRWa6
VOujw5H5SNz/0egwLX0tdHA114gk957EWW67c4cX8jJGKLhD+rcdqsq08p8kDi1L
93FcXmn/6pUCyziKrlA4b9v7LWIbxcceVOF34GfID5yHI9Y/QCB/IIDEgEw+OyQm
jgSubJrIqg0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMC
AYYwHQYDVR0OBBYEFIQYzIU07LwMlJQuCFmcx7IQTgoIMA0GCSqGSIb3DQEBCwUA
A4IBAQCY8jdaQZChGsV2USggNiMOruYou6r4lK5IpDB/G/wkjUu0yKGX9rbxenDI
U5PMCCjjmCXPI6T53iHTfIUJrU6adTrCC2qJeHZERxhlbI1Bjjt/msv0tadQ1wUs
N+gDS63pYaACbvXy8MWy7Vu33PqUXHeeE6V/Uq2V8viTO96LXFvKWlJbYK8U90vv
o/ufQJVtMVT8QtPHRh8jrdkPSHCa2XV4cdFyQzR1bldZwgJcJmApzyMZFo6IQ6XU
5MsI+yMRQ+hDKXJioaldXgjUkK642M4UwtBV8ob2xJNDd2ZhwLnoQdeXeGADbkpy
rqXRfboQnoZsG4q5WTP468SQvvG5
-----END CERTIFICATE-----
)EOF";

//AWS IoT 콜백 함수
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  char message[length];
  for (int i = 0; i < length; i++) {
    message[i] = (char)payload[i];
  }
  Serial.print(message);
  Serial.println();
  
//  int ret = strcmp(message, "ON");
//  if(ret == 0){
//    digitalWrite(pin_red_led, HIGH);
//  }else{
//    digitalWrite(pin_red_led, LOW);
//  }
}

//AWS IoT 인증서 세팅
X509List ca(ca_str);
X509List cert(cert_str);
PrivateKey key(key_str);
WiFiClientSecure wifiClient;
PubSubClient client(host, 8883, callback, wifiClient); //set  MQTT port number to 8883 as per //standard

void reconnect() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (client.connect(thingId)) {
      Serial.println("connected");
      // Once connected, publish an announcement...
      client.publish(topic, ch_value);
      // ... and resubscribe
      client.subscribe("inTopic/AirCon");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");

      char buf[256];
      wifiClient.getLastSSLError(buf,256);
      Serial.print("WiFiClientSecure SSL error: ");
      Serial.println(buf);

      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

// 우리나라 시간으로 세팅
void setClock() {
  configTime(3 * 3600, 0, "pool.ntp.org", "time.nist.gov");

  Serial.print("Waiting for NTP time sync: ");
  time_t now = time(nullptr);
  while (now < 8 * 3600 * 2) {
    delay(500);
    Serial.print(".");
    now = time(nullptr);
  }
  Serial.println("");
  struct tm timeinfo;
  gmtime_r(&now, &timeinfo);
  Serial.print("Current time: ");
  Serial.print(asctime(&timeinfo));
}


//AP모드 진입 시 ip, gateway, subnetmask 설정
IPAddress local_ip(192,168,4,2);
IPAddress gateway(192,168,4,1);
IPAddress netmask(255,255,255,0);

//HTML 레이아웃
char webpage[] PROGMEM = R"=====(
<html>
<head>
</head>
<body>
  <form>
    <fieldset>
      <div>
        <label for="ssid">SSID</label>      
        <input value="" id="ssid" placeholder="SSID">
      </div>
      <div>
        <label for="password">PASSWORD</label>
        <input type="password" value="" id="password" placeholder="PASSWORD">
      </div>
      <div>
        <button class="primary" id="savebtn" type="button" onclick="myFunction();">SAVE</button>
      </div>
    </fieldset>
  </form>
</body>
<script>
function myFunction()
{
  console.log("button was clicked!");
  var ssid = document.getElementById("ssid").value;
  var password = document.getElementById("password").value;
  var data = {ssid:ssid, password:password};
  var xhr = new XMLHttpRequest();
  var url = "/settings.html";
  xhr.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      // Typical action to be performed when the document is ready:
      if(xhr.responseText != null){
        console.log(xhr.responseText);
      }
    }
  };
  xhr.open("POST", url, true);
  console.log(url);
  xhr.send(JSON.stringify(data));
};
</script>
</html>
)=====";

void setup()
{
  pinMode(pin_led, OUTPUT); // D7에 led 연결
  DHTsensor.begin();//온습도 센서
  pinMode(button,INPUT_PULLUP); // D2에 버튼 연결
  Serial.begin(115200); // 시리얼 통신 시작
  SPIFFS.begin(); // 파일시스템 시작
  wifiConnect(); // 와이파이 연결시도
  server.on("/",[](){server.send_P(200,"text/html", webpage);});
  server.on("/toggle",toggleLED);
  server.on("/settings.html", HTTP_POST, handleSettingsUpdate);
  server.begin();
}
//mqtt 메시지 보내는 것
long lastMsg = 0;
char msg[50];

void loop()
{    
  int btn = digitalRead(button);
//  delay(500);
  if(btn == 0){ // 파일에 저장되어있는 값을 없애줘야함.
    DynamicJsonBuffer jSTABuffer;
    JsonObject& jObject = jSTABuffer.parseObject("");
    File configFile = SPIFFS.open("/config.json", "w");
    if (!configFile) {
      Serial.println("file open failed");
    }
    jObject.printTo(configFile);  
    configFile.close();
    client.disconnect();
    wifiConnect();
  }

  if(_status == "WIFI_STA"){
    
    if (!client.connected()) {
      reconnect();
    }
    client.loop();
    
    long now = millis();
      
    if (now - lastMsg > 5000) { //5초마다 메시지를 보내겠다.
      lastMsg = now; // 현재 시간을 저장
      float temp = DHTsensor.readTemperature();
      float humidity = DHTsensor.readHumidity();
      
      StaticJsonBuffer<200> jsonBuffer;
      JsonObject& root = jsonBuffer.createObject();
      root["tempvalue"] = temp;
      root["humivalue"] = humidity;

      root.printTo(msg);
//      snprintf (msg, 75, ch_value);
      Serial.print("Publish message: ");
      Serial.println(msg);
      client.publish(topic, msg);
    }
  }
  
  server.handleClient();
}

void handleSettingsUpdate()
{
  String data = server.arg("plain");
  Serial.print("data : ");
  Serial.println(data);
  DynamicJsonBuffer jBuffer;
  JsonObject& jObject = jBuffer.parseObject(data);
  
  File configFile = SPIFFS.open("/config.json", "w");
  if (!configFile) {
    Serial.println("file open failed");
  }
  jObject.printTo(configFile);  
  configFile.close();

  server.send(200, "application/json", "{\"status\" : \"ok\"}");
  delay(500);
  
  wifiConnect();
  
}

void wifiConnect()
{
  //reset networking
  WiFi.softAPdisconnect(true); // AP모드 연결해제
  WiFi.disconnect();          // wifi 연결 해제
  delay(1000);
  //check for stored credentials
  if(SPIFFS.exists("/config.json")){ // 파일시스템에 config.json의 존재유무
    const char * _ssid = "", *_pass = ""; // ssid, password 변수선언
    File configFile = SPIFFS.open("/config.json", "r"); // config.json 파일을 읽음 열겠다.
    if(configFile){ //존재한다면
      size_t size = configFile.size(); // 파일의 크기를 체크
      std::unique_ptr<char[]> buf(new char[size]); //(?)
      configFile.readBytes(buf.get(), size); // buf.get으로 가져온 파일의 데이터 크기만큼 읽겠다. 선언(?)
      configFile.close(); // 파일시스템 닫기

      DynamicJsonBuffer jsonBuffer; // json 파싱하기 위한 클래스
      JsonObject& jObject = jsonBuffer.parseObject(buf.get()); // json을 파싱하기 위한 것
      if(jObject.success()) // 파싱하는데 성공했을 시
      {
        _ssid = jObject["ssid"];
        _pass = jObject["password"];
        Serial.print("_ssid : ");
        Serial.println(_ssid);
        Serial.print("_pass :");
        Serial.println(_pass);
        WiFi.mode(WIFI_STA); //wifi 모드를 station 모드로 선언
        WiFi.begin(_ssid, _pass); //입력한 ssid, password로 와이파이 시작
        unsigned long startTime = millis(); // 프로그램 시작 후 지난 시간 변수를 저장
        while (WiFi.status() != WL_CONNECTED) // 와이파이가 연결이 안되었다면
        {
          delay(500);
          Serial.print(".");
          digitalWrite(pin_led,!digitalRead(pin_led));
          if ((unsigned long)(millis() - startTime) >= 10000) break; // (프로그램 시작 후 지난 시간 - 이 함수를 실행시켰을 때의 시간)이 5초보다 커질경우 while문을 빠져나온다.
        }
      }
    }
  }else{
    Serial.println("not exists");
  }

  if (WiFi.status() == WL_CONNECTED) // 와이파이가 연결되었을 때(STA모드)
  {
    _status = "WIFI_STA";
    digitalWrite(pin_led,LOW); // LED ON

    //publish
    wifiClient.setTrustAnchors(&ca);
    wifiClient.setClientRSACert(&cert, &key);
    Serial.println("Certifications and key are set");
    
    setClock();
    client.setCallback(callback);
  } else // 연결되지 못했을 때 
  {
    _status = "WIFI_AP";
    WiFi.mode(WIFI_AP); // APmode로 설정
    WiFi.softAPConfig(local_ip, gateway, netmask); // 미리 선언해둔 ip, gateway, subnetmask 지정
    WiFi.softAP(mySsid); // 개방형인 AP모드로 와이파이 실행
    digitalWrite(pin_led,HIGH);
  }
  Serial.println("");
  WiFi.printDiag(Serial); // wifi 정보 serial통신으로 확인
}

void toggleLED()
{
  digitalWrite(pin_led,!digitalRead(pin_led)); // 핀의 값을 읽어 HIGH이면 LOW, LOW면 HIGH으로 변경
  server.send(204,"");
}
