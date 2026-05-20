export class User {
    constructor(
        public id: number,
        public username: string,
        public email: string,
        public password: string, // Hashed password
        public bio: string,
        public image: string | null,
    ) {}

    update(params: {
        username?: string;
        email?: string;
        bio?: string;
        image?: string | null;
        password?: string;
    }) {
        if (params.username !== undefined) this.username = params.username;
        if (params.email !== undefined) this.email = params.email;
        if (params.bio !== undefined) this.bio = params.bio;
        if (params.image !== undefined) this.image = params.image;
        if (params.password !== undefined) this.password = params.password;
    }
}

export interface Profile {
    username: string;
    bio: string;
    image: string | null;
    following: boolean;
}
