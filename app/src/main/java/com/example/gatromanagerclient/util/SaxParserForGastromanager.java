/*Copyright 2021 GastroRice

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


package com.example.gatromanagerclient.util;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.gastromanager.models.DrillDownMenuButton;
import com.gastromanager.models.DrillDownMenuItemDetail;
import com.gastromanager.models.DrillDownMenuItemOptionChoiceDetail;
import com.gastromanager.models.DrillDownMenuItemOptionDetail;
import com.gastromanager.models.DrillDownMenuType;
import com.gastromanager.models.DrillDownMenus;
import com.gastromanager.models.Menu;
import com.gastromanager.models.MenuDetail;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

public class SaxParserForGastromanager {

    MenuDetail menuDetail;
    private static SaxParserForGastromanager instance = null;
    private SaxParserForGastromanager(){
    }

    public static SaxParserForGastromanager getInstance() {
        if(instance == null) {
            instance = new SaxParserForGastromanager();
        }
        return instance;
    }

    public MenuDetail parseXml(String xmlData, Boolean isRefresh) {
        if(isRefresh && xmlData != null) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = null;
                saxParser = factory.newSAXParser();
                Xmlhandler xmlhandler = new Xmlhandler();
                InputStream xmlStream = new ByteArrayInputStream(xmlData.getBytes());
                saxParser.parse(xmlStream, xmlhandler);
                //saxParser.parse("C:\\Users\\Admin\\IdeaProjects\\GastroManager\\JDesktopJava\\data\\sample_tempalte.xml", xmlhandler);
                menuDetail = xmlhandler.getMenuDetail();
                addMenuItemtoDrillDownMenuButton(menuDetail);
                System.out.println("Done");
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }
        return menuDetail;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void main(String[] args) {
        try {
            SaxParserForGastromanager parser = SaxParserForGastromanager.getInstance();
            MenuDetail menuDetailInfo = null;
            menuDetailInfo = parser.parseXml(XmlUtil.readFileToString("C:\\\\Users\\\\Admin\\\\IdeaProjects\\\\GastroManager\\\\JDesktopJava\\\\data\\\\sample_tempalte.xml",
                    Charset.defaultCharset()), true);
            System.out.println(menuDetailInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addMenuItemtoDrillDownMenuButton(MenuDetail menuDetail){
        Map<String, DrillDownMenuItemDetail> menuItemMap = menuDetail.getMenu().getItemMap();
        List<DrillDownMenuType> menuTypes = menuDetail.getDrillDownMenus().getDrillDownMenuTypes();
        for(DrillDownMenuType menuType : menuTypes) {
            List<DrillDownMenuButton> buttons = menuType.getButtons();
            for(DrillDownMenuButton button: buttons) {
                button.setMenuItemDetail(menuItemMap.get(button.getName()));
            }
        }
    }

    public static class Xmlhandler extends DefaultHandler {


        private StringBuilder elementValue;
        private DrillDownMenus drillDownMenus;
        private DrillDownMenuType drillDownMenuType;
        private DrillDownMenuButton drillDownMenuButton;
        private MenuDetail menuDetail;
        private Menu menu;
        private Stack<DrillDownMenuItemDetail> itemStack;
        private DrillDownMenuItemOptionDetail drillDownMenuItemOptionDetail;
        private DrillDownMenuItemOptionChoiceDetail drillDownMenuItemOptionChoiceDetail;

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (elementValue == null) {
                elementValue = new StringBuilder();
            } else {
                elementValue.append(ch, start, length);
            }
        }

        @Override
        public void startDocument() throws SAXException {
            System.out.println("start of the document");
            menuDetail = new MenuDetail();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case "menues":
                    System.out.println("start of menues"+ qName);
                    menu = new Menu();
                    break;
                case "item":
                    DrillDownMenuItemDetail item = null;
                    if(itemStack == null) { //main item else sub item
                        itemStack = new Stack<>();
                    }
                    item = new DrillDownMenuItemDetail();
                    item.setMenuItemName(attributes.getValue("name"));
                    System.out.println("start of item"+ qName);
                    itemStack.push(item);
                    break;
                case "option":
                    System.out.println("start of option "+ qName +" "+attributes.getValue("name"));
                    if(itemStack != null && !itemStack.empty()) {
                        drillDownMenuItemOptionDetail = new DrillDownMenuItemOptionDetail();
                        drillDownMenuItemOptionDetail.setId(attributes.getValue("id"));
                        drillDownMenuItemOptionDetail.setName(attributes.getValue("name"));
                        drillDownMenuItemOptionDetail.setPrice(Double.valueOf(
                                (attributes.getValue("price") != null) ? attributes.getValue("price") :
                                        attributes.getValue("overwrite_price")
                        ));
                        DrillDownMenuItemDetail currentItem  = itemStack.peek();
                        if(currentItem.getOptionsMap() == null) {
                            currentItem.setOptionsMap(new HashMap<>());
                        }
                        currentItem.getOptionsMap().put(drillDownMenuItemOptionDetail.getName(), drillDownMenuItemOptionDetail);
                    }

                    break;
                case "choice":
                    System.out.println("start of choice "+ qName);
                    drillDownMenuItemOptionChoiceDetail = new DrillDownMenuItemOptionChoiceDetail();
                    drillDownMenuItemOptionChoiceDetail.setName(attributes.getValue("name"));
                    drillDownMenuItemOptionChoiceDetail.setPrice(Double.valueOf(attributes.getValue("price")));
                    break;
                case "drilldownmenus":
                    System.out.println("start of drill down menus"+ qName);
                    drillDownMenus = new DrillDownMenus();
                    break;
                case "drilldownmenu":
                    System.out.println("drill down menu type"+ qName+" "+attributes.getValue("name") + attributes.getValue("height"));
                    if(drillDownMenus != null) {
                        if (drillDownMenus.getDrillDownMenuTypes() == null) {
                            drillDownMenus.setDrillDownMenuTypes(new ArrayList<>());
                        }
                        drillDownMenuType = new DrillDownMenuType();
                        drillDownMenuType.setName(attributes.getValue("name"));
                        drillDownMenuType.setHeight(attributes.getValue("height"));
                        drillDownMenuType.setWidth(attributes.getValue("width"));
                        drillDownMenus.getDrillDownMenuTypes().add(this.drillDownMenuType);
                    }
                    break;
                case "button":
                    if(drillDownMenuType != null) {
                        if(drillDownMenuType.getButtons() == null) {
                            drillDownMenuType.setButtons(new ArrayList<>());
                        }
                        drillDownMenuButton = new DrillDownMenuButton();
                        drillDownMenuButton.setName(attributes.getValue("name"));
                        drillDownMenuButton.setHeight(attributes.getValue("height"));
                        drillDownMenuButton.setWidth(attributes.getValue("width"));
                        drillDownMenuType.getButtons().add(drillDownMenuButton);
                    }

                    break;
                default:
                    System.out.println("start of "+qName);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case "menues":
                    System.out.println("end of menus"+ qName);
                    menuDetail.setMenu(menu);
                    menu = null;
                    break;
                case "item":
                    System.out.println("end of item "+ qName);
                    DrillDownMenuItemDetail item = itemStack.pop();
                    if(!itemStack.empty()) {
                        DrillDownMenuItemDetail parentItem  = itemStack.peek();
                        System.out.println("adding "+ item.getMenuItemName() + "to "+parentItem.getMenuItemName());
                        if(parentItem.getSubItems() == null) {
                            parentItem.setSubItems(new ArrayList<>());
                        }
                        parentItem.getSubItems().add(item);
                    } else { //add main item to map
                        if(menu.getItemMap() == null) {
                            menu.setItemMap(new HashMap<>());
                        }
                        menu.getItemMap().put(item.getMenuItemName(), item);
                        itemStack = null;
                    }
                    break;
                case "option":
                    System.out.println("end of option"+ qName);
                    drillDownMenuItemOptionDetail = null;
                    break;
                case "choice":
                    System.out.println("end of choice "+ qName);
                    if(drillDownMenuItemOptionDetail != null) {
                        drillDownMenuItemOptionDetail.setChoice(drillDownMenuItemOptionChoiceDetail);
                    }
                    drillDownMenuItemOptionChoiceDetail = null;
                    break;
                case "drilldownmenus":
                    System.out.println("end of drill down menus"+ qName + drillDownMenus.getDrillDownMenuTypes());
                    if(menuDetail == null) {
                        menuDetail = new MenuDetail();
                    }
                    menuDetail.setDrillDownMenus(drillDownMenus);
                    drillDownMenus = null;
                    break;
                case "drilldownmenu":
                    this.drillDownMenuType = null;
                    break;
                case "button":
                    this.drillDownMenuButton = null;
                    break;
                default:
                    System.out.println("end of "+ qName);
            }
        }

        public MenuDetail getMenuDetail() {
            return menuDetail;
        }
    }
}
