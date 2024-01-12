#include <Adafruit_Fingerprint.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

LiquidCrystal_I2C lcd(0x27, 16, 2); // set the LCD address to 0x27 for a 16 chars and 2 line display

#if (defined(__AVR__) || defined(ESP8266)) && !defined(__AVR_ATmega2560__)

SoftwareSerial mySerial(2, 3);

#else
#define mySerial Serial1

#endif

Adafruit_Fingerprint finger = Adafruit_Fingerprint(&mySerial);

uint8_t id;
const int buttonPin = 4;
int buttonState = 0;
const int buttonPin2 = 5;
int buttonState2 = 0;
int scanMode = 0;

void setup()
{
  lcd.init();
  lcd.backlight();
  pinMode(buttonPin, INPUT);
  Serial.begin(9600);
  while (!Serial);  // For Yun/Leo/Micro/Zero/...
  delay(100);
  Serial.println("\n\nFingerprint Attendance System");

  finger.begin(57600);

  if (finger.verifyPassword()) {
    Serial.println("Found fingerprint sensor!");
  } else {
    Serial.println("Did not find fingerprint sensor :(");
    while (1) {
      delay(1);
    }
  }
  finger.getParameters();
  finger.getTemplateCount();
  if (finger.templateCount == 0) {
    Serial.println("Sensor doesn't contain any fingerprint data.");
  }
  else {
    Serial.println("Waiting for valid finger...");
    Serial.print("Sensor contains "); Serial.print(finger.templateCount); Serial.println(" templates");
  }
  Serial.println("Please select a mode");
  lcd.setCursor(6, 0);
  lcd.print("FAS");
  lcd.setCursor(5, 1);
  lcd.print("System");
  delay(2000);
  lcd.clear();
  lcd.setCursor(2, 0);
  lcd.print("Please select");
  lcd.setCursor(5, 1);
  lcd.print("a mode");
}

uint8_t readnumber(void) {
  uint8_t num = 0;

  while (num == 0) {
    while (! Serial.available());
    num = Serial.parseInt();
  }
  return num;



}

void loop()
{
  buttonState = digitalRead(buttonPin);
  buttonState2 = digitalRead(buttonPin2);
  if (buttonState2 == HIGH) {
    Check();
    Serial.println("Please select a mode");
    lcd.clear();
    lcd.setCursor(2, 0);
    lcd.print("Please select");
    lcd.setCursor(5, 1);
    lcd.print("a mode");

  }

  if (buttonState == HIGH) {
    getFingerprintID();
    Serial.println("Please select a mode");
    lcd.clear();
    lcd.setCursor(2, 0);
    lcd.print("Please select");
    lcd.setCursor(5, 1);
    lcd.print("a mode");

  }


}

void Check() {
  int ac = 0;
  int a = 0;
  Serial.println("Checking....");
  lcd.clear();
  lcd.setCursor(2, 0);
  lcd.print("Checking...");
  while (a == 0) {
    uint8_t p = finger.getImage();
    switch (p) {
      case FINGERPRINT_OK:
        Serial.println("Image taken");
        break;

      default:
        continue;
    }

    // OK success!

    p = finger.image2Tz();
    switch (p) {
      case FINGERPRINT_OK:
        break;

      default:
        continue;
    }

    // OK converted!
    p = finger.fingerSearch();
    if (p == FINGERPRINT_OK) {
      Serial.println("Found a print match!");
      Serial.println("This finger is already enrolled");
      Serial.println("Please use another finger");
      lcd.clear();
      lcd.setCursor(2, 0);
      lcd.print("Can't Enroll");
      lcd.setCursor(5, 1);
      lcd.print("Retry");
      delay(4000);


      a = 1;
      return finger.fingerID;
    } else if (p == FINGERPRINT_NOTFOUND) {
      Serial.println("Finger not enrolled");
      a = 1;
      ac = 1;
    }
  }
  if (ac == 1) {
    Enroll();
  }
}

void Enroll() {
  Serial.println("Ready to enroll a fingerprint!");
  lcd.clear();
  lcd.setCursor(2, 0);
  lcd.print("Entry ID");
  finger.getParameters();
  finger.getTemplateCount();
  id = finger.templateCount + 1;
  if (id == 0) {// ID #0 not allowed, try again!
    return;
  }
  Serial.print("Enrolling ID #");
  Serial.println(id);
  lcd.clear();
  lcd.setCursor(2, 0);
  lcd.print("Enrolment");
  lcd.setCursor(5, 1);
  lcd.print("Mode");

  while (!  getFingerprintEnroll() );
}

uint8_t getFingerprintEnroll() {

  int p = -1;
  Serial.print("Waiting for valid finger to enroll as #"); Serial.println(id);
  Serial.println("Place your Finger");
  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
    switch (p) {
      case FINGERPRINT_OK:
        Serial.println("Image taken");
        break;
      default:
        break;
    }
  }


  p = finger.image2Tz(1);
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image converted");
      break;
    case FINGERPRINT_IMAGEMESS:
      Serial.println("Image too messy");
      return p;
    case FINGERPRINT_PACKETRECIEVEERR:
      Serial.println("Communication error");
      return p;
    case FINGERPRINT_FEATUREFAIL:
      Serial.println("Could not find fingerprint features");
      return p;
    case FINGERPRINT_INVALIDIMAGE:
      Serial.println("Could not find fingerprint features");
      return p;
    default:
      Serial.println("Unknown error");
      return p;
  }

  Serial.println("Remove finger");
  delay(2000);
  p = 0;
  while (p != FINGERPRINT_NOFINGER) {
    p = finger.getImage();
  }
  Serial.print("ID "); Serial.println(id);
  p = -1;
  Serial.println("Place same finger again");
  while (p != FINGERPRINT_OK) {
    p = finger.getImage();
    switch (p) {
      case FINGERPRINT_OK:
        Serial.println("Image taken");
        break;
      default:
        break;
    }
  }


  p = finger.image2Tz(2);
  switch (p) {
    case FINGERPRINT_OK:
      Serial.println("Image converted");
      break;
    default:
      return p;
  }


  Serial.print("Creating model for #");  Serial.println(id);

  p = finger.createModel();
  if (p == FINGERPRINT_OK) {
    Serial.println("Prints matched!");
  } else if (p == FINGERPRINT_ENROLLMISMATCH) {
    Serial.println("Fingerprints did not match");
    return p;
  }

  Serial.print("ID "); Serial.println(id);
  p = finger.storeModel(id);
  if (p == FINGERPRINT_OK) {
    Serial.println("Stored!");
    lcd.clear();
    lcd.setCursor(4, 0);
    lcd.print("Stored");
    lcd.setCursor(5, 1);
    lcd.print("on ID#"); lcd.print(id);
    delay(4000);
  }
  else if (p == FINGERPRINT_BADLOCATION) {
    Serial.println("Could not store in that location");
    return p;
  }
  return true;
}

uint8_t getFingerprintID() {
  int a = 0;
  Serial.println("Attendance Mode:");
  Serial.println("Please place your Finger");
  lcd.clear();
  lcd.setCursor(2, 0);
  lcd.print("Attendance");
  lcd.setCursor(5, 1);
  lcd.print("Mode");
  while (a == 0) {
    uint8_t p = finger.getImage();
    switch (p) {
      case FINGERPRINT_OK:
        Serial.println("Image taken");
        break;

      default:
        continue;
    }

    // OK success!

    p = finger.image2Tz();
    switch (p) {
      case FINGERPRINT_OK:
        break;

      default:
        continue;
    }

    // OK converted!
    p = finger.fingerSearch();
    if (p == FINGERPRINT_OK) {
      Serial.println("Found a print match!");
      Serial.println("Found ID #" + String(finger.fingerID) + "   ");
      lcd.clear();
      lcd.setCursor(2, 0);
      lcd.print("Student ID#"); lcd.print(finger.fingerID);
      lcd.setCursor(3, 1);
      lcd.print("is Present");
      delay(4000);

      a = 1;
      return finger.fingerID;
    } else if (p == FINGERPRINT_NOTFOUND) {
      Serial.println("Did not find a match");
      lcd.clear();
      lcd.setCursor(2, 0);
      lcd.print("Did not find");
      lcd.setCursor(5, 1);
      lcd.print("a match");
    }
  }
}

// returns -1 if failed, otherwise returns ID #
int getFingerprintIDez() {
  uint8_t p = finger.getImage();
  if (p != FINGERPRINT_OK)  return -1;

  p = finger.image2Tz();
  if (p != FINGERPRINT_OK)  return -1;

  p = finger.fingerFastSearch();
  if (p != FINGERPRINT_OK)  return -1;

  // found a match!
  Serial.print("Found ID #"); Serial.print(finger.fingerID);
  Serial.print(" with confidence of "); Serial.println(finger.confidence);

  return finger.fingerID;
}
