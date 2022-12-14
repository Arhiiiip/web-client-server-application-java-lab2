package servlets;

import java.io.PrintWriter;
import java.util.*;



import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.AsyncContext;
//import jakarta.servlet.ServletContext;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletResponse;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
import javax.servlet.*;
import javax.servlet.http.*;
import utils.Data;


import java.io.IOException;

public class CheckChangeServlet extends HttpServlet {
    private List<AsyncContext> contexts = new LinkedList<>();
    private Map<AsyncContext, String> mapSession = new HashMap<>();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final AsyncContext asyncContext = request.startAsync(request, response);
        mapSession.put(asyncContext, request.getSession().getId());
        asyncContext.setTimeout(10 * 60 * 1000);
        contexts.add(asyncContext);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Data bean = new Data();
        List<AsyncContext> asyncContexts = new ArrayList<>(this.contexts);
        this.contexts.clear();

        double x = Double.parseDouble(request.getParameter("x_value"));
        double y = Double.parseDouble(request.getParameter("y_value"));
        double r = Double.parseDouble(request.getParameter("r_value"));
        String executeTime = request.getParameter("exeTime");
        String currentTime = request.getParameter("curTime");
        String result = request.getParameter("result");
        String session = request.getSession().getId();

        if (result.equals("change")) {
            bean.setX(x);
            bean.setY(y);
            bean.setR(r);
            bean.setCurrentTime(currentTime);
            bean.setResult(result);
            bean.setExecuteTime(executeTime);

            for (AsyncContext asyncContext : asyncContexts) {
                try {
                    ServletResponse resp = asyncContext.getResponse();
                    String sessionAC = mapSession.get(asyncContext);
                    resp.setContentType("application/json; charset=UTF-8");
                    PrintWriter writer = resp.getWriter();
                    if (session.equals(sessionAC)) {
                        writer.println("");
                        writer.flush();
                        asyncContext.complete();
                        continue;
                    }
                    writer.println(bean.jsonBean());
                    writer.flush();
                    asyncContext.complete();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else if (result.equals("resChange")) {
            for (AsyncContext asyncContext : asyncContexts) {
                try {
                    ServletResponse resp = asyncContext.getResponse();
                    String sessionAC = mapSession.get(asyncContext);
                    resp.setContentType("application/json; charset=UTF-8");
                    PrintWriter writer = resp.getWriter();
                    if (session.equals(sessionAC)) {
                        writer.println("");
                        writer.flush();
                        asyncContext.complete();
                        continue;
                    }

                    Map<String, String> answer = new HashMap<>();
                    answer.put("answer", "resChange");
                    ObjectMapper objectMapper = new ObjectMapper();
                    writer.println(objectMapper.writeValueAsString(answer));
                    writer.flush();
                    asyncContext.complete();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}