package sentifi.stockprice.stock;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sentifi.stockprice.utils.Utils;
import sentifi.stockprice.cache.ClosePriceCacheData;
import sentifi.stockprice.exception.InvalidDataException;

public class TwoHundredDayMovingAverage {

	private static final Integer NO_DAYS = 200;
	private static final String NO_DATA_FOR_STARTDATE = "No data for start date provided. The first possible start date %s is provided";

	private String ticker;
	private double average;
	private boolean isRecomputed;
	private String firstPossibleStartDate;

	public TwoHundredDayMovingAverage() {
	}

	public TwoHundredDayMovingAverage(String ticker, Date startDate, ClosePriceCacheData cpcd) {
		this(ticker, startDate, cpcd, false);
	}

	public TwoHundredDayMovingAverage(String ticker, Date startDate, ClosePriceCacheData cpcd, boolean isRecomputed) {
		this.ticker = ticker;
		this.average = computeAverage(startDate, cpcd.getData());
		this.isRecomputed = isRecomputed;
		this.firstPossibleStartDate = Utils.formatDate(startDate);
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
		ObjectNode contentNode = JsonNodeFactory.instance.objectNode();
		ObjectNode thdmaNode = contentNode.objectNode();

		thdmaNode.put("Ticker", this.ticker);
		thdmaNode.put("Avg", String.valueOf(this.average));

		if (isRecomputed) {
			String msg = String.format(NO_DATA_FOR_STARTDATE, this.firstPossibleStartDate);
			thdmaNode.put("Message", msg);
		}

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
			Date date = Utils.parseDate(String.valueOf(data.get(i).get(ClosePriceCacheData.DATECLOSE_IDX)));

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

	public static String convertMultiple200dmaAsString(String[] splitTickerSymbols, Object... objects) {
		ArrayNode contentNode = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < splitTickerSymbols.length; i++) {
			if (objects[i] instanceof InvalidDataException) {
				contentNode.add(((InvalidDataException) objects[i]).convertIdeAsJsonNode());
			} else {
				contentNode.add(((TwoHundredDayMovingAverage) objects[i]).convert200dmaAsJsonNode());
			}
		}
		return contentNode.toString();
	}
}
