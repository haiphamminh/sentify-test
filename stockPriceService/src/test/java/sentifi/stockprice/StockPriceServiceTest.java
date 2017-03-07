package sentifi.stockprice;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import sentifi.stockprice.cache.ClosePriceCache;
import sentifi.stockprice.cache.ClosePriceCacheData;
import sentifi.stockprice.exception.InvalidDataException;
import sentifi.stockprice.stock.ClosePrice;
import sentifi.stockprice.stock.StockPriceService;
import sentifi.stockprice.stock.TwoHundredDayMovingAverage;
import sentifi.stockprice.utils.Utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ StockPriceService.class, ClosePriceCache.class })
public class StockPriceServiceTest {

	private StockPriceService stockPriceService;

	@Before
	public void setUp() throws JsonParseException, JsonMappingException, IOException {
		stockPriceService = PowerMock.createPartialMock(StockPriceService.class, "readAndCacheStockData", String.class);
	}

	@After
	public void tearDown() {
		stockPriceService = null;
	}

	@Test
	public void closePriceRestApi_InvalidStartDate() {
		assert_closePrice("fakeTicker", "invalidDate", "invalidDate",
				StockPricePropertiesComponent.getInstance().getInvalidStartDateMsg());
	}

	@Test
	public void closePriceRestApi_InvalidEndDate() {
		assert_closePrice("fakeTicker", "2017-02-24", "invalidDate",
				StockPricePropertiesComponent.getInstance().getInvalidEndDateMsg());
	}

	@Test
	public void closePriceRestApi_InvalidRangeOfDate() {
		assert_closePrice("fakeTicker", "2017-02-30", "2017-02-20",
				StockPricePropertiesComponent.getInstance().getInvalidRangeOfDatesMsg());
	}

	@Test
	public void closePriceRestApi_InvalidTickerSymbol() {
		assert_closePrice("fakeTicker", "2017-02-24", "2017-03-01",
				StockPricePropertiesComponent.getInstance().getInvalidTickerSymbolMsg());
	}

	@Test
	public void closePriceRestApi_ValidData() {
		ClosePriceCacheData cpcd = buildData();

		PowerMock.replay(stockPriceService);

		String fakeTicker = "fakeTicker";
		String startDateStr = "2017-02-24";
		String endDateStr = "2017-03-01";

		String result = stockPriceService.closePriceRestApi(fakeTicker, startDateStr, endDateStr);

		PowerMock.verify(stockPriceService);

		assertThat(result).isEqualTo(
				(new ClosePrice(fakeTicker, Utils.parseDate(startDateStr), Utils.parseDate(endDateStr), cpcd))
						.convertClosePriceAsString());
	}

	@Test
	public void twoHundredDayMovingAverageRestApi_InvalidStartDate() {
		assert_200dma("fakeTicker", "invalidStartDate",
				StockPricePropertiesComponent.getInstance().getInvalidStartDateMsg());
	}

	@Test
	public void twoHundredDayMovingAverageRestApi_NoData() {
		ClosePriceCacheData cpcd = buildData();

		ClosePriceCache.getInstance();
		PowerMock.stub(PowerMock.method(ClosePriceCache.class, "get", String.class)).toReturn(cpcd);

		PowerMock.replay(stockPriceService);

		String message = String.format(StockPricePropertiesComponent.getInstance().getNoDataForStartDateMsg(),
				cpcd.getData().get(0).get(ClosePriceCacheData.DATECLOSE_IDX));
		assert_200dma("fakeTicker", "2017-03-05", message);

		PowerMock.verify(stockPriceService);
	}

	@Test
	public void twoHundredDayMovingAverageRestApi_ValidData() {
		ClosePriceCacheData cpcd = buildData();

		PowerMock.replay(stockPriceService);

		String fakeTicker = "fakeTicker";
		String startDateStr = "2017-02-24";

		String result = stockPriceService.twoHundredDayMovingAverageRestApi(fakeTicker, startDateStr);

		PowerMock.verify(stockPriceService);

		assertThat(result).isEqualTo((new TwoHundredDayMovingAverage(fakeTicker, Utils.parseDate(startDateStr), cpcd))
				.convert200dmaAsString());
	}

	@Test
	public void twoHundredDayMovingAverageForTickerSymbolsRestApi_NoTickerSymbols() {
		String result = stockPriceService.twoHundredDayMovingAverageForTickerSymbolsRestApi("fakeDate", "");

		String expectedResult = (new InvalidDataException("",
				StockPricePropertiesComponent.getInstance().getNoTickerSymbolMsg())).convertIdeAsJsonString();

		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	public void twoHundredDayMovingAverageForTickerSymbolsRestApi_InvalidStartDate() {
		assert_200dmaForTickerSymbols(new String[] { "GE" }, "fakeDate",
				new InvalidDataException("GE", StockPricePropertiesComponent.getInstance().getInvalidStartDateMsg()));
	}

	@Test
	public void twoHundredDayMovingAverageForTickerSymbolsRestApi_ValidData() {
		ClosePriceCacheData cpcd = buildData();

		PowerMock.replay(stockPriceService);

		String[] tickerSymbols = new String[] { "GE", "FB" };
		String startDateStr = "2017-02-24";

		String result = stockPriceService.twoHundredDayMovingAverageForTickerSymbolsRestApi(startDateStr,
				String.join(",", tickerSymbols));

		Date startDate = Utils.parseDate(startDateStr);

		String expectedResult = TwoHundredDayMovingAverage.convertMultiple200dmaAsString(tickerSymbols,
				new TwoHundredDayMovingAverage(tickerSymbols[0], startDate, cpcd),
				new TwoHundredDayMovingAverage(tickerSymbols[1], startDate, cpcd));

		assertThat(result).isEqualTo(expectedResult);

		PowerMock.verify(stockPriceService);

	}

	private void assert_closePrice(String tickerSymbol, String startDateStr, String endDateStr, String message) {
		String result = stockPriceService.closePriceRestApi(tickerSymbol, startDateStr, endDateStr);
		String expectedResult = (new InvalidDataException(tickerSymbol, message)).convertIdeAsJsonString();

		assertThat(result).isEqualTo(expectedResult);
	}

	private void assert_200dma(String tickerSymbol, String startDateStr, String message) {
		String result = stockPriceService.twoHundredDayMovingAverageRestApi(tickerSymbol, startDateStr);
		String expectedResult = (new InvalidDataException(tickerSymbol, message)).convertIdeAsJsonString();

		assertThat(result).isEqualTo(expectedResult);
	}

	private void assert_200dmaForTickerSymbols(String[] tickerSymbols, String startDateStr, Object... objects) {
		String result = stockPriceService.twoHundredDayMovingAverageForTickerSymbolsRestApi(startDateStr,
				String.join(",", tickerSymbols));

		String expectedResult = TwoHundredDayMovingAverage.convertMultiple200dmaAsString(tickerSymbols, objects);

		assertThat(result).isEqualTo(expectedResult);
	}

	private ClosePriceCacheData buildData() {
		List<List<Object>> data = new ArrayList<List<Object>>();
		List<Object> insideData = new ArrayList<Object>();

		Collections.addAll(insideData, "2017-03-01", 49.25);
		data.add(insideData);

		insideData.clear();
		Collections.addAll(insideData, "2017-02-27", 48.7);
		data.add(insideData);

		insideData.clear();
		Collections.addAll(insideData, "2017-02-24", 48.55);
		data.add(insideData);

		ClosePriceCacheData cpcd = new ClosePriceCacheData(data);

		try {
			PowerMock.expectPrivate(stockPriceService, "readAndCacheStockData", EasyMock.anyString()).andReturn(cpcd)
					.anyTimes();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cpcd;
	}
}
