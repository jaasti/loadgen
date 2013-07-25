package org.open.loadgen;

import java.lang.reflect.Constructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class Pattern
{
  public enum graphtype { simple, exponential, logarithm, sine, cosine, tangent };
  public enum patternimplementations { constant, increasing, decreasing, single };
  
  Date _startDate;
  Date _endDate;
  graphtype _type;
  static Map _implementations;
  static int _maximumUsers;
  
  static
  {
    registerMyself(Pattern.patternimplementations.increasing, IncreasingPattern.class);
    registerMyself(Pattern.patternimplementations.decreasing, DecreasingPattern.class);
    registerMyself(Pattern.patternimplementations.constant, ConstantPattern.class);
    registerMyself(Pattern.patternimplementations.single, SinglePattern.class);
  }
  
  public Pattern(Date start, Date end, graphtype type)
  {
    _startDate = start;
    _endDate = end;
    _type = type;
  }
  
  protected static void registerMyself(patternimplementations impl, Class pat)
  {
    if (_implementations == null)
      _implementations = new HashMap();
      
    _implementations.put(impl, pat);
  }
  
  public abstract int getCurrentNumberOfUsers(int occurrence);
  public abstract int getMaxUsers();
  
  public boolean isRunningNow()
  {
    Date now = new Date();
    System.out.println("Comparing: " + now + " start: " + _startDate + " end: " + _endDate);
    boolean bRet = now.after(_startDate);
    bRet = (bRet || now.equals(_startDate));
    bRet = (bRet && now.before(_endDate));
    
    return bRet;
  }
  
  public static int getMaximumUsersInRun()
  {
    return _maximumUsers;
  }
  
  public static Pattern getPattern(Date start, Date end, String patternparms)
    throws Exception
  {
    String[] parms = patternparms.split("-");
    patternimplementations impl = Pattern.patternimplementations.valueOf(parms[0]);
    graphtype type = Pattern.graphtype.valueOf(parms[1]);
    Class pat = (Class)_implementations.get(impl);
    Class[] clsparms = new Class[] { Date.class, Date.class, graphtype.class, String[].class };
    Object[] objparms = new Object[] { start, end, type, parms };
    
    Constructor construct = pat.getConstructor(clsparms);
    Pattern retpat = (Pattern)construct.newInstance(objparms);
    if (_maximumUsers < retpat.getMaxUsers())
      _maximumUsers = retpat.getMaxUsers();
      
    return retpat;
  }
}
