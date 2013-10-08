package com.ocse.android.app.sql;

/**
 * User: Administrator
 * Date: 13-9-20
 * Time: 上午10:16
 */
public class Douban {
    private int _id;
    private String tag;
    private int start;
    private int end;

    public Douban() {
    }

    public Douban(String tag, int start, int end) {
        this.tag = tag;
        this.start = start;
        this.end = end;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Douban{");
        sb.append("_id=").append(_id);
        sb.append(", tag='").append(tag).append('\'');
        sb.append(", start=").append(start);
        sb.append(", end=").append(end);
        sb.append('}');
        return sb.toString();
    }
}
