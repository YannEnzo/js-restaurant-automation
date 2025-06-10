# J's Restaurant Management System 🍽️

A comprehensive JavaFX-based restaurant automation system designed to streamline operations, improve service quality, and provide management insights.

## 🌟 Features

- **👤 Multi-Role Authentication**: Secure access for Managers, Waiters, Cooks, and Busboys
- **🪑 Real-Time Table Management**: Live status updates (Available, Occupied, Needs Cleaning)
- **📋 Digital Order Processing**: Streamlined order entry and kitchen queue management
- **💳 Payment Integration**: Support for cash and credit card transactions
- **📊 Analytics Dashboard**: Sales reports, inventory tracking, and performance metrics
- **🔄 Live Updates**: Real-time synchronization across all user interfaces

## 🛠️ Tech Stack

- **Frontend**: JavaFX with FXML
- **Backend**: Java 8+
- **Database**: MySQL 8.0
- **Architecture**: MVC Pattern with DAO
- **Build Tool**: Maven (if applicable)

## 🚀 Quick Start

### Prerequisites

```bash
# Java Development Kit 8+
java -version

# MySQL Server 8.0+
mysql --version
```

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/js-restaurant-system.git
   cd js-restaurant-system
   ```

2. **Database Setup**
   ```bash
   # Create database
   mysql -u root -p
   CREATE DATABASE js_restaurant;
   
   # Import schema
   mysql -u root -p js_restaurant < database/schema.sql
   ```

3. **Configure Database Connection**
   ```java
   // Update DAO/DatabaseManager.java
   private static final String DB_URL = "jdbc:mysql://localhost:3306/js_restaurant";
   private static final String USER = "your_username";
   private static final String PASS = "your_password";
   ```

4. **Run the Application**
   ```bash
   # Using JAR file
   java -jar js-restaurant.jar
   
   # Or compile and run
   javac -cp ".:lib/*" *.java
   java -cp ".:lib/*" Main
   ```

## 👥 Default Login Credentials

| Role | Username | Password |
|------|----------|----------|
| Manager | `man1` | `manager00` |
| Waiter | `wait1` | `waiter00` |
| Cook | `bbb` | `bbb` |
| Busboy | `aaa` | `aaa` |

## 📁 Project Structure

```
js-restaurant-system/
├── src/
│   ├── Controllers/          # UI Controllers
│   ├── DAO/                 # Data Access Objects
│   ├── Model/               # Business Logic & Models
│   ├── Views/               # FXML UI Files
│   └── Resources/           # Images & Assets
├── database/
│   └── schema.sql          # Database Schema
├── docs/                   # Project Documentation
├── lib/                    # External Libraries
└── README.md
```

## 🎯 System Requirements

### Minimum Requirements
- **OS**: Windows 10, macOS 10.14, or Linux
- **RAM**: 4GB
- **Storage**: 500MB free space
- **Display**: 1280x720 resolution

### Recommended
- **RAM**: 8GB+
- **Display**: 1920x1080 resolution

## 📱 Screenshots

<div align="center">

### Login Screen
![image](https://github.com/user-attachments/assets/6d4ecc6a-1c89-4382-bc45-c88aba82ca6d)

### Manager Dashboard
![image](https://github.com/user-attachments/assets/fa699c0d-6fc9-4040-874c-331012eb3971)

### Order Management
![image](https://github.com/user-attachments/assets/32974600-892b-452f-bb60-83e43908cdd9)


</div>

## 🔧 Development

### Setting up Development Environment

1. **Import into IDE** (IntelliJ IDEA, Eclipse, NetBeans)
2. **Configure JavaFX** in your IDE
3. **Set up MySQL** connection
4. **Run** `Main.java`

### Building from Source

```bash
# Compile all Java files
find . -name "*.java" > sources.txt
javac -cp ".:lib/*" @sources.txt

# Create JAR
jar cfm js-restaurant.jar MANIFEST.MF *.class
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Authors

- **Team Syntax Synergy** - *Initial work*

## 🐛 Known Issues

- [ ] Table status may occasionally require manual refresh
- [ ] Payment processing simulation only
- [ ] Reports export functionality in development

## 🔮 Future Enhancements

- [ ] Mobile app integration
- [ ] Cloud database support
- [ ] Advanced analytics with charts
- [ ] Inventory management automation
- [ ] Multi-restaurant support


## 🙏 Acknowledgments

- JavaFX community for excellent documentation
- MySQL team for robust database solutions
- All contributors and testers

---

<div align="center">

**⭐ Star this repository if you found it helpful!**


</div>
