package sentifi.stockprice.stock;

import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import sentifi.stockprice.cache.ClosePriceCache;
import sentifi.stockprice.cache.ClosePriceCacheData;
import sentifi.stockprice.exception.InvalidDataException;

import sentifi.stockprice.utils.ObjUtils;

@Service
public class StockPriceService implements IStockPriceService {

	@Override
	public String closePriceRestApi(String ticker_symbol, String startDateStr, String endDateStr) {

		Date startDate = null, endDate = null;
		try {
			startDate = ObjUtils.getDateFormat().parse(startDateStr);
		} catch (ParseException e) {
			return generateErrorMessage(ticker_symbol, "404", "An invalid start date is provided");
		}

		try {
			endDate = ObjUtils.getDateFormat().parse(endDateStr);
		} catch (ParseException e) {
			return generateErrorMessage(ticker_symbol, "404", "An invalid end date is provided");
		}

		if (startDate.compareTo(endDate) > 0) {
			return generateErrorMessage(ticker_symbol, "404", "An invalid range of dates is provided");
		}

		ClosePriceCacheData cpcd = readAndCacheStockData(ticker_symbol);
		if (cpcd == null) {
			return generateErrorMessage(ticker_symbol, "404", "An invalid ticker symbol is provided");
		}

		ClosePrice cp = new ClosePrice(ticker_symbol, startDate, endDate, cpcd);
		return cp.convertClosePriceAsString();
	}

	@Override
	public String twoHundredDayMovingAverageRestApi(String ticker_symbol, String startDateStr) {

		TwoHundredDayMovingAverage thdma;
		try {
			thdma = request200dma(ticker_symbol, startDateStr);
		} catch (InvalidDataException e) {
			return generateErrorMessage(e);
		}

		if (thdma.getAverage() == -1) {
			ClosePriceCacheData cpcd = ClosePriceCache.getInstance().get(ticker_symbol);
			String msg = String.format("No data for the start date. The first possible start date is %s",
					cpcd.getData().get(0).get(ClosePriceCacheData.DATECLOSE_IDX));
			return generateErrorMessage(ticker_symbol, "404", msg);
		}
		return thdma.convert200dmaAsString();
	}

	@Override
	public String twoHundredDayMovingAverageForTickerSymbolsRestApi(String startDateStr, String tickerSymbols) {

		String[] splitTickerSymbols = tickerSymbols.split(",");

		JsonNodeFactory jnf = JsonNodeFactory.instance;
		ArrayNode content = jnf.arrayNode();
		for (String ticker_symbol : splitTickerSymbols) {
			TwoHundredDayMovingAverage thdma = null;
			try {
				thdma = request200dma(ticker_symbol, startDateStr);
			} catch (InvalidDataException e) {
				content.add(e.convertIdeAsJsonNode());
			}

			// no data found for start date provided, then compute 200dma of the
			// first possible start date
			if (thdma.getAverage() == -1) {
				// get cache data
				ClosePriceCacheData cpcd = ClosePriceCache.getInstance().get(ticker_symbol);
				if (cpcd != null) {
					Date firstPossibleStartDate = null;
					try {
						firstPossibleStartDate = ObjUtils.getDateFormat()
								.parse(cpcd.getData().get(0).get(0).toString());
					} catch (ParseException pe) {
						return generateErrorMessage(ticker_symbol, "404", "An invalid start date is provided");
					}
					thdma = new TwoHundredDayMovingAverage(ticker_symbol, firstPossibleStartDate, cpcd);
				}
			}

			if (thdma != null) {
				content.add(thdma.convert200dmaAsJsonNode());
			}
		}

		return content.toString();
	}

	private ClosePriceCacheData readAndCacheStockData(String ticker_symbol) {
		StockInfo stockInfo = null;

		// get cache data
		ClosePriceCacheData cpcd = ClosePriceCache.getInstance().get(ticker_symbol);
		if (cpcd == null) {
			String url = String.format("https://www.quandl.com/api/v3/datasets/WIKI/%s.json", ticker_symbol);
			try {
				stockInfo = ObjUtils.getObjectMapper().readValue(new URL(url), StockInfo.class);
			} catch (Exception e) {
				return null;
			}

			// cache data
			List<List<Object>> data = new ArrayList<List<Object>>();
			for (List<Object> value : stockInfo.getDataset().getData()) {
				List<Object> insideData = new ArrayList<Object>();
				insideData.add(value.get(0)); // date close
				insideData.add(value.get(4)); // close price
				data.add(insideData);
			}
			cpcd = ClosePriceCache.getInstance().cache(ticker_symbol, data);
		}
		return cpcd;
	}

	private TwoHundredDayMovingAverage request200dma(String ticker_symbol, String startDateStr)
			throws InvalidDataException {

		Date startDate = null;
		try {
			startDate = ObjUtils.getDateFormat().parse(startDateStr);
		} catch (ParseException e) {
			throw new InvalidDataException(ticker_symbol, "404", "An invalid start date is provided");
		}

		ClosePriceCacheData cpcd = readAndCacheStockData(ticker_symbol);
		if (cpcd == null) {
			throw new InvalidDataException(ticker_symbol, "404", "An invalid ticker symbol is provided");
		}

		return new TwoHundredDayMovingAverage(ticker_symbol, startDate, cpcd);
	}

	private String generateErrorMessage(String ticker, String code, String message) {
		return generateErrorMessage(new InvalidDataException(ticker, code, message));
	}

	private String generateErrorMessage(InvalidDataException ide) {
		return ide.convertIdeAsJsonString();
	}
}
