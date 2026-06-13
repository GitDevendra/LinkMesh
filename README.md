# 🔗 Link Mesh

A **production-grade URL shortening service** with custom aliases, configurable TTLs, real-time click tracking, and a premium tier — built with Spring Boot, Redis, ZooKeeper, and Docker.

---

## ✨ Features

- **Custom Short URLs** — Generate compact 7-character codes via Snowflake ID + Base62 encoding
- **Custom Aliases** — Users can define their own short code
- **Configurable TTLs** — Set expiry duration per link
- **Real-Time Click Tracking** — Monitor redirects and analytics per URL
- **FREE / PREMIUM Tiers** — Role-based access control with Razorpay payment integration for upgrades
- **OAuth 2.0 Login** — Secure authentication via social login
- **Sub-millisecond Redirects** — Redis cache layer for near-instant URL resolution
- **Auto Expiry Cleanup** — Nightly cron job purges expired URLs from MySQL
- **Fully Containerized** — One-command setup with Docker Compose

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot |
| Security | Spring Security, OAuth 2.0, JWT |
| Cache | Redis |
| Database | MySQL |
| Distributed ID | Apache ZooKeeper (Curator) + Snowflake Algorithm |
| Payments | Razorpay |
| Frontend | JavaScript, HTML, CSS |
| DevOps | Docker, Docker Compose |
| Scheduling | Spring Cron Job |

---

## 🏗️ Architecture Highlights

### Snowflake ID Generation
Worker IDs are assigned via **ZooKeeper ephemeral nodes**, ensuring uniqueness across instances. IDs are Base62-encoded to produce compact 7-character short codes.

### Redis Caching
Every URL resolution hits Redis first for sub-millisecond response times. Cache entries are evicted precisely on TTL expiry — no stale data, no full cache flushes.

### FREE / PREMIUM Enforcement
Users start on the FREE tier with usage limits. A Razorpay payment flow upgrades them to PREMIUM, unlocking custom aliases, longer TTLs, and higher usage quotas.

### Nightly Cleanup
A scheduled cron job runs every night to purge expired URLs from MySQL, keeping the database lean without manual intervention.

---

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose installed

### Run Locally

```bash
git clone https://github.com/GitDevendra/LinkMesh
cd Link-Mesh
docker-compose up --build
```

The full stack (Spring Boot + MySQL + Redis + ZooKeeper) will start automatically.

### Environment Variables

Create a `.env` file in the `Backend/` directory:

```env
# Database
DB_URL=jdbc:mysql://mysql:3306/linkmesh
DB_USERNAME=root
DB_PASSWORD=your_password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# OAuth 2.0
OAUTH2_GOOGLE_CLIENT_ID=your_google_client_id
OAUTH2_GOOGLE_CLIENT_SECRET=your_google_client_secret

# JWT
JWT_SECRET=your_jwt_secret

# Razorpay
RAZORPAY_KEY_ID=your_razorpay_key
RAZORPAY_KEY_SECRET=your_razorpay_secret
```

---

## 📁 Project Structure

```
Link-Mesh/
├── Backend/          # Spring Boot application
│   ├── src/
│   ├── Dockerfile
│   └── ...
├── frontend/         # JavaScript frontend
│   └── ...
└── docker-compose.yml
```

---

## ⭐ Show Your Support

If you found this project interesting, please consider giving it a ⭐ on GitHub!
# LinkMesh
