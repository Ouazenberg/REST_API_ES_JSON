package com.elasticsearch.car4u.service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.elasticsearch.car4u.model.Vehicle;
import com.fasterxml.jackson.databind.ObjectMapper;


public class VehicleDaoImpl {

	  ObjectMapper mapper = new ObjectMapper();

	  public List<Vehicle> getListVehicles()
	  {
		List<Vehicle> listVehicles = new ArrayList<Vehicle>(); 
	    
		try
	    {
	      // create our mysql database connection
	      String myDriver = "com.mysql.cj.jdbc.Driver";
	      String myUrl = "jdbc:mysql://localhost:3306/agil_auto";
	      Class.forName(myDriver);
	      Connection conn = DriverManager.getConnection(myUrl, "root", "");
	      
	      // our SQL SELECT query. 
	      // if you only need a few columns, specify them by name instead of using "*"
	      String query = "SELECT * FROM vehicle where vehicle_energy_type_code = 'DIES'";

	      // create the java statement
	      Statement st = conn.createStatement();
	      
	      // execute the query, and get a java resultset
	      ResultSet rs = st.executeQuery(query);
	      
	      // iterate through the java resultset
	      while (rs.next())
	      {
    	  Vehicle vehicle = new Vehicle();
	        vehicle.setVehicle_id(rs.getLong("vehicle_id"));
	        vehicle.setVehicle_segment_code(rs.getString("vehicle_segment_code"));
	        vehicle.setVehicle_color(rs.getString("vehicle_color"));
	        vehicle.setVehicle_energy_type_code(rs.getString("vehicle_energy_type_code"));
	        vehicle.setVehicle_fiscal_power(rs.getLong("vehicle_fiscal_power"));
	        vehicle.setVehicle_mixed_consumption(rs.getDouble("vehicle_mixed_consumption"));
	        // Put the results in a list
	        listVehicles.add(vehicle);
	      }
	      st.close();
	    }
	    catch (Exception e)
	    {
	      System.err.println("Got an exception! ");
	      System.err.println(e.getMessage());
	    }
	    return listVehicles;
	  }

	}
