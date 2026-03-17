import sqlite3
import os

db_path = "market_data.db"
if os.path.exists(db_path):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute("SELECT symbol, name, is_active FROM instruments")
    rows = cursor.fetchall()
    for row in rows:
        print(f"{row[0]}|{row[1]}|{row[2]}")
    conn.close()
else:
    print("Database not found")
