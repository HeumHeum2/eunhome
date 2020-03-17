#include <Servo.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <PubSubClient.h>
#include <time.h>
#include <FS.h>
#include <ArduinoJson.h>

#define RLOAD 10.0
#define RZERO 76.63
#define PARA 116.6020682
#define PARB 2.769034857

Servo servo;

ESP8266WebServer server;
//센서 연결
uint8_t pin_led = 13; // D7 GPIO13
uint8_t button = 0; // D8 GPIO0 INPUT PULLUP
uint8_t servoPin = 14; // D5 GPIO14
uint8_t firePin = 12; // D6 GPIO12

char* ch_value = ""; // 센서상태를 메시지로 보낼 변수
int pos = 0; // 서보모터 각도 설정
int gas_value;

//AP - STA 모드 연결 위한 세팅
char* ssid = "YOUR_SSID"; //굳이 설정해주지 않아도 됌.
char* mySsid = "GasValve0000"; // AP ssid설정
char* _status = "WIFI_AP";

//AWS IoT를 위한 세팅
const char *thingId = "GasValve";          // 사물 이름 (thing ID) 
const char *host = "a2lewy1etbgc6q-ats.iot.ap-northeast-2.amazonaws.com"; // AWS IoT Core 주소
const char *topic = "outTopic/GasValve0000"; // 보낼 토픽


const char cert_str[] PROGMEM = R"EOF(
// 사물 인증서 (파일 이름: xxxxxxxxxx-certificate.pem.crt)
)EOF";

const char key_str[] PROGMEM = R"EOF(
// 사물 인증서 프라이빗 키 (파일 이름: xxxxxxxxxx-private.pem.key)
)EOF";

const char ca_str[] PROGMEM = R"EOF(
// Amazon Trust Services(ATS) 엔드포인트 CA 인증서 (서버인증 > "RSA 2048비트 키: Amazon Root CA 1" 다운로드)
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
  
  int ret = strcmp(message, "ON");
  if(ret == 0){
    pos = 90;
  }else{
    pos = 0;
  }
  
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
      client.subscribe("inTopic/GasValve0000");
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
  pinMode(button,INPUT_PULLUP); // D2에 버튼 연결
  pinMode(firePin, INPUT);
  
  servo.attach(servoPin);
 
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
  servo.write(pos);   // pos = 0 -> OFF // pos = 90 -> ON
  
  if(digitalRead(firePin) == 1){
    pos = 0;
  }
  
  int btn = digitalRead(button);
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
    gas_value = analogRead(0);
    gas_value = (1023./(float)gas_value) * 5. - 1.* RLOAD;
    float Resistance;
    Resistance = gas_value;
    float PPM;
    PPM = PARA * pow((Resistance/RZERO), -PARB);
    
    if (!client.connected()) {
      reconnect();
    }
    client.loop();
    
    long now = millis();
    if (now - lastMsg > 5000) { //5초마다 메시지를 보내겠다.
      lastMsg = now; // 현재 시간을 저장
      StaticJsonBuffer<200> jsonBuffer;
      JsonObject& root = jsonBuffer.createObject();
      root["gasvalue"] = PPM; // 가스농도 저장
      if(pos == 0){
        ch_value = "OFF";
      }else if (pos == 90){
        ch_value = "ON";
      }
      root["gasstatus"] = ch_value; // 가스벨브 온/오프 저장
      
      root.printTo(msg);
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
