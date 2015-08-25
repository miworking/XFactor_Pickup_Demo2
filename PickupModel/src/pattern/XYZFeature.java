package pattern;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by julie on 8/1/15.
 */
public class XYZFeature {
    private DoubleFeatureUnit x;
    private DoubleFeatureUnit y;
    private DoubleFeatureUnit z;
    private DoubleFeatureUnit v;
    private String header_prefix;
    
    public XYZFeature() {
    	this.x = new DoubleFeatureUnit();
    	this.y = new DoubleFeatureUnit();
    	this.z = new DoubleFeatureUnit();
    	this.v = new DoubleFeatureUnit();
    }
    
    public void setHeaderPrefix(String headerprefix) {
    	this.header_prefix = headerprefix;
    	x.setHeaderprefix(headerprefix + "X");
    	y.setHeaderprefix(headerprefix + "Y");
    	z.setHeaderprefix(headerprefix + "Z");
    	v.setHeaderprefix(headerprefix + "V");
    }
    
    public String getHeaderprefix() {
    	return this.header_prefix;
    }
    
    public boolean readFromCSV(String fileName) {
    	System.out.println(fileName);
        try {
            BufferedReader br = new BufferedReader( new FileReader(fileName));
            String strLine = null;
            StringTokenizer st = null;
            int lineNumber = 0, tokenNumber = 0;

            
            while((strLine = br.readLine()) != null) {
            	lineNumber++;
            	if (lineNumber == 1) { 
            		continue;
                }
                String[] result = strLine.split(",");
                this.x.addSample(Double.parseDouble(result[0]));
                this.y.addSample(Double.parseDouble(result[1]));
                this.z.addSample(Double.parseDouble(result[2]));
                double sum = 0;
                for (int i = 0; i < 3; i++) {
                	sum += Double.parseDouble(result[i]) * Double.parseDouble(result[i]);
                }
                this.v.addSample(Math.sqrt(sum));
            }
//            printValue();
            return true;
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(this.x.toString());
    	sb.append(this.y.toString());
    	sb.append(this.z.toString());
    	sb.append(this.v.toString());
    	return sb.toString();
    }
    public void printValue() {
    	System.out.println("=======================================================================================");
//    	System.out.println("X: " + this.x.toString());
//    	System.out.println("Y: " + this.y.toString());
//    	System.out.println("Z: " + this.z.toString());
//    	System.out.println("V: " + this.v.toString());
    	System.out.println(this.toString());
    }
    
    public String getHeader() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(this.x.getHeader());
    	sb.append(this.y.getHeader());
    	sb.append(this.z.getHeader());
    	sb.append(this.v.getHeader());
    	return sb.toString();
    }

}
