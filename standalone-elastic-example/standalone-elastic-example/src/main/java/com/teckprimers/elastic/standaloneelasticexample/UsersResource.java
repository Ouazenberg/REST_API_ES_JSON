package com.teckprimers.elastic.standaloneelasticexample;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import entities.CarFilter;
import entities.Vehicle;
import vehicleDAO.VehicleDaoImpl;

@RestController
@RequestMapping(value = "/car4u")
public class UsersResource {
	
	TransportClient client;
	VehicleDaoImpl vehicledao;
	
	@SuppressWarnings("resource")
	public UsersResource() throws UnknownHostException {
		//Create the TransportClient to connect with the index agil_auto in ElasticSearch 
		this.client = new PreBuiltTransportClient(Settings.EMPTY)
		        .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

	}
	


/***************************** Filter index data *******************************/
	@GetMapping("/filter" )
	public Object filter() throws IOException{
		
		 try {	 	
			 	//read filtering elements from filter.json file 
	            File jsonFile = new File("/home/feoua/Téléchargements/filter.json");	    		 
	    		String jsonString = FileUtils.readFileToString(jsonFile);
	            
	            //Print the jsonString 
	            System.out.println(jsonString);
	            
	            //Put the recovered elements into a java object carFilter
	            ObjectMapper mapper = new ObjectMapper();
	    		CarFilter carFilter = mapper.readValue(jsonString, CarFilter.class);
	    		
	    		//Set the query to retrieve the needed data depending on the filter elements
	    		BoolQueryBuilder query = QueryBuilders.boolQuery()
	    				.must(QueryBuilders.matchQuery("vehicle_color", carFilter.getVehicle_color()))
	    				.must(QueryBuilders.matchQuery("vehicle_fiscal_power", carFilter.getvehicle_fiscal_power()));
	    		
	    		//Execute the query and get the response of elasticSearch 
	    		SearchResponse searchResponse = client.prepareSearch("agil_auto").setQuery(query).get();
	            	    		
//	    		//Convert the jsonString to a jsonObject 
//	    		JsonElement jelement = new JsonParser().parse(jsonString);
//	    		JsonObject jobject = jelement.getAsJsonObject();
//	    		
//	    		//Set the changes for the update
//	    		jobject.addProperty("vehicle_color", "Blanc Alfa");
//	    		jobject.addProperty("vehicle_transmission_type_code", "Auto");
//		    		
//	    		//Write and update the new changes into the jsonFile
//	    		Gson gson = new Gson();
//	    		String resultingJson = gson.toJson(jelement);
//	    		FileUtils.writeStringToFile(jsonFile, resultingJson);
	    		
	    		//Return the ES search results
	        	return searchResponse.getHits();
	 
	        } catch (Exception e) {
	            return e;
        }	
	}
	
/**************** Insert/Create (if does't exist) data into index ****************/
	@SuppressWarnings("unchecked")
	@GetMapping("/insert" )
	public Object insert() throws IOException{
		VehicleDaoImpl vehicleDaoImpl= new VehicleDaoImpl();
		java.util.List<Vehicle> listVehicle = vehicleDaoImpl.getListVehicles();
		
		String responseString = "";
		ObjectMapper oMapper = new ObjectMapper();
		
		for(Vehicle vehicle: listVehicle) {
	        Map<String, Object> map = oMapper.convertValue(vehicle, Map.class);
	        IndexResponse response = client.prepareIndex("agil_auto", "vehicle", ""+vehicle.getVehicle_id())
					.setSource(map)
					.get();
	        responseString += response.getResult().toString()+"\n"; 
		}						
	  return responseString;
        	
	}
	
/***************************** Update data in index *******************************/
	@SuppressWarnings("unchecked")
	@GetMapping("/update" )
	public Object update() throws IOException{
		
		java.util.List<Vehicle> listVehicle = new ArrayList<Vehicle>();
		listVehicle.add(new Vehicle(12, "URB", "Noir", "DIES", 7, 4.2));		
		listVehicle.add(new Vehicle(126, "CIT", "Noir", "ESSE", 7, 3.7));
		
		ObjectMapper oMapper = new ObjectMapper();
		String responseString ="";
		
		for(Vehicle vehicle: listVehicle) {
	        Map<String, Object> map = oMapper.convertValue(vehicle, Map.class);
	        UpdateResponse response = client.prepareUpdate("agil_auto", "vehicle", ""+vehicle.getVehicle_id())
					.setDoc(map)
					.setFetchSource(true)
					.get();    
	        responseString += response.getResult().toString(); 
		}
		return responseString;   
	}
	
/***************************** View document data in index **************************/	
	@GetMapping("/view/{id}" )
	public Object view(@PathVariable final String id) throws IOException{
		
		GetResponse response = client.prepareGet("agil_auto", "vehicle", id)
				.get();    
		return response.getSource();   
	}
	
/***************************** Delete document in index ****************************/	
	@GetMapping("/delete/{id}" )
	public Object delete(@PathVariable final String id) throws IOException{
		
		DeleteResponse deleteResponse = client.prepareDelete("agil_auto", "vehicle", id).get();     
		
		return deleteResponse.getResult().toString();   
	}

/***************************** Delete index *****************************************/	
	@GetMapping("/deleteIndex/{index}" )
	public Object deleteIndex(@PathVariable final String index) throws IOException{
		try {
			client.admin().indices().delete(new DeleteIndexRequest(index))
						  .actionGet();
		} catch(Exception e) {
			return index + " NOT FOUND";   
		}
		return index + " DELETED";
	}

/***************************** Define mappings for index *****************************/
	@GetMapping("/mappings" )
	public Object mappings() throws IOException{
		
		//Define The settings-analyzers of the mappings
		XContentBuilder settings = XContentFactory.jsonBuilder()
            .startObject()
            	.startObject("index")
	                .startObject("analysis")
	                	.startObject("filter")
	                		.startObject("autocomplete_filter")
		                		.field("type", "edge_ngram")
		                		.field("min_gram", 1)
		                		.field("max_gram", 20)
		                	.endObject()
	                	.endObject()
	                    .startObject("analyzer")
	                        .startObject("analyzer_case_insensitive")
	                            .field("tokenizer", "keyword")
	                            .field("filter", "lowercase")
	                        .endObject()
	                        .startObject("autocomplete")
	                        	.field("type", "custom")
                            	.field("tokenizer", "standard")
                            	.field("filter", new String[]{"autocomplete_filter", "lowercase"})
                            .endObject()
	                    .endObject()
	                .endObject()
	            .endObject()
            .endObject();
		
		CreateIndexResponse createResponse = client.admin().indices().prepareCreate("agil_auto")
        .setSettings(settings)
        .execute().actionGet();
		
		System.out.println(createResponse.isAcknowledged());
		
		//Define the mappings of index agil_auto
		XContentBuilder mapping = XContentFactory.jsonBuilder()
			.startObject()
                .startObject("vehicle")
                     .startObject("properties")			
                         .startObject("vehicle_id")
                             .field("type", "long")
	                     .endObject()
	                     .startObject("vehicle_fiscal_power")
	                         .field("type", "long")
	                     .endObject()
                         .startObject("vehicle_color")
	                         .field("type", "text")
	                         .startObject("fields")
	                         	.startObject("keyword")
	                         		.field("type", "keyword")
	                         		.field("ignore_above",256)
	                         	.endObject()
	                         .endObject()
	                         .field("analyzer", "autocomplete")
	                     .endObject()
	                     .startObject("vehicle_segment_code")
	                         .field("type", "text")
	                         .startObject("fields")
	                         	.startObject("keyword")
	                         		.field("type", "keyword")
	                         		.field("ignore_above",256)
	                         	.endObject()
	                         .endObject()
	                         .field("analyzer", "analyzer_case_insensitive")
	                     .endObject()
	                     .startObject("vehicle_energy_type_code")
	                         .field("type", "text")
	                         .startObject("fields")
	                         	.startObject("keyword")
	                         		.field("type", "keyword")
	                         		.field("ignore_above",256)
	                         	.endObject()
	                         .endObject()
	                         .field("analyzer", "analyzer_case_insensitive")
                         .endObject()
                         .startObject("vehicle_mixed_consumption")
	                         .field("type", "double")
	                     .endObject()
	                 .endObject()
                .endObject()
            .endObject();
		
		PutMappingResponse putMappingResponse = client.admin().indices()
                .preparePutMapping("agil_auto")
                .setType("vehicle")
                .setSource(mapping)
                .execute().actionGet();
		
		System.out.println(putMappingResponse.isAcknowledged());
		
		return 1;   
	}
	
}