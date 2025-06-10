/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import Model.MenuItem;
import Model.MenuItemAddon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to load menu items into the database
 */
public class MenuDataLoader {
    private static final Logger logger = Logger.getLogger(MenuDataLoader.class.getName());
    private final DatabaseManager dbManager;
    private final MenuItemDAO menuItemDAO;
    
    /**
     * Constructor
     */
    public MenuDataLoader() {
        this.dbManager = DatabaseManager.getInstance();
        this.menuItemDAO = MenuItemDAO.getInstance();
    }
    
    /**
     * Load all menu items into the database
     * @return true if successful, false otherwise
     */
    public boolean loadMenuItems() {
        try {
            // Clear existing menu items first (optional, comment out if you want to preserve existing items)
            clearMenuItems();
            
            // Load categories
            loadCategories();
            
            // Load menu items by category
            loadAppetizers();
            loadSalads();
            loadEntrees();
            loadSides();
            loadSandwiches();
            loadBurgers();
            loadBeverages();
            
            // Refresh the menu cache
            MenuCache.refreshCache();
            
            logger.info("Menu items loaded successfully");
            return true;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error loading menu items", ex);
            return false;
        }
    }
    
    /**
     * Clear all existing menu items
     */
    private void clearMenuItems() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);
            
            // First delete all menu item addons
            String sqlAddons = "DELETE FROM menu_item_addon";
            stmt = conn.prepareStatement(sqlAddons);
            stmt.executeUpdate();
            
            // Then delete all menu items
            String sqlItems = "DELETE FROM menu_item";
            stmt = conn.prepareStatement(sqlItems);
            stmt.executeUpdate();
            
            conn.commit();
            logger.info("Cleared existing menu items");
        } catch (SQLException ex) {
            if (conn != null) {
                conn.rollback();
            }
            throw ex;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    /**
     * Load menu categories
     */
    private void loadCategories() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);
            
            // Clear existing categories
            String sqlClear = "DELETE FROM menu_category";
            stmt = conn.prepareStatement(sqlClear);
            stmt.executeUpdate();
            stmt.close();
            
            // Insert categories
            String sql = "INSERT INTO menu_category (category_id, name, display_order) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            
            // Category IDs:
            // 1: Appetizers
            // 2: Salads
            // 3: Entrees
            // 4: Sandwiches
            // 5: Burgers
            // 6: Sides
            // 7: Beverages
            
            stmt.setInt(1, 1);
            stmt.setString(2, "Appetizers");
            stmt.setInt(3, 1);
            stmt.executeUpdate();
            
            stmt.setInt(1, 2);
            stmt.setString(2, "Salads");
            stmt.setInt(3, 2);
            stmt.executeUpdate();
            
            stmt.setInt(1, 3);
            stmt.setString(2, "Entrees");
            stmt.setInt(3, 3);
            stmt.executeUpdate();
            
            stmt.setInt(1, 4);
            stmt.setString(2, "Sandwiches");
            stmt.setInt(3, 4);
            stmt.executeUpdate();
            
            stmt.setInt(1, 5);
            stmt.setString(2, "Burgers");
            stmt.setInt(3, 5);
            stmt.executeUpdate();
            
            stmt.setInt(1, 6);
            stmt.setString(2, "Sides");
            stmt.setInt(3, 6);
            stmt.executeUpdate();
            
            stmt.setInt(1, 7);
            stmt.setString(2, "Beverages");
            stmt.setInt(3, 7);
            stmt.executeUpdate();
            
            conn.commit();
            logger.info("Menu categories loaded successfully");
        } catch (SQLException ex) {
            if (conn != null) {
                conn.rollback();
            }
            throw ex;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    /**
     * Load appetizers menu items
     */
    private void loadAppetizers() throws SQLException {
        // Category ID for Appetizers is 1
        int categoryId = 1;
        
        // Chicken Nachos
        MenuItem chickenNachos = new MenuItem();
        chickenNachos.setItemId("APP001");
        chickenNachos.setName("Chicken Nachos");
        chickenNachos.setDescription("Pulled chicken, spicy white cheese sauce, & cheddar cheese topped with red onions & cilantro.");
        chickenNachos.setCategoryId(categoryId);
        chickenNachos.setPrice(8.50);
        chickenNachos.setAvailable(true);
        
        // Add BBQ sauce addon
        List<MenuItemAddon> chickenNachosAddons = new ArrayList<>();
        MenuItemAddon bbqSauce = new MenuItemAddon();
        bbqSauce.setName("BBQ Sauce");
        bbqSauce.setPrice(0.50);
        chickenNachosAddons.add(bbqSauce);
        chickenNachos.setAddons(chickenNachosAddons);
        
        // Pork Nachos
        MenuItem porkNachos = new MenuItem();
        porkNachos.setItemId("APP002");
        porkNachos.setName("Pork Nachos");
        porkNachos.setDescription("Pulled pork, spicy white cheese sauce, & pepper jack cheese topped with tomato, scallions & cilantro.");
        porkNachos.setCategoryId(categoryId);
        porkNachos.setPrice(8.50);
        porkNachos.setAvailable(true);
        
        // Add BBQ sauce addon
        List<MenuItemAddon> porkNachosAddons = new ArrayList<>();
        MenuItemAddon bbqSauce2 = new MenuItemAddon();
        bbqSauce2.setName("BBQ Sauce");
        bbqSauce2.setPrice(0.50);
        porkNachosAddons.add(bbqSauce2);
        porkNachos.setAddons(porkNachosAddons);
        
        // Sliders
        MenuItem sliders = new MenuItem();
        sliders.setItemId("APP003");
        sliders.setName("Pork or Chicken Sliders");
        sliders.setDescription("3 sliders with your choice of pulled pork or chicken. Choose from Chipotle, Jim Beam or Carolina Gold BBQ sauce.");
        sliders.setCategoryId(categoryId);
        sliders.setPrice(5.00);
        sliders.setAvailable(true);
        
        // Add sauce options addons
        List<MenuItemAddon> slidersAddons = new ArrayList<>();
        MenuItemAddon chipotle = new MenuItemAddon();
        chipotle.setName("Chipotle Sauce");
        chipotle.setPrice(0.0);
        slidersAddons.add(chipotle);
        
        MenuItemAddon jimBeam = new MenuItemAddon();
        jimBeam.setName("Jim Beam Sauce");
        jimBeam.setPrice(0.0);
        slidersAddons.add(jimBeam);
        
        MenuItemAddon carolinaGold = new MenuItemAddon();
        carolinaGold.setName("Carolina Gold BBQ");
        carolinaGold.setPrice(0.0);
        slidersAddons.add(carolinaGold);
        
        sliders.setAddons(slidersAddons);
        
        // Catfish Bites
        MenuItem catfishBites = new MenuItem();
        catfishBites.setItemId("APP004");
        catfishBites.setName("Catfish Bites");
        catfishBites.setDescription("Catfish pieces cornmeal-battered & fried. Served with lemon & spicy cocktail sauce.");
        catfishBites.setCategoryId(categoryId);
        catfishBites.setPrice(6.50);
        catfishBites.setAvailable(true);
        
        // Fried Veggies
        MenuItem friedVeggies = new MenuItem();
        friedVeggies.setItemId("APP005");
        friedVeggies.setName("Fried Veggies");
        friedVeggies.setDescription("Choice of okra, zucchini, squash, or Mix & Match. Served with a side of spicy ranch.");
        friedVeggies.setCategoryId(categoryId);
        friedVeggies.setPrice(6.50);
        friedVeggies.setAvailable(true);
        
        // Add veggie choice addons
        List<MenuItemAddon> friedVeggiesAddons = new ArrayList<>();
        MenuItemAddon okra = new MenuItemAddon();
        okra.setName("Okra");
        okra.setPrice(0.0);
        friedVeggiesAddons.add(okra);
        
        MenuItemAddon zucchini = new MenuItemAddon();
        zucchini.setName("Zucchini");
        zucchini.setPrice(0.0);
        friedVeggiesAddons.add(zucchini);
        
        MenuItemAddon squash = new MenuItemAddon();
        squash.setName("Squash");
        squash.setPrice(0.0);
        friedVeggiesAddons.add(squash);
        
        MenuItemAddon mixMatch = new MenuItemAddon();
        mixMatch.setName("Mix & Match");
        mixMatch.setPrice(0.0);
        friedVeggiesAddons.add(mixMatch);
        
        friedVeggies.setAddons(friedVeggiesAddons);
        
        // Save all appetizers to database
        menuItemDAO.add(chickenNachos);
        menuItemDAO.add(porkNachos);
        menuItemDAO.add(sliders);
        menuItemDAO.add(catfishBites);
        menuItemDAO.add(friedVeggies);
        
        logger.info("Appetizers loaded successfully");
    }
    
    /**
     * Load salads menu items
     */
    private void loadSalads() throws SQLException {
        // Category ID for Salads is 2
        int categoryId = 2;
        
        // House Salad
        MenuItem houseSalad = new MenuItem();
        houseSalad.setItemId("SAL001");
        houseSalad.setName("House Salad");
        houseSalad.setDescription("Mixed Greens, topped with bacon, tomato & blue cheese crumbles.");
        houseSalad.setCategoryId(categoryId);
        houseSalad.setPrice(7.50);
        houseSalad.setAvailable(true);
        
        // Wedge Salad
        MenuItem wedgeSalad = new MenuItem();
        wedgeSalad.setItemId("SAL002");
        wedgeSalad.setName("Wedge Salad");
        wedgeSalad.setDescription("Iceberg lettuce wedge topped with bacon, tomato & blue cheese crumbles.");
        wedgeSalad.setCategoryId(categoryId);
        wedgeSalad.setPrice(7.50);
        wedgeSalad.setAvailable(true);
        
        // Caesar Salad
        MenuItem caesarSalad = new MenuItem();
        caesarSalad.setItemId("SAL003");
        caesarSalad.setName("Caesar Salad");
        caesarSalad.setDescription("Romaine lettuce, shredded Parmesan cheese & croutons tossed in Caesar dressing.");
        caesarSalad.setCategoryId(categoryId);
        caesarSalad.setPrice(7.50);
        caesarSalad.setAvailable(true);
        
        // Sweet Potato Chicken Salad
        MenuItem sweetPotatoSalad = new MenuItem();
        sweetPotatoSalad.setItemId("SAL004");
        sweetPotatoSalad.setName("Sweet Potato Chicken Salad");
        sweetPotatoSalad.setDescription("Mixed greens, red onion, dried cranberries & goat cheese crumbles topped with chilled sweet potato crusted chicken.");
        sweetPotatoSalad.setCategoryId(categoryId);
        sweetPotatoSalad.setPrice(11.50);
        sweetPotatoSalad.setAvailable(true);
        
        // Save all salads to database
        menuItemDAO.add(houseSalad);
        menuItemDAO.add(wedgeSalad);
        menuItemDAO.add(caesarSalad);
        menuItemDAO.add(sweetPotatoSalad);
        
        logger.info("Salads loaded successfully");
    }
    
    /**
     * Load entrees menu items
     */
    private void loadEntrees() throws SQLException {
        // Category ID for Entrees is 3
        int categoryId = 3;
        
        // Shrimp & Grits
        MenuItem shrimpGrits = new MenuItem();
        shrimpGrits.setItemId("ENT001");
        shrimpGrits.setName("Shrimp & Grits");
        shrimpGrits.setDescription("Sautéed shrimp with garlic served on top of cheese grits, topped with sautéed peppers & onions. Served with 2 sides.");
        shrimpGrits.setCategoryId(categoryId);
        shrimpGrits.setPrice(13.50);
        shrimpGrits.setAvailable(true);
        
        // Sweet Tea Fried Chicken
        MenuItem sweetTeaChicken = new MenuItem();
        sweetTeaChicken.setItemId("ENT002");
        sweetTeaChicken.setName("Sweet Tea Fried Chicken");
        sweetTeaChicken.setDescription("Fried chicken breast marinated in sweet tea & spices, topped with a sweet tea reduction. Served with 2 sides.");
        sweetTeaChicken.setCategoryId(categoryId);
        sweetTeaChicken.setPrice(11.50);
        sweetTeaChicken.setAvailable(true);
        
        // Caribbean Chicken
        MenuItem caribbeanChicken = new MenuItem();
        caribbeanChicken.setItemId("ENT003");
        caribbeanChicken.setName("Caribbean Chicken");
        caribbeanChicken.setDescription("Grilled chicken marinated in spicy Caribbean seasoning topped with mango salsa & avocado. Served with 2 sides.");
        caribbeanChicken.setCategoryId(categoryId);
        caribbeanChicken.setPrice(11.50);
        caribbeanChicken.setAvailable(true);
        
        // Grilled Pork Chops
        MenuItem porkChops = new MenuItem();
        porkChops.setItemId("ENT004");
        porkChops.setName("Grilled Pork Chops");
        porkChops.setDescription("Two bone-in grilled pork chops. Served with 2 sides.");
        porkChops.setCategoryId(categoryId);
        porkChops.setPrice(11.00);
        porkChops.setAvailable(true);
        
        // New York Strip Steak
        MenuItem nyStrip = new MenuItem();
        nyStrip.setItemId("ENT005");
        nyStrip.setName("New York Strip Steak");
        nyStrip.setDescription("New York Strip Steak cut in-house. Cooked to your desired temperature. Served with 2 sides.");
        nyStrip.setCategoryId(categoryId);
        nyStrip.setPrice(17.00);
        nyStrip.setAvailable(true);
        
        // Cooking temperature addons
        List<MenuItemAddon> steakAddons = new ArrayList<>();
        MenuItemAddon rare = new MenuItemAddon();
        rare.setName("Rare");
        rare.setPrice(0.0);
        steakAddons.add(rare);
        
        MenuItemAddon mediumRare = new MenuItemAddon();
        mediumRare.setName("Medium Rare");
        mediumRare.setPrice(0.0);
        steakAddons.add(mediumRare);
        
        MenuItemAddon medium = new MenuItemAddon();
        medium.setName("Medium");
        medium.setPrice(0.0);
        steakAddons.add(medium);
        
        MenuItemAddon mediumWell = new MenuItemAddon();
        mediumWell.setName("Medium Well");
        mediumWell.setPrice(0.0);
        steakAddons.add(mediumWell);
        
        MenuItemAddon wellDone = new MenuItemAddon();
        wellDone.setName("Well Done");
        wellDone.setPrice(0.0);
        steakAddons.add(wellDone);
        
        nyStrip.setAddons(steakAddons);
        
        // Seared Tuna
        MenuItem searedTuna = new MenuItem();
        searedTuna.setItemId("ENT006");
        searedTuna.setName("Seared Tuna");
        searedTuna.setDescription("Seared ahi tuna cooked to your desired temperature, topped with mango salsa & a honey lime vinaigrette drizzle. Served with 2 sides.");
        searedTuna.setCategoryId(categoryId);
        searedTuna.setPrice(15.00);
        searedTuna.setAvailable(true);
        searedTuna.setAddons(steakAddons); // Reuse the temperature options
        
        // Captain Crunch Chicken Tenders
        MenuItem chickenTenders = new MenuItem();
        chickenTenders.setItemId("ENT007");
        chickenTenders.setName("Captain Crunch Chicken Tenders");
        chickenTenders.setDescription("Fried chicken tenders coated in Captain Crunch with a dipping sauce. Served with 2 sides.");
        chickenTenders.setCategoryId(categoryId);
        chickenTenders.setPrice(11.50);
        chickenTenders.setAvailable(true);
        
        // Shock Top Grouper Fingers
        MenuItem grouperFingers = new MenuItem();
        grouperFingers.setItemId("ENT008");
        grouperFingers.setName("Shock Top Grouper Fingers");
        grouperFingers.setDescription("Shock Top beer-battered grouper served with tartar sauce & a lemon extra. Served with 2 sides.");
        grouperFingers.setCategoryId(categoryId);
        grouperFingers.setPrice(11.50);
        grouperFingers.setAvailable(true);
        
        // Mac & Cheese Bar
        MenuItem macCheese = new MenuItem();
        macCheese.setItemId("ENT009");
        macCheese.setName("Mac & Cheese Bar");
        macCheese.setDescription("Cast iron skillet of mac & cheese with choice of regular cheese or spicy cheese & choice of two toppings.");
        macCheese.setCategoryId(categoryId);
        macCheese.setPrice(8.50);
        macCheese.setAvailable(true);
        
        // Mac & Cheese toppings
        List<MenuItemAddon> macCheeseAddons = new ArrayList<>();
        MenuItemAddon pepperJack = new MenuItemAddon();
        pepperJack.setName("Pepper Jack Cheese");
        pepperJack.setPrice(0.0);
        macCheeseAddons.add(pepperJack);
        
        MenuItemAddon cheddar = new MenuItemAddon();
        cheddar.setName("Cheddar Cheese");
        cheddar.setPrice(0.0);
        macCheeseAddons.add(cheddar);
        
        MenuItemAddon swiss = new MenuItemAddon();
        swiss.setName("Swiss Cheese");
        swiss.setPrice(0.0);
        macCheeseAddons.add(swiss);
        
        MenuItemAddon mozzarella = new MenuItemAddon();
        mozzarella.setName("Mozzarella Cheese");
        mozzarella.setPrice(0.0);
        macCheeseAddons.add(mozzarella);
        
        MenuItemAddon goatCheese = new MenuItemAddon();
        goatCheese.setName("Goat Cheese");
        goatCheese.setPrice(0.0);
        macCheeseAddons.add(goatCheese);
        
        MenuItemAddon bacon = new MenuItemAddon();
        bacon.setName("Bacon");
        bacon.setPrice(0.0);
        macCheeseAddons.add(bacon);
        
        MenuItemAddon broccoli = new MenuItemAddon();
        broccoli.setName("Broccoli");
        broccoli.setPrice(0.0);
        macCheeseAddons.add(broccoli);
        
        MenuItemAddon mushrooms = new MenuItemAddon();
        mushrooms.setName("Mushrooms");
        mushrooms.setPrice(0.0);
        macCheeseAddons.add(mushrooms);
        
        MenuItemAddon grilledOnions = new MenuItemAddon();
        grilledOnions.setName("Grilled Onions");
        grilledOnions.setPrice(0.0);
        macCheeseAddons.add(grilledOnions);
        
        MenuItemAddon jalapenos = new MenuItemAddon();
        jalapenos.setName("Jalapenos");
        jalapenos.setPrice(0.0);
        macCheeseAddons.add(jalapenos);
        
        MenuItemAddon spinach = new MenuItemAddon();
        spinach.setName("Spinach");
        spinach.setPrice(0.0);
        macCheeseAddons.add(spinach);
        
        MenuItemAddon tomatoes = new MenuItemAddon();
        tomatoes.setName("Tomatoes");
        tomatoes.setPrice(0.0);
        macCheeseAddons.add(tomatoes);
        
        macCheese.setAddons(macCheeseAddons);
        
        // Save all entrees to database
        menuItemDAO.add(shrimpGrits);
        menuItemDAO.add(sweetTeaChicken);
        menuItemDAO.add(caribbeanChicken);
        menuItemDAO.add(porkChops);
        menuItemDAO.add(nyStrip);
        menuItemDAO.add(searedTuna);
        menuItemDAO.add(chickenTenders);
        menuItemDAO.add(grouperFingers);
        menuItemDAO.add(macCheese);
        
        logger.info("Entrees loaded successfully");
    }
    
    /**
     * Load sandwiches menu items
     */
    private void loadSandwiches() throws SQLException {
        // Category ID for Sandwiches is 4
        int categoryId = 4;
        
        // Grilled Cheese
        MenuItem grilledCheese = new MenuItem();
        grilledCheese.setItemId("SAN001");
        grilledCheese.setName("Grilled Cheese");
        grilledCheese.setDescription("American cheese served on multigrain or white bread.");
        grilledCheese.setCategoryId(categoryId);
        grilledCheese.setPrice(5.50);
        grilledCheese.setAvailable(true);
        
        // Add bread choice addons
        List<MenuItemAddon> breadAddons = new ArrayList<>();
        MenuItemAddon multigrain = new MenuItemAddon();
        multigrain.setName("Multigrain Bread");
        multigrain.setPrice(0.0);
        breadAddons.add(multigrain);
        
        MenuItemAddon white = new MenuItemAddon();
        white.setName("White Bread");
        white.setPrice(0.0);
        breadAddons.add(white);
        
        grilledCheese.setAddons(breadAddons);
        
        // Chicken BLT&A
        MenuItem chickenBLTA = new MenuItem();
        chickenBLTA.setItemId("SAN002");
        chickenBLTA.setName("Chicken BLT&A");
        chickenBLTA.setDescription("Grilled chicken, bacon, lettuce, tomato & avocado on a pretzel bun.");
        chickenBLTA.setCategoryId(categoryId);
        chickenBLTA.setPrice(10.00);
        chickenBLTA.setAvailable(true);
        
        // Philly
        MenuItem philly = new MenuItem();
        philly.setItemId("SAN003");
        philly.setName("Philly");
        philly.setDescription("Choice of shaved New York Strip steak or grilled chicken topped with mushrooms, peppers, onions & provolone cheese on a hoagie.");
        philly.setCategoryId(categoryId);
        philly.setPrice(13.50);
        philly.setAvailable(true);
        
        // Add meat choice addons
        List<MenuItemAddon> meatAddons = new ArrayList<>();
        MenuItemAddon steak = new MenuItemAddon();
        steak.setName("NY Strip Steak");
        steak.setPrice(0.0);
        meatAddons.add(steak);
        
        MenuItemAddon chicken = new MenuItemAddon();
        chicken.setName("Grilled Chicken");
        chicken.setPrice(0.0);
        meatAddons.add(chicken);
        
        philly.setAddons(meatAddons);
        
        // Club
        MenuItem club = new MenuItem();
        club.setItemId("SAN004");
        club.setName("Club");
        club.setDescription("Ham, turkey, Swiss, cheddar, lettuce, tomato, mayo & bacon on multigrain bread.");
        club.setCategoryId(categoryId);
        club.setPrice(10.00);
        club.setAvailable(true);
        
        // Meatball Sub
        MenuItem meatballSub = new MenuItem();
        meatballSub.setItemId("SAN005");
        meatballSub.setName("Meatball Sub");
        meatballSub.setDescription("House-made meatballs topped with marinara & mozzarella cheese. Sautéed pepper & onions on request.");
        meatballSub.setCategoryId(categoryId);
        meatballSub.setPrice(10.00);
        meatballSub.setAvailable(true);
        
        // Add peppers & onions addon
        List<MenuItemAddon> meatballAddons = new ArrayList<>();
        MenuItemAddon peppersOnions = new MenuItemAddon();
        peppersOnions.setName("Sautéed Peppers & Onions");
        peppersOnions.setPrice(0.0);
        meatballAddons.add(peppersOnions);
        
        meatballSub.setAddons(meatballAddons);
        
        // Save all sandwiches to database
        menuItemDAO.add(grilledCheese);
        menuItemDAO.add(chickenBLTA);
        menuItemDAO.add(philly);
        menuItemDAO.add(club);
        menuItemDAO.add(meatballSub);
        
        logger.info("Sandwiches loaded successfully");
    }
    
    /**
     * Load burgers menu items
     */
    private void loadBurgers() throws SQLException {
        // Category ID for Burgers is 5
        int categoryId = 5;
        
        // Bacon Cheeseburger
        MenuItem baconCheeseburger = new MenuItem();
        baconCheeseburger.setItemId("BUR001");
        baconCheeseburger.setName("Bacon Cheeseburger");
        baconCheeseburger.setDescription("8-ounce burger topped with bacon & your choice of cheese on a brioche bun.");
        baconCheeseburger.setCategoryId(categoryId);
        baconCheeseburger.setPrice(11.00);
        baconCheeseburger.setAvailable(true);
        
        // Add cheese choice addons
        List<MenuItemAddon> cheeseAddons = new ArrayList<>();
        MenuItemAddon cheddar = new MenuItemAddon();
        cheddar.setName("Cheddar");
        cheddar.setPrice(0.0);
        cheeseAddons.add(cheddar);
        
        MenuItemAddon american = new MenuItemAddon();
        american.setName("American");
        american.setPrice(0.0);
        cheeseAddons.add(american);
        
        MenuItemAddon swiss = new MenuItemAddon();
        swiss.setName("Swiss");
        swiss.setPrice(0.0);
        cheeseAddons.add(swiss);
        
        MenuItemAddon provolone = new MenuItemAddon();
        provolone.setName("Provolone");
        provolone.setPrice(0.0);
        cheeseAddons.add(provolone);
        
        MenuItemAddon pepperJack = new MenuItemAddon();
        pepperJack.setName("Pepper Jack");
        pepperJack.setPrice(0.0);
        cheeseAddons.add(pepperJack);
        
        MenuItemAddon blueCheese = new MenuItemAddon();
        blueCheese.setName("Blue Cheese");
        blueCheese.setPrice(0.0);
        cheeseAddons.add(blueCheese);
        
        MenuItemAddon pimento = new MenuItemAddon();
        pimento.setName("Pimento Cheese");
        pimento.setPrice(0.0);
        cheeseAddons.add(pimento);
        
        baconCheeseburger.setAddons(cheeseAddons);
        
        // Carolina Burger
        MenuItem carolinaBurger = new MenuItem();
        carolinaBurger.setItemId("BUR002");
        carolinaBurger.setName("Carolina Burger");
        carolinaBurger.setDescription("8-ounce burger topped with chili, diced onions & slaw on a brioche bun.");
        carolinaBurger.setCategoryId(categoryId);
        carolinaBurger.setPrice(11.00);
        carolinaBurger.setAvailable(true);
        
        // Portobello Burger
        MenuItem portobelloBurger = new MenuItem();
        portobelloBurger.setItemId("BUR003");
        portobelloBurger.setName("Portobello Burger (V)");
        portobelloBurger.setDescription("Marinated Portobello mushroom cap topped with mango salsa, lettuce, tomato & onion on a telera bun.");
        portobelloBurger.setCategoryId(categoryId);
        portobelloBurger.setPrice(8.50);
        portobelloBurger.setAvailable(true);
        
        // Vegan Boca Burger
        MenuItem veganBurger = new MenuItem();
        veganBurger.setItemId("BUR004");
        veganBurger.setName("Vegan Boca Burger (V)");
        veganBurger.setDescription("Vegan Boca Burger topped with lettuce, tomato & onion on a telera bun.");
        veganBurger.setCategoryId(categoryId);
        veganBurger.setPrice(10.00);
        veganBurger.setAvailable(true);
        
        // Save all burgers to database
        menuItemDAO.add(baconCheeseburger);
        menuItemDAO.add(carolinaBurger);
        menuItemDAO.add(portobelloBurger);
        menuItemDAO.add(veganBurger);
        
        logger.info("Burgers loaded successfully");
    }
    
    /**
     * Load sides menu items
     */
    private void loadSides() throws SQLException {
        // Category ID for Sides is 6
        int categoryId = 6;
        
        // Curly Fries
        MenuItem curlyFries = new MenuItem();
        curlyFries.setItemId("SID001");
        curlyFries.setName("Curly Fries");
        curlyFries.setDescription("Seasoned curly french fries.");
        curlyFries.setCategoryId(categoryId);
        curlyFries.setPrice(2.50);
        curlyFries.setAvailable(true);
        
        // Wing Chips
        MenuItem wingChips = new MenuItem();
        wingChips.setItemId("SID002");
        wingChips.setName("Wing Chips");
        wingChips.setDescription("Crispy fried potato chips, perfect for dipping.");
        wingChips.setCategoryId(categoryId);
        wingChips.setPrice(2.50);
        wingChips.setAvailable(true);
        
        // Sweet Potato Fries
        MenuItem sweetPotatoFries = new MenuItem();
        sweetPotatoFries.setItemId("SID003");
        sweetPotatoFries.setName("Sweet Potato Fries");
        sweetPotatoFries.setDescription("Crispy sweet potato fries.");
        sweetPotatoFries.setCategoryId(categoryId);
        sweetPotatoFries.setPrice(2.50);
        sweetPotatoFries.setAvailable(true);
        
        // Creamy Cabbage Slaw
        MenuItem cabbageSlaw = new MenuItem();
        cabbageSlaw.setItemId("SID004");
        cabbageSlaw.setName("Creamy Cabbage Slaw");
        cabbageSlaw.setDescription("Cabbage in a creamy dressing.");
        cabbageSlaw.setCategoryId(categoryId);
        cabbageSlaw.setPrice(2.50);
        cabbageSlaw.setAvailable(true);
        
        // Cheese Grits
        MenuItem cheeseGrits = new MenuItem();
        cheeseGrits.setItemId("SID005");
        cheeseGrits.setName("Adluh Cheese Grits");
        cheeseGrits.setDescription("Creamy grits with cheese.");
        cheeseGrits.setCategoryId(categoryId);
        cheeseGrits.setPrice(2.50);
        cheeseGrits.setAvailable(true);
        
        // Mashed Potatoes
        MenuItem mashedPotatoes = new MenuItem();
        mashedPotatoes.setItemId("SID006");
        mashedPotatoes.setName("Mashed Potatoes");
        mashedPotatoes.setDescription("Creamy mashed potatoes.");
        mashedPotatoes.setCategoryId(categoryId);
        mashedPotatoes.setPrice(2.50);
        mashedPotatoes.setAvailable(true);
        
        // Mac & Cheese
        MenuItem macCheese = new MenuItem();
        macCheese.setItemId("SID007");
        macCheese.setName("Mac & Cheese");
        macCheese.setDescription("Creamy macaroni and cheese.");
        macCheese.setCategoryId(categoryId);
        macCheese.setPrice(2.50);
        macCheese.setAvailable(true);
        
        // Seasonal Vegetables
        MenuItem seasonalVeggies = new MenuItem();
        seasonalVeggies.setItemId("SID008");
        seasonalVeggies.setName("Seasonal Vegetables");
        seasonalVeggies.setDescription("Fresh seasonal vegetable medley.");
        seasonalVeggies.setCategoryId(categoryId);
        seasonalVeggies.setPrice(2.50);
        seasonalVeggies.setAvailable(true);
        
        // Baked Beans
        MenuItem bakedBeans = new MenuItem();
        bakedBeans.setItemId("SID009");
        bakedBeans.setName("Baked Beans");
        bakedBeans.setDescription("Slow-cooked baked beans.");
        bakedBeans.setCategoryId(categoryId);
        bakedBeans.setPrice(2.50);
        bakedBeans.setAvailable(true);
        
        // Save all sides to database
        menuItemDAO.add(curlyFries);
        menuItemDAO.add(wingChips);
        menuItemDAO.add(sweetPotatoFries);
        menuItemDAO.add(cabbageSlaw);
        menuItemDAO.add(cheeseGrits);
        menuItemDAO.add(mashedPotatoes);
        menuItemDAO.add(macCheese);
        menuItemDAO.add(seasonalVeggies);
        menuItemDAO.add(bakedBeans);
        
        logger.info("Sides loaded successfully");
    }
    
    /**
     * Load beverages menu items
     */
    private void loadBeverages() throws SQLException {
        // Category ID for Beverages is 7
        int categoryId = 7;
        
        // Sweet Tea
        MenuItem sweetTea = new MenuItem();
        sweetTea.setItemId("BEV001");
        sweetTea.setName("Sweet Tea");
        sweetTea.setDescription("Southern sweet tea.");
        sweetTea.setCategoryId(categoryId);
        sweetTea.setPrice(2.00);
        sweetTea.setAvailable(true);
        
        // Unsweetened Tea
        MenuItem unsweetenedTea = new MenuItem();
        unsweetenedTea.setItemId("BEV002");
        unsweetenedTea.setName("Unsweetened Tea");
        unsweetenedTea.setDescription("Freshly brewed unsweetened tea.");
        unsweetenedTea.setCategoryId(categoryId);
        unsweetenedTea.setPrice(2.00);
        unsweetenedTea.setAvailable(true);
        
        // Coke
        MenuItem coke = new MenuItem();
        coke.setItemId("BEV003");
        coke.setName("Coke");
        coke.setDescription("Classic Coca-Cola.");
        coke.setCategoryId(categoryId);
        coke.setPrice(2.00);
        coke.setAvailable(true);
        
        // Diet Coke
        MenuItem dietCoke = new MenuItem();
        dietCoke.setItemId("BEV004");
        dietCoke.setName("Diet Coke");
        dietCoke.setDescription("Diet Coca-Cola.");
        dietCoke.setCategoryId(categoryId);
        dietCoke.setPrice(2.00);
        dietCoke.setAvailable(true);
        
        // Sprite
        MenuItem sprite = new MenuItem();
        sprite.setItemId("BEV005");
        sprite.setName("Sprite");
        sprite.setDescription("Lemon-lime flavored soda.");
        sprite.setCategoryId(categoryId);
        sprite.setPrice(2.00);
        sprite.setAvailable(true);
        
        // Bottled Water
        MenuItem water = new MenuItem();
        water.setItemId("BEV006");
        water.setName("Bottled Water");
        water.setDescription("Purified bottled water.");
        water.setCategoryId(categoryId);
        water.setPrice(2.00);
        water.setAvailable(true);
        
        // Lemonade
        MenuItem lemonade = new MenuItem();
        lemonade.setItemId("BEV007");
        lemonade.setName("Lemonade");
        lemonade.setDescription("Fresh-squeezed lemonade.");
        lemonade.setCategoryId(categoryId);
        lemonade.setPrice(2.00);
        lemonade.setAvailable(true);
        
        // Orange Juice
        MenuItem orangeJuice = new MenuItem();
        orangeJuice.setItemId("BEV008");
        orangeJuice.setName("Orange Juice");
        orangeJuice.setDescription("Fresh-squeezed orange juice.");
        orangeJuice.setCategoryId(categoryId);
        orangeJuice.setPrice(2.00);
        orangeJuice.setAvailable(true);
        
        // Save all beverages to database
        menuItemDAO.add(sweetTea);
        menuItemDAO.add(unsweetenedTea);
        menuItemDAO.add(coke);
        menuItemDAO.add(dietCoke);
        menuItemDAO.add(sprite);
        menuItemDAO.add(water);
        menuItemDAO.add(lemonade);
        menuItemDAO.add(orangeJuice);
        
        logger.info("Beverages loaded successfully");
    }
}
