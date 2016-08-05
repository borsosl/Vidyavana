package hu.vidyavana.db.dao;

import hu.vidyavana.db.api.Sql;
import hu.vidyavana.db.api.StatementCallback;
import hu.vidyavana.db.model.Bookmark;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("EmptyCatchBlock")
public class BookmarkDao {


    public static Bookmark findById(final int id)
    {
        final Bookmark[] bookmark = new Bookmark[1];
        Sql.System.wrapPreparedStatement("select * from bookmark where id=?", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if(!rs.next())
                    return;
                bookmark[0] = getBookmark(rs);
            }
        });
        return bookmark[0];
    }

    public static int count(int userId)
    {
        int[] res = new int[1];
        String sql = "select count(*) from bookmark where user_id=?";
        Sql.System.wrapPreparedStatement(sql, new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                res[0] = rs.getInt(1);
            }
        });
        return res[0];
    }

    public static List<Bookmark> getRecent10(int userId)
    {
        return getQueryResults("select * from bookmark" +
                " where user_id=?" +
                " order by last_used desc limit 10",
                stmt -> {
                    try {
                        stmt.setInt(1, userId);
                    } catch (SQLException e) {}
                });
    }

    public static List<Bookmark> getFiltered100(int userId, String filter)
    {
        String inner = "select * from bookmark" +
                " where user_id=? and lower(name) like ?" +
                " order by last_used desc limit 100";
        String sql = "select * from (" + inner + ") order by lower(name)";
        return getQueryResults(sql, stmt -> {
                    try {
                        stmt.setInt(1, userId);
                        stmt.setString(2, '%' + filter.toLowerCase() + '%');
                    } catch (SQLException e) {}
                });
    }

    public static int countFiltered(int userId, String filter)
    {
        int[] res = new int[1];
        String sql = "select count(*) from bookmark" +
                " where user_id=? and name like ?";
        Sql.System.wrapPreparedStatement(sql, new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setInt(1, userId);
                stmt.setString(2, '%' + filter.toLowerCase() + '%');
                ResultSet rs = stmt.executeQuery();
                rs.next();
                res[0] = rs.getInt(1);
            }
        });
        return res[0];
    }

    private static List<Bookmark> getQueryResults(String sql, Consumer<PreparedStatement> prepare) {
        List<Bookmark> bookmarks = new ArrayList<>();
        Sql.System.wrapPreparedStatement(sql, new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                prepare.accept(stmt);
                ResultSet rs = stmt.executeQuery();
                while(rs.next())
                {
                    Bookmark bookmark = getBookmark(rs);
                    bookmarks.add(bookmark);
                }
            }
        });
        return bookmarks;
    }

    public static void insert(final Bookmark bookmark) {
        Sql.System.wrapPreparedStatement("insert into bookmark " +
                "(user_id, name, follow, book_segment, ordinal," +
                " short_ref, last_used) " +
                "values (?,?,?,?,?,?,now())", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setInt(1, bookmark.userId);
                stmt.setString(2, bookmark.name);
                stmt.setBoolean(3, bookmark.follow);
                stmt.setInt(4, bookmark.bookSegmentId);
                stmt.setInt(5, bookmark.ordinal);
                stmt.setString(6, bookmark.shortRef);
                stmt.executeUpdate();
            }
        });
    }

    public static void update(final Bookmark bookmark)
    {
        Sql.System.wrapPreparedStatement("update bookmark " +
                "set name=?, follow=?, book_segment=?, ordinal=?, " +
                "short_ref=?, last_used=now() " +
                "where id=? and user_id=?", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setString(1, bookmark.name);
                stmt.setBoolean(2, bookmark.follow);
                stmt.setInt(3, bookmark.bookSegmentId);
                stmt.setInt(4, bookmark.ordinal);
                stmt.setString(5, bookmark.shortRef);
                stmt.setInt(6, bookmark.id);
                stmt.setInt(7, bookmark.userId);
                stmt.executeUpdate();
            }
        });
    }

    public static void updateLastUsed(final int id)
    {
        Sql.System.wrapPreparedStatement("update bookmark" +
                " set last_used=now()" +
                " where id=?", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
        });
    }

    public static void delete(final int id, final int userId)
    {
        Sql.System.wrapPreparedStatement("delete from bookmark" +
                " where id=? and user_id=?", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setInt(1, id);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
        });
    }

    public static Bookmark getBookmark(ResultSet rs)
    {
        Bookmark bookmark = new Bookmark();
        try {
            bookmark.id = rs.getInt("id");
        } catch (SQLException ex) {}
        try {
            bookmark.userId = rs.getInt("user_id");
        } catch (SQLException ex) {}
        try {
            bookmark.name = rs.getString("name");
        } catch (SQLException ex) {}
        try {
            bookmark.follow = rs.getBoolean("follow");
        } catch (SQLException ex) {}
        try {
            bookmark.bookSegmentId = rs.getInt("book_segment");
        } catch (SQLException ex) {}
        try {
            bookmark.ordinal = rs.getInt("ordinal");
        } catch (SQLException ex) {}
        try {
            bookmark.shortRef = rs.getString("short_ref");
        } catch (SQLException ex) {}
        try {
            bookmark.lastUsed = rs.getTimestamp("last_used").getTime();
        } catch (SQLException ex) {}
        return bookmark;
    }

    public static void updateFollowed(int id, int userId, int bookSegmentId, int ordinal, String shortRef) {
        Sql.System.wrapPreparedStatement("update bookmark " +
                "set book_segment=?, ordinal=?, short_ref=?, last_used=now() " +
                "where id=? and user_id=?", new StatementCallback()
        {
            @Override
            public void usePreparedStatement(PreparedStatement stmt) throws SQLException
            {
                stmt.setInt(1, bookSegmentId);
                stmt.setInt(2, ordinal);
                stmt.setString(3, shortRef);
                stmt.setInt(4, id);
                stmt.setInt(5, userId);
                stmt.executeUpdate();
            }
        });
    }
}
