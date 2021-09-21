package com.example.gatromanagerclient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gatromanagerclient.socket.Client;
import com.example.gatromanagerclient.util.Constants;
import com.example.gatromanagerclient.util.SaxParserForGastromanager;
import com.example.gatromanagerclient.util.Util;
import com.gastromanager.models.DrillDownMenuButton;
import com.gastromanager.models.DrillDownMenuItemDetail;
import com.gastromanager.models.DrillDownMenuItemOptionDetail;
import com.gastromanager.models.DrillDownMenuType;
import com.gastromanager.models.MenuDetail;
import com.gastromanager.models.OrderDetailQuery;
import com.gastromanager.models.OrderItemInfo;
import com.gastromanager.models.SelectedOrderItem;
import com.gastromanager.models.SelectedOrderItemOption;
import com.gastromanager.models.SignOffOrderInfo;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity2 extends AppCompatActivity implements OrderListAdapter.OrderItemClickListener {

    private AppBarConfiguration appBarConfiguration;
    private RecyclerView orderDetailsView;
    private AppCompatButton fetchOrderDetailsButton;
    private MenuDetail menuDetail;
    private Stack<String> orderSelectionStack;
    private AbsoluteLayout menuView;
    private LinearLayout menuOptionsView;
    private Map<String, String> optionsSelectionMap;
    TextInputEditText orderIdInputTextField;
    TextInputEditText floorIdInputTextField;
    TextInputEditText tableIdInputTextField;
    EditText menuIdInputTextField;
    private AppCompatButton selectMenuIdButton;
    private SelectedOrderItem mainSelectedOrderItem;
    private OrderListAdapter orderListAdapter;
    private List<OrderItemInfo> orderItemInfoList  = new ArrayList<>();

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_order_screen);

        //Order details view
        orderDetailsView = (RecyclerView) findViewById(R.id.textView2);
        orderListAdapter = new OrderListAdapter(orderItemInfoList, this);
        orderDetailsView.setHasFixedSize(true);
        orderDetailsView.setLayoutManager(new LinearLayoutManager(this));
        orderDetailsView.setAdapter(orderListAdapter);

        //System.out.println("Current order details :" + orderDetailsView.getText());
        orderIdInputTextField = findViewById(R.id.orderIdInputTextField);
        floorIdInputTextField = findViewById(R.id.floorIdInputTextField);
        tableIdInputTextField = findViewById(R.id.tableIdInputTextField);
        tableIdInputTextField.setOnFocusChangeListener((v, hasFocus) -> {
                    if(!hasFocus) {
                        if(floorIdInputTextField.getText() != null &&
                                tableIdInputTextField.getText() != null) {
                            String floorId = floorIdInputTextField.getText().toString();
                            String tableId = tableIdInputTextField.getText().toString();
                            if(!floorId.trim().equals(Constants.EMPTY_RESULT)
                                    && !tableId.trim().equals(Constants.EMPTY_RESULT)) {
                                new GetHumanReadableOrderIdTask().execute();
                            }
                        }
                    }
                }

        );
        menuIdInputTextField = findViewById(R.id.menuIdInputTextField);

        fetchOrderDetailsButton = findViewById(R.id.fetchOrderInfoButton);
        //loadOrderDetails(null);
        fetchOrderDetailsButton.setOnClickListener(v -> {
                    System.out.println("inside get order");
                    orderDetailsView.removeAllViews();
                    String inputOrderId = (orderIdInputTextField.getText() != null) ?
                            orderIdInputTextField.getText().toString() : null;
                    buildAndSendOrderQuery(inputOrderId);
                }
        );

        //Previous
        ImageView prevOverButton = findViewById(R.id.prevOrderButton);
        prevOverButton.setOnClickListener(v -> {
                    System.out.println("inside get previous order");
                    orderDetailsView.removeAllViews();
                    //EditText orderIdInputTextField = findViewById(R.id.orderIdInputTextField);
                    String inputOrderId = (orderIdInputTextField.getText() != null) ?
                            orderIdInputTextField.getText().toString() : null;
                    if (Util.isNumeric(inputOrderId)) {
                        Integer currOrderId = Integer.valueOf(inputOrderId);
                        currOrderId = (currOrderId - 1 < 0) ? 0 : currOrderId - 1;
                        orderIdInputTextField.setText(String.valueOf(currOrderId));
                        inputOrderId = (orderIdInputTextField.getText() != null) ?
                                orderIdInputTextField.getText().toString() : null;
                    }
                    buildAndSendOrderQuery(inputOrderId);
                }
        );

        //Next
        ImageView nextOverButton = findViewById(R.id.nextOrderButton);
        nextOverButton.setOnClickListener(v -> {
                    System.out.println("inside get previous order");
                    //orderDetailsView.setText("");
                    //EditText orderIdInputTextField = findViewById(R.id.orderIdInputTextField);
                    String inputOrderId = (orderIdInputTextField.getText() != null) ?
                            orderIdInputTextField.getText().toString() : null;
                    if (Util.isNumeric(inputOrderId)) {
                        Integer currOrderId = Integer.valueOf(inputOrderId);
                        orderIdInputTextField.setText(String.valueOf(currOrderId + 1));
                        inputOrderId = (orderIdInputTextField.getText() != null) ?
                                orderIdInputTextField.getText().toString() : null;
                    }

                    buildAndSendOrderQuery(inputOrderId);
                }
        );
        //Drill down menu
        Spinner drilldownMenuTypeSpinner = findViewById(R.id.spinner2);
        //Menu space
        menuView = findViewById(R.id.menuItemsView);
        menuOptionsView = findViewById(R.id.menuItemOptionsView);
        //fetch data from server
        try {
            menuDetail = new LoadMenuTask().execute("menu", this, drilldownMenuTypeSpinner).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        drilldownMenuTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {

                String selectedItem = adapter.getItemAtPosition(i).toString();
                System.out.println("selected " + selectedItem + " " + menuDetail);
                menuView.removeAllViews();
                loadMenuView(selectedItem, menuDetail, menuView, menuOptionsView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        /*Button addToOrderButton = findViewById(R.id.addToOrderButton);
        addToOrderButton.setOnClickListener(v -> {
            loadOrderItemFromSelection();
            new AddOrderItemTask().execute(mainSelectedOrderItem);
            menuOptionsView.removeAllViews();
        });*/

        AppCompatButton newOrderIdButton = findViewById(R.id.newOrderButton);
        newOrderIdButton.setOnClickListener(v -> {
            //orderDetailsView.setText("");
            new GetNextOrderIdTask().execute();
            orderListAdapter.updateOrderItemList(null);
        });
        selectMenuIdButton = findViewById(R.id.selectMenuIdButton);
        selectMenuIdButton.setOnClickListener(v -> {
            String menuId = menuIdInputTextField.getText().toString();
            SelectedOrderItem selectedOrderItem = loadSelectedOrderFromMenuId(menuId);

            if (selectedOrderItem != null) {
                selectedOrderItem.setFloorId((floorIdInputTextField.getText() == null) ? null : floorIdInputTextField.getText().toString());
                selectedOrderItem.setTableId((tableIdInputTextField.getText() == null) ? null : tableIdInputTextField.getText().toString());
                selectedOrderItem.setOrderId((orderIdInputTextField.getText() == null) ? null : orderIdInputTextField.getText().toString());

                if (checkIfItemIsReadyForSelection(selectedOrderItem)) {
                    new AddOrderItemTask().execute(selectedOrderItem);
                    reloadOrderItemView();
                } else {
                    buildOptionsSpinner(selectedOrderItem, selectedOrderItem);
                }
            }

        });

        AppCompatButton signOffOrderButton = findViewById(R.id.signOffOrderButton);
        signOffOrderButton.setOnClickListener(v -> {
            sendSignOffOrderRequest();
        });
    }

    private Boolean checkIfItemIsReadyForSelection(SelectedOrderItem selectedOrderItem) {
        return (selectedOrderItem.getSubItems() != null && selectedOrderItem.getSubItems().size() == 1
                && selectedOrderItem.getSubItems().get(0).getAllOptions() == null);

    }

    private void sendSignOffOrderRequest() {
        String inputOrderId = (orderIdInputTextField.getText() != null) ?
                orderIdInputTextField.getText().toString() : null;
        if(inputOrderId != null) {
            OrderDetailQuery orderDetailQuery = new OrderDetailQuery();
            orderDetailQuery.setHumanreadableId(inputOrderId);
            orderDetailQuery.setServerName("Here comes the app name");
            if (floorIdInputTextField.getText() != null) {
                orderDetailQuery.setFloorId(floorIdInputTextField.getText().toString());
            }
            if (tableIdInputTextField.getText() != null) {
                orderDetailQuery.setTableId(tableIdInputTextField.getText().toString());
            }
            SignOffOrderInfo signOffOrderInfo = new SignOffOrderInfo();
            signOffOrderInfo.setOrderDetailQuery(orderDetailQuery);
            new SignOffOrdertask().execute(signOffOrderInfo, this);

        } else {
            showMessage("Order Id not provided !!");
        }
    }

    private void buildAndSendOrderQuery(String inputOrderId) {
        if(inputOrderId != null) {
            System.out.println("get details for order id " + inputOrderId);
            OrderDetailQuery orderDetailQuery = new OrderDetailQuery();
            orderDetailQuery.setHumanreadableId(inputOrderId);
            if (floorIdInputTextField.getText() != null) {
                orderDetailQuery.setFloorId(floorIdInputTextField.getText().toString());
            }
            if (tableIdInputTextField.getText() != null) {
                orderDetailQuery.setTableId(tableIdInputTextField.getText().toString());
            }
            if (inputOrderId != null && !inputOrderId.isEmpty()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                    new GetOrderDetailsTask().execute(orderDetailQuery, this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    showMessage("Order details could not be fetched");
                }

            }
        } else {
            showMessage("Order Id is required");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void buildOptionsSpinner(SelectedOrderItem selectedOrderItem, SelectedOrderItem root) {
        if (selectedOrderItem.getAllOptions() != null) {
            Spinner menuIdItemOptionsSpinner = new Spinner(this);
            System.out.println("adding spinner for " + selectedOrderItem.getItemName());
            menuIdItemOptionsSpinner.setTransitionName(selectedOrderItem.getItemName() + "Options");
            List<String> optionKeys = new ArrayList<>();
            optionKeys.add("");
            for (Map.Entry<String, SelectedOrderItemOption> option : selectedOrderItem.getAllOptions().entrySet()) {
                optionKeys.add(option.getKey());
            }
            System.out.println("Options " + optionKeys);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item, optionKeys);
            arrayAdapter.setDropDownViewResource(android.R.layout
                    .simple_spinner_dropdown_item);
            menuIdItemOptionsSpinner.setAdapter(arrayAdapter);
            menuIdItemOptionsSpinner.setSelection(0);
            menuIdItemOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    SelectedOrderItemOption selectedOrderItemOption = selectedOrderItem.getAllOptions().get(selectedItem);
                    if (selectedOrderItemOption != null) {
                        selectedOrderItem.setOption(selectedOrderItemOption);
                        StringBuilder orderItemInfoBuilder = new StringBuilder();
                        /*if (orderDetailsView.getText() != null && orderDetailsView.getText().length() > 0) {
                            orderItemInfoBuilder.append(orderDetailsView.getText() + "\n");
                        }*/
                        loadMenuItemDesc(selectedOrderItem, root, orderItemInfoBuilder, null);
                        //orderDetailsView.setText(orderItemInfoBuilder.toString());

                        new AddOrderItemTask().execute(root);
                        reloadOrderItemView();
                        menuOptionsView.removeAllViews();
                    }
                }

                private void loadMenuItemDesc(SelectedOrderItem selectedOrderItem, SelectedOrderItem parent,
                                              StringBuilder orderItemInfoBuilder, List<String> optionsAdded) {
                    orderItemInfoBuilder.append(parent.getItemName() + "\t");
                    if (optionsAdded == null) {
                        optionsAdded = new ArrayList<>();
                    }
                    if (parent.getOption() != null) {
                        orderItemInfoBuilder.append(parent.getOption().getName() + "\t");
                        optionsAdded.add(parent.getOption().getId());
                    }
                    if (selectedOrderItem.getSubItems() != null) {
                        loadMenuItemDesc(selectedOrderItem.getSubItems().get(0), selectedOrderItem, orderItemInfoBuilder, optionsAdded);
                    } else {
                        orderItemInfoBuilder.append(selectedOrderItem.getItemName());
                        if (!optionsAdded.contains(selectedOrderItem.getOption().getId())) {
                            orderItemInfoBuilder
                                    .append("-")
                                    .append(selectedOrderItem.getOption().getName()).append("\n");
                        }
                    }
                    /*if(parent.getSubItems() != null) {
                        if(parent.getSubItems().get(0) == selectedOrderItem) {
                            if(selectedOrderItem.getSubItems() != null) {
                                //TODO check if this case is possible
                            } else {
                                orderItemInfoBuilder.append(selectedOrderItem.getItemName()+"-");
                                orderItemInfoBuilder.append(selectedOrderItem.getOption() !=null ?
                                        selectedOrderItem.getOption().getName()+"\n":"");
                            }
                        } else {
                            loadMenuItemDesc(selectedOrderItem, selectedOrderItem.getSubItems().get(0),
                                    orderItemInfoBuilder );
                        }

                    } else {
                        System.out.println("Error ..in loading selection");
                    }*/
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            menuOptionsView.addView(menuIdItemOptionsSpinner);
        } else {
            if (selectedOrderItem.getSubItems() != null && selectedOrderItem.getSubItems().size() > 0) {
                buildOptionsSpinner(selectedOrderItem.getSubItems().get(0), root);
            }
        }
    }

    private SelectedOrderItem loadSelectedOrderFromMenuId(String menuId) {
        return menuDetail.getMenu().getQuickMenuIdRefMap().get(menuId);
    }

    private void loadMenuItems(DrillDownMenuType menuType, MenuDetail menuDetail, AbsoluteLayout view, LinearLayout menuOptionView) {
        System.out.println(" Drill Down Menu view width: " + view.getWidth() +
                " height " + view.getHeight());
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (menuType.getHeight() != null) {
            layoutParams.height = Integer.parseInt(menuType.getHeight());
        }
        if (menuType.getWidth() != null) {
            layoutParams.width = Integer.parseInt(menuType.getWidth());
        }
        view.setLayoutParams(layoutParams);
        List<DrillDownMenuButton> menuButtons = menuType.getButtons();
        Integer xCoord = 0;
        Integer yCoord = 0;
        for (DrillDownMenuButton menuButton : menuButtons) {
            Button menuButtonView = new Button(this);
            menuButtonView.setWidth(Integer.parseInt(menuButton.getWidth()));
            menuButtonView.setHeight(Integer.parseInt(menuButton.getHeight()));
            if (menuButton.getxPosition() != null) {
                menuButtonView.setX(Float.parseFloat(menuButton.getxPosition()));
            } else {
                menuButtonView.setX(Float.parseFloat(xCoord.toString()));
                //xCoord = xCoord + Integer.parseInt(menuButton.getWidth()) + Constants.BUTTON_SPACING;
            }
            if (menuButton.getyPosition() != null) {
                menuButtonView.setY(Float.parseFloat(menuButton.getyPosition()));
            } else {
                menuButtonView.setY(Float.parseFloat(yCoord.toString()));
                yCoord = yCoord + Integer.parseInt(menuButton.getHeight()) + Constants.BUTTON_SPACING;
            }

            menuButtonView.setText(menuButton.getName());
            menuButtonView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onClick(View v) {
                    // your handler code here
                    menuOptionsView.setVisibility(View.VISIBLE);
                    menuOptionsView.removeAllViews();
                    CharSequence itemName = menuButtonView.getText();
                    System.out.println("main item " + itemName);
                    mainSelectedOrderItem = null;
                    mainSelectedOrderItem = new SelectedOrderItem();
                    mainSelectedOrderItem.setItemName(itemName.toString());
                    mainSelectedOrderItem.setTarget(menuButton.getTarget());
                    mainSelectedOrderItem.setOrderId((orderIdInputTextField.getText() == null) ? null : orderIdInputTextField.getText().toString());
                    //floor id and table id
                    mainSelectedOrderItem.setFloorId((floorIdInputTextField.getText() == null) ? null : floorIdInputTextField.getText().toString());
                    mainSelectedOrderItem.setTableId((tableIdInputTextField.getText() == null) ? null : tableIdInputTextField.getText().toString());


                    if (orderSelectionStack == null) {
                        orderSelectionStack = new Stack<>();
                    }
                    orderSelectionStack.push(itemName.toString());
                    DrillDownMenuItemDetail currentDrillDownMenuItemDetail = menuButton.getMenuItemDetail();
                    if(currentDrillDownMenuItemDetail.getOptionsMap() != null) {
                        loadMenuItemOptionsView(currentDrillDownMenuItemDetail, menuButtonView,
                                menuOptionView, true, itemName.toString(), mainSelectedOrderItem);
                    }
                    if(currentDrillDownMenuItemDetail.getSubItems() != null) {
                        loadMenuSubItems(currentDrillDownMenuItemDetail, menuOptionsView, itemName.toString(), mainSelectedOrderItem);
                    }
                    if(currentDrillDownMenuItemDetail.getSubItems() == null && currentDrillDownMenuItemDetail.getOptionsMap() == null) {
                        addItemAndReload(mainSelectedOrderItem);
                    }
                }
            });
            view.addView(menuButtonView);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadMenuItemOptionsView(DrillDownMenuItemDetail menuItemDetail, View parentView,
                                         LinearLayout menuOptionsView, Boolean isRefresh,
                                         String mainItem, SelectedOrderItem selectedOrderItem) {

        if (isRefresh) {
            menuOptionsView.removeAllViews();
        }
        Map<String, DrillDownMenuItemOptionDetail> optionsMap = menuItemDetail.getOptionsMap();
        if (optionsMap != null) {
            Spinner menuItemOptionsSpinner = new Spinner(this);

            menuItemOptionsSpinner.setTransitionName(menuItemDetail.getMenuItemName() + "Options");
            List<String> optionKeys = new ArrayList<>();
            optionKeys.add("");
            optionKeys.addAll(optionsMap.keySet());
            System.out.println("Options " + optionKeys);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item, optionKeys);
            arrayAdapter.setDropDownViewResource(android.R.layout
                    .simple_spinner_dropdown_item);
            menuItemOptionsSpinner.setAdapter(arrayAdapter);
            menuItemOptionsSpinner.setSelection(0);
            menuItemOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    SelectedOrderItemOption selectedOrderItemOption = null;
                    System.out.println("Selected item " + menuItemOptionsSpinner.getSelectedItem().toString());
                    if (!menuItemOptionsSpinner.getSelectedItem().toString().equals("")) {
                        //orderSelectionStack.add(menuItemOptionsSpinner.getSelectedItem().toString());
                        if (optionsSelectionMap == null) {
                            optionsSelectionMap = new HashMap<>();
                        }
                        optionsSelectionMap.put(menuItemOptionsSpinner.getTransitionName(), menuItemOptionsSpinner.getSelectedItem().toString());
                        System.out.println("Option " + menuItemOptionsSpinner.getSelectedItem().toString() + " selected for " + mainItem);
                        selectedOrderItemOption = new SelectedOrderItemOption();
                        selectedOrderItemOption.setId(getOptionId(optionsMap));
                        selectedOrderItemOption.setName(menuItemOptionsSpinner.getSelectedItem().toString());
                        selectedOrderItem.setOption(selectedOrderItemOption);
                    }

                    if(menuItemDetail.getSubItems() == null && selectedOrderItemOption != null) { //final selection
                        selectedOrderItem.setOption(selectedOrderItemOption);
                        addItemAndReload(mainSelectedOrderItem);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    menuItemOptionsSpinner.setSelection(0);
                }

            });

            menuOptionsView.addView(menuItemOptionsSpinner);
            //createAndLoadPopupWindow(menuOptionsView, menuItemOptionsSpinner);
            //loadMenuSubItems(menuItemDetail, menuOptionsView, mainItem, mainSelectedOrderItem);
        } else {
            System.out.println("No options for the item "+menuItemDetail.getMenuItemName());
        }
    }

    private void addItemAndReload(SelectedOrderItem selectedOrderItem) {
        new AddOrderItemTask().execute(selectedOrderItem);
        reloadOrderItemView();
        menuOptionsView.removeAllViews();
        menuOptionsView.setVisibility(View.INVISIBLE);
    }

    public void showMessage(String message) {

        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT ).show();
    }

    public void showMessage(String message, OrderItemInfo orderItem) {

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity2.this);
        alert.setTitle(message);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onClick(DialogInterface dialog, int whichButton) {
                if(orderItem.getPrintStatus() == 1) {
                    showMessage("Order Item is already printed !!");
                } else {
                    new RemoveOrderItemTask().execute(orderItem);
                    orderListAdapter.getOrderItems().remove(orderItem);
                    orderListAdapter.updateOrderItemList(orderListAdapter.getOrderItems());
                    reloadOrderItemView();
                }
            }

        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //what you need to do after click "Cancel"
            }
        });
        alert.show();
    }

    private void createAndLoadPopupWindow(View parentView, View currentView) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        //View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(currentView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);

        /*// dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });*/

    }

    private View createAndLoadPopupWindow(View view) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        return popupView;

    }

    private String getOptionId(Map<String, DrillDownMenuItemOptionDetail> optionsMap) {
        String optionId = null;
        if (optionsMap != null && optionsMap.entrySet().iterator().hasNext()) {
            optionId = optionsMap.entrySet().iterator().next().getValue().getId();
        }
        return optionId;
    }

    private Boolean checkIfChildViewExists(String itemName, LinearLayout currentView) {
        Boolean doesChildExist = false;
        Integer childCount = currentView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = currentView.getChildAt(i);
            if (view instanceof Button) {
                Button currentButton = (Button) view;
                if (currentButton.getText().equals(itemName)) {
                    doesChildExist = true;
                    break;
                }
            }
        }
        return doesChildExist;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadMenuSubItems(DrillDownMenuItemDetail menuItemDetail, LinearLayout menuOptionsView,
                                  String mainItem, SelectedOrderItem selectedOrderItem) {
        List<DrillDownMenuItemDetail> subItems = menuItemDetail.getSubItems();
        System.out.println("Sub items " + subItems);

        if (subItems != null) {
            for (DrillDownMenuItemDetail subItem : subItems) {
                String menuItemName = subItem.getMenuItemName();
                if (!checkIfChildViewExists(menuItemName, menuOptionsView)) {
                    Button menuButtonView = new Button(this);
                    menuButtonView.setText(menuItemName);
                    menuButtonView.setTransitionName(menuItemName);
                    System.out.println("adding " + menuItemName);
                    menuButtonView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            // your handler code here
                            SelectedOrderItem currSelectedOrderItem = new SelectedOrderItem();
                            currSelectedOrderItem.setItemName(menuItemName);
                            System.out.println("Sub item " + menuItemName + " selected for " + mainItem);
                            orderSelectionStack.push(menuButtonView.getText().toString());
                            if(mainSelectedOrderItem == selectedOrderItem) {
                                if(mainSelectedOrderItem.getSubItems() == null) {
                                    mainSelectedOrderItem.setSubItems(new ArrayList<>());
                                }
                                mainSelectedOrderItem.getSubItems().add(currSelectedOrderItem);
                            }
                            if (subItem.getSubItems() != null && subItem.getSubItems().size() > 0) {
                                loadMenuItemOptionsView(subItem, menuButtonView, menuOptionsView, false, menuItemName, currSelectedOrderItem);
                                loadMenuSubItems(subItem, menuOptionsView, menuItemName, currSelectedOrderItem);
                            } else if(subItem.getOptionsMap() != null && subItem.getOptionsMap().size() > 0) {
                                if(mainSelectedOrderItem.getOption() != null &&
                                        subItem.getOptionsMap().get(mainSelectedOrderItem.getOption().getName()).getId().equals
                                                (mainSelectedOrderItem.getOption().getId())) {
                                    currSelectedOrderItem.setOption(mainSelectedOrderItem.getOption());
                                    if(mainSelectedOrderItem != selectedOrderItem) {
                                        if (selectedOrderItem.getSubItems() == null) {
                                            selectedOrderItem.setSubItems(new ArrayList<>());
                                        }
                                        selectedOrderItem.getSubItems().add(currSelectedOrderItem);
                                    }
                                    System.out.println("sub items count "+mainSelectedOrderItem.getSubItems().size());
                                    addItemAndReload(mainSelectedOrderItem);
                                } else {
                                    loadMenuItemOptionsView(subItem, menuButtonView, menuOptionsView, false, menuItemName, currSelectedOrderItem);
                                }
                            } else { //leaf selection item where the subitem does not contain options or sub items
                                if(currSelectedOrderItem.getOption() == null) {
                                    currSelectedOrderItem.setOption(mainSelectedOrderItem.getOption());
                                }
                                if(mainSelectedOrderItem != selectedOrderItem) {
                                    if (selectedOrderItem.getSubItems() == null) {
                                        selectedOrderItem.setSubItems(new ArrayList<>());
                                    }
                                    selectedOrderItem.getSubItems().add(currSelectedOrderItem);
                                }
                                addItemAndReload(mainSelectedOrderItem);

                            }
                        }
                    });
                    menuOptionsView.addView(menuButtonView);
                }
            }
        }
    }

    private void reloadOrderItemView() {
        System.out.println("Reloading Order item view details for order id " + orderIdInputTextField.getText());
        orderDetailsView.removeAllViews();
        String inputOrderId = (orderIdInputTextField.getText() != null) ?
                orderIdInputTextField.getText().toString() : null;
        if(inputOrderId != null) {
            buildAndSendOrderQuery(inputOrderId);
        } else {
            showMessage("Order Id is empty !!");
        }
    }

    private void loadMenuView(String selectedMenuType, MenuDetail menuDetail, AbsoluteLayout view, LinearLayout menuOptionsView) {
        List<DrillDownMenuType> menuTypeList = menuDetail.getDrillDownMenus().getDrillDownMenuTypes();
        for (DrillDownMenuType menuType : menuTypeList) {
            if (menuType.getName().equals(selectedMenuType)) {
                loadMenuItems(menuType, menuDetail, view, menuOptionsView);
                break;
            }
        }
    }

    private void loadMenuTypes(Spinner spinner, MenuDetail menuDetail) {
        if (menuDetail != null && spinner != null) {
            List<String> menuTypes = new ArrayList<>();
            for (DrillDownMenuType menuType : menuDetail.getDrillDownMenus().getDrillDownMenuTypes()) {
                menuTypes.add(menuType.getName());
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item, menuTypes);
            arrayAdapter.setDropDownViewResource(android.R.layout
                    .simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);
        }
    }

    private MenuDetail loadMenuDetails(String response) {
        MenuDetail responseMenuDetail = null;
        System.out.println("loading menu details for " + response);
        SaxParserForGastromanager parser = SaxParserForGastromanager.getInstance();
        responseMenuDetail = parser.parseXml(response, true);
        menuDetail = responseMenuDetail;
        return responseMenuDetail;
    }

    private void loadOrderItemFromSelection() {
        Stack<String> orderDetailOutputStack = new Stack<>();
        ;
        while (!orderSelectionStack.empty()) {
            String itemName = orderSelectionStack.pop();
            String itemOptionSelected = (optionsSelectionMap != null) ? optionsSelectionMap.get(itemName + "Options"): null;

            if (itemOptionSelected == null) {
                orderDetailOutputStack.push(itemName);
            } else {
                orderDetailOutputStack.push(itemName + " -> " + itemOptionSelected);
            }
        }

        if (orderDetailOutputStack != null) {
            StringBuilder orderItemInfoBuilder = new StringBuilder();
            /*if (orderDetailsView.getText() != null && orderDetailsView.getText().length() > 0) {
                orderItemInfoBuilder.append(orderDetailsView.getText() + "\n");
            }*/
            while (!orderDetailOutputStack.empty()) {
                String temp = orderDetailOutputStack.pop();
                //orderItemInfoBuilder.append(order DetailOutputStack.pop() + "\n");
                System.out.println("curr item " + temp);

                orderItemInfoBuilder.append(temp + "\n");
            }
            System.out.println("Sub items : " + mainSelectedOrderItem.getSubItems());
            //orderDetailsView.setText(orderItemInfoBuilder.toString());

        }
    }

    @Override
    public void onOrderItemClick(int position) {
        OrderItemInfo orderItemInfo = orderListAdapter.getOrderItems().get(position);
        showMessage("Do you wish to remove the item "+orderItemInfo.getXmlText(), orderItemInfo);
        reloadOrderItemView();
    }

    @SuppressLint("StaticFieldLeak")
    private class SignOffOrdertask extends AsyncTask<Object, Void, Void> {
        Boolean response;
        MainActivity2 mainActivity2;
        SignOffOrderInfo requestIdentifier;

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Object... request) {
            Iterator iterator = Arrays.stream(request).iterator();
            int paramCount = 0;
            while (iterator.hasNext()) {
                Object param = iterator.next();
                switch (paramCount) {
                    case 0:
                        requestIdentifier = (SignOffOrderInfo) param;
                        break;
                    case 1:
                        mainActivity2 = (MainActivity2) param;
                        break;
                }
                paramCount++;
            }
            Client client = new Client();
            response = client.signOffOrder(requestIdentifier);
            System.out.println("isOrderPrinted " + response);
            client = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (response) {
                reloadOrderItemView();
                mainActivity2.showMessage("Order Details successfully sent to printer!!");
            } else {
                mainActivity2.showMessage("Failed to send order details to the printer!!");
            }
            super.onPostExecute(result);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetOrderDetailsTask extends AsyncTask<Object, Void, Void> {
        List<OrderItemInfo> response;
        MainActivity2 mainActivity2;
        OrderDetailQuery requestIdentifier;

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Object... request) {
            Iterator iterator = Arrays.stream(request).iterator();
            int paramCount = 0;
            while (iterator.hasNext()) {
                Object param = iterator.next();
                switch (paramCount) {
                    case 0:
                        requestIdentifier = (OrderDetailQuery) param;
                        break;
                    case 1:
                        mainActivity2 = (MainActivity2) param;
                        break;
                }
                paramCount++;
            }
            Client client = new Client();//.getInstance();
            //response = client.getResponse(Arrays.stream(request).findFirst().get());
            response = client.getOrderInfo(requestIdentifier);
            System.out.println("Requesting for " + Arrays.stream(request).findFirst().get());
            System.out.println("Response " + response);
            client = null;
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Void result) {
            //orderDetailsView.setText(response);
            orderListAdapter.updateOrderItemList(response);
            if (response == null || response.equals("")) {
                mainActivity2.showMessage("Order Details not found!!");
            } else {
                System.out.println("Order item details loaded");
            }
            super.onPostExecute(result);
        }
    }

    private class LoadMenuTask extends AsyncTask<Object, Void, MenuDetail> {
        MainActivity2 mainActivity2;
        String requestIdentifier;
        Spinner menuTypeSpinner;

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected MenuDetail doInBackground(Object... request) {
            Iterator iterator = Arrays.stream(request).iterator();
            int paramCount = 0;
            while (iterator.hasNext()) {
                Object param = iterator.next();
                switch (paramCount) {
                    case 0:
                        requestIdentifier = (String) param;
                        break;
                    case 1:
                        mainActivity2 = (MainActivity2) param;
                        break;
                    case 2:
                        menuTypeSpinner = (Spinner) param;
                        break;
                }
                paramCount++;
            }
            Client client = new Client();//Client.getInstance();
            //String serverResponse = client.getResponse(requestIdentifier);
            MenuDetail serverResponse = client.getMenuDetails(requestIdentifier);
            System.out.println("Requesting for " + requestIdentifier);
            //System.out.println("Received "+serverResponse);
            client = null;
            return serverResponse;
        }

        @Override
        protected void onPostExecute(MenuDetail result) {
            //txtOne.setText(response);
            System.out.println("Received on postExecute " + result);
            if (result == null) {
                mainActivity2.showMessage("Unable to receive Menu info !!");
            }
            //MenuDetail menuDetail = mainActivity2.loadMenuDetails(result);
            mainActivity2.loadMenuTypes(menuTypeSpinner, result);
            super.onPostExecute(result);
        }
    }


    private class GetNextOrderIdTask extends AsyncTask<Void, Void, Integer> {
        Integer response;

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Integer doInBackground(Void... voids) {
            Client client = new Client();//.getInstance();
            //response = client.getResponse(Arrays.stream(request).findFirst().get());
            response = client.getNewOrderId("newOrderId");
            System.out.println("Requesting for new order Id");
            System.out.println("Response " + response);
            client = null;
            return response;
        }

        @Override
        protected void onPostExecute(Integer result) {
            orderIdInputTextField.setText(result.toString());
            super.onPostExecute(result);
        }
    }

    private class GetHumanReadableOrderIdTask extends AsyncTask<Void, Void, Integer> {
        Integer response;

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Integer doInBackground(Void... voids) {
            Client client = new Client();
            response = client.getHumanReadableOrderId(floorIdInputTextField.getText().toString(),
                    tableIdInputTextField.getText().toString());
            System.out.println("Requesting for starting human readable order Id");
            System.out.println("Response " + response);
            client = null;
            return response;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(result != null) {
                orderIdInputTextField.setText(result.toString());
            } else {
                showMessage("No existing orders");
            }

            super.onPostExecute(result);
        }
    }

    private class AddOrderItemTask extends AsyncTask<SelectedOrderItem, Void, Void> {
        @RequiresApi(api = Build.VERSION_CODES.N)

        @Override
        protected Void doInBackground(SelectedOrderItem... selectedOrderItems) {
            Client client = new Client();//.getInstance();
            //response = client.getResponse(Arrays.stream(request).findFirst().get());
            client.sendOrderItemData(Arrays.stream(selectedOrderItems).findFirst().get());
            System.out.println("Sent order item to server");
            client = null;
            return null;
        }
    }


    private class RemoveOrderItemTask extends AsyncTask<OrderItemInfo, Void, Void> {
        @RequiresApi(api = Build.VERSION_CODES.N)

        @Override
        protected Void doInBackground(OrderItemInfo... orderItemInfos) {
            Client client = new Client();//.getInstance();
            client.removeOrderItem(Arrays.stream(orderItemInfos).findFirst().get());
            System.out.println("Sent order item for deletion to server");
            client = null;
            return null;
        }
    }
}