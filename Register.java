
/**
 *
 * @author Tom
 */
public class Register {

    Sale sale = new Sale();
    Return ret = new Return();
    DBConnect db = new DBConnect();
    String rentID = ("");

    public Register() {
    }

    Rental rental = new Rental();

    public void addItemToRental(String id, String quant) {
        System.out.println("select * from SOFTWARE_product where UPC = '" + id + "'");
        if (!db.query("select * from SOFTWARE_product where UPC = " + id + "")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Invalid UPC");
        }

        boolean match = false;

        Item item = new Item(db.getString("upc"), db.getString("ITEM_NAME"), db.getString("description"), db.getString("daily_rate"), quant);

        for (int i = 0; i < rental.getSize(); i++) {

            if ((rental.get(i).id == item.getId()) && (rental.get(i).cost == item.getCost())) {

                int newquant = rental.get(i).quantity + Integer.parseInt(quant);
                Item newItem = new Item(db.getString("upc"), db.getString("ITEM_NAME"), db.getString("description"), db.getString("cost"), String.valueOf(newquant));

                rental.add(newItem);
                rental.remove(rental.get(i));
                match = true;
            }

        }
        if (match == false) {
            rental.add(item);
        }

    }

    public void addItemToSale(String id, String quant) {
        System.out.println("select * from SOFTWARE_product where UPC = '" + id + "'");
        if (!db.query("select * from SOFTWARE_product where UPC = " + id + "")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Invalid UPC");
        }

        boolean match = false;

        Item item = new Item(db.getString("upc"), db.getString("ITEM_NAME"), db.getString("description"), db.getString("cost"), quant);

        for (int i = 0; i < sale.getSize(); i++) {

            if ((sale.get(i).id == item.getId()) && (sale.get(i).cost == item.getCost())) {

                int newquant = sale.get(i).quantity + Integer.parseInt(quant);
                Item newItem = new Item(db.getString("upc"), db.getString("ITEM_NAME"), db.getString("description"), db.getString("cost"), String.valueOf(newquant));

                sale.add(newItem);
                sale.remove(sale.get(i));
                match = true;
            }

        }
        if (match == false) {
            sale.add(item);
        }
    }

    public void removeLastItemSale() {
        sale.remove(sale.get(sale.getSize() - 1));
    }

    //removes last item in the rental grid
    public void removeLastItemRental() {
        rental.remove(rental.get(rental.getSize() - 1));
    }

    public void finishRental() {
        System.out.println("1");
        rental.calculateTotal();
        String strInput = "";
        float tendered = 0;
        String ccard = "";
        String brand = "LOL";
        System.out.println("2");
        int input;
        while ((input = isParsableLong(strInput = ((String) javax.swing.JOptionPane.showInputDialog(null, "Please enter your credit card number", "")))) < 1 || (!validateNumber(strInput))) {
            if (input == -1) /* cancel*/ {
                return;
            }
            if (input == 0) {
                javax.swing.JOptionPane.showMessageDialog(null, "Invalid credit card number");
            } else {
                javax.swing.JOptionPane.showMessageDialog(null, "Invalid credit card number");
            }
        }
        tendered = sale.getTotal();

        brand = findBrand(strInput);
        ccard = strInput;

        System.out.println("CREDITS");
        for (int i = 0; i < rental.getSize(); i++) {
            System.out.println("update software_product set stock = stock - " + rental.get(i).quantity + " where UPC = '" + rental.get(i).id + "'");
            db.update("update software_product set stock = stock - " + rental.get(i).quantity + " where UPC = " + rental.get(i).id + "");

        }
        System.out.println("3");
        Receipt r = new Receipt(sale, tendered, ccard, brand);
        r.printRentalReceipt();

        rental = new Rental();

    }

    private int isParsableFloat(String input) {
        int parsable = 1;
        float payment = -1;

        try {
            payment = Float.parseFloat(input);

            if (payment < (sale.getSubTotal() + sale.getTax())) {
                parsable = -2;
            }
        } catch (NumberFormatException e) {
            parsable = 0;
        } catch (NullPointerException e) {
            parsable = -1;
        }
        return parsable;
    }

    private int isParsableLong(String input) {
        int parsable = 1;
        if (input == null) {
            return -1;
        }

        try {
            Long.parseLong(input);
        } catch (NumberFormatException e) {
            parsable = 0;
        } catch (NullPointerException e) {
            System.out.println("kill your parents");
            parsable = -1;
        }
        return parsable;
    }

    private boolean validateNumber(String pnr) {
        // this only works if you are certain all input will be at 
        //pnr = pnr.substring(extraChars, 10 + extraChars);
        int sum = 0;
        for (int i = 0; i < pnr.length() - 1; i++) {
            char tmp = pnr.charAt(i);
            System.out.print(tmp + " ");
            int num = tmp - '0';
            int product;
            if (i % 2 != 0) {
                product = num * 1;
            } else {
                product = num * 2;
            }
            if (product > 9) {
                product -= 9;
            }
            sum += product;
            System.out.println(product + " ");
        }
        System.out.println(sum + " " + (pnr.charAt(pnr.length() - 1) - '0') + " " + (sum % 10));
        return (10 - (sum % 10) == (pnr.charAt(pnr.length() - 1) - '0'));
    }

    private String findBrand(String ccard) {
        if (ccard.charAt(0) == '4') {
            return "Visa";
        }
        if (ccard.charAt(0) == '3' && (ccard.charAt(1) == '4' && ccard.charAt(1) == '7')) {
            return "American Express";
        }
        if (ccard.charAt(0) == '5' && (ccard.charAt(1) == '1' && ccard.charAt(1) == '2' && ccard.charAt(1) == '3' && ccard.charAt(1) == '4' && ccard.charAt(1) == '5')) {
            return "Master Card";
        } else {
            return "Unknown";
        }
    }

    public void finishSale() {

        sale.calculateTotal();
        String strInput = "";
        float tendered = 0;
        String ccard = "";
        String brand = "LOL";
        //cash or credit?
        Object[] options = {"Cash", "Credit", "Cancel"};
        int n = javax.swing.JOptionPane.showOptionDialog(null, "Subtotal: $" + String.format("%.02f", sale.getSubTotal())
                + "\r\nTax: $" + String.format("%.02f", sale.getTax()) + "\r\nTotal: $" + String.format("%.02f", sale.getSubTotal() + sale.getTax()) + "\r\nChoose payment method.", "Payment Method",
                javax.swing.JOptionPane.YES_NO_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE,
                null, options, options[2]);

        switch (n) {
            case 0: {
                System.out.println("CA$H MONEY");
                int input;
                while ((input = isParsableFloat(strInput = ((String) javax.swing.JOptionPane.showInputDialog(null, "Your total: $" + String.format("%.02f", sale.getSubTotal() + sale.getTax()), "")))) < 1) {
                    if (input == -1) /* they hit cancel */ {
                        return;
                    }
                    System.out.println("WRONG");
                    if (input == 0) {
                        javax.swing.JOptionPane.showMessageDialog(null, "Invalid cash input.");
                    }
                    if (input == -2) {
                        javax.swing.JOptionPane.showMessageDialog(null, "That's not enough, einstein");
                    }
                }
                tendered = Float.parseFloat(strInput);
                break;
            } /* cash */

            case 1: {
                int input;
                while ((input = isParsableLong(strInput = ((String) javax.swing.JOptionPane.showInputDialog(null, "Please enter your credit card number", "")))) < 1 || (!validateNumber(strInput))) {
                    if (input == -1) /* cancel*/ {
                        return;
                    }
                    if (input == 0) {
                        javax.swing.JOptionPane.showMessageDialog(null, "Invalid credit card number");
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(null, "Invalid credit card number");
                    }
                }
                tendered = sale.getTotal();

                brand = findBrand(strInput);
                ccard = strInput;

                System.out.println("CREDITS");
                break;
            } /* credit */

            case 2: {
                System.out.println("what did you do??");
                return;
            } /* cancel */

        }

        for (int i = 0; i < sale.getSize(); i++) {
            System.out.println("update software_product set stock = stock - " + sale.get(i).quantity + " where UPC = '" + sale.get(i).id + "'");
            db.update("update software_product set stock = stock - " + sale.get(i).quantity + " where UPC = " + sale.get(i).id + "");

        }

        Receipt r = new Receipt(sale, tendered, ccard, brand);
        r.printReceipt();

        sale = new Sale();
    }

    public void cancelSale() {
        sale = new Sale();
    }

    public void cancelRental() {
        rental = new Rental();
    }

    public Item getSaleItem(int n) {
        return sale.get(n);
    }

    public Item getReturnItem(int n) {
        return ret.get(n);
    }

    public float getSaleSubtotal() {

        return sale.getSubTotal();
    }

    public float getReturnSubtotal() {
        return ret.getSubTotal();
    }

    public void editItemRental(String id, String quant, int i) {

        System.out.println("select * from SOFTWARE_product where UPC = '" + id + "'");
        if (!db.query("select * from SOFTWARE_product where UPC = " + id + "")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Invalid UPC");
        }

        int newquant = Integer.parseInt(quant);
        Item newItem = new Item(db.getString("upc"), db.getString("ITEM_NAME"), db.getString("description"), db.getString("cost"), String.valueOf(newquant));

        rental.add(newItem);
        rental.remove(rental.get(i));

    }

    public void editItemSale(String id, String quant, int i) {
        System.out.println("select * from SOFTWARE_product where UPC = '" + id + "'");
        if (!db.query("select * from SOFTWARE_product where UPC = " + id + "")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Invalid UPC");
        }

        int newquant = Integer.parseInt(quant);
        Item newItem = new Item(db.getString("upc"), db.getString("ITEM_NAME"), db.getString("description"), db.getString("cost"), String.valueOf(newquant));

        sale.add(newItem);
        sale.remove(sale.get(i));

    }

    public float getRentalSubtotal() {
        return rental.getSubTotal();
    }

    public Item getRentalItem(int n) {
        return rental.get(n);
    }

    public void addItemToReturn(String id, String quant) {
        if (!db.query("select * from SOFTWARE_product where UPC = " + id + "")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Invalid UPC");
        }

        boolean match = false;

        Item item = new Item(db.getString("upc"), db.getString("ITEM_NAME"), db.getString("description"), db.getString("cost"), quant);
        System.out.println(item.getName() + "   " + item.getCost());

        for (int i = 0; i < ret.getSize(); i++) {

            if ((ret.get(i).id == item.getId()) && (ret.get(i).cost == item.getCost())) {

                int newquant = ret.get(i).quantity + Integer.parseInt(quant);
                Item newItem = new Item(db.getString("upc"), db.getString("ITEM_NAME"), db.getString("description"), db.getString("cost"), String.valueOf(newquant));

                ret.add(newItem);
                ret.remove(ret.get(i));
                match = true;
            }

        }
        if (match == false) {
            ret.add(item);
        }

    }

    public void removeItemSale(int removeIt) {
        //sale.remove(sale.get(sale.getSize() - 1));
        sale.remove(sale.get(removeIt));
    }

    //removes last item in the rental grid
    public void removeItemRental(int removeIt) {
        rental.remove(rental.get(removeIt));
        //rental.remove(rental.get(rental.getSize() - 1));
    }

    public void finishReturn() {
        ReceiptReturn r = new ReceiptReturn(ret);
        ret.calculateTotal();

        for (int i = 0; i < ret.getSize(); i++) {
            System.out.println("update software_product set stock = stock + " + ret.get(i).quantity + " where UPC = '" + ret.get(i).id + "'");
            db.update("update software_product set stock = stock + " + ret.get(i).quantity + " where UPC = " + ret.get(i).id + "");

        }
        r.printReceipt();

        ret = new Return();
    }

    public void editItemReturn(String id, String quant, int i) {

        System.out.println("select * from SOFTWARE_product where UPC = '" + id + "'");
        if (!db.query("select * from SOFTWARE_product where UPC = " + id + "")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Invalid UPC");
        }

        int newquant = Integer.parseInt(quant);
        Item newItem = new Item(db.getString("upc"), db.getString("ITEM_NAME"), db.getString("description"), db.getString("cost"), String.valueOf(newquant));

        ret.add(newItem);
        ret.remove(ret.get(i));

    }

    public void removeItemReturn(int removeIt) {
        ret.remove(ret.get(removeIt));
        //ret.remove(ret.get(ret.getSize() - 1));
    }

    public void cancelReturn() {
        ret = new Return();
    }

}
