/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software;you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation;either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program;if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.catination.server.console.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.mortbay.log.Log;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;



/** 
 * A controller class to process the user related requests.  
 *
 *
 */
public class UploadController extends MultiActionController {

	protected final Log log = LogFactory.getLog(getClass());
	
	public UploadController() {
	}

	public ModelAndView list(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("upload/page");
		return mav;
	}

	public ModelAndView send(HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		uploadImage(req, resp);
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("redirect:upload.do");
		log.info("send finished");
		return mav;
	}
	
	protected void uploadImage(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		log.info("req="+req.toString());
		req.setCharacterEncoding("utf-8");
		resp.setContentType("text/html;charset=utf-8");
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1024*1024);
		ServletFileUpload sfu = new ServletFileUpload(factory);
		sfu.setFileSizeMax(1024*1024*10); //Max. 4M per file
		sfu.setSizeMax(1024*1024*10*5);
		
		String adv_info_1 = req.getParameter("file1_info");
		String adv_info_2 = req.getParameter("file2_info");
		String adv_info_3 = req.getParameter("file3_info");
		String adv_info_4 = req.getParameter("file4_info");
		String adv_info_5 = req.getParameter("file5_info");
		log.info(adv_info_1);
		try {
			List<FileItem> items = sfu.parseRequest(req);
			log.info("req.items.size()="+items.size());
			for (int i = 0;i < items.size();i++) {
				FileItem item = items.get(i);
				if(!item.isFormField()){
					ServletContext sctx = getServletContext();
					String path = sctx.getRealPath("/advertise");
					log.info(path);

					String fileName = item.getName();
					log.info(fileName);
					if (fileName == null || fileName.isEmpty()) {
						log.error("Destination file name is null!");
						continue;
					}
					
					if (!(item.getContentType().equals("image/pjpeg")
							|| item.getContentType().equals("image/x-png"))) {
						log.error("Wrong content type: "+item.getContentType());
						continue;
					}

					String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
					File file = new File(path+"\\ADVERTISEMENT_UPLOAD_PIC_"+(i+1)+fileSuffix);
					//if(!file.exists()){
						log.debug("Writed to the file:"+file.getAbsolutePath());
						item.write(file);
						//resp.sendRedirect("/index.html");
					//}
				} else {
					String field = item.getFieldName();
					if (field.equals("file1_info")) {
						adv_info_1 = item.getString();
					} else if (field.equals("file2_info")) {
						adv_info_2 = item.getString();
					} else if (field.equals("file3_info")) {
						adv_info_3 = item.getString();
					} else if (field.equals("file4_info")) {
						adv_info_4 = item.getString();
					} else if (field.equals("file5_info")) {
						adv_info_5 = item.getString();
					}
					log.debug("field='"+field+"', value='"+item.getString()+"'");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void upload(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		log.info("req="+req.toString());
		req.setCharacterEncoding("utf-8");
		resp.setContentType("text/html;charset=utf-8");
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1024*1024);
		ServletFileUpload sfu = new ServletFileUpload(factory);
		sfu.setFileSizeMax(1024*1024*10); //Max. 4M per file
		sfu.setSizeMax(1024*1024*10*5);
		String adv_info_1 = req.getParameter("file1_info");
		String adv_info_2 = req.getParameter("file2_info");
		String adv_info_3 = req.getParameter("file3_info");
		String adv_info_4 = req.getParameter("file4_info");
		String adv_info_5 = req.getParameter("file5_info");
		log.info(adv_info_1);
		try {
			List<FileItem> items = sfu.parseRequest(req);
			log.info("req.items.size()="+items.size());
			for (int i = 0;i < items.size();i++) {
				FileItem item = items.get(i);
				if(!item.isFormField()){
					ServletContext sctx = getServletContext();
					String path = sctx.getRealPath("/upload");
					log.info(path);

					String fileName = item.getName();
					log.info(fileName);
					if (fileName == null || fileName.isEmpty()) {
						log.error("Destination file name is null!");
						continue;
					}

					fileName = fileName.substring(fileName.lastIndexOf("\\")+1);
					File file = new File(path+"\\"+fileName);
					if(file.exists()){
						item.delete();
					}
					log.debug("Writed to the file:"+file.getAbsolutePath());
					item.write(file);
					//resp.sendRedirect("/index.jsp");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

