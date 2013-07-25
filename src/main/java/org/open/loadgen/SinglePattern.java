/**
 * SMART - State Machine ARchiTecture
 *
 * Copyright (C) 2012 Individual contributors as indicated by
 * the @authors tag
 *
 * This file is a part of SMART.
 *
 * SMART is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SMART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * */
 
/**
 * ************************************************************
 * HEADERS
 * ************************************************************
 * File:                org.open.loadgen.SinglePattern
 * Author:              vjaasti
 * Revision:            1.0
 * Date:                Jul 16, 2013
 *
 * ************************************************************
 * REVISIONS
 * ************************************************************
 * <Purpose>
 *
 * ************************************************************
 * */

package org.open.loadgen;

import java.util.Date;

public class SinglePattern extends Pattern
{
    boolean completed;

    public SinglePattern(Date start, Date end, graphtype type)
    {
        super(start, end, type);
        // TODO Auto-generated constructor stub
        completed = false;
    }

    @Override
    public int getCurrentNumberOfUsers(int occurrence)
    {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public int getMaxUsers()
    {
        // TODO Auto-generated method stub
        return 1;
    }
    
    public boolean isRunningNow()
    {
        System.out.println("Single pattern:DONE:"+completed);
        if(!completed)
        {   
            completed = true;
            return true;
        }
            
        return false;
    }

}
