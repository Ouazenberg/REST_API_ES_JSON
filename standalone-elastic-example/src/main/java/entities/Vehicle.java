package entities;

public class Vehicle {

	private long vehicle_id;
	private String vehicle_segment_code;
	private String vehicle_color;
	private String vehicle_energy_type_code;
	private long vehicle_fiscal_power;
	private double vehicle_mixed_consumption;
	
	public Vehicle() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Vehicle(long vehicle_id, String vehicle_segment_code, String vehicle_color, String vehicle_energy_type_code,
			long vehicle_fiscal_power, double vehicle_mixed_consumption) {
		super();
		this.vehicle_id = vehicle_id;
		this.vehicle_segment_code = vehicle_segment_code;
		this.vehicle_color = vehicle_color;
		this.vehicle_energy_type_code = vehicle_energy_type_code;
		this.vehicle_fiscal_power = vehicle_fiscal_power;
		this.vehicle_mixed_consumption = vehicle_mixed_consumption;
	}

	public long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public String getVehicle_segment_code() {
		return vehicle_segment_code;
	}

	public void setVehicle_segment_code(String vehicle_segment_code) {
		this.vehicle_segment_code = vehicle_segment_code;
	}

	public String getVehicle_color() {
		return vehicle_color;
	}

	public void setVehicle_color(String vehicle_color) {
		this.vehicle_color = vehicle_color;
	}

	public String getVehicle_energy_type_code() {
		return vehicle_energy_type_code;
	}

	public void setVehicle_energy_type_code(String vehicle_energy_type_code) {
		this.vehicle_energy_type_code = vehicle_energy_type_code;
	}

	public long getVehicle_fiscal_power() {
		return vehicle_fiscal_power;
	}

	public void setVehicle_fiscal_power(long vehicle_fiscal_power) {
		this.vehicle_fiscal_power = vehicle_fiscal_power;
	}

	public double getVehicle_mixed_consumption() {
		return vehicle_mixed_consumption;
	}

	public void setVehicle_mixed_consumption(double vehicle_mixed_consumption) {
		this.vehicle_mixed_consumption = vehicle_mixed_consumption;
	}
	
}
