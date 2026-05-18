import { loadConfig } from "./shared/config/config.js";
import { initOtel } from "./shared/config/otel.js";

// 1. Initialize configuration and OpenTelemetry immediately.
// No application code or framework modules can be imported above this line.
const config = loadConfig();
const otelSDK = initOtel(config.otel);

// 2. Dynamically import the application logic.
// This guarantees that Express/HTTP modules are loaded AFTER OTel hooks are ready.
const { App } = await import("./hive/app.js");

const hiveApp = new App(config);
const app = await hiveApp.bootstrap();

const PORT = config.server.port;
const server = app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});

async function gracefulShutdown() {
    console.log("Shutting down...");
    server.close(async () => {
        console.log("HTTP server closed.");
        try {
            await hiveApp.shutdown();
            console.log("Database connections closed.");
            if (otelSDK) {
                await otelSDK.shutdown();
                console.log("OpenTelemetry SDK shut down.");
            }
            process.exit(0);
        } catch (err) {
            console.error("Error during shutdown:", err);
            process.exit(1);
        }
    });

    // Force shutdown if it takes too long
    setTimeout(() => {
        console.error("Could not close connections in time, forcefully shutting down");
        process.exit(1);
    }, 10000);
}

process.on("SIGTERM", gracefulShutdown);
process.on("SIGINT", gracefulShutdown);
