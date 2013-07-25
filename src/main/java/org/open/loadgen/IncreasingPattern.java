package org.open.loadgen;

import java.util.Date;

public class IncreasingPattern extends Pattern
{
  int _startUsers;
  int _maxUsers;
  double _slope = 1;
  
  public IncreasingPattern(Date start, Date end, int startat, double slope, int maxUsers, graphtype type)
  {
    super(start, end, type);
    _slope = slope;
    _startUsers = startat;
    _maxUsers = maxUsers;
  }
  
  public IncreasingPattern(Date start, Date end, Pattern.graphtype type, String[] parms)
    throws Exception
  {
    super(start, end, type);
    if (parms.length < 5)
      throw new IllegalArgumentException("Need parameters startat, slope and maxUsers");
      
    _startUsers = Integer.parseInt(parms[2]);
    _slope = Double.parseDouble(parms[3]);
    _maxUsers = Integer.parseInt(parms[4]);
  }
  
  public int getMaxUsers()
  {
    return _maxUsers;
  }
  
  public int getCurrentNumberOfUsers(int occurrence)
  {
    int users = _startUsers;
    double currentIncrease = (_slope * occurrence);
    if (occurrence > 1)
    {
      switch (_type)
      {
        case exponential:
          users = _startUsers + (int)Math.round(Math.pow(2.718, 1 + currentIncrease));
          break;
        case logarithm:
          users = _startUsers + (int)Math.round(Math.log(currentIncrease));
          break;
        case sine:
          users = _startUsers + (int)Math.round(Math.sin(currentIncrease));
          break;
        case cosine:
          users = _startUsers + (int)Math.round(Math.cos(currentIncrease));
          break;
        case tangent:
          users = _startUsers + (int)Math.round(Math.tan(currentIncrease));
          break;
        default:
          users = _startUsers + (int)Math.floor(currentIncrease);
          break;
      }
    }
    
    if (users > _maxUsers)
      users = _maxUsers;
      
    return users;
  }
}
