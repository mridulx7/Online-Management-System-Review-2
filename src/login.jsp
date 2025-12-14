<form action="../login" method="post">
    <h2>Login</h2>
    Email: <input type="text" name="email" required><br>
    Password: <input type="password" name="password" required><br>
    <button type="submit">Login</button>

    <% if (request.getParameter("error") != null) { %>
        <p style="color:red;">Invalid credentials</p>
    <% } %>
</form>
