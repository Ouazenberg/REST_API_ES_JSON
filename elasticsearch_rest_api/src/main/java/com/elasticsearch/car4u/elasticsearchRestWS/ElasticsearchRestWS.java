package com.elasticsearch.car4u.elasticsearchRestWS;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;

import org.elasticsearch.action.update.UpdateResponse;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.io.stream.NotSerializableExceptionWrapper;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsException;
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

import com.elasticsearch.car4u.model.CarFilter;
import com.elasticsearch.car4u.model.Vehicle;
import com.elasticsearch.car4u.service.VehicleDaoImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/car4u")
public class ElasticsearchRestWS {
	
	TransportClient client;
	VehicleDaoImpl vehicledao;
	ObjectMapper oMapper = new ObjectMapper();
	
	@SuppressWarnings("resource")
	public ElasticsearchRestWS() throws UnknownHostException {
		//Create the TransportClient to connect with the index agil_auto in ElasticSearch 
		this.client = new PreBuiltTransportClient(Settings.EMPTY)
		        .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

	}

/***************************** Filter index data *******************************/
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Returns the list of vehicles filtered according to the given data in filter.json")
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
	            return e.getMessage();
        }	
	}
	
/**************** Insert/Create (if does't exist) data into index ****************/
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Inserts the list of vehicles retrieved from Data-Base into agil_auto index")
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
	        responseString = response.getResult().toString(); 
		}						
	  return responseString;
        	
	}
	
/***************************** Update data in index *******************************/
	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Updates a single vehicle data whose id is given as a Path Variable") 
	@PutMapping("/vehicles/{id}")
	public Object update(@PathVariable final String id) throws IOException{

		Vehicle vehicle = new Vehicle(4000, "URB", "Noir", "DIES", 7, 4.2);
		Map<String, Object> map = oMapper.convertValue(vehicle, Map.class);
		
		try {
			 UpdateResponse response = client.prepareUpdate("agil_auto", "vehicle", id)
						.setDoc(map)
						.setFetchSource(true)
						.get();  
				
				return response.getResult().toString();
		} catch (Exception e) {
			return e.getMessage();
		}   
	}
	
/***************************** View document data in index **************************/	
	@ApiOperation(value = "Returns a single vehicle data whose id is given as a Path Variable") 
	@GetMapping("/vehicles/{id}" )
	public Object view(@PathVariable final String id) throws IOException{
		
		try {
			GetResponse response = client.prepareGet("agil_auto", "vehicle", id).get();    
			if(response.isExists())
				return response.getSource(); 
			else 
				return "[vehicle] ["+id+"]: document missing";
		
		}catch (Exception e) {
			return e.getMessage();	
		}
	}
	
/***************************** Delete document in index ****************************/	
	@ApiOperation(value = "Deletes a single vehicle whose id is given as a Path Variable") 
	@DeleteMapping("/vehicles/{id}" )
	public Object delete(@PathVariable final String id){
		try {
			DeleteResponse deleteResponse = client.prepareDelete("agil_auto", "vehicle", id).get();     
			return deleteResponse.getResult().toString();   

		} catch (Exception e) {
			return e.getMessage();
		}
	}

/***************************** Delete index *****************************************/	
	@ApiOperation(value = "Deletes completely the index whose name is given as a Path Variable") 
	@DeleteMapping("/index/{index}" )
	public Object deleteIndex(@PathVariable final String index) throws IOException{
		try {
			client.admin().indices().delete(new DeleteIndexRequest(index))
						  .actionGet();
			return index + " DELETED";
		
		} catch(Exception e) {
			return e.getMessage();   
		}		
	}

/***************************** Define mappings for index *****************************/
	@ApiOperation(value = "Sets the settings and mappings for the index agil_auto which are separately defined in json files") 
	@PostMapping("/vehicles/mappings" )
	public Object mappings() throws IOException{
		
		try {
			
			//Define The settings-analyzers of index agil_auto
			File jsonSettingsFile = new File("/home/feoua/Bureau/json/settings.json");	    		 
			String jsonSettingString = FileUtils.readFileToString(jsonSettingsFile);
			
			client.admin().indices().prepareCreate("agil_auto")
			        .setSettings(jsonSettingString, XContentType.JSON)
			        .execute().actionGet();
		
			//Define the mappings of index agil_auto
			File jsonMappingFile = new File("/home/feoua/Bureau/json/mappings.json");	    		 
			String jsonMappingString = FileUtils.readFileToString(jsonMappingFile);
			
			PutMappingRequest putMappingRequest = new PutMappingRequest("agil_auto");
					putMappingRequest.type("vehicle"); 
					putMappingRequest.source(jsonMappingString, XContentType.JSON);
					if(client.admin().indices().putMapping(putMappingRequest).actionGet().isAcknowledged())
						return "Index Created --> Mappings Defined";
					else 
						return "Exception : Wrong Format of Mappings !!";
		
		}catch (NotSerializableExceptionWrapper | ElasticsearchParseException e) {
			return "Failed to Map the index: check the provided mappings FORMAT ";
			
		}catch (SettingsException e) {
			return "Failed to set the index: check the provided settings";
					
		}catch (Exception e) {
			System.out.println(e);
			return e.getMessage();
		}	    
	}
	
}