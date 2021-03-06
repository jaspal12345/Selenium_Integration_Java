package com.qainfotech.ImageAccessibility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


import Report.TableBuilder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageWebdriver extends ChromeDriver {
	private String Server_URL = "http://localhost:5000/api/upload_image_get_relevancy";
	Document document = new Document();
    PdfPTable  Table= null;
 
    public ImageWebdriver() {
    	document.setPageSize(PageSize.A4);
    	try {
			Table= TableBuilder.createTable();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
			PdfWriter.getInstance(document, new FileOutputStream("Report.pdf"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        document.open();
    }
		
    	public void close() {
    		try {
    			document.add(Table);
    		} catch (DocumentException e) {
    			e.printStackTrace();
    		}
            document.close();
            System.out.println( "PDF report Created! in src folder Report.pdf" );
            super.close();
            
            File dir=new File("Images");
            for(File file: dir.listFiles())
            {
                if (!file.isDirectory() && !file.getName().contains("gitkeep")) { 
                    file.delete();
                }
    	}
    	}
	
	public boolean is_Alt_Text_Relvant(WebElement element) {

		String src_url =element.getAttribute("src");
		String alt_text =element.getAttribute("alt");
		String vicinityText =element.findElement(By.xpath("../..")).getText();
		String filename = null;
		OkHttpClient client = new OkHttpClient().newBuilder()  
		        .connectTimeout(1, TimeUnit.MINUTES)
		        .readTimeout(30, TimeUnit.SECONDS)
		        .writeTimeout(15, TimeUnit.SECONDS)
		        .build();
		
		try {
			filename = this.saveImage(src_url);
			File file = new File("Images/" + filename);
			RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart("file", filename, RequestBody.create(MediaType.parse("text/plain"), file))
					.addFormDataPart("alt", alt_text).addFormDataPart("vicinity", vicinityText).build();
			Request request = new Request.Builder().url(Server_URL).post(formBody).build();
			Response response = client.newCall(request).execute();
			String jsonResponse=response.body().string();
			   JsonElement jelement = new JsonParser().parse(jsonResponse);
			    JsonObject  jobject = jelement.getAsJsonObject();
			    String result = jobject.get("result").getAsString();
			    JsonArray text_classes = jobject.getAsJsonArray("text_classes");
			    String texts="";
			    for(int i=0;i<text_classes.size();i++)
			    {
			    	texts=texts+"::"+text_classes.get(i).getAsString();
			    }
			    				    
			    JsonArray possible_classes = jobject.getAsJsonArray("possible_texts");
			    JsonObject TempObj=new JsonObject();
			    String Possible_texts="";
			    for(int i=0;i<possible_classes.size();i++)
			    {
			    	TempObj=possible_classes.get(i).getAsJsonObject();
			    	Possible_texts=Possible_texts+"::"+TempObj.get("Entity").getAsString();
			    }
			    System.out.println("------------------------Result for Image::"+src_url+"-----------------------");
			    System.out.println("\n================Image Indentification Result====================\n\t"+Possible_texts.replace("::","\n\t")+"\n+++++++++Text classes Results ++++++++\n\t"+texts.replace("::","\n\t")+"\n=======================================================");
			    TableBuilder.addNewRow(Table,"Images/" + filename,alt_text,result,Possible_texts.replace("::","\n"));
			    
			    if(result.contains("RED"))
			    {   
			    	//System.out.println("Expected text classes\t"+texts+ " to be in Possible texts"+Possible_texts );
			    	System.out.println("\nFAIL\n");
			    	return false;
			    }
			    else
			    {   
			    	System.out.println("\nPASS\n");
			    	return true;
			    }
			    
		}

		catch (Exception e) {
			System.out.println("============Exception occured==============");
			e.printStackTrace();
		}
	
		return false;

	}
	public boolean is_Alt_Text_Relvant(WebElement element,String url) {
		this.Server_URL=url;
		String src_url =  element.getAttribute("src");
		String alt_text = element.getAttribute("alt");
		String vicinityText = element.findElement(By.xpath("../..")).getText();
		String filename = null;
		File file=null;
		OkHttpClient client = new OkHttpClient();
		try {
			filename = this.saveImage(src_url);
			 file = new File("Images/" + filename);
			RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart("file", filename, RequestBody.create(MediaType.parse("text/plain"), file))
					.addFormDataPart("alt", alt_text).addFormDataPart("vicinity", vicinityText).build();
			Request request = new Request.Builder().url(Server_URL).post(formBody).build();
			Response response = client.newCall(request).execute();
			String jsonResponse=response.body().string();
			   JsonElement jelement = new JsonParser().parse(jsonResponse);
			    JsonObject  jobject = jelement.getAsJsonObject();
			    String result = jobject.get("result").getAsString();
			    JsonArray text_classes = jobject.getAsJsonArray("text_classes");
			    String texts="";
			    for(int i=0;i<text_classes.size();i++)
			    {
			    	texts=texts+"::"+text_classes.get(i).getAsString();
			    }
			    				    
			    JsonArray possible_classes = jobject.getAsJsonArray("possible_texts");
			    JsonObject TempObj=new JsonObject();
			    String Possible_texts="";
			    for(int i=0;i<possible_classes.size();i++)
			    {
			    	TempObj=possible_classes.get(i).getAsJsonObject();
			    	Possible_texts=Possible_texts+"::"+TempObj.get("Entity").getAsString();
			    }
			 
			    file.delete();
			    if(result.contains("RED"))
			    {   
			    	//System.out.println("\t\t\t\t\tExpected text classes\t"+texts+ "to be in Possible texts"+Possible_texts );
			    	System.out.println("\nFAIL\n");
			    	return false;
			    }
			    else
			    {   
			    	System.out.println("\nPASS\n");
			    	return true;
			    }
			    
		}

		catch (Exception e) {
			System.out.println("======================Exception occured in finding out the relvancy in Image=================");
			e.printStackTrace();
		}
		return false;

	}

	public String saveImage(String imageUrl) throws IOException {
		File Source_url = new File(imageUrl);
		String fileName = Source_url.getName();

		URL url = new URL(imageUrl);
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream("Images/" + fileName);

		byte[] b = new byte[2048];
		int length;

		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}

		is.close();
		os.close();
		return fileName;
	}


}
