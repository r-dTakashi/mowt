/*
 *  eltres_sample.ino - 固定値を送信するサンプルプログラム
 *  Copyright 2022 CRESCO DIGITAL TECHNOLOGIES, LTD.
 */
/**
 * 説明
 *   本プログラムは、送信回数に応じた値を格納したペイロードのELTRES送信を5回実施します。
 *   ELTRESエラー時を除き、初回GNSS電波受信後にELTRES送信を開始し、5回実施後に正常終了状態でループします。
 *   ELTRESエラー時を除き、正常終了状態になるまでGGA情報をシリアルモニタへ出力します。
 *   ELTRESエラー時は異常終了状態でループします。
 *   LEDの動きについては補足事項を確認してください。i
 * 
 * 動作環境
 *   Arduino IDEを利用するSpresense Arduino 開発環境(2.3.0)上で利用できます。
 *   以下のArduinoライブラリを利用します。
 *     ・ELTRESアドオンボード用ライブラリ(1.1.0)
 * 
 * 利用方法
 *   1. ELTRESアドオンボード用ライブラリをArduino IDEにインストールします。
 *   2. 本プログラムをArduino IDEで開き、Spresenseに書き込みます。
 *   3. 電源を入れてプログラムを起動します。
 *      シリアルモニタを確認したい場合、Arduino IDEと接続し、
 *      Arduino IDEの上部メニューから「ツール」->「シリアルモニタ」を選択し、シリアルモニタを起動します。
 *      ・GGA情報は"[gga]"から始まる出力で確認できます。
 * 
 * 注意事項
 *   本プログラムは、ELTRES送信間隔が1分であることを前提としてます。
 *   3分契約の場合、EltresAddonBoard.begin()の引数 ELTRES_BOARD_SEND_MODE_1MIN を
 *   ELTRES_BOARD_SEND_MODE_3MIN に変更してください。
 * 
 * 補足事項
 *   サンプルプログラムは、下記のようにLEDを用いて状態を表現します。
 *     LED0：[プログラム状態] 起動中：点灯、正常・異常終了：消灯
 *     LED1：[GNSS電波状態] 受信中：点灯、未受信：消灯
 *     LED2：[ELTRES状態] 送信中：点灯、待機中：消灯
 *     LED3：[エラー状態] GNSS受信エラー：点滅、ELTRESエラー：点灯、エラー無し：消灯
 *           ※GNSS受信エラー発生時はGNSS電波受信するまで待ち続けます。（LED0は点灯、LED3は点滅）
 *           ※ELTRESエラー発生時は異常終了状態でループします。（LED0は消灯、LED3は点灯）
 */
#include <EltresAddonBoard.h>
#include <Wire.h>
#include "Adafruit_SHT31.h"
#include <DFRobot_SHT3x.h>

const int WIND_INPUT = 0;
DFRobot_SHT3x   sht3x;

#define SHT31_address 0x44
int Stemp;int SRH;
float temp;float RH;
byte readbuffer[6];

// PIN定義：LED(プログラム状態)
#define LED_RUN PIN_LED0
// PIN定義：LED(GNSS電波状態)
#define LED_GNSS PIN_LED1
// PIN定義：LED(ELTRES状態)
#define LED_SND PIN_LED2
// PIN定義：LED(エラー状態)
#define LED_ERR PIN_LED3

// プログラム内部状態：初期状態
#define PROGRAM_STS_INIT      (0)
// プログラム内部状態：起動中
#define PROGRAM_STS_RUNNING   (1)
// プログラム内部状態：終了処理中
#define PROGRAM_STS_STOPPING  (2)
// プログラム内部状態：終了
#define PROGRAM_STS_STOPPED   (3)

// プログラム内部状態
int program_sts = PROGRAM_STS_INIT;
// GNSS電波受信タイムアウト（GNSS受信エラー）発生フラグ
bool gnss_recevie_timeout = false;
// 点滅処理で最後に変更した時間
uint64_t last_change_blink_time = 0;
// イベント通知での送信直前通知（5秒前）受信フラグ
bool event_send_ready = false;
// イベント通知でのアイドル状態受信フラグ
bool event_idle = false;
// 送信回数
int send_count = 0;
// ペイロードデータ格納場所
uint8_t payload[16];

/**
 * @brief イベント通知受信コールバック
 * @param event イベント種別
 */
void eltres_event_cb(eltres_board_event event) {
  switch (event) {
  case ELTRES_BOARD_EVT_GNSS_TMOUT:
    // GNSS電波受信タイムアウト
    Serial.println("gnss wait timeout error.");
    gnss_recevie_timeout = true;
    break;
  case ELTRES_BOARD_EVT_IDLE:
    // アイドル状態
    Serial.println("waiting sending timings.");
    digitalWrite(LED_SND, LOW);
    event_idle = true;
    break;
  case ELTRES_BOARD_EVT_SEND_READY:
    // 送信直前通知（5秒前）
    Serial.println("Shortly before sending, so setup payload if need.");
    event_send_ready = true;
    break;
  case ELTRES_BOARD_EVT_SENDING:
    // 送信開始
    Serial.println("start sending.");
    digitalWrite(LED_SND, HIGH);
    break;
  case ELTRES_BOARD_EVT_GNSS_UNRECEIVE:
    // GNSS電波未受信
    Serial.println("gnss wave has not been received.");
    digitalWrite(LED_GNSS, LOW);
    break;
  case ELTRES_BOARD_EVT_GNSS_RECEIVE:
    // GNSS電波受信
    Serial.println("gnss wave has been received.");
    digitalWrite(LED_GNSS, HIGH);
    gnss_recevie_timeout = false;
    break;
  case ELTRES_BOARD_EVT_FAULT:
    // 内部エラー発生
    Serial.println("internal error.");
    break;
  }
}

/**
 * @brief GGA情報受信コールバック
 * @param gga_info GGA情報のポインタ
 */
void gga_event_cb(const eltres_board_gga_info *gga_info) {
  Serial.print("[gga]");
  if (gga_info->m_pos_status) {
    // 測位状態
    // GGA情報をシリアルモニタへ出力
    Serial.print("utc: ");
    Serial.println((const char *)gga_info->m_utc);
    Serial.print("lat: ");
    Serial.print((const char *)gga_info->m_n_s);
    Serial.print((const char *)gga_info->m_lat);
    Serial.print(", lon: ");
    Serial.print((const char *)gga_info->m_e_w);
    Serial.println((const char *)gga_info->m_lon);
    Serial.print("pos_status: ");
    Serial.print(gga_info->m_pos_status);
    Serial.print(", sat_used: ");
    Serial.println(gga_info->m_sat_used);
    Serial.print("hdop: ");
    Serial.print(gga_info->m_hdop);
    Serial.print(", height: ");
    Serial.print(gga_info->m_height);
    Serial.print(" m, geoid: ");
    Serial.print(gga_info->m_geoid);
    Serial.println(" m");
  } else {
    // 非測位状態
    // "invalid data"をシリアルモニタへ出力
    Serial.println("invalid data.");
  }
}

/**
 * @brief setup()関数
 */
void setup() {
  // シリアルモニタ出力設定
  Serial.begin(115200);
  Wire.begin();

  while (sht3x.begin() != 0) {
    Serial.println("Failed to DFRobot connection");
    delay(1000);
  }

  Serial.print("Chip serial number");
  Serial.println(sht3x.readSerialNumber());
  if(!sht3x.softReset()) Serial.println("Failed to Initialize the chip....");

  // LED初期設定
  pinMode(LED_RUN, OUTPUT);
  digitalWrite(LED_RUN, HIGH);
  pinMode(LED_GNSS, OUTPUT);
  digitalWrite(LED_GNSS, LOW);
  pinMode(LED_SND, OUTPUT);
  digitalWrite(LED_SND, LOW);
  pinMode(LED_ERR, OUTPUT);
  digitalWrite(LED_ERR, LOW);

  // ELTRES起動処理
  eltres_board_result ret = EltresAddonBoard.begin(ELTRES_BOARD_SEND_MODE_1MIN,eltres_event_cb, gga_event_cb);
  if (ret != ELTRES_BOARD_RESULT_OK) {
    // ELTRESエラー発生
    digitalWrite(LED_RUN, LOW);
    digitalWrite(LED_ERR, HIGH);
    program_sts = PROGRAM_STS_STOPPED;
    Serial.print("cannot start eltres board (");
    Serial.print(ret);
    Serial.println(").");
  } else {
    // 正常
    program_sts = PROGRAM_STS_RUNNING;
  }
}

/**
 * @brief loop()関数
 */
void loop() {
  uint32_t gnss_time;
  int32_t remaining;
  
  float val = analogRead( WIND_INPUT );
  float temp2 = sht3x.getTemperatureC();

  Wire.beginTransmission(SHT31_address);
     Wire.write(0x24);
     Wire.write((byte)0x00);
     Wire.endTransmission();
     Wire.requestFrom(SHT31_address, 6);
     readbuffer[0] = Wire.read();
     readbuffer[1] = Wire.read();
     readbuffer[2] = Wire.read(); // CRC
     readbuffer[3] = Wire.read();
     readbuffer[4] = Wire.read();
     readbuffer[5] = Wire.read(); // CRC
     if (readbuffer[2] == crc8(readbuffer)) {
         Stemp = readbuffer[0] << 8 ;
         Stemp = Stemp + readbuffer[1];
         temp = -45 + Stemp * 175.0 / 65535.0;
     }
     if (readbuffer[5] == crc8(readbuffer+3)) {
         SRH = readbuffer[3] << 8 ;
         SRH = SRH + readbuffer[4];
         RH = 100.0 * SRH / 65535.0;
     }
     //Serial.println(" temp_0x44 = " + String(temp,1) + "'C ");

  Serial.print("temp : ");
  Serial.println(temp);
  Serial.print("temp2 : ");
  Serial.println(temp2);
  Serial.print("val : ");
  Serial.println(val);

  switch (program_sts) {
    case PROGRAM_STS_RUNNING:
      // プログラム内部状態：起動中
      if (gnss_recevie_timeout) {
        // GNSS電波受信タイムアウト（GNSS受信エラー）時の点滅処理
        uint64_t now_time = millis();
        if ((now_time - last_change_blink_time) >= 1000) {
          last_change_blink_time = now_time;
          bool set_value = digitalRead(LED_ERR);
          bool next_value = (set_value == LOW) ? HIGH : LOW;
          digitalWrite(LED_ERR, next_value);
        }
      } else {
        digitalWrite(LED_ERR, LOW);
      }

      if (event_send_ready) {
        // 送信直前通知時の処理
        event_send_ready = false;

        memset(payload, 0x00, sizeof(payload));
        // 送信ペイロードの設定
        if( send_count % 2 == 0 ){
          payload[0] = 0x80;
          uint32_t raw;
          memcpy(&raw, &temp, sizeof(uint32_t));
          payload[1] = (uint8_t)((raw>>24) & 0xff );
          payload[2] = (uint8_t)((raw>>16) & 0xff );
          payload[3] = (uint8_t)((raw>>8) & 0xff );
          payload[4] = (uint8_t)((raw>>0) & 0xff );
          memcpy(&raw, &val, sizeof(uint32_t));
          payload[5] = (uint8_t)((raw>>24) & 0xff );
          payload[6] = (uint8_t)((raw>>16) & 0xff );
          payload[7] = (uint8_t)((raw>>8) & 0xff );
          payload[8] = (uint8_t)((raw>>0) & 0xff );
        }
        else{
          payload[0] = 0x82;
          uint32_t raw;
          memcpy(&raw, &temp2, sizeof(uint32_t));
          payload[1] = (uint8_t)((raw>>24) & 0xff );
          payload[2] = (uint8_t)((raw>>16) & 0xff );
          payload[3] = (uint8_t)((raw>>8) & 0xff );
          payload[4] = (uint8_t)((raw>>0) & 0xff );
        }
        
        EltresAddonBoard.set_payload(payload);
        send_count += 1;
      }
      
      if (event_idle) {
        // 送信完了時の処理
        event_idle = false;
        
        // GNSS時刻(epoch秒)の取得
        EltresAddonBoard.get_gnss_time(&gnss_time);
        Serial.print("gnss time: ");
        Serial.print(gnss_time);
        Serial.println(" sec");
        // 次送信までの残り時間の取得
        EltresAddonBoard.get_remaing_time(&remaining);
        Serial.print("remaining time: ");
        Serial.print(remaining);
        Serial.println(" sec");
      }
      break;
      
    case PROGRAM_STS_STOPPING:
      // プログラム内部状態：終了処理中
      // ELTRES停止処理(注意：この処理を行わないとELTRESが自動送信し続ける)
      EltresAddonBoard.end();
      digitalWrite(LED_RUN, LOW);
      digitalWrite(LED_GNSS, LOW);
      program_sts = PROGRAM_STS_STOPPED;
      break;
      
    case PROGRAM_STS_STOPPED:
      // プログラム内部状態：終了
      break;
  }
  // 次のループ処理まで100ミリ秒待機
  delay(100);
}

unsigned char table[256] =
{
 0, 94, 188, 226, 97, 63, 221, 131, 194, 156, 126, 32, 163, 253, 31, 65,157, 195, 33, 127, 252, 162, 64, 30, 95, 1, 227, 189, 62, 96, 130, 220, 35, 125, 159, 193, 66, 28, 254, 160, 225, 191, 93, 3, 128, 222, 60, 98, 190, 224, 2, 92, 223, 129, 99, 61, 124, 34, 192, 158, 29, 67, 161, 255, 70, 24, 250, 164, 39, 121, 155, 197, 132, 218, 56, 102, 229, 187, 89, 7, 219, 133, 103, 57, 186, 228, 6, 88, 25, 71, 165, 251, 120, 38, 196, 154, 101, 59, 217, 135, 4, 90, 184, 230, 167, 249, 27, 69, 198, 152, 122, 36, 248, 166, 68, 26, 153, 199, 37, 123, 58, 100, 134, 216, 91, 5, 231, 185, 140, 210, 48, 110, 237, 179, 81, 15, 78, 16, 242, 172, 47, 113, 147, 205, 17, 79, 173, 243, 112, 46, 204, 146, 211, 141, 111, 49, 178, 236, 14, 80, 175, 241, 19, 77, 206, 144, 114, 44, 109, 51, 209, 143, 12, 82, 176, 238, 50, 108, 142, 208, 83, 13, 239, 177, 240, 174, 76, 18, 145, 207, 45, 115, 202, 148, 118, 40, 171, 245, 23, 73, 8, 86, 180, 234, 105, 55, 213, 139, 87, 9, 235, 181, 54, 104, 138, 212, 149, 203, 41, 119, 244, 170, 72, 22,233, 183, 85, 11, 136, 214, 52, 106, 43, 117, 151, 201, 74, 20, 246, 168, 116, 42, 200, 150, 21, 75, 169, 247, 182, 232, 10, 84, 215, 137, 107, 53
};
 
unsigned char crc8(const unsigned char * data) {
     unsigned char crc = 0xff;
     crc = table[reflect(data[0]) ^ crc];
     crc = table[reflect(data[1]) ^ crc];
     return reflect(crc);
}
 
unsigned char reflect(unsigned char c) {
     unsigned char r=0;
     for (int i=0;i<8;i++){
         if (c&(1<<i)){
             r=r|(1<<(7-i));
         }
     }
     return r;
}
