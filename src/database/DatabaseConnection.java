package database;

import Interface.Player;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author A7med
 */
public class DatabaseConnection {

    private final String username = "root";
    private final String password = "root";
    private final String dBName = "javaproject?verifyServerCertificate=false&useSSL=true";
    private final String url = "jdbc:mysql://localhost:3306/";

    Statement statement;

    public DatabaseConnection() throws ClassNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded");
            // Connect to a database
            Connection connection = DriverManager.getConnection(url + dBName, username, password);
            System.out.println("Database connected");
            statement = connection.createStatement();

        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //0 get All player From DataBase
    public List<Player> selectAllPlayers() {
        List<Player> players = new ArrayList<>();
        try {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM playerdata order by win DESC");
            while (resultSet.next()) {
                Player player = new Player();
                player.setUserName(resultSet.getString("username"));
                player.setWin(resultSet.getInt("win"));
                player.setGender(resultSet.getString("gender"));
                if (resultSet.getInt("status") == 0) {
                    player.setStatus(0);
                } else {
                    player.setStatus(1);
                }
                players.add(player);
            }
        } catch (SQLException ex) {
            return players;
        }
        return players;
    }

    //get All Active player From DataBase
    public List<Player> selectActivePlayers() {
        List<Player> players = new ArrayList<>();
        try {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM playerdata where status = '1' order by win DESC");
            while (resultSet.next()) {
                Player player = new Player();
                player.setUserName(resultSet.getString("username"));
                player.setWin(resultSet.getInt("win"));
                if (resultSet.getInt("status") == 0) {
                    player.setStatus(0);
                } else {
                    player.setStatus(1);
                }
                players.add(player);
            }
        } catch (SQLException ex) {
            return players;
        }
        return players;
    }

    //1
    public boolean signUpPlayer(Player player) {

        try {
            ResultSet resultSet;
            resultSet = statement.executeQuery(
                    "SELECT * FROM playerdata where username='" + player.getUserName() + "'");
            if (resultSet.next()) {
                return false;
            } else {
                statement.execute(
                        "INSERT INTO playerdata (username,password) VALUES "
                        + "('" + player.getUserName()
                        + "', '" + player.getPassword() + "')");
                return (true);
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    //2
    public Player login(String userName, String password) {
        try {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM playerdata where username='" + userName + "' and password = " + password);
            if (resultSet.next()) {
                if (setStatusActive(userName)) {
                    //System.out.println("active");
                    return getPlayer(userName);
                }
                System.out.println(userName);
            }
            return null;
        } catch (SQLException ex) {
            return null;
        }
    }

    //3 set Player Active in DataBase
    public boolean setStatusActive(String userName) {
        try {
            return !(statement.execute("UPDATE playerdata SET status = 1 WHERE (username = '" + userName + "')"));
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    //4get All player Data
    public Player getPlayer(String userName) {
        try {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM playerdata where username='" + userName + "'");
            if (resultSet.first()) {
                Player player = new Player();
                player.setUserName(resultSet.getString("username"));
                player.setWin(resultSet.getInt("win"));
                if (resultSet.getInt("status") == 0) {
                    player.setStatus(0);
                } else {
                    player.setStatus(1);
                }
                return player;
            }
        } catch (SQLException ex) {
            return null;
        }
        return null;
    }

    /*
    //5 set Player InActive in DataBase
    public boolean setStatusInactive(String userName) {
        try {
            return !(statement.execute("UPDATE playerdata SET status = 0 WHERE (username = '" + userName + "')"));
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    //6
    
    public boolean updateWin(Player player) {
        try {
            int win = player.getWin();
            win++;
            return !(statement.execute("UPDATE playerdata SET win = " + win + " WHERE (username = '" + player.getUserName() + "')"));
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean updateDraw(Player player) {
        try {
            int draw = player.getDraw();
            draw++;
            return !(statement.execute("UPDATE playerdata SET draw = " + draw + " WHERE (username = '" + player.getUserName() + "')"));
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean updateLose(Player player) {
        try {
            int lose = player.getLose();
            lose++;
            return !(statement.execute("UPDATE playerdata SET lose = " + lose + " WHERE (username = '" + player.getUserName() + "')"));
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean updateMatches(Player player) {
        try {
            int totalMatches = player.getWin();
            totalMatches++;
            return !(statement.execute("UPDATE playerdata SET totalMatches = " + totalMatches + " WHERE (username = '" + player.getUserName() + "')"));
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
     */
    // By server
    // method called by server to set the username offline
    public boolean setOffline(String userName) {
        try {
            return !(statement.execute("UPDATE playerdata SET status = '0' WHERE (username = '"
                    + userName + "')"));
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    // method called by server to delete client
    public boolean deleteClient(String userName) {
        try {
            return !(statement.execute("DELETE FROM playerdata WHERE (username = '"
                    + userName + "')"));
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    // select all funtion
    // reurns result set of all items in the database
    public ResultSet selectAll() throws SQLException {
        // statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from playerdata order by win DESC");
        return resultSet;
    }

    public boolean updateWin(String userName) {

        ResultSet resultSet;
        try {
            resultSet = statement.executeQuery("SELECT * FROM playerdata where username='"
                    + userName + "'");
            int win = 0;
            if (resultSet.first()) {
                win = resultSet.getInt("win");
                win++;
            }
            return !(statement.execute("UPDATE playerdata SET win = " + win + " WHERE (username = '"
                    + userName + "')"));

        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean updateDraw(String userName) {

        ResultSet resultSet;
        try {
            resultSet = statement.executeQuery("SELECT * FROM playerdata where username='"
                    + userName + "'");
            int draw = 0;
            if (resultSet.first()) {
                draw = resultSet.getInt("draw");
                draw++;
            }
            return !(statement.execute("UPDATE playerdata SET draw = " + draw + " WHERE (username = '"
                    + userName + "')"));

        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean updateLose(String userName) {

        ResultSet resultSet;
        try {
            resultSet = statement.executeQuery("SELECT * FROM playerdata where username='"
                    + userName + "'");
            int lose = 0;
            if (resultSet.first()) {
                lose = resultSet.getInt("lose");
                lose++;
            }
            return !(statement.execute("UPDATE playerdata SET lose = " + lose + " WHERE (username = '"
                    + userName + "')"));

        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public int getWinScore(String userName) {

        ResultSet resultSet;
        int win = 0;
        try {
            resultSet = statement.executeQuery("SELECT * FROM playerdata where username='"
                    + userName + "'");

            if (resultSet.first()) {
                win = resultSet.getInt("win");

            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return win;

    }

    public int getLoseScore(String userName) {

        ResultSet resultSet;
        int lose = 0;
        try {
            resultSet = statement.executeQuery("SELECT * FROM playerdata where username='"
                    + userName + "'");

            if (resultSet.first()) {
                lose = resultSet.getInt("lose");

            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lose;

    }

    public int getDrawScore(String userName) {

        ResultSet resultSet;
        int draw = 0;
        try {
            resultSet = statement.executeQuery("SELECT * FROM playerdata where username='"
                    + userName + "'");

            if (resultSet.first()) {
                draw = resultSet.getInt("draw");

            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return draw;

    }

    public int calculateTotalGames(String userName) {
        return getWinScore(userName) + getDrawScore(userName) + getLoseScore(userName);
    }

    public int getStatus(String userName) {
        ResultSet resultSet;
        int status = 0;
        try {
            resultSet = statement.executeQuery("SELECT * FROM playerdata where username='"
                    + userName + "'");

            if (resultSet.first()) {
                status = resultSet.getInt("status");

            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return status;

    }
}
