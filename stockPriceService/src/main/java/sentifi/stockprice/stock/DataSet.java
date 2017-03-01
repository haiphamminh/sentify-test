package sentifi.stockprice.stock;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DataSet {
	private Integer id;
	private String dataset_code;
	private String database_code;
	private String name;
	private String description;
	private Date refreshed_at;
	private Date newest_available_date;
	private Date oldest_available_date;
	private List<String> column_names;
	private String frequency;
	private String type;
	private Boolean premium;
	private Object limit;
	private Object transform;
	private Integer column_index;
	private Date start_date;
	private Date end_date;
	private List<List<Object>> data;
	private Object collapse;
	private Object order;
	private Integer database_id;

	public DataSet() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDataset_code() {
		return dataset_code;
	}

	public void setDataset_code(String dataset_code) {
		this.dataset_code = dataset_code;
	}

	public String getDatabase_code() {
		return database_code;
	}

	public void setDatabase_code(String database_code) {
		this.database_code = database_code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getRefreshed_at() {
		return refreshed_at;
	}

	public void setRefreshed_at(Date refreshed_at) {
		this.refreshed_at = refreshed_at;
	}

	public Date getNewest_available_date() {
		return newest_available_date;
	}

	public void setNewest_available_date(Date newest_available_date) {
		this.newest_available_date = newest_available_date;
	}

	public Date getOldest_available_date() {
		return oldest_available_date;
	}

	public void setOldest_available_date(Date oldest_available_date) {
		this.oldest_available_date = oldest_available_date;
	}

	public List<String> getColumn_names() {
		return column_names;
	}

	public void setColumn_names(List<String> column_names) {
		this.column_names = column_names;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getPremium() {
		return premium;
	}

	public void setPremium(Boolean premium) {
		this.premium = premium;
	}

	public Object getLimit() {
		return limit;
	}

	public void setLimit(Object limit) {
		this.limit = limit;
	}

	public Object getTransform() {
		return transform;
	}

	public void setTransform(Object transform) {
		this.transform = transform;
	}

	public Integer getColumn_index() {
		return column_index;
	}

	public void setColumn_index(Integer column_index) {
		this.column_index = column_index;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}

	public List<List<Object>> getData() {
		return data;
	}

	public void setData(List<List<Object>> data) {
		this.data = data;
		Collections.reverse(this.data);
	}

	public Object getCollapse() {
		return collapse;
	}

	public void setCollapse(Object collapse) {
		this.collapse = collapse;
	}

	public Object getOrder() {
		return order;
	}

	public void setOrder(Object order) {
		this.order = order;
	}

	public Integer getDatabase_id() {
		return database_id;
	}

	public void setDatabase_id(Integer database_id) {
		this.database_id = database_id;
	}
}
