package cache;

import controllers.OrderController;
import model.Order;
import utils.Config;
import java.util.ArrayList;

//TODO: Build this cache and use it. FIX
public class OrderCache {

    // List of orders
    private ArrayList<Order> orders;

    // The time the cache should live
    private long ttl;

    // Sets the time when the cache is created
    private long created;

    public OrderCache() {
        this.ttl = Config.getOrder_TTL();
    }

    public ArrayList<Order> getOrders(Boolean forceUpdate) {

        // Force updateOrder clears the cache when set to true
        // However, the age of the cache is determined and the result says if we should updateUser.
        // If the list is empty/null it will also check for new products
        if (forceUpdate
                || ((this.created + this.ttl) >= (System.currentTimeMillis() / 1000L))
                || this.orders == null) {

            // Get orders from controller, since we wish to updateOrders.
            ArrayList<Order> orders = OrderController.getOrders();

            // Set orders for the instance and set created timestamp
            this.orders = orders;
            this.created = System.currentTimeMillis() / 1000L;
        }

        // Return the orders
        return this.orders;
    }
}