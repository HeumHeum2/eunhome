

uint8_t button = 0;

void setup() {
  Serial.begin(115200);
  pinMode(button, INPUT_PULLUP);
}

// the loop function runs over and over again forever
void loop() {
  int btn = digitalRead(button);
  Serial.println(btn);
  delay(100);
//  int analog = analogRead(A0);
//  Serial.println(analog);
//  delay(100);  // Wait for a while
}
