package com.example.potholder2.task;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.potholder2.ServerProvider;
import com.example.potholder2.utils.Utils;

public class LoginTask extends AsyncTask<String, Void, Boolean>
{
	private ProgressDialog mDialog = null;
	private Context mContext;
	private ServerProvider mSp;
	
	public LoginTask (Context context, ServerProvider sp)
	{
		mContext = context;
		mSp = sp;
		trustEveryone();
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		//show a spinner
		if (mContext != null)
		{
			mDialog = new ProgressDialog(mContext);
			mDialog.setMessage("Logging in, please wait...");
			mDialog.show();
		}
	}

	@Override
	protected Boolean doInBackground(String... params)
	{
		if (params.length == 2)
		{
			final String userName = params[0];
			final String password = params[1];
			JSONObject jsonObj = createJSONFromParams(userName, password);
			
			if (jsonObj != null)
			{
				//post to the server
				return postLoginInfo(mContext, "https://184.73.109.244:3000/users/auth", jsonObj);
			}
			
		}
		
		return false;
	}
	
	private JSONObject createJSONFromParams (String user, String pass)
	{		
		try
		{
			JSONObject loginObject = new JSONObject();
			// put elements into the object as a key-value pair
			loginObject.put("email", user);
			loginObject.put("password", pass);
			return loginObject;
		}
		catch (Exception ex)
		{
			if (mDialog != null)
			{
				mDialog.dismiss();
			}
			ex.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(Boolean result)
	{
		super.onPostExecute(result);
		mDialog.dismiss();
		mSp.onLoginComplete(result);
	}
	
	private boolean postLoginInfo(Context applicationContext, String uri,JSONObject jsonObj)
	{
		String contentType = "application/json";
		OutputStreamWriter wr = null;
		// Send data
		try{
			final URL url = new URL(uri);
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
			httpsURLConnection.setRequestMethod("POST");
			
			httpsURLConnection.setRequestProperty("Content-Type", contentType);
			
			httpsURLConnection.setDoOutput(true);

			wr = new OutputStreamWriter(httpsURLConnection.getOutputStream());
			wr.write(jsonObj.toString());
			wr.flush();

			httpsURLConnection.connect();
			int code = httpsURLConnection.getResponseCode();
			

			String headerName  = "";
			String cIsForCookie = ""; // this saves the auth token
			for (int i = 1; (headerName = httpsURLConnection.getHeaderFieldKey(i)) != null; i++) {
				if (headerName.equalsIgnoreCase("Set-Cookie")) {
					String cookie = httpsURLConnection.getHeaderField(i);
					cIsForCookie = cIsForCookie.concat(cookie.substring(0, cookie.indexOf(";") + 1));
					cIsForCookie = cIsForCookie.concat(" ");
					//save the cookie
				}
			}
			Utils.saveSetting(mContext, "cookie", cIsForCookie);
		}
		catch (Exception ex)
		{
			if (mDialog != null)
			{
				mDialog.dismiss();
			}
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static void trustEveryone() { 
		try { 
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){ 
				public boolean verify(String hostname, SSLSession session) { 
					return true; 
				}}); 
			SSLContext context = SSLContext.getInstance("TLS"); 
			context.init(null, new X509TrustManager[]{new X509TrustManager(){ 
				public void checkClientTrusted(X509Certificate[] chain, 
						String authType) throws CertificateException {} 
				public void checkServerTrusted(X509Certificate[] chain, 
						String authType) throws CertificateException {} 
				public X509Certificate[] getAcceptedIssuers() { 
					return new X509Certificate[0]; 
				}}}, new SecureRandom()); 
			HttpsURLConnection.setDefaultSSLSocketFactory( 
					context.getSocketFactory()); 
		} catch (Exception e) { // should never happen 
			e.printStackTrace(); 
		}
	}

}
