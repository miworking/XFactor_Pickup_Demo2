package pattern;


/**
 * Created by julie on 8/1/15.
 */
public class Feature {
	XYZFeature accelerometer;
	XYZFeature magnetic;
	XYZFeature rotation;
	String timestamp;
	String user;
	
	
	public void setPrefix() {
		this.accelerometer.setHeaderPrefix("accelerometer");
		this.magnetic.setHeaderPrefix("magnetic");
		this.rotation.setHeaderPrefix("rotation");
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	
	public boolean getFeatureFromCSV(String dir, String timestamp) {
		this.timestamp = timestamp;
		String accFileName = dir + timestamp + "acc.csv";
		accelerometer = new XYZFeature();
		if (!accelerometer.readFromCSV(accFileName)) {
			System.out.println("can't read accelerometer from csv file");
			return false;
		}
		
		
		String magneticFileName = dir + timestamp + "magnetic.csv";
		magnetic = new XYZFeature();
		if (!magnetic.readFromCSV(magneticFileName)) {
			System.out.println("can't read magnetic from csv file");
			return false;
		}
		
		String rotationFileName = dir + timestamp + "rotation.csv";
		rotation = new XYZFeature();
		if (!rotation.readFromCSV(rotationFileName)) {
			System.out.println("can't read rotation from csv file");
			return false;
		}		
		setPrefix();
		return true;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
//		sb.append(this.user + "_" + timestamp + ",");
		sb.append(this.accelerometer.toString());
		sb.append(this.magnetic.toString());
		sb.append(this.rotation.toString());
		return sb.toString();
	}
	
	public String getHeader() {
		StringBuilder sb = new StringBuilder();
//		sb.append("timestamp,");
		sb.append(this.accelerometer.getHeader());
		sb.append(this.magnetic.getHeader());
		sb.append(this.rotation.getHeader());
		sb.append("label");
		return sb.toString();
	}
}
