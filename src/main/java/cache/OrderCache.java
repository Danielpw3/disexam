package cache;

import controllers.OrderController;
import model.Order;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it. - Fixed (getOrders)
public class OrderCache {

    private ArrayList <Order> orders;

    private long ttl;

    private long created;

    public OrderCache () {
        this.ttl = Config.getOrderTtl();
    }

    public ArrayList <Order> getOrders (Boolean forceUpdate) {

        if (forceUpdate
                ||((this.created + this.ttl) <= (System.currentTimeMillis()/1000L)) // mindre end, istedet for og null, istedet ofte isEmpty
                || this.orders == null) {
            // Read orders from the database
            ArrayList <Order> orders = OrderController.getOrdersFromdb();

            this.orders = orders;
            this.created = System.currentTimeMillis()/1000L;
        }
        return this.orders;
    }

    // Add order to cache
    public void addOrder(Order order) {
        this.orders.add(order);
    }

    // Remove order to cache
    public void removeOrder(Order order) {
        this.orders.remove(order);
    }
}
