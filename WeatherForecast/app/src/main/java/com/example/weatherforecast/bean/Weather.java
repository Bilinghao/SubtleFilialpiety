package com.example.weatherforecast.bean;

public class Weather {

    private String title;
    private String updateTime;
    private String date;
    private String tempNow;
    private String tempMax;
    private String tempMin;
    private String textDay;
    private String windDir;
    private String windScale;
    private String aqi;
    private String pm25;
    private String warningTitle;
    private String moveIndex;
    private String wearIndex;
    private String uvIndex;
    private String comfortIndex;

    public String getTempNow() {
        return tempNow;
    }

    public void setTempNow(String tempNow) {
        this.tempNow = tempNow;
    }

    public String getTempMax() {
        return tempMax;
    }

    public void setTempMax(String tempMax) {
        this.tempMax = tempMax;
    }

    public String getTempMin() {
        return tempMin;
    }

    public void setTempMin(String tempMin) {
        this.tempMin = tempMin;
    }

    public String getMoveIndex() {
        return moveIndex;
    }

    public void setMoveIndex(String moveIndex) {
        this.moveIndex = moveIndex;
    }

    public String getWearIndex() {
        return wearIndex;
    }

    public void setWearIndex(String wearIndex) {
        this.wearIndex = wearIndex;
    }

    public String getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(String uvIndex) {
        this.uvIndex = uvIndex;
    }

    public String getComfortIndex() {
        return comfortIndex;
    }

    public void setComfortIndex(String comfortIndex) {
        this.comfortIndex = comfortIndex;
    }



    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }



    public String getTextDay() {
        return textDay;
    }

    public void setTextDay(String textDay) {
        this.textDay = textDay;
    }

    public String getWindDir() {
        return windDir;
    }

    public void setWindDir(String windDir) {
        this.windDir = windDir;
    }

    public String getWindScale() {
        return windScale;
    }

    public void setWindScale(String windScale) {
        this.windScale = windScale;
    }

    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public String getWarningTitle() {
        return warningTitle;
    }

    public void setWarningTitle(String warningTitle) {
        this.warningTitle = warningTitle;
    }

    public String getWarningText() {
        return warningText;
    }

    public void setWarningText(String warningText) {
        this.warningText = warningText;
    }

    public String getWarningTime() {
        return warningTime;
    }

    public void setWarningTime(String warningTime) {
        this.warningTime = warningTime;
    }

    private String warningText;
    private String warningTime;


}
