# NutriSense AI

NutriSense AI is an Android application designed to make nutrition tracking frictionless. By combining real-time on-device computer vision with generative AI, it identifies meals and estimates nutritional data directly from a camera feed.

## Core Features

*   **Real-time Scanning:** Uses Google ML Kit to identify food items in the camera viewfinder with live bounding box overlays.
*   **AI-Powered Analysis:** Leverages the Gemini API (Multimodal) to analyze cropped images and provide detailed breakdowns of calories, macros (P/C/F), and dietary flags.
*   **Intelligent Cropping:** The app automatically crops the image to the detected food region before sending it to the cloud to improve accuracy and reduce bandwidth.
*   **Daily Dashboard:** Track daily totals against goals and view meal history.
*   **Offline Support:** Stores all history and nutrition data locally using Room.

## Technical Stack

*   **Language:** Kotlin
*   **UI:** Jetpack Compose (100%)
*   **Camera:** CameraX
*   **Machine Learning:** ML Kit (Object Detection & Tracking)
*   **Generative AI:** Google Gemini AI SDK
*   **Architecture:** Clean Architecture + MVVM
*   **DI:** Dagger Hilt
*   **Database:** Room
*   **Asynchrony:** Kotlin Coroutines & Flow

## How it Works

The app employs a two-tier analysis pipeline:

1.  **On-Device (The "Spotter"):** ML Kit runs locally on the device to detect objects in the stream. This provides the low-latency visual feedback (green boxes) and determines the "Region of Interest."
2.  **Cloud (The "Expert"):** Once the user captures a photo, the app crops it to the ML Kit coordinates and sends only the relevant snippet to the Gemini API. This ensures the AI model focuses on the actual food, leading to much more accurate caloric estimations.

## Setup

1.  Clone this repository.
2.  Obtain an API Key from the [Google AI Studio](https://aistudio.google.com/).
3.  Add the key to your `local.properties` file:
    ```properties
    GEMINI_API_KEY=your_api_key_here
    ```
4.  Build and run using Android Studio.

## Architecture Overview

The codebase is organized into three layers:
*   **data:** Repository implementations, Room database, and API data sources.
*   **domain:** Pure business logic, interfaces, and data models.
*   **presentation:** UI components, ViewModels, and navigation logic.
