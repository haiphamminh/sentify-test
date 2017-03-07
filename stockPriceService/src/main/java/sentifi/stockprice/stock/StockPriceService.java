package sentifi.stockprice.stock;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import sentifi.stockprice.StockPricePropertiesComponent;
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
			return generateErrorMessage(ticker_symbol,
					StockPricePropertiesComponent.getInstance().getInvalidStartDateMsg());
		}

		Date endDate = Utils.parseDate(endDateStr);
		if (endDate == null) {
			return generateErrorMessage(ticker_symbol,
					StockPricePropertiesComponent.getInstance().getInvalidEndDateMsg());
		}

		if (startDate.compareTo(endDate) > 0) {
			return generateErrorMessage(ticker_symbol,
					StockPricePropertiesComponent.getInstance().getInvalidRangeOfDatesMsg());
		}

		ClosePriceCacheData cpcd = readAndCacheStockData(ticker_symbol);
		if (cpcd == null) {
			return generateErrorMessage(ticker_symbol,
					StockPricePropertiesComponent.getInstance().getInvalidTickerSymbolMsg());
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
			String msg = String.format(StockPricePropertiesComponent.getInstance().getNoDataForStartDateMsg(),
					cpcd.getData().get(0).get(ClosePriceCacheData.DATECLOSE_IDX));
			return generateErrorMessage(ticker_symbol, msg);
		}
		return thdma.convert200dmaAsString();
	}

	@Override
	public String twoHundredDayMovingAverageForTickerSymbolsRestApi(String startDateStr, String tickerSymbols) {

		if (tickerSymbols.isEmpty()) {
			return generateErrorMessage("", StockPricePropertiesComponent.getInstance().getNoTickerSymbolMsg());
		}

		String[] splitTickerSymbols = tickerSymbols.split(",");

		ArrayNode content = JsonNodeFactory.instance.arrayNode();
		for (String ticker_symbol : splitTickerSymbols) {
			TwoHundredDayMovingAverage thdma = null;
			try {
				ticker_symbol = ticker_symbol.trim();
				thdma = request200dma(ticker_symbol, startDateStr);
			} catch (InvalidDataException e) {
				content.add(e.convertIdeAsJsonNode());
			}

			if (thdma != null) {
				// no data found for start date provided, then recompute 200dma
				// of the first possible start date
				if (thdma.getAverage() == -1) {
					// get cache data
					ClosePriceCacheData cpcd = ClosePriceCache.getInstance().get(ticker_symbol);
					if (cpcd != null) {
						String dateStr = cpcd.getData().get(0).get(ClosePriceCacheData.DATECLOSE_IDX).toString();
						Date firstPossibleStartDate = Utils.parseDate(dateStr);
						if (firstPossibleStartDate == null) {
							return generateErrorMessage(ticker_symbol,
									StockPricePropertiesComponent.getInstance().getInvalidStartDateMsg());
						}
						thdma = new TwoHundredDayMovingAverage(ticker_symbol, firstPossibleStartDate, cpcd);
						String msg = String.format(
								StockPricePropertiesComponent.getInstance().getDataForFirstPossibleStartDateMsg(),
								dateStr);
						content.add(thdma.convert200dmaAsJsonNode(true, msg));
					}

				} else {
					content.add(thdma.convert200dmaAsJsonNode());
				}
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
			throw new InvalidDataException(ticker_symbol,
					StockPricePropertiesComponent.getInstance().getInvalidStartDateMsg());
		}

		ClosePriceCacheData cpcd = readAndCacheStockData(ticker_symbol);
		if (cpcd == null) {
			throw new InvalidDataException(ticker_symbol,
					StockPricePropertiesComponent.getInstance().getInvalidTickerSymbolMsg());
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
