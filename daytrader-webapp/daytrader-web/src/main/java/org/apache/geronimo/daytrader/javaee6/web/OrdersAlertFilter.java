/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.daytrader.javaee6.web;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebFilter;


import org.apache.geronimo.daytrader.javaee6.web.TradeAction;
import org.apache.geronimo.daytrader.javaee6.core.api.TradeServices;
import org.apache.geronimo.daytrader.javaee6.core.direct.RequestUtils;
import org.apache.geronimo.daytrader.javaee6.utils.Log;
import org.apache.geronimo.daytrader.javaee6.utils.TradeConfig;


@WebFilter("/app")
public class OrdersAlertFilter implements Filter {

    /**
     * Constructor for CompletedOrdersAlertFilter
     */
    public OrdersAlertFilter() {
        super();
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    private FilterConfig filterConfig = null;
    public void init(FilterConfig filterConfig) throws ServletException {
          this.filterConfig = filterConfig;
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(
        ServletRequest req,
        ServletResponse resp,
        FilterChain chain)
        throws IOException, ServletException {
        if (filterConfig == null)
            return;
        
        try
        {
            String action = req.getParameter("action");
            if ( action != null ) 
            {
                action = action.trim();
                if ( (action.length() > 0) && (!action.equals("logout")) )
                {
                    String userID = null;
                    if ( action.equals("login") )
                        userID = req.getParameter("uid");
                    else
                    {
                    	
                    	
                    	
                        HttpSession session = ((HttpServletRequest)req).getSession(false);
                        if (session == null) // generate new session
                        {
                        	session = ((HttpServletRequest)req).getSession(true);
                            //set session expiry to 5 minutes
                            session.setMaxInactiveInterval(5*60); 
                        }

                        // use cookies; instead of session attributes
//                        userID = (String) session.getAttribute("uidBean");
//                        boolean userIdCookieFound = false;
                        Cookie[] cookies = ((HttpServletRequest)req).getCookies();
                        if (cookies != null)
                        {
                        	for(Cookie cookie : cookies)
                        	{		
                                if (cookie.getName().equals("uidBean"))
                                {   
                                	System.out.println("OrdersAlertFilter:doFilter() - userID cookie found: " + RequestUtils.encodeCookie(cookie) );   
                                	if ( (cookie.getValue() != null) && (cookie.getValue().trim().length()>0) )
                                	{
                                    	userID = cookie.getValue();	
                                	}
                                	else
                                	{
                                		userID = null;
                                	}
//                                	userIdCookieFound = true;                                 	
                                	break;
                                }
                        	}
//                        	if (!userIdCookieFound)
//                        	{
//                               	System.out.println("OrdersAlertFilter:doFilter() - userID cookie not found" );
//                        	}
                        }
//                        else
//                        {
//                        	System.out.println("OrdersAlertFilter:doFilter() -  the request has no cookies" );
//                        }
                        
                        
                    }
                    if ( (userID != null) && (userID.trim().length()>0) )
                    {    
                        TradeServices tAction=null;
                        tAction = new TradeAction();
                        java.util.Collection closedOrders = tAction.getClosedOrders(userID);
                        if ( (closedOrders!=null) && (closedOrders.size() > 0) ) {
                            req.setAttribute("closedOrders", closedOrders);
                        }
                        if (Log.doTrace()) {
                            Log.printCollection("OrderAlertFilter: userID="+userID+" closedOrders=", closedOrders);
                        }
                    }
                }    
            }
        }
        catch (Exception e)
        {
            Log.error(e, "OrdersAlertFilter - Error checking for closedOrders");
        }

        ServletContext sc = filterConfig.getServletContext();
        
        //String xyz = (String) sc.getAttribute("hitCounter");
        chain.doFilter(req, resp/*wrapper*/);        

    }

    /**
     * @see Filter#destroy()
     */
    public void destroy() {
        this.filterConfig = null;    
    }

}

