package com.example.pymonitormobile;

import java.io.Serializable;

public class Pi implements Serializable {

    private String ip;
    private String temperature;
    private String disk_total_space;
    private String disk_avail_space;
    private String ram_total_space;
    private String ram_avail_space;

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setDisk_total_space(String disk_total_space) {
        this.disk_total_space = disk_total_space;
    }

    public void setDisk_avail_space(String disk_avail_space) {
        this.disk_avail_space = disk_avail_space;
    }

    public void setRam_total_space(String ram_total_space) {
        this.ram_total_space = ram_total_space;
    }

    public void setRam_avail_space(String ram_avail_space) {
        this.ram_avail_space = ram_avail_space;
    }
    public void setIp (String ip){
        this.ip = ip;
    }
    public String getIp (){
        return this.ip;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getDisk_total_space() {
        return disk_total_space;
    }

    public String getDisk_avail_space() {
        return disk_avail_space;
    }

    public String getRam_total_space() {
        return ram_total_space;
    }

    public String getRam_avail_space() {
        return ram_avail_space;
    }
}
