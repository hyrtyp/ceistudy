package com.weibo.sdk.android.demo;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hyrt.ceiphone.R;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.keep.AccessTokenKeeper;
//import com.weibo.sdk.android.sso.SsoHandler;
import com.weibo.sdk.android.util.Utility;

public class MainActivity extends Activity {

	private Weibo mWeibo;
	// private static final String SECRET = "94540faae025fd1189060f33b7168ae1";
	private static final String CONSUMER_KEY = "4178506814";// 替换为开发者的appkey，例如"1646212860";
	private static final String REDIRECT_URL = "http://mob.cei.gov.cn:8000/toDownloadCenter.action";
	private Intent it = null;
	private Button authBtn, apiBtn, ssoBtn, cancelBtn;
	private TextView mText;
	public static Oauth2AccessToken accessToken;
	public static final String TAG = "sinasdk";
	public static MainActivity mainActivity;

	/**
	 * SsoHandler 仅当sdk支持sso时有效，
	 */
	// SsoHandler mSsoHandler;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mWeibo = Weibo.getInstance(CONSUMER_KEY, REDIRECT_URL);
		mainActivity = MainActivity.this;
		authBtn = (Button) findViewById(R.id.auth);
		authBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mWeibo.authorize(MainActivity.this, new AuthDialogListener());
			}
		});
		ssoBtn = (Button) findViewById(R.id.sso);// 触发sso的按钮
		try {
			Class sso = Class.forName("com.weibo.sdk.android.sso.SsoHandler");
			//ssoBtn.setVisibility(View.VISIBLE);
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
			Log.i(TAG, "com.weibo.sdk.android.sso.SsoHandler not found");

		}
		ssoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/**
				 * 下面两个注释掉的代码，仅当sdk支持sso时有效，
				 */

				/*
				 * mSsoHandler =new SsoHandler(MainActivity.this,mWeibo);
				 * mSsoHandler.authorize( new AuthDialogListener());
				 */
			}
		});
		cancelBtn = (Button) findViewById(R.id.apiCancel);
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AccessTokenKeeper.clear(MainActivity.this);
				apiBtn.setVisibility(View.INVISIBLE);
				authBtn.setVisibility(View.VISIBLE);
				ssoBtn.setVisibility(View.VISIBLE);
				cancelBtn.setVisibility(View.INVISIBLE);
				mText.setText("");
			}
		});

		Button send = (Button) findViewById(R.id.sendBtn);
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(it);
			}
		});

		apiBtn = (Button) findViewById(R.id.apiBtn);
		apiBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, APITypeListActivity.class);
				startActivity(intent);
			}
		});
		mText = (TextView) findViewById(R.id.show);
		MainActivity.accessToken = AccessTokenKeeper.readAccessToken(this);
		if (MainActivity.accessToken.isSessionValid()) {
			Weibo.isWifi = Utility.isWifi(this);
			Intent it = new Intent(MainActivity.this, ShareActivity.class);
			it.putExtra(ShareActivity.EXTRA_ACCESS_TOKEN,
					MainActivity.accessToken.getToken());
			it.putExtra(ShareActivity.EXTRA_EXPIRES_IN,
					MainActivity.accessToken.getExpiresTime());
			startActivity(it);
			/*
			 * try { Class sso =
			 * Class.forName("com.weibo.sdk.android.api.WeiboAPI");//
			 * 如果支持weiboapi的话，显示api功能演示入口按钮 apiBtn.setVisibility(View.VISIBLE);
			 * } catch (ClassNotFoundException e) { // e.printStackTrace();
			 * Log.i(TAG, "com.weibo.sdk.android.api.WeiboAPI not found");
			 * 
			 * }
			 */
			//authBtn.setVisibility(View.INVISIBLE);
			//ssoBtn.setVisibility(View.INVISIBLE);
			//cancelBtn.setVisibility(View.VISIBLE);
			String date = new java.text.SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
					.format(new java.util.Date(MainActivity.accessToken
							.getExpiresTime()));
			mText.setText("access_token 仍在有效期内,无需再次登录: \naccess_token:"
					+ MainActivity.accessToken.getToken() + "\n有效期：" + date);
		} else {
			new Handler().post(new Runnable() {

				@Override
				public void run() {
					mWeibo.authorize(MainActivity.this, new AuthDialogListener());
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			MainActivity.accessToken = new Oauth2AccessToken(token, expires_in);
			if (MainActivity.accessToken.isSessionValid()) {
				String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
						.format(new java.util.Date(MainActivity.accessToken.getExpiresTime()));
				mText.setText("认证成功: \r\n access_token: " + token + "\r\n"+ "expires_in: " + expires_in + "\r\n有效期：" + date);
				Intent it = new Intent(MainActivity.this, ShareActivity.class);
				it.putExtra(ShareActivity.EXTRA_ACCESS_TOKEN,
						MainActivity.accessToken.getToken());
				it.putExtra(ShareActivity.EXTRA_EXPIRES_IN,
						MainActivity.accessToken.getExpiresTime());
				startActivity(it);
				/*try {
					Class sso = Class
							.forName("com.weibo.sdk.android.api.WeiboAPI");// 如果支持weiboapi的话，显示api功能演示入口按钮
					apiBtn.setVisibility(View.VISIBLE);
				} catch (ClassNotFoundException e) {
					// e.printStackTrace();
					Log.i(TAG, "com.weibo.sdk.android.api.WeiboAPI not found");

				}*/
				//cancelBtn.setVisibility(View.VISIBLE);
				AccessTokenKeeper.keepAccessToken(MainActivity.this,accessToken);
				//Toast.makeText(MainActivity.this, "认证成功", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onError(WeiboDialogError e) {
			Toast.makeText(getApplicationContext(),
					"Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "Auth cancel",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getApplicationContext(),
					"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		/**
		 * 下面两个注释掉的代码，仅当sdk支持sso时有效，
		 */
		/*
		 * if(mSsoHandler!=null){ mSsoHandler.authorizeCallBack(requestCode,
		 * resultCode, data); }
		 */
	}

}