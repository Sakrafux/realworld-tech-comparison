import { vi } from "vitest";
import type { ArticleRepository, UserProvider } from "../../cells/article/ports.js";

export const mockArticleRepository = (): any => ({
    create: vi.fn(),
    getBySlug: vi.fn(),
    getByTitle: vi.fn(),
    findAll: vi.fn(),
    findFeed: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    favorite: vi.fn(),
    unfavorite: vi.fn(),
    findAllTags: vi.fn(),
});

export const mockUserProvider = (): any => ({
    getUser: vi.fn(),
    getUserByUsername: vi.fn(),
});
