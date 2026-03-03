
const { Client } = require('pg');
require('dotenv').config();

const client = new Client({
    connectionString: process.env.DATABASE_URL,
});

async function testConnection() {
    try {
        console.log("Attempting to connect to:", process.env.DATABASE_URL);
        await client.connect();
        console.log("Connected successfully!");
        const res = await client.query('SELECT NOW()');
        console.log("Database time:", res.rows[0]);
        await client.end();
    } catch (err) {
        console.error("Connection error:", err);
    }
}

testConnection();
