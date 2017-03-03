package sentifi.stockprice.stock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sentifi.stockprice.cache.ClosePriceCacheData;
import sentifi.stockprice.utils.Utils;

public class ClosePrice {

	private String ticker;
	private List<List<String>> dateCloses;

	public ClosePrice(String ticker, Date startDate, Date endDate, ClosePriceCacheData cpcd) {
		this.ticker = ticker;
		this.dateCloses = new ArrayList<List<String>>();

		for (List<Object> value : cpcd.getData()) {
			String date = value.get(ClosePriceCacheData.DATECLOSE_IDX).toString();
			Date d = Utils.parseDate(date);
			if (d.compareTo(startDate) >= 0 && d.compareTo(endDate) <= 0) {
				List<String> data = new ArrayList<String>();
				data.add(date);
				data.add(value.get(ClosePriceCacheData.CLOSEPRICE_IDX).toString());
				this.dateCloses.add(data);
			}
		}
	}

	public JsonNode convertClosePriceAsJson() {
		JsonNodeFactory jnf = JsonNodeFactory.instance;
		ObjectNode contentNode = jnf.objectNode();

		ObjectNode dateClosesNode = jnf.objectNode();
		dateClosesNode.put("Ticker", this.ticker);

		int i = 1;
		for (List<String> dateClose : this.dateCloses) {
			ArrayNode dateCloseNode = jnf.arrayNode();
			String fieldName = "DateClose";
			if (this.dateCloses.size() > 1) {
				fieldName = String.format("DateClose_%d", i++);
			}
			dateClosesNode.set(fieldName, dateCloseNode);
			for (String value : dateClose) {
				dateCloseNode.add(value);
			}
		}

		ArrayNode closePriceNode = contentNode.arrayNode();
		closePriceNode.add(dateClosesNode);
		contentNode.set("Prices", closePriceNode);

		return contentNode;
	}

	public String convertClosePriceAsString() {
		return convertClosePriceAsJson().toString();
	}

}
