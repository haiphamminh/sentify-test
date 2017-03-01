package sentifi.stockprice.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ClosePriceCache {

	private static final int MAX_SIZE = 100;

	private Map<String, ClosePriceCacheData> closePriceCacheMap;
	private static ClosePriceCache instance;

	private ClosePriceCache() {
		closePriceCacheMap = new HashMap<String, ClosePriceCacheData>(0);
	}

	public static ClosePriceCache getInstance() {
		if (instance == null) {
			instance = new ClosePriceCache();
		}
		return instance;
	}

	public ClosePriceCacheData cache(String ticker, List<List<Object>> data) {
		ClosePriceCacheData cpcd = closePriceCacheMap.get(ticker);
		if (cpcd == null) {
			if (closePriceCacheMap.size() == MAX_SIZE) {
				Set<Entry<String, ClosePriceCacheData>> set = closePriceCacheMap.entrySet();				
				List<Entry<String, ClosePriceCacheData>> list = new ArrayList<Entry<String, ClosePriceCacheData>>(set);
				closePriceCacheMap.remove(list.get(list.size() - 1).getKey());
			}
			cpcd = new ClosePriceCacheData(data);
			closePriceCacheMap.put(ticker, cpcd);
		}		
		return cpcd;
	}

	public ClosePriceCacheData get(String ticker) {
		ClosePriceCacheData cpcd = closePriceCacheMap.get(ticker);
		if (cpcd != null) {
			cpcd.increaseAccessCount();
		}
		sort();
		return cpcd;
	}

	private void sort() {
		Set<Entry<String, ClosePriceCacheData>> set = closePriceCacheMap.entrySet();
		List<Entry<String, ClosePriceCacheData>> list = new ArrayList<Entry<String, ClosePriceCacheData>>(set);
		Collections.sort(list, new Comparator<Entry<String, ClosePriceCacheData>>() {
			public int compare(Entry<String, ClosePriceCacheData> o1, Entry<String, ClosePriceCacheData> o2) {
				return o2.getValue().getAccessCount().compareTo(o1.getValue().getAccessCount());
			}
		});
	}
}
