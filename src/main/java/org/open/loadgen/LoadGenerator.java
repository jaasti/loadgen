package org.open.loadgen;

import java.io.BufferedReader;
import java.io.FileReader;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LoadGenerator implements Runnable
{
  private class pat
  {
    Date _start;
    Date _end;
    String _patternparms;
  }
  
  public class ResetBarrier implements Runnable
  {
    LoadGenerator _barrier;
    public ResetBarrier(LoadGenerator barrier)
    {
      _barrier = barrier;
    }
    
    public void run()
    {
      _barrier._barrier.reset();
    }
  }
  
  private pat[] readPatterns(String file)
    throws Exception
  {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    List lst = new ArrayList();
    try
    {
      String line = reader.readLine();
      while (line != null)
      {
        String[] vals = line.split(";");
        Date dt = format.parse(vals[0]);
        Calendar cal = new GregorianCalendar();
        cal.setTime(dt);
        Calendar stdt = new GregorianCalendar();
        stdt.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        stdt.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        pat onepat = new pat();
        onepat._start = stdt.getTime();
        
        dt = format.parse(vals[1]);
        cal.setTime(dt);
        Calendar enddt = new GregorianCalendar();
        enddt.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        enddt.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        onepat._end = enddt.getTime();
        
        onepat._patternparms = vals[2];
        lst.add(onepat);
        line = reader.readLine();
      }
    }
    finally
    {
      reader.close();
    }

    return (pat[])lst.toArray(new pat[]{});
  }

  List _patterns;
  String[] _urls;
  ThreadPoolExecutor _executor;
  ScheduledExecutorService _scheduler;
  ScheduledFuture _schedule;
  int _occurrence = 0;
  CyclicBarrier _barrier;
  int _prevPattern;
  
  public LoadGenerator(String patternfile, String urlfile)
    throws Exception
  {
    pat[] patterns = readPatterns(patternfile);
    String[] urls = readURLs(urlfile);
    _patterns = new ArrayList();
    for (int i = 0; i < patterns.length; i++)
    {
      Pattern pat = Pattern.getPattern(patterns[i]._start, patterns[i]._end, patterns[i]._patternparms);
      _patterns.add(pat);
    }
    
    _urls = urls;
    _executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(Pattern.getMaximumUsersInRun());
    _scheduler = Executors.newScheduledThreadPool(1);
    _prevPattern = -1;
  }
  
  public void loadSystem()
    throws Exception
  {
    _schedule = _scheduler.scheduleWithFixedDelay(this, 0, 10, TimeUnit.SECONDS); //run every minute
    _scheduler.awaitTermination(24 * 60 * 60, TimeUnit.SECONDS); //can run a maximum of 24 hrs.
      
      //run();
  }
  
  
  
  public List<String> runLoad()
  {
      List<String> resp = new ArrayList<String>();
      try
      {
        _occurrence++;
        int users = 0;
        boolean patternfound = false;
        for (int i = 0; i < _patterns.size(); i++)
        {
          Pattern pat = (Pattern)_patterns.get(i);
          if (pat.isRunningNow())
          {
            if (_prevPattern != i)
            {
              _prevPattern = i;
              _occurrence = 0;
            }
            users = pat.getCurrentNumberOfUsers(_occurrence);
            patternfound = true;
            break;
          }
        }
        
        if (!patternfound)
        {
          _executor.shutdownNow();
          _scheduler.shutdownNow();
        }
        else
        {
          System.out.println("Pinging server with " + users + " users.");
          _barrier = new CyclicBarrier(users);
          CountDownLatch stopSignal = new CountDownLatch(users);
          for (int i = 0; i < users; i++)
          {
            HTTPRequestor request = new HTTPRequestor(_urls, _barrier, stopSignal);
            resp = request.runRequests();
            //_executor.execute(request);
          }
          
          stopSignal.await();
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      
      return resp;
  }
  public void run()
  {
    try
    {
      _occurrence++;
      int users = 0;
      boolean patternfound = false;
      for (int i = 0; i < _patterns.size(); i++)
      {
        Pattern pat = (Pattern)_patterns.get(i);
        if (pat.isRunningNow())
        {
          if (_prevPattern != i)
          {
            _prevPattern = i;
            _occurrence = 0;
          }
          users = pat.getCurrentNumberOfUsers(_occurrence);
          patternfound = true;
          break;
        }
      }
      
      if (!patternfound)
      {
        _executor.shutdownNow();
        _scheduler.shutdownNow();
      }
      else
      {
        System.out.println("Pinging server with " + users + " users.");
        _barrier = new CyclicBarrier(users);
        CountDownLatch stopSignal = new CountDownLatch(users);
        for (int i = 0; i < users; i++)
        {
          HTTPRequestor request = new HTTPRequestor(_urls, _barrier, stopSignal);
          //List<String> resp = request.runRequests();
          _executor.execute(request);
        }
        
        stopSignal.await();
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  private String[] readURLs(String file)
    throws Exception
  {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    List lst = new ArrayList();
    try
    {
      String line = reader.readLine();
      while(line != null)
      {
        lst.add(line);
        line = reader.readLine();
      }
    }
    finally
    {
      reader.close();
    }
    
    return (String[])lst.toArray(new String[] {});
  }
  
  public static void main(String[] args)
    throws Exception
  {
    if (args.length < 2)
    {
      System.out.println("Usage: LoadGenerator patternfile urlsfile");
      return;      
    }
    
    LoadGenerator gen = new LoadGenerator(args[0], args[1]);
    gen.loadSystem();
  }
}
