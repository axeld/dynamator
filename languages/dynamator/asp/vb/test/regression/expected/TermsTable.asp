<% ' generated by Dynamator Sat Jan 31 14:56:55 CST 2004
%>
    <%@ Language=VBScript %>
    <%
        Dim terms(5)
        Dim item

        terms(0) = "epilog|The end of the generated file, after the template."
        terms(1) = "locator|An element in a Dynamator annotations file that identifies the set of elements to which a set of overrides will apply."
        terms(2) = "marker|An invisible HTML element or attribute that identifies a location in an HTML file."
        terms(3) = "override|An element in a Dynamator annotations file that specifies a set of changes to be applied to the template."
        terms(4) = "prolog|The beginning of the generated file, before the template."
        terms(5) = "template|The file containing the static text pattern to be output by the generated program."
    %>
  <html>
  <head>
    <title>Dynamator Glossary</title>
  </head>
  <body>
    <table>
      <tr>
        <td>Term</td>
        <td>Definition</td>
      </tr>
      
      <%
        For Each item in terms
      %><tr class="entry">
          <td class="term"><%= Mid(item, 1, InStr(item, "|")-1) %></td>
          <td class="definition"><%= Mid(item, InStr(item, "|")+1) %></td>
        </tr>
      <%
        Next
      %>
    </table>
  </body>
</html>
