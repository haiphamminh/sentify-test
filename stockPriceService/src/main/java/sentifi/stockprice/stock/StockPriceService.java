package sentifi.stockprice.stock;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import sentifi.stockprice.cache.ClosePriceCache;
import sentifi.stockprice.cache.ClosePriceCacheData;
import sentifi.stockprice.exception.InvalidDataException;

import sentifi.stockprice.utils.Utils;

@Service
public class StockPriceService implements IStockPriceService {

	@Override
	public String closePriceRestApi(String ticker_symbol, String startDateStr, String endDateStr) {

		Date startDate = Utils.parseDate(startDateStr);
		if (startDate == null) {
			return generateErrorMessage(ticker_symbol, InvalidDataException.INVALID_STARTDATE);
		}

		Date endDate = Utils.parseDate(endDateStr);
		if (endDate == null) {
			return generateErrorMessage(ticker_symbol, InvalidDataException.INVALID_ENDDATE);
		}

		if (startDate.compareTo(endDate) > 0) {
			return generateErrorMessage(ticker_symbol, InvalidDataException.INVALID_RANGE_OF_DATE);
		}

		ClosePriceCacheData cpcd = readAndCacheStockData(ticker_symbol);
		if (cpcd == null) {
			return generateErrorMessage(ticker_symbol, InvalidDataException.INVALID_TICKER_SYMBOL);
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
			String msg = String.format(InvalidDataException.NO_DATA_FOR_START_DATE,
					cpcd.getData().get(0).get(ClosePriceCacheData.DATECLOSE_IDX));
			return generateErrorMessage(ticker_symbol, msg);
		}
		return thdma.convert200dmaAsString();
	}

	@Override
	public String twoHundredDayMovingAverageForTickerSymbolsRestApi(String startDateStr, String tickerSymbols) {

		if (tickerSymbols.isEmpty()) {
			return generateErrorMessage("", InvalidDataException.NO_TICKER_SYMBOL);
		}

		String[] splitTickerSymbols = tickerSymbols.split(",");

		ArrayNode content = JsonNodeFactory.instance.arrayNode();
		for (String ticker_symbol : splitTickerSymbols) {
			TwoHundredDayMovingAverage thdma = null;
			try {
				thdma = request200dma(ticker_symbol, startDateStr);
			} catch (InvalidDataException e) {
				content.add(e.convertIdeAsJsonNode());
			}

			// no data found for start date provided, then compute 200dma of the
			// first possible start date
			if (thdma != null && thdma.getAverage() == -1) {
				// get cache data
				ClosePriceCacheData cpcd = ClosePriceCache.getInstance().get(ticker_symbol);
				if (cpcd != null) {
					Date firstPossibleStartDate = Utils
							.parseDate(cpcd.getData().get(0).get(ClosePriceCacheData.DATECLOSE_IDX).toString());
					if (firstPossibleStartDate == null) {
						return generateErrorMessage(ticker_symbol, InvalidDataException.INVALID_STARTDATE);
					}
					thdma = new TwoHundredDayMovingAverage(ticker_symbol, firstPossibleStartDate, cpcd, true);
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
				stockInfo = Utils.getObjectMapper().readValue(new URL(url), StockInfo.class);
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

		Date startDate = Utils.parseDate(startDateStr);
		if (startDate == null) {
			throw new InvalidDataException(ticker_symbol, InvalidDataException.INVALID_STARTDATE);
		}

		ClosePriceCacheData cpcd = readAndCacheStockData(ticker_symbol);
		if (cpcd == null) {
			throw new InvalidDataException(ticker_symbol, InvalidDataException.INVALID_TICKER_SYMBOL);
		}

		return new TwoHundredDayMovingAverage(ticker_symbol, startDate, cpcd);
	}

	private String generateErrorMessage(String ticker, String message) {
		return generateErrorMessage(new InvalidDataException(ticker, message));
	}

	private String generateErrorMessage(InvalidDataException ide) {
		return ide.convertIdeAsJsonString();
	}
}
