package com.example.potholder2.task;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.example.potholder2.ServerProvider;
import com.example.potholder2.utils.Utils;

public class PostPotHole extends AsyncTask<Object, Void, Boolean>
{
	private ServerProvider mSp;
	private Context mContext;
	
	public PostPotHole (Context context, ServerProvider sp)
	{
		mSp = sp;
		mContext = context;
		trustEveryone();
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Object... params)
	{
		if (params.length == 2)
		{
			final ArrayList <Double> location = (ArrayList<Double>) params[0];
			final String wavUrl = (String) params[1];
			JSONObject jsonObj = createJSONFromParams(location, wavUrl);
			
			if (jsonObj != null)
			{
				//post to the server
				return postPotHoleInfo("https://184.73.109.244:3000/potholes", jsonObj);
			}
			
		}
		
		return false;
	}
	
	private JSONObject createJSONFromParams (ArrayList<Double> location, String wavUrl)
	{		
		try
		{
			JSONObject potHoleObject = new JSONObject();
			// put elements into the object as a key-value pair
			//40.3, -73.9
			JSONArray locationArray = new JSONArray();
			locationArray.put(0, 40.3);
			locationArray.put(1, -73.9);
			potHoleObject.put("location", locationArray);
		//	potHoleObject.put("wav_url", wavUrl);
			return potHoleObject;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(Boolean result)
	{
		super.onPostExecute(result);
		mSp.onLoginComplete(result);
	}
	
	private boolean postPotHoleInfo(String uri,JSONObject jsonObj)
	{
		String contentType = "application/json";
		OutputStreamWriter wr = null;
		// Send data
		try{
			final URL url = new URL(uri);
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
			httpsURLConnection.setRequestMethod("POST");
			
			httpsURLConnection.setRequestProperty("Content-Type", contentType);
			httpsURLConnection.addRequestProperty("Cookie", Utils.getSetting(mContext, "cookie", ""));
			httpsURLConnection.setDoOutput(true);

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
	}
	
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