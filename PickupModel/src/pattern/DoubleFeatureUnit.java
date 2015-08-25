package pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 * Created by julie on 8/1/15.
 */
public class DoubleFeatureUnit {

	private final String TAG = "======";
	private final int FFTNUM = 3;
	private List<Double> data;
	double[] dataArray;
	private double mean = Double.NaN;
	private double std = Double.NaN;
	private double min = Double.NaN;
	private double max = Double.NaN;
	private double percentile25 = Double.NaN;
	private double percentile50 = Double.NaN;
	private double percentile75 = Double.NaN;
	private Complex[] fft = null;
	private String headerprefix;

	public DoubleFeatureUnit() {
		this.data = new ArrayList<Double>();
	}

	public DoubleFeatureUnit(List<Double> data) {
		this.data = data;
	}
	
	public void setHeaderprefix(String headerprefix) {
		this.headerprefix = headerprefix;
	}
	
	
	
	public int getSampleCounts() {
		return this.data.size();
	}

	public List<Double> getData() {
		return data;
	}

	public void setData(List<Double> data) {
		this.data = data;
	}

	public void addSample(double value) {
		if (this.data == null) {
			this.data = new ArrayList<Double>();
		}
		this.data.add(value);
		updateDataArray();
	}

	public double getMean() {
		if (this.data == null) {
			return Double.NaN;
		}
		this.mean = new Mean().evaluate(getDataArray());
		return mean;
	}

	public double getStd() {
		if (this.data == null) {
			return Double.NaN;
		}
		this.std = new StandardDeviation().evaluate(getDataArray());
		return std;
	}

	public double getMin() {
		if (this.data == null) {
			return Double.NaN;
		}
		this.min = Collections.min(data);
		return min;
	}

	public double getMax() {
		if (this.data == null) {
			return Double.NaN;
		}
		this.max = Collections.max(data);
		return max;
	}

	public double getPercentile25() {
		if (this.data == null) {
			return Double.NaN;
		}
		this.percentile25 = new Percentile().evaluate(getDataArray(), 25);
		return percentile25;
	}

	public double getPercentile50() {
		if (this.data == null) {
			return Double.NaN;
		}
		this.percentile50 = new Percentile().evaluate(getDataArray(), 50);
		return percentile50;
	}

	public double getPercentile75() {
		if (this.data == null) {
			return Double.NaN;
		}
		this.percentile75 = new Percentile().evaluate(getDataArray(), 75);
		return percentile75;
	}

	public Complex[] getFft() {
		int power2size = (int)Math.pow(2.0, Math.ceil(Math.log(data.size()) / Math.log(2)));
//		System.out.println("Padding from " + data.size() + " to " + power2size);
		double[] data4fft = new double[power2size];
		for (int i = 0; i < power2size; i++) {
			if (i < data.size()) {
				data4fft[i] = data.get(i);
			} else {
				data4fft[i] = 0;
			}
		}
		fft = new FastFourierTransformer(DftNormalization.STANDARD).transform(
				data4fft, TransformType.FORWARD);
		return fft;
	}

	private double[] getDataArray() {
		updateDataArray();
		return dataArray;
	}

	private void updateDataArray() {
		if (data == null) {
			dataArray = null;
		}
		if (dataArray == null || dataArray.length != data.size()) {
			dataArray = new double[data.size()];
			for (int i = 0; i < data.size(); i++) {
				dataArray[i] = data.get(i);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getMean()).append(",");
		sb.append(getStd()).append(",");
		sb.append(getMax()).append(",");
		sb.append(getMin()).append(",");
		sb.append(getPercentile25()).append(",");
		sb.append(getPercentile50()).append(",");
		sb.append(getPercentile75()).append(",");
		for (int i = 0; i < getFft().length && i < FFTNUM; i++) {
			sb.append(fft[i].getReal()).append(",");
			sb.append(fft[i].getImaginary()).append(",");
		}
		return sb.toString();
	}
	public String getHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append(headerprefix).append("_mean,");
		sb.append(headerprefix).append("_std,");
		sb.append(headerprefix).append("_max,");
		sb.append(headerprefix).append("_min,");
		sb.append(headerprefix).append("_getPercentile25,");
		sb.append(headerprefix).append("_getPercentile50,");
		sb.append(headerprefix).append("_getPercentile75,");
		for (int i = 0; i < getFft().length && i < FFTNUM; i++) {
			sb.append(headerprefix + "_fft_" + i + "_real,");
			sb.append(headerprefix + "_fft_" + i + "_imaginary,");
		}
		return sb.toString();
	}
}