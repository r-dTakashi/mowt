package com.example.mowt;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class PostThread extends AsyncTask<Uri.Builder, Void, String> {
	private final Activity MainActivity;

	public PostThread(Activity mainActivity) {
		MainActivity = mainActivity;
	}

	@Override
	protected String doInBackground(Uri.Builder... builder){
		String post_str = Post();

		//POSTで受け取った結果からJSONオブジェクトを生成
		JSONObject rootJSON = null;
		try {
			rootJSON = new JSONObject(post_str);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String token = null;
		try {
			token = Objects.requireNonNull(rootJSON).getString("token");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String get_str = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			get_str = Get(token);
		}

		return get_str;
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	protected void onPostExecute(String result) {
		TextView text1 = (TextView) MainActivity.findViewById(R.id.text1);
		TextView text2 = (TextView) MainActivity.findViewById(R.id.text2);
		TextView text3 = (TextView) MainActivity.findViewById(R.id.text3);
		TextView text4 = (TextView) MainActivity.findViewById(R.id.text4);
		TextView time =  (TextView) MainActivity.findViewById(R.id.text11);

		JSONObject rootJSON = null;
		try {
			rootJSON = new JSONObject(result);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONArray rootJSON2 = null;
		try {
			rootJSON2 = Objects.requireNonNull(rootJSON).getJSONArray("payload");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONObject j0 = null;
		try {
			j0 = Objects.requireNonNull(rootJSON2).getJSONObject(0);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		double in = 0 ,out = 0;
		int wind = 0;
		try {
			if(Objects.requireNonNull(j0).getInt("carbonDioxide") == 0 ){
				in = Objects.requireNonNull(j0).getDouble("temperature");
			} else {
				out = Objects.requireNonNull(j0).getDouble("temperature");
				wind = Objects.requireNonNull(j0).getInt("carbonDioxide");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONObject j1 = null;
		try {
			j1 = Objects.requireNonNull(rootJSON2).getJSONObject(1);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			if(Objects.requireNonNull(j1).getInt("carbonDioxide") == 0 ){
				in = Objects.requireNonNull(j1).getDouble("temperature");
			} else {
				out = Objects.requireNonNull(j1).getDouble("temperature");
				wind = Objects.requireNonNull(j1).getInt("carbonDioxide");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		BigDecimal it = new BigDecimal(in);
		BigDecimal item = it.setScale(2, BigDecimal.ROUND_HALF_UP);

		BigDecimal ot = new BigDecimal(out);
		BigDecimal otem = ot.setScale(2, BigDecimal.ROUND_HALF_UP);

		text1.setText(String.valueOf(item));
		text2.setText(String.valueOf(otem));
		if( wind > 400 ) text3.setText("強い風");
		else if(wind > 100) text3.setText("弱い風");
		else text3.setText("なし");

		boolean i1 = false, i2 = false, i3 = false, i4 = false, i5 = false, i6 = false;
		boolean o1 = false, o2 = false, o3 = false, o4 = false, o5 = false, o6 = false;
		boolean w1 = false, w2 = false, w3 = false;

		if(in > 30) i1 = true;
		else if(in > 25) i2 = true;
		else if(in > 20) i3 = true;
		else if(in > 13) i4 = true;
		else if(in > 5)	 i5 = true;
		else i6 = true;

		if(out > 30) o1 = true;
		else if(out > 25) o2 = true;
		else if(out > 20) o3 = true;
		else if(out > 13) o4 = true;
		else if(out > 5) o5 = true;
		else o6 = true;

		if( wind > 400 ) w1 = true;
		else if(wind > 100) w2 = true;
		else w3 = true;

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter time1 = DateTimeFormatter.ofPattern("yyyy/MM/dd/hh:mm");
		String t = time1.format(now);

		time.setText(t);

		if(i1 && (o1 || (o2 && w3))) text4.setText("エアコンを付けてください．熱中症になるかもしれません");
		if(i1 && (o2 && (w1 || w2))) text4.setText("エアコンは必要ないです．窓を開けてください");
		if(i1 && (o3 || o4 || o5 || o6)) text4.setText("エアコンは必要ないです．窓を開けてください．風がないので涼しくなるのに時間がかかるかもしれません");
		if(i1 && (o3 || o4 || o5 || o6) && (w1 || w2)) text4.setText("エアコンは必要ないです．窓を開けると風が入ってきて涼しいです");
		if(i2 && o1) text4.setText("快適な空間です．空調や窓をあけることは必要ないです.窓をあけると室温が上がるので注意してください");
		if((i2 || i3) && (o2 || o3)) text4.setText("快適な空間です．空調や窓をあけることは必要ないです");
		if(i2 && ((o4 && w3) || o5 || o6)) text4.setText("快適な空間ですが，外は寒いです．外出時に寒く感じてしまうので暖房はつけないようにしましょう");
		if(i3 && o1) text4.setText("少し室内が寒いので空調はつけないようにしましょう．外出時に暑く感じてしまいます");
		if(i3 && (o4 && w3)) text4.setText("快適な空間です．空調や窓をあけることは必要ないですが，今換気すると温度変化が小さいです");
		if(i3 && (o4 && (w2 || w1))) text4.setText("快適な空間です．空調は必要ないです．今換気すると温度変化が大きいので，窓を開けないようにしましょう");
		if(i3 && ((o5 && w3) || o6)) text4.setText("快適な空間ですが，外は寒いです．外出時に寒く感じてしまうので暖房はつけないようにしましょう");
		if(i4 && (o1 || o2 || o3)) text4.setText("外の温度に対し，少し寒い室温なので，エアコンはつけないようにしましょう");
		if(i4 && (o4 && w3)) text4.setText("寒いと感じたら空調を使わず，服を着込みましょう．今換気すると温度変化が小さいです");
		if(i4 && (o4 && (w2 || w1))) text4.setText("寒いと感じたら空調を使わず，服を着込みましょう．今換気すると温度変化が大きいので窓を開けないようにしましょう");
		if(i4 && o5 && w3) text4.setText("快適な空間です．今は風も少ないので，換気しても温度が低下しにくいでしょう");
		if(i4 && (o6 || (o5 && (w1 || w2)))) text4.setText("外出時には寒いと感じるでしょう．暖房などをつけないようにして，この温度を保ちましょう");
		if((i5 || i6) && (o1 || o2 || o3 || o4)) text4.setText("外の温度に対し，室内が寒いです．エアコンを付けていたら消しましょう");
		if(i5 && (o5 || (o6 && w3))) text4.setText("今換気をすれば，温度の変化が小さいでしょう");
		if(i5 && (o6 || (o5 && (w1 || w2)))) text4.setText("外は寒いです．今換気すると室温が急激に低下します．");
		if(i6 && (o5 || o6)) text4.setText("室温が低いので，暖房などをつけましょう");

	}

	public static String Post(){
		URL url = null;
		try {
			url = new URL("https://api.clip-viewer-lite.com/auth/token");
		} catch (MalformedURLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) Objects.requireNonNull(url).openConnection();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		Objects.requireNonNull(con).setRequestProperty("X-API-Key", "vwYOJ8c6wV8kzfzXUH6hm5MowHprUbot8Y4X2c89");

		try {
			con.setRequestMethod("POST");
		} catch (ProtocolException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		con.setDoOutput(true);
		String jsonInputString = "{\"username\": \"r.d.tsuchitori0723@gmail.com\", \"password\": \"T0c72S3K5a5TKC\"}";
		try (OutputStream os = con.getOutputStream()) {
			byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
			os.write(input, 0, input.length);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		int responseCode = 0;
		try {
			responseCode = con.getResponseCode();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		System.out.println("Response code: " + responseCode);

		InputStreamReader inputStreamReader = null;
		if (responseCode >= 200 && responseCode < 400) {
			try {
				inputStreamReader = new InputStreamReader(con.getInputStream());
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		} else {
			inputStreamReader = new InputStreamReader(con.getErrorStream());
		}
		BufferedReader in = new BufferedReader(inputStreamReader);
		String inputLine;
		StringBuilder response = new StringBuilder();
		try {
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return response.toString();
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public static String Get(String token){
		String from,to;
		String Time_url = "https://api.clip-viewer-lite.com/payload/000101492c?from=";

		LocalDateTime one = LocalDateTime.now();
		LocalDateTime five = LocalDateTime.now();

		//one = one.minusMinutes(1);
		five = five.minusMinutes(15);

		DateTimeFormatter time1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter time2 = DateTimeFormatter.ofPattern("HHmm");

		to = time1.format(one) + "T" + time2.format(one);
		from = time1.format(five) + "T" + time2.format(five);

		Time_url = Time_url + from + "&to=" + to;

		URL url = null;
		try {
			url = new URL(Time_url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) Objects.requireNonNull(url).openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Objects.requireNonNull(con).setRequestProperty("X-API-Key", "vwYOJ8c6wV8kzfzXUH6hm5MowHprUbot8Y4X2c89");
		con.setRequestProperty("Authorization", token);

		try {
			con.setRequestMethod("GET");
		} catch (ProtocolException e) {
			e.printStackTrace();
		}

		int responseCode = 0;
		try {
			responseCode = con.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Response code: " + responseCode);

		InputStreamReader inputStreamReader = null;
		if (responseCode >= 200 && responseCode < 400) {
			try {
				inputStreamReader = new InputStreamReader(con.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			inputStreamReader = new InputStreamReader(con.getErrorStream());
		}
		BufferedReader in = new BufferedReader(inputStreamReader);
		String inputLine = null;
		StringBuilder response = new StringBuilder();
		while (true) {
			try {
				if (!((inputLine = in.readLine()) != null)) break;
			} catch (IOException e) {
				e.printStackTrace();
			}
			response.append(inputLine);
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return response.toString();
	}

//	public static double InTemp(int payload){
//		int temp = 0;
//
//
//
//		return temp;
//	}
}
