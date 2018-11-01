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

package org.apache.geronimo.daytrader.javaee6.core.direct;

import javax.servlet.http.Cookie;

public class RequestUtils {
	
	/**
	 * Encode a cookie as per RFC 2109.  The resulting string 
	 * can be used as the value for a <code>Set-Cookie</code> 
	 * header.
	 *
	 * @param cookie The cookie to encode.
	 * @return A string following RFC 2109.
	 */
	public static String encodeCookie(Cookie cookie) {

	    StringBuffer buf = new StringBuffer( cookie.getName() );
	    buf.append("=");
	    buf.append(cookie.getValue());

	    if (cookie.getComment() != null) {
	        buf.append("; Comment=\"");
	        buf.append(cookie.getComment());
	        buf.append("\"");
	    }

	    if (cookie.getDomain() != null) {
	        buf.append("; Domain=\"");
	        buf.append(cookie.getDomain());
	        buf.append("\"");
	    }

	    long age = cookie.getMaxAge();
	    if (cookie.getMaxAge() >= 0) {
	        buf.append("; Max-Age=\"");
	        buf.append(cookie.getMaxAge());
	        buf.append("\"");
	    }

	    if (cookie.getPath() != null) {
	        buf.append("; Path=\"");
	        buf.append(cookie.getPath());
	        buf.append("\"");
	    }

	    if (cookie.getSecure()) {
	        buf.append("; Secure");
	    }

	    if (cookie.getVersion() > 0) {
	        buf.append("; Version=\"");
	        buf.append(cookie.getVersion());
	        buf.append("\"");
	    }

	    return (buf.toString());
	}

}
