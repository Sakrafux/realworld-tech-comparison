import { App } from "./hive/app.js";

const hiveApp = new App();
const app = await hiveApp.bootstrap();

const PORT = process.env.PORT || 8080;

app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
