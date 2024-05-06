import tkinter as tk
from tkinter import ttk
import sqlite3

class PasswordManagerGUI:
    def _init_(self):
        self.window = tk.Tk()
        self.window.title("Password Manager")
        self.window.geometry("800x500")

        self.master_password = "admin123"
        self.database = "passwordmanager.db"

        self.create_widgets()
        self.connect_to_database()

    def create_widgets(self):
        self.notebook = ttk.Notebook(self.window)
        self.notebook.pack(fill="both", expand=True)

        self.tab1 = ttk.Frame(self.notebook)
        self.tab2 = ttk.Frame(self.notebook)
        self.tab3 = ttk.Frame(self.notebook)

        self.notebook.add(self.tab1, text="Add Password")
        self.notebook.add(self.tab2, text="Display Passwords")
        self.notebook.add(self.tab3, text="Remove Password")

        # Tab 1: Add Password
        self.website_label = tk.Label(self.tab1, text="Website:")
        self.website_label.grid(row=0, column=0)
        self.website_entry = tk.Entry(self.tab1)
        self.website_entry.grid(row=0, column=1)

        self.username_label = tk.Label(self.tab1, text="Username:")
        self.username_label.grid(row=1, column=0)
        self.username_entry = tk.Entry(self.tab1)
        self.username_entry.grid(row=1, column=1)

        self.password_label = tk.Label(self.tab1, text="Password:")
        self.password_label.grid(row=2, column=0)
        self.password_entry = tk.Entry(self.tab1, show="*")
        self.password_entry.grid(row=2, column=1)

        self.add_button = tk.Button(self.tab1, text="Add Password", command=self.add_password)
        self.add_button.grid(row=3, column=0, columnspan=2)

        # Tab 2: Display Passwords
        self.tree = ttk.Treeview(self.tab2, columns=("ID", "Website", "Username", "Password"))
        self.tree.heading("#1", text="ID")
        self.tree.heading("#2", text="Website")
        self.tree.heading("#3", text="Username")
        self.tree.heading("#4", text="Password")
        self.tree.pack()

        self.display_button = tk.Button(self.tab2, text="Display Passwords", command=self.display_passwords)
        self.display_button.pack()

        # Tab 3: Remove Password
        self.remove_label = tk.Label(self.tab3, text="Select a password to remove:")
        self.remove_label.pack()

        self.remove_tree = ttk.Treeview(self.tab3, columns=("ID", "Website", "Username", "Password"))
        self.remove_tree.heading("#1", text="ID")
        self.remove_tree.heading("#2", text="Website")
        self.remove_tree.heading("#3", text="Username")
        self.remove_tree.heading("#4", text="Password")
        self.remove_tree.pack()

        self.remove_button = tk.Button(self.tab3, text="Remove Password", command=self.remove_password)
        self.remove_button.pack()

        self.notebook.select(self.tab1)

    def connect_to_database(self):
        self.conn = sqlite3.connect(self.database)
        self.cursor = self.conn.cursor()
        self.cursor.execute(
            "CREATE TABLE IF NOT EXISTS passwords (id INTEGER PRIMARY KEY, website TEXT, username TEXT, password TEXT)")
        self.conn.commit()

    def add_password(self):
        website = self.website_entry.get()
        username = self.username_entry.get()
        password = self.password_entry.get()

        self.cursor.execute("INSERT INTO passwords (website, username, password) VALUES (?, ?, ?)",
                            (website, username, password))
        self.conn.commit()

        self.website_entry.delete(0, tk.END)
        self.username_entry.delete(0, tk.END)
        self.password_entry.delete(0, tk.END)

    def display_passwords(self):
        for row in self.tree.get_children():
            self.tree.delete(row)

        self.cursor.execute("SELECT * FROM passwords")
        data = self.cursor.fetchall()

        for row in data:
            self.tree.insert("", "end", values=row)

    def remove_password(self):
        selected_item = self.remove_tree.selection()

        if not selected_item:
            return

        item = self.remove_tree.item(selected_item)
        id = item["values"][0]

        self.cursor.execute("DELETE FROM passwords WHERE id=?", (id,))
        self.conn.commit()
        self.display_passwords()

    def run(self):
        self.window.mainloop()

if __name__ == "_main_":
    app = PasswordManagerGUI()
    app.run()
