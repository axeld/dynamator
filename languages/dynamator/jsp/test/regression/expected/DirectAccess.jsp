<%-- generated by Dynamator Sat Jan 31 14:22:06 CST 2004
--%>
    <%@ page session="false" %>
    <%!
        private String getGreeting()
        {
            return "Hello World, says Dynamator!";
        }
    %>
    <%
        String countArgument = request.getParameter("count");
        int count = 
            ( countArgument == null || countArgument.length() == 0 )
            ? 5
            : Integer.parseInt(countArgument);
    %>
  <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title><%= 
        ( count == 0 ) 
        ? getGreeting()
        : String.valueOf(count)
     %></title>
<%
      if ( count > 0 ) 
      {
        %><meta http-equiv="Refresh" content="<%= 
        "1; URL=" + request.getRequestURI() + "?count=" + (count-1)
       %>"><%
      }
    %>
  </head>
  <body>
    <p align="center"><font class="DynamicText" size="+3"><%= 
        ( count == 0 ) 
        ? getGreeting()
        : String.valueOf(count)
     %></font></p>
  </body>
</html>
