package com.nakesh.mmx.filemanagement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

public class FileManager {
	private final String UPLOADED_FILE_PATH = "/Users/nsingh/Pictures/Uploaded/";
	
	/**
	 * header sample
	 * {
	 * 		Content-Type=[image/png], 
	 * 		Content-Disposition=[form-data; name="file"; filename="filename.extension"]
	 * }
	 **/
	//get uploaded filename
	public String getFileName(MultivaluedMap<String, String> header) {

			String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
			
			for (String filename : contentDisposition) {
				if ((filename.trim().startsWith("filename"))) {

					String[] name = filename.split("=");
					
					String finalFileName = name[1].trim().replaceAll("\"", "");
					return finalFileName;
				}
			}
			return "unknown";
		}
	
	public String getFilePathToUpload(String _fileName) {
		File file = new File(UPLOADED_FILE_PATH);
		if(!file.isDirectory()) {
			file.mkdirs();
		}
		return UPLOADED_FILE_PATH + _fileName;
	}
	
		public void writeFile(byte[] content, String _filePath) {

			try {
				File file = new File(_filePath);

				if (!file.exists()) {
					file.createNewFile();
				}

				FileOutputStream fop = new FileOutputStream(file);

				fop.write(content);
				fop.flush();

				fop.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		public void writeData(List<Map<String, Object>> _dataList) {
			for (Map<String, Object> dataMap : _dataList) {
				writeDataToFile(dataMap);
			}
		}
		
		public void writeDataToFile(Map<String, Object> _dataMap) {
			String copiedFilePath = UPLOADED_FILE_PATH + "Downloaded/";
			File file = new File(copiedFilePath);
			if(!file.isDirectory()) {
				file.mkdirs();
			}
			writeFile((byte[]) _dataMap.get("fileData"), copiedFilePath +  _dataMap.get("name"));
		}
}
