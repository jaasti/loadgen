package org.open.loadgen;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.net.URLConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class HTTPRequestor implements Runnable
{
    String[] _urlsToHit;
    URL  _currentURL;
    HttpURLConnection _currentConnection;
    OutputStream _currentStream;
    int _currentRequest;
    
    CyclicBarrier _startSignal;
    CountDownLatch _stopSignal;
    
    List<String> _responses;
    
    boolean _single = false;
  
    public HTTPRequestor(String[] urls, CyclicBarrier startSignal, CountDownLatch stopSignal)
    {
      _currentRequest = 0;
      _urlsToHit = urls;
      _startSignal = startSignal;
      _stopSignal = stopSignal;
      
      _responses = new ArrayList<String>();
    }
    
    protected boolean bufferNextRequest()
      throws Exception
    {
      boolean bRet = false;
      if ((_urlsToHit != null) && (_currentRequest < _urlsToHit.length))
      {
        String url = _urlsToHit[_currentRequest];
        _currentRequest++;
        int index = url.indexOf('?');
        String data = "";
        if (index >= 0)
        {
          data = url.substring(index + 1);
          url = url.substring(0, index);
        }
        
        _currentURL = new URL(url);
        _currentConnection = (HttpURLConnection)_currentURL.openConnection();
        _currentConnection.setDoOutput(true);
        _currentConnection.setRequestMethod("POST");
        _currentStream = _currentConnection.getOutputStream();
        _currentStream.write(data.getBytes());
        bRet = true;
      }
      
      return bRet;
      
    }
  
    protected void flushRequest()
      throws Exception
    {
      if (_currentStream != null)
      {
        _startSignal.await();
        _startSignal.reset();
        _currentStream.flush();
        _currentStream.close();
        BufferedReader rd = new BufferedReader(new InputStreamReader(_currentConnection.getInputStream()));
        String line;
        StringBuffer buf = new StringBuffer();
        while ((line = rd.readLine()) != null) 
        {
            buf.append(line);
        }
        rd.close();
        
        _responses.add(buf.toString());
        
        
      }
    }
    
    public void run()
    {
      try
      {
        while (bufferNextRequest())
        {
          flushRequest();
          //if(_single) 
            //  break;
        }
      }
      catch (Exception e)
      {
        //e.printStackTrace();
      }
      finally
      {
        _stopSignal.countDown();        
      }
    }
    
    public List<String> runRequests()
    {
        try
        {
          while (bufferNextRequest())
          {
            flushRequest();
            //if(_single) 
              //  break;
          }
        }
        catch (Exception e)
        {
          //e.printStackTrace();
        }
        finally
        {
          _stopSignal.countDown();        
        }
        
        return _responses;
    }
    
}
