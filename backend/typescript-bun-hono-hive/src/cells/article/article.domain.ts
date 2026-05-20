export interface Author {
    username: string;
    bio: string;
    image: string | null;
    following: boolean;
}

export interface Tag {
    name: string;
}

export class Article {
    constructor(
        public id: number,
        public slug: string,
        public title: string,
        public description: string,
        public body: string,
        public tagList: string[],
        public createdAt: Date,
        public updatedAt: Date,
        public favorited: boolean,
        public favoritesCount: number,
        public author: Author,
    ) {}

    async update(
        params: {
            title?: string;
            description?: string;
            body?: string;
        },
        checkDuplicate: (title: string, slug: string) => Promise<void>,
    ) {
        if (params.title !== undefined && params.title !== this.title) {
            const newTitle = params.title;
            const newSlug = slugify(newTitle);
            await checkDuplicate(newTitle, newSlug);
            this.title = newTitle;
            this.slug = newSlug;
        }

        if (params.description !== undefined) {
            this.description = params.description;
        }

        if (params.body !== undefined) {
            this.body = params.body;
        }

        this.updatedAt = new Date();
    }
}

export function slugify(title: string): string {
    return title.toLowerCase().replace(/\s+/g, "-");
}
