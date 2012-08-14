package com.example.potholder2.task;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.example.potholder2.ServerProvider;
import com.example.potholder2.utils.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

public class PostWavFile extends AsyncTask<Void, Void, Boolean>
{
	private Context mContext;
	
	public PostWavFile (Context context, ServerProvider sp)
	{
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		return doFileUpload();
	}

	private boolean doFileUpload(){
        HttpsURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String existingFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/narf.wav";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1*1024*1024;
        String responseFromServer = "";
        String urlString = "https://184.73.109.244:3000/files/wav";
        try
        {
         //------------------ CLIENT REQUEST
        FileInputStream fileInputStream = new FileInputStream(new File(existingFileName) );
         // open a URL connection to the Servlet
         URL url = new URL(urlString);
         // Open a HTTP connection to the URL
         conn = (HttpsURLConnection) url.openConnection();
         // Allow Inputs
         conn.setDoInput(true);
         // Allow Outputs
         conn.setDoOutput(true);
         // Don't use a cached copy.
         conn.setUseCaches(false);
         // Use a post method.
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Connection", "Keep-Alive");
         conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
         String loginCookie = Utils.getSetting(mContext, "cookie", null);

         conn.setRequestProperty("Cookie", loginCookie);

         dos = new DataOutputStream( conn.getOutputStream() );
         dos.writeBytes(twoHyphens + boundary + lineEnd);
         dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + existingFileName + "\"" + lineEnd);
         dos.writeBytes(lineEnd);
         // create a buffer of maximum size
         bytesAvailable = fileInputStream.available();
         bufferSize = Math.min(bytesAvailable, maxBufferSize);
         buffer = new byte[bufferSize];
         // read file and write it into form...
         bytesRead = fileInputStream.read(buffer, 0, bufferSize);
         while (bytesRead > 0)
         {
          dos.write(buffer, 0, bufferSize);
          bytesAvailable = fileInputStream.available();
          bufferSize = Math.min(bytesAvailable, maxBufferSize);
          bytesRead = fileInputStream.read(buffer, 0, bufferSize);
         }
         // send multipart form data necesssary after file data...
         dos.writeBytes(lineEnd);
         dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
         // close streams
         fileInputStream.close();
         dos.flush();
         dos.close();
        }
        catch (Exception ex)
        {
             ex.printStackTrace();
         	return false;
        }
        //------------------ read the SERVER RESPONSE
        try {
              inStream = new DataInputStream ( conn.getInputStream() );
              String str;
      	    StringBuffer sb = new StringBuffer();
              while (( str = inStream.readLine()) != null)
              {
            	  sb.append(str);
              }
              inStream.close();

        }
        catch (Exception e)
        {
        	e.printStackTrace();
        	return false;
        }
        
        return true;
      }

}
