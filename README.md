# Monster Loco Tracker 🥤

A Spring Boot 3 + Vue 3 application for tracking Monster Energy drink prices from Maudau.

## Features

- **Price Tracking**: Automatically scrape and track prices from Maudau
- **Price History**: View historical price data with charts
- **Special Mango Loco Badge**: Featured highlighting for the Mango Loco flavor
- **Dark Theme**: Monster Energy themed UI with Mango Orange and Monster Green colors

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- SQLite Database
- Jsoup (Web Scraping)

### Frontend
- Vue 3
- Vite
- Tailwind CSS
- Chart.js / Vue-ChartJS
- Axios

## Project Structure

```
monster-loco-tracker/
├── pom.xml                          # Maven configuration
├── src/
│   └── main/
│       ├── java/com/monsterloco/
│       │   ├── MonsterLocoTrackerApplication.java
│       │   ├── config/
│       │   │   └── DataInitializer.java
│       │   ├── controller/
│       │   │   └── DrinkController.java
│       │   ├── model/
│       │   │   ├── MonsterDrink.java
│       │   │   └── PriceHistory.java
│       │   ├── repository/
│       │   │   ├── MonsterDrinkRepository.java
│       │   │   └── PriceHistoryRepository.java
│       │   └── service/
│       │       ├── DrinkService.java
│       │       └── ScraperService.java
│       └── resources/
│           └── application.properties
└── frontend/
    ├── package.json
    ├── vite.config.js
    ├── tailwind.config.js
    ├── postcss.config.js
    ├── index.html
    └── src/
        ├── main.js
        ├── style.css
        ├── App.vue
        ├── components/
        │   ├── DrinkCard.vue
        │   └── PriceHistoryModal.vue
        └── services/
            └── api.js
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 18+
- npm or yarn

### Running the Backend

```bash
cd monster-loco-tracker

# Build the project
mvn clean install

# Run the Spring Boot application
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Running the Frontend

```bash
cd monster-loco-tracker/frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will start on `http://localhost:5173`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/drinks` | Get all drinks with latest prices |
| GET | `/api/drinks/{id}` | Get drink by ID |
| GET | `/api/drinks/{id}/history` | Get price history for a drink |
| POST | `/api/sync` | Trigger price sync from Maudau |
| GET | `/api/health` | Health check |

## Tracked Products

1. **Mango Loco** (Featured) - `MD_763227`
2. **Juiced Monarch** - `MD_MONARCH`
3. **Original** - `MD_ORIGINAL`
4. **Ultra White** - `MD_ULTRA_WHITE`

## Configuration

The application uses SQLite which is auto-created in the project root as `monster_loco.db`.

Key configuration in `application.properties`:
- `server.port=8080` - Backend server port
- `spring.datasource.url=jdbc:sqlite:monster_loco.db` - SQLite database path

## Notes

- The scraper uses specific CSS selectors for Maudau's website structure
- Prices are stored in Ukrainian Hryvnia (₴)
- The first price sync should be triggered manually via the "Sync Prices" button
