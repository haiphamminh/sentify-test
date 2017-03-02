package sentifi.stockprice.stock;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sentifi.stockprice.utils.ObjUtils;
import sentifi.stockprice.cache.ClosePriceCacheData;

public class TwoHundredDayMovingAverage {

	private static Integer NO_DAYS = 200;

	private String ticker;
	private double average;

	public TwoHundredDayMovingAverage() {
	}

	public TwoHundredDayMovingAverage(String ticker, Date startDate, ClosePriceCacheData cpcd) {
		this.ticker = ticker;
		this.average = computeAverage(startDate, cpcd.getData());
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public JsonNode convert200dmaAsJsonNode() {
		JsonNodeFactory jnf = JsonNodeFactory.instance;
		ObjectNode contentNode = jnf.objectNode();
		ObjectNode thdmaNode = contentNode.objectNode();

		thdmaNode.put("Ticker", this.ticker);
		thdmaNode.put("Avg", String.valueOf(this.average));

		contentNode.set("200dma", thdmaNode);
		return contentNode;
	}

	public String convert200dmaAsString() {
		return convert200dmaAsJsonNode().toString();
	}

	private double computeAverage(Date startDate, List<List<Object>> data) {
		double avg = 0;
		int i = 0;
		while (i < data.size()) {
			Date date = new Date();
			try {
				date = ObjUtils.getDateFormat().parse(String.valueOf(data.get(i).get(ClosePriceCacheData.DATECLOSE_IDX)));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			// find the first date which is equal or right after start date
			if (date.compareTo(startDate) >= 0) {
				break;
			}
			i++;
		}

		// no data found with start date provided
		if (i == data.size()) {
			return -1;
		}

		int j = 0;
		while (j < NO_DAYS && i < data.size()) {
			avg += Double.valueOf(data.get(i).get(ClosePriceCacheData.CLOSEPRICE_IDX).toString());
			i++;
			j++;
		}

		return (avg / j);
	}
}
