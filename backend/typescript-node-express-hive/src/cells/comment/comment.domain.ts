export interface Author {
    username: string;
    bio: string;
    image: string | null;
    following: boolean;
}

export class Comment {
    constructor(
        public id: number,
        public createdAt: Date,
        public updatedAt: Date,
        public body: string,
        public author: Author,
    ) {}
}
