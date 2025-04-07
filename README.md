# 🎉 Role-Based Event Management App (Android)

An Android application for managing events with role-based access control. Built using **Java** in **Android Studio**, the app supports four distinct user roles with tailored features and dashboards.

## 🔑 User Roles

1. **Admin**
   - Create, edit, and delete events
   - Manage users and roles
   - View full platform analytics

2. **Manager**
   - Manage sessions and speakers
   - Check-in attendees via QR code
   - Upload resources and announcements

3. **Attendee**
   - Register for and view upcoming events
   - Scan tickets for check-in
   - View session schedule and speaker details

## ☁️ Backend & Services

- **Firebase Authentication** – Secure user sign-up and sign-in
- **Cloud Firestore** – Real-time database for storing events, users, sessions
- **Firebase Storage** – Store files like event posters, QR codes, and session materials

## 📱 Tech Stack

| Layer         | Technology           |
|---------------|----------------------|
| Language      | Java                 |
| IDE           | Android Studio       |
| UI Design     | XML Layouts + Material Design |
| Backend       | Firebase Auth + Firestore |
| Storage       | Firebase Storage     |
| Build Tool    | Gradle               |

## 🚀 Key Features

- 🔒 Role-based login and dashboard redirection
- 📆 Event creation and participation
- 📲 QR code-based check-in
- 🗓️ Session scheduling and speaker management
- 🖼️ Upload/view images and resources
- 📊 Real-time data sync using Firestore
- 🔔 Notifications and announcements
