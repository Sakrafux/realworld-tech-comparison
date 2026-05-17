import express, { type Express, type Request, type Response } from "express";

const app: Express = express();

app.use(express.json());

app.get("/", (req: Request, res: Response) => {
    res.json({ message: "Hello World from Express, TypeScript, and pnpm!" });
});

export default app;
