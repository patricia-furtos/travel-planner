# 🌍 TravelPlanner — Collaborative Group Trip & Budget Manager

A full-stack web application designed for planning group vacations, building day-by-day itineraries, and splitting shared expenses effortlessly.

## ✨ Key Features

- **Trip Collaboration & Invites:** Create trips and invite friends via unique invite codes or links (Airbnb-style access).
- **Day-by-Day Itinerary:** Organize activities, locations, and schedules with interactive map integration.
- **Group Budget Sharing (Splitwise-like):** Track shared expenses, automatically convert foreign currencies, and calculate simplified debt settlements ("who owes whom").
- **Group Decision Polls:** Vote on accommodations, restaurants, and activities.
- **Shared Checklist & Documents:** Collaborative packing lists and storage for flight/accommodation tickets.

## 🛠️ Tech Stack

- **Frontend:** Next.js (App Router), TypeScript, Tailwind CSS, shadcn/ui
- **Backend:** Java 21, Spring Boot, Spring Data JPA (Hibernate)
- **Database & Auth:** PostgreSQL (Supabase), Supabase Auth & Storage
## 🚀 Local Setup Instructions

### Prerequisites
- **Java 21** (JDK 21)
- **Node.js 18+**
- A **Supabase** project (for PostgreSQL database & authentication)

## 📂 Project Structure
```text
travel-planner/
├── backend/    # Java Spring Boot REST API
└── frontend/   # Next.js TypeScript Web Application

### 1. Clone the repository
```bash
git clone [https://github.com/USERNAME/travel-planner.git](https://github.com/USERNAME/travel-planner.git)
cd travel-planner
```

### 2. Backend Setup (Spring Boot)
1. Navigate to the backend resources folder:
   ```bash
   cd backend/src/main/resources
   ```
2. Copy `application.properties.example` to `application.properties`:
   ```bash
   cp application.properties.example application.properties
   ```
3. Open `application.properties` and replace `PAROLA_TA_AICI` with your Supabase database password.
4. Run the Spring Boot application (`BackendApplication.java`) — the API will start on `http://localhost:8080`.

### 3. Frontend Setup (Next.js)
1. Navigate to the frontend folder and install dependencies:
   ```bash
   cd frontend
   npm install
   ```
2. Create a `.env.local` file in the `frontend/` directory and add your Supabase credentials:
   ```env
   NEXT_PUBLIC_SUPABASE_URL=your_supabase_project_url
   NEXT_PUBLIC_SUPABASE_ANON_KEY=your_supabase_publishable_key
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
4. Open `http://localhost:3000` in your browser.

---

## 👥 Authors

- **Patricia Fürtös** — Full-Stack Development
- **Alexandru-Florin Dipșan** — Full-Stack Development
