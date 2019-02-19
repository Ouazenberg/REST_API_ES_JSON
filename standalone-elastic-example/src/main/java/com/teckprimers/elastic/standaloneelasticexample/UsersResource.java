package com.teckprimers.elastic.standaloneelasticexample;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CarFilter;

@RestController
@RequestMapping(value = "/rest/users")
public class UsersResource {
	
	@SuppressWarnings("resource")
	@GetMapping("/get" )
	public Object get() throws IOException{
		
		//String jsonString = "{\"vehicle_color\":\"Blanc Alfa\",\"vehicle_transmission_type_code\":\"MECA\"}";
		
		 JSONParser parser = new JSONParser();
		 
		 try {
			 
	            Object obj = parser.parse(new FileReader("/home/feoua/Téléchargements/filter.json"));
	            JSONObject jsonObject = (JSONObject) obj;
	            String jsonString = jsonObject.toString();
	            
	            System.out.println(jsonString);
	            
	            ObjectMapper mapper = new ObjectMapper();
	    		CarFilter carFilter = mapper.readValue(jsonString, CarFilter.class);
	    		
	    		
	    		TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
	    		        .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));
	    		
	    		BoolQueryBuilder first = QueryBuilders.boolQuery()
	    				.must(QueryBuilders.termQuery("vehicle_color.keyword", carFilter.getVehicle_color()))
	    				.must(QueryBuilders.termQuery("vehicle_transmission_type_code.keyword", carFilter.getVehicle_transmission_type_code()));

	    		
	            SearchResponse searchResponse = client.prepareSearch("agil_auto").setQuery(first).get();
	            
	        	return searchResponse;
	 
	        } catch (Exception e) {
	            return e;
        }	
	}
}
