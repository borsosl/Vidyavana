package hu.vidyavana.db.dao;

import hu.vidyavana.db.api.Sql;
import hu.vidyavana.db.api.StatementCallback;
import hu.vidyavana.db.model.ForgottenPassword;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("EmptyCatchBlock")
public class ForgottenPasswordDao {

    public static void insert(final ForgottenPassword ec) {
        Sql.System.wrapPreparedStatement("insert into forgotten_password " +
                "(email, password, created) " +
                "values (?,?,now())", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setString(1, ec.email);
                stmt.setString(2, ec.password);
                stmt.executeUpdate();
            }
        });
    }

    public static void delete(final String email)
    {
        Sql.System.wrapPreparedStatement("delete from forgotten_password" +
                " where email=?", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setString(1, email);
                stmt.executeUpdate();
            }
        });
    }

    public static ForgottenPassword find(final String email)
    {
        final ForgottenPassword[] ec = new ForgottenPassword[1];
        Sql.System.wrapPreparedStatement("select * from forgotten_password where email=?", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if(!rs.next())
                    return;
                ec[0] = getForgottenPassword(rs);
            }
        });
        return ec[0];
    }

    public static ForgottenPassword getForgottenPassword(ResultSet rs)
    {
        ForgottenPassword ec = new ForgottenPassword();
        try {
            ec.id = rs.getInt("id");
        } catch (SQLException ex) {}
        try {
            ec.email = rs.getString("email");
        } catch (SQLException ex) {}
        try {
            ec.password = rs.getString("password");
        } catch (SQLException ex) {}
        return ec;
    }
}
