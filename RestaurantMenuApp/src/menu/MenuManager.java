package menu;

import core.RestaurantManager;
import core.Restaurant;
import enums.DataType;
import tools.ConsolePrinter;
import tools.FileIO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages menu items in the restaurant list database through a CLI.
 */
public class MenuManager extends RestaurantManager {
    /**
     * Initialises the manager with a restaurant object for data storage and manipulation.
     * @param restaurant Restaurant instance from main
     */
    public MenuManager(Restaurant restaurant) {
        super(restaurant);
    }

    /**
     * Please see the method description in RestaurantManager.
     * @see RestaurantManager
     */
    @Override
    public void init() throws Exception {
        Comparator<MenuItem> comparator = Comparator.comparing(MenuItem::getName);
        getRestaurant().setDefaultComparator(DataType.ALA_CARTE_ITEM, comparator);
        getRestaurant().setDefaultComparator(DataType.PROMO_PACKAGE, comparator);
        getRestaurant().setUniqueId(DataType.PROMO_PACKAGE, 99999);

        final FileIO f = new FileIO();
        final List<String[]> alaCarteData = f.read(DataType.ALA_CARTE_ITEM).stream().map(data -> data.split(" // ")).filter(data -> data.length == 4).collect(Collectors.toList());
        final List<String[]> promoPackageData = f.read(DataType.PROMO_PACKAGE).stream().map(data -> data.split(" // ")).filter(data -> data.length == 4).collect(Collectors.toList());

        for (String[] data : alaCarteData) {
            try {
                final int id = getRestaurant().generateUniqueId(DataType.ALA_CARTE_ITEM);
                final String name = data[1];
                final BigDecimal price = new BigDecimal(data[2]);
                final String category = data[3];
                getRestaurant().load(new AlaCarteItem(id, name, price, category.toLowerCase()));
            } catch (NumberFormatException e) {
                throw (new Exception("Invalid file data detected for " + DataType.ALA_CARTE_ITEM.name() + ": " + e.getMessage()));
            }
        }

        getRestaurant().bulkSave(DataType.ALA_CARTE_ITEM);

        for (String[] data : promoPackageData) {
            final List<AlaCarteItem> itemList = new ArrayList<>();

            for (String itemData : data[3].split("--")) {
                final int itemId = Integer.parseInt(itemData);
                final AlaCarteItem item = getRestaurant().getDataFromId(DataType.ALA_CARTE_ITEM, itemId);
                itemList.add(item);
            }

            try {
                final int id = getRestaurant().generateUniqueId(DataType.PROMO_PACKAGE);
                final String name = data[1];
                final BigDecimal price = new BigDecimal(data[2]);
                getRestaurant().load(new PromotionPackage(id, name, price, itemList));
            } catch (NumberFormatException e) {
                throw (new Exception("Invalid file data detected for " + DataType.PROMO_PACKAGE.name() + ": " + e.getMessage()));
            }
        }

        getRestaurant().bulkSave(DataType.PROMO_PACKAGE);
    }

    /**
     * Please see the method description in RestaurantManager.
     * @see RestaurantManager
     */
    @Override
    public String[] getMainCLIOptions() {
        return new String[]{
                "View menu",
                "Add new ala-carte item",
                "Add new promotional package",
                "Manage ala-carte items",
                "Manage promotional packages",
        };
    }

    /**
     * Please see the method description in RestaurantManager.
     * @see RestaurantManager
     */
    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[]{
                () -> display(1),
                () -> display(2),
                () -> display(3),
                () -> display(4),
                () -> display(5),
        };
    }

    /**
     * Maps to the various methods to run. Used as some methods throw exceptions, which will be caught by this method for logging purposes.
     * @param which which method to run
     */
    private void display(int which) {
        try {
            switch (which) {
                case 1:
                    viewMenu();
                    break;

                case 2:
                    addMenuItem(DataType.ALA_CARTE_ITEM);
                    break;

                case 3:
                    addMenuItem(DataType.PROMO_PACKAGE);
                    break;

                case 4:
                    manageMenuItems(DataType.ALA_CARTE_ITEM);
                    break;

                case 5:
                    manageMenuItems(DataType.PROMO_PACKAGE);
                    break;
            }
        } catch (Exception e) {
            ConsolePrinter.logToFile(e.getMessage(), e);
        }
    }

    /**
     * Displays all items in the menu, both ala-carte and promotional packages.
     * @throws Exception Errors that occurred while displaying the menu.
     */
    private void viewMenu() throws Exception {
        final List<String> displayList = new ArrayList<>();
        final Set<String> categoryList = new TreeSet<>();
        final List<AlaCarteItem> alaCarteItemList = getRestaurant().getDataList(DataType.ALA_CARTE_ITEM);
        alaCarteItemList.forEach(item -> categoryList.add(item.getCategory()));

        for (String category : categoryList) {
            displayList.add("\\SUB" + category);
            final List<String> tempList = alaCarteItemList.stream().filter(x -> x.matchCategory(category)).map(AlaCarteItem::toDisplayString).collect(Collectors.toList());
            displayList.addAll(tempList);
        }

        final List<PromotionPackage> promoPackageList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
        final List<String> tempList = promoPackageList.stream().map(PromotionPackage::toDisplayString).collect(Collectors.toList());
        displayList.add("\\SUB" + "Promotion Packages");
        displayList.addAll(tempList);

        ConsolePrinter.clearCmd();
        ConsolePrinter.printTable("View Menu Items", "", displayList, false);
        getInputHelper().getInt("Enter 0 to go back", 0, 0);
        ConsolePrinter.clearCmd();
    }

    /**
     * Adds a menu item into the restaurant list database. Records the name of the item and calls one of two other sub-functions, 'addAlaCarteItem' and 'addPromoPackage', to complete the process.
     * @param dataType Type of item - ala-carte or promotional package.
     * @throws Exception Errors that occurred while adding the item.
     */
    private void addMenuItem(DataType dataType) throws Exception {
        ConsolePrinter.printInstructions(Collections.singletonList("Enter -back in item name to go back."));
        final String name = getInputHelper().getString("Enter item name");
        if (name.equalsIgnoreCase("-back")) {
            ConsolePrinter.clearCmd();
            return;
        }

        if (dataType.equals(DataType.ALA_CARTE_ITEM)) {
            addAlaCarteItem(name);
        } else {
            addPromoPackage(name);
        }
    }

    /**
     * Called by addMenuItem as an extension for adding ala-carte items. Records the price and category of the ala-carte item.
     * @param name name of the item recorded in addMenuItem
     * @throws Exception errors that occurred while adding the item
     */
    private void addAlaCarteItem(String name) throws Exception {
        final BigDecimal price = new BigDecimal(getInputHelper().getDouble("Enter item price"));
        final String category = getInputHelper().getString("Enter item category");
        final List<MenuItem> dataList = getRestaurant().getDataList(DataType.ALA_CARTE_ITEM);
        boolean isNameExists = dataList.stream().anyMatch(x -> x.getName().equalsIgnoreCase(name));

        if (isNameExists) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to add item as it already exists on the menu.");
            return;
        }

        final int id = getRestaurant().generateUniqueId(DataType.ALA_CARTE_ITEM);
        AlaCarteItem item = new AlaCarteItem(id, name, price, category.toLowerCase());
        getRestaurant().save(item);
        ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Item has been added successfully.");
    }

    /**
     * Called by addMenuItem as an extension for adding promotional packages. Allows users the select from the list of ala-carte items to add into the package, calculating the price automatically.
     * Formula for price calculation = 0.8 * (sum of all items' price)
     * @param name name of the item recorded in addMenuItem
     * @throws Exception errors that occurred while adding the item
     */
    private void addPromoPackage(String name) throws Exception {
        List<MenuItem> dataList = getRestaurant().getDataList(DataType.ALA_CARTE_ITEM);
        final List<String> nameList = dataList.stream().map(MenuItem::getName).collect(Collectors.toList());
        final List<String> choiceList = ConsolePrinter.formatChoiceList(nameList, Collections.singletonList("Go back"));
        ConsolePrinter.printTable("Command // Ala-Carte Items", choiceList, true);

        final List<AlaCarteItem> itemList = new ArrayList<>();
        BigDecimal price = new BigDecimal(0).setScale(2, RoundingMode.FLOOR);
        int itemIndex;
        String cont = "Y";

        do {
            if (cont.equalsIgnoreCase("Y")) {
                itemIndex = getInputHelper().getInt("Select an item to add to the package", 0, nameList.size()) - 1;
                AlaCarteItem item = getRestaurant().getDataFromIndex(DataType.ALA_CARTE_ITEM, itemIndex);
                itemList.add(item);
                price = price.add(item.getPrice());
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, item.getName() + " added to package successfully.");
            }

            cont = getInputHelper().getString("Add another item to package? [Y = YES | N = NO]");
        } while (!cont.equalsIgnoreCase("N"));

        dataList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
        boolean isNameExists = dataList.stream().anyMatch(x -> x.getName().equalsIgnoreCase(name));

        if (isNameExists) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to add item as it already exists on the menu.");
            return;
        }

        final int id = getRestaurant().generateUniqueId(DataType.PROMO_PACKAGE);
        PromotionPackage item = new PromotionPackage(id, name, price.multiply(new BigDecimal(0.8)), itemList);
        getRestaurant().save(item);
        ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Package has been added successfully.");
    }

    /**
     * Manages menu items in the restaurant list database. Calls updateItem and removeItem accordingly.
     * @param dataType dataType of the item to be modified
     * @throws Exception errors that occurred while managing item
     */
    private void manageMenuItems(DataType dataType) throws Exception {
        final List<MenuItem> dataList = getRestaurant().getDataList(dataType);
        final List<String> itemList = dataList.stream().map(MenuItem::getName).collect(Collectors.toList());
        List<String> choiceList = ConsolePrinter.formatChoiceList(itemList, null);

        ConsolePrinter.printTable("Manage Menu Items", "Command // Menu Item", choiceList, true);
        int itemIndex = getInputHelper().getInt("Select an item to manage", 0, itemList.size()) - 1;
        if (itemIndex == -1) {
            ConsolePrinter.clearCmd();
            return;
        }

        MenuItem item = getRestaurant().getDataFromIndex(dataType, itemIndex);
        final String[] actions = dataType.equals(DataType.ALA_CARTE_ITEM) ? new String[]{"Change name.", "Change price.", "Change category", "Remove item from menu."} : new String[]{"Change name.", "Remove package from menu."};
        choiceList = ConsolePrinter.formatChoiceList(Arrays.asList(actions), null);

        ConsolePrinter.printTable("Command // Action", choiceList, true);
        final int action = getInputHelper().getInt("Select an action", 0, actions.length);
        if (action == 0) {
            ConsolePrinter.clearCmd();
            return;
        }

        if ((action == 2 && dataType.equals(DataType.PROMO_PACKAGE)) || (action == 4 && dataType.equals(DataType.ALA_CARTE_ITEM))) {
            ConsolePrinter.printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));

            if (getInputHelper().getString("Confirm remove?").equalsIgnoreCase("Y")) {
                removeItem(item);
            }
        } else {
            updateItem(item, action);
        }
    }

    /**
     * Updates a menu item in the restaurant list database.
     * @param item item obtained in calling function - to be updated
     * @param action action the user chose in calling function
     * @throws Exception errors that occurred while updating item
     */
    private void updateItem(MenuItem item, int action) throws Exception {
        switch (action) {
            case 1:
            case 3:
                ConsolePrinter.printInstructions(Collections.singletonList("Enter -back to go back."));
                final String input = getInputHelper().getString("Enter the new " + ((action == 1) ? "name" : "category"));

                if (!input.equalsIgnoreCase("-back")) {
                    if (action == 1) {
                        item.setName(input);
                    } else {
                        ((AlaCarteItem) item).setCategory(input);
                    }

                    getRestaurant().save(item);
                    final List<PromotionPackage> dataList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
                    dataList.forEach(PromotionPackage::refreshPrice);
                    ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Item has been updated successfully.");
                } else {
                    ConsolePrinter.clearCmd();
                    return;
                }
                break;

            case 2:
                final BigDecimal price = new BigDecimal(getInputHelper().getDouble("Enter the new price")).setScale(2, RoundingMode.FLOOR);
                item.setPrice(price);
                getRestaurant().save(item);
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Item has been updated successfully.");
                break;
        }
    }

    /**
     * Removes a menu item from the restaurant list database.
     * @param item item obtained in calling function - to be removed
     * @throws Exception errors that occurred while removing the item
     */
    private void removeItem(MenuItem item) throws Exception {
        if (getRestaurant().getDataList(DataType.ORDER).size() > 0) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to remove item as there are active orders. Clear all orders before removing items.");
            return;
        }

        if (item instanceof AlaCarteItem) {
            final List<PromotionPackage> dataList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
            boolean isItemInPackages = dataList.stream().anyMatch(x -> x.getAlaCarteItems().stream().anyMatch(y -> y.matchId(item.getId())));

            if (isItemInPackages) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "This item is part of a promotion package. Please remove the package first.");
                return;
            }
        }

        getRestaurant().remove(item);
        ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Item has been removed successfully.");
    }
}
