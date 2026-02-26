
#  Tweet Archive

A Spring Boot application that imports your Twitter/X archive into a PostgreSQL database, analyzes your tweets using Grok AI, and exposes everything through a secure REST API.

---

##  Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [How It Works](#how-it-works)
- [API Endpoints](#api-endpoints)
- [CI/CD Pipeline](#cicd-pipeline)

---

## Overview

Twitter/X allows you to download your entire tweet history as an archive. This app takes that archive, processes the `tweet.json` file, and gives your data a new home — stored in PostgreSQL, enriched with AI analysis via Grok, and accessible through a secure JWT-authenticated API.

---

##  Features

- **Twitter Archive Import** — Upload your `tweet.json` file from your Twitter data export
- **S3 Upload** — Archive file is securely uploaded to an AWS S3 bucket before processing
- **Batch Processing** — Spring Batch handles the heavy lifting of reading and persisting tweet data to the database
- **AI Analysis** — Tweets are analyzed using Grok AI (Google GenAI SDK) for insights and summaries
- **JWT Authentication** — Secure API access using JSON Web Tokens
- **Email Notifications** — Notify users on batch job completion via Spring Mail + Thymeleaf templates
- **Input Validation** — Request validation with Spring Validation
- **CI/CD** — Automated build, test, and deployment pipeline

---

##  Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.1 |
| Language | Java 17 |
| Database | PostgreSQL |
| ORM | Spring Data JPA |
| Batch Processing | Spring Batch 6.0.1 |
| Cloud Storage | AWS S3 (SDK 1.12.x) |
| AI Analysis | Grok (Google GenAI SDK 1.0.0) |
| Security | Spring Security + JJWT 0.12.6 |
| Email | Spring Mail + Thymeleaf |
| Serialization | Jackson Databind |
| Boilerplate | Lombok |

---

##  Architecture

```
Twitter Archive (.zip)
        │
        ▼
   tweet.json
        │
        ▼
  REST API (Upload)
        │
        ├──► AWS S3 Bucket (raw file storage)
        │
        ▼
  Spring Batch Job
   ┌────────────────┐
   │  ItemReader    │  ← Reads tweet.json from S3
   │  ItemProcessor │  ← Transforms tweet data
   │  ItemWriter    │  ← Writes to PostgreSQL
   └────────────────┘
        │
        ▼
   PostgreSQL DB
        │
        ▼
   Grok AI Analysis
        │
        ▼
   REST API (Query & Insights)
```

---

##  Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL database
- AWS account with an S3 bucket
- Grok / Google GenAI API key

### Clone the Repository

```bash
git clone https://github.com/your-username/tweetArchive.git
cd tweetArchive
```

### Configure Environment

Copy the example config and fill in your values:

```bash
cp src/main/resources/application.example.properties src/main/resources/application.properties
```

See the [Configuration](#configuration) section for all required properties.

### Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

---

##  Configuration

Set the following in `application.properties` or as environment variables:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/tweet_archive
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# AWS S3
aws.s3.bucket-name=your-s3-bucket
aws.access-key-id=your-access-key
aws.secret-access-key=your-secret-key
aws.region=us-east-1

# JWT
jwt.secret=your-jwt-secret
jwt.expiration-ms=86400000

# Grok / GenAI
genai.api-key=your-grok-api-key

# Mail
spring.mail.host=smtp.your-provider.com
spring.mail.port=587
spring.mail.username=your-email@example.com
spring.mail.password=your-email-password
```

>  **Never commit secrets to version control.** Use environment variables or a secrets manager in production.

---

##  How It Works

1. **Download your Twitter archive** from [Twitter Settings → Your Account → Download an archive of your data](https://twitter.com/settings/download_your_data)
2. **Extract** the archive and locate `data/tweet.json`
3. **Upload** `tweet.json` via the API — the file is stored in your configured S3 bucket
4. **Spring Batch** picks up the job, reads tweets from S3, and persists them to PostgreSQL
5. **Grok AI** analyzes your tweets — surfacing topics, sentiment, patterns, and more
6. **Query** your tweet history and AI insights through the REST API

---

##  API Endpoints

> All endpoints require a valid JWT `Authorization: Bearer <token>` header unless stated otherwise.

###  Auth

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `POST` | `/v1/api/auth/register` | No | Register a new user |
| `POST` | `/v1/api/auth/login` | No | Authenticate and receive a JWT token |

###  S3 Upload

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `POST` | `/v1/api/s3/upload` | Yes | Upload your `tweet.json` file to S3. The file is keyed per user (`{userId}_tweet.json`) |

###  Tweets

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `POST` | `/v1/api/tweets/upload-job` | Yes | Trigger Spring Batch job to import tweets from S3 into the database |
| `POST` | `/v1/api/tweets/analysis-job` | Yes | Trigger Grok AI analysis job with custom evaluation criteria |
| `GET` | `/v1/api/tweets` | Yes | Get all imported tweets (paginated). Params: `page`, `size`, `sort` |
| `GET` | `/v1/api/tweets/flagged` | Yes | Get tweets flagged by AI analysis (paginated). Params: `page`, `size`, `sort` |
| `GET` | `/v1/api/tweets/criteria` | Yes | List all saved evaluation criteria |
| `DELETE` | `/v1/api/tweets/{id}` | Yes | Delete a specific tweet by ID |

### Pagination Query Parameters

All paginated endpoints (`/tweets`, `/tweets/flagged`) accept:

| Param | Default | Description |
|---|---|---|
| `page` | `0` | Page number (zero-indexed) |
| `size` | `20` | Number of results per page |
| `sort` | `desc` | Sort order (`asc` or `desc`) |

### Analysis Job Request Body

`POST /v1/api/tweets/analysis-job` expects a JSON body:

```json
{
  "criteriaName": "Toxicity Check",
  "criteriaList": ["hate speech", "profanity", "harassment"]
}
```

If a criteria set with the same name already exists for the user, the existing one is reused.

---

##  CI/CD Pipeline

This project uses a CI/CD pipeline that automates:

- **Build** — Compiles the project with Maven on every push
- **Test** — Runs unit and integration tests
- **Docker Build** — Packages the app as a Docker image
- **Deploy** — Deploys to the target environment on merges to `main`

Pipeline configuration can be found in `.github/workflows/` (GitHub Actions) or your equivalent CI config file.

---
