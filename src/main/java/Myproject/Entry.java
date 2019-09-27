package myproject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonPointer;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

public class Entry {

	private ObjectMapper objectMapper = new ObjectMapper();

	private static Set<String> stateList= new HashSet<String>();

	public static void main(String args[]) throws ParseException, IOException {
		System.out.println("Hello");
		JSONParser jsonParser = new JSONParser();
		/*FileReader reader = new FileReader("D://json//File1.json");
		FileReader reader2 = new FileReader("D://json//File2.json");*/
		JsonReader reader1 = Json.createReader(new FileReader("D://json//File3.json"));
		JsonReader reader2 = Json.createReader(new FileReader("D://json//File4.json"));
		try {

			/*	Object obj =  jsonParser.parse(reader);
			Object obj2 =  jsonParser.parse(reader2);


			System.out.println(obj.toString());
			System.out.println(obj2.toString());

			Entry entry = new Entry();
			entry.assertJson(obj.toString(), obj2.toString());*/



			JsonStructure jsonStructure1 = reader1.read();
			JsonStructure jsonStructure2 = reader2.read();
			

			System.out.println(jsonStructure1.toString());
			System.out.println(jsonStructure2.toString());

			Entry entry = new Entry();
			String diff= entry.assertJson(jsonStructure1.toString(), jsonStructure2.toString());

			System.out.println("diff checker "+diff);
			retreiveStatesForDiff(diff,jsonStructure1);

		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			reader1.close();
			reader2.close();
		}
	}

	private static void retreiveStatesForDiff(String diff, JsonStructure jsonStructure1) throws ParseException {
		JSONParser parser = new JSONParser(); 
		JSONArray json = (JSONArray) parser.parse(diff);

		Iterator<JSONObject> itr2 = json.iterator(); 
		while(itr2.hasNext()) {
			JSONObject itr1 = (JSONObject)itr2.next();

			String operand= itr1.get("op").toString();
			String path= itr1.get("path").toString();

			if(path.contains("/header/RCSId")|| path.contains("/uuid")||path.startsWith("/stateFlow"))
				continue;
			if(path.contains("/header/") && !path.contains("/header/RCSId")) {
				stateList.add("ALL_STATES");
				continue;
			}
			// create a proper json pointer based on the parameter that was modified 
			int res = new Scanner(path).useDelimiter("\\D+").nextInt();
			String pathListPointer = "/states/"+res+"/id";

			JsonPointer jsonPointer = Json.createPointer(pathListPointer);
			String eachErrorState = "";

			if(itr1.get("value")!=null && jsonPointer.getValue(jsonStructure1) instanceof JsonString ) {
				JsonString js= (JsonString)jsonPointer.getValue(jsonStructure1);
				System.out.println(path+" -> "+js.getString());
				eachErrorState=js.getString();
			}

			if(eachErrorState!=null && !eachErrorState.isEmpty())
				stateList.add(eachErrorState);

			//add extra state names based on a few separate conditions
			if(itr1.get("value")!=null && path.contains("/destinations/")) {
				System.out.println(path+" -> "+itr1.get("value").toString());
				stateList.add(itr1.get("value").toString());
			}
			addSeparateState(path,jsonStructure1);

		}
		System.out.println(stateList);
	}


	private static void addSeparateState(String path,JsonStructure jsonStructure1) throws ParseException {
		if(path.contains("/fields/")) {
			String fieldPath= path.substring(0,path.indexOf("/fields/")+10)+"name";
			JsonPointer jsonPointer = Json.createPointer(fieldPath);
			JsonString js= (JsonString)jsonPointer.getValue(jsonStructure1);
			System.out.println("Field name is "+js);

			JSONParser parser = new JSONParser(); 
			JSONObject json = (JSONObject) parser.parse(jsonStructure1.toString());
			JSONArray jsArray = (JSONArray)json.get("states");
			Iterator<JSONObject> itr2 = jsArray.iterator(); 
			while(itr2.hasNext()) {
				JSONObject itr1 = (JSONObject)itr2.next();	
				if(itr1.toString().contains((CharSequence)js.toString())){
					System.out.println(path+" -> "+itr1.get("id").toString());
					stateList.add(itr1.get("id").toString());
				}
			}

		}
	}

	public  String assertJson(String actualJson,String modifiedJson) throws JsonProcessingException, IOException {
		JsonNode actual = objectMapper.readTree(actualJson);
		JsonNode expected = objectMapper.readTree(modifiedJson);


		String diff = JsonDiff.asJson(actual, expected).toString();
		System.out.println(diff);
		/*if (!diff.equals("[]")) {
			throw new AssertionFailedError("Json objects are not equal: " + diff);
		}*/


		return diff;
	}
}
