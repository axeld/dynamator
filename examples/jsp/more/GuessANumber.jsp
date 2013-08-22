<%-- generated by Dynamator Thu Oct 10 16:40:07 CDT 2002
--%>
<%@ page session="true" %>
<%@ page import="java.util.Vector"%>
<%!
    private static final int highValue = 100;
%>
<%
    Integer answerValue = (Integer) session.getValue("answer");
    if ( answerValue == null )
    {
        answerValue = new Integer(
            Math.abs(
                new Random(System.currentTimeMillis()).nextInt() 
                % highValue));
        session.putValue("answer", answerValue);
    }
    int answer = answerValue.intValue();
    Vector history = (Vector) session.getValue("history");
    if ( history == null )
    {
        history = new Vector();
        session.putValue("history", history);
    }
    String guessValue = request.getParameter("guess");
    int guess;
    if ( guessValue == null )
    {
        guessValue = "";
        guess = -1;
    }
    else
    {
        guess = Integer.parseInt(guessValue);
        history.addElement(new Integer(guess));
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Guess a Number</title>
  </head>
  <body>
    <hr>
    <form action="<%= 
      guess != answer 
      ? response.encodeURL(request.getRequestURI())
      : request.getRequestURI()
     %>" method="GET">
      <center>
        <p>Guess a number between 1 and <%= 
    highValue
 %>.</p>
        <table align="center">
<%
            {
              java.util.Vector $history = history;
              int lim$history = $history.size();
              Integer item;
              for ( int iItem = 0; iItem < lim$history; ++iItem )
              {
                item = (Integer) $history.elementAt(iItem);
          %>
                <tr class="HistoryRow">
                  <td><%= item %> is <%= item.intValue() < answer ? "low" : item.intValue() > answer ? "high" : "equal" %> </td>
                </tr><%
              }
            }
          %>
          <tr>
<%
              if ( guess != answer ) 
              {
                %>
                <td id="NewGuess">New guess: <input size="3" value="" maxlength="3" type="TEXT" name="guess"> </td><%
              }
            %>
          </tr>
        </table>
        <input value="<%= 
      guess != answer ? "Guess" : "New Game"
     %>" type="SUBMIT">
      </center>
    </form>
  </body>
</html>
