package org.apache.catalina.loader;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = "/annotatedServlet")
public class MyAnnotatedServlet extends HttpServlet {

    static final String MESSAGE = "This is generated by an annotated servlet";
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        resp.setContentType("test/plain");
        resp.getWriter().println(MESSAGE);
    }

}
