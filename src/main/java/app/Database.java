package app;

import java.sql.SQLException;
import java.util.List;

import app.models.*;
import sql_in_java.ORM;
import sql_in_java.Table;
import sql_in_java.Constraint;

public class Database {

    public static void registerTables(ORM orm) {
        Table Supplier = Table.create("Supplier")
            .has("supplierID").asInt().is(Constraint.PRIMARYKEY)
            .has("name").asString()
            .has("contact").asString()
            .has("email").asString()
            .has("address").asString()
            .has("rating").asDecimal();

        Table Ingredient = Table.create("Ingredient")
            .has("ingredientID").asInt().is(Constraint.PRIMARYKEY)
            .has("name").asString()
            .has("type").asString()
            .has("stock").asInt()
            .has("restockThreshold").asInt()
            .has("supplierID").asInt().is(Constraint.NOTNULL)
            .refers(Supplier.c("supplierID"));

        Table Product = Table.create("Product")
            .has("productID").asInt().is(Constraint.PRIMARYKEY)
            .has("name").asString()
            .has("price").asDecimal()
            .has("category").asString();

        Table CafeTable = Table.create("CafeTable")
            .has("tableID").asInt().is(Constraint.PRIMARYKEY)
            .has("capacity").asInt()
            .has("location").asString();

        Table Customer = Table.create("Customer")
            .has("customerID").asInt().is(Constraint.PRIMARYKEY)
            .has("firstName").asString()
            .has("lastName").asString();

        Table CustomerOrder = Table.create("CustomerOrder")
            .has("orderID").asInt().is(Constraint.PRIMARYKEY)
            .has("customerID").asInt().is(Constraint.NOTNULL)
            .has("tableID").asInt()
            .has("time").asInt()
            .has("paymentMethod").asString()
            .refers(Customer.c("customerID"))
            .refers(CafeTable.c("tableID"));

        Table OnlineOrder = Table.create("OnlineOrder")
            .has("orderID").asInt().is(Constraint.PRIMARYKEY)
            .has("isConfirmed").asBoolean()
            .refers(CustomerOrder.c("orderID"));

        Table RestockRequest = Table.create("RestockRequest")
            .has("requestID").asInt().is(Constraint.PRIMARYKEY)
            .has("ingredientID").asInt().is(Constraint.NOTNULL)
            .has("supplierID").asInt().is(Constraint.NOTNULL)
            .has("quantityRequested").asInt()
            .has("status").asString()
            .has("requestedAt").asInt()
            .refers(Ingredient.c("ingredientID"))
            .refers(Supplier.c("supplierID"));

        Table ProductIngredient = Table.create("ProductIngredient")
            .has("productIngredientID").asInt()
                .is(Constraint.PRIMARYKEY)
            .has("productID").asInt().is(Constraint.NOTNULL)
            .has("ingredientID").asInt().is(Constraint.NOTNULL)
            .has("quantityRequired").asInt()
            .refers(Product.c("productID"))
            .refers(Ingredient.c("ingredientID"));

        Table OrderItem = Table.create("OrderItem")
            .has("orderItemID").asInt()
                .is(Constraint.PRIMARYKEY)
            .has("orderID").asInt().is(Constraint.NOTNULL)
            .has("productID").asInt().is(Constraint.NOTNULL)
            .has("quantity").asInt()
            .has("preparedCount").asInt()
            .has("priceAtOrder").asDecimal()
            .refers(CustomerOrder.c("orderID"))
            .refers(Product.c("productID"));

        orm.register(Supplier.class, Supplier);
        orm.register(Ingredient.class, Ingredient);
        orm.register(Product.class, Product);
        orm.register(CafeTable.class, CafeTable);
        orm.register(Customer.class, Customer);
        orm.register(CustomerOrder.class, CustomerOrder);
        orm.register(OnlineOrder.class, OnlineOrder);
        orm.register(RestockRequest.class, RestockRequest);
        orm.register(ProductIngredient.class, ProductIngredient);
        orm.register(OrderItem.class, OrderItem);
        
        // Add a status column to CustomerOrder for filtering (used in queries)
        CustomerOrder.has("status").asString();
    }

    public static void createTables(ORM orm) {
        Database.registerTables(orm);
        String tables = orm.createSchema();
        try {
            orm.getConnection().createStatement().executeUpdate(tables);
        } catch (SQLException e) {
            throw new RuntimeException("ERROR while emitting DDL in createTables. \n DDL: " + tables, e);
        }
    }

    public static List<Product> selectAllProducts(ORM orm) {
        return orm.from(Product.class).fetch();
    }
}
