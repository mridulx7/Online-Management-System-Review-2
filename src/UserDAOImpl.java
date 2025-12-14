package com.eventmgmt.dao;

import com.eventmgmt.model.User;
import com.eventmgmt.model.Admin;
import com.eventmgmt.model.Organizer;
import com.eventmgmt.model.Attendee;
import com.eventmgmt.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private Connection conn;

    public UserDAOImpl() {
        conn = DBConnection.getConnection();
    }

    @Override
    public boolean insert(User user) {
        String sql = "INSERT INTO users(name, email, password, role) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Insert User Error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET name=?, email=?, password=?, role=? WHERE id=?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Update User Error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id=?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Delete User Error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public User getById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (Exception e) {
            System.out.println("GetById Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email=?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (Exception e) {
            System.out.println("GetUserByEmail Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (Exception e) {
            System.out.println("GetAll Error: " + e.getMessage());
        }

        return list;
    }

    private User mapUser(ResultSet rs) throws Exception {
        String role = rs.getString("role");

        User user;
        switch (role.toUpperCase()) {
            case "ADMIN":
                user = new Admin();
                break;
            case "ORGANIZER":
                user = new Organizer();
                break;
            default:
                user = new Attendee();
        }

        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(role);

        return user;
    }
}
