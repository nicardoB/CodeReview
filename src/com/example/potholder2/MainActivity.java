package com.example.potholder2;

import java.util.ArrayList;

import com.example.potholder2.task.GetPotHole;
import com.example.potholder2.task.LoginTask;
import com.example.potholder2.task.PostPotHole;
import com.example.potholder2.task.PostWavFile;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity implements ServerProvider
{
	private boolean mLoggedIn = false;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //login user
        loginUser();
    }
    
    private void loginUser ()
    {
    	LoginTask lt = new LoginTask(this, this);
    	String [] params = new String [2];
    	params [0] = "test@test.com";
    	params [1] = "testtest";
    	lt.execute(params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void onPostClick (View v)
    {
    	if (mLoggedIn)
    	{
			PostPotHole pph = new PostPotHole(this, new ServerProvider()
			{
				@Override
				public void onLoginComplete(boolean result)
				{
					// TODO Auto-generated method stub
					
				}
			}
			);
			Object [] params = new Object [2];
			params[0] = new ArrayList<Double>();
			params[1] = null;
			pph.execute(params);
    	}
    }
    
    public void onGetClick (View v)
    {
    	if (mLoggedIn)
    	{
    		GetPotHole gph = new GetPotHole(this, new ServerProvider()
			{
				
				@Override
				public void onLoginComplete(boolean result)
				{
					
				}
			});
    		
    		gph.execute();
    	}
    }
    
    public void onPostWavClick (View v)
    {
    	if (mLoggedIn)
    	{
    		PostWavFile pwf = new PostWavFile(this, new ServerProvider()
			{
				
				@Override
				public void onLoginComplete(boolean result)
				{
					
				}
			});
    		
    		pwf.execute();
    	}
    }

	@Override
	public void onLoginComplete(boolean result)
	{
		if (result)
		{
			mLoggedIn = true;
		}
	}
}
