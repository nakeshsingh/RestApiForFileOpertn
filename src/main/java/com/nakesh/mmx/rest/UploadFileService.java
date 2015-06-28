 package com.nakesh.mmx.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.nakesh.mmx.filemanagement.FileManager;
import com.nakesh.mmx.sql.SqlOperation;

@Path("/file")
public class UploadFileService {

	@POST
	@Path("/upload")
	@Consumes("multipart/form-data")
	public Response uploadFile(MultipartFormDataInput input) {

		String fileName = "";
		String filePath = "";
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get("uploadedFile");
		byte[] inputFileBytes = null;

		for (InputPart inputPart : inputParts) {

			MultivaluedMap<String, String> header = inputPart.getHeaders();
			FileManager fileManager = new FileManager();
			fileName = fileManager.getFileName(header);
			String contentType = header.getFirst("Content-Type");
			
			//Check for invalid contentType.
			if(!validateFile(contentType)) {
				return Response.status(200).entity("file : " + fileName + "  is not aalowed to download because of Content-Type : " + contentType + " is not allowed").build();
			}
			inputFileBytes = getInputStream(inputPart);
			
			//Check for empty file.
			if(inputFileBytes.length == 0) {
				return Response.status(200).entity("file : " + fileName + "  is empty!").build();
			}
			
			//constructs upload file path
		    filePath = fileManager.getFilePathToUpload(fileName);
			
		    //Write file on Server.
		    fileManager.writeFile(inputFileBytes, filePath);
		    System.out.println("Input file : " + fileName + "   is written to server file");
			
		    //Check for duplicate file.
		    if(isSameFile(filePath, fileName, contentType, inputFileBytes)) {
		    	return Response.status(200).entity("Input file : " + fileName + "   already exist on server database").build();
		    }
			saveFileInDataBase(new File(filePath), contentType, inputFileBytes.length);
			System.out.println("Input file : " + fileName + "   is uploaded to server database");
			
			List<Map<String, Object>> dataList = SqlOperation.getDataFromDataBase();
			System.out.println("Douwnloaded files from server database");
			
			fileManager.writeData(dataList);
			System.out.println("Douwnloaded files to server file");
			
			System.out.println("uploadFile is called ::: Uploaded file name = " + fileName + "   ::   filePath = " + filePath + "   ::   content-type = " + contentType);
			System.out.println(".....Done.....");
		}
		return Response.status(200).entity("uploadFile is called ::: Uploaded file name = " + fileName + "   ::   filePath = " + filePath).build();
	}
	
	@GET
	@Path("/get")
	@Produces("image/text") //@PathParam("fileName") String _fileName, @PathParam("version") String _version
	public Response getFile(@DefaultValue("") @QueryParam("fileName") String _fileName,
			@DefaultValue("")@QueryParam("version") String _version) {
		String filePathDir = "/Users/nsingh/Pictures/DownloadFromServer/";
		File file = new File(filePathDir);
		if(!file.isDirectory()) {	
			file.mkdirs();
		}
		String filePath = filePathDir + _fileName;
		
		byte[] dataByte = (byte[]) SqlOperation.getfileFromDataBase(_fileName, _version).get(4);
		ResponseBuilder response = null;
		if(dataByte == null) {
			response = Response.ok("No data is there");
		} else {
		FileManager fileManager = new FileManager();
		fileManager.writeFile(dataByte, filePath);
		File fileToSend = new File(filePath);
			response = Response.ok((Object) fileToSend);
		response.header("Content-Disposition",
			"attachment; filename=" + _fileName);
		}
		return response.build();
 
	}
	
	@GET
	@Path("/getByVerson/{version}")
	@Produces("image/text")
	public Response getFileByVersionId(@PathParam("version") String _version) {
		String filePathDir = "/Users/nsingh/Pictures/DownloadFromServer/";
		File file = new File(filePathDir);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		List<Object> storedDataList = SqlOperation.getfileFromDataBase(_version);
		String filePath = filePathDir + storedDataList.get(0);
		byte[] dataByte = (byte[])storedDataList.get(4);
		ResponseBuilder response = null;
		if (dataByte == null) {
			response = Response.ok("No data is there");
		} else {
			FileManager fileManager = new FileManager();
			fileManager.writeFile(dataByte, filePath);
			File fileToSend = new File(filePath);
			response = Response.ok((Object) fileToSend);
			response.header("Content-Disposition", "attachment; filename=" + storedDataList.get(0));
		}
		return response.build();

	}
	
	private void saveFileInDataBase(File _inputFile, String _contentType, int _length) {
		String version = SqlOperation.getLatestVersion();
		SqlOperation.saveToDataBase(_inputFile, _contentType, getNextVersion(version), _length);
	}
	
	private String getNextVersion(String _version) {
		if(_version == null) {
			return "v_1";
		} else {
			String[] versionArray = _version.trim().split("_");
			return versionArray[0] + "_" + (Integer.parseInt(versionArray[1]) + 1);
		}
	}
	
	private byte[] getInputStream(InputPart _inputPart) {
		try {
			InputStream inputStream = _inputPart.getBody(InputStream.class, null);
			return IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private boolean validateFile(String _contentType) {
		if(_contentType.contains("x-javascript") || _contentType.contains("sql") || _contentType.contains("octet-stream")
				 || _contentType.contains("exe") || _contentType.contains("x-winexe")  || _contentType.contains("application/x-msdos-program")) {
			return false;
		} 
		return true;
	}
	
	private boolean compareFiles(File _file1, List<Object> storedDataList) {
		FileManager fileManager = new FileManager();
		String storedFilePath = "/Users/nsingh/Pictures/Uploaded/ToCompare/";
		File file = new File(storedFilePath);
		if(!file.isDirectory()) {
			file.mkdirs();
		}
		storedFilePath = storedFilePath + storedDataList.get(0);
		fileManager.writeFile((byte[])storedDataList.get(4), storedFilePath);
		boolean compareResult = false;
		try {
			if(FileUtils.contentEquals(_file1, new File(storedFilePath))) {
				compareResult = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        System.out.println("Are the files are same? " + compareResult);
        return compareResult;
	}
	
	private boolean isSameFile(String _inputFilePath, String _fileName, String _contentType, byte[] inputFileByte) {
		List<Object> storedDataList = SqlOperation.getfileFromDataBase(_fileName, null);
	    if(!storedDataList.isEmpty() && _fileName.equalsIgnoreCase((String) storedDataList.get(0)) 
	    		&& _contentType.equalsIgnoreCase((String) storedDataList.get(1)) 
	    		&& (inputFileByte.length == Integer.parseInt((String) storedDataList.get(3)))	
	    		&& compareFiles(new File(_inputFilePath), storedDataList)) {
	    	return true;
	    }
	    return false;
	}
	
	public static void main(String[] args) throws Exception 
    {
            /* Get the files to be compared first */
            File file1 = new File("/Users/nsingh/Pictures/pasa.jpeg");
            File file2 = new File("/Users/nsingh/Pictures/Uploaded/.jpeg");
    
            boolean compareResult = FileUtils.contentEquals(file1, file2);
            System.out.println("Are the files are same? " + compareResult);
            
    }
}