
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Formatter;
import java.util.Locale;
import java.util.Scanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.eclipse.jdt.internal.compiler.ast.ThisReference;

import cc.mallet.examples.TopicModel;

/**
 * Servlet implementation class ServletClass
 */
 @WebServlet("/ServletClass")
//@WebServlet("/")
@MultipartConfig
public class ServletClass extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Formatter outPut = new Formatter(new StringBuilder(), Locale.US);
	private String path;
	
	// storage directory

	public long sizeOfFile = 0;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ServletClass() {
		super();
//		path = this.getServletContext().getRealPath("WEB-INF/lib/stoplist.txt");
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		ServletConfig config = getServletConfig();
		super.init(config);
		ServletContext context = config.getServletContext();
		path = context.getRealPath("WEB-INF/lib/stoplist.txt");
		System.out.println(config.getServletContext());

		PrintWriter out = response.getWriter();
		out.println("doGet() invoked");
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println(path);
		String description = request.getParameter("description"); // Retrieves <input type="text" name="description">
		Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
		InputStream fileContent = filePart.getInputStream();
		Scanner scanner = new Scanner(fileContent);
		Formatter output = new Formatter();

		TopicModelingClass topicModel = new TopicModelingClass(fileContent, path);
		try {
			output = topicModel.processMethod();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		scanner.close();
		response.getWriter().write(output.toString());

	}
	


}
