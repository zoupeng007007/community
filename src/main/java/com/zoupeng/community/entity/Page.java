package com.zoupeng.community.entity;


import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
//封装分页相关的信息,每一页的数据数，
public class Page {

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    // 当前页码
    private int current = 1;
    // 显示上限
    private int limit = 10;
    // 数据的总数（用于计算总页数）
    private int rows;
    //查询路径（用于复用分页的链接）
    private String path;

    /*限制最大页数*/
    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public void setRows(int rows) {
        if (rows >= 0) this.rows = rows;
    }

    //获取当前页的起始行
    public int getOffset() {
        return (current - 1) * limit;
    }

    /*获取总页数*/
    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return (rows / limit) + 1;
        }
    }

    /*获取起始页码*/
    public int getFrom() {
        int from = current - 2;
        return Math.max(from, 1);
    }

    /*显示前后五行*/
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return Math.min(total, to);
    }
}
