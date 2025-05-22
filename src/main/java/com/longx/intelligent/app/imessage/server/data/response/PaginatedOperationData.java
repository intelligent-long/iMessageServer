package com.longx.intelligent.app.imessage.server.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by LONG on 2023/12/15 at 8:14 PM.
 */
public class PaginatedOperationData<T> extends OperationStatus{
    private boolean hasMore;
    private List<T> data;
    private Integer total;

    public static <T> PaginatedOperationData<T> paginatedSuccess(List<T> data, boolean hasMore, Integer total){
        return new PaginatedOperationData<T>(0, "成功", data, hasMore, total);
    }

    public static <T> PaginatedOperationData<T> paginatedSuccess(List<T> data, boolean hasMore){
        return new PaginatedOperationData<T>(0, "成功", data, hasMore, null);
    }

    public static <T> PaginatedOperationData<T> paginatedFailure(){
        return new PaginatedOperationData<>(-200, "失败", null, false, null);
    }

    public PaginatedOperationData() {
    }

    public PaginatedOperationData(int code, String message, List<T> data, boolean hasMore) {
        this(code, message, data, hasMore, null);
    }

    public PaginatedOperationData(int code, String message, List<T> data, boolean hasMore, Integer total) {
        super(code, message);
        this.hasMore = hasMore;
        this.data = data;
        this.total = total;
    }

    @JsonProperty("hasMore")
    public boolean hasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
