// File: UltraAdvancedHotelSystem.java
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.sql.*;

// Database Helper Skeleton
class DBHelper {
    static Connection getConnection() {
        try {
            // Replace with your DB config
            String url = "jdbc:mysql://localhost:3306/shrms";
            String user = "root";
            String pass = "";
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"Database Connection Failed!");
            return null;
        }
    }
}

// Room Model
class Room {
    int number;
    String type;
    double price;
    boolean isBooked;
    Customer customer;

    Room(int number, String type, double price) {
        this.number = number;
        this.type = type;
        this.price = price;
        this.isBooked = false;
        this.customer = null;
    }
}

// Customer Model
class Customer {
    String name, cnic;
    int days;
    ArrayList<String> breakfast, lunch, dinner;

    Customer(String name, String cnic, int days,
             ArrayList<String> breakfast, ArrayList<String> lunch, ArrayList<String> dinner) {
        this.name = name;
        this.cnic = cnic;
        this.days = days;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
    }

    double calculateMealCost() {
        Map<String,Integer> prices = new HashMap<>();
        prices.put("Egg",50); prices.put("Paratha",30); prices.put("Channay",40);
        prices.put("Tea",20); prices.put("Mineral Water",30); prices.put("Biryani",150);
        prices.put("Karahi",200); prices.put("Mutton Karahi",300); prices.put("Beef Karahi",250);
        prices.put("Salad",50);

        double total = 0;
        for(String item: breakfast) total += prices.get(item)*days;
        for(String item: lunch) total += prices.get(item)*days;
        for(String item: dinner) total += prices.get(item)*days;
        return total;
    }
}

// Hotel Model
class Hotel {
    ArrayList<Room> rooms = new ArrayList<>();

    Hotel() {
        for(int i=1;i<=20;i++) rooms.add(new Room(100+i,"Suite",5000));
        for(int i=1;i<=20;i++) rooms.add(new Room(200+i,"Single",2000));
        for(int i=1;i<=20;i++) rooms.add(new Room(300+i,"Double",3500));
    }

    Room getRoom(int number) {
        for(Room r: rooms) if(r.number==number) return r;
        return null;
    }
}

// GUI
public class UltraAdvancedHotelSystem extends JFrame {
    Hotel hotel;
    JTextField nameField, cnicField, daysField, roomField;
    JCheckBox eggBox, parathaBox, channayBox, teaBox, waterBox;
    JCheckBox biryaniBox, karahiBox, saladBox, muttonBox, beefBox;
    JTextArea billArea;
    DefaultTableModel tableModel;
    JTable roomTable;

    public UltraAdvancedHotelSystem() {
        hotel = new Hotel();
        setTitle("Ultra Advanced Hotel System");
        setSize(1100,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5,5));

        // Left Panel - Form
        JPanel formPanel = new JPanel(new GridLayout(18,2,5,5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Booking Details"));
        nameField = new JTextField(); cnicField = new JTextField(); daysField = new JTextField(); roomField = new JTextField();

        eggBox=new JCheckBox("Egg"); parathaBox=new JCheckBox("Paratha"); channayBox=new JCheckBox("Channay");
        teaBox=new JCheckBox("Tea"); waterBox=new JCheckBox("Mineral Water");
        biryaniBox=new JCheckBox("Biryani"); karahiBox=new JCheckBox("Karahi"); saladBox=new JCheckBox("Salad");
        muttonBox=new JCheckBox("Mutton Karahi"); beefBox=new JCheckBox("Beef Karahi");

        JButton bookBtn = new JButton("Book Room"), cancelBtn = new JButton("Cancel Booking");
        JButton clearBtn = new JButton("Clear Form"), showBtn = new JButton("Show Available Rooms");

        formPanel.add(new JLabel("Customer Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("CNIC:")); formPanel.add(cnicField);
        formPanel.add(new JLabel("Days of Stay:")); formPanel.add(daysField);
        formPanel.add(new JLabel("Room Number:")); formPanel.add(roomField);

        formPanel.add(new JLabel("Breakfast Items:")); formPanel.add(new JLabel(""));
        formPanel.add(eggBox); formPanel.add(parathaBox);
        formPanel.add(channayBox); formPanel.add(teaBox);
        formPanel.add(waterBox); formPanel.add(new JLabel(""));

        formPanel.add(new JLabel("Lunch/Dinner Items:")); formPanel.add(new JLabel(""));
        formPanel.add(biryaniBox); formPanel.add(karahiBox);
        formPanel.add(muttonBox); formPanel.add(beefBox);
        formPanel.add(saladBox); formPanel.add(new JLabel(""));

        formPanel.add(bookBtn); formPanel.add(cancelBtn);
        formPanel.add(clearBtn); formPanel.add(showBtn);

        add(formPanel,BorderLayout.WEST);

        // Center Panel - Rooms Table
        tableModel = new DefaultTableModel(new String[]{"Room Number","Type","Price","Status"},0){
            public boolean isCellEditable(int r,int c){ return false;}
        };
        roomTable = new JTable(tableModel){
            public Component prepareRenderer(TableCellRenderer r,int row,int col){
                Component c = super.prepareRenderer(r,row,col);
                String status = (String)getModel().getValueAt(row,3);
                if(status.startsWith("Booked")) c.setBackground(Color.RED);
                else c.setBackground(Color.GREEN);
                c.setForeground(Color.BLACK);
                return c;
            }
        };
        JScrollPane tableScroll = new JScrollPane(roomTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Rooms Status"));
        add(tableScroll,BorderLayout.CENTER);
        updateRoomTable();

        // Right Panel - Bill
        billArea = new JTextArea(); billArea.setEditable(false);
        JScrollPane billScroll = new JScrollPane(billArea);
        billScroll.setBorder(BorderFactory.createTitledBorder("Bill"));
        billScroll.setPreferredSize(new Dimension(350,0));
        add(billScroll,BorderLayout.EAST);

        // Button Actions
        bookBtn.addActionListener(e->bookRoomAction());
        cancelBtn.addActionListener(e->cancelBookingAction());
        clearBtn.addActionListener(e->clearForm());
        showBtn.addActionListener(e->updateRoomTable());

        setVisible(true);
    }

    void updateRoomTable(){
        tableModel.setRowCount(0);
        for(Room r: hotel.rooms){
            String status = r.isBooked?"Booked by "+r.customer.name:"Available";
            tableModel.addRow(new Object[]{r.number,r.type,r.price,status});
        }
    }

    void bookRoomAction(){
        try{
            String name=nameField.getText();
            String cnic=cnicField.getText();
            int days=Integer.parseInt(daysField.getText());
            int roomNum=Integer.parseInt(roomField.getText());

            ArrayList<String> breakfast = new ArrayList<>();
            if(eggBox.isSelected()) breakfast.add("Egg");
            if(parathaBox.isSelected()) breakfast.add("Paratha");
            if(channayBox.isSelected()) breakfast.add("Channay");
            if(teaBox.isSelected()) breakfast.add("Tea");
            if(waterBox.isSelected()) breakfast.add("Mineral Water");

            ArrayList<String> lunch = new ArrayList<>();
            if(biryaniBox.isSelected()) lunch.add("Biryani");
            if(karahiBox.isSelected()) lunch.add("Karahi");
            if(muttonBox.isSelected()) lunch.add("Mutton Karahi");
            if(beefBox.isSelected()) lunch.add("Beef Karahi");
            if(saladBox.isSelected()) lunch.add("Salad");

            Room room = hotel.getRoom(roomNum);
            if(room!=null && !room.isBooked){
                Customer c = new Customer(name,cnic,days,breakfast,lunch,lunch);
                room.customer = c; room.isBooked=true;
                generateBill(room,c);
                updateRoomTable();
            } else JOptionPane.showMessageDialog(this,"Room not available or invalid number!");
        }catch(Exception ex){
            JOptionPane.showMessageDialog(this,"Invalid input! Fill all fields correctly.");
        }
    }

    void cancelBookingAction(){
        try{
            int roomNum = Integer.parseInt(roomField.getText());
            Room room = hotel.getRoom(roomNum);
            if(room!=null && room.isBooked){
                room.isBooked=false; room.customer=null;
                billArea.setText("");
                updateRoomTable();
                JOptionPane.showMessageDialog(this,"Booking cancelled successfully.");
            } else JOptionPane.showMessageDialog(this,"Room not booked or invalid number!");
        }catch(Exception ex){ JOptionPane.showMessageDialog(this,"Enter valid room number!"); }
    }

    void generateBill(Room r, Customer c){
        double roomCost = r.price*c.days;
        double mealCost = c.calculateMealCost();
        double total = roomCost+mealCost;

        StringBuilder sb = new StringBuilder();
        sb.append("----- BILL -----\nCustomer: ").append(c.name).append("\nCNIC: ").append(c.cnic)
          .append("\nRoom Number: ").append(r.number).append("\nRoom Type: ").append(r.type)
          .append("\nDays: ").append(c.days).append("\nRoom Cost: PKR ").append(roomCost)
          .append("\nBreakfast: ").append(c.breakfast).append("\nLunch: ").append(c.lunch)
          .append("\nDinner: ").append(c.dinner).append("\nMeal Cost: PKR ").append(mealCost)
          .append("\nTotal: PKR ").append(total).append("\n----------------\n");
        billArea.setText(sb.toString());
    }

    void clearForm(){
        nameField.setText(""); cnicField.setText(""); daysField.setText(""); roomField.setText("");
        eggBox.setSelected(false); parathaBox.setSelected(false); channayBox.setSelected(false);
        teaBox.setSelected(false); waterBox.setSelected(false);
        biryaniBox.setSelected(false); karahiBox.setSelected(false); saladBox.setSelected(false);
        muttonBox.setSelected(false); beefBox.setSelected(false);
        billArea.setText("");
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(()->new UltraAdvancedHotelSystem());
    }
}
