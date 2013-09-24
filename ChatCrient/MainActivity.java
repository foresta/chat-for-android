package com.foresta.chat;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

//ソケット通信
public class MainActivity extends Activity implements View.OnClickListener {

	private final static String BR = System.getProperty("line.separator");
	private final static int    WC = LinearLayout.LayoutParams.WRAP_CONTENT;
	private final static int    MP = LinearLayout.LayoutParams.MATCH_PARENT;

	//IPアドレスの指定
	private String IP;                    //ここの文字列にＩＰアドレスを指定する
	private boolean settingFlag = false;

	private EditText ipEdit;

	private TextView receiveLabel; //受信ラベル
	private EditText sendEdit;     //送信エディットテキスト
	private Button   sendBtn;        //送信ボタン

	private Socket        socket; //ソケット
	private InputStream   input;  //入力ストリーム
	private OutputStream  output; //出力ストリーム
	private boolean       error;  //エラー

	private final Handler handler = new Handler(); //ハンドラ

	//アクティビティ起動時に呼ばれる
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		//レイアウトの生成
		LinearLayout layout = new LinearLayout(this);
		layout.setBackgroundColor(Color.rgb(255, 255, 255));
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);

		//送信エディットテキストの生成
		sendEdit = new EditText(this);
		sendEdit.setId(2);
		sendEdit.setText("",TextView.BufferType.NORMAL);
		sendEdit.setLayoutParams(new LinearLayout.LayoutParams(MP,WC));
		layout.addView(sendEdit);

		//送信ボタンの生成
		sendBtn = new Button(this);
		sendBtn.setText("送信");
		sendBtn.setOnClickListener(this);
		sendBtn.setLayoutParams(new LinearLayout.LayoutParams(WC,WC));
		layout.addView(sendBtn);

		//受信ラベルの生成
		receiveLabel = new TextView(this);
		receiveLabel.setId(1);
		receiveLabel.setText("");
		receiveLabel.setTextSize(16.0f);
		receiveLabel.setTextColor(Color.rgb(0, 0, 0));
		receiveLabel.setLayoutParams(new LinearLayout.LayoutParams(MP,WC));
		layout.addView(receiveLabel);

		ipEdit = new EditText(this);
		setIPDialog();

	}

	//アクティビティ開始時に呼ばれる
	@Override
	public void onStart(){
		super.onStart();

	}

	//アクティビティ停止時に呼ばれる
	@Override
	public void onStop(){
		super.onStop();
		disconnect();
	}

	private void setIPDialog(){
		showTextDialog(this,"ChatServerのIPアドレスを記入してください。", ipEdit,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						
						IP = ipEdit.getText().toString();

						//スレッドの生成
						Thread thread = new Thread(){
							public void run(){
								try{
									//接続
									connect(IP,8080);
								} catch (Exception e) {
								}
							}
						};
						thread.start();
					}
				});
	}

	//受信テキストの追加
	private void addText(final String text){
		//ハンドラの生成
		handler.post(new Runnable(){
			public void run() {
				receiveLabel.setText(text + BR + receiveLabel.getText());
			}
		});
	}

	//接続処理
	private void connect(String ip, int port){
		int size;
		String str;
		byte[] w = new byte[1024]; //ストリームを通すためにバイト配列で保存
		try {
			//ソケット接続
			addText("接続中");
			socket = new Socket(ip, port);
			input  = socket.getInputStream();
			output = socket.getOutputStream();
			addText("接続完了");

			//受信ループ
			while (socket!=null && socket.isConnected()){
				//データの受信
				size = input.read(w); //インプットデータ読み込み
				if( size <= 0){ //データがなければ
					continue;
				}

				str = new String(w, 0, size, "UTF-8"); //バイト配列をString型に変換

				//ラベルへの文字列追加
				addText(str);
			}

		} catch (Exception e){
			addText("通信に失敗しました");
			e.printStackTrace();
		}
	}

	//切断処理
	public void disconnect(){
		try {
			//ソケットを閉じる
			socket.close();
			socket = null;
		} catch (Exception e) {
		}
	}

	//ボタンクリックイベント処理
	public void onClick(View v){
		//スレッドの生成
		Thread thread = new Thread(new Runnable(){
			public void run(){
				error = false;
				try {
					//データの送信
					if (socket != null && socket.isConnected()) {
						byte[] w = sendEdit.getText().toString().getBytes("UTF-8");
						output.write(w);
						output.flush();
					}
				} catch (Exception e) {
					error = true;
				}

				//ハンドラの生成
				handler.post(new Runnable(){
					public void run(){
						//送信エラーが起こらなければ
						if( !error ){
							sendEdit.setText("",TextView.BufferType.NORMAL);
						}
						else {
							addText("通信に失敗しました");
						}
					}
				});
			}
		});

		thread.start();
	}


	//ダイアログ表示処理
	private static void showDialog(Context context, String title, String text){
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setMessage(text);
		ad.setPositiveButton("OK", null);
		ad.show();
	}

	//テキストダイアログ表示処理
	private static void showTextDialog(Context context, String title, EditText editText,
										DialogInterface.OnClickListener listener) {
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setView(editText);
		ad.setPositiveButton("OK", listener);
		ad.show();
	}
}
