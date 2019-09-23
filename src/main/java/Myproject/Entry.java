package Myproject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

import junit.framework.AssertionFailedError;

public class Entry {
	
	private ObjectMapper objectMapper = new ObjectMapper();

	public static void main(String args[]) throws ParseException {
		System.out.println("Hello");
		JSONParser jsonParser = new JSONParser();
		try {
			FileReader reader = new FileReader("D://json//File1.json");
			FileReader reader2 = new FileReader("D://json//File2.json");
			Object obj =  jsonParser.parse(reader);
			Object obj2 =  jsonParser.parse(reader2);
			
			
			System.out.println(obj.toString());
			System.out.println(obj2.toString());
			
			Entry entry = new Entry();
			entry.assertJson(obj.toString(), obj2.toString());
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public  void assertJson(String actualJson,String modifiedJson) throws JsonProcessingException, IOException {
		JsonNode actual = objectMapper.readTree(actualJson);
		JsonNode expected = objectMapper.readTree(modifiedJson);


		String diff = JsonDiff.asJson(actual, expected).toString();
		System.out.println(diff);
		if (!diff.equals("[]")) {
			throw new AssertionFailedError("Json objects are not equal: " + diff);
		}
	}
}
