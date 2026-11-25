# StockMaster - Inventory Management System

A beginner-friendly Java Swing desktop application for small businesses.

## Features
- Admin and employee demo login
- Add, update, search and delete products
- Manage quantities and low-stock thresholds
- Record sales and automatically deduct stock
- Low-stock alerts
- Daily revenue dashboard
- Sales history
- Local data persistence using Java serialization

## Requirements
- JDK 8 or later
- Apache NetBeans with Maven support

## Open in NetBeans
1. Extract the `StockMaster` folder from the ZIP.
2. In NetBeans choose **File > Open Project**.
3. Select the extracted `StockMaster` folder (the one containing `pom.xml`).
4. Wait for NetBeans to load the Maven project.
5. Expand **Source Packages > stockmaster**.
6. Right-click `Main.java` and choose **Run File**.

## Demo accounts
- Admin: `admin` / `admin123`
- Employee: `employee` / `emp123`

Both demo accounts can use the application modules. The role is shown in the window header.

## Data storage
The application creates `stockmaster-data.ser` in the working directory when it saves data. Keep this file if you want to retain products and sales between sessions.

## Notes
This is a local desktop learning project. It does not use MySQL or XAMPP.
