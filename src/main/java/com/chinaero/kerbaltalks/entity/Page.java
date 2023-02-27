package com.chinaero.kerbaltalks.entity;

import lombok.Data;

/**
 * 封装分页相关的信息
 */
@Data
public class Page {

    // 当前的页码
    private int current = 1;
    //显示上限
    private int limit = 10;
    // 数据总数，用于计算总页数
    private int rows;
    // 查询路径，每个按钮是一个路径
    private String path;

    public void setCurrent(int current) {
        if (current >= 1)
            this.current = current;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public void setRows(int rows) {
        if (rows >= 0)
            this.rows = rows;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     * @return
     */
    public int getOffset() {
        return current * limit - limit;
    }

    /**
     * 获取总页数，为了显示页码
     * @return
     */
    public int getTotal() {
        // rows / limit
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    /**
     * 获取起始页码，左侧边界
     * @return
     */
    public int getFrom() {
        int from = current - 2;
        return Math.max(from, 1);
    }

    /**
     * 获取结束页码
     * @return
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return Math.min(to, total);

    }
}
