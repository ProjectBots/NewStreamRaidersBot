package api;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Test extends HttpServlet {

	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			testGet(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void testGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		System.out.println("Get");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			testPost(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void testPost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		StringBuilder sb = new StringBuilder("Post - ").append(req.getQueryString()).append("\nHeaders:\n");
		var hns = req.getHeaderNames();
		hns.asIterator().forEachRemaining(s -> sb.append("   ").append(s).append(": ").append(req.getHeader(s)).append("\n"));
		sb.append("Body:\n");
		for(String s : req.getReader().readLine().split("&"))
			sb.append("   ").append(s).append("\n");
		System.out.println(sb.toString());
		
		switch (req.getParameter("cn")) {
		case "unlockUnit":
			resp.getWriter().append("{\"errorMessage\":null,\"data\":{\"sth\":1,\"a\":\"b\"}}").close();
			break;
		default:
			resp.getWriter().append("{\"errorMessage\": \"unknown cn (from TestServer)\"}").close();
			break;
		}
		
	}
}
