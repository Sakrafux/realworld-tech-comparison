import { describe, it, expect } from 'vitest';
import { apiClient } from '../utils/apiClient';

interface TagsResponse {
    tags: string[];
}

describe('Tags API', () => {
    it('GET /tags should return a list of tags', async () => {
        const response = await apiClient.get<TagsResponse>('/tags');
        
        expect(response.status).toBe(200);
        expect(response.data).toHaveProperty('tags');
        expect(Array.isArray(response.data.tags)).toBe(true);
    });
});
