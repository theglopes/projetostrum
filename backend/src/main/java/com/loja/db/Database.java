package com.loja.db;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

/**
 * Simplified SQLite helper to provision the demo database.
 */
public final class Database {
    private static final String DB_FILENAME = "loja.db";
    private static final Path DB_PATH;
    private static final String JDBC_URL;

    static {
        DB_PATH = resolveDatabasePath();
        JDBC_URL = "jdbc:sqlite:" + DB_PATH.toString();
        ensureDatabaseDirectory();
        bootstrapSchema();
    }

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    private static Path resolveDatabasePath() {
        String baseDir = System.getProperty("db.dir");
        if (baseDir != null && !baseDir.isBlank()) {
            return Paths.get(baseDir, DB_FILENAME).toAbsolutePath();
        }
        Path cwd = Paths.get("").toAbsolutePath();
        Path defaultDir = cwd.resolve("data");
        return defaultDir.resolve(DB_FILENAME);
    }

    private static void ensureDatabaseDirectory() {
        File dir = DB_PATH.getParent().toFile();
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
    }

    private static void bootstrapSchema() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'USER',
                    plan TEXT NOT NULL DEFAULT 'FREE',
                    created_at TEXT NOT NULL
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS games (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    price REAL NOT NULL,
                    image TEXT,
                    promo INTEGER NOT NULL DEFAULT 0
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS sessions (
                    token TEXT PRIMARY KEY,
                    user_id INTEGER NOT NULL,
                    expires_at INTEGER NOT NULL,
                    created_at INTEGER NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    total REAL NOT NULL,
                    created_at INTEGER NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL,
                    game_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    price REAL NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                    FOREIGN KEY (game_id) REFERENCES games(id)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS developer_requests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL UNIQUE,
                    studio_name TEXT NOT NULL,
                    cnpj TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    resolved_by INTEGER,
                    resolved_at INTEGER,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS game_submissions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    price REAL NOT NULL,
                    image TEXT,
                    promo INTEGER NOT NULL DEFAULT 0,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    approved_game_id INTEGER,
                    resolved_by INTEGER,
                    resolved_at INTEGER,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL,
                    FOREIGN KEY (approved_game_id) REFERENCES games(id) ON DELETE SET NULL
                )
            """);

            ensureUsersSchemaUpgrades(conn);
            ensureGameSubmissionSchemaUpgrades(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar banco de dados", e);
        }
    }

    public static void ensureSeededGames() {
        String[] games = {
            "Grand Theft Auto 6|199.99|css/Imagens/grand-theft-auto-6.png|1",
            "Metal Gear Solid V: The Phantom Pain|49.90|css/Imagens/metal-gear-solid-v-the-phantom-pain.jpg|0",
            "Alan Wake 2|179.90|css/Imagens/alan-wake-2.jpeg|0",
            "Assassin's Creed Mirage|149.90|css/Imagens/assassins-creed-mirage.png|1",
            "Dark Souls Remastered|59.90|css/Imagens/dark-souls-remastered.png|0",
            "Cyberpunk 2077|99.90|css/Imagens/cyberpunk-2077.jpg|1",
            "Dying Light 2 Stay Human|89.90|css/Imagens/dying-light-2-stay-human.jpeg|0",
            "Detroit Become Human|59.90|css/Imagens/detroit-become-human.jpg|0",
            "God of War|119.90|css/Imagens/god-of-war.jpeg|1",
            "Elden Ring|199.90|css/Imagens/elden-ring.png|1",
            "Hogwarts Legacy|229.90|css/Imagens/hogwarts-legacy.png|0",
            "Hades|49.90|css/Imagens/hades.jpg|0",
            "Grand Theft Auto San Andreas|39.90|css/Imagens/grand-theft-auto-san-andreas.jpg|1",
            "Hollow Knight|29.90|css/Imagens/hollow-knight.jpg|0",
            "Resident Evil 4|159.90|css/Imagens/resident-evil-4.png|1",
            "Red Dead Redemption 2|129.90|css/Imagens/red-dead-redemption-2.jpg|0",
            "The Last of Us Part 1|249.90|css/Imagens/the-last-of-us-part-1.png|1",
            "The Witcher 3|39.90|css/Imagens/the-witcher-3.jpg|1"
        };

        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            var rs = st.executeQuery("SELECT COUNT(*) FROM games");
            long count = rs.next() ? rs.getLong(1) : 0;
            if (count > 0) {
                return;
            }

            conn.setAutoCommit(false);
            try (var insert = conn.prepareStatement("""
                INSERT INTO games(name, price, image, promo)
                VALUES (?, ?, ?, ?)
            """)) {
                for (String def : games) {
                    String[] parts = def.split("\\|");
                    insert.setString(1, parts[0]);
                    insert.setDouble(2, Double.parseDouble(parts[1]));
                    insert.setString(3, parts[2]);
                    insert.setInt(4, Integer.parseInt(parts[3]));
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            conn.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Falha ao povoar jogos iniciais", ex);
        }
    }

    public static long unixNow() {
        return Instant.now().getEpochSecond();
    }

    private static void ensureUsersSchemaUpgrades(Connection conn) throws SQLException {
        if (!hasColumn(conn, "users", "role")) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'USER'");
            }
        }
        if (!hasColumn(conn, "users", "plan")) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE users ADD COLUMN plan TEXT NOT NULL DEFAULT 'FREE'");
            }
        }
    }

    private static void ensureGameSubmissionSchemaUpgrades(Connection conn) throws SQLException {
        if (!hasColumn(conn, "game_submissions", "approved_game_id")) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE game_submissions ADD COLUMN approved_game_id INTEGER");
            }
        }
        if (!hasColumn(conn, "game_submissions", "resolved_by")) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE game_submissions ADD COLUMN resolved_by INTEGER");
            }
        }
        if (!hasColumn(conn, "game_submissions", "resolved_at")) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE game_submissions ADD COLUMN resolved_at INTEGER");
            }
        }
    }

    private static boolean hasColumn(Connection conn, String table, String column) throws SQLException {
        String pragma = "PRAGMA table_info(" + table + ")";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(pragma)) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
