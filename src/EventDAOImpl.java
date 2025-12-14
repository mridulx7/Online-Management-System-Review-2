package com.eventmgmt.dao;

import com.eventmgmt.model.Event;
import com.eventmgmt.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAOImpl implements EventDAO {

    private Connection conn;

    public EventDAOImpl() {
        conn = DBConnection.getConnection();
    }

    @Override
    public boolean insert(Event event) {
        String sql = "INSERT INTO events(organizer_id, title, description, date, time, venue, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, event.getOrganizerId());
            ps.setString(2, event.getTitle());
            ps.setString(3, event.getDescription());
            ps.setDate(4, java.sql.Date.valueOf(event.getDate()));
            ps.setTime(5, java.sql.Time.valueOf(event.getTime()));
            ps.setString(6, event.getVenue());
            ps.setString(7, event.getStatus());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Insert Event Error: " + e);
        }
        return false;
    }

    @Override
    public boolean update(Event event) {
        String sql = "UPDATE events SET organizer_id=?, title=?, description=?, date=?, time=?, venue=?, status=? WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, event.getOrganizerId());
            ps.setString(2, event.getTitle());
            ps.setString(3, event.getDescription());
            ps.setDate(4, java.sql.Date.valueOf(event.getDate()));
            ps.setTime(5, java.sql.Time.valueOf(event.getTime()));
            ps.setString(6, event.getVenue());
            ps.setString(7, event.getStatus());
            ps.setInt(8, event.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Update Event Error: " + e);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM events WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Delete Event Error: " + e);
        }
        return false;
    }

    @Override
    public Event getById(int id) {
        String sql = "SELECT * FROM events WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapEvent(rs);
            }
        } catch (Exception e) {
            System.out.println("GetById Event Error: " + e);
        }
        return null;
    }

    @Override
    public List<Event> getAll() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                list.add(mapEvent(rs));
            }
        } catch (Exception e) {
            System.out.println("GetAll Events Error: " + e);
        }
        return list;
    }

    private Event mapEvent(ResultSet rs) throws Exception {
        return new Event(
                rs.getInt("id"),
                rs.getInt("organizer_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getDate("date").toLocalDate(),
                rs.getTime("time").toLocalTime(),
                rs.getString("venue"),
                rs.getString("status")
        );
    }
}
