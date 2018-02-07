package com.runoob.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


/**
 * Servlet implementation class HelloServlet
 */
@WebServlet("/HelloServlet")
public class HelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    //storage directory
    private static final String UPLOAD_DIR = "upload";
    
    //uplaod config
    private static final int MEMORY_THRESHOLD = 1024 * 1024 *3;//3MB
    private static final int SIZE_LIMIT = 1024 * 1024 * 20;//20MB
    private static final int MAX_REQUEST_SIZE = 1024 * 1024 *30;//30MB

    
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HelloServlet() {
        super();
        // TODO Auto-generated constructor stub
    }


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    		if(!ServletFileUpload.isMultipartContent(request)) {
    			PrintWriter writer = response.getWriter();
    			writer.println("ERROR: the form must contain enctype=multipart/form-data\"");
    			writer.flush();
    			return;
    		}
    		DiskFileItemFactory factory = new DiskFileItemFactory();
    		//set memory threshold - if surpassed it'll be stored at temp directory
    		factory.setSizeThreshold(MEMORY_THRESHOLD);
    		//set temp directory
    		factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
    		
    		ServletFileUpload upload = new ServletFileUpload(factory);
    		
    		//set max upload file size
        upload.setFileSizeMax(SIZE_LIMIT);
        //set max request size(containing document and form data)
        upload.setSizeMax(MAX_REQUEST_SIZE);
        
        //construct a temp path to store uploaded files. This path is relative to current directory
        String uploadPath = getServletContext().getRealPath("./") + File.separator + UPLOAD_DIR;
        
        //create a new directory if none exists
        File uploadDir = new File(uploadPath);
        if(!uploadDir.exists()) {
        		uploadDir.mkdir();
        }
        
        
        try {
        		//extract file data from request
        		List<FileItem> formItems = upload.parseRequest(request);
        		
        		if(formItems != null && formItems.size() > 0) {
        			//iterate through form data
        			for(FileItem item : formItems) {
        				//deal with fields outside the form
        				if(!item.isFormField()) {
        					String fileName = new File(item.getName()).getName();
        					String filePath = uploadPath + File.separator + fileName;
        					File storeFile = new File(filePath);
        					
        					//display the upload path of file
        					System.out.println(filePath);
        					//store the file on the disk
        					item.write(storeFile);
        					request.setAttribute("message", "File uploaded");
        					
        				}
        			}
        		}
        	
        	
        	
        	
        }catch (Exception e) {
        		request.setAttribute("message", "Error" + e.getMessage());
        }
        getServletContext().getRequestDispatcher("/messagePage.jsp").forward(request, response);
        
        
        
        
        
    }

}