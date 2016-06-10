package com.example.example1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;

public class Connect extends Activity {

	EditText edit, number1, number2, number3, number4;
	String total;
	
	/* ����Ŀ�� �������� �ƴҰ��,
	 *  Thread -> while explode ! -> Actually, this case never will take place. because, verification-code have differences. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect);

		edit = (EditText) findViewById(R.id.edit1);
		edit.setHintTextColor(Color.WHITE);
		edit.setHint(android.os.Build.MODEL);

		number1 = (EditText) findViewById(R.id.number1);
		number2 = (EditText) findViewById(R.id.number2);
		number3 = (EditText) findViewById(R.id.number3);
		number4 = (EditText) findViewById(R.id.number4);
		autofocus();

		// Connect Button
		ButtonRectangle btn = (ButtonRectangle) findViewById(R.id.btn1);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new SendPost().execute(); // ���� ���� ���
			}
		});
	}

	private void autofocus() {
		number1.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (number1.length() == 1) { // edit1 ���� ���Ѱ��� 6�̶�� ����������
					number2.requestFocus(); // �ι�°EditText �� ��Ŀ���� �Ѿ�� �˴ϴ�
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		number2.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (number2.length() == 1) { // edit1 ���� ���Ѱ��� 6�̶�� ����������
					number3.requestFocus(); // �ι�°EditText �� ��Ŀ���� �Ѿ�� �˴ϴ�
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		number3.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (number3.length() == 1) { // edit1 ���� ���Ѱ��� 6�̶�� ����������
					number4.requestFocus(); // �ι�°EditText �� ��Ŀ���� �Ѿ�� �˴ϴ�
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private class SendPost extends AsyncTask<Void, Void, String> {
		protected String doInBackground(Void... unused) {
			try {
				HttpPost request = new HttpPost(
						"http://ss.issro.net/ServerIp.php?r_number="
								+ number1.getText().toString()
								+ number2.getText().toString()
								+ number3.getText().toString()
								+ number4.getText().toString());

				Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
				HttpEntity enty = new UrlEncodedFormEntity(nameValue,
						HTTP.UTF_8);
				request.setEntity(enty);

				HttpClient client = new DefaultHttpClient();
				HttpResponse res = client.execute(request);

				// ���� ���� : total
				HttpEntity entityResponse = res.getEntity();
				InputStream im = entityResponse.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(im, HTTP.UTF_8));

				total = "";
				String tmp = "";
				while ((tmp = reader.readLine()) != null) {
					if (tmp != null) {
						total += tmp;
					}
				}
				im.close();
				Log.e("check", total);
				return total;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(String result) {
			if (total != null && total != "") {
				Intent intent = new Intent(Connect.this, Client.class);
				String name = edit.getText().toString();
				if(name.equals("")) name = edit.getHint().toString();
				intent.putExtra("name", name); // ����name ����
				intent.putExtra("total", total); // �������� ����
				startActivity(intent);
			} else {
				Toast.makeText(getApplicationContext(), "���� ��ȣ�� ��ġ���� �ʽ��ϴ�.", Toast.LENGTH_SHORT).show();
			}
		}
	}
}