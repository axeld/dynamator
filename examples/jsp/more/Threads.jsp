<%-- generated by Dynamator Thu Oct 10 16:40:07 CDT 2002
--%>
<%@ page session="false" %>
<%
    ThreadGroup tg = Thread.currentThread().getThreadGroup();

    Thread[] threads = new Thread[tg.activeCount()];
    int n = tg.enumerate(threads);

    // ensure array is full
    if ( n < threads.length )
    {
        Thread[] newThreads = new Thread[n];
        System.arraycopy(threads, 0, newThreads, 0, n);
        threads = newThreads;
    }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Currently Executing Threads</title>
    <style type="text/css">
              #CurrentThread
              {
                  background-color: #FFFF00;
              }
            
    </style>
  </head>
  <body>
    <table border="1">
      <tr align="center">
        <td>Name</td>
        <td>Priority</td>
        <td>Daemon?</td>
      </tr>
<%
        {
          Thread[] $threads = threads;
          int lim$threads = $threads.length;
          Thread thread;
          for ( int iThreads = 0; iThreads < lim$threads; ++iThreads )
          {
            thread = $threads[iThreads];
      %>
            <tr class="threads" align="center"<%
              if ( thread == Thread.currentThread() ) 
              {
                %> id="<%= "CurrentThread" %>"<%
              }
            %>>
              <td class="name"><%= thread.getName() %></td>
              <td class="priority"><%= thread.getPriority() %></td>
              <td><%
                  if ( thread.isDaemon() ) 
                  {
                    %>Y<%
                  }
                %><%
                  if ( ! thread.isDaemon() ) 
                  {
                    %>&nbsp;<%
                  }
                %> </td>
            </tr><%
          }
        }
      %>
    </table>
  </body>
</html>
