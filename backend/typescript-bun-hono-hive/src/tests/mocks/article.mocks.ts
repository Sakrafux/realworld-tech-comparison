import { mock } from "bun:test";

export const mockArticleRepository = (): any => ({
    create: mock(),
    getBySlug: mock(),
    getByTitle: mock(),
    findAll: mock(),
    findFeed: mock(),
    update: mock(),
    delete: mock(),
    favorite: mock(),
    unfavorite: mock(),
    findAllTags: mock(),
});

export const mockUserProvider = (): any => ({
    getUser: mock(),
    getUserByUsername: mock(),
});
