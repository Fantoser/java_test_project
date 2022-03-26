
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.json.JSONArray;
import org.json.JSONObject;

public class App {

     //VARIABLES
     int windowWidth = 600;
     int windowHeight = 500;
     static HashMap<String, String> userMap = new HashMap<String, String>();
     static HashMap<String, String> addressMap = new HashMap<String, String>();
     static HashMap<String, String> companyMap = new HashMap<String, String>();
     public static String DBname = "test_project_db";
 
     //INITIALIZATION OF GUI ELEMENTS
     JFrame frame = new JFrame("Java Test Project");
     JPanel panel = new JPanel();
     JLabel fieldLabel = new JLabel("URL");
     JLabel textLabel = new JLabel("https://jsonplaceholder.typicode.com/users");
     JButton inputBtn = new JButton("Send request");
     static JTextArea textArea = new JTextArea(8, 20);
     static JScrollPane areaScrollPane = new JScrollPane(textArea);
     static JScrollBar scrollBar = areaScrollPane.getVerticalScrollBar();

     public App() {

        //SET UP GRIDBAGLAYOUT
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 30, 5, 5);
        
        //"URL" LABEL
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.weighty = 0.1;
        gbc.weighty = 0;
        panel.add(fieldLabel);

        //TEXTLABEL
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(textLabel, gbc);
        

        gbc.insets = new Insets(5, 5, 5, 30);
        
        //INPUT BUTTON
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        inputBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writeLine("Sending request...");
                try {
                    sendRequest(textLabel.getText());
                } catch (IOException | InterruptedException e1) {
                    e1.printStackTrace();
                }
            } 
          });
        panel.add(inputBtn, gbc);
        
        gbc.insets = new Insets(5, 30, 5, 30);

        //TEXTAREA WRAPPED IN SCROLLPANE
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        textArea.setEditable(false);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(areaScrollPane, gbc);

        //SET UP FRAME
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(windowWidth, windowHeight);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

     }
     
     public static JTextArea getTextArea() {
        return textArea;
    }

    public static JScrollBar getScrollBar() {
        return scrollBar;
    }

    public static JScrollPane getAreaScrollPane() {
        return areaScrollPane;
    }

    private static void writeText(String text) {
        getTextArea().append(text);
        getAreaScrollPane().validate();
        getScrollBar().setValue(scrollBar.getMaximum());

    }

    private static void writeLine(String text) {
        getTextArea().append("\n");
        writeText(text);
    }

    private static void sendRequest(String URL) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(App::parsed);
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        

        //HANDLING RESPONE CODES
        switch (response.statusCode()) {
            case 200:
                writeLine("200 - OK");
                break;
            case 203:
                writeLine("203 - Non-Authoritative Information");
                break;
            case 204:
                writeLine("204 - No Content");
                break;
            case 400:
                writeLine("400 - Bad Request");
                break;
            case 401:
                writeLine("401 - Unauthorized");
                break;
            case 403:
                writeLine("403 - Forbidden");
                break;
            case 404:
                writeLine("404 - Not found");
                break;
            case 405:
                writeLine("405 - Method Not Allowed");
                break;
            case 406:
                writeLine("406 - Not Acceptable");
                break;
            case 407:
                writeLine("407 - Proxy Authentication Required");
                break;
            case 408:
                writeLine("408 - Request Timeout");
                break;
            case 412:
                writeLine("412 - Precondition Failed");
                break;
            case 500:
                writeLine("500 - Internal Server Error");
                break;
            case 502:
                writeLine("502 - Bad Gateway");
                break;
            case 503:
                writeLine("503 - Service Unavailable");
                break;
            case 504:
                writeLine("504 - Gateway Timeout");
                break;
            case 505:
                writeLine("505 - HTTP Version Not Supported");
                break;
        }

    }

    private static String parsed(String responseBody) {
        JSONArray users = new JSONArray(responseBody);
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);

            Iterator<String> keys = user.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                if (user.get(key) instanceof JSONObject) {
                    propertyHandler((JSONObject) user.get(key), key);
                } else {
                    if (key.equals("email")) {
                        if (emailValid(user.get(key).toString()) == false) {
                            userMap.put("email", "");
                            continue;
                        }
                    }
                   userMap.put(key, user.get(key).toString());
                }
            }
            dbInsert();
            writeLine(String.format("%s inserted into the database", user.get("name")));
        }
        return "Finished";
    }


    private static void propertyHandler(JSONObject dic, String type) {
        Iterator<String> dicKeys = dic.keys();
        while(dicKeys.hasNext()) {
            String key = dicKeys.next();
            if (type.equals("address") || type.equals("geo")) {
                if (key.equals("geo")) {
                    propertyHandler((JSONObject) dic.get(key), "geo");
                } else {
                    addressMap.put(key, dic.get(key).toString());
                }
            }
            if (type.equals("company")) {
                companyMap.put(key, dic.get(key).toString());
            }
        }
    }

    
    private static Boolean emailValid(String email) {
        
        String regex = "^(.+)@(.+)$";
 
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(email);

        return (matcher.matches()) ? (true) : (false);
    }
    
    private static void dbInsert() {

        String URL = "jdbc:postgresql://localhost:5432/" + DBname;
        String username = "postgres";
        String password = "12345";

        try {
            Connection connection = DriverManager.getConnection(URL, username, password);
            String sql_user = String.format("INSERT INTO users (id, name, username, email, phone, website, addressID, companyID) " + 
                                                "VALUES (%s, '%s','%s', '%s', '%s', '%s', %s, %s)", 
                                                Integer.parseInt(userMap.get("id")), userMap.get("name").toString(), userMap.get("username"), userMap.get("email"), userMap.get("phone"), userMap.get("website"), Integer.parseInt(userMap.get("id")), Integer.parseInt(userMap.get("id")));

            String sql_address = String.format("INSERT INTO address (id, street, suite, city, zipcode, lat, lng)" + 
                                                "VALUES ('%s', '%s', '%s', '%s', '%s', %s, %s)",
                                                Integer.parseInt(userMap.get("id")), addressMap.get("street"), addressMap.get("suite"), addressMap.get("city"), addressMap.get("zipcode"), addressMap.get("lat"), addressMap.get("lng"));
            
            String sql_company = String.format("INSERT INTO company (id, name, catchphrase, bs)" + 
                                                "VALUES ('%s', '%s', '%s', '%s')",
                                                Integer.parseInt(userMap.get("id")), companyMap.get("name"), companyMap.get("catchPhrase"), companyMap.get("bs"));

            Statement statement = connection.createStatement();

            statement.executeUpdate(sql_user);
            statement.executeUpdate(sql_address);
            statement.executeUpdate(sql_company);

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void createDatabase() throws SQLException {

        Properties prop = new Properties();

		try {
            prop.load(new FileInputStream("data.properties"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
		String user = prop.getProperty("username");
		String pass = prop.getProperty("password");
		String port = prop.getProperty("port");

        String URL = "jdbc:postgresql://localhost:" + port +"/";
        String username = user;
        String password = pass;
        

        Connection connection;

        connection = DriverManager.getConnection(URL, username, password);

        Statement stmt = connection.createStatement();
        
        writeText("Creating database...");
        try {
            stmt.execute(String.format("CREATE DATABASE %s", DBname));
            writeLine("Database created");
        } catch (SQLException e) {
            writeLine("Database already exist, proceed...");
        }

        connection.close();

        createTables();
        
    }


    private static void createTables() throws SQLException{

        Properties prop = new Properties();

		try {
            prop.load(new FileInputStream("data.properties"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
		String user = prop.getProperty("username");
		String pass = prop.getProperty("password");
		String port = prop.getProperty("port");

        String URL = "jdbc:postgresql://localhost:" + port +"/" + DBname;
        String username = user;
        String password = pass;

        Connection connection;

        connection = DriverManager.getConnection(URL, username, password);

        Statement stmt = connection.createStatement();

        writeLine("Creating tables....");

        try {
            stmt.execute("CREATE TABLE users" +
                            " (id INTEGER not NULL," +
                            " name VARCHAR(255)," + 
                            " username VARCHAR(255)," + 
                            " email VARCHAR(255)," + 
                            " phone VARCHAR(255)," + 
                            " website VARCHAR(255)," + 
                            " addressid INTEGER," + 
                            " companyid INTEGER," + 
                            " PRIMARY KEY ( id ))");

            writeLine("Table users created");
        } catch (SQLException e) {
            writeLine("Table users already exist, proceed...");
        }

        try {
            stmt.execute("CREATE TABLE address" +
                            " (id INTEGER not NULL," +
                            " street VARCHAR(255)," + 
                            " suite VARCHAR(255)," + 
                            " city VARCHAR(255)," + 
                            " zipcode VARCHAR(255)," + 
                            " lat INTEGER," + 
                            " lng INTEGER," + 
                            " PRIMARY KEY ( id ))");
                            
            writeLine("Table address created");
        } catch (SQLException e) {
            writeLine("Table address already exist, proceed...");
        }

        try {
            stmt.execute("CREATE TABLE company" +
                            " (id INTEGER not NULL," +
                            " name VARCHAR(255)," + 
                            " catchphrase VARCHAR(255)," + 
                            " bs VARCHAR(255)," + 
                            " PRIMARY KEY ( id ))");

            writeLine("Table company created");
        } catch (SQLException e) {
            writeLine("Table company already exist, proceed...");
        }

        connection.close();

        writeLine("Tables created");
    }

    
    public static void main(String[] args) throws Exception {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new App();
                try {
                    createDatabase();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
}
