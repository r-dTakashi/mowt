package com.example.mowt;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Uri.Builder builder = new Uri.Builder();

		Button reload = (Button) findViewById(R.id.reload);
		Button reload2 = (Button) findViewById(R.id.reload2);
		Switch s1 = (Switch) findViewById(R.id.switch1);
		Switch s2 = (Switch) findViewById(R.id.switch2);
		Switch s3 = (Switch) findViewById(R.id.switch3);
		Switch s4 = (Switch) findViewById(R.id.switch4);
		Switch s5 = (Switch) findViewById(R.id.switch5);

		TextView text12 = (TextView) findViewById(R.id.text12);

		reload.setOnClickListener((View v) ->{
			PostThread task = new PostThread(this);
			task.execute(builder);
		});

		final boolean[] f1 = {false};
		final boolean[] f2 = {false};
		final boolean[] f3 = {false};
		final boolean[] f4 = {false};
		final boolean[] f5 = {false};

		s1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if(isChecked) {
					f1[0] = true;
				} else {
					f1[0] = false;
				}
			}
		});

		s2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if(isChecked) {
					f2[0] = true;
				} else {
					f2[0] = false;
				}
			}
		});

		s3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if(isChecked) {
					f3[0] = true;
				} else {
					f3[0] = false;
				}
			}
		});

		s4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if(isChecked) {
					f4[0] = true;
					s5.setClickable(false);
				} else {
					f4[0] = false;
					s5.setClickable(true);
				}
			}
		});

		s5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if(isChecked) {
					f5[0] = true;
					s4.setClickable(false);
				} else {
					f5[0] = false;
					s4.setClickable(true);
				}
			}
		});

		reload2.setOnClickListener((View v) ->{
			if(!f1[0] && !f2[0] && !f3[0]) text12.setText("着ている服を選んでください");
			else if(!f1[0] && !f2[0]) text12.setText("中に着ている服を選んでください");
			else if(!f4[0] && !f5[0]) text12.setText("履いているズボンの種類を選んでください");

			if(f1[0] && f2[0] && f3[0] && f4[0]) {
				Thread1234 t1234 = new Thread1234(this);
				t1234.execute(builder);
			}
			if(f1[0] && f2[0] && !f3[0] && f4[0]) {
				Thread124 t124 = new Thread124(this);
				t124.execute(builder);
			}
			if(f1[0] && !f2[0] && f3[0] && f4[0]){
				Thread134 t134 = new Thread134(this);
				t134.execute(builder);
			}
			if(f1[0] && !f2[0]  && !f3[0] && f4[0]){
				Thread14 t14 = new Thread14(this);
				t14.execute(builder);
			}

			if(f1[0] && f2[0] && f3[0] && f5[0]) {
				Thread1235 t1235 = new Thread1235(this);
				t1235.execute(builder);
			}
			if(f1[0] && f2[0] && !f3[0] && f5[0]) {
				Thread125 t125 = new Thread125(this);
				t125.execute(builder);
			}
			if(f1[0] && !f2[0] && f3[0] && f5[0]){
				Thread135 t135 = new Thread135(this);
				t135.execute(builder);
			}
			if(f1[0] && !f2[0] && !f3[0] && f5[0]){
				Thread15 t15 = new Thread15(this);
				t15.execute(builder);
			}

			if(f2[0] && f3[0] && f4[0]){
				Thread234 t234 = new Thread234(this);
				t234.execute(builder);
			} else if(f2[0] && f4[0]){
				Thread24 t24 = new Thread24(this);
				t24.execute(builder);
			}
		});
	}

}