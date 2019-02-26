package entities;

public class CarFilter {
	
	private String vehicle_color;
	private String vehicle_energy_type_code;
	
	public CarFilter() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CarFilter(String vehicle_color, String vehicle_energy_type_code) {
		super();
		this.vehicle_color = vehicle_color;
		this.vehicle_energy_type_code = vehicle_energy_type_code;
	}

	public String getVehicle_color() {
		return vehicle_color;
	}

	public void setVehicle_color(String vehicle_color) {
		this.vehicle_color = vehicle_color;
	}

	public String getvehicle_energy_type_code() {
		return vehicle_energy_type_code;
	}

	public void setvehicle_energy_type_code(String vehicle_energy_type_code) {
		this.vehicle_energy_type_code = vehicle_energy_type_code;
	}
	
	

}
