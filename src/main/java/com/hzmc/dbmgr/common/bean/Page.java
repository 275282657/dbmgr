package com.hzmc.dbmgr.common.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Page implements java.io.Serializable {

	public static final int DEFAULT_PAGE_SIZE = 10;// 默认每页大小
	private int batchPage=10; //默认10页一批
	private Collection<Object> items = new ArrayList<Object>();
	private int currentPage = 1;
	private int totalCount; // 总记录数
	private String orderField; // 排序字段
	private boolean ascend; // 是否升序
	private Map<Object, Object> searchParameters = new HashMap<Object, Object>();
	private int pageSize = DEFAULT_PAGE_SIZE; // 每页大小
	private boolean paged = true;
	private int totalPage;
	private int startPage;
	private int endPage;
	
	public int getStartPage() {
		if(this.currentPage%batchPage!=0){
			return this.currentPage/batchPage*batchPage+1;
		}else{
			return (this.currentPage-1)/batchPage*batchPage+1;
		}
		
	}

	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}

	public int getEndPage() {
		int endpage=1;
		if(this.currentPage%batchPage!=0){
			endpage = (this.currentPage/batchPage+1)*batchPage;
		}else{
			endpage = ((this.currentPage-1)/batchPage+1)*batchPage;
		}
		if(endpage>getTotalPage()){
			return getTotalPage();
		}else{
			return endpage;
		}
	}

	public void setEndPage(int endPage) {
		this.endPage = endPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public Collection<Object> getItems() {
		return items;
	}

	public void setItems(Collection<Object> items) {
		this.items = items;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	// 设置当前页
	public void setCurrentPage(String currentPage) {
		int tempCurrentPage;
		if (currentPage == null || "".equals(currentPage)
				|| currentPage.equals("1")) {
			tempCurrentPage = 1;
			this.setCurrentPage(tempCurrentPage);

		} else {
			try {
				this.setCurrentPage(Integer.parseInt(currentPage));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int count) {
		this.totalCount = count;
	}

	public String getOrderField() {
		return orderField;
	}

	public void setOrderField(String orderField) {
		this.orderField = orderField;
	}

	public boolean isAscend() {
		return ascend;
	}

	public void setAscend(boolean ascend) {
		this.ascend = ascend;
	}

	public Map<Object, Object> getSearchParameters() {
		return searchParameters;
	}

	public void setSearchParameters(Map<Object, Object> searchParameters) {
		this.searchParameters = searchParameters;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/*---------- 计算开始和结束位置---------------------------------*/
	public int getStartNumber() {
		return (this.currentPage - 1) * pageSize + 1;
	}
	//MySQL 改动
	public int getLimitNumber() {
		return (this.currentPage - 1) * pageSize;
	}

	public int getEndNumber() {
		if (this.currentPage * pageSize > this.totalCount) {
			return getStartNumber() + this.totalCount % pageSize - 1;
		} else {
			return getStartNumber() + pageSize - 1;
		}
	}

	/*--------------/trustuser/precreate.do------------------------------*/

	public int getTotalPage() {
		int totalPage = (totalCount / pageSize);
		if (totalCount % pageSize != 0) {
			totalPage++;
		}
		return totalPage;
	}

	/** --------------计算总页数------------------- */
	public int getPreviewPage() {
		if (this.currentPage == 1) {
			return this.currentPage;
		} else {
			return currentPage - 1;
		}
	}

	public int getNextPage() {
		if (this.currentPage == getTotalPage()) {
			return this.currentPage;
		} else {
			return currentPage + 1;
		}
	}

	public void addSearchParameter(Object key, Object value) {
		if (value instanceof String) {
			String stringValue = (String) value;
			value = stringValue.trim();
		}
		this.searchParameters.put(key, value);
	}

	public boolean isPaged() {
		return paged;
	}

	public void setPaged(boolean paged) {
		this.paged = paged;
	}

	
	/**  
	 * 获取batchPage  
	 * @return batchPage batchPage  
	 */
	public int getBatchPage() {
		return batchPage;
	}

	
	/**  
	 * 设置batchPage  
	 * @param batchPage batchPage  
	 */
	public void setBatchPage(int batchPage) {
		this.batchPage = batchPage;
	}
	
}
