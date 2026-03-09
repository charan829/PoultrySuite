# PoultrySuite Setup Guide (For Your Friend)

This guide will help you set up and run the **PoultrySuite** project on your laptop.

## 1. Prerequisites (Install these first)
Before you start, make sure you have these installed:
*   **Node.js (v18 or newer)**: [Download here](https://nodejs.org/)
*   **PostgreSQL**: [Download here](https://www.postgresql.org/) (Remember the password you set during installation!)
*   **Android Studio**: [Download here](https://developer.android.com/studio)
*   **Java JDK 17**: Android Studio usually comes with this, but it's good to have.

---

## 2. Setting Up the Backend (Database & Server)
1.  **Extract the Files**: Unzip the folder you received.
2.  **Create the Database**:
    *   Open **pgAdmin 4** (it comes with PostgreSQL).
    *   Right-click `Databases` -> `Create` -> `Database...`.
    *   Name it `poultrysuite` and click Save.
3.  **Configure the Server**:
    *   Open the `backend` folder.
    *   Create a file named `.env` and paste this (replace `YOUR_PASSWORD` with your Postgres password):
        ```env
        DATABASE_URL="postgresql://postgres:YOUR_PASSWORD@localhost:5432/poultrysuite?schema=public"
        JWT_SECRET="poultry-suite-secret-key-2024"
        ```
4.  **Run the Server**:
    *   Open a terminal (CMD or PowerShell) in the `backend` folder.
    *   Type these commands one by one:
        ```bash
        npm install
        npx prisma generate
        npx prisma db push
        npm start
        ```
    *   Keep this terminal open! You should see "Server running on port 3000".

---

## 3. Running the Android App
1.  Open **Android Studio**.
2.  Click **Open** and select the main project folder (the one containing `app`, `backend`, etc.).
3.  Wait for the "Gradle Sync" to finish (check the progress bar at the bottom).
4.  **Connect your phone** via USB and enable "USB Debugging" in your phone's developer settings.
5.  **Fix the Connection**:
    *   Open a new terminal on your laptop and type:
        ```bash
        adb reverse tcp:3000 tcp:3000
        ```
        *(This makes the phone talk to your laptop's server easily.)*
6.  Click the **Green Play Arrow** at the top of Android Studio to run the app!

---

## 4. Troubleshooting
*   **App says "Connection Error"**: Make sure the backend server (Step 2.4) is running and you ran the `adb reverse` command.
*   **Database Error**: Ensure your password in the `.env` file is exactly what you set during PostgreSQL installation.
