package com.eventmgmt.dao;

import com.eventmgmt.model.Ticket;
import com.eventmgmt.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDAOImpl implements TicketDAO {

    private Connection conn;

    public TicketDAOImpl() {
        conn = DBConnection.getConnection();
    }

    @Override
    public boolean insert(Ticket t) {
        String sql = "INSERT INTO tickets(event_id, price, quantity) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, t.getEventId());
            ps.setDouble(2, t.getPrice());
            ps.setInt(3, t.getQuantity());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Insert Ticket Error: " + e);
        }
        return false;
    }

    @Override
    public boolean update(Ticket t) {
        String sql = "UPDATE tickets SET event_id=?, price=?, quantity=? WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, t.getEventId());
            ps.setDouble(2, t.getPrice());
            ps.setInt(3, t.getQuantity());
            ps.setInt(4, t.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Update Ticket Error: " + e);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM tickets WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Delete Ticket Error: " + e);
        }
        return false;
    }

    @Override
    public Ticket getById(int id) {
        String sql = "SELECT * FROM tickets WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapTicket(rs);
            }

        } catch (Exception e) {
            System.out.println("GetById Ticket Error: " + e);
        }
        return null;
    }

    @Override
    public List<Ticket> getAll() {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM tickets";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                list.add(mapTicket(rs));
            }

        } catch (Exception e) {
            System.out.println("GetAll Tickets Error: " + e);
        }
        return list;
    }

    private Ticket mapTicket(ResultSet rs) throws Exception {
        return new Ticket(
                rs.getInt("id"),
                rs.getInt("event_id"),
                rs.getDouble("price"),
                rs.getInt("quantity")
        );
    }
}
