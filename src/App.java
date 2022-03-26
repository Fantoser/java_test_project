
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
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
 
     //INITIALIZATION OF GUI ELEMENTS
     JFrame frame = new JFrame("Java Test Project");
     JPanel panel = new JPanel();
     JLabel fieldLabel = new JLabel("URL");
     JTextField textField = new JTextField("https://jsonplaceholder.typicode.com/users");
     JButton inputBtn = new JButton("Bevitel");
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

        //INPUT FIELD
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(textField, gbc);
        

        gbc.insets = new Insets(5, 5, 5, 30);
        
        //INPUT BUTTON
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        inputBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writeText("Sending request...");
                try {
                    sendRequest(textField.getText());
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
                    //.join();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
        

        //HANDLING RESPONE CODES

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

        String jdbcURL = "jdbc:postgresql://localhost:5432/test";
        String username = "postgres";
        String password = "12345";

        try {
            Connection connection = DriverManager.getConnection(jdbcURL, username, password);
            String sql_user = String.format("INSERT INTO users (id, name, username, email, phone, website, addressID, companyID) " + 
                                                "VALUES (%s, '%s','%s', '%s', '%s', '%s', %s, %s)", 
                Integer.parseInt(userMap.get("id")), userMap.get("name").toString(), userMap.get("username"), userMap.get("email"), userMap.get("phone"), userMap.get("website"), Integer.parseInt(userMap.get("id")), Integer.parseInt(userMap.get("id")));

            String sql_address = String.format("INSERT INTO address (street, suite, city, zipcode, lat, lng)" + 
                                                "VALUES ('%s', '%s', '%s', '%s', %s, %s)",
                addressMap.get("street"), addressMap.get("suite"), addressMap.get("city"), addressMap.get("zipcode"), addressMap.get("lat"), addressMap.get("lng"));
            
            String sql_company = String.format("INSERT INTO company (name, catchphrase, bs)" + 
                                                "VALUES ('%s', '%s', '%s')", 
                companyMap.get("name"), companyMap.get("catchPhrase"), companyMap.get("bs"));

            Statement statement = connection.createStatement();

            statement.executeUpdate(sql_user);
            statement.executeUpdate(sql_address);
            statement.executeUpdate(sql_company);

            connection.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    public static void main(String[] args) throws Exception {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new App();
                try {
                    sendRequest("https://jsonplaceholder.typicode.com/users");
                } catch (IOException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
}
