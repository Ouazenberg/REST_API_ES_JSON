package com.teckprimers.elastic.standaloneelasticexample;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;

import org.elasticsearch.action.update.UpdateResponse;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;

import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequestBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.CarFilter;
import entities.Vehicle;
import vehicleDAO.VehicleDaoImpl;

@RestController
@RequestMapping(value = "/car4u")
public class UsersResource {
	
	TransportClient client;
	VehicleDaoImpl vehicledao;
	ObjectMapper oMapper = new ObjectMapper();
	
	@SuppressWarnings("resource")
	public UsersResource() throws UnknownHostException {
		//Create the TransportClient to connect with the index agil_auto in ElasticSearch 
		this.client = new PreBuiltTransportClient(Settings.EMPTY)
		        .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

	}
	


/***************************** Filter index data *******************************/
	@SuppressWarnings("unchecked")
	@GetMapping("/vehicles" )
	public Object filter() throws IOException{
		
		 try {	 	
			 	//read filtering elements from filter.json file 
	            File filterFile = new File("/home/feoua/Bureau/json/filter.json");	    		 
	    		String filterString = FileUtils.readFileToString(filterFile);
	            
	            //Put the recovered elements into a java object carFilter
	            ObjectMapper mapper = new ObjectMapper();
	    		CarFilter carFilter = mapper.readValue(filterString, CarFilter.class);
	    		
	    		File queryFile = new File("/home/feoua/Bureau/json/query.json");	    		 
	    		String queryString = FileUtils.readFileToString(queryFile);
	    		
	    		//Execute the query and get the response of elasticSearch 
	    		Map<String, Object> template_params = oMapper.convertValue(carFilter, Map.class);
	    		
	    		SearchResponse sr = new SearchTemplateRequestBuilder(client)
	    		        .setScript(queryString)
	    		        .setScriptType(ScriptType.INLINE) 
	    		        .setScriptParams(template_params) 
	    		        .setRequest(new SearchRequest())     
	    		        .get()                               
	    		        .getResponse();        
	            	    		   		
	    		//Return the ES search results
	        	return sr.getHits();
	 
	        } catch (Exception e) {
	            return e;
        }	
	}
	
/**************** Insert/Create (if does't exist) data into index ****************/
	@SuppressWarnings("unchecked")
	@PostMapping("/vehicles" )
	public Object insert() throws IOException{
		VehicleDaoImpl vehicleDaoImpl= new VehicleDaoImpl();
		List<Vehicle> listVehicle = vehicleDaoImpl.getListVehicles();
		
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
	@PutMapping("/vehicles/{id}")
	public Object update(@PathVariable final String id) throws IOException{
		
		
		Vehicle vehicle = new Vehicle(4000, "URB", "Noir", "DIES", 7, 4.2);
				
		Map<String, Object> map = oMapper.convertValue(vehicle, Map.class);

        UpdateResponse response = client.prepareUpdate("agil_auto", "vehicle", id)
				.setDoc(map)
				.setFetchSource(true)
				.get();  
		
		return response.getResult().toString();   
	}
	
/***************************** View document data in index **************************/	
	@GetMapping("/vehicles/{id}" )
	public Object view(@PathVariable final String id) throws IOException{
		
		GetResponse response = client.prepareGet("agil_auto", "vehicle", id)
				.get();    
		return response.getSource();   
	}
	
/***************************** Delete document in index ****************************/	
	@DeleteMapping("/vehicles/{id}" )
	public Object delete(@PathVariable final String id){
		
		DeleteResponse deleteResponse = client.prepareDelete("agil_auto", "vehicle", id).get();     
		
		return deleteResponse.getResult().toString();   
	}

/***************************** Delete index *****************************************/	
	@DeleteMapping("/index/{index}" )
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
	@PostMapping("/vehicles/mappings" )
	public Object mappings() throws IOException{
		
		//Define The settings-analyzers of index agil_auto
		File jsonSettingsFile = new File("/home/feoua/Bureau/json/settings.json");	    		 
		String jsonSettingString = FileUtils.readFileToString(jsonSettingsFile);
		
		CreateIndexResponse createResponse = client.admin().indices().prepareCreate("agil_auto")
        .setSettings(jsonSettingString, XContentType.JSON)
        .execute().actionGet();

		
		//Define the mappings of index agil_auto
		File jsonMappingFile = new File("/home/feoua/Bureau/json/mappings.json");	    		 
		String jsonMappingString = FileUtils.readFileToString(jsonMappingFile);
		
		PutMappingRequest putMappingRequest = new PutMappingRequest("agil_auto");
		putMappingRequest.type("vehicle"); 
		putMappingRequest.source(jsonMappingString, XContentType.JSON);
		client.admin().indices().putMapping(putMappingRequest);
		
		return createResponse.isAcknowledged();   
	}
	
}