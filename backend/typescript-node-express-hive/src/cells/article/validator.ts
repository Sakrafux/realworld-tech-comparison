import { z } from "zod";

export const createArticleSchema = z.object({
    article: z.object({
        title: z.string().min(1).max(200),
        description: z.string().min(1).max(500),
        body: z.string().min(1),
        tagList: z.array(z.string().max(20)).optional().default([]),
    }),
});

export const updateArticleSchema = z.object({
    article: z.object({
        title: z.string().max(200).optional(),
        description: z.string().max(500).optional(),
        body: z.string().optional(),
    }),
});
