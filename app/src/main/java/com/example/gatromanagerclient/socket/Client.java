/*Copyright 2021 GastroRice

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


package com.example.gatromanagerclient.socket;

import com.example.gatromanagerclient.ui.splash.SplashActivity;
import android.provider.Settings;
import com.example.gatromanagerclient.ui.settings.SettingsActivity;
import com.example.gatromanagerclient.ui.splash.SplashActivity;
import com.gastromanager.models.HumanReadableIdQuery;
import com.gastromanager.models.MenuDetail;
import com.gastromanager.models.OrderDetailQuery;
import com.gastromanager.models.OrderDetailsView;
import com.gastromanager.models.OrderItemInfo;
import com.gastromanager.models.OrderItemTransactionInfo;
import com.gastromanager.models.PaymentInformationQuery;
import com.gastromanager.models.SelectedOrderItem;
import com.gastromanager.models.SignOffOrderInfo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Client {

    Socket socket  = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    Integer serverPort = 5000;
    static Client client = null;
    public Client() {
        try {
            //send to server
            socket = new Socket(SplashActivity.SERVER_IP, SplashActivity.SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Client getInstance() {
        if(client == null) {
            client = new Client();
        }
        return client;
    }

    public void close() {
        try {
            socket.close();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void attemptSend() {
    }

    public Boolean signOffOrder(SignOffOrderInfo request) {
        Boolean isOrderPrinted  = false;

        try {
            out.writeObject(request);
            isOrderPrinted = (Boolean) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return isOrderPrinted;
    }

    public List<OrderItemInfo> getOrderInfo(OrderDetailQuery request) {
        ArrayList<OrderItemInfo> response = null;

        try {
            //sendTextData(request, out); //request could be orderId
            out.writeObject(request);
            response = (ArrayList<OrderItemInfo>) in.readObject();

            //response = (List<OrderItem>) in.readObject();
            if(response == null || response.size() == 0) {
                System.out.println(" No items received");
            } else {
                System.out.println( "received item count " + response.size());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return response;
    }

    public List<OrderItemInfo> payOrderItems(OrderItemTransactionInfo request) {
        ArrayList<OrderItemInfo> response = null;

        try {
            //sendTextData(request, out); //request could be orderId
            out.writeObject(request);
            response = (ArrayList<OrderItemInfo>) in.readObject();

            //response = (List<OrderItem>) in.readObject();
            if(response == null || response.size() == 0) {
                System.out.println(" No items received");
            } else {
                System.out.println( "received item count " + response.size());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return response;
    }

   /* public String getOrderInfo(OrderDetailQuery request) {
        StringBuilder responseBuilder = null;

        try {
            //sendTextData(request, out); //request could be orderId
            out.writeObject(request);
            ArrayList<String> orderItems = (ArrayList<String>) in.readObject();
            if(orderItems.size() == 0) {
                responseBuilder = null;
            } else {
                for (String item : orderItems) {
                    if (responseBuilder == null) {
                        responseBuilder = new StringBuilder();
                    }
                    responseBuilder.append(item);
                    System.out.println(item);

                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return (responseBuilder == null) ? Constants.EMPTY_RESULT :responseBuilder.toString();
    }*/

    public String getResponse(String request) {
        String response = null;

        try {
            sendTextData(request, out); //request could be orderId / menu
            //in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            Object responseFromServer = in.readObject();
            if(responseFromServer instanceof OrderDetailsView) {
                response =  ((OrderDetailsView) responseFromServer).getOrderDetailsView();
            }
            //System.out.println("Client received order from Server: "+response);
            //close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return response;
    }

    public Integer getNewOrderId(String newOrderId) {
        Object response = null;
        Integer newGenOrderId = null;
        try {
            sendTextData(newOrderId, out); //request could be orderId / menu
            //in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            //if(in.available() > 0){
            response = in.readObject();
            newGenOrderId = (response == null) ? null : (Integer) response;
            //}
            //System.out.println("Client received order from Server: "+response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return newGenOrderId;
    }

    public Integer getHumanReadableOrderId(String floorId, String tableId) {
        Object response = null;
        Integer startingHumanReadableId = null;
        try {
            HumanReadableIdQuery humanReadableIdQuery = new HumanReadableIdQuery();
            humanReadableIdQuery.setFloorId(Integer.parseInt(floorId));
            humanReadableIdQuery.setTableId(Integer.parseInt(tableId));
            out.writeObject(humanReadableIdQuery);
            response = in.readObject();
            startingHumanReadableId = (response == null) ? null : (Integer) response;
            //}
            //System.out.println("Client received order from Server: "+response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            startingHumanReadableId = 0;
        } finally {
            close();
        }

        return startingHumanReadableId;
    }

    public void readQueryPaymentInformation()
    {
        PaymentInformationQuery response;
        try {
            PaymentInformationQuery piq = new PaymentInformationQuery();
            out.writeObject(piq);
            response = (PaymentInformationQuery) in.readObject();
            SettingsActivity.currency = response.getCurrency();
            SettingsActivity.salestaxes = response.getTaxes();
            //}
            //System.out.println("Client received order from Server: "+response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public MenuDetail getMenuDetails(String request) {
        Object response = null;
        MenuDetail menuDetail = null;

        try {
            sendTextData(request, out); //request could be orderId / menu
            //in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            //if(in.available() > 0){
                response = in.readObject();
                menuDetail = (response == null) ? null : (MenuDetail) response;
            //}
            //System.out.println("Client received order from Server: "+response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return menuDetail;
    }


    public void sendTextData(String textContent, ObjectOutputStream out) {
        try {
            System.out.println(textContent);
            out.writeObject(textContent);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void sendOrderItemData(SelectedOrderItem orderItem) {
        try {
            System.out.println("sending order item info "+orderItem.getItemName());
            out.writeObject(orderItem);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void removeOrderItem(OrderItemInfo orderItem) {
        try {
            System.out.println("sending order item info for deletion "+orderItem.getOrderId());
            out.writeObject(orderItem);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {

        Client client = Client.getInstance();
        client.getResponse("1");

    }
}