package org.open.loadgen;

import java.util.Date;

public class ConstantPattern extends Pattern
{
  int _users;
  
  public ConstantPattern(Date start, Date end, int users, graphtype type)
  {
    super(start, end, type);
    _users = users;
  }
  
  public ConstantPattern(Date start, Date end, graphtype type, String[] parms)
    throws Exception
  {
    super(start, end, type);
    if (parms.length < 3)
      throw new IllegalArgumentException("Need parameters startat, slope and maxUsers");
      
    _users = Integer.parseInt(parms[2]);
  }
  
  public int getMaxUsers()
  {
    return _users;
  }

  
  public int getCurrentNumberOfUsers(int occurrence)
  {
    return _users;
  }
}
