package com.example.potholder2.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.potholder2.ServerProvider;
import com.example.potholder2.utils.Utils;

public class GetPotHole extends AsyncTask<Void, Void, Boolean>
{
	private ServerProvider mSp;
	private Context mContext;
	private ProgressDialog mDialog = null;
	
	public GetPotHole (Context context, ServerProvider sp)
	{
		mSp = sp;
		mContext = context;
		trustEveryone();
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		if (mContext != null)
		{
			mDialog = new ProgressDialog(mContext);
			mDialog.setMessage("Querying all potholes, please wait...");
			mDialog.show();
		}
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		try
		{
			String result= getAllPotholes ("https://184.73.109.244:3000/potholes");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return true;
	}
	
	
	@Override
	protected void onPostExecute(Boolean result)
	{
		super.onPostExecute(result);
		mSp.onLoginComplete(result);
	}
	
	private String getAllPotholes (String stringUrl) throws IOException
	{
		String contentType = "application/json";
		HttpURLConnection conn = null;
		try 
		{
			String loginCookie = Utils.getSetting(mContext, "cookie", null);
			if (loginCookie != null)
			{
				URL url = new URL(stringUrl);

				conn = (HttpURLConnection)url.openConnection();
				conn.setRequestProperty("Content-Type", contentType);
				conn.setRequestProperty("Cookie", loginCookie);

				//conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setUseCaches(false); 
				conn.setRequestMethod("GET"); 

				// connect and flush the request out
				conn.connect();
				int responseCode = conn.getResponseCode();
				if (responseCode == 200)
				{
					StringBuilder sb = new StringBuilder(); 
					BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String line;
					while ((line = rd.readLine()) != null) 
					{
						sb.append(line);
					}
					rd.close();
					sb.trimToSize();
					//[{"user_id":"5025601a9de973e104000001","location":[40.3,-73.9],"created_at":"2012-08-11T19:55:38.408Z","_id":"5026b8baf2ae33750b000001"}]
					return sb.toString();
				}
			}
		} 
		finally{
			if(conn != null)
				conn.disconnect();
		}
		return null;
	}
	
/*	private boolean getAllPotholes(String uri)
	{
		String contentType = "application/json";
		OutputStreamWriter wr = null;
		// Send data
		try{
			final URL url = new URL(uri);
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
			httpsURLConnection.setRequestMethod("GET");
			
			httpsURLConnection.setRequestProperty("Content-Type", contentType);
			httpsURLConnection.addRequestProperty("Cookie", Utils.getSetting(mContext, "cookie", ""));

			wr = new OutputStreamWriter(httpsURLConnection.getOutputStream());
			wr.write(jsonObj.toString());
			wr.flush();

			httpsURLConnection.connect();
			int code = httpsURLConnection.getResponseCode();
			
			getResponse(httpsURLConnection);
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
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		return true;
	}*/
	
	public static String getResponse(HttpsURLConnection conn) {
	    InputStream is = null;
	    try {
	        if(conn.getResponseCode()>=400){
	            is = conn.getErrorStream();
	        }
	        else{
	            is=conn.getInputStream();
	        }
	    
	    int ch;
	    StringBuffer sb = new StringBuffer();
	    while ((ch = is.read()) != -1) {
	     sb.append((char) ch);
	    }
	    //System.out.println(sb.toString());
	    return sb.toString();
	    // return conferenceId;
	   }
	    catch (Exception e){
	    e.printStackTrace();
	    }

	     return null;   
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