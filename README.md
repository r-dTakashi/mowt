# Mowt: 温度と風を観測して電力ひっ迫を防ぐシステム([PDF](Mowt_slide.pdf))
**本アイデアは「ELTRES アイデアコンテスト2022 常識を飛び越えろ！」で[アイデア賞を受賞しました](https://iot.sonynetwork.co.jp/column/column051/)**

## 概要
Mowtは、周辺の温度と風の状態を観測し、電力の無駄な消費を防ぐことで電力ひっ迫の問題を解決するシステムです。特に暑い地域や寒い地域での生活の安全性を確保しながら、エアコンの過剰使用を防ぐことを目指しています。

## 課題
最近の日本では、急激な温度変化やリモートワークの増加に伴い、無意識のうちにエアコンを使いすぎていることがあります。これにより、電力の無駄な消費が増加し、電力ひっ迫の原因となっています。エアコンへの過剰な依存を防ぐことで、電力の効率的な使用を実現したいと考えました。

## 解決方法
### 温度と風の観測:
部屋ごとに温度センサと風を検知する小型風力タービンを設置し、温度差と風の有無を検知します。  
これにより、部屋ごとの最適な空調使用を判断します。
### データ収集と表示:
SPRESENSEにセンサを取り付け、ELTRES通信でクラウドにデータをアップロードします。([コード１](mowt_spresense))  
クラウド上のデータをAPIを使ってAndroidアプリで取得し、室内外の温度と風の状態を表示します。 ([コード2](mowt))
### アプリの機能([コード2](mowt)):
温度と風の状態を表示し、空調の必要性を判断します。  
着ている服装を選択し、外出時の適切な服装を提案します。

## プロトタイプの仕様
### ハードウェア
室内と室外の温度センサ  
小型風力タービン（風を検知）

SPRESENSE
### ソフトウェア
ELTRES通信でデータをクラウドにアップロード([コード１](mowt_spresense))  
APIを使ってデータを取得し、Androidアプリで表示([コード2](mowt))

## 実証実験の手法と結果
### 冬の気候での検証:
昼と夜の温度差による違いを確認。  
夜に室内が暑ければ暖房を切るように提示。
### 風による検証:
風の有無による表示の差を確認。  
換気のタイミングを判断し、部屋の温度を効果的に下げる。
### 夏の気候での検証:
エアコンの停止を提示できることを確認。
### 風と夏の気候での検証:
風があるときのほうが温度低下しやすく、更にエアコンの停止を強調。

## 結果の考察
冬の状態では、比較的暖かい場所や時間帯では空調の使用を控えることができ、電力が抑えられる。  
夏の状態では、涼しい時間帯や場所で電力を抑えることができる。

## 参考文献
「ELTRES アイデアコンテスト2022 常識を飛び越えろ！」受賞アイデア結果発表, https://iot.sonynetwork.co.jp/column/column051/, (閲覧日： 2024/6/2)
