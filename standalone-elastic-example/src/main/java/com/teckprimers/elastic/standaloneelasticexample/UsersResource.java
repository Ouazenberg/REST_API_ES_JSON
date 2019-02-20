package com.teckprimers.elastic.standaloneelasticexample;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import entities.CarFilter;

@RestController
@RequestMapping(value = "/rest/users")
public class UsersResource {
	
	@SuppressWarnings("resource")
	@GetMapping("/get" )
	public Object get() throws IOException{
		
		 try {	 	
			 	//read filtering elements from filter.json file 
	            File jsonFile = new File("/home/feoua/Téléchargements/filter.json");	    		 
	    		String jsonString = FileUtils.readFileToString(jsonFile);
	            
	            //Print the jsonString 
	            System.out.println(jsonString);
	            
	            //Put the recovered elements into a java object carFilter
	            ObjectMapper mapper = new ObjectMapper();
	    		CarFilter carFilter = mapper.readValue(jsonString, CarFilter.class);
	    		
	    		//Create the TransportClient to connect with the index agil_auto in ElasticSearch 
	    		TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
	    		        .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));
	    		
	    		//Set the query to recover the needed data depending on the filter elements
	    		BoolQueryBuilder first = QueryBuilders.boolQuery()
	    				.must(QueryBuilders.matchQuery("vehicle_color", carFilter.getVehicle_color()))
	    				.must(QueryBuilders.matchQuery("vehicle_transmission_type_code", carFilter.getVehicle_transmission_type_code()));
	    		
	    		//Execute the query and get the response of elasticSearch 
	    		SearchResponse searchResponse = client.prepareSearch("agil_auto").setQuery(first).get();
	            	    		
	    		//Convert the jsonString to a jsonObject 
	    		JsonElement jelement = new JsonParser().parse(jsonString);
	    		JsonObject jobject = jelement.getAsJsonObject();
	    		
	    		//Set the changes for the update
	    		jobject.addProperty("vehicle_color", "Gris Acier");
	    		jobject.addProperty("vehicle_transmission_type_code", "Auto");
	    		
	    		//Write and update the new changes into the jsonFile
	    		Gson gson = new Gson();
	    		String resultingJson = gson.toJson(jelement);
	    		FileUtils.writeStringToFile(jsonFile, resultingJson);
	    		 
	    		//Return the ES search results
	        	return searchResponse;
	 
	        } catch (Exception e) {
	            return e;
        }	
	}
}
