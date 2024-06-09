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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Thread1235 extends AsyncTask<Uri.Builder, Void, String> {
	private final Activity MainActivity;

	public Thread1235(Activity mainActivity) {
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

	@Override
	protected void onPostExecute(String result) {
		TextView t = (TextView) MainActivity.findViewById(R.id.text12);


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

		double p_out = 0.0;
		JSONObject j15 = null;
		try {
			j15 = Objects.requireNonNull(rootJSON2).getJSONObject(15);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			if(Objects.requireNonNull(j15).getInt("carbonDioxide") != 0 ){
				p_out = Objects.requireNonNull(j15).getDouble("temperature");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if(p_out == 0) {
			try {
				j15 = Objects.requireNonNull(rootJSON2).getJSONObject(16);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			try {
				if (Objects.requireNonNull(j15).getInt("carbonDioxide") != 0) {
					p_out = Objects.requireNonNull(j15).getDouble("temperature");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		if(in - out >= 7) {
			if(Math.abs(out - p_out) > 2) t.setText("今日は急激な温度変化があり，上にもう2枚を着たり，ヒートテック，長ズボンがおすすめ");
			else if(wind > 200) t.setText("風が強いので，上にもう2枚着たり，ヒートテック，長ズボンがおすすめ");
			else t.setText("もう2枚着たり，ヒートテック，長ズボンがおすすめ");
		} else if(in - out >= 3) {
			if(Math.abs(out - p_out) > 2) t.setText("今日は急激な温度変化があるため，もう1枚着たり，長ズボンがおすすめ");
			else if(wind > 200) t.setText("風が強いので，上にもう1枚着たり，長ズボンがおすすめ");
			else t.setText("もう1枚着たり，長ズボンがおすすめ");
		} else if(in - out >= -3){
			if(Math.abs(out - p_out) > 2) t.setText("今日は急激な温度変化があります．外出時は気をつけましょう");
			else t.setText("外の温度は部屋と変わらず，温度変化も少ないので，厚着の服は必要ないです");
		} else {
			if(Math.abs(out - p_out) > 2) t.setText("今日は急激な温度変化がありますが，厚着の服は必要ないです");
			else t.setText("少し暑いかもしれないので，何枚か脱いだほうがいいかもしれないです");
		}

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
		five = five.minusMinutes(20);

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
				if ((inputLine = in.readLine()) == null) break;
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

}

