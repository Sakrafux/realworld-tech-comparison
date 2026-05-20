import { z } from "zod";

export const loginSchema = z.object({
    user: z.object({
        email: z.email(),
        password: z.string().min(1),
    }),
});

export const registerSchema = z.object({
    user: z.object({
        username: z.string().min(3).max(50),
        email: z.email().max(100),
        password: z.string().min(8).max(60),
    }),
});

export const updateUserSchema = z.object({
    user: z.object({
        username: z.string().min(3).max(50).optional(),
        email: z.email().max(100).optional(),
        password: z.string().min(8).max(60).optional(),
        bio: z.string().optional(),
        image: z.url().nullable().optional(),
    }),
});
