package com.eventmgmt.dao;

import com.eventmgmt.model.Registration;
import com.eventmgmt.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAOImpl implements RegistrationDAO {

    private Connection conn;

    public RegistrationDAOImpl() {
        conn = DBConnection.getConnection();
    }

    @Override
    public boolean insert(Registration registration) {
        String sql = "INSERT INTO registrations(user_id, event_id, ticket_id, status) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, registration.getUserId());
            ps.setInt(2, registration.getEventId());
            ps.setInt(3, registration.getTicketId());
            ps.setString(4, registration.getStatus());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Insert Registration Error: " + e);
        }
        return false;
    }

    @Override
    public boolean update(Registration registration) {
        String sql = "UPDATE registrations SET user_id=?, event_id=?, ticket_id=?, status=? WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, registration.getUserId());
            ps.setInt(2, registration.getEventId());
            ps.setInt(3, registration.getTicketId());
            ps.setString(4, registration.getStatus());
            ps.setInt(5, registration.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Update Registration Error: " + e);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM registrations WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Delete Registration Error: " + e);
        }
        return false;
    }

    @Override
    public Registration getById(int id) {
        String sql = "SELECT * FROM registrations WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRegistration(rs);
            }

        } catch (Exception e) {
            System.out.println("GetById Registration Error: " + e);
        }
        return null;
    }

    @Override
    public List<Registration> getAll() {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT * FROM registrations";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                list.add(mapRegistration(rs));
            }

        } catch (Exception e) {
            System.out.println("GetAll Registrations Error: " + e);
        }
        return list;
    }

    @Override
    public List<Registration> getByUserId(int userId) {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT * FROM registrations WHERE user_id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRegistration(rs));
            }

        } catch (Exception e) {
            System.out.println("GetByUserId Registrations Error: " + e);
        }
        return list;
    }

    @Override
    public List<Registration> getByEventId(int eventId) {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT * FROM registrations WHERE event_id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRegistration(rs));
            }

        } catch (Exception e) {
            System.out.println("GetByEventId Registrations Error: " + e);
        }
        return list;
    }

    @Override
    public Registration getByUserAndEvent(int userId, int eventId) {
        String sql = "SELECT * FROM registrations WHERE user_id=? AND event_id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRegistration(rs);
            }

        } catch (Exception e) {
            System.out.println("GetByUserAndEvent Registration Error: " + e);
        }
        return null;
    }

    private Registration mapRegistration(ResultSet rs) throws Exception {
        return new Registration(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("event_id"),
                rs.getInt("ticket_id"),
                rs.getString("status")
        );
    }
}
