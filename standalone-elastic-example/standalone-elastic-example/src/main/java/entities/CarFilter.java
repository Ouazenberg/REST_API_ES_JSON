package entities;

public class CarFilter {
	
	private String vehicle_color;
	private String vehicle_fiscal_power;
	
	public CarFilter() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CarFilter(String vehicle_color, String vehicle_fiscal_power) {
		super();
		this.vehicle_color = vehicle_color;
		this.vehicle_fiscal_power = vehicle_fiscal_power;
	}

	public String getVehicle_color() {
		return vehicle_color;
	}

	public void setVehicle_color(String vehicle_color) {
		this.vehicle_color = vehicle_color;
	}

	public String getvehicle_fiscal_power() {
		return vehicle_fiscal_power;
	}

	public void setvehicle_fiscal_power(String vehicle_fiscal_power) {
		this.vehicle_fiscal_power = vehicle_fiscal_power;
	}
	
	

}
