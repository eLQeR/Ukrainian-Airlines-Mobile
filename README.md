# Ukrainian Airlines Mobile App

A comprehensive Android application for Ukrainian Airlines built with Kotlin and modern Android development practices.

## Features

- **User Authentication**: JWT-based login and registration with automatic token refresh
- **Flight Search**: Search for direct and transfer flights between airports
- **Booking Management**: Create, view, and cancel flight bookings
- **User Profile**: View and manage user account information
- **Material Design 3**: Modern UI with intuitive navigation

## Architecture

- **MVVM Pattern**: Clean separation of concerns with ViewModels and LiveData
- **Repository Pattern**: Centralized data management with API and local storage
- **Retrofit**: HTTP client for API communication
- **Navigation Component**: Single-activity architecture with fragment navigation
- **SharedPreferences**: Secure token storage with automatic refresh

## Technical Stack

- **Language**: Kotlin
- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: MVVM with Repository pattern
- **Networking**: Retrofit 2 with OkHttp
- **UI**: Material Design 3, RecyclerView, Navigation Component
- **Dependency Injection**: Manual dependency injection
- **Build System**: Gradle with Kotlin DSL

## Key Components

### Authentication

- JWT token management with automatic refresh
- Secure token storage using SharedPreferences
- Token expiration handling

### Flight Search

- Airport selection with autocomplete
- Direct and transfer flight options
- Real-time flight availability

### Booking System

- Order creation and management
- Payment integration ready
- Order history and cancellation

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Build and run on device/emulator
4. Register or login to access features

## API Integration

The app integrates with the Ukrainian Airlines backend API for:

- User authentication
- Airport and route data
- Flight search and booking
- Order management

## Development

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK API 21+

### Building

```bash
./gradlew assembleDebug
```

### Testing

```bash
./gradlew test
```

## Contributing

This repository contains the mobile application component of the Ukrainian Airlines system. For backend contributions, see the main [Ukrainian-Airlines](https://github.com/eLQeR/Ukrainian-Airlines) repository.

## License

This project is part of the Ukrainian Airlines system.
