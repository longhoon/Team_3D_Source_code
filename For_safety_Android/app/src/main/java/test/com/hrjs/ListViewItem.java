package test.com.hrjs;

/**
 * Created by Heera on 2017-05-01.
 */

public class ListViewItem {
    private String titleStr;
    private String descStr;
    private double latStr;
    private double lonStr;

    public void setTitle(String title){
        titleStr = title;
    }
    public void setDesc(String desc){
        descStr = desc;
    }
    public void setLat(double lat){
        latStr = lat;
    }
    public void setLon(double lon){
        lonStr = lon;
    }

    public String getTitle(){
        return this.titleStr;
    }

    public String getDesc(){
        return this.descStr;
    }

    public double getLat() {
        return this.latStr;
    }

    public double getLon(){
        return this.lonStr;
    }

}
