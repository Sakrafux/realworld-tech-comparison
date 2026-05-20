import { loadConfig } from "./shared/config/config.js";

const config = loadConfig();

const { App } = await import("./hive/app.js");

const hiveApp = new App(config);
const app = await hiveApp.bootstrap();

const PORT = config.server.port;
const server = Bun.serve({
    port: parseInt(PORT),
    fetch: app.fetch,
});

console.log(`Server is running on http://localhost:${PORT}`);

async function gracefulShutdown() {
    console.log("Shutting down...");
    server.stop(true);
    console.log("HTTP server stopped.");
    try {
        await hiveApp.shutdown();
        console.log("Database connections closed.");
        process.exit(0);
    } catch (err) {
        console.error("Error during shutdown:", err);
        process.exit(1);
    }
}

process.on("SIGTERM", gracefulShutdown);
process.on("SIGINT", gracefulShutdown);
