package com.gastromanager.models;

import java.io.Serializable;
import java.util.Map;

public class Menu implements Serializable {
    Map<String, DrillDownMenuItemDetail> itemMap;
    Map<String, DrillDownMenuItemDetail> mainItemMap;
    Map<String, SelectedOrderItem> quickMenuIdRefMap;

    public Map<String, DrillDownMenuItemDetail> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<String, DrillDownMenuItemDetail> itemMap) {
        this.itemMap = itemMap;
    }

    public Map<String, DrillDownMenuItemDetail> getMainItemMap() {
        return mainItemMap;
    }

    public void setMainItemMap(Map<String, DrillDownMenuItemDetail> mainItemMap) {
        this.mainItemMap = mainItemMap;
    }

    public Map<String, SelectedOrderItem> getQuickMenuIdRefMap() {
        return quickMenuIdRefMap;
    }

    public void setQuickMenuIdRefMap(Map<String, SelectedOrderItem> quickMenuIdRefMap) {
        this.quickMenuIdRefMap = quickMenuIdRefMap;
    }
}
