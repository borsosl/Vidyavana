package hu.vidyavana.db.dao;

import hu.vidyavana.db.api.Sql;
import hu.vidyavana.db.api.StatementCallback;
import hu.vidyavana.db.model.EmailChange;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("EmptyCatchBlock")
public class EmailChangeDao {

    public static void insert(final EmailChange ec) {
        Sql.System.wrapPreparedStatement("insert into email_change " +
                "(user_id, old_email, new_email, token, created) " +
                "values (?,?,?,?,now())", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setInt(1, ec.userId);
                stmt.setString(2, ec.oldEmail);
                stmt.setString(3, ec.newEmail);
                stmt.setString(4, ec.token);
                stmt.executeUpdate();
            }
        });
    }

    public static void delete(final int userId)
    {
        Sql.System.wrapPreparedStatement("delete from email_change" +
                " where user_id=?", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }
        });
    }

    public static EmailChange findByToken(final String token)
    {
        final EmailChange[] ec = new EmailChange[1];
        Sql.System.wrapPreparedStatement("select * from email_change where token=?", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setString(1, token);
                ResultSet rs = stmt.executeQuery();
                if(!rs.next())
                    return;
                ec[0] = getEmailChange(rs);
            }
        });
        return ec[0];
    }

    public static EmailChange getEmailChange(ResultSet rs)
    {
        EmailChange ec = new EmailChange();
        try {
            ec.id = rs.getInt("id");
        } catch (SQLException ex) {}
        try {
            ec.userId = rs.getInt("user_id");
        } catch (SQLException ex) {}
        try {
            ec.oldEmail = rs.getString("old_email");
        } catch (SQLException ex) {}
        try {
            ec.newEmail = rs.getString("new_email");
        } catch (SQLException ex) {}
        try {
            ec.token = rs.getString("token");
        } catch (SQLException ex) {}
        return ec;
    }
}
