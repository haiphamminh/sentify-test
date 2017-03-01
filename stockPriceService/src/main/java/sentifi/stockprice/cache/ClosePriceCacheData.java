package sentifi.stockprice.cache;

import java.util.ArrayList;
import java.util.List;

public class ClosePriceCacheData {

	public static final int DATECLOSE_IDX = 0;
	public static final int CLOSEPRICE_IDX = 1;

	private List<List<Object>> data;
	private double accessCount = 0;

	public ClosePriceCacheData(List<List<Object>> data) {
		this.accessCount++;
		this.data = new ArrayList<List<Object>>();
		this.data.addAll(data);
	}

	public List<List<Object>> getData() {
		return data;
	}

	public void setData(List<List<Object>> data) {
		this.data = data;
	}

	public Double getAccessCount() {
		return accessCount;
	}

	public void increaseAccessCount() {
		this.accessCount++;
	}
}
