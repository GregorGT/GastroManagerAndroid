package com.gastromanager.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DrillDownMenuItemDetail implements Serializable {
    String menuItemName;
    String uuid;
    Double price;
    String menuId;
    Map<String, DrillDownMenuItemOptionDetail> options;
    List<DrillDownMenuItemDetail> subItems;

    public DrillDownMenuItemDetail() {

    }

    public DrillDownMenuItemDetail(String menuItemName, Map<String, DrillDownMenuItemOptionDetail> options, List<DrillDownMenuItemDetail> subItems) {
        this.menuItemName = menuItemName;
        this.options = options;
        this.subItems = subItems;
    }

    public Map<String, DrillDownMenuItemOptionDetail> getOptionsMap() {
        return options;
    }

    public void setOptionsMap(Map<String, DrillDownMenuItemOptionDetail> optionsMap) {
        this.options = optionsMap;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public List<DrillDownMenuItemDetail> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<DrillDownMenuItemDetail> subItems) {
        this.subItems = subItems;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }
}
